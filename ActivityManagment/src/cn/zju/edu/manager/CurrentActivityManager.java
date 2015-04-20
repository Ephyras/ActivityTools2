package cn.zju.edu.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.util.DateUtil;
import cn.zju.edu.util.CommonUtil;
import cn.zju.edu.util.InteractionUtil;
import cn.zju.edu.blf.db.*;
import de.jaret.util.date.JaretDate;
import cn.zju.edu.blf.dao.*;

public class CurrentActivityManager {
	private List<LowLevelInteraction> interactions = new ArrayList<LowLevelInteraction>();
	private HashMap<String, ActivityObject> map = new HashMap<String, ActivityObject>();
	//private Set<ActivityObject> activities = new TreeSet<ActivityObject>();
	private DBImpl db;
	private String lastTime;
	private String user;
	private int interval = 30;
	
	public CurrentActivityManager() throws Exception
	{
		user = System.getProperty("user.name");
		db = new DBImpl();
		lastTime = DateUtil.getBeforeTime(30);
	}
	
	public void addInteractions(List<LowLevelInteraction> list)
	{
		interactions.addAll(list);
		
		for(int i=0; i<list.size(); i++)
		{
			LowLevelInteraction u = list.get(i);
			
			if("iexplore.exe".equals(u.getApplication()))
			{
				System.out.println(u.getWindow() + "/" + u.getParentWindow());
			}
			
			if(InteractionUtil.filterApplicationForCurrent(u)) continue;
			
			String title = InteractionUtil.getInteractionTitle(u);
			
			if(map.containsKey(title))
			{
				map.get(title).setLastTime(u.getTimestamp());
			}
			else
			{
				ActivityObject a = new ActivityObject();
				a.setTitle(title);
				a.setApplication(u.getApplication());
				a.setLastTime(u.getTimestamp());
				map.put(title, a);
				
				//isAddNew = true;
			}
		}
		
		lastTime = interactions.get(interactions.size()-1).getTimestamp();
	}
	
	public boolean retrieveInteractions() throws Exception
	{
		String sql = "select * from tbl_interactions where user_name = '" + user + "'" 
				+ " and " + "timestamp > '" + lastTime + "'"
				+ " order by timestamp";
		
		List<LowLevelInteraction> list = db.getInteractions(sql);
		
		if(list.size() <= 0) return false;
		
		System.out.println("get new interactions: " + list.size());
		System.out.println("last time: " + lastTime);
		addInteractions(list);
		
		removeExpiredInteractions();
		
		return true;
	}
	
	public void removeExpiredInteractions() throws Exception
	{
	   Iterator<Entry<String, ActivityObject>> it = map.entrySet().iterator();
	   while (it.hasNext())
	   {
		   	Entry<String, ActivityObject> entry = it.next();
		   	ActivityObject a = entry.getValue();
			
			if(DateUtil.compareNow(a.getLastTime()) > interval * 60)
			{
				it.remove();
			}
	   }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ActivityObject> getOrderedMap()
	{
		List<ActivityObject> list = new LinkedList(map.values());
		
		Collections.sort(list);
		
		
		return list;
	}
	
	
}

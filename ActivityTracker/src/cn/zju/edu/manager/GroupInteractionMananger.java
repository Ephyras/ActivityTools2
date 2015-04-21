package cn.zju.edu.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.db.*;
import cn.zju.edu.util.DateUtil;
import cn.zju.edu.util.InteractionUtil;
import de.jaret.util.date.JaretDate;
import cn.zju.edu.blf.dao.*;

public class GroupInteractionMananger {
	private List<LowLevelInteraction> interactions = new ArrayList<LowLevelInteraction>();
	private List<List<Integer>> groups;
	private String user = System.getProperty("user.name");
	private DBImpl db;
	
	public GroupInteractionMananger() throws Exception
	{
		db = new DBImpl();
	}
	
	public void groupInteractions() throws Exception
	{
		String t = db.getMaxTimeOfHasGrouped(user);
		
		String sql = "select * from tbl_interactions where user_name = '" + user + "'";
		if(t!= null && !"".equals(t))
		{
			sql += " and timestamp > '" + t + "'";
		}
		sql += " order by timestamp";
		
		interactions = db.getInteractions(sql);
		groups = aggrLLInteractions();
		
		for(int i=0; i<groups.size(); i++)
		{
			String groupTitle = this.getGroupTitle(groups.get(i));
			String groupApp = this.getGroupApp(groups.get(i));
			
			int groupId = db.insertGroupedInteraction(groupTitle, groupApp, user);
			
			for(int j=0; j<groups.get(i).size(); j++)
			{
				int k = groups.get(i).get(j);
				String time = interactions.get(k).getTimestamp();
				
				
				int nextK = k+1 >= interactions.size() ? k : k+1;
				String nextTime = interactions.get(nextK).getTimestamp();
				double duration = DateUtil.calcInterval(time, nextTime);
				//boolean hasScreen = interactions.get(k).isHasScreen();
				
				db.insertGroupDetail(groupId, time, duration);
			}
		}
		System.out.println("insert groups: " + groups.size());
	}
	
	public List<List<Integer>> aggrLLInteractions() throws Exception
	{
		final double THRESHOLD = 1 * 60;
		
		List<List<Integer>> groups = new ArrayList<List<Integer>>();
		Set<Integer> hasAggr = new HashSet<Integer>();
		
		int i = 0;
		while(i < interactions.size())
		{
			LowLevelInteraction u = interactions.get(i);
			if(InteractionUtil.filterApplication(u)) 
			{
				i++;
				continue;
			}
			
			if(hasAggr.contains(i)) 
			{
				i++;
				continue;
			}
			
			hasAggr.add(i);
			List<Integer> group = new ArrayList<Integer>();
			group.add(i);
			
			int j = i + 1;
			while(j < interactions.size())
			{
				LowLevelInteraction v = interactions.get(j);
				double interval = DateUtil.calcInterval(u.getTimestamp(), v.getTimestamp());
				
				if(hasAggr.contains(j)) 
				{
					j++;
					continue;
				}
				
				if( !InteractionUtil.isAggregated(u, v) && interval > THRESHOLD)
				{
					break;
				}
				else if(InteractionUtil.isAggregated(u, v))
				{
					if(interval > 60 * 60)
					{
						break;
					}
					
					hasAggr.add(j);
					group.add(j);
					if(InteractionUtil.getWindowName(u).equals(InteractionUtil.getWindowName(v)))
					{
						u = v;
					}
				}
				
				j++;
			}
			groups.add(group);
		}
		
		return groups;
	}
	
	public double calcGroupInterval(List<Integer> group) throws Exception
	{
		if(group.size() <= 0) return 0;
		
		int k = group.get(0);
		int sz = group.size();
		
		int k2 = group.get(sz-1) + 1;
		k2 = k2 >= interactions.size() ? k2-1 : k2;
		
		JaretDate t1 = getStartDate(group);
		JaretDate t2 = getEndDate(group);
		return t2.diffMilliSeconds(t1) * 1.0 / 1000;
		
	}
	
	public double calcGroupInterval2(List<Integer> group) throws Exception
	{
		double sum = 0;
		for(int i=0; i<group.size(); i++)
		{
			int k = group.get(0);
			int k2 = k + 1;
			String t1 = interactions.get(k).getTimestamp();
			String t2 = interactions.get(k2).getTimestamp();
			
			double interval = DateUtil.calcInterval(t1, t2);
			if(interval > 60 * 10) continue;
			
			sum += interval;
		}
		
		return sum;
		
	}
	
	public String getGroupTitle(List<Integer> group)
	{
		if(group.size() <= 0) return "";
		
		String title = "";
		for(int i=0; i<group.size(); i++)
		{
			LowLevelInteraction u = interactions.get(group.get(i));
			
			String app = u.getApplication();
			title = InteractionUtil.getWindowName(u);
			if(InteractionUtil.isBrowser(app))
			{
				title = InteractionUtil.getInteractionTitle(u);
			}
			else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
			{
				String pattern = ".+\\s\\-\\s.*\\.(java|xml|txt|class)\\s\\-\\sEclipse";
				if(title.matches(pattern))
				{
					int index1 = title.lastIndexOf("/");
					int index2 = title.lastIndexOf(" - ");
					int index3 = title.indexOf(" - ");
					String fileName = title.substring(index1+1, index2);
					String pack = title.substring(index3+3, index1);
					title = fileName + "(" + pack + ")";
				}
			}
			
			if(!"".equals(title))
			{
				return title;
			}
		}
		
		return title;
	}
	
	public String getGroupApp(List<Integer> group)
	{
		if(group.size() <= 0) return "";
		
		return interactions.get(group.get(0)).getApplication();
	}
	
	public JaretDate getStartDate(List<Integer> group) throws Exception
	{
		if(group.size() <= 0) return null;
		
		String t = interactions.get(group.get(0)).getTimestamp();
		
		return DateUtil.toJaretDate(t);
	}
	
	public JaretDate getEndDate(List<Integer> group) throws Exception
	{
		if(group.size() <= 0) return null;
		
		int i = 0;
		String t = interactions.get(group.get(i)).getTimestamp();;
		for(; i<group.size()-1 && group.get(i)<interactions.size(); i++)
		{
			int k2 = group.get(i+1);
			String t2 = interactions.get(k2).getTimestamp();
			double interval = DateUtil.calcInterval(t, t2);
			if(interval > 60 * 60)
			{
				break;
			}
			
			t = t2;
		}
		
		return DateUtil.toJaretDate(t);
	}
	
	public static void main(String[] args) throws Exception
	{
		String user = System.getProperty("user.name");
		System.out.println(user);
		
		GroupInteractionMananger gm = new GroupInteractionMananger();
		gm.groupInteractions();
	}
	
}

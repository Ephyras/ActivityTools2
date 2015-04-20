package cn.zju.edu;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.scene.Group;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.dao.CResource;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.timeline.model.TimelineEvent;
import cn.zju.edu.util.DateUtil;
import de.jaret.util.date.JaretDate;
import de.jaret.util.ui.timebars.model.DefaultRowHeader;
import de.jaret.util.ui.timebars.model.DefaultTimeBarModel;
import de.jaret.util.ui.timebars.model.DefaultTimeBarRowModel;
import de.jaret.util.ui.timebars.model.TimeBarModel;
import cn.zju.edu.model.*;

public class DataManager {
	public final static HashMap<String,String> APP_MAP = new HashMap<String, String>();
	static
	{
		APP_MAP.put("firefox.exe", "Mozilla Firefox");
		APP_MAP.put("chrome.exe", "Google Chrome");
		APP_MAP.put("iexplore.exe", "Internet Explorer");
		APP_MAP.put("eclipse.exe", "Eclipse");
		APP_MAP.put("javaw.exe", "Eclipse");
		APP_MAP.put("devenv.exe", "Visual Studio");
		APP_MAP.put("WINWORD.exe", "Word Document");
	}
	
	private DBImpl db;
	private String user;
	
	private List<LowLevelInteraction> interactions = new ArrayList<LowLevelInteraction>();
	
	private boolean hasGroup = false;
	private List<List<Integer>> groups = new ArrayList<List<Integer>>();
	private Set<String> appSet = new HashSet<String>();
	
	public DataManager() throws Exception
	{
		db = new DBImpl();
		user = "baolingfeng";
	}
	
	public List<CResource> getOverviewData(String[] app, String order) throws SQLException
	{
		return db.getOverviewData(app, order);
	}
	
	public void updateOverviewProcessTime(String t) throws SQLException
	{
		 db.updateOverviewProcessTime(t);
	}
	
	public static boolean filterApplication(LowLevelInteraction u)
	{
		if("explorer.exe".equals(u.getApplication())) 
		{
			return true;
		}
		else if(isBrowser(u.getApplication()))
		{
			return !isWebPage(u);
		}
		else if("eclipse.exe".equals(u.getApplication()) || "javaw.exe".equals(u.getApplication()))
		{
			return  !isEclipseMainWindow(u);
		}
		else if("WINWORD.exe".equals(u.getApplication()))
		{
			return !isWinWORDFile(u);
		}
		else if("devenv.exe".equals(u.getApplication()))
		{
			return "".equals(getVSFile(u));
		}
		
		return false;
	}
	
	public static boolean isBrowser(String app)
	{
		return "firefox.exe".equals(app) || "chrome.exe".equals(app) || "iexplore.exe".equals(app);
	}
	
	public static String getWindowName(LowLevelInteraction u)
	{
		String app = u.getApplication();
		if("firefox.exe".equals(app) || "iexplore.exe".equals(app))
		{
			return !"".equals(u.getWindow()) ? u.getWindow() : u.getParentWindow();
		}
		else if("chrome.exe".equals(app))
		{
			return "Chrome Legacy Window".equals(u.getWindow()) || "".equals(u.getWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("WINWORD.EXE".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("devenv.exe".equals(app))
		{
			String w = "".equals(u.getWindow()) ? u.getParentUiName() : u.getWindow();
			String vsfile = getVSFile(u);
			if(!"".equals(vsfile)) 
			{
				return vsfile + " (" + w + ")";
			}
			
			return w;
		}
		else
		{
			return !"".equals(u.getWindow()) ? u.getWindow() : u.getParentWindow();
		}
		
	}
	
	public static boolean isWebPage(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		String appName = APP_MAP.get(u.getApplication());
		return w.endsWith(" - " + appName);
	}
	
	public static boolean isEclipseMainWindow(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		String pattern = ".+\\s\\-\\s.*\\.(java|xml|txt|class)\\s\\-\\sEclipse";
		return w.matches(pattern);
	}
	
	public static boolean isWinWORDFile(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		return w.contains("doc");
	}
	
	public static String getVSFile(LowLevelInteraction u)
	{
		if("Text Editor".equals(u.getUiName()))
		{
			if(u.getParentUiName().contains(".cpp") || u.getParentUiName().contains(".h"))
			{
				return u.getParentUiName();
			}
		}
		else if("Solution Explorer".equals(u.getParentUiName()))
		{
			if(u.getUiName().contains(".cpp") || u.getUiName().contains(".h"))
			{
				return u.getUiName();
			}
		}
		
		return "";
	}
	
	public boolean isAggregated(LowLevelInteraction u, LowLevelInteraction v) //u: previous, v: next
	{
		if(v.getApplication() != null && !v.getApplication().equals(u.getApplication())) return false;
		
		String app = u.getApplication();
		String w1 = getWindowName(u);
		String w2 = getWindowName(v);
		if("firefox.exe".equals(app) || "iexplore.exe".equals(app))
		{
			return isWebPage(u) && (w1.equals(w2) || "".equals(w2) || !isWebPage(v));
		}
		else if("chrome.exe".equals(app))
		{
			return w1.equals(w2) || "".equals(w2) || !isWebPage(v);
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			return isEclipseMainWindow(u) && (w1.equals(w2) || "".equals(w2) || !isEclipseMainWindow(v));
		}
		else if("WINWORD.exe".equals(app))
		{
			return isWinWORDFile(u) && (w1.equals(w2) || "".equals(w2) || !isWinWORDFile(v));
		}
		else if("devenv.exe".equals(app))
		{
			String f1 = getVSFile(u);
			String f2 = getVSFile(v);
			
			return !"".equals(f1) && (w1.equals(w2) || "".equals(f2));
			
		}
		else
		{
			return w1.equals(w2) || "".equals(w2);
		}
	}
	
	public void getInteractionData(String sql) throws Exception
	{
		interactions =  db.getInteractions(sql);
	}
	
	public void aggrLLInteractions() throws Exception
	{
		final double THRESHOLD = 1 * 60;
		
		Set<Integer> hasAggr = new HashSet<Integer>();
		int i = 0;
		while(i < interactions.size())
		{
			LowLevelInteraction u = interactions.get(i);
			if(filterApplication(u)) 
			{
				i++;
				continue;
			}
			
			appSet.add(u.getApplication());
			
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
				
				if( !isAggregated(u, v) && interval > THRESHOLD)
				{
					break;
				}
				else if(isAggregated(u, v))
				{
					if(interval > 60 * 60)
					{
						break;
					}
					
					hasAggr.add(j);
					group.add(j);
					if(getWindowName(u).equals(getWindowName(v)))
					{
						u = v;
					}
				}
				
				j++;
			}
			groups.add(group);
		}
		
		hasGroup = true;
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
			title = getWindowName(u);
			if(isBrowser(app))
			{
				int index = title.indexOf(" - " + APP_MAP.get(app));
				if(index >= 0)
				{
					title = title.substring(0, index);
				}
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
	
	public  TimeBarModel createFlatModel() throws Exception
	{
		 DefaultTimeBarModel model = new DefaultTimeBarModel();
		 
		 HashMap<String, EventTimeBarRow> bars = new  HashMap<String, EventTimeBarRow>();
		 Iterator<String> iterator = appSet.iterator();
		 while(iterator.hasNext())
		 {
			 String app = iterator.next();
			 String headerLabel = "Other Application";
			 if(APP_MAP.containsKey(app))
			 {
				 headerLabel = APP_MAP.get(app);
				 DefaultRowHeader header = new DefaultRowHeader(headerLabel);
				 EventTimeBarRow row = new EventTimeBarRow(header);
				 bars.put(app, row);
				 model.addRow(row);
			 }
			 else
			 {
				 if(!bars.containsKey(headerLabel))
				 {
					 DefaultRowHeader header = new DefaultRowHeader(headerLabel);
					 EventTimeBarRow row = new EventTimeBarRow(header);
					 bars.put(app, row);
					 model.addRow(row);
				 }
			 }
			 
		 }
		 
		 for(int i=0; i<groups.size(); i++)
		 {
			 if(calcGroupInterval(groups.get(i)) < 10) 
			 {
				 continue;
			 }
			 
			 String title = getGroupTitle(groups.get(i));
			 EventInterval interval = new EventInterval(getStartDate(groups.get(i)), getEndDate(groups.get(i)));
			 
			 System.out.println(title + "==>" + interval.getSeconds());
			 
			 interval.setTitle(title);
			 bars.get(getGroupApp(groups.get(i))).addInterval(interval);
		 }
		 
		 return model;
	 }
	
	public DefaultTimeBarModel createTimeModel() throws Exception
	{
		DefaultTimeBarModel model = new DefaultTimeBarModel();
		DefaultTimeBarRowModel row = new DefaultTimeBarRowModel(new DefaultRowHeader("events"));
		for(int i=0; i<groups.size(); i++)
		{
			if(calcGroupInterval(groups.get(i)) < 10) 
			 {
				 continue;
			 }
			 
			 TimelineEvent event = new TimelineEvent();
			 
			 String title = getGroupTitle(groups.get(i));
			 event.setTitle(title);
		     event.setContent(title);
		     
		     event.setBegin(getStartDate(groups.get(i)));
		     event.setEnd(getEndDate(groups.get(i)));
		     event.setDuration(false);
		     
		     //System.out.println(event.getSeconds());
		     
		     row.addInterval(event);
		}
		model.addRow(row);
		return model;
	}
	
	public void processOverviewData() throws Exception
	{
		String processTime = db.getOverviewProcessTime();
		
		String sql = "select * from tbl_interactions where user_name = '" + user + "'";
		if(processTime != null && !"".equals(processTime))
		{
			sql += " and timestamp > '" + processTime + "'";
		}
		sql += "order by timestamp";
		
		getInteractionData(sql);
		this.aggrLLInteractions();
		
		HashMap<String, CResource> m = new HashMap<String, CResource>();
		for(int i=0; i<groups.size(); i++)
		{
			String title = getGroupTitle(groups.get(i));
			String app = this.getGroupApp(groups.get(i));
			double duration = this.calcGroupInterval2(groups.get(i));
			
			String scope = "";
			if("eclipse.exe".equals(app))
			{
				scope = getWindowName(interactions.get(groups.get(i).get(0)));
				int idx1 = scope.indexOf(" - ");
				int idx2 = scope.lastIndexOf(" - ");
				if(idx1 >=0 && idx2>=0)
				{
					try
					{
						scope = scope.substring(idx1+3, idx2);
					}catch(Exception e)
					{
						System.out.println(scope);
					}
				}
			}
			else if("devenv.exe".equals(app))
			{
				scope = getWindowName(interactions.get(groups.get(i).get(0)));
			}
			
			String key = title + "#" + app + "#" + scope;
			String lasttime = interactions.get(groups.get(i).get(0)).getTimestamp();
			if(m.containsKey(key))
			{
				CResource r = m.get(key);
				r.setDuration(r.getDuration() + duration);
				r.setLasttime(lasttime);
			}
			else
			{
				CResource r = new CResource();
				r.setName(title);
				r.setApplication(app);
				r.setDuration(duration);
				r.setScope(scope);
				r.setLasttime(lasttime);
				r.setType("Resourc");
				m.put(key, r);
			}
		}
		
		db.insertOverview(new ArrayList<CResource>(m.values()));
		db.updateOverviewProcessTime(interactions.get(interactions.size()-1).getTimestamp());
	}
	
	public void insertGroupedInteraction() throws Exception
	{
		for(int i=0; i<groups.size(); i++)
		{
			String groupTitle = this.getGroupTitle(groups.get(i));
			String groupApp = this.getGroupApp(groups.get(i));
			
			int groupId = db.insertGroupedInteraction(groupTitle, groupApp, "baolingfeng");
			
			for(int j=0; j<groups.get(i).size(); j++)
			{
				int k = groups.get(i).get(j);
				String time = interactions.get(k).getTimestamp();
				boolean hasScreen = interactions.get(k).isHasScreen();
				
				db.insertGroupDetail(groupId, time, 0);
			}
		}
	}
	
	public DBImpl getDb() {
		return db;
	}

	public void setDb(DBImpl db) {
		this.db = db;
	}

	public List<LowLevelInteraction> getInteractions() {
		return interactions;
	}

	public void setInteractions(List<LowLevelInteraction> interactions) {
		this.interactions = interactions;
	}

	public boolean isHasGroup() {
		return hasGroup;
	}

	public void setHasGroup(boolean hasGroup) {
		this.hasGroup = hasGroup;
	}

	public List<List<Integer>> getGroups() {
		return groups;
	}

	public void setGroups(List<List<Integer>> groups) {
		this.groups = groups;
	}

	public static void test() throws Exception
	{
		DataManager dm = new DataManager();
		String user = "baolingfeng";
		String sql = "select * from tbl_interactions where user_name = '" + user + "'" 
				//+ " and " + "timestamp>='2015-02-05 00:00:00.000'"
				+ " order by timestamp";
		
		dm.getInteractionData(sql);
		dm.aggrLLInteractions();
		dm.insertGroupedInteraction();
		
	}
	
	public static void main(String args[]) throws Exception
	{
		test();
		
	}
}

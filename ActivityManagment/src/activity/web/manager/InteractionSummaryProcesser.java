package activity.web.manager;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.util.CompareUtil;
import cn.zju.edu.util.DateUtil;
import cn.zju.edu.util.InteractionUtil;
import activity.web.db.DBResultSet;
import activity.web.db.MySqlImpl;

public class InteractionSummaryProcesser extends BackendProcesser{
	Logger logger = Logger.getLogger(InteractionSummaryProcesser.class.getName());
	
	private String lastTime = null;
	
	public InteractionSummaryProcesser()
	{
		super();
	}
	
	protected String getLastGroupedInteractionTime(String username)
	{
		String sql = "select max(interaction_time) as lasttime from tbl_group_detail a, tbl_group_interactions b where a.group_id = b.group_id and b.user_name = '" + username + "'";
		DBResultSet rs = db.retrieveResultSet(sql);
		
		if(rs.getRecords().size() > 0)
		{
			return rs.getRecords().get(0).getString("lasttime");
		}
		
		return "";
	}
	
	public void process()
	{
		List<String> users = getAllUsers();
		for(String user : users)
		{
			try
			{
				lastTime = getLastGroupedInteractionTime(user);
				logger.info("lattime: " + lastTime);
				
				List<LowLevelInteraction> interactions = this.getInteractions(user, lastTime);
				logger.info("interaction size: " + interactions.size());
				
				aggrLLInteractions(interactions, user);
				
				/*
				for(int i=0; i<groups.size(); i++)
				{
					String groupTitle = this.getGroupTitle(groups.get(i), interactions);
					String groupApp = this.getGroupApp(groups.get(i), interactions);
					
					
					String sql = "insert into tbl_group_interactions(group_title, group_app, user_name) values(?, ?, ?)";
					int groupId = db.insertWithAutoId(sql, groupTitle, groupApp, user);
					logger.info("new group: " + groupId + "/" + groupTitle + "/" + groups.get(i).size());
					
					sql = "insert into tbl_group_detail(group_id, interaction_time, duration) values(?, ?, ?)";
					for(int j=0; j<groups.get(i).size(); j++)
					{
						int k = groups.get(i).get(j);
						String time = interactions.get(k).getTimestamp();
						
						
						int nextK = k+1 >= interactions.size() ? k : k+1;
						String nextTime = interactions.get(nextK).getTimestamp();
						double duration = DateUtil.calcInterval(time, nextTime);
						//boolean hasScreen = interactions.get(k).isHasScreen();
						
						db.insert(sql,groupId, time, duration);
					}
				}
				*/
			}catch(Exception e)
			{
				e.printStackTrace();
				logger.info(e.getCause());
			}
		}
		
		for(String user : users)
		{
			processScreenshots(user);
		}
		
		finish();
	}
	
	public void processScreenshots(String user)
	{
		DataManager dm = new DataManager(user, db);
		
		List<GroupedInteraction> groups = dm.retrieveGroupInteractions(lastTime);
		
		for(int i=0; i<groups.size(); i++)
		{
			BufferedImage pre = null;
			for(int j=0; j<groups.get(i).getDetails().size(); j++)
			{
				String time = groups.get(i).getDetails().get(j).getTime();
				int screenStatus = groups.get(i).getDetails().get(j).getScreenStatus();
				if(screenStatus > 0)
				{
					continue;
				}
				
				BufferedImage cur = null;
				screenStatus = 2;
				
				LowLevelInteraction ll = dm.getAnInteractions2(time, true);
				if(ll == null)
				{
					System.out.println(time);
				}
				
				if(ll != null && ll.isHasScreen())
				{
					if(pre != null)
					{
						double thr = CompareUtil.compareImage(pre, ll.getScreen());
						if(thr < 0.9)
						{
							screenStatus = 1;
							cur = ll.getScreen();
						}
						logger.info("correlation: " +thr);
					}
					else
					{
						cur = ll.getScreen();
						screenStatus = 1;
					}
					pre = ll.getScreen();
				}
				
				dm.updateGroupDetail(time, screenStatus, cur);
				//set screen to empty in tbl_interactions
				//dm.clearInteractionScreen(time);
			}
		}
		logger.info("finish procseeing screenshots + " + user);
	}
	
	public String getGroupApp(List<Integer> group, List<LowLevelInteraction> interactions)
	{
		if(group.size() <= 0) return "";
		
		return interactions.get(group.get(0)).getApplication();
	}
	
	public String getGroupTitle(List<Integer> group, List<LowLevelInteraction> interactions)
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
					try
					{
						int index1 = title.lastIndexOf("/");
						int index2 = title.lastIndexOf(" - ");
						int index3 = title.indexOf(" - ");
						String fileName = title.substring(index1+1, index2);
						String pack = title.substring(index3+3, index1);
						title = fileName + "(" + pack + ")";
					}catch(Exception e)
					{
						System.out.println(title);
					}
				}
			}
			
			if(!"".equals(title))
			{
				return title;
			}
		}
		
		return title;
	}
	
	public  void aggrLLInteractions(List<LowLevelInteraction> interactions, String user) throws Exception
	{
		final double THRESHOLD = 60 * 60;
		
		List<List<Integer>> groups = new ArrayList<List<Integer>>();
		Set<Integer> hasAggr = new HashSet<Integer>();
		
		Map<String, Set<Integer>> groupMap = new HashMap<String, Set<Integer>>();
		
		int i = 0;
		int omit = 0;
		while(i < interactions.size())
		{
			LowLevelInteraction u = interactions.get(i);
			if("explorer.exe".equals(u.getApplication()))
			{
				omit++;
			}
			if(!InteractionUtil.isMainWindow(u))
			{
				i++;
				continue;
			}
			
			String title = InteractionUtil.getInteractionTitle(u);
			Set<Integer> curGroup = null;
			if(groupMap.containsKey(title))
			{
				curGroup = groupMap.get(title);
			}
			else
			{
				curGroup = new HashSet<Integer>();
				groupMap.put(title, curGroup);
			}
			curGroup.add(i);
			
//			if(hasAggr.contains(i)) 
//			{
//				i++;
//				continue;
//			}
			
//			hasAggr.add(i);
//			List<Integer> group = new ArrayList<Integer>();
//			group.add(i);
			
			int j = i + 1;
			while(j < interactions.size())
			{
				LowLevelInteraction v = interactions.get(j);
				//double interval = DateUtil.calcInterval(u.getTimestamp(), v.getTimestamp());
				 
				//if(interval > THRESHOLD) break;
				
//				if(hasAggr.contains(j)) 
//				{
//					j++;
//					continue;
//				}
				
				if(v.getApplication() != null && !v.getApplication().equals(u.getApplication()))
				{
					break;
				}
				else if(InteractionUtil.isMainWindow(u) && InteractionUtil.isMainWindow(v))
				{
					String w1 = InteractionUtil.getInteractionTitle(u);
					String w2 = InteractionUtil.getInteractionTitle(v);
					if(w1.equals(w2))
					{
						hasAggr.add(j);
						curGroup.add(j);
					}
					else
					{
						break;
					}
				}
				else if(InteractionUtil.isMainWindow(u) && !InteractionUtil.isMainWindow(v))
				{
					curGroup.add(j);
					//return true;
				}
				else
				{
					break;
				}
				
//				if( !InteractionUtil.isAggregated(u, v) && interval > THRESHOLD)
//				{
//					break;
//				}
//				else if(InteractionUtil.isAggregated(u, v))
//				{
//					if(interval > 60 * 60)
//					{
//						break;
//					}
//					
//					hasAggr.add(j);
//					group.add(j);
//					if(InteractionUtil.getWindowName(u).equals(InteractionUtil.getWindowName(v)))
//					{
//						u = v;
//					}
//				}
				
				j++;
			}
			i = j;
			//groups.add(group);
		}
		
		int num = 0;
		Set<Integer> all = new HashSet<Integer>();
		for(Entry<String, Set<Integer>> e: groupMap.entrySet())
		{
			System.out.println(e.getKey());
			List<Integer> list = new ArrayList<Integer>(e.getValue());
			Collections.sort(list);
			
			List<Integer> sperated = new ArrayList<Integer>();
			sperated.add(-1);
			for(int k=0; k<list.size()-1; k++)
			{
				int idx1 = list.get(k);
				int idx2 = list.get(k+1);
				LowLevelInteraction u = interactions.get(idx1);
				LowLevelInteraction v = interactions.get(idx2);
				
				double interval = DateUtil.calcInterval(u.getTimestamp(), v.getTimestamp());
				if(interval > THRESHOLD)
				{
					sperated.add(k);
				}
			}
			sperated.add(list.size()-1);
			
			for(int k=0; k<sperated.size()-1; k++)
			{
				int from = sperated.get(k)+1;
				int to = sperated.get(k+1);
				
				String groupTitle = e.getKey();
				String groupApp = this.getGroupApp(list, interactions);
				
				String sql = "insert into tbl_group_interactions(group_title, group_app, user_name) values(?, ?, ?)";
				int groupId = db.insertWithAutoId(sql, groupTitle, groupApp, user);
				logger.info("new group: " + groupId + "/" + groupTitle + "/" + (to-from));
				
				sql = "insert into tbl_group_detail(group_id, interaction_time, duration) values(?, ?, ?)";
				for(int j=from; j<=to && j<list.size(); j++)
				{
					int m = list.get(j);
					String time = interactions.get(m).getTimestamp();
					
					int nextK = m+1 >= interactions.size() ? m : m+1;
					String nextTime = interactions.get(nextK).getTimestamp();
					double duration = DateUtil.calcInterval(time, nextTime);
					if(duration > 60 * 60)
						duration = 1;
					
					db.insert(sql,groupId, time, duration);
				}
			}
			
			num += list.size();
			System.out.println();
		}
		
		System.out.println(num + "/" + interactions.size() + "/" + omit + "/" + all);
		//return groups;
	}
	
	public static void main(String[] args) throws Exception
	{
		InteractionSummaryProcesser isp = new InteractionSummaryProcesser();
		
		List<LowLevelInteraction> interactions = isp.getInteractions("baolingfeng", "2015-04-27 00:00:00.000");
		isp.aggrLLInteractions(interactions, "baolingfeng");
		
		//isp.process();
		
		//isp.getAllUsers();
		
		System.out.println(Boolean.parseBoolean("true"));
	}
}

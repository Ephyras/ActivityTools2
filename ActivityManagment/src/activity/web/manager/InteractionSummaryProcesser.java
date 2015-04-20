package activity.web.manager;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	
	private String imageTime = null;
	
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
				String lastTime = getLastGroupedInteractionTime(user);
				
				List<LowLevelInteraction> interactions = this.getInteractions(user, lastTime);
				
				List<List<Integer>> groups = aggrLLInteractions(interactions);
				
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
		
		List<GroupedInteraction> groups = dm.retrieveGroupInteractions(imageTime);
		
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
				
				if(j > 0)
				{
					//...
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
						if(thr < 0.8)
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
				//db.updateGroupDetail(user, time, screenStatus, cur);
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
	
	public List<List<Integer>> aggrLLInteractions(List<LowLevelInteraction> interactions) throws Exception
	{
		final double THRESHOLD = 60 * 60;
		
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
	
	public static void main(String[] args)
	{
		//InteractionSummaryProcesser isp = new InteractionSummaryProcesser();
		
		//isp.getAllUsers();
		
		System.out.println(Boolean.parseBoolean("true"));
	}
}

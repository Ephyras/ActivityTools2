package activity.web.manager;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.ActionDetail;
import cn.zju.edu.blf.dao.CodeChange;
import cn.zju.edu.blf.dao.CodeChangeDetail;
import cn.zju.edu.blf.dao.GroupDetail;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import activity.web.db.DBRecord;
import activity.web.db.DBResultSet;
import activity.web.db.MySqlImpl;
import cn.zju.edu.util.*;

public class DataManager {
	Logger logger = Logger.getLogger(DataManager.class.getName());
	
	private MySqlImpl db;
	private String user;
	private String time;
	private String contextPath;
	
	public DataManager(String user)
	{
		db = new MySqlImpl();
		this.user = user;
	}
	
	public DataManager(String user, MySqlImpl db)
	{
		this.db = db;
		this.user = user;
	}
	
	public void setContextPath(String contextPath)
	{
		this.contextPath = contextPath;
	}
	
	public List<GroupedInteraction> retrieveGroupInteractions(String time)
	{
		String sql = "select a.group_id, group_title, group_app, interaction_time, duration, screen_status "
				+ "from tbl_group_interactions a, tbl_group_detail b where a.group_id = b.group_id ";
		sql += "and user_name = '" + user + "' ";
		
		if(time!=null && !"".equals(time))
		{
			sql += " and interaction_time> '" + time + "'";
		}
		
		sql += " order by group_id, interaction_time";
		
		logger.info(sql);
		
		DBResultSet rs = db.retrieveResultSet(sql);
		
		int preId = -1;
		List<GroupedInteraction> list = new ArrayList<GroupedInteraction>();
		for(int i=0; i<rs.getRecords().size(); i++)
		{
			DBRecord r = rs.getRecords().get(i);
			
			int groupId = r.getInt("group_id");
			
			String groupTitle = r.getString("group_title");
			String groupApp = r.getString("group_app");
			String interactionTime = r.getString("interaction_time");
			double duration = r.getDouble("duration");
			int screenStatus = r.getInt("screen_status");
			
			if(preId == groupId)
			{
				GroupDetail detail = new GroupDetail();
				detail.setGroupId(groupId);
				detail.setTime(interactionTime);
				detail.setScreenStatus(screenStatus);
				detail.setDuration(duration);
				
				list.get(list.size()-1).addDetail(detail);
				list.get(list.size()-1).setDuration(list.get(list.size()-1).getDuration() + duration);
			}
			else
			{
				GroupedInteraction a = new GroupedInteraction();
				a.setGroupId(groupId);
				a.setTitle(groupTitle);
				a.setApplication(groupApp);
				a.setDuration(duration);
				GroupDetail detail = new GroupDetail();
				detail.setGroupId(groupId);
				detail.setTime(interactionTime);
				detail.setScreenStatus(screenStatus);
				detail.setDuration(duration);
				
				List<GroupDetail> details = new ArrayList<GroupDetail>();
				details.add(detail);
				a.setDetails(details);
				list.add(a);
				
				preId = groupId;
			}
		}
		
		return list;
	}
	
	public List<GroupedInteraction> groupByDay(List<GroupedInteraction> groups)
	{
		List<GroupedInteraction> groupByDay = new ArrayList<GroupedInteraction>();
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			
			int index = -1;
			for(int j=0; j<groupByDay.size(); j++)
			{
				GroupedInteraction g2 = groupByDay.get(j);
				if(g.getTitle().equals(g2.getTitle()) && g.getApplication().equals(g2.getApplication()))
				{
					if(g.getDetails().size() <= 0 || g2.getDetails().size()<=0) continue;
					
					String t1 = g.getDetails().get(0).getTime();
					String t2 = g2.getDetails().get(0).getTime();
					
					if(DateUtil.isSameDay(t1, t2))
					{
						index = j; break;
					}
				}
			}
			
			String from = g.getDetails().get(0).getTime();
			String to = g.getDetails().get(g.getDetails().size()-1).getTime();
			
			if(index >= 0)
			{
				GroupedInteraction g2 = groupByDay.get(index);
				g2.setDuration(g2.getDuration() + g.getDuration());
				g2.addDetail(g.getDetails());
				g2.addTimeslot(from, to);
			}
			else
			{
				GroupedInteraction newG = new GroupedInteraction(g);
				newG.addTimeslot(from, to);
				groupByDay.add(newG);
			}
		}
		
		return groupByDay;
	}
	
	public List<GroupedInteraction> aggrAllGroup(List<GroupedInteraction> groups)
	{
		List<GroupedInteraction> aggrGroup = new ArrayList<GroupedInteraction>();
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			
			int index = -1;
			for(int j=0; j<aggrGroup.size(); j++)
			{
				GroupedInteraction g2 = aggrGroup.get(j);
				if(g.getTitle().equals(g2.getTitle()) && g.getApplication().equals(g2.getApplication()))
				{
					index = j;
					break;
				}
			}
			
			if(index >= 0)
			{
				GroupedInteraction g2 = aggrGroup.get(index);
				g2.setDuration(g2.getDuration() + g.getDuration());
				g2.addDetail(g.getDetails());
			}
			else
			{
				GroupedInteraction newG = new GroupedInteraction();
				newG.setApplication(g.getApplication());
				newG.setTitle(g.getTitle());
				newG.setDuration(g.getDuration());
				newG.addDetail(g.getDetails());
				
				aggrGroup.add(newG);
			}
		}
		
		return aggrGroup;
	}
	
	public LowLevelInteraction getAnInteractions2(String time, boolean withScreen)
	{
		String sql = "select timestamp, window, parent_window, application, point_x, point_y, win_rect_left,"
				+ " win_rect_top, win_rect_right, win_rect_bottom, ui_name, ui_type, ui_value, "
				+ " parent_ui_name, parent_ui_type, ui_bound_left, ui_bound_top, ui_bound_right, ui_bound_bottom,"
				+ " has_screen ";
		/*
		String sql = "select a.timestamp, a.window, a.parent_window, a.application, a.point_x, a.point_y, a.win_rect_left,"
				+ " a.win_rect_top, a.win_rect_right, a.win_rect_bottom, a.ui_name, a.ui_type, a.ui_value, "
				+ " a.parent_ui_name, a.parent_ui_type, a.ui_bound_left, a.ui_bound_top, a.ui_bound_right, a.ui_bound_bottom,"
				+ "c.screen_status ";
			*/
		
		if(withScreen)
		{
			sql += ", screen ";
		}
		sql += "from tbl_interactions"
			+" where user_name = '" + user + "' and timestamp = '" + time + "'"; 
		
		DBResultSet rs = db.retrieveResultSet(sql);
		
		if(rs.getRecords().size() > 0)
		{
			DBRecord r = rs.getRecords().get(0);
			
			LowLevelInteraction li = InteractionUtil.fromDBRecord(r);
			
			//logger.info(li.getScreen());
			
			return li;
		}
		
		return null;
	}
	
	public LowLevelInteraction getAnInteractions(String time, boolean withScreen)
	{
		/*
		String sql = "select timestamp, window, parent_window, application, point_x, point_y, win_rect_left,"
				+ " win_rect_top, win_rect_right, win_rect_bottom, ui_name, ui_type, ui_value, "
				+ " parent_ui_name, parent_ui_type, ui_bound_left, ui_bound_top, ui_bound_right, ui_bound_bottom,"
				+ " has_screen ";
		*/
		
		String sql = "select a.timestamp, a.window, a.parent_window, a.application, a.point_x, a.point_y, a.win_rect_left,"
				+ " a.win_rect_top, a.win_rect_right, a.win_rect_bottom, a.ui_name, a.ui_type, a.ui_value, "
				+ " a.parent_ui_name, a.parent_ui_type, a.ui_bound_left, a.ui_bound_top, a.ui_bound_right, a.ui_bound_bottom,"
				+ "c.screen_status ";
			
		if(withScreen)
		{
			sql += ", c.screen ";
		}
		sql += "from tbl_interactions a, tbl_group_interactions b, tbl_group_detail c"
			+" where a.timestamp = c.interaction_time and b.group_id = c.group_id and a.user_name = '" + user + "' and a.timestamp = '" + time + "'"; 
		
		
		DBResultSet rs = db.retrieveResultSet(sql);
		
		if(rs.getRecords().size() > 0)
		{
			DBRecord r = rs.getRecords().get(0);
			
			LowLevelInteraction li = new LowLevelInteraction();
			li.setTimestamp(r.getString("timestamp"));
			li.setWindow(r.getString("window"));
			li.setParentWindow(r.getString("parent_window"));
			li.setApplication(r.getString("application"));
			li.setPx(r.getInt("point_x"));
			li.setPy(r.getInt("point_y"));
			li.setwRectLeft(r.getInt("win_rect_left"));
			li.setwRectTop(r.getInt("win_rect_top"));
			li.setwRectRight(r.getInt("win_rect_right"));
			li.setwRectBottom(r.getInt("win_rect_bottom"));
			li.setUiName(r.getString("ui_name"));
			li.setUiType(r.getString("ui_type"));
			li.setUiValue(r.getString("ui_value"));
			li.setParentUiName(r.getString("parent_ui_name"));
			li.setParentUiType(r.getString("parent_ui_type"));
			li.setUiBoundLeft(r.getInt("ui_bound_left"));
			li.setUiBoundTop(r.getInt("ui_bound_top"));
			li.setUiBoundRight(r.getInt("ui_bound_right"));
			li.setUiBoundBottom(r.getInt("ui_bound_bottom"));
			li.setHasScreen(r.getInt("screen_status") == 1);
			li.setScreen(r.getBufferedImage("screen"));
			
			//logger.info(li.getScreen());
			
			return li;
		}
		
		return null;
	}
	
	public List<LowLevelInteraction> getInteractions(String time)
	{
		String sql = "select timestamp, window, parent_window, application, point_x, point_y, win_rect_left,"
				+ " win_rect_top, win_rect_right, win_rect_bottom, ui_name, ui_type, ui_value, "
				+ " parent_ui_name, parent_ui_type, ui_bound_left, ui_bound_top, ui_bound_right, ui_bound_bottom,"
				+ " has_screen from tbl_interactions where user_name = '";
		sql += user + "'";
		
		if(time!= null && !"".equals(time))
		{
			sql += " and timestamp > '" + time + "'";
		}
		sql += " order by timestamp";
		
		DBResultSet rs = db.retrieveResultSet(sql);
		
		List<LowLevelInteraction> list = new ArrayList<LowLevelInteraction>();
		for(int i=0; i<rs.getRecords().size(); i++)
		{
			list.add(InteractionUtil.fromDBRecord(rs.getRecords().get(i)));
		}
		return list;
	}
	
	public void updateGroupDetail(String time, int screenStatus, BufferedImage img)
	{
		String sql = "call update_group_detail(?, ?, ?, ?)";
		
		db.executeCall(sql, user, time, screenStatus, img);
		
	}
	
	public int getGroupId(String time)
	{
		String sql = "SELECT a.group_id FROM hci.tbl_group_interactions a, tbl_group_detail b "
				+ "where a.group_id = b.group_id and a.user_name = '" + user + "' and "
						+ "interaction_time = '" + time + "'";
		
		DBResultSet rs = db.retrieveResultSet(sql);
		if(rs.getRecords().size() > 0)
		{
			return rs.getRecords().get(0).getInt("group_id");
		}
		
		return -1;
	}
	
	public BufferedImage getScreenshot(String groupId, String time)
	{
		String sql = "select screen, length(screen) as len from tbl_group_detail where group_id = " + groupId + " and interaction_time = '" + time + "'";
		logger.info(sql);
		
		DBResultSet rs = db.retrieveResultSet(sql);
		if(rs.getRecords().size() > 0)
		{
			DBRecord r =  rs.getRecords().get(0);
			//long len = r.getLong("len");
			return  r.getBufferedImage("screen");
		}
		
		logger.info("no screenshot");
		return null;
	}
	
	public BufferedImage getScreenshot(String time)
	{
		String sql = "select screen from tbl_group_interactions a, tbl_group_detail b where a.group_id = b.group_id "
				+ " and user_name='" + user + "' and interaction_time = '" + time + "'";
		logger.info(sql);
		
		DBResultSet rs = db.retrieveResultSet(sql);
		if(rs.getRecords().size() > 0)
		{
			DBRecord r =  rs.getRecords().get(0);
			return  r.getBufferedImage("screen");
		}
		
		logger.info("no screenshot");
		return null;
		
	}
	
	public BufferedImage getScreenshot2(String time)
	{
		String sql = "select screen from tbl_interactions where "
				+ "user_name='" + user + "' and timestamp = '" + time + "'";
		//logger.info(sql);
		
		DBResultSet rs = db.retrieveResultSet(sql);
		if(rs.getRecords().size() > 0)
		{
			DBRecord r =  rs.getRecords().get(0);
			return  r.getBufferedImage("screen");
		}
		
		logger.info("no screenshot");
		return null;
		
	}
	
	public List<CodeChange> getCodeChanges(String title, String app, String time, boolean isDay, List<GroupedInteraction> groups) throws Exception
	{
		List<CodeChange> changes = new ArrayList<CodeChange>();
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			if(g.getTitle().equals(title) && g.getApplication().equals(app))
			{
				int sz = g.getDetails().size()-1;
				String t = g.getDetails().get(sz).getTime();
				Date d = DateUtil.formatTime(t);
				String t2 = DateUtil.fromDate(d, "yyyy-MM-dd");
				if(isDay && !t2.equals(time)) continue;
				
				LowLevelInteraction first = null;
				for(int k=0; k<g.getDetails().size(); k++)
				{
					t = g.getDetails().get(k).getTime();
					LowLevelInteraction ll = getAnInteractions(t, false);
					
					if(ll != null && (InteractionUtil.isControlType("edit", ll.getUiType()) && ll.getParentUiName().contains(".java")))
					{
						first = ll; break;
					}
				}
				
				LowLevelInteraction last = null;
				for(int k=sz; k>=0; k--)
				{
					t = g.getDetails().get(k).getTime();
					LowLevelInteraction ll = getAnInteractions(t, false);
					
					if(ll != null && (InteractionUtil.isControlType("edit", ll.getUiType()) && ll.getParentUiName().contains(".java")))
					{
						last = ll; break;
					}
				}
				
				if(first == null || last == null) continue;
				
				String firstSource = first.getUiValue().replaceAll("\\\\n", "\n");
				firstSource = firstSource.replaceAll("\\\\t", "\t");
				String lastSource = last.getUiValue().replaceAll("\\\\n", "\n");
				lastSource = lastSource.replaceAll("\\\\t", "\t");
				//String changeText = CompareUtil.compareTextInHtml(firstSource, lastSource);
				List<CodeChangeDetail> details = CompareUtil.compareTextInDetail(firstSource, lastSource);
				
				//if(details.size() <= 0) continue;
				
				CodeChange c = new CodeChange();
				c.setTime(first.getTimestamp());
				c.setSource(firstSource);
				c.setDetail(details);
				changes.add(c);
				
				
			}
		}
		return changes;
	}
	
	private boolean isDirectShowUIImage(LowLevelInteraction ll)
	{
		int width = ll.getUiBoundRight() - ll.getUiBoundLeft();
		int height = ll.getUiBoundBottom() - ll.getUiBoundTop();
		
		return width < 100 && height < 100;
	}
	
	public Map<String, List<ActionDetail>> getLLInteractionsForDetail(String title, String app,String day, boolean isDay,  List<GroupedInteraction> groups) throws Exception
	{
		//List<LowLevelInteraction> list = new ArrayList<LowLevelInteraction>();
		
		HashMap<Integer, List<LowLevelInteraction>> map = new HashMap<Integer, List<LowLevelInteraction>>();
		
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			if(g.getTitle().equals(title) && g.getApplication().equals(app))
			{
				
				for(int j=0; j<g.getDetails().size(); j++)
				{
					GroupDetail detail = g.getDetails().get(j);
					String t = g.getDetails().get(j).getTime();
					Date d = DateUtil.formatTime(t);
					String t2 = DateUtil.fromDate(d, "yyyy-MM-dd");
					if(isDay && !t2.equals(day)) continue;
					
					LowLevelInteraction ll = this.getAnInteractions(g.getDetails().get(j).getTime(), false);
					if(ll == null) continue;
					
					if(map.containsKey(detail.getGroupId()))
					{
						map.get(detail.getGroupId()).add(ll);
					}
					else
					{
						List<LowLevelInteraction> list2 = new ArrayList<LowLevelInteraction>();
						list2.add(ll);
						map.put(detail.getGroupId(), list2);
					}
				}
			}
		}
		
		TreeMap<String, List<ActionDetail>> resMap = new TreeMap<String, List<ActionDetail>>();
		
		for(Entry<Integer, List<LowLevelInteraction>> entry: map.entrySet())
		{
			List<LowLevelInteraction> list = entry.getValue();
			
			Set<ActionDetail> actions = new LinkedHashSet<ActionDetail>();
			for(int i=0; i<list.size(); i++)
			{
				LowLevelInteraction ll = list.get(i);
				
				if(InteractionUtil.isControlType("tab item", ll.getUiType()) || 
						InteractionUtil.isControlType("pane", ll.getUiType()) ||
						InteractionUtil.isControlType("window", ll.getUiType())) 
					continue;
				
				if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
				{
					if(InteractionUtil.isControlType("edit", ll.getUiType()) && 
							(ll.getParentUiName().contains(".java") || "Source".equalsIgnoreCase(ll.getParentUiName())))
					{
						continue;
					}

					if(InteractionUtil.isControlType("edit", ll.getUiType()) && 
							(ll.getParentUiName().equalsIgnoreCase("console")))
					{
						String content = ll.getUiValue();
						if(content.indexOf("Exception") < 0) continue;
					}
				}
				
				ActionDetail ad = new ActionDetail();
				ad.setControlType(ll.getUiType());
				ad.setParent(ll.getParentUiName());
				ad.setTime(ll.getTimestamp());
				String action = "";
				if("".equals(ll.getUiName()) && "".equals(ll.getUiValue()))
				{
					action = "No Accessibility Information";
				}
				else if("".equals(ll.getUiValue()))
				{
					action += ll.getUiName();
				}
				else if("".equals(ll.getUiName()))
				{
					action += ll.getUiValue();
				}
				else
				{
					action += ll.getUiName() + "(" + ll.getUiValue() + ")";
				}
				
				if(isDirectShowUIImage(ll) && "No Accessibility Information".equals(action)
						//||(InteractionUtil.isControlType("pane", ll.getUiType()) && ll.getParentUiName().endsWith("java"))
						)
				{
					String t = ll.getTimestamp();
					if(!ll.isHasScreen())
					{
						for(int j=i-1; j>=0; j--)
						{
							if(list.get(j).isHasScreen())
							{
								t = list.get(j).getTimestamp();
								break;
							}
						}
					}
					
					ad.setImgUrl(" <img src='" + contextPath + "/GetScreenshotsServlet?time="+t+"&acc=true'/>");
				}
				
				ad.setAction(action);
				
				//System.out.println("action hash code:" + ad.hashCode());
				actions.add(ad);
			}
			
			List<ActionDetail> list2 = new ArrayList<ActionDetail>(actions);
			
			//logger.info( list.get(0).getTimestamp() + " : " + CommonUtil.toJson4Action(list2));
			
			resMap.put(list.get(0).getTimestamp(), list2);
		}
		
		//Collections.sort(list2);
		
		return resMap;
	}
	
	private void setImgUrl(LowLevelInteraction ll)
	{
		
	}
	
	public void close()
	{
		db.close();
	}
	
	public static void main(String[] args) throws Exception
	{
		DataManager dm = new DataManager("baolingfeng");
		
		LowLevelInteraction li = dm.getAnInteractions("2015-03-24 17:52:27.365", false);
		
		/*
		List<GroupedInteraction> gi = dm.retrieveGroupInteractions("2015-03-22 00:00:00.000");
		
		List<GroupedInteraction> groupByDay = dm.groupByDay(gi);
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<groupByDay.size(); i++)
		{
			if(groupByDay.get(i).getDuration() < 300) continue;
			
			String title = groupByDay.get(i).getTitle();
			
			if("".equals(title.trim())) continue;
			
			if(groupByDay.get(i).getDetails().size() <= 0) continue;
			
			String time = groupByDay.get(i).getDetails().get(0).getTime();
			
			String time2 = DateUtil.fromDate(DateUtil.formatTime(time), "yyyy-MM-dd'T'HH:mm:ss");
			
			sb.append("{title: '" + title + "', \n");
			sb.append("start: '" + time2 + "'}, \n");
			
			System.out.println(groupByDay.get(i).getTitle() + " / " + groupByDay.get(i).getDuration());
		}
		*/
		
		
		//System.out.println(sb.toString());
	}
}

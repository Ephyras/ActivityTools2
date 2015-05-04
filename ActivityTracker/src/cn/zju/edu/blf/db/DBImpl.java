package cn.zju.edu.blf.db;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import cn.zju.edu.ActivityConfiguration;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.dao.CResource;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.dao.GroupDetail;
import cn.zju.edu.manager.IconManager;

public class DBImpl {
	Logger logger = Logger.getLogger(DBImpl.class.getName());
	
	//private static String URL = "jdbc:mysql://155.69.147.247:3306/hci";
	//private static String USER_NAME = "blf";
	//private static String PASSWORD = "123456";
	
	private Connection connection;
	
	
	public void close()
	{
		try
		{
			connection.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/*
	private void readDBConfig()
	{
		try
		{
			InputStreamReader reader = null;
			
			File f = new File("db.txt");
			if(f.exists())
			{
				logger.info("read from current directory");
				reader = new InputStreamReader(new FileInputStream(f));
			}
			else
			{
				reader = new InputStreamReader(DBImpl.class.getResourceAsStream("/config/db.txt")); 
			}
			
			BufferedReader br = new BufferedReader(reader);
			
			String line = br.readLine();

	        while (line != null) {
	            String[] params = line.split("=");
	        	if("HOST".equals(params[0]))
	        	{
	        		URL = params[1];
	        		int index = URL.indexOf("://");
	        		if( index < 0)
	        		{
	        			throw new Exception("url is invlid: " + URL);
	        		}
	        		else 
	        		{
	        			URL = "jdbc:mysql" + URL.substring(index);
	        		}
	        	}
	        	else if("USER".equals(params[0]))
	        	{
	        		USER_NAME = params[1];
	        	}
	        	else if("PASSWORD".equals(params[0]))
	        	{
	        		PASSWORD = params[1];
	        	}
	            
	            line = br.readLine();
	        }
		}catch(Exception e)
		{
			logger.info("read db config error: ", e);
		}
	}
	*/
	public DBImpl() throws Exception
	{
		//readDBConfig();
		connection = getConnection();
	}
	
	public Connection getConnection() throws ClassNotFoundException
	{
		Connection connection = null;
		Class.forName("com.mysql.jdbc.Driver");
	    
		try
		{
			String url = ActivityConfiguration.getInstance().getURL();
			String username = ActivityConfiguration.getInstance().getUSER_NAME();
			String password = ActivityConfiguration.getInstance().getPASSWORD();
			
			connection = DriverManager
		          .getConnection(url + "?user=" +  username + "&password=" + password +"&useUnicode=true&characterEncoding=utf8");
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return connection;
	}
	
	public List<LowLevelInteraction> getInteractions(String sql) throws SQLException
	{
		List<LowLevelInteraction> list = new ArrayList<LowLevelInteraction>();
	
		Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(Integer.MIN_VALUE);
		
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next())
		{
			LowLevelInteraction ui = new LowLevelInteraction();
			ui.setTimestamp(rs.getString("timestamp"));
			ui.setWindow(rs.getString("window"));
			ui.setParentWindow(rs.getString("parent_window"));
			ui.setApplication(rs.getString("application"));
			ui.setPx(rs.getInt("point_x"));
			ui.setPy(rs.getInt("point_y"));
			ui.setwRectLeft(rs.getInt("win_rect_left"));
			ui.setwRectTop(rs.getInt("win_rect_top"));
			ui.setwRectRight(rs.getInt("win_rect_right"));
			ui.setwRectBottom(rs.getInt("win_rect_bottom"));
			ui.setUiName(rs.getString("ui_name"));
			ui.setUiType(rs.getString("ui_type"));
			ui.setUiValue(rs.getString("ui_value"));
			ui.setParentUiName(rs.getString("parent_ui_name"));
			ui.setParentUiType(rs.getString("parent_ui_type"));
			ui.setUiBoundLeft(rs.getInt("ui_bound_left"));
			ui.setUiBoundTop(rs.getInt("ui_bound_top"));
			ui.setUiBoundRight(rs.getInt("ui_bound_right"));
			ui.setUiBoundBottom(rs.getInt("ui_bound_bottom"));
			ui.setHasScreen(rs.getBoolean("has_screen"));
			
			list.add(ui);
		}
		
		rs.close();
		stmt.close();
		
		return list;
	}
	
	public LowLevelInteraction getAnInteractions(String timestamp, String user) throws SQLException
	{
		String sql = "select * from tbl_interactions where user_name = '" + user + "' and timestamp = '" + timestamp + "'"; 
		
		List<LowLevelInteraction> list = getInteractions(sql);
		if(list.size() > 0)
		{
			return list.get(0);
		}
		return null;
	}
	
	public LowLevelInteraction getAnInteractionsWithScreen(String timestamp, String user)
	{
		String sql = "select *  from tbl_interactions"
				+ " where user_name = '" + user + "' and timestamp = '" + timestamp + "'"; 
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				LowLevelInteraction ui = new LowLevelInteraction();
				ui.setTimestamp(rs.getString("timestamp"));
				ui.setWindow(rs.getString("window"));
				ui.setParentWindow(rs.getString("parent_window"));
				ui.setApplication(rs.getString("application"));
				ui.setPx(rs.getInt("point_x"));
				ui.setPy(rs.getInt("point_y"));
				ui.setwRectLeft(rs.getInt("win_rect_left"));
				ui.setwRectTop(rs.getInt("win_rect_top"));
				ui.setwRectRight(rs.getInt("win_rect_right"));
				ui.setwRectBottom(rs.getInt("win_rect_bottom"));
				ui.setUiName(rs.getString("ui_name"));
				ui.setUiType(rs.getString("ui_type"));
				ui.setUiValue(rs.getString("ui_value"));
				ui.setParentUiName(rs.getString("parent_ui_name"));
				ui.setParentUiType(rs.getString("parent_ui_type"));
				ui.setUiBoundLeft(rs.getInt("ui_bound_left"));
				ui.setUiBoundTop(rs.getInt("ui_bound_top"));
				ui.setUiBoundRight(rs.getInt("ui_bound_right"));
				ui.setUiBoundBottom(rs.getInt("ui_bound_bottom"));
				ui.setHasScreen(rs.getBoolean("has_screen"));
				if(ui.isHasScreen())
				{
					ui.setScreen(ImageIO.read(rs.getBinaryStream("screen")));
				}
				
				return ui;
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			return null;
		}finally
		{
			try{
				rs.close();
				stmt.close();
			}catch(Exception e)
			{
				
			}
		}
		
	}
	
	public LowLevelInteraction getAnInteractionsWithScreen2(String timestamp, String user, boolean useScreen)
	{
		String sql = "select a.*, c.screen_status, c.screen as screen_in_group  from tbl_interactions a, tbl_group_interactions b, tbl_group_detail c"
				+ " where a.timestamp = c.interaction_time and b.group_id = c.group_id and a.user_name = '" + user + "' and a.timestamp = '" + timestamp + "'"; 
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				LowLevelInteraction ui = new LowLevelInteraction();
				ui.setTimestamp(rs.getString("timestamp"));
				ui.setWindow(rs.getString("window"));
				ui.setParentWindow(rs.getString("parent_window"));
				ui.setApplication(rs.getString("application"));
				ui.setPx(rs.getInt("point_x"));
				ui.setPy(rs.getInt("point_y"));
				ui.setwRectLeft(rs.getInt("win_rect_left"));
				ui.setwRectTop(rs.getInt("win_rect_top"));
				ui.setwRectRight(rs.getInt("win_rect_right"));
				ui.setwRectBottom(rs.getInt("win_rect_bottom"));
				ui.setUiName(rs.getString("ui_name"));
				ui.setUiType(rs.getString("ui_type"));
				ui.setUiValue(rs.getString("ui_value"));
				ui.setParentUiName(rs.getString("parent_ui_name"));
				ui.setParentUiType(rs.getString("parent_ui_type"));
				ui.setUiBoundLeft(rs.getInt("ui_bound_left"));
				ui.setUiBoundTop(rs.getInt("ui_bound_top"));
				ui.setUiBoundRight(rs.getInt("ui_bound_right"));
				ui.setUiBoundBottom(rs.getInt("ui_bound_bottom"));
				int screenStatus = rs.getInt("screen_status");
				ui.setHasScreen(screenStatus == 1);
				if(ui.isHasScreen() && useScreen)
				{
					ui.setScreen(ImageIO.read(rs.getBinaryStream("screen_in_group")));
				}
				
				return ui;
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			return null;
		}finally
		{
			try{
				rs.close();
				stmt.close();
			}catch(Exception e)
			{
				
			}
		}
		
	}
	
	public String getOverviewProcessTime() throws SQLException
	{
		String sql = "select * from tbl_gobal_var where param_name = 'overview_process_time'";
		Statement stmt = connection.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		
		if(rs.next())
		{
			return rs.getString("param_value");
		}
		
		rs.close();
		stmt.close();
		
		return null;
	}
	
	public void updateOverviewProcessTime(String t) throws SQLException
	{
		String sql = "update tbl_gobal_var set param_value = '" + t + "' where param_name = 'overview_process_time'";
		System.out.println(sql);
		
		Statement stmt = connection.createStatement();
		
		stmt.executeUpdate(sql);
		
		stmt.close();
	}
	
	public void insertOverview(List<CResource> list) throws SQLException
	{
		String sql = "insert into tbl_overview(resource, application, type, duration, lasttime, scope) values(?,?,?,?,?,?)";
		PreparedStatement pStat = connection.prepareStatement(sql);
		
		for(int i=0; i<list.size(); i++)
		{
			int index = 1;
			pStat.setString(index++, list.get(i).getName());
			pStat.setString(index++, list.get(i).getApplication());
			pStat.setString(index++, list.get(i).getType());
			pStat.setDouble(index++, list.get(i).getDuration());
			pStat.setString(index++, list.get(i).getLasttime());
			pStat.setString(index++, list.get(i).getScope());
			
			pStat.executeUpdate();
		}
		
		pStat.close();
	}
	
	public List<CResource> getOverviewData(String sql) throws SQLException
	{
		List<CResource> list = new ArrayList<CResource>();
		
		Statement stmt = connection.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next())
		{
			CResource r = new CResource();
			r.setName(rs.getString("resource"));
			r.setApplication(rs.getString("application"));
			r.setType(rs.getString("type"));
			r.setDuration(rs.getDouble("duration"));
			r.setLasttime(rs.getString("lasttime"));
			r.setScope(rs.getString("scope"));
			
			list.add(r);
		}
		
		rs.close();
		stmt.close();
		
		return list;
	}
	
	public List<CResource> getOverviewData(String[] app, String order) throws SQLException
	{
		String sql = "select * from tbl_overview";
		if(app != null && app.length !=0)
		{
			String condition = "application in (";
			for(int i=0; i<app.length; i++)
			{
				condition += "'" + app[i] + "',";
			}
			condition = condition.substring(0, condition.length()-1);
			condition += ")";
			sql += " where " + condition;
		}
		sql += " order by " + order + " desc";
		System.out.println(sql);
		
		return getOverviewData(sql);
	}
	
	public List<LowLevelInteraction> getInteractionsByUser(String user) throws SQLException
	{
		
		String sql = "select * from tbl_interactions where user_name = '" + user + "'" 
				+ "order by timestamp";
		
		return getInteractions(sql);
	}
	
	public List<LowLevelInteraction> getInteractionsByUser(String user, String timeCondition) throws SQLException
	{
		
		String sql = "select * from tbl_interactions where user_name = '" + user + "'" 
				+ " and " + timeCondition
				+ " order by timestamp";
		
		return getInteractions(sql);
	}
	
	public int insertGroupedInteraction(String groupTitle, String groupApp, String user)  throws SQLException
	{
		String sql = "insert into tbl_group_interactions(group_title, group_app, user_name) values(?, ?, ?)";
		
		PreparedStatement pStat = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		
		pStat.setString(1, groupTitle);
		pStat.setString(2, groupApp);
		pStat.setString(3, user);
		
		pStat.executeUpdate();
		
		ResultSet rs = pStat.getGeneratedKeys();
		int autoId = -1;
		if(rs.next())
		{
			autoId =  rs.getInt(1);
		}
		
		rs.close();
		pStat.close();
		
		return autoId;
	}
	
	public void insertGroupDetail(int groupId, String interactionTime, double duration) throws SQLException
	{
		String sql = "insert into tbl_group_detail(group_id, interaction_time, duration) values(?, ?, ?)";
		
		PreparedStatement pStat = connection.prepareStatement(sql);
		
		pStat.setInt(1, groupId);
		pStat.setString(2, interactionTime);
		pStat.setDouble(3, duration);
		
		pStat.executeUpdate();
		
		pStat.close();
	}
	
	public String getMaxTimeOfHasGrouped(String user) throws SQLException
	{
		String sql = "select max(interaction_time) from tbl_group_detail a, tbl_group_interactions b where a.group_id = b.group_id and b.user_name = '" + user + "'";
		
		Statement stat = connection.createStatement();
		
		ResultSet rs = stat.executeQuery(sql);
		if(rs.next())
		{
			return rs.getString(1);
		}
		
		rs.close();
		stat.close();
		
		return "";
	}
	
	public List<GroupedInteraction> getGroupInteractions(String sql)
	{
		try
		{
			List<GroupedInteraction> list = new ArrayList<GroupedInteraction>();
			Statement stat = connection.createStatement();
			
			ResultSet rs = stat.executeQuery(sql);
			
			int preId = -1;
			
			while(rs.next())
			{
				int groupId = rs.getInt("group_id");
				String groupTitle = rs.getString("group_title");
				String groupApp = rs.getString("group_app");
				String interactionTime = rs.getString("interaction_time");
				double duration = rs.getDouble("duration");
				int screenStatus = rs.getInt("screen_status");
				
				if(preId == groupId)
				{
					GroupDetail detail = new GroupDetail();
					detail.setGroupId(groupId);
					detail.setTime(interactionTime);
					detail.setScreenStatus(screenStatus);
					
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
					
					List<GroupDetail> details = new ArrayList<GroupDetail>();
					details.add(detail);
					a.setDetails(details);
					list.add(a);
					
					preId = groupId;
				}
			}
			
			System.out.println("finish retrieve history activities");
			
			rs.close();
			stat.close();
			return list;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public List<GroupedInteraction> getGroupInteractionsInDay(String day, String user)
	{
		String sql = "select a.group_id, group_title, group_app, interaction_time, duration, screen_status from tbl_group_interactions a, tbl_group_detail b where a.group_id = b.group_id ";
		sql += "and user_name = '" + user + "' ";
		
		String from = day + " 00:00:00.000";
		String to = day + " 24:00:00.000";
		
		sql += " and interaction_time> '" + from + "' and interaction_time<'" + to + "' ";
		
		sql += " order by group_id, interaction_time";
		
		return getGroupInteractions(sql);
	}
	
	public List<GroupedInteraction> getGroupInteractionsGreaterThan(String t, String user) throws SQLException
	{
		System.out.println("retrieve history activities");
		
		String sql = "select a.group_id, group_title, group_app, interaction_time, duration, screen_status from tbl_group_interactions a, tbl_group_detail b where a.group_id = b.group_id ";
		sql += "and user_name = '" + user + "' ";
		
		if(t!=null && !"".equals(t))
		{
			sql += " and interaction_time> '" + t + "'";
		}
		
		sql += " order by group_id";
		
		System.out.println(sql);
		
		return getGroupInteractions(sql);
		
	}
	
	public void updateGroupDetail(String user, String time, int screenStatus, BufferedImage img)  throws Exception
	{
		CallableStatement cs = null;
		try
		{
			System.out.println("update group detail: " + time + "/" + screenStatus);
			
			String sql = "call update_group_detail(?, ?, ?, ?)";
			
			cs = connection.prepareCall(sql);
			cs.setString(1, user);
			cs.setString(2, time);
			cs.setInt(3, screenStatus);
			
			InputStream is = null;
			if(img != null)
			{
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(img, "png", os);
				is = new ByteArrayInputStream(os.toByteArray());
			}
			cs.setBlob(4, is);
			
			cs.executeUpdate();
		}
		catch(Exception e)
		{
			System.out.println("exception: " + time);
			e.printStackTrace();
		}
		finally
		{
			if(cs != null) cs.close();
		}
	}
	
	public List<String> getAllUsers()
	{
		String sql = "select distinct user_name from tbl_interactions";
		
		List<String> userList = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				userList.add(rs.getString("user_name"));
			}
			stmt.close();
			rs.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return userList;
	}
}

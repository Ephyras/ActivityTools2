package activity.web.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.util.InteractionUtil;
import activity.web.db.DBResultSet;
import activity.web.db.MySqlImpl;

public abstract class BackendProcesser {
	Logger logger = Logger.getLogger(BackendProcesser.class.getName());
	
	static{
		System.loadLibrary( "opencv_java249" );
	}
	
	protected MySqlImpl db;
	
	public BackendProcesser()
	{
		db = new MySqlImpl();
	}
	
	public List<String> getAllUsers()
	{
		String sql = "select distinct user_name from tbl_interactions";
		DBResultSet rs = db.retrieveResultSet(sql);
		
		List<String> userList = new ArrayList<String>();
		
		for(int i=0; i<rs.getRecords().size(); i++)
		{
			String u = rs.getRecords().get(i).getString("user_name");
			userList.add(u);
			
			//logger.info(u + "'s last time: " + getLastGroupedInteractionTime(u));
		}
		
		return userList;
	}
	
	public List<LowLevelInteraction> getInteractions(String user, String time)
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
	
	public void finish()
	{
		db.close();
		logger.info("finish");
	}
	
	public abstract void process();
	
}

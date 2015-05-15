package activity.web.manager;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.util.*;
import activity.web.db.DBRecord;
import activity.web.db.DBResultSet;
import activity.web.opencv.*;

public class ActionMatcher extends BackendProcesser{
	static Logger logger = Logger.getLogger(ActionMatcher.class.getName());
	
	public static List<IconItem> icons = new ArrayList<IconItem>();
	public static Map<String, IconFilter> iconFilters = new HashMap<String,IconFilter>();
	static
	{
		try
		{
			//System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
			
			IconItem bp = new IconItem("breakpoint");
			bp.setItem(CVUtil.readInputStreamIntoMat(TemplateMatcher.class.getResourceAsStream("/icon/templates/bp.png")));
			bp.setItemMask(CVUtil.readInputStreamIntoMat(TemplateMatcher.class.getResourceAsStream("/icon/templates/bp_mask.png")));
			
			icons.add(bp);
			
			iconFilters.put(bp.getName(), new IconFilter("eclipse.exe", "pane"));
			
		}catch(Exception e)
		{
			logger.info(e.getMessage(), e);
		}
	}
	
	public String getLastProcessTime(String user)
	{
		String sql = "select max(timestamp) as lasttime from tbl_interactions where user_name = '" + user + "' and ui_type = 'Template Matching'";
		DBResultSet rs = db.retrieveResultSet(sql);
		
		if(rs.getRecords().size() > 0)
		{
			return rs.getRecords().get(0).getString("lasttime");
		}
		
		return "";
	}
	
	public BufferedImage getScreenshot(String user, String time)
	{
		String sql = "select screen from tbl_interactions where "
				+ "user_name='" + user + "' and timestamp = '" + time + "'";
		
		DBResultSet rs = db.retrieveResultSet(sql);
		if(rs.getRecords().size() > 0)
		{
			DBRecord r =  rs.getRecords().get(0);
			return  r.getBufferedImage("screen");
		}
		
		logger.info("no screenshot");
		return null;
		
	}
	
	public boolean updateInteraction(String user, String time, String actionName, Rect r)
	{
		String sql = "update tbl_interactions set ui_name=?, ui_type=?, ui_bound_left=?, ui_bound_top=?, ui_bound_right=?, ui_bound_bottom=? "
				+ "where user_name=? and timestamp=?";
		
		logger.info(sql);
		
		return db.update(sql, actionName, "Template Matching", r.x, r.y, r.x+r.width, r.y+r.height, user, time);
	}
	
	public void process()
	{
		TemplateMatcher tm = new TemplateMatcher();
		
		for(String user : getAllUsers())
		{
			logger.info("action matching: " + user);
			String lasttime = getLastProcessTime(user);
			
			List<LowLevelInteraction> actions = this.getInteractions(user, lasttime);
			
			for(IconItem icon : icons)
			{
				List<Rect> preDetects = null;
				for(LowLevelInteraction li : actions)
				{
					IconFilter filter = iconFilters.get(icon.getName());
					if(!filter.getApplication().equals(li.getApplication()) || 
							!InteractionUtil.isControlType(filter.getUiType(), li.getUiType()) || !li.isHasScreen())
					{
						continue;
					}
					
					List<Rect> detects = null;
					if(li.isHasScreen())
					{
						BufferedImage img = getScreenshot(user, li.getTimestamp());
						if(img == null) continue;
						
						Mat m = CVUtil.img2Mat(img);
						ImageItem imgItem = new ImageItem();
						imgItem.setImage(m);
						
						detects = tm.match(imgItem, icon);
						
						preDetects = detects;
					}
					else if(preDetects != null) 
					{
						detects = preDetects;
					}
					
					if(detects == null) continue;
					
					for(int i=0; i<detects.size(); i++)
					{
						//Core.rectangle(imgItem.getImage(), detects.get(i).tl(), detects.get(i).br(), new Scalar(0,0,255));
						
						Rect r = detects.get(i);
						double x = (r.tl().x + r.br().x) / 2;
						double y = (r.tl().y + r.br().y) / 2;
						
						int px = li.getPx();
						int py = li.getPy();
						int ux = li.getUiBoundLeft();
						int uy = li.getUiBoundTop();
						int uw = li.getUiBoundRight() - li.getUiBoundLeft();
						int uh = li.getUiBoundBottom() - li.getUiBoundTop();
						
						Rect uiRect = new Rect(ux, uy, uw, uh);
						if(Math.abs(x - li.getPx()) <= r.width && Math.abs(y - li.getPy()) <= r.height)
						//if(uiRect.contains(new Point(x,y)))
						{
							updateInteraction(user, li.getTimestamp(), icon.getName(), r);
							
							logger.info(li.getTimestamp() + " - " + icon.getName() + " is detected (" + r.x + "," + r.y + " - " 
									+ px + "," + py + ") [" + ux + "," + uy + ","+uw+","+uh+"]");
							
							break;
						}
					}
				}
			}
		}
		
		finish();
	}
	
	public static void main(String[] args) throws Exception
	{
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
		DataManager dm = new DataManager("baolingfeng");
		LowLevelInteraction li = dm.getAnInteractions2("2015-02-02 20:14:32.052", false);
		
		BufferedImage img = dm.getScreenshot2(li.getTimestamp());
		
		ImageIO.write(img, "png", new File("D:/temp/out2.png"));
		
		Mat m = CVUtil.img2Mat(img);
		Highgui.imwrite("D:/temp/out.png", m);
		
		ImageItem imgItem = new ImageItem();
		imgItem.setImage(m);
		
		TemplateMatcher tm = new TemplateMatcher();
		
		for(IconItem icon : icons)
		{
			List<Rect> detects = tm.match(imgItem, icon);
			
			for(int i=0; i<detects.size(); i++)
			{
				Rect r = detects.get(0);
				logger.info(r.x + "," + r.y);
				Core.rectangle(imgItem.getImage(), detects.get(i).tl(), detects.get(i).br(), new Scalar(0,0,255));
			}
		}
		
		Highgui.imwrite("D:/temp/out3.png", imgItem.getImage());
	}
}

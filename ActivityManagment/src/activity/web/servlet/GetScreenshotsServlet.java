package activity.web.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import activity.web.manager.*;
import activity.web.db.*;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.util.*;
/**
 * Servlet implementation class GetScreenshotsServlet
 */
@WebServlet("/GetScreenshotsServlet")
public class GetScreenshotsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	Logger logger = Logger.getLogger(GetScreenshotsServlet.class.getName());
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetScreenshotsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.info("get screenshots...");
		//String id = request.getParameter("group");
		String time = request.getParameter("time");
		String acc = request.getParameter("acc");
		
		boolean isAcc = true;
		if(acc == null || !"true".equals(acc))
		{
			isAcc = false;
		}
		//logger.info(id + "/" + time);
		
		HttpSession session = request.getSession();
		
		String user = (String)session.getAttribute("user");
		
		//DataManager dm = (DataManager)session.getAttribute("dataManager");
		//if(dm == null) return;
		
		String contextPath = request.getServletContext().getContextPath();
		
		BufferedImage img = getScreenshot(time, user, contextPath, isAcc);
		if(img == null) return;
		
		if(!isAcc)
			img = (BufferedImage)MyImageUtil.getScaledImage(img, 1000, 800);
		
		response.setContentType("image/png");
		ImageIO.write(img, "png", response.getOutputStream() );
		//((DataBufferByte) img.getData().getDataBuffer()).getData();
		//response.getOutputStream().write();
		//PrintWriter writer = response.getWriter();
		//writer.close();
	}
	
	public BufferedImage getScreenshot(String time, String user, String contextPath, boolean isAcc)
	{
		DataManager dm = new DataManager(user);
		dm.setContextPath(contextPath);
		
		BufferedImage img = dm.getScreenshot(time);
		if(img == null)
		{
			logger.info("image is null");
			return null;
		}
		
		LowLevelInteraction ll = dm.getAnInteractions(time, false);
		dm.close();
		
		if(ll == null) 
		{
			logger.info("interaction info is null");
			return null;
		}
		
		int left = ll.getUiBoundLeft();
		int top = ll.getUiBoundTop();
		int right = ll.getUiBoundRight();
		int bottom = ll.getUiBoundBottom();
		
		if(isAcc)
		{
			int x = left<0 ? 0:left;
			int y = top<0 ? 0 : top;
			int w = right - left <=0 ? 0 : right-left;
			int h = bottom - top <=0 ? 0 : bottom-top;
			/*
			if(InteractionUtil.isControlType("pane", ll.getUiType()) 
					&& ll.getParentUiName().endsWith("java"))
			{
				logger.info("java pane to find debug point");
				x = ll.getPx() - 20;
				y = ll.getPy() - 20;
				w = 40;
				h = 40;
				//img.getSubimage(ll.getPx()-20, ll.getPy()-20)
			}
			*/
			
			try
			{
				img = img.getSubimage(x, y, w, h);
			}catch(Exception e)
			{
				logger.info(e.getMessage(), e);
				return null;
			}
		}
		else 
		{
			img = MyImageUtil.drawRectOnImage(img, left, top, right-left, bottom-top);
			
			int cx = left-100;
			int cy = top-30;
			
			cx = cx < 30 ? 30 : cx;
			cy = cy < 30 ? 30 : cy;
			
			
			img = MyImageUtil.drawStringOnImage(img, getUIDetail(ll), cx, cy);
		}
		
		img = MyImageUtil.drawCircleOnImage(img, ll.getPx(), ll.getPy(), 10);
		
		return img;
		
		/*
		MySqlImpl db = new MySqlImpl();
		
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
		
		db.close();
		return null;
		*/
	}
	
	public String getUIDetail(LowLevelInteraction ll)
	{
		String app = ll.getApplication();
		String append[] = {".java", ".jsp", ".txt", "css", ".xml"};
		if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			if(InteractionUtil.isControlType("edit", ll.getUiType()))
			{
				for(int i=0; i<append.length; i++)
				{
					if(ll.getParentUiName().contains(append[i]))
					{
						return ll.getParentUiName();
					}
				}

				
				return ll.getParentUiName() + "'s content: " + ll.getUiValue().substring(0, 20);
				
			}
		}
		
		String action = "";
		String uiname = ll.getUiName();
		if(uiname.length()>20) 
		{
			uiname = uiname.substring(0, 20) + "...";
		}
		String uivalue = ll.getUiValue();
		if(uivalue.length()>20) 
		{
			uivalue = uivalue.substring(0, 20) + "...";
		}
		if("".equals(ll.getUiName()) && "".equals(ll.getUiValue()))
		{
			action = "No Accessibility Information";
		}
//		else if("".equals(ll.getUiValue()))
//		{
//			action += uiname;
//		}
		else if("".equals(ll.getUiName()))
		{
			action = uivalue;
		}
		else
		{
			action = uiname;
		}
		
		return action;
	}
	
}

package activity.web.servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import activity.web.manager.DataManager;
import cn.zju.edu.blf.dao.*;
import cn.zju.edu.util.CommonUtil;
/**
 * Servlet implementation class GetAccessServlet
 */
@WebServlet("/GetAccessServlet")
public class GetAccessServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	Logger logger = Logger.getLogger(GetAccessServlet.class.getName());
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetAccessServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//String title = request.getParameter("title");
		//byte[] bytes = title.getBytes(StandardCharsets.ISO_8859_1);
		//title = new String(bytes, StandardCharsets.UTF_8);
		
//		String info = request.getParameter("info");
//		boolean isDay = Boolean.parseBoolean(request.getParameter("isday"));
		
		String timestamp = request.getParameter("timestamp");
		
		logger.info(timestamp);
		
//		String app = info.split("/")[0];
//		String time = info.split("/")[1];
		
		HttpSession session = request.getSession();
		
//		DataManager dm = (DataManager)session.getAttribute("dataManager");
//		List<GroupedInteraction> groups = (List<GroupedInteraction>)session.getAttribute("aggrInteractions");
		Map<String, List<ActionDetail>> actionsMap = (Map<String, List<ActionDetail>>)session.getAttribute("actiondetail");
		if(actionsMap == null) 
		{
			response.getWriter().println("Session time out");
			return;
		}
		
		try
		{
			//HashMap<String, List<ActionDetail>> actions = dm.getLLInteractionsForDetail(title, app, time, isDay, groups);
			//List<ActionDetail> actions = dm.getLLInteractionsForDetail(title, app, time, isDay, groups);
			
			List<ActionDetail> actions = actionsMap.get(timestamp);
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			String res = CommonUtil.toJson4Action(actions);
			logger.info(res);
			
			response.getWriter().println(res);
		}catch(Exception e)
		{
			logger.info(e.getMessage(), e);
		}
	}

}

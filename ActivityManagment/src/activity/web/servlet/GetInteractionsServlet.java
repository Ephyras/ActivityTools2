package activity.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.util.PropertyFilter;

import org.apache.log4j.Logger;

import activity.web.manager.*;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.util.*;
import activity.web.db.dao.*;
/**
 * Servlet implementation class GetInteractionsServlet
 */
@WebServlet("/GetInteractionsServlet")
public class GetInteractionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
      
	Logger logger = Logger.getLogger(GetInteractionsServlet.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetInteractionsServlet() {
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
		request.setCharacterEncoding("UTF-8");
		
		String user = request.getParameter("user");
		String eclipseCheck = request.getParameter("eclipse");
		String browserCheck = request.getParameter("browser");
		String officeCheck = request.getParameter("office");
		String vsCheck = request.getParameter("vs");
		String otherCheck = request.getParameter("other");
		String filter = request.getParameter("filter");
		String reset = request.getParameter("reset");
		
		int interval = 120;
		String strInterval = request.getParameter("interval");
		if(strInterval != null)
		{
			interval = Integer.parseInt(strInterval);
		}
		HttpSession session = request.getSession();
		session.getAttribute("user");
		
		if(reset != null && "true".equals(reset))
		{
			session.removeAttribute("coordinatedGroup");
		}
		
		logger.info("servlet to get interaction data: " + user + "/" + eclipseCheck + "/" + browserCheck + "/" + officeCheck + "/" + vsCheck + "/" + otherCheck+"/" + strInterval);
		try
		{
			DataManager dm = (DataManager)session.getAttribute("dataManager");
			if(dm == null || strInterval != null)
			{
				dm = new DataManager(user);
				String contextPath = request.getServletContext().getContextPath();
				dm.setContextPath(contextPath);
				
				session.setAttribute("dataManager", dm);
			}
			
			EventManager em = (EventManager)session.getAttribute("eventManager");
			if(em == null || strInterval != null)
			{
				em = new EventManager(user);
				session.setAttribute("eventManager", em);
			}
			em.setEclipseCheck(eclipseCheck);
			em.setBrowserCheck(browserCheck);
			em.setOfficeCheck(officeCheck);
			em.setVsCheck(vsCheck);
			em.setOtherCheck(otherCheck);
			if(filter != null)
				em.setFilterQuery(filter);
			
			List<GroupedInteraction> allinteactions = (List<GroupedInteraction>)session.getAttribute("allinteractions");
			//List<GroupedInteraction> filteredGroup = (List<GroupedInteraction>)session.getAttribute("interactions");
			List<GroupedInteraction> groupByDay = (List<GroupedInteraction>)session.getAttribute("interatcionsByDay");
			List<GroupedInteraction> aggrGroup = (List<GroupedInteraction>)session.getAttribute("aggrInteractions");
			List<GroupedInteraction> coordinatedGroup = (List<GroupedInteraction>)session.getAttribute("coordinatedGroup");
			
			if(allinteactions == null || strInterval != null)
			{
				logger.info("retrieve data from database...");
				allinteactions = dm.retrieveGroupInteractions(DateUtil.getDayBeforeOrAfter(new Date(), -interval));
				session.setAttribute("allinteractions", allinteactions);
			}
			
			List<GroupedInteraction> filteredGroup = new ArrayList<GroupedInteraction>();
			for(int i=0; i<allinteactions.size(); i++)
			{
				if(!em.filterEvent(allinteactions.get(i))) 
				{
					filteredGroup.add(allinteactions.get(i));
				}
			}
			groupByDay = dm.groupByDay(filteredGroup);
			aggrGroup = dm.aggrAllGroup(filteredGroup);
			session.setAttribute("interactions", filteredGroup);
			session.setAttribute("interatcionsByDay", groupByDay);
			session.setAttribute("aggrInteractions", aggrGroup);
			
			
			List<GroupedInteraction> group = groupByDay;
			if(coordinatedGroup != null)
			{
				group = coordinatedGroup;
			}
			
			List<EventDao> events = new ArrayList<EventDao>();
			for(int i=0; i<group.size(); i++)
			{
				String title = group.get(i).getTitle();
				
				String time = group.get(i).getDetails().get(0).getTime();
				
				EventDao e = new EventDao();
				e.setTitle(title);
				e.setStart(time);
				e.setDescription("test");
				e.setApplication(group.get(i).getApplication());
				e.setColor(em.getColor(group.get(i)));
				e.setDuration(group.get(i).getDuration());
				
				events.add(e);
			}
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			String res = CommonUtil.toJson(events);
			logger.info(res);
			pw.println(res);
		}catch(Exception e)
		{
			logger.info(e.toString());
		}
	}
	
}

package activity.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import activity.web.db.dao.EventDao;
import activity.web.manager.DataManager;
import activity.web.manager.EventManager;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.util.CommonUtil;
import cn.zju.edu.util.DateUtil;

/**
 * Servlet implementation class FindCoordinatedActivityServlet
 */
@WebServlet("/FindCoordinatedActivityServlet")
public class FindCoordinatedActivityServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	Logger logger = Logger.getLogger(FindCoordinatedActivityServlet.class.getName());
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FindCoordinatedActivityServlet() {
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
		logger.info("find coordinated activities...");
		HttpSession session = request.getSession();
		
		String user = request.getParameter("user");
		if(user == null || "".equals(user))
		{
			user = (String)session.getAttribute("user");
		}
		
		int interval = 90;
		String strInterval = request.getParameter("interval");
		if(strInterval != null)
		{
			interval = Integer.parseInt(strInterval);
		}
		
		String title = request.getParameter("title");
		String application = request.getParameter("application");
		logger.info(title + "/" + application);
		
		DataManager dm = new DataManager(user);
		
		EventManager em = (EventManager)session.getAttribute("eventManager");
		if(em == null)
		{
			em = new EventManager(user);
		}
		
		List<GroupedInteraction> gi = (List<GroupedInteraction>)session.getAttribute("interactions");
		List<GroupedInteraction> groupByDay = (List<GroupedInteraction>)session.getAttribute("interatcionsByDay");
		/*
		if(gi == null)
		{
			logger.info("get the new interactions data...");
			gi = dm.retrieveGroupInteractions(DateUtil.getDayBeforeOrAfter(new Date(), -interval));
			List<GroupedInteraction> filteredGroup = new ArrayList<GroupedInteraction>();
			for(int i=0; i<gi.size(); i++)
			{
				if(!em.filterEvent(gi.get(i))) 
				{
					filteredGroup.add(gi.get(i));
				}
			}
			
			groupByDay = dm.groupByDay(filteredGroup);
			 
			session.setAttribute("interactions", filteredGroup);
			session.setAttribute("interatcionsByDay", groupByDay);
		}
		*/
		
		try
		{
			List<GroupedInteraction> coordinatedGroup = new ArrayList<GroupedInteraction>();
			int MAX_INTERVAL = 1 * 60;
			for(int i=0; i<groupByDay.size(); i++)
			{
				GroupedInteraction g = groupByDay.get(i);
				if(g.getDetails().size() <= 0) break;
				
				String t1 = g.getDetails().get(0).getTime();
				
				if(title.equals(g.getTitle()) && application.equals(g.getApplication()))
				{
					coordinatedGroup.add(g);
					
					for(int j=0; j<g.getTimeslots().size(); j+=2)
					{
						String from1 = g.getTimeslots().get(j);
						String to1 = g.getTimeslots().get(j+1);
						
						boolean flag = false;
						for(int k=0; k<groupByDay.size(); k++)
						{
							GroupedInteraction g2 = groupByDay.get(k);
							if(g2.getDetails().size() <= 0) continue;
							
							String t2 = g2.getDetails().get(0).getTime();
							
							if(!DateUtil.isSameDay(t1, t2)) continue;
							
							for(int m=0; m<g2.getTimeslots().size(); m+=2)
							{
								String from2 = g2.getTimeslots().get(m);
								String to2 = g2.getTimeslots().get(m+1);
								
								if(!(DateUtil.calcInterval(to1, from2) > MAX_INTERVAL ||  
										DateUtil.calcInterval(to2, from1) >MAX_INTERVAL))
								{
									coordinatedGroup.add(g2); 
									flag = true;
									break;
								}
							}
						}
						if(flag) break;
					}
					
					/*
					int j = i-1;
					while(j>=0)
					{
						GroupedInteraction g2 = groupByDay.get(j);
						if(g2.getDetails().size() <= 0) continue;
						
						String t2 = g2.getDetails().get(0).getTime();
						
						if(!DateUtil.isSameDay(t1, t2)) break;
						
						for(int m=0; m<g.getDetails().size(); m++)
						{
							boolean flag = false;
							for(int n=0; n<g2.getDetails().size(); n++)
							{
								String dt1 = g.getDetails().get(m).getTime();
								String dt2 = g2.getDetails().get(n).getTime();
								
								if(Math.abs(DateUtil.calcInterval(dt1, dt2)) < 60 * 10 
										&& g2.getDetails().get(n).getDuration() > 5)
								{
									coordinatedGroup.add(g2);
									flag = true;
									break;
								}
							}
							if(flag) break;
						}
						j--;
					}
					
					j = i+1;
					while(j<groupByDay.size())
					{
						GroupedInteraction g2 = groupByDay.get(j);
						if(g2.getDetails().size() <= 0) continue;
						
						String t2 = g2.getDetails().get(0).getTime();
						
						if(!DateUtil.isSameDay(t1, t2)) break;
						
						for(int m=0; m<g.getDetails().size(); m++)
						{
							boolean flag = false;
							for(int n=0; n<g2.getDetails().size(); n++)
							{
								String dt1 = g.getDetails().get(m).getTime();
								String dt2 = g2.getDetails().get(n).getTime();
								
								if(Math.abs(DateUtil.calcInterval(dt1, dt2)) < 60 * 10
										&& g2.getDetails().get(n).getDuration() > 5)
								{
									coordinatedGroup.add(g2);
									flag = true;
									break;
								}
							}
							if(flag) break;
						}
						j++;
					}
					*/
				}
			}
			
			session.setAttribute("coordinatedGroup", coordinatedGroup);
			
			List<EventDao> events = new ArrayList<EventDao>();
			for(int i=0; i<coordinatedGroup.size(); i++)
			{
				title = coordinatedGroup.get(i).getTitle();
				
				String time = coordinatedGroup.get(i).getDetails().get(0).getTime();
				
				EventDao e = new EventDao();
				e.setTitle(title);
				e.setStart(time);
				e.setDescription("test");
				e.setApplication(coordinatedGroup.get(i).getApplication());
				e.setColor(em.getColor(coordinatedGroup.get(i)));
				e.setDuration(coordinatedGroup.get(i).getDuration());
				
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
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		
		
	}

}

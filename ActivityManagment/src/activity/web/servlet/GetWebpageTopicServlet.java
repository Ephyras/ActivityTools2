package activity.web.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import activity.web.postproc.WebpageTitleProcesser;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.util.DateUtil;

/**
 * Servlet implementation class GetWebpageTopicServlet
 */
@WebServlet("/GetWebpageTopicServlet")
public class GetWebpageTopicServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	Logger logger = Logger.getLogger(GetWebpageTopicServlet.class.getName());
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetWebpageTopicServlet() {
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
		
		HttpSession session = request.getSession();
		
		List<GroupedInteraction> aggrGroup = (List<GroupedInteraction>)session.getAttribute("aggrInteractions");
		if(aggrGroup == null)
		{
			return;
		}
		
		String day = request.getParameter("day");
		String eclipseCheck = request.getParameter("eclipse");
		String browserCheck = request.getParameter("browser");
		String officeCheck = request.getParameter("office");
		String vsCheck = request.getParameter("vs");
		String otherCheck = request.getParameter("other");
		
		int beforeDay = 10;
		if(day != null && !"".equals(day)) 
		{
			beforeDay = Integer.parseInt(day);
		}
		
		String dateFilter = DateUtil.getDayBeforeOrAfter(new Date(), -beforeDay);
		logger.info("topic day: " + dateFilter);
		
		WebpageTitleProcesser wp = new WebpageTitleProcesser();
		wp.setEclipseCheck(eclipseCheck);
		wp.setBrowserCheck(browserCheck);
		wp.setOfficeCheck(officeCheck);
		wp.setVsCheck(vsCheck);
		wp.setOtherCheck(otherCheck);
		
		String res = wp.process(dateFilter, aggrGroup);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		logger.info(res);
		
		response.getWriter().write(res);
	}

}

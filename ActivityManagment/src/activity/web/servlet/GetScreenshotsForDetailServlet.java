package activity.web.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.ActionDetail;

/**
 * Servlet implementation class GetScreenshotsForDetailServlet
 */
@WebServlet("/GetScreenshotsForDetailServlet")
public class GetScreenshotsForDetailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	Logger logger = Logger.getLogger(GetScreenshotsForDetailServlet.class.getName());
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetScreenshotsForDetailServlet() {
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
		
		String timestamp = request.getParameter("timestamp");
		
		Map<String, List<String>> map = (Map<String, List<String>>)session.getAttribute("screendetail");
		if(map == null) 
		{
			response.getWriter().println("Session time out");
			return;
		}
		
		List<String> times = map.get(timestamp);
		if(times.size()>0)
		{
			logger.info("no screenshot");
		}
		
		String contextPath = request.getServletContext().getContextPath();
		String base = contextPath + "/GetScreenshotsServlet?time=";
		
		String res = "<div class=\"slider-wrapper theme-bar\"><div class=\"ribbon\"></div><div id=\"slider\" class=\"nivoSlider\">";
		
		for(int i=0; i<times.size(); i++)
		{
			
			res += "<img src=\"" + base+times.get(i) + "\"/>";
		}
		
		res += "</div></div>";
		
		response.getWriter().println(res);
	}

}

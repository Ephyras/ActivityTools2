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

import activity.web.postproc.QueryProcesser;
import activity.web.postproc.WebpageTitleProcesser;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.util.DateUtil;

/**
 * Servlet implementation class GetKeywordServlet
 */
@WebServlet("/GetKeywordServlet")
public class GetKeywordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	Logger logger = Logger.getLogger(GetKeywordServlet.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetKeywordServlet() {
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
		
		List<GroupedInteraction> interactions = (List<GroupedInteraction>)session.getAttribute("interactions");
		if(interactions == null)
		{
			return;
		}
		
		String day = request.getParameter("day");
		
		int beforeDay = Integer.parseInt(day);
		String dateFilter = DateUtil.getDayBeforeOrAfter(new Date(), -beforeDay);
		logger.info("keyword: " + dateFilter);
		
		QueryProcesser qp = new QueryProcesser();
		String res = qp.process(dateFilter, interactions);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		logger.info(res);
		
		response.getWriter().write(res);
	}

}

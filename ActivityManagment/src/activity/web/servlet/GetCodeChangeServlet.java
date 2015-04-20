package activity.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.*;
import cn.zju.edu.util.CommonUtil;
/**
 * Servlet implementation class GetCodeChangeServlet
 */
@WebServlet("/GetCodeChangeServlet")
public class GetCodeChangeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	Logger logger = Logger.getLogger(GetCodeChangeServlet.class.getName());
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetCodeChangeServlet() {
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
		logger.info("get code change");
		
		request.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession();
		List<CodeChange> changes = (List<CodeChange>)session.getAttribute("codechanges");
		if(changes == null)
		{
			logger.info("session timeout");
			return;
		}
		
		String type = request.getParameter("type");
		String time = request.getParameter("time");
		if(type == null || time == null) return;
		
		time = time.trim();
		logger.info(type + "/" + time); 
		
		String resText = "No Content";
		CodeChange change = null;
		for(int i=0; i<changes.size(); i++)
		{
			CodeChange c = changes.get(i);
			
			if(time.equals(c.getTime()))
			{
				change = c;
				
				
			}
		}
		
		if(change == null) return;
		PrintWriter writer = response.getWriter();
		
		if("source".equals(type))
		{
			writer.print(change.getSource());
		}
		else if("change".equals(type))
		{
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			List<CodeChangeDetail> data = change.getDetail();
			String res = CommonUtil.toJson4CodeChange(data);
			logger.info(res);
			
			writer.println(res);
		}
		
		writer.close();
		
		//logger.info(resText);
		
		
	}

}

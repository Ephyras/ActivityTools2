 <%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="activity.web.manager.*" %>
<%@ page import="cn.zju.edu.blf.dao.*" %>
<%@ page import="cn.zju.edu.util.*" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%
	request.setCharacterEncoding("UTF-8");

	String title = request.getParameter("title");
	byte[] bytes = title.getBytes(StandardCharsets.ISO_8859_1);
	title = new String(bytes, StandardCharsets.UTF_8);
	
	String info = request.getParameter("info");
	boolean isDay = Boolean.parseBoolean(request.getParameter("isday"));
	Logger logger = Logger.getLogger(this.getClass().getName());

	String contextPath = request.getServletContext().getContextPath();
	
	logger.info(title + "/" + info);
	
	String app = info.split("/")[0];
	String time = info.split("/")[1];
	
	DataManager dm = (DataManager)session.getAttribute("dataManager");
	List<GroupedInteraction> groupByDay = (List<GroupedInteraction>)session.getAttribute("interatcionsByDay");
	if(dm == null || groupByDay == null) 
	{
		response.getWriter().println("Session time out");
		return;
	}
	
	for(int i=0; i<groupByDay.size(); i++)
	{
		GroupedInteraction g = groupByDay.get(i);
		if(g.getDetails().size() <= 0) continue;
		
		String t = g.getDetails().get(0).getTime();
		Date d = DateUtil.formatTime(t);
		String t2 = DateUtil.fromDate(d, "yyyy-MM-dd");
		if(isDay && ! time.equals(t2)) continue;
		
		if(title.equals(g.getTitle()) && app.equals(g.getApplication()))
		{
			logger.info(t);
%>
<div class="slider-wrapper theme-bar">
    <div class="ribbon"></div>
		<div id="slider" class="nivoSlider">
<% 
			for(int j=0; j<g.getDetails().size(); j++)
			{
				if(g.getDetails().get(j).getScreenStatus() == 1)
				{
					int groupId = dm.getGroupId(g.getDetails().get(j).getTime());
					String src = contextPath + "/GetScreenshotsServlet?time="+g.getDetails().get(j).getTime();
%>					
					<img src="<%=src %>" />
<%
				}
			}
			
			break;
		}
	}
	
%>
</div></div>

<link rel="stylesheet" href="../lib/themes/bar/bar.css" type="text/css" />
<script>
$('#slider').nivoSlider({
	directionNav: true,
	prevText: 'Prev',                 // Prev directionNav text
    nextText: 'Next',                 // Next directionNav text
    manualAdvance: true
});
</script>
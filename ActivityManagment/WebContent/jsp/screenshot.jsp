 <%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="activity.web.manager.*" %>
<%@ page import="cn.zju.edu.blf.dao.*" %>
<%@ page import="cn.zju.edu.util.*" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.Map.Entry" %>
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
	List<GroupedInteraction> groups = (List<GroupedInteraction>)session.getAttribute("aggrInteractions");
	if(dm == null || groups == null) 
	{
		response.getWriter().println("Session time out");
		return;
	}
	
	Map<String, List<String>> map = dm.getScreenshotsForDetail(title, app, time, isDay, groups);
	session.setAttribute("screendetail", map);
%>

<table style="width:100%">
	<tr> 
	<td width="20%" style="vertical-align: top;">
		<div id="jstree_div">
			<ul>
			<% 
				for(Entry<String, List<String>> a: map.entrySet())
				{
			%>
				<li data-jstree='{"icon":"../images/time.png"}' style="font-weight:bold;">
					<%=a.getKey() %>
				</li>
			<% 
				}
			%>
			</ul>
		</div>
	</td>
	<td width="80%" style="vertical-align: text-top;">
		<div id="screens"></div>
		<!-- 
		<div class="slider-wrapper theme-bar">
    	<div class="ribbon"></div>
		<div id="slider" class="nivoSlider">
		</div></div>
		 -->
	</td>
</tr>
</table>


<link rel="stylesheet" href="../lib/themes/bar/bar.css" type="text/css" />
<script>
$('#jstree_div').jstree();
$('.jstree-node').css("font-weight","bold");
$('#jstree_div').on('changed.jstree', function (e, data) {
	//$('#detail').show();
	
	var selectedNode = data.instance.get_node(data.selected[0]);
	var timestamp = selectedNode.text;
	
	$.ajax({
		type: 'POST',
		url: '${pageContext.request.contextPath}/GetScreenshotsForDetailServlet?timestamp='+timestamp, 
		success: function(responseText)
		{
			console.log(responseText);
			$('#screens').html('');
			$('#screens').html(responseText);
			
			$('#slider').nivoSlider({
				directionNav: true,
				prevText: 'Prev',                 // Prev directionNav text
			    nextText: 'Next',                 // Next directionNav text
			    manualAdvance: true
			});
		}
	})
	
 });


</script>
 <%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="activity.web.manager.*" %>
<%@ page import="cn.zju.edu.blf.dao.*" %>
<%@ page import="cn.zju.edu.util.*" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.Map.Entry" %>

<script>
		var actionMap = {};
</script>

<%
	String title = request.getParameter("title");
	byte[] bytes = title.getBytes(StandardCharsets.ISO_8859_1);
	title = new String(bytes, StandardCharsets.UTF_8);
	
	title = title.replace("\\", "\\\\");
	
	String info = request.getParameter("info");
	boolean isDay = Boolean.parseBoolean(request.getParameter("isday"));
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	logger.info(title + "/" + info + "/");
	
	String app = info.split("/")[0];
	String time = info.split("/")[1];
	
	DataManager dm = (DataManager)session.getAttribute("dataManager");
	List<GroupedInteraction> groups = (List<GroupedInteraction>)session.getAttribute("aggrInteractions");
	if(dm == null || groups == null) 
	{
		response.getWriter().println("Session time out");
		return;
	}
	
	Map<String, List<ActionDetail>> actions = dm.getLLInteractionsForDetail(title, app, time, isDay, groups);
	session.setAttribute("actiondetail", actions);
%>
	<table style="width:100%"><tr> <td width="20%" style="vertical-align: top;">
	<div id="jstree_div">
	<ul>
<% 
	for(Entry<String, List<ActionDetail>> a: actions.entrySet())
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
	<div id="detail" style="display:none">
		<table id="codechange" class="display" cellspacing="0" width="100%">
		<thead>
		<tr>
			<th width="10%">Time</th>
			<th width="70%">UI Name</th>
			<th width="10%">UI Type</th>
			<th width="10%">Parent UI Name</th>
		</tr>
		</thead>
		</table>
	</div>
	</td>
 	</tr></table>

<link rel="stylesheet" href="../lib/jquery.dataTables.min.css" />
<link rel="stylesheet" href="../lib/codemirror.css" /> 	
<link rel="stylesheet" href="../lib/addon/show-hint.css">

<script>
$('#jstree_div').jstree();

$('.jstree-node').css("font-weight","bold");

 var isInit = false;
 
 $('#jstree_div').on('changed.jstree', function (e, data) {
	$('#detail').show();
	
	var selectedNode = data.instance.get_node(data.selected[0]);
	var timestamp = selectedNode.text;
	
	if(isInit)
	{
		$('#codechange').DataTable().clear();
	}
	
	showTable(timestamp);
 });

 function showTable(timestamp)
 {
	 $('#codechange').DataTable({
		 	"bDestroy": true,
			"lengthMenu": [[10, 20, 30, -1], [10, 20, 30, "All"]],
			"ajax": "${pageContext.request.contextPath}/GetAccessServlet?timestamp="+timestamp,
			"columns" : [
			 	{"data" : "time", "width": "10%"},
			 	{"data" : "action", "width": "60%"},
			 	{"data" : "controlType", "width": "15%"},
			 	{"data" : "parent", "width": "15%"}
			 ],
			"aoColumnDefs" : [
			   {
				"aTargets" : [1],
				"mRender" : function(d, t, v)
				{
					//console.log(v);
					if(v['imgUrl'] != null && v['imgUrl'] != "")
						return d + v['imgUrl'];
					
					if(v['controlType'] == "edit" && v['parent'] == "Console")
					{
						return "<textarea>" + d + "</textarea>";
					}
					
					return d;
				}
			   },
			   {
				   "aTargets" : [0],
					"mRender" : function(d, t, v)
					{
						return d.split(" ")[1];
					}  
			   }
			   ],
			"rowCallback" : function(r, d){
				var t = $(r).find("textarea")[0];
				if(t != null && t != undefined && $(r).find(".CodeMirror").length <= 0)
				{
					var changeEditor = CodeMirror.fromTextArea($(r).find("textarea")[0], {
					     lineNumbers: true,
					     matchBrackets: true,
					     readOnly: true,
					     viewportMargin: Infinity,
					     mode: "highlightSearch"
					   });
					
					setTimeout(function() {
						changeEditor.setSize(1000, null);
						changeEditor.refresh();
					},1);
				}
			}
		});
	 
	 isInit = true;
 }
 
 
</script>


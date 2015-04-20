 <%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="activity.web.manager.*" %>
<%@ page import="cn.zju.edu.blf.dao.*" %>
<%@ page import="cn.zju.edu.util.*" %>
<%@ page import="java.nio.charset.StandardCharsets" %>

<%
	String title = request.getParameter("title");
	byte[] bytes = title.getBytes(StandardCharsets.ISO_8859_1);
	title = new String(bytes, StandardCharsets.UTF_8);
	
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
	
	//List<ActionDetail> actions = dm.getLLInteractionsForDetail(title, app, time, isDay, groups);
%>
<table id="codechange" class="display" cellspacing="0" width="100%">
<thead>
<tr>
	<th width="10%">Time</th>
	<th width="70%">Action</th>
	<th width="10%">Control Type</th>
	<th width="10%">Parent Control</th>
</tr>
</thead>
</table>


<link rel="stylesheet" href="../lib/jquery.dataTables.min.css" />
<link rel="stylesheet" href="../lib/codemirror.css" /> 	
<link rel="stylesheet" href="../lib/addon/show-hint.css">
<script>
var title = '<%=title%>';
var info = '<%=info%>';
var isday = <%=isDay%>;

CodeMirror.defineMode("highlightSearch", function(config, parserConfig) {
    var searchOverlay = {
      token: function(stream, state) {
          if (stream.match("Exception")) {
              return "highlightSearch";
          }

          while (stream.next() != null && !stream.match("Exception", false)) {}
          return null;
      }
    };
    return CodeMirror.overlayMode(CodeMirror.getMode(config, parserConfig.backdrop), searchOverlay);
  });

$('#codechange').DataTable({
	"lengthMenu": [[10, 20, 30, -1], [10, 20, 30, "All"]],
	"ajax": "${pageContext.request.contextPath}/GetAccessServlet?title="+title+"&info="+info+"&isday="+isday,
	"columns" : [
	 	{"data" : "time", "width": "10%"},
	 	{"data" : "action", "width": "70%"},
	 	{"data" : "controlType", "width": "10%"},
	 	{"data" : "parent", "width": "10%"}
	 ],
	"aoColumnDefs" : [{
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
	}],
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
</script>

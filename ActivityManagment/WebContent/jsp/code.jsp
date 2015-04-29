<%@ page import="java.util.*" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="activity.web.manager.*" %>
<%@ page import="cn.zju.edu.blf.dao.*" %>
<%@ page import="cn.zju.edu.util.*" %>

<%
	String title = request.getParameter("title");
	String info = request.getParameter("info");
	boolean isDay = Boolean.parseBoolean(request.getParameter("isday"));
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	logger.info(title + "/" + info + "/" + isDay);
	
	String app = info.split("/")[0];
	String time = info.split("/")[1];
	
	DataManager dm = (DataManager)session.getAttribute("dataManager");
	List<GroupedInteraction> groups = (List<GroupedInteraction>)session.getAttribute("allinteractions");
	if(dm == null || groups == null) 
	{
		response.getWriter().println("Session time out");
		return;
	}
	
	List<CodeChange> changes = dm.getCodeChanges(title, app, time, isDay, groups);
	session.setAttribute("codechanges", changes);
%>
	<table style="width:100%"><tr> <td width="20%" style="vertical-align: top;">
	<div id="jstree_div">
	<ul>
<% 
	for(int i=0; i<changes.size(); i++)
	{
%>
	<li data-jstree='{"icon":"../images/time.png"}'>
		<%=changes.get(i).getTime() %>
		<ul>
			<li data-jstree='{"icon":"../images/edit.png"}'>Source Code</li>
		<%if(changes.get(i).getDetail().size()>0){ %>
			<li data-jstree='{"icon":"../images/pen.png"}'>Code Change</li>
		<%} %>
		</ul>
	</li>
<% 	
	}
%>
	 </ul>
	 </div>
	</td>
	<td width="80%">
	<div>
		<table id="codechange" class="display" cellspacing="0" width="100%">
		</table>
	
		<textarea id="codesource"></textarea>
	</div>
	</td>
 	</tr></table>
 	
<link rel="stylesheet" href="../lib/jquery.dataTables.min.css" /> 	
<link rel="stylesheet" href="../lib/codemirror.css" /> 	
<link rel="stylesheet" href="../lib/addon/show-hint.css">
 <script>
 var editor = CodeMirror.fromTextArea(document.getElementById("codesource"), {
     lineNumbers: true,
     matchBrackets: true,
     readOnly: true,
     mode: "text/x-java"
   });
 //editor.setSize(null, "50%");
 //$("#codesource").css("display", "none");
 
 $('#jstree_div').jstree();
 
 $('.jstree-node').css("font-weight","bold");
 
 $('#jstree_div').on('changed.jstree', function (e, data) {
   //if(data.selected.length <= 0) return;
	
   var selectedNode = data.instance.get_node(data.selected[0]);
   var parentId = data.instance.get_parent(selectedNode);
   var parentNode = data.instance.get_node(parentId);
   
   //alert(parentId + "/" + parentNode.text);
   
   if(parentId == "#") return;
   
   var nodeText = selectedNode.text;
   var index = 0;
   
   $("#codechange").dataTable({
	   "lengthMenu": [[5, 10, 15, -1], [5, 10, 15, "All"]],
	   "ajax": "${pageContext.request.contextPath}/GetCodeChangeServlet?type=change&time="+parentNode.text,
		"columns" : [
		 	{"data" : "type"},
		 	{"data" : "content"}
		 ],
		 "bDestroy": true,
		"aoColumnDefs" : [
			{
				"aTargets" : [0],
				"mRender" : function(d, t, v)
				{
					if(d.indexOf("INSERT") >= 0)
					{
						return "<span style='color:green;'>" + d + "</span>";
					}
					else if(d.indexOf("DELETE") >= 0)
					{
						return "<span style='color:red;'>" + d + "</span>";
					}
					else if(d.indexOf("CHANGE") >= 0)
					{
						return "<span style='color:blue;'>" + d + "</span>";
					}

					return d;
				}
			},
			{
				"aTargets" : [1],
				"mRender" : function(d, t, v)
				{
					var res = "<textarea>" + d + "</textarea>";
	
					return res;
				}
			}	
		],
		"rowCallback" : function(r, d)
		{
			if($(r).find(".CodeMirror").length <= 0)
			{
				var changeEditor = CodeMirror.fromTextArea($(r).find("textarea")[0], {
				     lineNumbers: false,
				     matchBrackets: true,
				     viewportMargin: Infinity,
				     readOnly: true,
				     mode: "text/x-java"
				   });
				
				//alert(changeEditor.defaultTextHeight());
				
				if(changeEditor.lineCount() <= 5)
				{
					changeEditor.setSize(null, "100%");
				}
				
				setTimeout(function() {
					changeEditor.refresh();
				},1);
				
				//changeEditor.refresh();
			}
		}
   });
   
   var type="";
   if(nodeText == "Source Code")
	{
	   $.ajax({
			  type: 'POST',
			  url: '${pageContext.request.contextPath}/GetCodeChangeServlet',
			  data: {
				  type: "source",
				  time: parentNode.text
			  },
			  success: function(resText)
			  {
				$('#codechange').DataTable().clear();
				$('#codechange_wrapper').hide();
				//$("#codesource").val(resText);
				editor.setValue(resText);
				setTimeout(function() {
					editor.refresh();
				},1);
				
				$(editor.getWrapperElement()).show();
				//$("#codesource").css("display", "block");
				//$("#codesource").removeClass("nodisplay");
			  }
		   });
	}
   else if(nodeText == "Code Change")
	{
	   $("#codechange").show();
	   $(editor.getWrapperElement()).hide();
	}

 });
 </script>
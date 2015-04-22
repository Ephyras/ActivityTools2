<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href='../fullcalendar.css' rel='stylesheet' />
<link href='../lib/jquery-ui.min.css' rel='stylesheet' />
<link href='../lib/theme.css' rel='stylesheet' />
<link href='../lib/jquery.contextMenu.css' rel='stylesheet' />
<link href='../fullcalendar.print.css' rel='stylesheet' media='print' />
<link href='../css/mycss.css' rel='stylesheet' />
<link rel="stylesheet" href="../lib/nivo-slider.css" type="text/css" />
<link rel="stylesheet" href="../lib/themes/default/style.min.css" />

<script src='../lib/moment.min.js'></script>
<script src='../lib/jquery.min.js'></script>
<script src='../lib/jquery-ui.min.js'></script>
<script src='../lib/jquery.contextMenu.js'></script>
<script src='../lib/jquery.ui.position.js'></script>
<script src='../fullcalendar.js'></script>
<script src="../lib/jquery.nivo.slider.js" type="text/javascript"></script>
<script src="../lib/jstree.min.js"></script>
<script src="../lib/jquery.dataTables.min.js"></script>
<script src="../lib/codemirror.js"></script>
<script src="../lib/addon/matchbrackets.js"></script>
<script src="../lib/addon/overlay.js"></script>
<script src="../lib/clike.js"></script>
<script src="../lib/d3.min.js"></script>
<script src="../lib/d3.layout.cloud.js"></script>
<script src='../lib/util.js'></script>
<script src='../lib/carrotsearch.foamtree.js'></script>
<title>Activity Management</title>
<%
	String user = request.getParameter("user");
	String strInterval = request.getParameter("interval");
	String filter = request.getParameter("filter");
	if(filter == null) filter = "";
	String app = request.getParameter("app");
	session.setAttribute("user", user);
%>
<script>
	
	var user = "<%=user%>";
	var interval = "<%=strInterval%>";
	var filter = "<%=filter%>";
	var app = "<%=app%>";
	var now = getToday();
	var loadDialog;
	
	$(document).ready(function() {
		loadDialog = $("#loading").dialog({
		    hide: 'slide',
			show: 'slide',
			autoOpen: false
		});
		
		addContextMenu4Code();
		addContextMenu4Normal();
		
		var preSelectDay = getEventDate(new Date()); 
		$('.fc-day[data-date="' + preSelectDay + '"]').addClass("orange");
		
		$('#datepicker').datepicker({
	        inline: true,
	        onSelect: function(dateText, inst) {
	            var d = new Date(dateText);
	            $('#calendar').fullCalendar('gotoDate', d);
	            
	            var curSelectDay = getEventDate(d);
	            $('.fc-day[data-date="' + preSelectDay + '"]').removeClass("orange");
	            $('.fc-day[data-date="' + curSelectDay + '"]').addClass("orange");
	            
	            preSelectDay = curSelectDay;
	        }
	    }); 
		
		$('#calendar').fullCalendar({
			header: {
				left: 'prev,next today',
				center: 'title',
				right: 'month,basicWeek,basicDay'
			},
			defaultDate: now,
			timeFormat: 'H:mm',
			editable: true,
			eventLimit: true, // allow "more" link when too many events
			events: [],
			eventClick: function(calEvent, jsEvent, view) {
				$("#selectedEvent").val(calEvent.title);
				$("#selectedApp").val(calEvent.application);
				$("#selectedEvent").css("background-color", calEvent.color);
			},
			eventAfterRender: function(event, element, view) {
				if(hasEditHistory(event))
				{
					$(element).addClass('codecss');
				}
				else
				{
					$(element).addClass('normalcss');
				}
				
				$(element).append("<div class='eventhidden'>"+event.application + "/" + moment(event.start).format('YYYY-MM-DD') + "</div>")
				//$(element).append("<div style='display:none'>test</div>")
                //$(element).addClass('myeventcss');
            },
            dayClick: function(date, jsEvent, view) {
            	var curSelectDay = date.format();
            	
           		$('#datepicker').datepicker("setDate", date.toDate());
           		$('#calendar').fullCalendar('gotoDate', date);
           		
           		$('.fc-day[data-date="' + preSelectDay + '"]').removeClass("orange");
	            $('.fc-day[data-date="' + curSelectDay + '"]').addClass("orange");
	            
	            preSelectDay = curSelectDay;
           		
            }
			/*evnetRender: function(event, element){
				element.contextMenu({
					menu: 'myMenu'
				}, function(action, el, pos) {
					alert(
						'Action: ' + action + '\n\n' +
						'Element text: ' + $(el).text() + '\n\n' +
						'X: ' + pos.x + '  Y: ' + pos.y + ' (relative to element)\n\n' +
						'X: ' + pos.docX + '  Y: ' + pos.docY+ ' (relative to document)'
						);
				});
			}*/
		});
		
		$('.fc-prev-button').click(function(){
			var moment = $('#calendar').fullCalendar('getDate');
			$('#datePicker').datepicker('setDate', moment);
		});
	});

</script>
</head>
<body>
<div id="container">
	<div id="left">
		<table width="100%">
			<tr class="leftTableTr"><td>
				<fieldset class="leftgroup">
					<legend>Calendar</legend>
					<div id="datepicker"></div>
				</fieldset>
			</td></tr>
			<tr class="leftTableTr">
				<td>
				<fieldset class="leftgroup">
				<legend>Filter Applications</legend>
				<div id="applications">
					<div class="eclipseDiv">
						<input type="checkbox" checked="checked" name="eclipseCheck" id="eclipseCheck" /> <label for="eclipseCheck" class="labelCss">Eclipse</label>
					</div>
					<div class="browserDiv">
						<input type="checkbox" checked="checked" name="browserCheck" id="browserCheck" /> <label for="browserCheck" class="labelCss">Browser</label>
					</div>
					<div class="docDiv">
						<input type="checkbox" checked="checked" name="officeCheck" id="officeCheck" /> <label for="officeCheck" class="labelCss">Office Document</label>
					</div>
					<div class="vsDiv">
						<input type="checkbox" checked="checked" name="vsCheck" id="vsCheck" /> <label for="vsCheck" class="labelCss">Visual Studio</label>
					</div>
					<div class="otherDiv">
						<input type="checkbox" checked="checked" name="otherCheck" id="otherCheck" /> <label for="otherCheck" class="labelCss">Other Applications</label>
					</div>
				</div> 
				</fieldset>
			</td>
			</tr>
			<tr class="leftTableTr">
				<td align="left">
					<fieldset class="leftgroup">
					<legend>Filter Query</legend>
					<table width="100%">
						<tr>
							<td><input type="text" name="filterContent" id="filterContent" class="filterInput" value=""/>
								<input type="hidden" name="hiddenFilter" id="hiddenFilter" value=""/>
							</td>
						</tr>
						<tr>
							<td align="right">
								<input type="button" name="filterBtn" id="filterBtn" value="Filter" class="mybtn"/>
								<input type="button" name="clearFilterBtn" id="clearFilterBtn" value="Clear" class="mybtn"/>
							</td>
							
						</tr>
					</table>
					</fieldset>
				</td></tr>
			<tr class="leftTableTr">
			<td align="left">
					<fieldset class="leftgroup">
					<legend>Coordinated Activities</legend>
					<table width="100%">
						<tr>
							<td>
								<input type="text" name="selectedEvent" id="selectedEvent" class="selectedInput" value="" readonly/>
								<input type="hidden" name="selectedApp" id="selectedApp" value=""/>
							</td>
						</tr>
						<tr>
							<td align="right">
								<input type="button" name="coordinatedBtn" id="coordinatedBtn" value="Find Coordinated Activities" class="mybtn"/>
								<input type="button" name="reset" id="reset" value="Reset" class="mybtn" width="100px"/>
							</td>
						</tr>
					</table>
					</fieldset>
			</td></tr>
			<tr class="leftTableTr">
			<td align="right">
				<fieldset class="leftgroup">
				<legend>Advance Information</legend>
				<table width="100%">
					<tr><td>
						<span>Recent </span><input type="text" name="topicday" id="topicday" value="10" style="width:20%;text-align:right;"/> <span>days</span>
					</td></tr>
					<tr><td>
						<input type="button" name="topicBtn" id="topicBtn" value="Webpage Summary using Titles" class="mybtn"/>
						<input type="button" name="KeyBtn" id="KeyBtn" value="Query Summary" class="mybtn"/>
					</td></tr>
				</table>
				
					
				</fieldset>
			</td>
			</tr>
		</table>
	</div>
	
	<div id='calendar'></div>
	
</div>

<div id="loading" title="Loading..."> 
    	<p>Please wait ...</p>
	</div>
	
<div id="dialog"></div>

<script>
$(document).ready(function() 
{
	var source;
	var dataUrl = '${pageContext.request.contextPath}/GetInteractionsServlet?user='+user;
	if($.isNumeric(interval))
	{
		dataUrl += "&interval="+interval;
	}
	
	if(filter != null)
	{
		//dataUrl += "&filter="+filter;
		$("#filterContent").val(filter);
		$("#hiddenFilter").val(filter);
		$("#selectedEvent").val(filter);
		$("#selectedApp").val(app);
	}
	
	$.ajax({
		type: 'POST',
		url: dataUrl, 
		beforeSend: function(){
			loadDialog.dialog('open').html("<p>Please Wait...</p>");
		},
		success: function(responseText)
		{
			loadDialog.dialog('close');
			
			source = responseText;
		    $('#calendar').fullCalendar('addEventSource', source);
		    $('#calendar').fullCalendar('refetchEvents');
		}
	});
	
	clusterTopic();
	keywords();
	 
	$("#eclipseCheck, #browserCheck, #officeCheck, #vsCheck, #otherCheck").change(function() {
		$('#calendar').fullCalendar('removeEventSource', source);
		$('#calendar').fullCalendar('refetchEvents');
		
		$.ajax({
			type: 'POST',
			url: '${pageContext.request.contextPath}/GetInteractionsServlet?user='+user,
			data: {
				'eclipse': $("#eclipseCheck").is(':checked'),
				'browser': $("#browserCheck").is(':checked'),
				'office': $("#officeCheck").is(':checked'),
				'vs': $("#vsCheck").is(':checked'),
				'other': $("#otherCheck").is(':checked'),
			},
			beforeSend: function(){
				loadDialog.dialog('open').html("<p>Please Wait...</p>");
			},
			success: function(responseText){
				loadDialog.dialog('close');
				source = responseText;
			    $('#calendar').fullCalendar('addEventSource', source)
			    $('#calendar').fullCalendar('refetchEvents');
			}
		});
	});
	
	$("#filterBtn").click(function(){
		var filterQuery = $("#filterContent").val();
		if(filterQuery == null || filterQuery == "") return;
		
		$.ajax({
			type: 'POST',
			url: '${pageContext.request.contextPath}/GetInteractionsServlet?user='+user,
			data: {
				'filter': filterQuery,
				'eclipse': $("#eclipseCheck").is(':checked'),
				'browser': $("#browserCheck").is(':checked'),
				'office': $("#officeCheck").is(':checked'),
				'vs': $("#vsCheck").is(':checked'),
				'other': $("#otherCheck").is(':checked'),
			},
			beforeSend: function(){
				loadDialog.dialog('open').html("<p>Please Wait...</p>");
			},
			success: function(responseText){
				loadDialog.dialog('close');
				
				$("#hiddenFilter").val(filterQuery);
				
				$('#calendar').fullCalendar('removeEventSource', source);
				$('#calendar').fullCalendar('refetchEvents');
				
				source = responseText;
			    $('#calendar').fullCalendar('addEventSource', source)
			    $('#calendar').fullCalendar('refetchEvents');
			}
		});
	});
	
	$("#clearFilterBtn").click(function(){
		var filterQuery = $("#hiddenFilter").val();
		if(filterQuery == null || filterQuery == "") return;
		
		$("#filterContent").val("");
		$.ajax({
			type: 'POST',
			url: '${pageContext.request.contextPath}/GetInteractionsServlet?user='+user,
			data: {
				'filter': "",
				'eclipse': $("#eclipseCheck").is(':checked'),
				'browser': $("#browserCheck").is(':checked'),
				'office': $("#officeCheck").is(':checked'),
				'vs': $("#vsCheck").is(':checked'),
				'other': $("#otherCheck").is(':checked'),
			},
			beforeSend: function(){
				loadDialog.dialog('open').html("<p>Please Wait...</p>");
			},
			success: function(responseText){
				loadDialog.dialog('close');
				
				$("#hiddenFilter").val("")
				
				$('#calendar').fullCalendar('removeEventSource', source);
				$('#calendar').fullCalendar('refetchEvents');
				
				source = responseText;
			    $('#calendar').fullCalendar('addEventSource', source)
			    $('#calendar').fullCalendar('refetchEvents');
			}
		});
	});
	
	$("#coordinatedBtn").click(function(){
		var selectedEvent = $("#selectedEvent").val();
		var selectedApp = $("#selectedApp").val();
		
		if(selectedEvent == "" || selectedApp == "")
		{
			alert("Please choose a activity event.");
			return;
		}
		
		$.ajax({
			type: 'POST',
			url: '${pageContext.request.contextPath}/FindCoordinatedActivityServlet',
			data: {
				'title': selectedEvent,
				'application': selectedApp
			},
			beforeSend: function(){
				loadDialog.dialog('open').html("<p>Please Wait...</p>");
			},
			success: function(responseText){
				loadDialog.dialog('close');
				
				$('#calendar').fullCalendar('removeEventSource', source);
				$('#calendar').fullCalendar('refetchEvents');
				
				source = responseText;
			    $('#calendar').fullCalendar('addEventSource', source)
			    $('#calendar').fullCalendar('refetchEvents');
			}
		});
	});
	
	$("#reset").click(function(){
		$("#selectedEvent").val("");
		$("#selectedApp").val("");
		
		$.ajax({
			type: 'POST',
			url: '${pageContext.request.contextPath}/GetInteractionsServlet?user='+user,
			data: {
				'reset' : 'true',
				'eclipse': $("#eclipseCheck").is(':checked'),
				'browser': $("#browserCheck").is(':checked'),
				'office': $("#officeCheck").is(':checked'),
				'vs': $("#vsCheck").is(':checked'),
				'other': $("#otherCheck").is(':checked'),
			},
			success: function(responseText){
				$('#calendar').fullCalendar('removeEventSource', source);
				$('#calendar').fullCalendar('refetchEvents');
				
				source = responseText;
			    $('#calendar').fullCalendar('addEventSource', source)
			    $('#calendar').fullCalendar('refetchEvents');
			}
		});
	});
});
</script>

</body>
</html>
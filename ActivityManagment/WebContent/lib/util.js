function getToday()
{
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth()+1; //January is 0!
	var yyyy = today.getFullYear();

	if(dd<10) {
	    dd='0'+dd
	} 

	if(mm<10) {
	    mm='0'+mm
	} 

	today = mm+'/'+dd+'/'+yyyy;
	
	return today;
}

function getEventDate(d)
{
	var dd = d.getDate();
	var mm = d.getMonth()+1;
	var yyyy = d.getFullYear();
	
	if(dd<10) {
	    dd='0'+dd
	} 

	if(mm<10) {
	    mm='0'+mm
	} 

	return yyyy+'-'+mm+'-'+dd;
}

function monentStringToDate(moment)
{
	var s = moment.split("-");
	console.log(s[0]+s[1] + s[2]);
	
	return new Date(s[0], s[1]-1, s[2]);
}

function hasEditHistory(event)
{
	app = event.application;
	if(app == "eclipse.exe" || app == "javaw.exe" || app == "devenv.exe")
	{
		return true;
	}
	
	return false;
}

function addContextMenu4Code()
{
	$.contextMenu({
		selector: '.codecss',
		callback: function(key, options) {
			var title = options.$trigger.find(".fc-title").html();
			var info = options.$trigger.find(".eventhidden").html();
			
			if(key == "screenday")
			{
				showScreenDialog(title, info, true);
			}
			else if(key == "screenall")
			{
				showScreenDialog(title, info, false);
			}
			else if(key == "accday")
			{
				showAccessDialog(title, info, true);
			}
			else if(key == "accall")
			{
				showAccessDialog(title, info, false);
			}
			else if(key == "codeday")
			{
				showCodeHistoryDialog(title, info, true);
			}
			else if(key == "codeall")
			{
				showCodeHistoryDialog(title, info, false);
			}
        },
        items: {
            "showscreen": {name: "Show Activity Screenshots", icon: "screenshot", items: {
			    			"screenday": {name: "Current Day"},
			    			"screenall": {name: "Entire History"}
			    		}
            },
            "showacc": {name: "Show Activity Accessibility Information", icon: "access",items: {
			    			"accday": {name: "Current Day"},
			    			"accall": {name: "Entire History"}
			            }
    		},
            "showcode": {name: "Show Edit History",icon: "code", items: {
	            			"codeday": {name: "Current Day"},
	            			"codeall": {name: "Entire History"}
            			}
            }
        }
	});
}

function addContextMenu4Normal()
{
	$.contextMenu({
		selector: '.normalcss',
		callback: function(key, options) {
			var title = options.$trigger.find(".fc-title").html();
			var info = options.$trigger.find(".eventhidden").html();
			
			if(key == "screenday")
			{
				showScreenDialog(title, info, true);
			}
			else if(key == "screenall")
			{
				showScreenDialog(title, info, false);
			}
			else if(key == "accday")
			{
				showAccessDialog(title, info, true);
			}
			else if(key == "accall")
			{
				showAccessDialog(title, info, false);
			}
        },
        items: {
        	"showscreen": {name: "Show Activity Screenshots", icon: "screenshot", items: {
    			"screenday": {name: "Current Day"},
    			"screenall": {name: "Entire History"}
    		}
			},
			"showacc": {name: "Show Activity Accessibility Information", icon: "access",items: {
			    			"accday": {name: "Current Day"},
			    			"accall": {name: "Entire History"}
			            }
			}
        }
	});
}

function showScreenDialog(title, info, isday)
{
	$.ajax({
		//url:"${pageContext.request.contextPath}/GetScreenshotsServlet",
		url:"screenshot.jsp",
		data: {
			title : title,
			info : info,
			isday : isday
		},
		beforeSend: function(){
			loadDialog.dialog('open').html("<p>Please Wait...</p>");
		},
		success: function(resText)
		{
			loadDialog.dialog('close');
			$("#dialog").html(resText).dialog({
				modal: true,
				show: 'slide',
				hide: 'slide', 
				width : '1000px',
				height : 'auto', 
				title: title,
				resizable:true, 
				closeOnEscape:true,
				focus:true,
				close : function(event, ui) {
					$(this).html("")  
					$(this).hide();
				}
			})
		}
	});
}

function showCodeHistoryDialog(title, info, isday)
{
	$.ajax({
		url: "code.jsp",
		data: {
			title : title,
			info : info,
			isday: isday
		},
		beforeSend: function(){
			loadDialog.dialog('open').html("<p>Please Wait...</p>");
		},
		success: function(resText)
		{
			loadDialog.dialog('close');
			$("#dialog").html(resText).dialog({
				modal: true,
				show: 'slide',
				hide: 'slide', 
				width : '90%',
				height : 'auto', 
				title: title,
				resizable:true, 
				closeOnEscape:true,
				focus:true,
				close : function(event, ui) {
					$(this).html("")  
					$(this).hide();
				}
			})
		}
	});
}

function showAccessDialog(title, info, isday)
{
	$.ajax({
		url: "access.jsp",
		data: {
			title : title,
			info : info,
			isday : isday
		},
		beforeSend: function(){
			loadDialog.dialog('open').html("<p>Please Wait...</p>");
		},
		success: function(resText)
		{
			loadDialog.dialog('close');
			$("#dialog").html(resText).dialog({
				modal: true,
				show: 'slide',
				hide: 'slide', 
				width : '90%',
				height : 'auto', 
				title: title,
				resizable:true, 
				closeOnEscape:true,
				focus:true,
				close : function(event, ui) {
					$(this).html("")  
					$(this).hide();
				}
			})
		}
	});
}

function clusterTopic()
{
	$("#topicBtn").click(function(){
		//var day = $("#topicday").val();
		$.ajax({
			url: "topic.jsp",
			data:{
				day: 10,
				'eclipse': $("#eclipseCheck").is(':checked'),
				'browser': $("#browserCheck").is(':checked'),
				'office': $("#officeCheck").is(':checked'),
				'vs': $("#vsCheck").is(':checked'),
				'other': $("#otherCheck").is(':checked'),
			},
			success:function(resText)
			{
				$("#dialog").html(resText).dialog({
					modal: true,
					show: 'slide',
					hide: 'slide', 
					width : '50%',
					height : 'auto', 
					title: "Web Page Topics",
					resizable:true, 
					closeOnEscape:true,
					position: ['center', 0],
					focus:true,
					close : function(event, ui) {
						$(this).html("")  
						$(this).hide();
					}
				})
			}
		});
	});
}

function keywords()
{
	$("#KeyBtn").click(function(){
		var day = $("#topicday").val();
		$.ajax({
			url: "keyword.jsp",
			data:{
				day: day
			},
			success:function(resText)
			{
				$("#dialog").html(resText).dialog({
					modal: true,
					show: 'slide',
					hide: 'slide', 
					width : '60%',
					height : 'auto', 
					title: "Query Summary",
					resizable:true, 
					closeOnEscape:true,
					position: ['center', 0],
					focus:true,
					close : function(event, ui) {
						$(this).html("")  
						$(this).hide();
					}
				})
			}
		});
	});
}
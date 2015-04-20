 <%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<div style="float:top; text-align:right;color:blue;font-size:12px;"> 
	<input type="radio" value="1" name="ttype" /> Table
	<input type="radio" value="2" name="ttype" checked/> Foam Tree
</div>

<hr style="color:blue;height:2px;"/>

<div style="text-align:center;">
<div id="topictree" style="display:none; font-size:12px;"></div>
<div id="wordcloud" style="margin:auto; width: 800px; height: 600px; overflow:auto;"></div>
</div>

<script>
var day = '<%=request.getParameter("day")%>';
var fill = d3.scale.category20();

var topics;
var words;

var w = 960;
var h = 600;

$.ajax({
	type: 'POST',
	url: '${pageContext.request.contextPath}/GetWebpageTopicServlet?day='+day, 
	success: function(responseText)
	{
		topics = responseText['topic'];
		words = responseText['words'];
		
		$("#topictree").jstree({
			'core':{
				'data' : topics,
				'themes':{
					"icons":false
				}
			}
		});
		
		//words.sort(compare_topic_desc);
		//var top = words.length/2>100 ? 100 : words.length/2;
		
		var foamtree = new CarrotSearchFoamTree({
	          id: "wordcloud",
	          dataObject: {
	        	groups: words
	        		/*
	        		.slice(0, top).filter(function(d){
	        		console.log(d.key+':' +d.value);
	        		if(d['value'] <= 2)
	        		{
	        			console.log(d['key'] + ':' + d['value']);
	        			return false;
	        		}
	        		return true;
	        		})
	        		*/
	        	.map(function(d){
	            	return {label:d['key'], weight: d['value']}
	            })
	          }
	        });
		/*
		d3.layout.cloud()
			   .size([w, h])
		  	  .words(words.map(function(d) {
		        	return {text: d['key'], size: d['value']};
		      	}))
		      //.rotate(function() { return ~~(Math.random() * 2) * 60; })
		      .font("Impact")
		      .fontSize(function(d) {
		    	  if(d.size>20)
		    	  {
		    		  return d.size
		    	  }
		    	  else if((d.size + 5)*1.5>20)
		    	  {
		    		  return 20;
		    	  }
		    	  else
		    	  {
		    		  return (d.size + 5)*1.5;
		    	  }
		    	})
		      .on("end", drawWords)
		      .start();
		*/
	}
});

function compare_topic_desc(a,b) {
	  if (a.value < b.value)
	     return 1;
	  if (a.value > b.value)
	    return -1;
	  return 0;
	}

function drawWords(words) {
    d3.select("#wordcloud").append("svg")
        .attr("width", w)
        .attr("height", h)
      .append("g")
        .attr("transform", "translate(" + [w >> 1, h >> 1] + ")")
      .selectAll("text")
        .data(words)
      .enter().append("text")
        .style("font-size", function(d) { var sz=d.size<5?5:d.size; return sz + "px"; })
        .style("font-family", "Impact")
        .style("fill", function(d, i) { return fill(i); })
        .attr("text-anchor", "middle")
        .attr("transform", function(d) {
          return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
        })
        .text(function(d) { return d.text; });
  }	

$("input:radio[name=ttype]").change(function(){
		var v = $(this).val();
		if(v == 1)
		{
			$("#wordcloud").hide();
			$("#topictree").show();
			//getTopics();
		}
		else if(v == 2)
		{
			$("#topictree").hide();
			$("#wordcloud").show();
		}
});



</script>
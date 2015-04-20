 <%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<style>
.node {
  stroke: #fff;
  stroke-width: 1.5px;
}

.link {
  stroke: #999;
  stroke-opacity: .6;
}
</style>

<div id="tabs">
<ul>
	<li><a href="#tabs-1">Keyword Occurrence in Queries</a></li>
	<li><a href="#tabs-2">Keyword Pair in Queries</a></li>
</ul>
	<div id="tabs-1">
		<div style="float:top; text-align:right;color:blue;font-size:12px;"> 
			Format: 
			<input type="radio" value="1" name="ttype"/> Tables
			<input type="radio" value="2" name="ttype" checked/> Foam Tree
		</div>
		
		<hr style="color:blue;height:2px;"/>
		
		<div style="text-align:center;">
		<table id="keytable" class="display" cellspacing="0" width="100%"></table>
		<div id="wordcloud" style="margin:auto; width:800px; height:600px; overflow:auto"></div>
		</div>
	</div>
	<div id="tabs-2">
		<div style="float:top; text-align:right;color:blue;font-size:12px;"> 
			Format: 
			<input type="radio" value="1" name="ttype2" checked/> Tables
			<input type="radio" value="2" name="ttype2"/> Graph
		</div>
		
		<hr style="color:blue;height:2px;"/>
		
		<table id="pairtable" class="display" cellspacing="0" width="100%"></table>
		<div id="pairgraph" style="display:none; text-align:center;width:100%"></div>
	</div>
</div>

<link rel="stylesheet" href="../lib/jquery.dataTables.min.css" />
<script>
$("#tabs").tabs();

var day = '<%=request.getParameter("day")%>';
var fill = d3.scale.category20();

var w = 1000;
var h = 600;

$.ajax({
	type: 'POST',
	url: '${pageContext.request.contextPath}/GetKeywordServlet?day='+day, 
	success: function(resText)
	{
		$('#keytable').DataTable({
			"lengthMenu": [[15, 30, 60, -1], [15, 30, 60, "All"]],
			"data": resText['keys'],
			"columns" : [
			 	{"data" : "key", "width": "50%"},
			 	{"data" : "number", "width": "50%"},
			 ],
			"order": [[ 1, "desc" ]]
			});
		
		$("#keytable_wrapper").hide();
		
		$('#pairtable').DataTable({
			"lengthMenu": [[15, 30, 60, -1], [15, 30, 60, "All"]],
			"data": resText['pairs'],
			"columns" : [
			 	{"data" : "k1", "width": "33%"},
			 	{"data" : "k2", "width": "33%"},
			 	{"data" : "number", "width": "33%"},
			 ],
			"order": [[ 2, "desc" ]]
			});
		
		var foamtree = new CarrotSearchFoamTree({
	          id: "wordcloud",
	          dataObject: {
	        	groups: resText['keys']
					.filter(function(d){
						if(d.number<2)
						{
							return false;
						}
						return true;
					})
	        		.map(function(d){
	            		return {label:d['key'], weight: d['number']}
	            	})
	          }
	        });
		
		/*
	 	d3.layout.cloud()
			   .size([w, h])
		  	  .words(resText['keys'].map(function(d) {
		        	return {text: d['key'], size: d['number']};
		      	}))
		      //.rotate(function() { return ~~(Math.random() * 2) * 60; })
		      .font("Impact")
		      .fontSize(function(d) {
		    	  if(d.size<10)
		    	  {
		    		  return d.size + 6;
		    	  }
		    	  else if(d.size>10 && d.size<20)
		    	  {
		    		  return d.size * 1.3
		    	  }
		    	  else
		    	  {
		    		  return d.size * 1.1;
		    	  }
		    	  
		    	})
		      .on("end", drawWords)
		      .start();
	 	*/
	 	
	 	var links = [];
	 	for(i=0; i<resText['pairs'].length;i++)
	 	{
	 		var k1 = resText['pairs'][i].k1;
	 		var k2 = resText['pairs'][i].k2;
	 		var number = resText['pairs'][i].number;
	 		
	 		var source = -1;
	 		var target = -1;
	 		for(j=0; j<resText['keys'].length; j++)
	 		{
	 			if(resText['keys'][j].key == k1)
	 			{
	 				source = j;
	 			}
	 			if(resText['keys'][j].key == k2)
	 			{
	 				target = j;
	 			}
	 			if(source != -1 && target != -1) break;
	 		}
	 		
	 		links.push({'source':source, 'target':target,'weight':number})
	 	}
	 	
	 	drawPairGraph(resText['keys'], links);
	}
});

$("input:radio[name=ttype]").change(function(){
	var v = $(this).val();
	if(v == 1)
	{
		$("#wordcloud").hide();
		$("#keytable_wrapper").show();
		//getTopics();
	}
	else if(v == 2)
	{
		$("#keytable_wrapper").hide();
		$("#wordcloud").show();
	}
});

$("input:radio[name=ttype2]").change(function(){
	var v = $(this).val();
	if(v == 1)
	{
		$("#pairgraph").hide();
		$("#pairtable_wrapper").show();
		//getTopics();
	}
	else if(v == 2)
	{
		$("#pairtable_wrapper").hide();
		$("#pairgraph").show();
	}
});

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

function drawPairGraph(keys, pairs)
{
	var svg = d3.select("#pairgraph").append("svg")
	    .attr("width", w)
	    .attr("height", h);
	
	var force = d3.layout.force()
			    .gravity(.05)
			    .distance(100)
			    .charge(-100)
			    .size([w, h]);
	
	force.nodes(keys)
	    .links(pairs)
	    .start();
	
	var link = svg.selectAll(".link")
    			.data(pairs)
  				.enter().append("line")
    			.attr("class", "link");
	
	var node = svg.selectAll(".node")
	    .data(keys)
	  .enter().append("g")
	    .attr("class", "node")
	    .call(force.drag);
	
	node.append("circle")
	    .attr("r", function(d){return d.number<7? 7 : d.number;})
	    .style("fill", function(d) { return fill(d.number); });
	
	node.append("title")
    	.text(function(d) { return d.key; });
	
	force.on("tick", function() {
	    link.attr("x1", function(d) { return d.source.x; })
	        .attr("y1", function(d) { return d.source.y; })
	        .attr("x2", function(d) { return d.target.x; })
	        .attr("y2", function(d) { return d.target.y; });
		
	    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	  });
}

</script>
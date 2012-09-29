<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<title>Welcome to the Digital Archaeological Record</title>
<meta name="lastModifiedDate" content="$Date: 2009-02-13 09:05:44 -0700 (Fri, 13 Feb 2009)$"/>
<meta name="google-site-verification" content="rd6Iv58lPY7jDqfNvOLe9-WXRpAEpwLZCCndxR64nSg" />
<script type='text/javascript' src='http://www.google.com/jsapi'></script>
<script type='text/javascript'>

   google.load('visualization', '1', {'packages': ['geomap']});
   google.setOnLoadCallback(drawVisualization);

function assignIsoData(data) {
  <#assign isoCount = 0>
  <@s.iterator value="iSOCountryCount" var="isoCountKey">
    <#--  remove replace once ISO3 digit replace with ISO2 -->
    data.setValue(${isoCount},0, "${isoCountKey.key?replace(" (ISO Country Code)","")}");
    data.setValue(${isoCount},1, ${isoCountKey.value?c});
    <#assign isoCount = isoCount +1> 
  </@s.iterator>

}

function drawVisualization() {
  var data = new google.visualization.DataTable();
  data.addColumn('string', 'ISO');
  data.addColumn('number', 'tDAR resources w/locations');
  data.addRows(${isoCount});
  assignIsoData(data);
    
  var geomap = new google.visualization.GeoMap(
      document.getElementById('map_canvas'));
      

  geomap.draw(data, {
    width:'545px',
    height: '345px', 
  });
    
  
   google.visualization.events.addListener(geomap, 'regionClick', function(e) {
            window.location.href= "/search/results?query=\""+ e.region + " (ISO Country Code)\"";
   });
}


      google.load("visualization", "1", {packages:["corechart"]});
      //google.setOnLoadCallback(drawChart);
</script>
<style type='text/css'>
.col66 {
    width:570px !important;
    height: 210px;
}

.col33 {
    width:325px;
    top:0px;
    height: 210px;
    position:absolute;
}

.col66+.col33 {
 left:607px;
}

</style>
</head>
<body>
<div class='post'>

<#if !sessionData?? || !sessionData.authenticated>
<div style="position:relative;display:block;clear:both;overflow:visible;height:250px">
</#if>
    <div class="glide       <#if !sessionData?? || !sessionData.authenticated>col66</#if>">
    <h3>About</h3>
    <#include "/includes/ftl/notice.ftl">
    </div>

<#if !sessionData?? || !sessionData.authenticated>
    <div class="col33 glide">
    <h3>Login</h3>
     <@nav.loginForm />
    </div>
</div>
</#if>
<div class="clear"></div>

<div class="glide">
<h3>Search</h3>
<@s.form action="search/results" method="GET" id='searchForm' >
<@search.queryField "Keyword"/>
<@s.submit value="Search" />
</@s.form>
</div>
<div class="glide" style="position:relative;">
<h3>Explore</h3>
<#assign graphWidth = 372 />
<#assign barWidth = (graphWidth  / (resourceTypeCounts.size()-2) - 10 )/>
<div id="contents_pie" style="height:345px;position:absolute;vertical-align:bottom;left:560px;width:${graphWidth?c}px;margin-left:10px;border:1px solid #CCC;background-color:#fff">
    <p style="margin-right: auto;margin-left: auto;text-align:center;margin-top:10px;margin-bottom:50px"><b>tDAR by the Numbers</b></p>
   <table style="margin-left: auto;vertical-align:bottom;margin-right: auto;margin-top:auto;margin-bottom:5px;position:relative;bottom:5px">
  <tr>
  <#assign resourceTypeCount = 0>
   <@s.iterator value="resourceTypeCounts" var="resourceCount">
    <#if resourceCount.key != 'ONTOLOGY' && resourceCount.key != 'CODING_SHEET'>
      <#assign resourceTypeCount = resourceCount.value.second + resourceTypeCount >
      
      <td><div class="label">${resourceCount.value.first}</div><div class="bar" id="${resourceCount.key}"></div></td>
    </#if>
  </@s.iterator>
  </tr>
  <tr>
   <@s.iterator value="resourceTypeCounts" var="resourceCount">
    <#if resourceCount.key != 'ONTOLOGY' && resourceCount.key != 'CODING_SHEET'>
      <td><div class="label">${resourceCount.key.label}</div></td>
    </#if>
   </@s.iterator>
   </tr>
</table>

<style>
<#assign barColors = ['#4B514D', '#2C4D56','#C3AA72','#DC7612','#BD3200','#A09D5B','#F6D86B'] />
table td  {vertical-align:bottom;;}
   <@s.iterator value="resourceTypeCounts" var="resourceCount" status="stat">
    #${resourceCount.key} {background-color: ${barColors[stat.index]}; height: ${(500 * (resourceCount.value.second / resourceTypeCount))?floor}px }
   </@s.iterator>
   

.bar {width:${barWidth?c}px;min-width:50px;;}
div.label {;top:0;z-index:1000;overflow:visible;;left:0px;position:relative;;display:block;text-align:center;font-size:smaller}
</style>
<script>
$(".bar").each(function() {
    var el = $(this);
    var color1 = el.css('background-color');
    var color2 = $.xcolor.darken(color1);
    el.hover(function(){
        el.stop().animate({backgroundColor: color2}, 'fast');
    }, function(){
        el.stop().animate({backgroundColor: color1}, 'fast');
    });
});
</script>


</div>
  <div id='map_canvas' style="border:1px solid #CCC;width:545px;clear:none"></div>

</div>
<div class="glide">
<h3>Getting Started</h3>
<ul>
<li> tutorial that can help you get started with tDAR is available 
<a href="http://dev.tdar.org/confluence/display/TDAR/User+Documentation">here</a>.</li>
<li> <a href="<@s.url value="/search/results?query=&searchProjects=true"/>">browse</a> all projects</li>
</ul>
</div>

</div>
</body>

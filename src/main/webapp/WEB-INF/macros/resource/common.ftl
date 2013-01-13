<#escape _untrusted as _untrusted?html>
<#import "/${themeDir}/settings.ftl" as settings />
<#-- 
$Id:Exp$
Common macros used in multiple contexts
-->
<#assign mb = 1048576 />
<#macro convertFileSize filesize=0>
<#assign kb = 1024 />
<#if (filesize > mb)>
${(filesize / mb)?string(",##0.00")}mb
<#elseif (filesize > kb)>
${(filesize / kb)?string(",##0.00")}kb
<#else>
${filesize?string(",##0.00")}b
</#if>
</#macro>
<#assign tdarBuildId><#include  "/version.txt" parse=false/></#assign>
<#assign tdarBuildId = tdarBuildId?trim?replace("+", ".001") />

<#macro globalJavascript>
<script type="text/javascript">

    <@baseUriJavascript />
    
    <@googleAnalyticsJavascript />

</script>
</#macro>

<#macro baseUriJavascript>
function getBaseURI() {
    return "<@s.url value='/' />";
}
function getURI(path) {
    return getBaseURI() + path;
}

TDAR.uri = function(path) {
    var uri = "<@s.url value='/' />";
    if(path) {uri += path;}
    return uri;
}
</#macro>

<#macro googleAnalyticsJavascript>
<#noescape>
  var _gaq = _gaq || [];
  <#if !production> 
    _gaq.push(['_setAccount', 'UA-13102200-5']); // TEST ACCOUNT
    _gaq.push(['_setDomainName', 'none']);
  <#else>
    _gaq.push(['_setAccount', '${googleAnalyticsId}']);
  </#if>

  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

//return basic perf stats (excellent diagram: http://dvcs.w3.org/hg/webperf/raw-file/tip/specs/NavigationTiming/Overview.html#processing-model)
(function() {
    var _getPerfStats = function() {
        var timing = window.performance.timing;
        return {
            //dns lookup timespan
            dns: timing.domainLookupEnd - timing.domainLookupStart,
            //connection timespan
            connect: timing.connectEnd - timing.connectStart,
            //time to first byte 
            ttfb: timing.responseStart - timing.connectEnd,
            //timespan of response load
            basePage: timing.responseEnd - timing.responseStart,
            //time to document.load
            frontEnd: timing.loadEventStart - timing.responseEnd
        };
    };
    
    var _trackEvent = function() {
        var arr = ["_trackEvent"].concat(Array.prototype.slice.call(arguments));
        _gaq.push(arr);
    };
    
    var _reportPerfStats = function() {
        if (!(window.performance && window.performance.timing )) return;
        var nav = window.performance.navigation;
        var navtype = undefined; 
        //backbutton navigation may skew stats. try to identify and tag w/ label (only supported in IE/chrome for now)
        if(nav && nav.type === nav.TYPE_BACK_FORWARD) {
            navtype = "backbutton";
        }
        
        if (typeof _gaq === "undefined") return;
        var perf = _getPerfStats();
        var key;
        for(key in perf) {
            _trackEvent("Navigation Timing(ms)", key, navtype, perf[key] ,  true);
        }
    };
    
    //here we explicitly hook into 'onload' since DOM timing stats are incomplete upon 'ready'.
    $(window).load(_reportPerfStats);
})();
    

</#noescape>
</#macro>


<#macro bootstrapNavbar>


            <div class="navbar">
              <div class="navbar-inner">
                <div class="container">
                  <!-- display this toggle button when navbar exceeds available width and 'collapses' -->   
                  <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                  </a>
                  
                  <!-- <a class="brand" href="#">Menu</a> -->
                  <!-- everything in the nav-collapse div will be hidden at 940px or less -->
                  <div class="nav-collapse">
                    <ul class="nav">
                      <li><a href="#">Home</a></li>
                      <li class="dropdown">
                          <a class="dropdown-toggle" data-toggle="dropdown" href="#">Search<b class="caret"></b></a>
                          <ul class="dropdown-menu">
                              <li><a href="<@s.url value='/search'/>">Search ${siteAcronym!""}</a></li>
                              <li><a href="<@s.url value='/browse/explore'/>">Explore</a></li>
                              <li><a href="<@s.url value='/search/results'/>">Browse All Resources</a></li>
                              <li><a href="<@s.url value='/search/collections'/>">Browse All Collections</a></li>
                          </ul>
                      </li>
                      <#if authenticatedUser??>
                      
                        <li  class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown" >Workspace<b class="caret"></b></a>
                            <ul class="dropdown-menu">
                                <li><a href="<@s.url value='/workspace/list'/>">Show Bookmarked Resources</a></li>
                                <li><a href="<@s.url value='/workspace/select-tables'/>">Integrate Bookmarked Data Tables</a></li>
                            </ul>
                        </li>
                
                      <li><a href="<@s.url value='/dashboard'/>">Your Resources</a></li>
                      <#if contributor!false>
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown" >New<b class="caret"></b></a>
                              <li><a href="<@s.url value='/project/add'/>">New...</a></li>
                              <li><a href="<@s.url value='/project/add'/>">New Project</a></li>
                              <li><a href="<@s.url value='/document/add'/>" class="item_line">New Document</a></li>
                              <li><a href="<@s.url value='/image/add'/>">New Image</a></li>
                              <#if administrator!false><li><a href="<@s.url value='/video/add'/>">New Video</a></li></#if>
                              <li><a href="<@s.url value='/dataset/add'/>">New Dataset</a></li>
                              <li><a href="<@s.url value='/coding-sheet/add'/>" class="item_line">New Coding Sheet</a></li>
                              <li><a href="<@s.url value='/ontology/add'/>">New Ontology</a></li>
                              <li><a href="<@s.url value='/sensory-data/add'/>">New Sensory Data</a></li>
                              <li><a href="<@s.url value='/collection/add'/>">New Collection</a></li>
                              <li style="border-top: 1px solid #AAA;"><a href="<@s.url value='/batch/add'/>">Batch Upload Tool</a></li>
                            <ul class="dropdown-menu">


                            </ul>
                        </li>
                        </#if>
                        <li><a href="<@s.url value='http://www.tdar.org'/>">About</a></li>
                        <#if editor!false>
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin<b class="caret"></b>
                            <ul class="dropdown-menu">
                                <li><a href="<@s.url value='/admin/internal'/>">Statistics</a></li>
                                <#if administrator!false><li><a href="<@s.url value='/admin/searchindex/build'/>">Build search index</a></li>
                                <li><a href="<@s.url value='/admin/system/activity'/>">System Activity</a></li>
                                
                                </#if>
                                <li><a href="<@s.url value='/admin/authority-management/index'/>">Merge duplicates</a></li>
                            </ul>
                        </li>
                        </#if>
                        <li><a href="<@s.url value='/logout'/>">Logout</a></li>
                        <#else>
                        <li><a href="<@s.url value='/login'/>">Login</a></li>
                        </#if>
                        <li class="dropdown">  
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Help
                                    <b class="caret"></b>
                                 </a>
                          <ul class="dropdown-menu">
                              <li><a href="${documentationUrl}">User Documentation</a></li>
                              <li><a href="${bugReportUrl}">Report a Bug</a></li>
                              <li><a href="${commentUrl}">Comments</a></li>
                              <#if authenticatedUser??>
                              <li><a href="<@s.url value='/entity/person/edit?id=${sessionData.person.id?c}'/>">Update your profile</a></li>
                              </#if>
                          </ul>
                        
                        </li>
                      
                    </ul>
                  </div><!-- /.nav-collapse -->
                </div>
              </div><!-- /navbar-inner -->
            </div>
</#macro>


<#macro resourceCollectionsRights effectiveResourceCollections_>
    <#if effectiveResourceCollections_?has_content>
    <h3>Access Permissions</h3>
    <#nested />
    <table class="tableFormat table">
    <thead><th>Collection</th><th>User</th>
    <#list availablePermissions as permission>
    <th>${permission.label}</th>
    </#list>
    <#list effectiveResourceCollections_ as collection_ >
      <#if collection_.authorizedUsers?has_content >
        <#list collection_.authorizedUsers as user>
        <tr>
          <td>
            <#if !collection_.internal>
               <a href="<@s.url value="/collection/${collection_.id?c}"/>"> ${collection_.name!"<em>un-named</em>"}</a>
            <#else>
                Local resource
            </#if>
          </td>
          <td>
            ${user.user.properName} <!-- ${user.user.properName}:${user.generalPermission} -->
          </td>
           <#list availablePermissions as permission>
          <td>
           <#if (user.generalPermission.effectivePermissions >= permission.effectivePermissions )>
           	<i class="icon-ok"></i>
           <#else>
            <i class="icon-remove"></i>
           </#if>
           </td>
	       </#list>
         </tr>
        </#list>
      </#if>
    </#list>
    </table>
    </#if>
</#macro>


<#macro pieChart map name type width=300 height=150>
    <#assign ilist = map />
    <#assign ikeys=ilist?keys />
    <div id="${name}"  style="width:${width}px;height:${height}px;"></div>
    
    <script>
<#noescape>
    var data${name} = [
    <#assign first = true/>
    <#assign legend = true>
    <#list ikeys as ikey>
      <#assign val = ilist.get(ikey) />
      <#assign label = ikey />
      <#if ikey.label??><#assign label=ikey.label ></#if>
      <#if (val?? && val > 0)>
        <#if !first>,</#if>{ label: "${label}", key:"${ikey}",  data: ${val?c},color: 
            <#if name !="resourceForUser">
                "${settings.barColors[ikey_index % settings.barColors?size]}"
            <#else>
                "${settings.barColors[ikey.order - 1]}"    
            </#if> }
        <#assign first=false/>
      </#if>
      <#if (ikey_index > settings.barColors?size)>
        <#assign legend = true>
      </#if>
    </#list>
    ];
</#noescape>
    $(function() {

        $.plot($("#${name}"), data${name}, {
            series: {
                pie: { 
                    show: true,
                    radius:1,
//                    tilt:.3,
                    label : {
                        formatter: function(label, series){
                            return '<div style="font-size:8pt;text-align:center;padding:2px;color:black;">'+label+' ('+series.datapoints.points[1]+ ')</div>';
                        },
                        radius: 6/7
                    }
                }
            },
            legend: {
                show: ${legend?string},
                position:"sw"
            },
            grid: {
                hoverable: true,
                clickable: true
            }
        });
        $("#${name}").bind("plotclick",function(event, pos, obj) { 
        for (var entry  in data${name}) {
            if (data${name}[entry].label == obj.series.label) {
                var key = data${name}[entry].key;
                var url = "<@s.url value="/search/results?"/>?<#noescape>${type}</#noescape>=" + key;
                document.location = url;
            }
        }
    });

    $("#${name}").bind("plothover", function (event, pos, item) {
            if (item) {
                if (previousPoint != item.seriesIndex) {
                    previousPoint = item.seriesIndex;
                    
                    $("#flottooltip").remove();
                    showTooltip(pos.pageX, pos.pageY  - 30, item.series.label );
               }
            }
            else {
                $("#flottooltip").remove();
                previousPoint = null;            
            }
    });

    });
    
            
    </script>

</#macro>

<#macro truncate text len=80>
<#compress>
  <#if text??>
  <#-- if text if greater than length -->
    <#if (text?length > len)>
    <#-- set pointer to last space before length (len) -->
     <#local ptr=text?last_index_of(" ",len) />
     <#-- if pointer to last space is greater than 1/2 of the max length, truncate at the pointer, 
           otherwise truncate at 3 before length -->
       <#if (ptr > len / 2)>
           ${text?substring(0,ptr)}...
       <#else>
           ${text?substring(0,len -3)}...
       </#if>
    <#else>
      ${text}
    </#if>
  </#if>
</#compress>
</#macro>

<#macro barGraph  resourceCacheObjects graphWidth=360 graphHeight=800 graphLabel="" rotateColors=true labelRotation=0 minWidth=50 searchKey="resourceTypes">
<#local totalItems = resourceCacheObjects?size />
   <#list resourceCacheObjects?sort_by("key") as key>
      <#if (key.count == 0) >
          <#local totalItems = totalItems - 1/>
      </#if>
    </#list>

    <#if (totalItems < 1)>
        <#local totalItems = 1 />
    </#if>
    
<#local barWidth = (graphWidth  / (totalItems) -6)/>
<div class="barGraph" style="width:${graphWidth?c}px;height:${graphHeight?c}px;" >
    <h3>${graphLabel}</h3>
   <table style="width:${graphWidth -5}px;height:${graphHeight - 15}px;">
  <tr>
  <#local resourceTypeCount = 0>
   <#list resourceCacheObjects?sort_by("key") as key>
      <#if (key.count > 0) >
        <#local calulatedValue= key.key >
        <#if calulatedValue?is_number>
            <#local calulatedValue=calulatedValue?c?string/>
        </#if>
        <#local resourceTypeCount = key.logCount + resourceTypeCount >
        <td>
              <a target="_top" href="<@s.url value="/search/results?${searchKey}=${calulatedValue}"/>">
              <div class="barlabel">${key.count}</div><div class="bar" id="${key.cssId}"></div></a>
        </td>
      </#if>
    </#list>
  </tr>
  <tr>
   <#list resourceCacheObjects?sort_by("key") as key>
      <#if (key.count > 0) >
      <td><div class="barlabel">${key.label}</div></td>
      </#if>
   </#list>
   </tr>
</table>

<!--[if IE]>
<style>
.barGraph table {
    position:absolute;
    bottom:5px;
}
</style>
<![endif]-->

<style>
table td  {vertical-align:bottom;}

    <#if resourceTypeCount == 0><#-- if database is empty, to prevent division by zero -->
        <#local resourceTypeCount = 1>
    </#if>

   <#list resourceCacheObjects?sort_by("key") as key>
    <#if (key.count > 0)>
       <#local color_= settings.barColors[0] />
        <#if rotateColors>
           <#local color_=settings.barColors[key.resourceType.order - 1] />
        </#if>
          #${key.cssId} {background-color: ${color_}; height: ${(2 * graphHeight * (key.logCount / resourceTypeCount))?floor}px }
    </#if>
   </#list>

   

.bar {width:${barWidth?c}px;;min-width:${minWidth?c}px }

 td > div.barlabel {
    <#if (labelRotation == 90) >
        -webkit-transform: rotate(90deg); 
        -moz-transform: rotate(90deg);    
        filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=1);
    </#if>
    <#if (labelRotation == 180) >
        -webkit-transform: rotate(180deg); 
        -moz-transform: rotate(180deg);    
        filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=2);
    </#if>
    <#if (labelRotation == 270 || labelRotation == -90) >
        -webkit-transform: rotate(-90deg); 
        -moz-transform: rotate(-90deg);    
        filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=3);
    </#if>
    
}
</style>
<script>
$(function() {
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
});
</script>

</div>
</#macro>

<#macro flotBarGraph  resourceCacheObjects graphWidth=368 graphHeight=800 graphLabel="" rotateColors=true labelRotation=0 minWidth=50 searchKey="g[0].creationDecade" explore=false max=100000 min=-1 minDataVal=10 >
<#local totalItems = resourceCacheObjects?size />
<#if (totalItems < 1)>
    <#local totalItems = 1 />
</#if>




<#local barWidth = (graphWidth  / (totalItems) - 10 )/>
<div class="barGraph" style="width:${graphWidth?c}px;height:${graphHeight?c}px;display:block;position:relative;clear:none;left:0px"> 
<script>
  <#local resourceTypeCount = 0>
   var data = [];
   var ticks = [];
   <#list resourceCacheObjects?sort_by("key") as key>
    <#local calulatedValue= key.key >
    <#if calulatedValue?is_number>
        <#local calulatedValue=calulatedValue?c?string/>
    </#if>
    <#if ((min?is_number && key.label?number > min ) 
        && (max?is_number && key.label?number < max ) && key.count > minDataVal )>
      <#local resourceTypeCount = key.logCount + resourceTypeCount >
      data[${key_index}] = ["${key.label}",${key.count?c}];
      ticks[${key_index}] = ["${key.label}",${key.label}];
    </#if>
    </#list>
$(function() {
    $.plot(
       $("#bargraph"),
       [
        {
          label: "${graphLabel?js_string}",
          data: data,
          bars: {
            show: true,
            barWidth: 9,
            align: "center"
          }
        }
      ],
    {
         grid: { hoverable: true, clickable: true },
         legend : { show:false }
    }
    );
        $("#bargraph").bind("plotclick",function(event, pos, obj) { 
        console.log(obj);
             if (obj) {
               //fixme: s.url links be
            var url = "<@s.url escapeAmp=false value="/search/results?${explore?string('explore=true&', '')}${searchKey}"/>" + obj.datapoint[0];
            console.log(url);
            document.location = url;
            }
        });
});
</script>
<div id="bargraph" style="width:${(graphWidth -5)?c}px;height:${(graphHeight -5)?c}px"></div>

<script>
$(function() {
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
});
</script>

</div>
</#macro>

<#macro worldMap forceAddSchemeHostAndPort=false>
<div class="mapcontainer" style="margin-left:-40px;position:relative;right:0px">
<script type="text/javascript">
$(function() {

$('.worldmap').maphilight({
      fade: true, 
      groupBy:"alt",
      strokeColor:'#ffffff'
    });
    
  $(".worldmap").delegate('area', 'mouseover',function(e) {
        $('[iso='+$(this).attr('iso')+']').each(function(index,val) {
            hightlight(true,val);
        });
   });

  $(".worldmap").delegate('area', 'mouseout',function(e) {
        $('[iso='+$(this).attr('iso')+']').each(function(index,val) {
            hightlight(false,val);
        });
    });
    
  });
  
  function hightlight(on,element) {
    var data = $(element).data('maphilight') || {};
    if (on) {
        data.oldFillColor=data.fillColor;
        data.oldFillOpacity=data.fillOpacity;
        data.oldStrokeColor=data.strokeColor;
        data.oldStrokeWidth=data.strokeWidth;

        data.fillColor='4B514D';
        data.fillOpacity=.5;
        data.strokeColor='111111';
        data.strokeWidth='.6';
    } else {
        data.fillColor=data.oldFillColor;
        data.fillOpacity=data.oldFillOpacity;
        data.strokeColor=data.oldStrokeColor;
        data.strokeWidth=data.oldStrokeWidth;
    }
    $(element).data('maphilight',data).trigger('alwaysOn.maphilight');
  }

  
<#-- 
FIXME: why not do this logic in the controller?
this bit of freemarker is voodoo:
  1. it iterates through our country code hash
  2. it creates a template which is the hash contents {"US":5, "CA":25}
  3. it evaluates the hash contents via hash addition to produce a new hash that can be used like ${codes['US']}
     which is important because ${iSOCountryCodes['US (ISO Country Code)']} does not work, nor does
     ${iSOCountryCodes.get('US (ISO Country Code)')
  -->
<#assign countryTotal = 0>
<#assign max = 0>
<#assign countryLogTotal = 0>
<#assign templateSource>{<#list geographicKeywordCache as key>
 <#assign code=key.label?substring(0,2) />
 "${code}" : ${key.count?c},<#assign countryTotal = key.count />
 "${code}_" : ${key.logCount?c}<#if key_has_next>,</#if>
    <#if (countryLogTotal < key.logCount)><#assign countryLogTotal = key.logCount /></#if>
    <#if (max < key.count)><#assign max = key.count /></#if>
   </#list>}</#assign>
  
<#assign codes= {} + templateSource?eval />

</script> 
<!-- div style="height:353px;border:1px solid #CCC;background-color:#fff;width:550px;padding-top:5px" -->
<img class="worldmap" src="<@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="/images/world_480_2.png" />" width=480 height=304 usemap="#world" > 
 <div id="map_legend">
  <div><span class='legendText'>none</span> 
    <#list settings.mapColors as color>
      <span class="legendBox" style="background-color:#${color}"></span>    
    </#list>
<span class='legendText'>many</span></div>
 </div>


<map name=world> 

<!--
<area href="#RU" title="Russian Federation" shape="poly" coords="543,103 , 539,104 , 534,104 , 539,104 , 541,107 , 541,111 , 538,109 , 535,111 , 531,113 , 528,116 , 525,115 , 522,116 , 519,116 , 517,119 , 517,123 , 516,127 , 513,128 , 511,132 , 508,133 , 507,127 , 507,123 , 510,120 , 514,117 , 519,114 , 519,111 , 514,114 , 510,112 , 507,114 , 504,117 , 500,118 , 497,117 , 494,117 , 489,117 , 486,119 , 481,122 , 476,127 , 479,128 , 482,128 , 485,130 , 484,133 , 484,137 , 482,140 , 477,145 , 474,148 , 471,147 , 471,144 , 474,143 , 476,140 , 472,140 , 469,138 , 465,137 , 464,133 , 462,130 , 459,129 , 455,130 , 454,133 , 452,136 , 448,136 , 443,137 , 440,137 , 437,137 , 432,135 , 429,135 , 424,133 , 421,134 , 418,136 , 414,135 , 409,136 , 405,137 , 402,137 , 399,134 , 396,133 , 392,130 , 387,129 , 384,128 , 380,126 , 377,126 , 373,127 , 370,128 , 366,129 , 367,131 , 366,135 , 363,134 , 360,134 , 357,135 , 354,133 , 349,134 , 346,135 , 345,138 , 349,141 , 348,145 , 350,148 , 346,150 , 343,148 , 340,147 , 336,146 , 333,145 , 332,142 , 335,139 , 333,136 , 330,135 , 327,133 , 323,132 , 324,129 , 322,126 , 319,125 , 317,121 , 317,118 , 321,116 , 317,115 , 323,110 , 321,107 , 320,104 , 320,100 , 321,96 , 319,92 , 322,90 , 325,90 , 329,91 , 333,94 , 337,96 , 336,100 , 329,100 , 325,99 , 328,102 , 328,105 , 332,107 , 330,104 , 333,104 , 336,105 , 338,101 , 341,100 , 341,97 , 342,94 , 345,95 , 344,99 , 349,96 , 353,93 , 357,92 , 361,93 , 364,93 , 366,89 , 371,90 , 375,92 , 378,92 , 375,90 , 376,86 , 378,82 , 378,79 , 382,78 , 383,82 , 384,86 , 384,89 , 385,94 , 383,97 , 380,99 , 383,100 , 386,97 , 387,93 , 391,93 , 387,92 , 385,88 , 385,84 , 387,81 , 390,85 , 393,85 , 389,83 , 392,80 , 396,81 , 400,83 , 398,86 , 400,83 , 396,80 , 395,77 , 402,75 , 406,74 , 403,71 , 406,69 , 415,66" iso="RU" 
<area href="#RU" title="Russian Federation" shape="poly" coords="415,66 , 419,65 , 422,64 , 423,67 , 422,63 , 426,63 , 429,57 , 433,57 , 432,61 , 435,61 , 441,62 , 443,65 , 444,69 , 441,71 , 436,75 , 433,77 , 437,77 , 440,75 , 444,75 , 444,79 , 447,75 , 452,76 , 457,78 , 461,79 , 464,82 , 467,83 , 469,86 , 473,82 , 478,83 , 481,84 , 484,81 , 494,80 , 490,81 , 494,81 , 491,82 , 494,81 , 498,81 , 498,85 , 501,85 , 507,85 , 512,86 , 513,89 , 515,92 , 519,89 , 523,90 , 527,92 , 529,88 , 532,89 , 537,89 , 541,91 , 543,95 , 543,99 , 543,103" iso="RU" alt="Russian Federation" 
-->

<!-- python svg2imagemap.py world.svg 550 420 -->
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="AF" coords="330,167 , 333,167 , 336,166 , 339,167 , 336,169 , 336,172 , 334,175 , 330,176 , 327,179 , 323,178 , 323,175 , 323,172 , 325,170 , 329,167" title="Afghanistan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="AO" coords="259,237 , 259,232 , 261,229 , 260,225 , 265,223 , 268,226 , 272,225 , 272,229 , 274,232 , 272,236 , 268,238 , 261,238" title="Angola" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="AR" coords="150,275 , 150,273 , 150,269 , 150,266 , 151,263 , 152,260 , 151,257 , 152,253 , 153,250 , 154,247 , 156,244 , 159,245 , 164,246 , 168,248 , 167,251 , 170,251 , 173,248 , 171,251 , 168,254 , 167,258 , 167,261 , 169,264 , 164,268 , 162,271 , 159,270 , 160,274 , 158,277 , 155,280 , 155,285 , 153,288 , 154,291 , 149,290 , 148,287 , 149,284 , 150,281 , 150,278" title="Argentina" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="AU" coords="443,252 , 443,257 , 440,260 , 439,263 , 436,266 , 433,267 , 429,267 , 425,265 , 424,262 , 423,259 , 420,261 , 418,258 , 414,256 , 409,258 , 406,258 , 402,260 , 399,261 , 395,262 , 394,258 , 393,255 , 392,252 , 391,248 , 392,245 , 395,242 , 399,241 , 402,240 , 403,237 , 406,235 , 409,233 , 412,235 , 413,231 , 417,230 , 419,231 , 420,234 , 423,236 , 426,238 , 428,235 , 428,232 , 432,233 , 433,236 , 434,239 , 437,241 , 438,244 , 441,247 , 443,250" title="Australia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="BF" coords="236,203 , 237,200 , 241,199 , 244,198 , 245,201 , 240,203 , 237,203" title="Burkina Faso" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="BO" coords="153,238 , 153,235 , 153,232 , 156,228 , 159,231 , 162,232 , 165,235 , 168,238 , 168,241 , 163,240 , 161,244 , 157,244 , 154,243 , 153,240" title="Plurinational State of Bolivia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="BR" coords="185,218 , 188,220 , 191,220 , 195,222 , 198,225 , 196,229 , 193,232 , 193,236 , 191,239 , 190,243 , 187,244 , 183,246 , 180,248 , 180,251 , 177,256 , 175,259 , 171,256 , 168,254 , 171,251 , 173,248 , 171,244 , 168,244 , 167,240 , 167,237 , 164,233 , 161,232 , 158,230 , 155,229 , 151,230 , 148,227 , 147,224 , 149,221 , 153,218 , 152,215 , 155,213 , 158,215 , 160,212 , 162,211 , 165,211 , 165,214 , 168,214 , 172,213 , 175,213 , 178,214 , 176,217 , 179,218 , 182,217 , 185,218" title="Brazil" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="BW" coords="269,247 , 269,244 , 271,239 , 274,238 , 277,240 , 281,243 , 278,246 , 274,248 , 270,250" title="Botswana" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="BY" coords="274,143 , 274,138 , 277,137 , 278,134 , 281,134 , 284,137 , 284,140 , 283,143 , 280,142 , 276,142" title="Belarus" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="100,72 , 99,68 , 102,69 , 106,70 , 105,75 , 102,75 , 98,77 , 94,76 , 98,75 , 95,73 , 92,74 , 93,71 , 93,68 , 96,69 , 98,72" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="100,86 , 102,89 , 103,85 , 102,82 , 106,84 , 106,87 , 107,91 , 111,94 , 109,98 , 104,98 , 101,100 , 96,100 , 91,97 , 94,94 , 98,94 , 95,93 , 90,93 , 93,90 , 89,90 , 89,87 , 94,82 , 94,85 , 98,85" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="108,55 , 106,51 , 109,51 , 111,54 , 113,57 , 113,60 , 109,58 , 106,56" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="112,82 , 116,80 , 115,84 , 117,86 , 115,89 , 112,87 , 110,84 , 113,84 , 112,81" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="118,82 , 120,79 , 123,80 , 123,85 , 121,88 , 119,85" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="123,52 , 120,50 , 117,47 , 121,45 , 117,44 , 121,43 , 119,39 , 120,36 , 125,41 , 127,44 , 130,47 , 129,50 , 132,51 , 130,54 , 128,57 , 123,58 , 121,55 , 125,52" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="130,73 , 134,71 , 137,71 , 138,75 , 135,77 , 132,77 , 128,77 , 125,76 , 123,73 , 122,68 , 119,69 , 117,65 , 121,65 , 125,67 , 126,70 , 127,73 , 131,73" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="149,120 , 152,122 , 153,125 , 154,128 , 157,128 , 158,125 , 161,127 , 162,130 , 163,133 , 166,135 , 165,138 , 168,138 , 171,141 , 167,143 , 163,146 , 157,145 , 152,149 , 157,147 , 158,150 , 160,153 , 163,154 , 160,155 , 157,157 , 161,154 , 157,154 , 155,151 , 151,153 , 145,155 , 141,157 , 135,160 , 136,157 , 136,154 , 133,151 , 128,149 , 125,149 , 121,148 , 95,148 , 83,148 , 81,145 , 77,144 , 77,141 , 76,138 , 74,135 , 71,132 , 69,128 , 67,125 , 62,124 , 59,96 , 64,99 , 67,99 , 71,96 , 74,95 , 70,98 , 73,96 , 77,94 , 80,98 , 81,95 , 85,96 , 91,99 , 94,100 , 98,103 , 101,104 , 103,107 , 103,104 , 104,101 , 108,101 , 112,103 , 116,103 , 116,100 , 118,103 , 118,106 , 118,103 , 121,100 , 120,96 , 118,93 , 118,90 , 121,89 , 122,92 , 123,95 , 125,99 , 128,99 , 128,103 , 131,102 , 132,99 , 132,96 , 136,96 , 137,100 , 137,104 , 135,107 , 130,107 , 130,110 , 126,108 , 130,110 , 128,114 , 123,115 , 123,118 , 121,121 , 120,124 , 120,127 , 123,131 , 127,132 , 131,134 , 136,135 , 136,139 , 138,142 , 140,139 , 139,136 , 143,133 , 143,130 , 142,126 , 142,123 , 141,119 , 145,119 , 149,119" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="152,44 , 150,47 , 146,47 , 146,50 , 143,50 , 146,52 , 141,52 , 145,53 , 145,57 , 141,59 , 140,63 , 136,61 , 136,64 , 140,64 , 142,67 , 138,69 , 137,66 , 133,67 , 130,67 , 127,68 , 128,64 , 128,61 , 131,61 , 134,63 , 136,59 , 134,62 , 133,59 , 133,56 , 132,59 , 130,56 , 136,56 , 137,53 , 133,53 , 132,49 , 130,46 , 134,45 , 137,49 , 135,44 , 142,42 , 141,38 , 137,42 , 134,41 , 130,42 , 132,39 , 136,38 , 132,39 , 129,42 , 133,37 , 126,39 , 128,35 , 124,35 , 125,32 , 128,30 , 132,32 , 130,29 , 134,28 , 140,32 , 135,27 , 137,23 , 141,25 , 144,25 , 142,22 , 146,21 , 149,24 , 149,20 , 151,20 , 157,22 , 154,25 , 157,23 , 160,23 , 161,27 , 164,27 , 160,33 , 154,34 , 152,38 , 157,35 , 155,40 , 153,44" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="160,104 , 163,106 , 161,109 , 158,110 , 158,107 , 155,107 , 154,110 , 158,112 , 159,115 , 158,118 , 155,115 , 156,118 , 153,118 , 150,116 , 147,113 , 144,112 , 141,112 , 144,111 , 147,110 , 148,106 , 149,103 , 147,100 , 143,100 , 143,97 , 140,94 , 137,95 , 131,95 , 127,93 , 126,90 , 126,87 , 126,84 , 128,81 , 133,80 , 130,85 , 131,89 , 130,92 , 133,90 , 132,87 , 132,84 , 135,81 , 138,84 , 138,87 , 141,88 , 141,85 , 144,85 , 144,89 , 147,89 , 147,92 , 150,91 , 150,94 , 153,92 , 152,95 , 156,95 , 152,97 , 156,98 , 153,98 , 155,101 , 158,103" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="85,67 , 88,63 , 91,63 , 92,66 , 89,66 , 88,70 , 84,70" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CA" coords="92,81 , 88,85 , 86,89 , 83,91 , 80,88 , 80,84 , 82,80 , 85,77 , 88,79 , 92,81" title="Canada" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CD" coords="282,221 , 283,225 , 281,228 , 281,231 , 277,231 , 273,230 , 272,226 , 269,225 , 265,225 , 260,223 , 263,221 , 265,218 , 267,213 , 268,210 , 272,211 , 275,210 , 278,210 , 282,210 , 284,213 , 282,218 , 281,221" title="The Democratic Republic of the Congo" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CF" coords="267,212 , 264,213 , 262,210 , 263,207 , 267,206 , 272,204 , 275,205 , 278,208 , 274,210 , 270,211" title="Central African Republic" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CG" coords="267,212 , 266,215 , 264,219 , 262,222 , 259,222 , 258,219 , 261,219 , 261,216 , 264,213 , 267,212" title="Congo" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CL" coords="147,279 , 148,276 , 148,273 , 147,270 , 147,267 , 148,263 , 150,259 , 150,255 , 151,251 , 151,248 , 151,245 , 151,239 , 154,242 , 156,245 , 154,248 , 153,251 , 152,255 , 151,258 , 152,260 , 151,263 , 151,267 , 149,270 , 149,274 , 150,277 , 150,280 , 149,283 , 147,287 , 149,290 , 154,291 , 151,292 , 148,293 , 148,289 , 146,286 , 147,283 , 146,281" title="Chile" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CM" coords="261,213 , 256,213 , 255,210 , 256,207 , 260,205 , 261,202 , 263,206 , 262,209 , 264,212 , 261,213" title="Cameroon" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CN" coords="359,182 , 356,181 , 353,179 , 349,177 , 346,176 , 346,173 , 345,170 , 342,168 , 340,165 , 341,162 , 345,161 , 348,158 , 348,155 , 351,154 , 352,151 , 355,151 , 356,147 , 359,149 , 362,152 , 365,155 , 368,158 , 373,159 , 376,159 , 379,160 , 383,159 , 387,159 , 388,156 , 391,155 , 395,153 , 399,152 , 396,150 , 395,146 , 399,146 , 400,143 , 401,139 , 404,139 , 408,140 , 408,143 , 410,146 , 413,148 , 416,150 , 419,149 , 418,152 , 415,155 , 414,158 , 410,160 , 406,162 , 402,164 , 401,161 , 397,164 , 399,167 , 402,167 , 399,170 , 401,174 , 402,176 , 402,179 , 400,182 , 398,185 , 394,188 , 391,187 , 388,189 , 385,189 , 381,187 , 378,188 , 375,190 , 372,187 , 371,184 , 371,180 , 368,179 , 364,180 , 361,180" title="China" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CO" coords="145,216 , 142,215 , 143,211 , 142,208 , 144,205 , 147,202 , 150,201 , 147,205 , 150,208 , 154,209 , 156,212 , 152,214 , 153,218 , 152,221 , 149,219 , 146,216" title="Columbia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CU" coords="145,191 , 142,191 , 139,189 , 135,188 , 139,187 , 142,189 , 145,190" title="Cuba" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="CZ" coords="268,147 , 265,148 , 261,148 , 259,145 , 263,144 , 266,145" title="Czech Republic" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="DE" coords="255,136 , 259,137 , 262,140 , 263,143 , 259,145 , 261,148 , 258,150 , 254,150 , 251,147 , 251,144 , 252,141 , 255,138" title="Germany" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="DZ" coords="249,192 , 245,191 , 237,185 , 232,182 , 234,178 , 237,177 , 242,175 , 241,171 , 245,168 , 248,168 , 252,167 , 254,170 , 253,173 , 256,177 , 256,181 , 256,184 , 253,190 , 249,192" title="Algeria" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="EG" coords="288,176 , 288,181 , 286,178 , 287,181 , 289,185 , 291,189 , 284,189 , 276,189 , 276,179 , 276,176 , 279,176 , 282,176 , 285,176" title="Egypt" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="EH" coords="221,190 , 223,186 , 224,183 , 232,181 , 227,183 , 227,187 , 221,189" title="Western Sahara" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ES" coords="236,169 , 234,166 , 234,163 , 235,160 , 232,160 , 233,157 , 237,157 , 241,157 , 244,159 , 247,159 , 244,162 , 243,164 , 240,168 , 237,168" title="Spain" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ET" coords="298,211 , 295,212 , 291,211 , 288,208 , 288,204 , 289,202 , 291,198 , 294,198 , 298,201 , 300,205 , 305,206 , 302,210 , 298,211" title="Ethiopia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="FI" coords="275,109 , 274,106 , 274,102 , 270,98 , 273,100 , 277,99 , 278,95 , 281,96 , 281,99 , 282,103 , 282,108 , 282,111 , 283,114 , 284,117 , 279,123 , 275,124 , 271,123 , 271,119 , 271,117 , 275,113 , 276,110" title="Finland" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="FR" coords="240,151 , 237,150 , 241,148 , 244,147 , 247,144 , 249,145 , 253,147 , 253,150 , 252,153 , 252,156 , 249,157 , 246,159 , 243,158 , 242,155 , 242,152" title="France" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="GB" coords="238,129 , 240,132 , 241,136 , 244,140 , 244,143 , 240,145 , 237,145 , 240,142 , 237,143 , 238,139 , 239,136 , 237,133 , 236,130 , 239,128" title="United Kingdom" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="GL" coords="174,102 , 178,102 , 175,101 , 177,98 , 178,95 , 172,93 , 177,94 , 176,91 , 175,88 , 171,90 , 172,86 , 171,83 , 170,80 , 170,77 , 168,74 , 166,71 , 162,69 , 159,69 , 156,69 , 152,68 , 152,65 , 157,64 , 153,63 , 157,63 , 154,61 , 150,60 , 149,56 , 153,54 , 157,52 , 158,46 , 155,45 , 157,42 , 160,38 , 161,41 , 161,37 , 164,35 , 165,31 , 170,36 , 167,32 , 170,28 , 173,30 , 173,34 , 174,31 , 179,34 , 177,29 , 177,26 , 185,33 , 185,30 , 184,24 , 188,24 , 191,28 , 191,25 , 183,23 , 187,22 , 187,19 , 193,24 , 195,20 , 193,17 , 196,18 , 199,17 , 204,15 , 209,18 , 205,20 , 202,21 , 197,22 , 202,22 , 206,20 , 211,20 , 209,24 , 213,23 , 214,28 , 211,29 , 202,29 , 200,32 , 205,31 , 210,31 , 207,35 , 212,33 , 214,30 , 214,35 , 212,40 , 216,34 , 219,36 , 220,32 , 224,32 , 227,34 , 225,38 , 222,41 , 219,42 , 215,42 , 222,42 , 218,44 , 216,47 , 220,46 , 217,50 , 217,53 , 215,57 , 214,61 , 218,61 , 218,63 , 215,65 , 215,69 , 218,73 , 214,72 , 215,75 , 218,76 , 214,77 , 215,81 , 211,81 , 207,82 , 211,84 , 208,84 , 211,85 , 214,88 , 215,91 , 212,92 , 210,89 , 206,87 , 210,90 , 207,91 , 206,94 , 210,94 , 214,95 , 211,97 , 209,100 , 206,101 , 202,102 , 199,105 , 196,108 , 193,109 , 190,111 , 190,114 , 189,117 , 188,120 , 187,123 , 184,123 , 180,122 , 180,119 , 177,116 , 178,113 , 175,111 , 176,108 , 178,105 , 174,108 , 173,105 , 176,104 , 173,104 , 178,104 , 173,104" title="Greenland" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="GN" coords="232,207 , 229,205 , 226,205 , 224,202 , 227,201 , 231,201 , 233,204" title="Guinea" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="GR" coords="274,165 , 273,168 , 272,165 , 270,162 , 275,160 , 278,160 , 274,162 , 273,165" title="Greece" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ID" coords="368,211 , 374,212 , 377,215 , 379,218 , 381,221 , 377,221 , 374,217 , 372,214 , 369,212" title="Indonesia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ID" coords="383,223 , 387,225 , 390,225 , 386,225 , 382,225" title="Indonesia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ID" coords="386,214 , 390,214 , 393,214 , 394,211 , 397,213 , 396,217 , 395,220 , 391,220 , 388,220 , 385,217 , 386,214" title="Indonesia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ID" coords="403,215 , 407,214 , 404,215 , 400,215 , 403,217 , 403,220 , 400,220 , 400,223 , 398,220 , 399,216 , 403,215" title="Indonesia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ID" coords="427,219 , 427,224 , 427,227 , 424,225 , 420,221 , 417,220 , 414,218 , 417,217 , 419,220 , 422,219 , 425,219" title="Indonesia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="IN" coords="370,180 , 367,183 , 366,186 , 363,187 , 363,184 , 360,183 , 359,186 , 357,189 , 354,192 , 351,195 , 348,197 , 348,201 , 346,204 , 343,205 , 342,202 , 340,198 , 338,193 , 338,189 , 335,190 , 332,187 , 335,186 , 335,183 , 338,179 , 341,176 , 340,173 , 343,171 , 346,172 , 346,174 , 349,178 , 350,181 , 354,182 , 357,183 , 360,182 , 363,182 , 366,179 , 369,179" title="India" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="IQ" coords="306,178 , 302,179 , 298,176 , 294,175 , 297,171 , 297,169 , 300,167 , 304,169 , 303,173 , 305,176" title="Iraq" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="IR" coords="323,170 , 322,173 , 323,176 , 324,180 , 324,183 , 321,184 , 318,182 , 315,183 , 312,181 , 308,178 , 305,176 , 303,174 , 303,170 , 302,167 , 301,164 , 304,165 , 308,166 , 311,166 , 314,167 , 318,166 , 321,167" title="Islamic Republic of Iran" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="IS" coords="224,108 , 226,111 , 222,114 , 219,116 , 216,114 , 212,112 , 212,109 , 215,108 , 219,108 , 222,107" title="Iceland" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="IT" coords="253,157 , 252,153 , 255,153 , 259,151 , 259,154 , 261,157 , 264,160 , 267,163 , 264,166 , 264,163 , 260,161 , 257,158 , 253,157" title="Italy" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="JP" coords="418,171 , 415,172 , 418,170 , 421,168 , 425,166 , 425,163 , 428,164 , 427,168 , 425,171 , 422,171 , 418,171" title="Japan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="KE" coords="298,211 , 297,217 , 295,220 , 292,220 , 287,217 , 289,214 , 288,210 , 291,211 , 295,212 , 298,211" title="Kenya" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="KG" coords="339,164 , 336,164 , 337,160 , 340,158 , 343,158 , 346,158 , 345,161 , 342,162" title="Kyrgyzstan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="KZ" coords="316,161 , 312,160 , 308,159 , 307,155 , 306,152 , 305,149 , 304,146 , 308,144 , 311,143 , 314,144 , 318,144 , 321,144 , 323,141 , 323,138 , 328,137 , 332,136 , 336,135 , 338,138 , 341,138 , 345,139 , 348,144 , 351,144 , 354,146 , 357,147 , 355,151 , 352,151 , 350,154 , 347,155 , 349,158 , 343,158 , 340,158 , 336,158 , 333,161 , 330,161 , 329,158 , 326,157 , 320,154 , 316,155 , 316,161" title="Kazakhstan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="LA" coords="374,191 , 377,189 , 380,191 , 381,194 , 384,197 , 381,199 , 380,196 , 376,193" title="Lao People's Democratic Republic" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="LV" coords="271,133 , 272,130 , 275,131 , 278,130 , 280,133 , 277,133 , 272,133" title="Latvia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="LY" coords="274,192 , 264,187 , 261,187 , 257,185 , 256,182 , 256,179 , 256,176 , 259,174 , 263,174 , 268,177 , 270,174 , 273,174 , 275,178 , 276,189 , 274,192" title="Libyan Arab Jamahiriya" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MA" coords="239,170 , 241,173 , 238,176 , 235,178 , 232,180 , 226,181 , 230,179 , 230,176 , 234,172 , 239,170" title="Morocco" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MG" coords="301,248 , 300,245 , 301,241 , 301,238 , 304,236 , 306,233 , 309,234 , 308,237 , 305,247 , 301,248" title="Madagascar" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ML" coords="236,203 , 233,204 , 231,201 , 228,200 , 231,197 , 236,197 , 235,185 , 245,190 , 249,192 , 249,196 , 245,197 , 242,198 , 239,200 , 236,203" title="Mali" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MM" coords="367,196 , 366,193 , 363,190 , 365,187 , 367,184 , 370,182 , 370,185 , 372,188 , 372,192 , 372,195 , 372,199 , 372,203 , 372,200 , 371,197 , 368,197" title="Myanmar" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MN" coords="396,152 , 393,154 , 389,155 , 388,158 , 383,159 , 380,160 , 376,159 , 373,159 , 370,158 , 367,156 , 363,155 , 362,151 , 359,149 , 360,146 , 364,144 , 367,146 , 370,146 , 371,143 , 377,143 , 381,145 , 384,146 , 388,147 , 392,145 , 395,146 , 394,149 , 398,149" title="Mongolia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MR" coords="222,196 , 222,193 , 221,190 , 226,189 , 227,183 , 232,183 , 237,185 , 236,196 , 231,197 , 228,197 , 225,196" title="Mauritania" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MX" coords="117,183 , 116,187 , 117,190 , 120,194 , 123,193 , 125,190 , 129,189 , 129,193 , 125,194 , 123,197 , 120,196 , 116,196 , 112,195 , 109,193 , 105,191 , 105,188 , 102,185 , 100,182 , 97,179 , 96,176 , 95,179 , 97,182 , 99,186 , 96,182 , 93,179 , 91,176 , 98,176 , 102,176 , 106,177 , 109,178 , 113,180 , 116,183" title="Mexico" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MY" coords="393,210 , 396,208 , 398,211 , 394,211 , 393,214 , 390,214 , 386,214 , 390,212" title="Malaysia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="MZ" coords="285,249 , 284,245 , 285,243 , 286,240 , 286,237 , 283,236 , 287,233 , 289,237 , 289,233 , 290,230 , 293,230 , 296,233 , 295,236 , 291,238 , 288,240 , 289,244 , 289,247 , 286,248" title="Mozambique" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="NA" coords="269,247 , 269,252 , 266,253 , 263,249 , 262,244 , 259,240 , 267,238 , 270,238 , 274,238 , 271,239 , 271,243 , 269,247" title="Namibia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="NE" coords="244,198 , 248,197 , 249,192 , 253,190 , 259,187 , 263,187 , 263,190 , 263,195 , 261,198 , 258,200 , 255,200 , 251,199 , 248,201 , 244,200" title="Niger" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="NG" coords="262,200 , 261,204 , 259,207 , 256,208 , 253,211 , 249,208 , 247,205 , 248,202 , 249,199 , 253,200 , 256,200 , 259,200" title="Nigeria" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="NO" coords="253,117 , 256,115 , 258,112 , 260,109 , 261,106 , 264,105 , 264,101 , 267,98 , 270,96 , 273,94 , 276,92 , 279,92 , 283,93 , 281,96 , 278,95 , 277,99 , 273,100 , 270,98 , 267,100 , 264,104 , 263,107 , 262,110 , 262,113 , 259,116 , 259,120 , 260,123 , 259,127 , 256,127 , 252,129 , 252,126 , 252,123 , 250,120 , 253,119" title="Norway" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="NZ" coords="470,271 , 468,274 , 466,277 , 464,280 , 461,280 , 463,276 , 466,274 , 468,271" title="New Zeland" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="NZ" coords="471,269 , 471,266 , 469,262 , 472,264 , 476,266 , 474,269 , 472,272 , 471,269" title="New Zeland" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="OM" coords="312,196 , 311,193 , 315,191 , 315,188 , 316,185 , 320,186 , 321,189 , 319,193 , 316,194 , 312,196" title="Oman" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="PE" coords="153,229 , 153,233 , 153,236 , 150,237 , 145,235 , 143,231 , 140,226 , 137,223 , 138,220 , 142,220 , 145,218 , 148,218 , 151,219 , 148,222 , 147,225 , 149,228 , 153,229" title="Peru" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="PG" coords="434,226 , 431,226 , 427,227 , 427,224 , 427,219 , 430,220 , 436,223 , 436,226 , 440,229 , 436,228" title="Papua New Guinea" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="PK" coords="345,170 , 340,171 , 340,174 , 339,177 , 337,181 , 334,182 , 336,185 , 333,186 , 330,184 , 327,184 , 324,184 , 325,180 , 330,178 , 333,175 , 334,172 , 336,169 , 340,168 , 343,169" title="Pakistan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="PL" coords="273,147 , 269,147 , 266,146 , 263,144 , 262,141 , 262,138 , 267,136 , 273,137 , 274,140 , 275,144 , 273,147" title="Poland" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="PY" coords="161,244 , 163,240 , 166,240 , 168,244 , 171,244 , 172,248 , 168,251 , 168,248 , 165,246 , 161,244" title="Paraquay" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RO" coords="273,156 , 270,153 , 273,149 , 276,150 , 280,151 , 281,154 , 277,156 , 273,157" title="Romania" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="313,90 , 310,87 , 313,84 , 317,83 , 316,86 , 317,91 , 313,92" title="Russian Federation" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="369,45 , 365,46 , 362,44 , 365,41 , 368,37 , 371,41 , 370,44" title="Russian Federation" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="373,51 , 371,54 , 368,52 , 366,49 , 367,46 , 370,45 , 370,48 , 374,48 , 373,51" title="Russian Federation" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="380,56 , 377,58 , 373,59 , 374,55 , 375,52 , 379,52 , 381,56" title="Russian Federation" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="428,140 , 430,143 , 432,148 , 430,152 , 428,148 , 428,145 , 428,141" title="Russian Federation" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="432,73 , 429,73 , 426,76 , 422,73 , 422,70 , 426,71 , 430,71" title="Russian Federation" />
<!-- -->
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="478,111 , 475,112 , 471,112 , 475,112 , 476,115 , 477,118 , 471,119 , 468,121 , 465,125 , 461,124 , 458,124 , 455,127 , 455,131 , 456,133 , 454,137 , 452,140 , 447,144 , 447,141 , 446,136 , 447,132 , 450,129 , 452,125 , 457,122 , 457,119 , 453,122 , 452,119 , 449,120 , 444,125 , 440,127 , 437,125 , 434,126 , 431,126 , 428,128 , 424,131 , 419,136 , 423,137 , 426,138 , 427,141 , 427,144 , 426,147 , 424,151 , 420,156 , 417,159 , 414,159 , 414,155 , 417,154 , 418,151 , 414,150 , 412,147 , 409,144 , 407,141 , 404,139 , 401,139 , 400,143 , 399,146 , 395,146 , 392,145 , 388,147 , 384,146 , 381,145 , 377,145 , 374,142 , 371,146 , 368,146 , 365,145 , 360,146 , 357,147 , 354,146 , 351,144 , 348,143 , 345,139 , 341,138 , 338,138 , 335,135 , 329,136 , 325,137 , 322,140 , 324,143 , 320,144 , 316,145 , 313,143 , 309,142 , 307,146 , 304,149 , 307,152 , 307,155 , 308,159 , 305,161 , 302,158 , 297,158 , 294,156 , 293,153 , 294,150 , 296,147 , 292,145 , 289,143 , 285,141 , 285,138 , 283,135 , 280,133 , 279,130 , 279,127 , 281,124 , 284,118 , 282,115 , 283,112 , 282,108 , 282,103 , 281,99 , 283,96 , 286,96 , 290,98 , 293,101 , 297,103 , 295,107 , 290,107 , 287,106 , 289,109 , 289,112 , 292,114 , 291,111 , 296,113 , 295,109 , 298,107 , 301,106 , 301,103 , 300,100 , 303,100 , 302,104 , 305,105 , 307,102 , 311,100 , 314,99 , 318,100 , 321,101 , 322,97 , 327,97 , 331,99 , 331,96 , 331,92 , 332,88 , 333,85 , 336,84 , 338,88 , 338,92 , 338,95 , 338,99 , 339,102 , 337,105 , 333,106 , 337,108 , 339,104 , 340,101 , 344,100 , 345,103 , 345,100 , 341,98 , 339,95 , 339,91 , 341,87 , 341,84 , 341,88 , 343,91 , 342,88 , 346,86 , 349,87 , 352,89 , 351,92 , 352,89 , 348,86 , 348,83 , 354,81 , 356,84 , 357,80 , 355,76 , 360,72 , 366,70 , 369,70 , 372,69 , 372,72 , 372,68 , 377,68 , 375,65 , 378,61 , 381,62 , 381,65 , 384,67 , 389,66 , 390,70 , 391,74 , 389,76 , 386,78 , 384,81 , 381,84 , 384,83 , 388,81 , 391,81 , 391,84 , 394,81 , 398,81 , 402,84 , 406,85 , 409,88 , 412,89 , 413,92 , 416,88 , 421,89 , 424,89 , 425,86 , 435,86 , 432,87 , 435,87 , 438,87 , 439,91 , 447,91 , 451,92 , 452,95 , 453,98 , 457,96 , 461,97 , 464,98 , 466,95 , 469,96 , 473,96 , 476,97 , 478,100 , 478,103 , 478,107 , 478,109" title="Russian Federation" />
<#--<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="478,111 , 475,112 , 471,112 , 475,112 , 476,115 , 477,118 , 471,119 , 468,121 , 465,125 , 461,124 , 458,124 , 455,127 , 455,131 , 456,133 , 454,137 , 452,140 , 447,144 , 447,141 , 446,136 , 447,132 , 450,129 , 452,125 , 457,122 , 457,119 , 453,122 , 452,119 , 449,120 , 444,125 , 440,127 , 437,125 , 434,126 , 431,126 , 428,128 , 424,131 , 419,136 , 423,137 , 426,138 , 427,141 , 427,144 , 426,147 , 424,151 , 420,156 , 417,159 , 414,159 , 414,155 , 417,154 , 418,151 , 414,150 , 412,147 , 409,144 , 407,141 , 404,139 , 401,139 , 400,143 , 399,146 , 395,146 , 392,145 , 388,147 , 384,146 , 381,145 , 377,145 , 374,142 , 371,146 , 368,146 , 365,145 , 360,146 , 357,147 , 354,146 , 351,144 , 348,143 , 345,139 , 341,138 , 338,138 , 335,135 , 329,136 , 325,137 , 322,140 , 324,143 , 320,144 , 316,145 , 313,143 , 309,142 , 307,146 , 304,149 , 307,152 , 307,155 , 308,159 , 305,161 , 302,158 , 297,158 , 294,156 , 293,153 , 294,150 , 296,147 , 292,145 , 289,143 , 285,141 , 285,138 , 283,135 , 280,133 , 279,130 , 279,127 , 281,124 , 284,118 , 282,115 , 283,112 , 282,108 , 282,103 , 281,99 , 283,96 , 286,96 , 290,98 , 293,101 , 297,103 , 295,107 , 290,107 , 287,106 , 289,109 , 289,112 , 292,114 , 291,111 , 296,113 , 295,109 , 298,107 , 301,106 , 301,103 , 300,100 , 303,100 , 302,104 , 305,105 , 307,102 , 311,100 , 314,99 , 318,100 , 321,101 , 322,97 , 327,97 , 331,99 , 331,96 , 331,92 , 332,88 , 333,85 , 336,84 , 338,88 , 338,92 , 338,95 , 338,99 , 339,102 , 337,105 , 333,106 , 337,108 , 339,104 , 340,101 , 344,100 , 345,103 , 345,100 , 341,98 , 339,95 , 339,91 , 341,87 , 341,84 , 341,88 , 343,91 , 342,88 , 346,86 , 349,87 , 352,89 , 351,92 , 352,89 , 348,86 , 348,83 , 354,81 , 356,84 , 357,80 , 355,76 , 360,72 , 366,70" title="Russian Federation" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="RU" coords="9,99 , 12,101 , 15,103 , 15,106 , 18,105 , 22,108 , 19,110 , 19,113 , 15,112 , 12,109 , 9,107 , 9,111 , 9,107 , 9,103 , 9,100" title="Russian Federation" />
--><@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="SA" coords="310,186 , 315,188 , 315,191 , 311,193 , 307,193 , 304,195 , 301,194 , 298,194 , 295,191 , 294,188 , 292,185 , 289,180 , 292,178 , 294,175 , 298,176 , 302,179 , 305,179 , 309,182 , 310,186" title="Saudi Arabia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="SD" coords="283,212 , 280,211 , 278,208 , 274,205 , 273,202 , 272,199 , 274,192 , 276,189 , 284,189 , 291,189 , 292,193 , 291,196 , 290,200 , 288,203 , 286,206 , 289,210 , 286,211" title="Sudan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="SE" coords="258,127 , 260,123 , 259,120 , 259,117 , 260,114 , 262,110 , 263,107 , 265,104 , 267,101 , 269,98 , 273,101 , 274,104 , 274,107 , 272,110 , 270,114 , 267,117 , 266,120 , 267,124 , 264,126 , 267,126 , 264,128 , 265,131 , 262,134 , 259,130 , 258,127" title="Sweden" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="SJ" coords="262,51 , 262,48 , 264,51 , 264,48 , 267,48 , 268,52 , 271,54 , 268,56 , 267,62 , 266,65 , 262,64 , 265,60 , 261,61 , 264,57 , 263,54 , 262,57 , 259,56 , 258,53 , 257,49 , 261,47 , 262,51" title="Svalbard and Jan Mayen" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="SJ" coords="268,47 , 269,44 , 272,46 , 272,43 , 279,46 , 277,49 , 274,52 , 270,51 , 272,48 , 268,48" title="Svalbard and Jan Mayen" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="SO" coords="298,211 , 302,210 , 306,206 , 301,205 , 300,202 , 303,203 , 306,202 , 309,201 , 309,205 , 307,209 , 303,213 , 300,215 , 297,217 , 297,213" title="Somalia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="TD" coords="273,203 , 270,205 , 266,206 , 263,206 , 263,203 , 261,199 , 263,195 , 264,191 , 263,187 , 274,192 , 274,197 , 272,200 , 273,203" title="Chad" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="TH" coords="374,208 , 371,205 , 373,202 , 371,198 , 371,194 , 372,191 , 375,193 , 379,193 , 381,197 , 378,198 , 377,202 , 374,200 , 372,203 , 374,207" title="Thailand" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="TJ" coords="332,167 , 331,164 , 335,161 , 337,164 , 340,165 , 337,168 , 334,167" title="Tajikistan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="TM" coords="323,170 , 321,167 , 316,166 , 312,166 , 311,162 , 312,160 , 316,161 , 319,159 , 322,160 , 326,164 , 330,166 , 325,169" title="Turkmenistan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="TN" coords="258,173 , 257,177 , 254,174 , 254,170 , 256,167 , 258,170 , 258,173" title="Tunisia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="TR" coords="290,169 , 287,169 , 284,168 , 280,168 , 278,165 , 282,162 , 285,160 , 288,160 , 292,161 , 295,161 , 299,161 , 302,163 , 301,166 , 298,167 , 294,168 , 291,168" title="Turkey" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="TZ" coords="289,230 , 285,227 , 282,224 , 283,220 , 283,217 , 287,217 , 292,220 , 294,223 , 295,227 , 293,230 , 290,230" title="United Republic of Tanzania" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="UA" coords="288,153 , 287,156 , 287,153 , 284,152 , 281,154 , 282,151 , 278,149 , 273,149 , 275,145 , 279,142 , 283,143 , 285,141 , 289,143 , 292,145 , 295,146 , 295,150 , 292,151 , 289,153" title="Ukraine" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="US" coords="156,154 , 153,157 , 151,160 , 147,162 , 146,165 , 143,165 , 145,169 , 142,172 , 138,174 , 137,177 , 139,182 , 136,182 , 135,179 , 132,178 , 129,177 , 126,177 , 122,178 , 118,180 , 117,183 , 114,182 , 111,178 , 107,178 , 104,175 , 98,176 , 93,174 , 91,174 , 87,171 , 84,169 , 84,166 , 81,162 , 81,158 , 82,154 , 81,149 , 84,150 , 95,148 , 119,148 , 123,149 , 127,149 , 133,151 , 136,154 , 136,157 , 135,160 , 140,158 , 144,156 , 150,155 , 152,152 , 155,154" title="United States" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="US" coords="29,122 , 27,119 , 29,116 , 33,115 , 34,112 , 30,112 , 26,112 , 24,109 , 27,108 , 32,108 , 32,105 , 26,101 , 30,99 , 32,94 , 35,93 , 39,90 , 42,92 , 45,93 , 49,93 , 54,95 , 59,96 , 59,124 , 64,126 , 67,125 , 71,131 , 74,134 , 71,134 , 70,131 , 69,128 , 66,127 , 63,126 , 59,125 , 56,124 , 53,123 , 49,122 , 48,125 , 47,122 , 44,124 , 42,127 , 41,130 , 38,132 , 35,134 , 31,136 , 36,132 , 38,129 , 35,127 , 32,127 , 32,124 , 28,123" title="United States" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="UZ" coords="332,167 , 326,164 , 324,161 , 320,159 , 317,160 , 316,155 , 320,154 , 323,156 , 327,157 , 330,161 , 333,161 , 336,161 , 333,163 , 332,166" title="Uzbekistan" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="VE" coords="161,204 , 165,206 , 163,209 , 160,211 , 158,215 , 155,212 , 155,210 , 152,207 , 149,207 , 149,202 , 152,202 , 157,203 , 160,203" title="Bolivarian Republic of Venezuela" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="VN" coords="379,203 , 383,201 , 383,198 , 382,195 , 379,192 , 377,189 , 381,187 , 384,189 , 381,193 , 384,196 , 386,199 , 385,202 , 382,203" title="Viet Nam" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="YE" coords="299,196 , 302,195 , 306,194 , 309,193 , 312,196 , 307,198 , 304,200 , 301,201 , 299,198" title="Yemen" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ZA" coords="269,247 , 273,249 , 276,248 , 279,245 , 284,244 , 285,249 , 285,252 , 282,256 , 278,260 , 274,260 , 270,261 , 266,258 , 266,255 , 265,252 , 269,252 , 269,247" title="South Africa" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ZM" coords="274,229 , 278,231 , 282,232 , 281,229 , 283,226 , 286,228 , 287,231 , 283,235 , 280,237 , 276,238 , 272,236 , 272,232" title="Zambia" />
<@renderMap forceAddSchemeHostAndPort=forceAddSchemeHostAndPort code="ZW" coords="283,235 , 286,237 , 286,240 , 285,243 , 281,244 , 277,240 , 280,237 , 283,235" title="Zimbabwe" />


</map>
 </div> 

</#macro>

<#macro renderMap code coords title forceAddSchemeHostAndPort=false>
 <#assign val=codes[code]?default(0)/>
 <#assign logCode= code+'_'/>
 <#if (val > 0)>

    <#if countryLogTotal == 0>
        <#assign countryLogTotal = 0 />
    </#if>

    <#assign percent = ((codes[logCode]/countryLogTotal) * 100)?floor />
    <#assign color = "#ffffff" />
    
     <#if (percent < 9) >
        <#assign color = settings.mapColors[1] />
     <#elseif (percent > 8 && percent < 17)>
        <#assign color = settings.mapColors[2] />
     <#elseif (percent > 16 && percent < 32)>
        <#assign color = settings.mapColors[3]/>
     <#elseif (percent > 31 && percent < 46)>
        <#assign color = settings.mapColors[4] />
     <#elseif (percent > 45 && percent < 61)>
        <#assign color = settings.mapColors[5] />
     <#elseif (percent > 60 && percent < 76)>
        <#assign color = settings.mapColors[6] />
     <#elseif (percent > 76 && percent < 85)>
        <#assign color = settings.mapColors[7] />
    <#else>
        <#assign color = settings.mapColors[8] />
    </#if>
<!-- [${code} : ${percent} ] -->
<#assign term>geographicKeywords="${code} (ISO Country Code)"</#assign>
     <area coords="${coords}" shape="poly" title="${title} (${val})" alt="${title} (${val})" target="_top"  href='<@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="/search/results?${term}"/>' iso="${code}"
     class="{alwaysOn:true,strokeColor:'666666',strokeWidth:'.5',fillColor:'${color}',fillOpacity:1}" ></area>
 </#if>
</#macro>

<#macro cartouche persistable useDocumentType=false>
    <#local cartouchePart><@upperPersistableTypeLabel persistable /></#local>
    <span class="cartouche"><i class="icon-${cartouchePart?replace(" ","")?lower_case}"></i> 
        <#if (persistable.status)?? && !persistable.active>
        ${persistable.status} <#t>
    </#if> 
    <@upperPersistableTypeLabel persistable />
     </span>
    <#nested />
</#macro>

<#-- FIXME: freemarker docs advise against setting locals w/ macro contents -->
<#macro upperPersistableTypeLabel persistable>
    <#if persistable.resourceType?has_content><#t>
        ${persistable.resourceType?replace("_", " ")?upper_case} <#t>
    <#elseif persistable.type?has_content><#t>
        COLLECTION<#t>
    <#else> <#t>
        PERSISTABLE<#t>
    </#if>
</#macro>


<#macro loginMenu showMenu=false>
 <#if showMenu>
    <ul class="subnav-rht hidden-phone hidden-tablet">
 </#if>
        <#if !(authenticatedUser??) > 
                <li><a href="<@s.url value="/account/new" />" class="button">Sign Up</a></li>
                <li><a href="<@s.url value="/login" />" class="button">Log In</a></li>
        <#else>
                <li><a href="<@s.url value="/logout" />" class="button">Logout</a></li>
        </#if>                        
 <#if showMenu>
    </ul>
 </#if>
</#macro>

<#macro resourceUsageInfo>
<#if uploadedResourceAccessStatistic?has_content && totalResourceAccessStatistic?has_content>
        <table class="table tableFormat">
            <tr>
                <#if !persistable?has_content || !persistable.resourceType?has_content || persistable.resourceType == 'PROJECT' ><th>Total # of Resource</th></#if>
                <th>Total # of Files</th>
                <th>Total Space (Uploaded Only)</th>
                <th>Total Space (Used by System)</th>
            </tr>
            <tr>
                <#if !persistable?has_content || !persistable.resourceType?has_content || persistable.resourceType == 'PROJECT' ><td>${uploadedResourceAccessStatistic.countResources}</td></#if>
                <td>${uploadedResourceAccessStatistic.countFiles}</td>
                <td><@convertFileSize uploadedResourceAccessStatistic.totalSpace /></td>
                <td><@convertFileSize totalResourceAccessStatistic.totalSpace /></td>
            </tr>
        </table>
    
</#if>
</#macro>

<#macro listAddresses person=person  choiceField="" addressId=-1>

<div class="row">
	<#list person.addresses  as address>
	    <div class="span3">
	    <#assign label = ""/>
	    <#if address.type?has_content>
	    <#assign label = address.type.label>
	    </#if>
	    	<#if choiceField?has_content>
        <label class="radio inline">
        <input type="radio" name="invoice.address.id" label="${label}" 
        value="${address.id}"  <#if address.id==addressId || (!addressId?has_content || addressId == -1) && address_index==0>checked=checked</#if>/>
	
	</#if>
	    
	    <@printAddress  address=address creatorId=person.id modifiable=true showLabel=false >
	        <b><#if address.type?has_content>${address.type.label!""}</#if></b>
	        </label><br/>
	    </@printAddress>
	    </div>
	</#list>
    <div class="span3">
    <#assign retUrl><@s.url includeParams="all"/></#assign>
	    <a class="button btn btn-primary submitButton" href="/entity/person/${person.id?c}/address?returnUrl=${retUrl?url}">Add Address</a>
    </div>
</div>
</#macro>

<#macro printAddress address=address creatorId=-1 creatorType='person'  modifiable=false deletable=false showLabel=true>
        <p>
<#if address.type?has_content && showLabel><b>${address.type.label!""}</b><br></#if>
           ${address.street1}<br/>
           ${address.street2}<br/>
           ${address.city}, ${address.state}, ${address.postal}<br/>
           ${address.country}<#if modifiable><br/>
           <a href="<@s.url value="/entity/${creatorType}/${creatorId?c}/address?addressId=${address.id}"/>">edit</a>
           </#if><#if deletable && modifiable> |</#if> 
           <#if deletable>
               <a href="/entity/${creatorType}/${creatorId?c}/delete-address?addressId=${address.id}">delete</a>
           </#if>
        </p>
</#macro>


<#macro checkedif arg1 arg2><#t>
<@valif "checked='checked'" arg1 arg2 />
</#macro>

<#macro selectedif arg1 arg2>
<@valif "selected='selected'" arg1 arg2 />
</#macro>

<#macro valif val arg1 arg2><#t>
<#if arg1=arg2>${val}</#if><#t>
</#macro>

<#macro boolfield name label id  value labelPosition="left" type="checkbox" labelTrue="Yes" labelFalse="No" cssClass="">
    <@boolfieldCheckbox name label id  value labelPosition cssClass />
</#macro>

<#macro boolfieldCheckbox name label id value labelPosition cssClass>
<#if value?? && value?string == 'true'>
    <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" 
        checked="checked"/>
<#else>
    <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" />
</#if>
</#macro>

<#macro boolfieldRadio name label id value labelPosition labelTrue labelFalse>
    <label>${label}</label>
    <input type="radio" name="${name}" id="${id}-true" value="true"  <@checkedif true value />  />
    <label for="${id}-true" class="datatable-cell-unstyled"> ${labelTrue}</label>
    <#if (labelPosition=="top")><br />
    <input type="radio" name="${name}" id="${id}-false" value="false" <@checkedif false value />   />
    <label for="${id}-false" class="datatable-cell-unstyled"> ${labelFalse}</label>
    <#else>
    <input type="radio" name="${name}" id="${id}-false" value="false"   />
    <label for="${id}-false" class="datatable-cell-unstyled"> ${labelFalse}</label>
    </#if>
</#macro>

<#macro boolfieldSelect name label id value labelPosition labelTrue labelFalse>
    <label>${label}</label>
    <select id="${id}" name="${name}">
    <#if (labelPosition=="top")><br /></#if>
        <option id="${id}-true" value="true" <@selectedif true value/> />${labelTrue}</option>
        <option id="${id}-false" value="false" <@selectedif false value/> />${labelFalse}</option>
    </select>
</#macro>


<#macro combobox name target autocompleteIdElement placeholder value cssClass autocompleteParentElement="" label="" bootstrapControl=true id="" addNewLink="">
            <#if bootstrapControl>
            <div class="control-group">
                <label class="control-label">${label}</label>
                <div class="controls">
            </#if>
                    <div class="input-append">
                        <@s.textfield theme="simple" name="${name}"  target="${target}"
                         label="${label}"
                         autocompleteParentElement="${autocompleteParentElement}"
                         autocompleteIdElement="${autocompleteIdElement}"
                         placeholder="${placeholder}"
                        value="${value}" cssClass="${cssClass}" />
                        <button type="button" class="btn show-all"><i class="icon-chevron-down"></i></button>         
                        <#if addNewLink?has_content>           
                        <a href="${addNewLink}"  onClick="setAdhocTarget(this);" class="btn show-all" target="_blank">add new</a>
                        </#if>                    
                    </div>
            <#if bootstrapControl>
                </div>
            </div>
            </#if>
</#macro>

<#--TODO: generify this so that you can choose radios or checkboxes -->
<#-- FIXME: not finished!!! -->
<#macro inlineCheckboxlist  name list id="inlineCheckboxList" listValue='label' label="">
    <div class="control-group" >
        <#if label != "" ><label class="control-label">${label}</label></#if>
        <div class="controls">
            <#list list as listItem>
                <label class="checkbox inline">
                    <@s.checkbox theme="simple" name="${name}" id="${id}_${listItem_index}" value="" />
                </label>
            </#list>
        </div>
        
        
    </div>
</#macro>

<#--//  TODO: actually implement this-->
<#function fileSize sizeInBytes>
    <#return "${sizeInByptes} bytes" />    
</#function>



</#escape>




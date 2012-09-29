<#escape _untrusted as _untrusted?html>
<#import "/${themeDir}/settings.ftl" as settings>
<#-- 
$Id:Exp$
Common macros used in multiple contexts
-->
<#macro convertFileSize filesize=0>
<#assign mb = 1048576 />
<#assign kb = 1024 />
<#if (filesize > mb)>
${(filesize / mb)?string(",##0.00")}mb
<#elseif (filesize > kb)>
${(filesize / kb)?string(",##0.00")}kb
<#else>
${filesize?string(",##0.00")}b
</#if>
</#macro>

<#macro resourceCollectionsRights effectiveResourceCollections_>
    <#if !effectiveResourceCollections_.empty>
    <h4>Access Permissions for this Resource</h4>
    <#nested />
    <table class="tableFormat zebracolors">
    <thead><th>Collection</th><th>User</th><th>Permission</th></thead>
    <#list effectiveResourceCollections_ as collection_>
      <#if collection_??>
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
            ${user.user.properName}
          </td>
          <td>
           ${user.generalPermission.label}
           </td>
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
    <div id="${name}" style="width:${width}px;height:${height}px"></div>
    
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
//					tilt:.3,
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
<#assign totalItems = resourceCacheObjects?size />
   <#list resourceCacheObjects?sort_by("key") as key>
	  <#if (key.count == 0) >
		  <#assign totalItems = totalItems - 1/>
      </#if>
    </#list>

	<#if (totalItems < 1)>
		<#assign totalItems = 1 />
	</#if>
	
<#assign barWidth = (graphWidth  / (totalItems) -6)/>
<div class="barGraph" style="width:${graphWidth?c}px;height:${graphHeight?c}px;" >
    <p style="margin-right: auto;margin-left: auto;text-align:center;margin-top:10px;margin-bottom:0px"><b>${graphLabel}</b></p>
   <table style="width:${graphWidth -5}px;height:${graphHeight - 15}px;">
  <tr>
  <#assign resourceTypeCount = 0>
   <#list resourceCacheObjects?sort_by("key") as key>
	  <#if (key.count > 0) >
		<#assign calulatedValue= key.key >
		<#if calulatedValue?is_number>
			<#assign calulatedValue=calulatedValue?c?string/>
		</#if>
        <#assign resourceTypeCount = key.logCount + resourceTypeCount >
        <td>
	      	<a href="<@s.url value="/search/results?${searchKey}=${calulatedValue}"/>">
	      	<div class="label">${key.count}</div><div class="bar" id="${key.cssId}"></div></a>
        </td>
	  </#if>
    </#list>
  </tr>
  <tr>
   <#list resourceCacheObjects?sort_by("key") as key>
	  <#if (key.count > 0) >
      <td><div class="label">${key.label}</div></td>
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
        <#assign resourceTypeCount = 1>
    </#if>

   <#list resourceCacheObjects?sort_by("key") as key>
    <#if (key.count > 0)>
	   <#assign color_= settings.barColors[0] />
	    <#if rotateColors>
	       <#assign color_=settings.barColors[key.resourceType.order - 1] />
	    </#if>
	      #${key.cssId} {background-color: ${color_}; height: ${(2 * graphHeight * (key.logCount / resourceTypeCount))?floor}px }
    </#if>
   </#list>

   

.bar {width:${barWidth?c}px;;min-width:${minWidth?c}px }

div.label {
    top: 0;
    z-index: 1000;
    overflow: visible;
    left: 0px;
    position: relative;
    display: block;
    text-align: center;
    font-size: smaller;
    line-height: 1.2em;
    vertical-align: top !important;
    height: 1.5em;
 }
 td > div.label {
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
<#assign totalItems = resourceCacheObjects?size />
<#if (totalItems < 1)>
	<#assign totalItems = 1 />
</#if>




<#assign barWidth = (graphWidth  / (totalItems) - 10 )/>
<div class="barGraph" style="width:${graphWidth?c}px;height:${graphHeight?c}px;display:block;position:relative;clear:none;left:0px"> 
<script>
  <#assign resourceTypeCount = 0>
   var data = [];
   var ticks = [];
   <#list resourceCacheObjects?sort_by("key") as key>
	<#assign calulatedValue= key.key >
	<#if calulatedValue?is_number>
		<#assign calulatedValue=calulatedValue?c?string/>
	</#if>
	${key.label?is_number?string}
	<#if ((min?is_number && key.label?number > min ) 
		&& (max?is_number && key.label?number < max ) && key.count > minDataVal )>
      <#assign resourceTypeCount = key.logCount + resourceTypeCount >
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
	      },
	    },
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

<#macro worldMap>

<script type="text/javascript">
$(function() {
    $('.map').maphilight({
      fade: true, 
      groupBy:"alt",
      strokeColor:'#ffffff'
    });
 
  $("map").delegate('area', 'mouseover',function(e) {
        $('[iso='+$(this).attr('iso')+']').each(function(index,val) {
            hightlight(true,val);
        });
   });

  $("map").delegate('area', 'mouseout',function(e) {
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
<div style="height:353px;border:1px solid #CCC;background-color:#fff;width:550px;padding-top:5px">
<img class=map src="<@s.url value="/images/world_550_2.png" />" width=545 height=345 usemap="#world" > 
 <div id="map_legend">
  <div><span class='legendText'>none</span> 
    <#list settings.mapColors as color>
      <span class="legendBox" style="background-color:#${color}"></span>    
    </#list>
<span class='legendText'>${max}+</span></div>
 </div>


<map name=world> 

<!--
<area href="#RU" title="Russian Federation" shape="poly" coords="543,103 , 539,104 , 534,104 , 539,104 , 541,107 , 541,111 , 538,109 , 535,111 , 531,113 , 528,116 , 525,115 , 522,116 , 519,116 , 517,119 , 517,123 , 516,127 , 513,128 , 511,132 , 508,133 , 507,127 , 507,123 , 510,120 , 514,117 , 519,114 , 519,111 , 514,114 , 510,112 , 507,114 , 504,117 , 500,118 , 497,117 , 494,117 , 489,117 , 486,119 , 481,122 , 476,127 , 479,128 , 482,128 , 485,130 , 484,133 , 484,137 , 482,140 , 477,145 , 474,148 , 471,147 , 471,144 , 474,143 , 476,140 , 472,140 , 469,138 , 465,137 , 464,133 , 462,130 , 459,129 , 455,130 , 454,133 , 452,136 , 448,136 , 443,137 , 440,137 , 437,137 , 432,135 , 429,135 , 424,133 , 421,134 , 418,136 , 414,135 , 409,136 , 405,137 , 402,137 , 399,134 , 396,133 , 392,130 , 387,129 , 384,128 , 380,126 , 377,126 , 373,127 , 370,128 , 366,129 , 367,131 , 366,135 , 363,134 , 360,134 , 357,135 , 354,133 , 349,134 , 346,135 , 345,138 , 349,141 , 348,145 , 350,148 , 346,150 , 343,148 , 340,147 , 336,146 , 333,145 , 332,142 , 335,139 , 333,136 , 330,135 , 327,133 , 323,132 , 324,129 , 322,126 , 319,125 , 317,121 , 317,118 , 321,116 , 317,115 , 323,110 , 321,107 , 320,104 , 320,100 , 321,96 , 319,92 , 322,90 , 325,90 , 329,91 , 333,94 , 337,96 , 336,100 , 329,100 , 325,99 , 328,102 , 328,105 , 332,107 , 330,104 , 333,104 , 336,105 , 338,101 , 341,100 , 341,97 , 342,94 , 345,95 , 344,99 , 349,96 , 353,93 , 357,92 , 361,93 , 364,93 , 366,89 , 371,90 , 375,92 , 378,92 , 375,90 , 376,86 , 378,82 , 378,79 , 382,78 , 383,82 , 384,86 , 384,89 , 385,94 , 383,97 , 380,99 , 383,100 , 386,97 , 387,93 , 391,93 , 387,92 , 385,88 , 385,84 , 387,81 , 390,85 , 393,85 , 389,83 , 392,80 , 396,81 , 400,83 , 398,86 , 400,83 , 396,80 , 395,77 , 402,75 , 406,74 , 403,71 , 406,69 , 415,66" iso="RU" 
<area href="#RU" title="Russian Federation" shape="poly" coords="415,66 , 419,65 , 422,64 , 423,67 , 422,63 , 426,63 , 429,57 , 433,57 , 432,61 , 435,61 , 441,62 , 443,65 , 444,69 , 441,71 , 436,75 , 433,77 , 437,77 , 440,75 , 444,75 , 444,79 , 447,75 , 452,76 , 457,78 , 461,79 , 464,82 , 467,83 , 469,86 , 473,82 , 478,83 , 481,84 , 484,81 , 494,80 , 490,81 , 494,81 , 491,82 , 494,81 , 498,81 , 498,85 , 501,85 , 507,85 , 512,86 , 513,89 , 515,92 , 519,89 , 523,90 , 527,92 , 529,88 , 532,89 , 537,89 , 541,91 , 543,95 , 543,99 , 543,103" iso="RU" alt="Russian Federation" 
-->

<!-- python svg2imagemap.py world.svg 550 420 -->
<@renderMap code="AF" coords="378,190 , 380,190 , 383,189 , 385,188 , 385,190 , 388,189 , 391,190 , 387,191 , 385,192 , 385,195 , 383,197 , 382,199 , 378,200 , 378,202 , 374,203 , 372,203 , 370,202 , 371,200 , 369,197 , 370,195 , 370,193 , 373,193 , 375,192 , 377,189" title="Afghanistan" />
<@renderMap code="AO" coords="296,269 , 296,267 , 297,264 , 299,262 , 298,258 , 298,255 , 304,254 , 305,257 , 308,256 , 311,255 , 311,258 , 312,261 , 314,261 , 314,263 , 312,263 , 312,268 , 314,270 , 310,271 , 307,270 , 300,270 , 298,269" title="Angola" />
<@renderMap code="AR" coords="172,313 , 171,310 , 171,307 , 172,305 , 172,302 , 173,299 , 173,297 , 174,295 , 174,292 , 174,290 , 174,288 , 175,285 , 176,283 , 177,281 , 179,278 , 182,277 , 185,276 , 188,279 , 191,281 , 192,283 , 194,285 , 197,284 , 198,282 , 198,284 , 195,286 , 193,289 , 192,291 , 192,294 , 191,296 , 193,299 , 193,302 , 187,304 , 185,306 , 182,307 , 182,309 , 184,310 , 182,311 , 181,314 , 179,316 , 178,318 , 180,319 , 178,323 , 177,325 , 175,328 , 177,330 , 174,330 , 171,330 , 171,327 , 169,324 , 170,322 , 171,320 , 172,317 , 172,315 , 172,313" title="Argentina" />
<@renderMap code="AT" coords="294,172 , 298,170 , 299,168 , 303,168 , 304,170 , 301,172 , 297,172 , 295,172" title="Austria" />
<@renderMap code="AU" coords="508,286 , 507,291 , 505,294 , 504,296 , 503,299 , 503,301 , 499,302 , 497,303 , 495,303 , 491,303 , 488,302 , 488,300 , 487,298 , 485,295 , 484,293 , 482,295 , 480,295 , 479,293 , 476,292 , 471,291 , 469,292 , 465,293 , 463,295 , 458,295 , 456,297 , 453,297 , 451,296 , 451,293 , 451,290 , 450,288 , 449,284 , 448,282 , 448,279 , 449,276 , 451,276 , 454,274 , 457,273 , 460,273 , 461,271 , 462,268 , 464,268 , 466,266 , 468,265 , 470,266 , 473,266 , 473,264 , 475,262 , 477,261 , 480,262 , 482,262 , 482,265 , 483,268 , 485,269 , 488,270 , 490,268 , 490,265 , 490,263 , 491,260 , 492,262 , 494,265 , 496,266 , 496,268 , 497,271 , 499,273 , 501,274 , 502,277 , 505,279 , 507,281 , 507,284" title="Australia" />
<@renderMap code="AZ" coords="348,187 , 347,185 , 346,183 , 348,183 , 353,180 , 355,183 , 356,186 , 353,188 , 351,187 , 348,187" title="Azerbaijan" />
<@renderMap code="BF" coords="270,231 , 271,229 , 273,226 , 275,225 , 277,225 , 279,224 , 281,227 , 282,229 , 280,230 , 278,230 , 275,230 , 275,232 , 272,232 , 270,231" title="Burkina Faso" />
<@renderMap code="BG" coords="321,178 , 320,180 , 318,182 , 315,182 , 312,181 , 312,179 , 315,178 , 318,178 , 321,178" title="Bulgaria" />
<@renderMap code="BO" coords="175,270 , 175,268 , 176,265 , 176,263 , 175,260 , 177,260 , 179,259 , 182,262 , 185,263 , 188,264 , 189,266 , 192,268 , 193,271 , 192,273 , 189,272 , 186,273 , 186,275 , 183,276 , 180,277 , 178,278 , 177,275 , 176,273 , 176,271" title="Plurinational State of Bolivia" />
<@renderMap code="BR" coords="212,248 , 212,250 , 214,249 , 216,249 , 219,249 , 221,250 , 223,252 , 226,253 , 227,255 , 226,258 , 223,260 , 222,263 , 220,266 , 220,269 , 219,273 , 218,275 , 216,278 , 213,278 , 211,279 , 207,281 , 206,284 , 206,287 , 203,291 , 201,292 , 203,289 , 201,291 , 200,294 , 198,292 , 196,290 , 193,289 , 195,286 , 198,284 , 198,282 , 198,279 , 196,277 , 192,277 , 192,274 , 193,271 , 191,269 , 189,268 , 188,265 , 186,264 , 183,263 , 181,259 , 178,260 , 175,260 , 173,258 , 171,259 , 169,257 , 169,255 , 170,252 , 173,251 , 175,247 , 174,245 , 177,243 , 180,244 , 183,243 , 183,240 , 185,240 , 187,239 , 189,238 , 189,240 , 190,243 , 192,243 , 194,243 , 197,242 , 200,242 , 202,240 , 203,242 , 202,246 , 200,248 , 203,247 , 203,249 , 205,248 , 207,246 , 210,247 , 212,248" title="Brazil" />
<@renderMap code="BW" coords="309,281 , 309,277 , 310,271 , 314,271 , 316,270 , 318,273 , 320,274 , 322,276 , 320,279 , 317,281 , 314,282 , 311,284 , 309,281" title="Botswana" />
<@renderMap code="BY" coords="314,162 , 314,159 , 314,157 , 317,156 , 319,153 , 321,151 , 324,152 , 325,155 , 326,157 , 326,159 , 324,162 , 322,162 , 320,162 , 316,161 , 314,162" title="Belarus" />
<@renderMap code="CA" coords="106,92 , 101,97 , 99,99 , 99,101 , 95,104 , 94,101 , 90,100 , 92,97 , 92,95 , 94,91 , 92,88 , 97,87 , 100,89 , 103,89 , 106,92" title="Canada" />
<@renderMap code="CA" coords="114,82 , 114,80 , 114,78 , 116,75 , 117,77 , 117,79 , 119,81 , 121,82 , 120,85 , 118,84 , 112,88 , 109,88 , 110,85 , 113,84 , 111,84 , 109,85 , 109,83 , 107,85 , 105,85 , 103,84 , 105,82 , 107,80 , 104,80 , 107,79 , 110,78 , 111,81 , 114,82" title="Canada" />
<@renderMap code="CA" coords="114,97 , 113,95 , 116,97 , 117,100 , 118,97 , 117,94 , 119,94 , 122,99 , 123,101 , 123,104 , 125,106 , 127,107 , 128,110 , 126,108 , 125,112 , 127,111 , 124,113 , 122,113 , 119,110 , 117,112 , 110,114 , 109,112 , 105,110 , 103,108 , 108,107 , 111,107 , 109,105 , 105,106 , 103,106 , 107,102 , 102,102 , 101,100 , 102,97 , 108,93 , 109,96 , 113,96 , 112,98 , 114,97" title="Canada" />
<@renderMap code="CA" coords="124,62 , 121,60 , 121,58 , 125,58 , 125,61 , 128,61 , 129,63 , 130,65 , 131,68 , 128,68 , 128,66 , 125,66 , 123,65 , 124,63" title="Canada" />
<@renderMap code="CA" coords="126,81 , 126,79 , 127,77 , 129,80 , 130,78 , 129,76 , 131,77 , 133,79 , 132,81 , 133,83 , 132,85 , 129,85 , 128,83 , 131,81 , 126,82" title="Canada" />
<@renderMap code="CA" coords="129,93 , 128,91 , 131,91 , 133,91 , 134,93 , 132,96 , 134,96 , 134,98 , 134,100 , 132,101 , 130,101 , 127,98 , 125,96 , 127,96 , 129,94 , 127,93" title="Canada" />
<@renderMap code="CA" coords="136,93 , 136,91 , 139,89 , 144,91 , 141,96 , 138,96 , 138,99 , 136,96 , 136,93" title="Canada" />
<@renderMap code="CA" coords="141,59 , 140,57 , 138,57 , 136,57 , 138,54 , 136,56 , 135,53 , 138,53 , 137,51 , 134,50 , 139,48 , 136,46 , 140,44 , 138,41 , 141,42 , 143,47 , 146,49 , 146,52 , 146,49 , 147,52 , 148,54 , 148,56 , 150,56 , 152,58 , 149,61 , 148,63 , 148,60 , 147,62 , 147,64 , 146,66 , 144,63 , 145,66 , 141,66 , 142,63 , 139,63 , 138,60 , 143,59 , 141,59" title="Canada" />
<@renderMap code="CA" coords="149,83 , 153,81 , 157,81 , 160,83 , 160,86 , 155,87 , 153,88 , 151,88 , 147,88 , 147,86 , 143,87 , 141,84 , 141,82 , 140,77 , 136,78 , 136,76 , 134,75 , 139,74 , 139,77 , 143,76 , 146,78 , 142,78 , 144,79 , 145,82 , 147,82 , 150,83" title="Canada" />
<@renderMap code="CA" coords="150,127 , 150,124 , 152,125 , 154,126 , 157,128 , 159,130 , 156,130 , 153,131 , 151,130 , 148,131 , 150,128" title="Canada" />
<@renderMap code="CA" coords="171,136 , 174,138 , 175,141 , 175,143 , 177,144 , 175,147 , 177,145 , 179,145 , 181,143 , 182,140 , 183,142 , 184,144 , 184,147 , 186,147 , 187,150 , 188,152 , 188,154 , 190,153 , 193,155 , 190,156 , 192,156 , 194,157 , 195,159 , 194,162 , 191,163 , 189,165 , 187,165 , 179,165 , 176,167 , 173,169 , 172,172 , 175,170 , 180,167 , 182,167 , 181,170 , 179,170 , 182,170 , 182,172 , 184,174 , 186,174 , 184,176 , 181,179 , 180,176 , 182,175 , 180,175 , 178,175 , 177,172 , 175,171 , 174,173 , 172,176 , 167,176 , 165,177 , 161,178 , 155,182 , 156,178 , 155,175 , 153,174 , 147,169 , 143,169 , 141,169 , 139,169 , 108,168 , 95,168 , 93,166 , 92,163 , 90,164 , 88,163 , 89,160 , 87,158 , 87,156 , 84,157 , 84,154 , 84,152 , 82,150 , 79,145 , 77,143 , 75,142 , 71,141 , 68,140 , 68,110 , 71,110 , 74,112 , 77,113 , 78,111 , 81,110 , 83,108 , 85,108 , 83,110 , 80,111 , 80,113 , 81,111 , 83,110 , 86,108 , 88,107 , 90,110 , 92,109 , 94,111 , 98,110 , 104,113 , 107,113 , 108,115 , 106,116 , 111,117 , 113,117 , 115,117 , 117,118 , 118,122 , 118,119 , 117,117 , 119,115 , 117,115 , 120,112 , 122,115 , 124,115 , 129,116 , 131,117 , 133,117 , 131,115 , 133,114 , 135,117 , 136,119 , 136,117 , 139,114 , 138,112 , 137,109 , 135,109 , 134,107 , 134,105 , 135,103 , 137,100 , 140,102 , 140,105 , 141,107 , 140,109 , 142,110 , 144,113 , 144,115 , 145,112 , 147,112 , 148,115 , 147,117 , 149,118 , 150,116 , 151,113 , 151,110 , 156,110 , 156,113 , 156,115 , 157,118 , 155,121 , 153,121 , 151,121 , 149,125 , 146,124 , 142,123 , 144,124 , 149,125 , 147,129 , 145,129 , 141,130 , 139,129 , 141,130 , 143,132 , 141,133 , 139,135 , 137,140 , 137,143 , 138,146 , 141,148 , 143,148 , 146,149 , 148,152 , 151,152 , 156,154 , 156,157 , 157,160 , 159,162 , 161,162 , 161,160 , 161,158 , 160,155 , 163,153 , 164,151 , 164,149 , 163,146 , 161,144 , 163,142 , 163,140 , 163,137 , 162,135 , 166,135 , 169,134 , 171,136" title="Canada" />
<@renderMap code="CA" coords="174,50 , 172,52 , 172,54 , 170,54 , 167,54 , 169,55 , 167,56 , 163,56 , 168,59 , 165,59 , 162,59 , 165,60 , 161,60 , 162,62 , 164,60 , 167,62 , 167,64 , 164,64 , 166,65 , 164,66 , 162,67 , 163,70 , 158,72 , 156,69 , 157,72 , 160,72 , 161,75 , 162,77 , 158,79 , 156,76 , 154,75 , 154,77 , 152,76 , 150,77 , 147,77 , 147,75 , 146,77 , 145,75 , 149,73 , 148,70 , 150,69 , 153,72 , 156,67 , 154,70 , 151,69 , 153,67 , 152,65 , 150,67 , 150,65 , 148,66 , 148,64 , 149,62 , 152,61 , 156,63 , 157,61 , 152,60 , 154,60 , 152,56 , 150,55 , 149,52 , 154,51 , 156,54 , 157,56 , 160,55 , 157,55 , 155,50 , 162,48 , 159,48 , 164,46 , 161,45 , 162,43 , 164,40 , 161,43 , 160,46 , 157,48 , 155,48 , 157,46 , 154,47 , 149,48 , 151,45 , 156,43 , 151,44 , 148,48 , 145,45 , 149,44 , 152,42 , 145,44 , 146,42 , 146,40 , 144,41 , 145,39 , 142,40 , 144,36 , 147,34 , 149,36 , 151,36 , 149,33 , 151,32 , 152,30 , 154,32 , 155,34 , 155,32 , 160,37 , 155,31 , 156,29 , 158,29 , 157,26 , 160,28 , 159,26 , 164,26 , 165,29 , 163,25 , 165,24 , 168,24 , 170,28 , 169,25 , 170,23 , 173,26 , 174,23 , 180,25 , 176,28 , 180,26 , 183,27 , 185,29 , 187,30 , 187,33 , 183,38 , 180,39 , 177,39 , 179,40 , 174,43 , 180,40 , 178,45 , 175,50" title="Canada" />
<@renderMap code="CA" coords="183,118 , 184,121 , 186,120 , 187,122 , 185,122 , 184,124 , 184,127 , 182,125 , 181,123 , 179,122 , 177,122 , 177,125 , 179,126 , 181,128 , 182,130 , 182,133 , 180,132 , 177,130 , 178,132 , 180,134 , 176,135 , 174,133 , 172,132 , 171,130 , 169,129 , 167,127 , 164,128 , 162,128 , 163,126 , 165,126 , 168,124 , 168,122 , 170,120 , 170,117 , 169,114 , 167,113 , 164,113 , 166,111 , 163,110 , 163,107 , 161,106 , 161,108 , 157,108 , 150,108 , 150,106 , 147,107 , 145,104 , 147,104 , 145,103 , 144,100 , 144,98 , 145,95 , 147,92 , 152,91 , 149,96 , 150,99 , 152,103 , 149,104 , 152,103 , 152,101 , 150,100 , 153,100 , 153,98 , 151,96 , 153,96 , 151,95 , 154,95 , 152,94 , 154,94 , 155,92 , 157,93 , 158,95 , 157,98 , 160,99 , 162,100 , 164,99 , 161,98 , 164,96 , 166,97 , 165,100 , 168,99 , 166,102 , 168,101 , 168,103 , 170,101 , 172,103 , 170,105 , 173,104 , 171,106 , 174,104 , 177,106 , 175,107 , 174,110 , 177,108 , 174,110 , 176,110 , 179,111 , 177,111 , 177,113 , 179,114 , 179,116 , 181,117 , 183,118" title="Canada" />
<@renderMap code="CA" coords="195,163 , 194,166 , 196,166 , 198,166 , 198,169 , 199,171 , 196,172 , 195,170 , 192,170 , 190,170 , 192,167 , 193,164 , 195,162" title="Canada" />
<@renderMap code="CA" coords="97,76 , 101,72 , 104,72 , 106,72 , 106,74 , 104,76 , 102,75 , 102,77 , 100,80 , 98,78 , 96,80 , 96,78" title="Canada" />
<@renderMap code="CD" coords="323,251 , 323,254 , 325,257 , 322,257 , 321,260 , 322,262 , 320,262 , 318,262 , 315,261 , 312,261 , 311,258 , 311,256 , 308,255 , 305,257 , 304,254 , 298,253 , 300,251 , 302,251 , 303,248 , 305,246 , 305,244 , 307,241 , 307,239 , 309,239 , 312,240 , 315,238 , 318,238 , 321,239 , 324,240 , 325,242 , 323,244 , 323,247 , 322,250" title="The Democratic Republic of the Congo" />
<@renderMap code="CF" coords="307,240 , 304,240 , 301,240 , 301,238 , 302,235 , 305,234 , 307,233 , 311,231 , 314,232 , 315,234 , 318,236 , 320,238 , 317,238 , 314,239 , 309,239 , 307,239" title="Central African Republic" />
<@renderMap code="CG" coords="307,240 , 305,244 , 305,246 , 303,248 , 302,251 , 300,252 , 298,252 , 295,251 , 296,248 , 298,248 , 300,248 , 299,246 , 300,243 , 302,243 , 304,240 , 306,240" title="Congo" />
<@renderMap code="CI" coords="275,232 , 275,234 , 274,237 , 271,238 , 268,239 , 268,237 , 266,235 , 267,233 , 269,231 , 271,231 , 273,231" title="Cote d'Ivoire" />
<@renderMap code="CL" coords="168,317 , 169,315 , 170,313 , 170,310 , 171,308 , 169,309 , 169,307 , 169,305 , 169,302 , 170,300 , 171,297 , 172,295 , 172,290 , 172,287 , 173,284 , 173,282 , 174,279 , 174,276 , 173,271 , 176,271 , 176,273 , 177,275 , 177,278 , 177,280 , 177,283 , 175,285 , 174,288 , 174,290 , 174,292 , 174,294 , 174,297 , 173,299 , 172,302 , 172,304 , 171,306 , 171,309 , 171,311 , 171,314 , 171,316 , 172,318 , 170,321 , 170,323 , 169,325 , 171,328 , 174,330 , 177,330 , 173,331 , 173,334 , 171,333 , 170,331 , 169,328 , 169,326 , 168,324 , 168,322 , 169,320 , 167,318" title="Chile" />
<@renderMap code="CM" coords="299,242 , 296,242 , 294,242 , 293,240 , 292,237 , 294,236 , 297,235 , 298,233 , 299,231 , 301,228 , 302,231 , 300,231 , 301,234 , 301,237 , 301,239 , 303,241 , 300,242" title="Cameroon" />
<@renderMap code="CN" coords="412,206 , 409,205 , 407,205 , 404,203 , 401,202 , 399,201 , 396,200 , 396,198 , 396,195 , 395,193 , 393,192 , 391,190 , 391,188 , 389,186 , 392,184 , 395,183 , 399,181 , 399,179 , 399,176 , 401,175 , 403,171 , 405,172 , 407,169 , 409,167 , 412,170 , 415,172 , 414,175 , 419,176 , 421,177 , 423,180 , 427,180 , 429,180 , 433,182 , 436,182 , 439,180 , 442,180 , 445,179 , 445,177 , 447,176 , 450,175 , 452,174 , 455,173 , 458,172 , 457,170 , 455,170 , 452,170 , 453,166 , 456,166 , 458,162 , 459,160 , 463,158 , 466,159 , 468,161 , 468,163 , 469,165 , 471,166 , 473,168 , 474,170 , 477,170 , 480,169 , 479,171 , 479,173 , 477,175 , 475,176 , 475,178 , 474,180 , 471,181 , 469,182 , 465,184 , 463,185 , 460,187 , 461,184 , 459,185 , 457,186 , 454,187 , 457,189 , 459,189 , 461,189 , 459,191 , 457,194 , 459,197 , 461,199 , 458,199 , 460,200 , 458,202 , 460,202 , 460,204 , 459,206 , 457,208 , 456,210 , 454,212 , 452,213 , 449,213 , 447,214 , 445,215 , 443,217 , 442,215 , 440,215 , 438,214 , 435,212 , 433,213 , 431,214 , 429,215 , 427,214 , 426,211 , 425,209 , 426,206 , 424,205 , 422,203 , 420,203 , 418,205 , 416,205 , 413,205" title="China" />
<@renderMap code="CO" coords="166,246 , 163,245 , 161,243 , 164,240 , 163,238 , 162,235 , 163,233 , 165,232 , 167,230 , 169,230 , 171,228 , 169,233 , 170,235 , 172,236 , 175,237 , 177,237 , 177,239 , 177,241 , 179,244 , 174,243 , 174,246 , 174,251 , 174,249 , 172,248 , 170,248 , 168,246" title="Columbia" />
<@renderMap code="CU" coords="166,217 , 163,217 , 161,215 , 158,214 , 156,214 , 154,214 , 152,214 , 156,212 , 159,213 , 162,214 , 166,216 , 168,217" title="Cuba" />
<@renderMap code="CZ" coords="307,166 , 304,168 , 301,167 , 298,167 , 297,165 , 300,164 , 303,164 , 305,165" title="Czech Republic" />
<@renderMap code="DE" coords="293,155 , 295,155 , 297,155 , 299,157 , 300,159 , 301,161 , 301,164 , 298,164 , 298,167 , 299,169 , 295,171 , 293,171 , 290,170 , 290,168 , 288,166 , 288,164 , 288,161 , 290,158 , 292,157 , 292,154" title="Germany" />
<@renderMap code="DZ" coords="285,218 , 282,217 , 272,210 , 266,206 , 266,204 , 268,202 , 270,202 , 273,200 , 277,198 , 276,195 , 277,193 , 279,192 , 283,191 , 285,190 , 287,191 , 289,190 , 292,190 , 291,193 , 290,196 , 291,198 , 293,201 , 293,205 , 294,207 , 294,209 , 296,211 , 290,216 , 287,218 , 285,218" title="Algeria" />
<@renderMap code="EC" coords="161,243 , 163,245 , 166,246 , 164,249 , 162,250 , 161,252 , 158,251 , 160,249 , 158,247 , 159,244" title="Ecuador" />
<@renderMap code="EG" coords="330,200 , 331,203 , 330,206 , 328,203 , 329,206 , 331,210 , 332,213 , 326,214 , 316,214 , 316,203 , 316,201 , 320,200 , 322,200 , 324,199 , 326,199 , 330,200" title="Egypt" />
<@renderMap code="EH" coords="253,216 , 254,213 , 255,211 , 257,208 , 259,206 , 266,206 , 266,208 , 261,208 , 261,212 , 259,215 , 253,215" title="Western Sahara" />
<@renderMap code="ER" coords="333,225 , 334,223 , 336,220 , 338,224 , 340,225 , 342,227 , 340,226 , 337,225 , 335,225 , 333,225" title="Eritrea" />
<@renderMap code="ES" coords="271,192 , 268,190 , 268,188 , 268,186 , 268,183 , 267,181 , 265,179 , 268,179 , 272,179 , 274,179 , 276,179 , 280,180 , 283,180 , 282,183 , 280,183 , 278,186 , 277,189 , 275,191 , 272,191" title="Spain" />
<@renderMap code="ET" coords="341,240 , 338,240 , 336,240 , 333,239 , 331,236 , 328,234 , 330,232 , 331,229 , 333,225 , 335,225 , 337,225 , 340,226 , 342,228 , 341,230 , 344,232 , 349,234 , 346,239 , 343,239" title="Ethiopia" />
<@renderMap code="FI" coords="315,124 , 315,120 , 314,118 , 314,116 , 312,114 , 309,112 , 312,113 , 315,113 , 317,112 , 317,109 , 320,108 , 322,110 , 321,112 , 322,115 , 324,117 , 322,120 , 323,122 , 323,125 , 323,128 , 324,130 , 326,133 , 320,140 , 318,140 , 315,141 , 313,142 , 311,139 , 311,137 , 311,135 , 311,132 , 314,130 , 315,128 , 316,126 , 315,124" title="Finland" />
<@renderMap code="FR" coords="276,171 , 273,170 , 274,168 , 277,168 , 276,166 , 278,167 , 281,165 , 283,163 , 285,165 , 287,166 , 290,167 , 290,169 , 288,172 , 289,175 , 289,177 , 289,179 , 286,179 , 283,179 , 281,180 , 278,180 , 277,176 , 277,174 , 276,172" title="France" />
<@renderMap code="GA" coords="292,247 , 293,245 , 296,244 , 299,242 , 300,245 , 300,248 , 298,248 , 296,248 , 295,251 , 293,249" title="Gabon" />
<@renderMap code="GB" coords="273,147 , 276,147 , 275,150 , 276,153 , 277,155 , 279,157 , 281,159 , 281,161 , 279,164 , 277,164 , 275,164 , 272,165 , 274,163 , 272,162 , 272,159 , 275,158 , 275,156 , 272,154 , 271,152 , 271,150 , 270,148 , 271,146 , 274,145" title="United Kingdom" />
<@renderMap code="GE" coords="346,183 , 344,183 , 341,182 , 341,180 , 338,179 , 341,179 , 344,180 , 347,180 , 348,183 , 346,182" title="Georgia" />
<@renderMap code="GH" coords="280,237 , 276,239 , 274,236 , 275,234 , 274,230 , 278,230 , 279,232 , 279,236" title="Ghana" />
<@renderMap code="GL" coords="200,116 , 204,116 , 203,114 , 200,115 , 203,113 , 204,111 , 204,109 , 201,108 , 197,105 , 200,105 , 203,107 , 203,104 , 201,104 , 202,101 , 199,101 , 198,98 , 199,100 , 197,102 , 197,100 , 197,98 , 197,96 , 196,94 , 196,92 , 195,89 , 194,87 , 192,84 , 191,81 , 186,78 , 183,79 , 181,79 , 179,78 , 179,80 , 176,79 , 177,76 , 174,75 , 172,73 , 179,73 , 176,72 , 178,71 , 180,72 , 180,70 , 177,70 , 174,70 , 170,66 , 170,64 , 176,62 , 180,60 , 182,57 , 181,53 , 179,52 , 178,50 , 180,48 , 184,43 , 185,47 , 184,44 , 187,43 , 187,41 , 187,37 , 189,36 , 191,38 , 194,41 , 191,36 , 192,33 , 195,32 , 198,32 , 199,34 , 198,39 , 200,36 , 205,39 , 202,36 , 205,36 , 203,33 , 202,30 , 205,30 , 212,37 , 212,35 , 212,32 , 216,33 , 213,32 , 210,27 , 216,27 , 216,30 , 216,27 , 219,29 , 219,31 , 219,29 , 217,27 , 210,26 , 210,23 , 214,25 , 211,23 , 215,23 , 215,21 , 221,27 , 220,24 , 223,24 , 221,24 , 224,23 , 221,22 , 221,19 , 224,20 , 224,18 , 227,17 , 233,17 , 240,20 , 240,22 , 234,22 , 231,24 , 229,23 , 225,25 , 228,25 , 231,25 , 233,23 , 236,23 , 241,23 , 240,27 , 243,25 , 247,29 , 245,32 , 241,33 , 232,33 , 234,34 , 230,36 , 235,35 , 241,35 , 241,37 , 238,40 , 242,38 , 243,35 , 245,34 , 246,37 , 246,40 , 245,42 , 243,45 , 242,48 , 244,45 , 248,39 , 250,39 , 252,39 , 253,36 , 257,36 , 261,39 , 258,43 , 255,47 , 252,46 , 247,48 , 254,48 , 253,51 , 249,50 , 248,54 , 250,52 , 253,52 , 252,55 , 249,57 , 249,59 , 249,61 , 247,63 , 247,65 , 246,69 , 248,67 , 250,69 , 247,69 , 250,72 , 252,74 , 247,74 , 246,76 , 246,78 , 248,79 , 246,80 , 249,80 , 250,83 , 246,82 , 248,84 , 245,84 , 248,85 , 247,87 , 250,87 , 250,89 , 246,87 , 245,90 , 246,92 , 246,90 , 248,91 , 248,93 , 245,94 , 243,92 , 245,92 , 243,91 , 240,90 , 242,93 , 240,94 , 238,93 , 238,95 , 241,95 , 238,96 , 241,96 , 241,98 , 245,100 , 245,103 , 247,105 , 243,106 , 242,104 , 240,102 , 238,101 , 236,99 , 238,101 , 241,102 , 239,104 , 236,104 , 235,106 , 239,106 , 236,108 , 239,107 , 241,106 , 244,108 , 246,107 , 243,109 , 242,111 , 239,113 , 237,114 , 235,115 , 233,115 , 230,113 , 231,115 , 229,118 , 228,121 , 225,121 , 223,124 , 222,122 , 222,124 , 219,124 , 219,126 , 218,128 , 218,130 , 216,132 , 214,133 , 215,136 , 215,138 , 214,140 , 211,141 , 211,139 , 209,139 , 207,139 , 207,137 , 206,135 , 204,133 , 202,131 , 203,129 , 203,126 , 202,128 , 202,126 , 201,124 , 203,124 , 201,124 , 199,123 , 202,120 , 199,123 , 200,121 , 198,119 , 201,118 , 203,119 , 201,118 , 198,119 , 200,117 , 204,118 , 202,116 , 199,118 , 199,116" title="Greenland" />
<@renderMap code="GN" coords="266,235 , 263,234 , 262,231 , 260,232 , 257,231 , 258,229 , 260,228 , 262,228 , 265,228 , 266,231 , 267,233 , 267,235" title="Guinea" />
<@renderMap code="GR" coords="314,188 , 313,191 , 311,191 , 310,189 , 313,189 , 311,188 , 309,186 , 310,183 , 313,183 , 315,182 , 318,183 , 314,184 , 314,186" title="Greece" />
<@renderMap code="GY" coords="194,243 , 192,243 , 190,243 , 189,240 , 189,238 , 187,237 , 188,235 , 190,234 , 191,236 , 193,237 , 192,239 , 193,241" title="Guyana" />
<@renderMap code="HR" coords="305,180 , 303,178 , 301,176 , 299,175 , 301,175 , 303,173 , 305,174 , 307,174 , 304,175 , 303,177 , 305,179" title="Croatia" />
<@renderMap code="HU" coords="309,173 , 307,174 , 304,173 , 304,171 , 307,170 , 310,168 , 312,169 , 310,173" title="Hungary" />
<@renderMap code="ID" coords="422,240 , 422,237 , 424,238 , 428,241 , 430,243 , 433,244 , 434,247 , 435,249 , 437,249 , 437,252 , 435,254 , 432,251 , 429,248 , 429,246 , 426,243 , 423,240" title="Indonesia" />
<@renderMap code="ID" coords="438,254 , 441,254 , 444,255 , 446,255 , 449,256 , 447,257 , 442,256 , 439,256 , 436,255 , 438,254" title="Indonesia" />
<@renderMap code="ID" coords="443,243 , 445,244 , 447,243 , 450,243 , 451,241 , 452,239 , 454,239 , 454,241 , 457,244 , 454,245 , 453,247 , 452,249 , 450,251 , 448,250 , 446,250 , 443,249 , 443,247 , 441,245 , 443,243" title="Indonesia" />
<@renderMap code="ID" coords="462,244 , 465,244 , 463,245 , 461,245 , 458,245 , 459,247 , 461,246 , 461,248 , 462,251 , 459,250 , 458,252 , 457,250 , 457,248 , 458,246 , 459,243 , 462,244" title="Indonesia" />
<@renderMap code="ID" coords="489,249 , 489,254 , 489,258 , 487,257 , 486,255 , 484,252 , 482,251 , 479,251 , 477,250 , 479,248 , 476,248 , 475,246 , 478,246 , 479,248 , 482,249 , 485,247 , 488,248" title="Indonesia" />
<@renderMap code="IE" coords="269,156 , 270,159 , 267,161 , 264,162 , 264,160 , 265,158 , 264,156 , 266,156 , 267,154 , 267,156 , 269,156" title="Ireland" />
<@renderMap code="IN" coords="424,205 , 421,207 , 420,209 , 419,211 , 418,214 , 417,212 , 417,210 , 414,209 , 412,208 , 411,210 , 411,212 , 412,214 , 409,215 , 408,217 , 405,218 , 402,221 , 400,223 , 399,227 , 398,229 , 397,232 , 395,234 , 393,231 , 392,229 , 390,225 , 388,223 , 388,219 , 388,216 , 387,214 , 384,216 , 382,214 , 384,213 , 382,213 , 382,211 , 384,211 , 384,209 , 383,206 , 386,205 , 388,202 , 390,199 , 389,197 , 389,195 , 392,195 , 394,194 , 397,195 , 397,197 , 396,200 , 399,201 , 398,204 , 401,205 , 403,206 , 405,206 , 407,207 , 410,208 , 410,205 , 413,207 , 416,207 , 417,205 , 420,203 , 422,203 , 424,205" title="India" />
<@renderMap code="IQ" coords="350,202 , 348,203 , 346,203 , 342,200 , 339,199 , 337,198 , 340,195 , 340,193 , 342,190 , 345,190 , 347,192 , 347,194 , 348,197 , 350,199 , 351,201" title="Iraq" />
<@renderMap code="IR" coords="370,193 , 370,195 , 369,197 , 370,200 , 370,202 , 371,204 , 373,206 , 371,209 , 368,209 , 366,209 , 364,207 , 361,207 , 357,206 , 355,204 , 353,202 , 351,202 , 350,199 , 348,197 , 347,195 , 347,193 , 346,191 , 345,188 , 345,185 , 348,187 , 350,186 , 351,188 , 355,187 , 357,189 , 360,189 , 364,188 , 367,189 , 370,191" title="Islamic Republic of Iran" />
<@renderMap code="IS" coords="256,122 , 257,125 , 258,127 , 256,128 , 252,130 , 249,131 , 245,130 , 246,128 , 243,127 , 246,126 , 243,125 , 243,123 , 245,123 , 247,126 , 248,123 , 251,122 , 253,122 , 256,122" title="Iceland" />
<@renderMap code="IT" coords="290,178 , 289,176 , 289,174 , 291,173 , 294,173 , 297,172 , 299,173 , 297,175 , 297,177 , 300,180 , 303,181 , 306,184 , 303,185 , 304,188 , 302,185 , 300,183 , 296,181 , 294,178 , 292,177" title="Italy" />
<@renderMap code="JP" coords="479,194 , 477,195 , 475,195 , 477,193 , 480,193 , 482,193 , 483,191 , 485,190 , 488,186 , 488,184 , 490,183 , 491,186 , 490,188 , 489,190 , 489,192 , 487,193 , 483,194 , 481,196" title="Japan" />
<@renderMap code="JP" coords="494,178 , 497,179 , 494,180 , 491,180 , 489,180 , 488,182 , 488,180 , 490,179 , 491,176 , 494,177" title="Japan" />
<@renderMap code="KE" coords="341,240 , 340,246 , 340,248 , 338,252 , 335,250 , 329,247 , 330,244 , 330,241 , 332,239 , 336,240 , 338,241 , 340,240" title="Kenya" />
<@renderMap code="KG" coords="389,186 , 387,186 , 384,186 , 386,184 , 384,183 , 385,180 , 389,180 , 392,180 , 397,180 , 396,182 , 393,183 , 391,184 , 389,185" title="Kyrgyzstan" />
<@renderMap code="KP" coords="471,187 , 468,189 , 466,189 , 466,187 , 464,185 , 467,183 , 470,183 , 472,180 , 473,183 , 469,186" title="Democratic People's Republic of Korea" />
<@renderMap code="KZ" coords="362,183 , 359,181 , 355,183 , 353,180 , 351,176 , 353,174 , 351,172 , 351,170 , 348,169 , 349,166 , 351,166 , 353,164 , 355,162 , 357,161 , 360,164 , 363,163 , 366,163 , 368,164 , 370,164 , 369,161 , 369,159 , 371,159 , 370,157 , 372,156 , 376,155 , 381,154 , 384,154 , 385,156 , 387,157 , 390,158 , 393,155 , 395,158 , 398,164 , 400,163 , 402,164 , 405,165 , 407,166 , 408,169 , 407,171 , 404,172 , 402,175 , 398,176 , 399,179 , 397,180 , 394,179 , 390,179 , 387,180 , 385,181 , 382,183 , 378,183 , 377,179 , 375,178 , 371,179 , 366,175 , 362,176 , 362,183" title="Kazakhstan" />
<@renderMap code="LA" coords="428,217 , 431,215 , 431,213 , 433,216 , 435,216 , 436,219 , 438,221 , 440,224 , 437,225 , 435,222 , 434,220 , 431,220 , 429,218" title="Lao People's Democratic Republic" />
<@renderMap code="LT" coords="314,157 , 313,154 , 310,153 , 310,151 , 315,151 , 317,151 , 318,153 , 317,156 , 314,157" title="Lithuania" />
<@renderMap code="LV" coords="310,151 , 311,149 , 313,148 , 315,149 , 316,146 , 319,147 , 321,149 , 320,152 , 317,151 , 315,151 , 312,151" title="Latvia" />
<@renderMap code="LY" coords="315,218 , 303,212 , 300,213 , 297,212 , 294,210 , 293,208 , 294,205 , 293,203 , 294,201 , 296,198 , 299,197 , 301,198 , 305,200 , 307,201 , 308,199 , 311,197 , 313,198 , 316,199 , 316,202 , 316,214 , 316,217" title="Libyan Arab Jamahiriya" />
<@renderMap code="MA" coords="274,193 , 276,195 , 277,197 , 275,198 , 271,201 , 269,202 , 266,204 , 259,206 , 262,205 , 264,202 , 264,200 , 266,197 , 269,195 , 270,192 , 274,193" title="Morocco" />
<@renderMap code="MG" coords="345,281 , 344,279 , 343,277 , 345,274 , 344,270 , 346,268 , 349,267 , 350,264 , 352,262 , 354,266 , 354,268 , 353,270 , 349,281 , 347,282" title="Madagascar" />
<@renderMap code="ML" coords="270,231 , 268,231 , 266,231 , 266,229 , 263,228 , 261,227 , 260,225 , 262,224 , 265,224 , 270,224 , 269,210 , 272,210 , 281,215 , 284,217 , 285,222 , 281,224 , 278,224 , 275,225 , 273,226 , 272,229 , 270,231" title="Mali" />
<@renderMap code="MM" coords="420,223 , 420,221 , 419,218 , 417,216 , 417,214 , 418,211 , 420,210 , 421,207 , 423,206 , 425,205 , 426,207 , 424,210 , 427,211 , 427,214 , 429,215 , 427,217 , 425,217 , 425,220 , 426,223 , 426,225 , 427,227 , 426,231 , 426,228 , 425,226 , 425,223 , 423,221 , 421,223" title="Myanmar" />
<@renderMap code="MN" coords="454,173 , 452,175 , 449,176 , 446,175 , 446,178 , 443,180 , 439,180 , 436,182 , 433,182 , 431,181 , 427,180 , 424,180 , 421,178 , 419,176 , 415,175 , 415,173 , 413,170 , 411,169 , 410,167 , 412,166 , 417,164 , 420,164 , 422,165 , 425,165 , 425,163 , 427,161 , 431,163 , 433,165 , 436,164 , 438,165 , 441,167 , 444,167 , 447,166 , 450,165 , 452,165 , 452,169 , 454,170 , 457,170 , 458,172 , 456,172" title="Mongolia" />
<@renderMap code="MR" coords="254,223 , 255,220 , 254,218 , 253,215 , 259,215 , 259,213 , 261,208 , 266,208 , 266,206 , 272,210 , 269,210 , 270,222 , 265,224 , 263,224 , 260,225 , 258,223 , 254,222" title="Mauritania" />
<@renderMap code="MW" coords="328,265 , 328,263 , 329,261 , 328,259 , 330,261 , 330,264 , 332,266 , 331,268 , 330,266" title="Malawi" />
<@renderMap code="MX" coords="134,208 , 133,210 , 133,213 , 133,215 , 135,217 , 138,220 , 141,219 , 143,217 , 147,215 , 149,215 , 148,217 , 148,219 , 146,220 , 143,220 , 144,222 , 141,224 , 138,223 , 135,223 , 133,223 , 130,222 , 126,220 , 124,220 , 122,218 , 121,216 , 121,213 , 118,210 , 116,209 , 115,206 , 111,203 , 110,201 , 107,199 , 108,202 , 110,204 , 112,207 , 113,210 , 115,212 , 111,210 , 111,208 , 107,206 , 107,203 , 105,201 , 104,199 , 107,197 , 113,200 , 117,200 , 120,199 , 122,201 , 124,203 , 127,202 , 129,205 , 131,207 , 133,208" title="Mexico" />
<@renderMap code="MY" coords="451,239 , 453,236 , 455,237 , 456,239 , 454,239 , 452,239 , 451,241 , 450,243 , 447,243 , 445,244 , 443,243 , 445,243 , 447,241 , 449,239" title="Malaysia" />
<@renderMap code="MZ" coords="327,282 , 327,280 , 325,277 , 327,274 , 328,272 , 328,270 , 325,268 , 328,265 , 330,267 , 331,269 , 332,266 , 330,264 , 330,261 , 333,262 , 335,261 , 337,261 , 339,260 , 339,263 , 339,265 , 339,267 , 337,269 , 334,271 , 331,273 , 331,277 , 331,279 , 328,282" title="Mozambique" />
<@renderMap code="NA" coords="309,281 , 309,286 , 306,287 , 303,286 , 301,282 , 300,280 , 300,277 , 299,275 , 297,272 , 296,269 , 298,269 , 306,270 , 309,270 , 314,270 , 316,270 , 314,271 , 310,271 , 310,276 , 309,281" title="Namibia" />
<@renderMap code="NE" coords="279,224 , 284,224 , 285,218 , 287,218 , 290,216 , 297,212 , 299,212 , 301,213 , 301,215 , 302,217 , 302,222 , 299,225 , 297,227 , 295,227 , 292,227 , 289,227 , 287,226 , 285,227 , 284,229 , 282,228 , 280,226" title="Niger" />
<@renderMap code="NG" coords="300,227 , 301,229 , 299,231 , 297,234 , 296,236 , 293,236 , 292,238 , 290,239 , 287,239 , 285,237 , 283,237 , 283,233 , 284,231 , 284,228 , 287,226 , 289,227 , 292,227 , 294,227 , 297,227 , 300,227" title="Nigeria" />
<@renderMap code="NO" coords="290,133 , 292,132 , 294,132 , 296,129 , 294,131 , 295,128 , 297,126 , 297,124 , 299,122 , 299,120 , 302,119 , 302,117 , 303,115 , 305,115 , 304,113 , 306,111 , 308,109 , 308,111 , 309,109 , 312,109 , 310,107 , 313,107 , 315,106 , 315,104 , 317,104 , 316,107 , 318,104 , 319,107 , 319,104 , 321,104 , 321,108 , 321,105 , 324,105 , 324,108 , 321,108 , 324,109 , 322,112 , 322,109 , 318,108 , 317,111 , 316,114 , 313,113 , 311,111 , 309,112 , 309,114 , 306,114 , 305,116 , 303,118 , 302,121 , 301,124 , 299,128 , 297,131 , 297,133 , 297,136 , 297,138 , 297,141 , 296,144 , 295,141 , 294,144 , 291,146 , 289,146 , 287,144 , 288,142 , 287,139 , 289,139 , 286,138 , 287,136 , 289,134" title="Norway" />
<@renderMap code="NP" coords="400,202 , 403,203 , 405,204 , 407,205 , 410,205 , 410,208 , 407,207 , 405,206 , 403,206 , 401,205 , 398,204 , 400,202" title="Nepal" />
<@renderMap code="NZ" coords="538,308 , 538,311 , 535,313 , 534,317 , 531,318 , 529,317 , 528,315 , 530,313 , 534,311 , 535,309 , 537,307 , 539,308" title="New Zeland" />
<@renderMap code="NZ" coords="540,305 , 540,302 , 540,300 , 538,298 , 537,296 , 539,297 , 540,300 , 542,301 , 545,301 , 544,303 , 543,306 , 541,308 , 540,306" title="New Zeland" />
<@renderMap code="OM" coords="358,222 , 356,219 , 361,217 , 362,214 , 362,211 , 364,211 , 366,212 , 368,214 , 366,217 , 365,219 , 363,220 , 361,221 , 358,222" title="Oman" />
<@renderMap code="PE" coords="175,260 , 176,263 , 176,265 , 175,267 , 175,269 , 172,270 , 167,267 , 165,265 , 164,262 , 161,257 , 159,255 , 157,252 , 159,250 , 160,252 , 162,250 , 164,249 , 166,247 , 169,247 , 172,248 , 174,249 , 174,251 , 171,251 , 169,254 , 168,256 , 170,258 , 172,259 , 175,260" title="Peru" />
<@renderMap code="PG" coords="497,257 , 495,256 , 493,256 , 492,258 , 490,258 , 489,255 , 489,249 , 493,250 , 496,252 , 499,254 , 499,256 , 502,258 , 504,260 , 500,259 , 498,258" title="Papua New Guinea" />
<@renderMap code="PK" coords="395,193 , 393,194 , 390,194 , 390,196 , 391,198 , 389,201 , 388,203 , 385,206 , 383,207 , 385,211 , 382,211 , 380,211 , 378,209 , 375,209 , 371,209 , 372,207 , 373,205 , 370,202 , 372,203 , 374,203 , 378,202 , 378,200 , 381,199 , 383,197 , 385,195 , 386,193 , 387,191 , 389,191 , 391,190 , 394,193" title="Pakistan" />
<@renderMap code="PL" coords="312,167 , 310,167 , 308,166 , 305,165 , 303,164 , 301,164 , 301,161 , 300,159 , 301,157 , 303,156 , 306,155 , 308,155 , 313,156 , 314,159 , 314,162 , 315,164 , 313,166" title="Poland" />
<@renderMap code="PT" coords="268,190 , 265,190 , 266,188 , 265,186 , 266,182 , 269,181 , 269,184 , 268,187 , 268,189" title="Portugal" />
<@renderMap code="PY" coords="185,277 , 186,274 , 189,272 , 192,273 , 192,277 , 194,277 , 196,279 , 197,282 , 196,284 , 192,285 , 193,282 , 189,279 , 185,277" title="Paraquay" />
<@renderMap code="RO" coords="313,177 , 310,175 , 312,171 , 315,170 , 318,170 , 321,172 , 321,175 , 323,175 , 321,178 , 319,177 , 317,178 , 313,178" title="Romania" />
<@renderMap code="RS" coords="307,178 , 308,176 , 308,173 , 311,175 , 312,178 , 312,180 , 309,182 , 309,179" title="Serbia" />
<@renderMap code="RU" coords="10,112 , 13,115 , 17,117 , 17,119 , 18,122 , 19,119 , 21,120 , 24,122 , 24,124 , 22,125 , 20,127 , 18,127 , 16,125 , 12,124 , 12,122 , 10,126 , 10,123 , 10,119 , 10,116 , 10,112" title="Russian Federation" />
<@renderMap code="RU" coords="359,103 , 356,102 , 356,99 , 358,97 , 358,95 , 361,93 , 363,94 , 362,97 , 362,99 , 363,103 , 365,105 , 363,105 , 360,105 , 359,103" title="Russian Federation" />

<@renderMap code="RU" coords="360,93 , 361,90 , 361,88 , 363,87 , 362,84 , 365,83 , 369,80 , 372,79 , 375,78 , 377,76 , 380,73 , 382,76 , 379,79 , 374,81 , 371,84 , 368,85 , 368,87 , 366,87 , 366,90 , 365,92 , 363,94 , 361,93" title="Russian Federation" />
<!--
<@renderMap code="RU" coords="419,80 , 422,79 , 424,79 , 426,78 , 427,81 , 428,84 , 427,82 , 428,80 , 426,77 , 429,76 , 431,77 , 429,76 , 429,74 , 433,70 , 437,70 , 434,73 , 437,73 , 439,74 , 438,77 , 440,76 , 445,76 , 447,78 , 448,80 , 448,82 , 446,87 , 442,89 , 440,92 , 438,93 , 436,96 , 441,94 , 443,93 , 443,90 , 446,92 , 448,95 , 449,97 , 448,94 , 451,92 , 456,92 , 458,95 , 461,95 , 465,97 , 467,98 , 469,102 , 469,100 , 468,98 , 471,101 , 473,104 , 475,105 , 476,103 , 477,100 , 478,102 , 482,101 , 485,104 , 485,101 , 488,102 , 487,100 , 487,97 , 489,97 , 498,98 , 495,98 , 498,98 , 497,100 , 498,98 , 500,98 , 503,100 , 503,102 , 505,103 , 512,104 , 516,104 , 518,107 , 519,110 , 520,112 , 521,109 , 524,109 , 528,110 , 530,110 , 532,113 , 534,112 , 534,110 , 533,108 , 537,108 , 542,109 , 546,110 , 548,114 , 548,118 , 548,121 , 548,124" title="Russian Federation" />
 -->
<@renderMap code="RU" coords="423,51 , 421,52 , 416,51 , 416,49 , 418,46 , 422,42 , 425,47 , 424,50" title="Russian Federation" />
<@renderMap code="RU" coords="428,58 , 428,61 , 426,62 , 421,59 , 420,57 , 418,56 , 420,54 , 420,52 , 424,51 , 424,55 , 426,52 , 428,54 , 428,58" title="Russian Federation" />
<@renderMap code="RU" coords="436,64 , 432,66 , 427,67 , 428,65 , 429,62 , 430,60 , 431,58 , 433,58 , 432,62 , 434,59 , 435,62 , 436,64" title="Russian Federation" />
<@renderMap code="RU" coords="491,158 , 491,156 , 493,158 , 493,160 , 493,162 , 494,165 , 495,168 , 493,167 , 492,170 , 493,172 , 491,173 , 491,170 , 491,168 , 491,164 , 490,161 , 491,159" title="Russian Federation" />
<@renderMap code="RU" coords="495,83 , 494,85 , 492,83 , 493,85 , 491,85 , 488,86 , 485,86 , 483,83 , 484,81 , 486,78 , 489,81 , 490,79 , 493,80 , 496,82" title="Russian Federation" />
<@renderMap code="RU" coords="548,126 , 545,128 , 543,126 , 539,127 , 542,127 , 544,127 , 545,129 , 546,131 , 547,133 , 544,134 , 540,135 , 537,137 , 534,140 , 532,139 , 528,140 , 525,141 , 523,141 , 523,143 , 521,146 , 523,147 , 522,149 , 522,152 , 520,153 , 520,155 , 518,156 , 518,158 , 516,159 , 515,161 , 513,164 , 512,160 , 511,154 , 511,152 , 512,150 , 513,147 , 515,147 , 517,144 , 520,141 , 523,139 , 524,136 , 525,134 , 523,134 , 522,136 , 519,139 , 518,136 , 515,136 , 513,137 , 512,139 , 509,141 , 510,144 , 508,143 , 505,144 , 502,142 , 500,143 , 498,143 , 494,143 , 491,144 , 489,146 , 486,149 , 484,151 , 481,154 , 483,155 , 483,157 , 486,156 , 488,156 , 490,158 , 490,161 , 489,163 , 489,165 , 488,168 , 487,170 , 485,173 , 482,177 , 479,180 , 477,180 , 474,180 , 475,178 , 476,175 , 478,174 , 479,172 , 480,170 , 477,169 , 474,170 , 474,168 , 472,167 , 469,166 , 469,164 , 468,161 , 466,159 , 463,158 , 459,158 , 459,160 , 457,165 , 453,166 , 451,166 , 447,166 , 444,167 , 441,167 , 439,165 , 436,164 , 433,165 , 431,163 , 428,162 , 425,162 , 426,164 , 422,165 , 420,165 , 418,164 , 413,166 , 411,166 , 408,166 , 406,166 , 404,164 , 402,164 , 400,163 , 395,158 , 393,156 , 390,157 , 388,158 , 386,155 , 385,153 , 382,153 , 377,155 , 373,156 , 370,156 , 371,158 , 369,159 , 369,161 , 370,164 , 368,164 , 366,164 , 364,163 , 362,164 , 359,162 , 356,162 , 354,163 , 352,164 , 349,165 , 349,167 , 349,169 , 352,172 , 353,175 , 352,179 , 350,183 , 348,182 , 346,180 , 343,179 , 339,178 , 337,177 , 334,176 , 335,174 , 337,172 , 338,169 , 339,167 , 336,165 , 333,165 , 331,163 , 329,160 , 327,160 , 328,158 , 325,156 , 325,153 , 323,152 , 321,149 , 320,147 , 320,144 , 321,142 , 324,142 , 321,140 , 325,134 , 325,132 , 324,129 , 324,127 , 323,124 , 322,120 , 324,117 , 322,115 , 322,113 , 324,110 , 326,109 , 328,109 , 329,111 , 332,111 , 335,113 , 338,116 , 340,117 , 340,120 , 336,123 , 332,122 , 329,120 , 326,119 , 329,121 , 331,123 , 331,127 , 333,129 , 335,130 , 333,127 , 336,127 , 339,128 , 338,125 , 341,123 , 343,121 , 344,118 , 345,116 , 343,113 , 347,114 , 348,116 , 346,117 , 347,119 , 350,119 , 350,117 , 352,116 , 355,114 , 357,114 , 360,112 , 359,114 , 362,113 , 364,114 , 367,112 , 367,114 , 370,112 , 369,110 , 375,110 , 379,113 , 381,115 , 382,113 , 381,110 , 378,110 , 379,108 , 379,105 , 381,100 , 382,96 , 386,96 , 388,98 , 387,101 , 387,103 , 387,106 , 387,108 , 387,112 , 389,114 , 388,116 , 387,118 , 386,121 , 382,120 , 386,122 , 389,119 , 390,116 , 390,114 , 393,112 , 394,115 , 394,117 , 397,117 , 395,117 , 395,114 , 392,111 , 389,111 , 389,109 , 390,106 , 389,103 , 389,100 , 391,99 , 391,96 , 392,98 , 391,101 , 393,103 , 396,104 , 395,102 , 393,101 , 394,99 , 396,98 , 399,99 , 403,101 , 402,103 , 402,105 , 402,107 , 402,104 , 403,106 , 403,103 , 403,100 , 401,98 , 399,97 , 399,95 , 399,93 , 406,92 , 408,91 , 407,93 , 408,95 , 407,93 , 410,91 , 407,89 , 407,87 , 409,85 , 412,82 , 419,80 , 422,79 , 424,79 , 426,78 , 427,81 , 428,84 , 427,82 , 428,80 , 426,77 , 429,76 , 431,77 , 429,76 , 429,74 , 433,70 , 437,70 , 434,73 , 437,73 , 439,74 , 438,77 , 440,76 , 445,76 , 447,78 , 448,80 , 448,82 , 446,87 , 442,89 , 440,92 , 438,93 , 436,96 , 441,94 , 443,93 , 443,90 , 446,92 , 448,95 , 449,97 , 448,94 , 451,92 , 456,92 , 458,95 , 461,95 , 465,97 , 467,98 , 469,102 , 469,100 , 468,98 , 471,101 , 473,104 , 475,105 , 476,103 , 477,100 , 478,102 , 482,101 , 485,104 , 485,101 , 488,102 , 487,100 , 487,97 , 489,97 , 498,98 , 495,98 , 498,98 , 497,100 , 498,98 , 500,98 , 503,100 , 503,102 , 505,103 , 512,104 , 516,104 , 518,107 , 519,110 , 520,112 , 521,109 , 524,109 , 528,110 , 530,110 , 532,113 , 534,112 , 534,110 , 533,108 , 537,108 , 542,109 , 546,110 , 548,114 , 548,118 , 548,121 , 548,124" title="Russian Federation" />
<!--
<@renderMap code="RU" coords="548,126 , 545,128 , 543,126 , 539,127 , 542,127 , 544,127 , 545,129 , 546,131 , 547,133 , 544,134 , 540,135 , 537,137 , 534,140 , 532,139 , 528,140 , 525,141 , 523,141 , 523,143 , 521,146 , 523,147 , 522,149 , 522,152 , 520,153 , 520,155 , 518,156 , 518,158 , 516,159 , 515,161 , 513,164 , 512,160 , 511,154 , 511,152 , 512,150 , 513,147 , 515,147 , 517,144 , 520,141 , 523,139 , 524,136 , 525,134 , 523,134 , 522,136 , 519,139 , 518,136 , 515,136 , 513,137 , 512,139 , 509,141 , 510,144 , 508,143 , 505,144 , 502,142 , 500,143 , 498,143 , 494,143 , 491,144 , 489,146 , 486,149 , 484,151 , 481,154 , 483,155 , 483,157 , 486,156 , 488,156 , 490,158 , 490,161 , 489,163 , 489,165 , 488,168 , 487,170 , 485,173 , 482,177 , 479,180 , 477,180 , 474,180 , 475,178 , 476,175 , 478,174 , 479,172 , 480,170 , 477,169 , 474,170 , 474,168 , 472,167 , 469,166 , 469,164 , 468,161 , 466,159 , 463,158 , 459,158 , 459,160 , 457,165 , 453,166 , 451,166 , 447,166 , 444,167 , 441,167 , 439,165 , 436,164 , 433,165 , 431,163 , 428,162 , 425,162 , 426,164 , 422,165 , 420,165 , 418,164 , 413,166 , 411,166 , 408,166 , 406,166 , 404,164 , 402,164 , 400,163 , 395,158 , 393,156 , 390,157 , 388,158 , 386,155 , 385,153 , 382,153 , 377,155 , 373,156 , 370,156 , 371,158 , 369,159 , 369,161 , 370,164 , 368,164 , 366,164 , 364,163 , 362,164 , 359,162 , 356,162 , 354,163 , 352,164 , 349,165 , 349,167 , 349,169 , 352,172 , 353,175 , 352,179 , 350,183 , 348,182 , 346,180 , 343,179 , 339,178 , 337,177 , 334,176 , 335,174 , 337,172 , 338,169 , 339,167 , 336,165 , 333,165 , 331,163 , 329,160 , 327,160 , 328,158 , 325,156 , 325,153 , 323,152 , 321,149 , 320,147 , 320,144 , 321,142 , 324,142 , 321,140 , 325,134 , 325,132 , 324,129 , 324,127 , 323,124 , 322,120 , 324,117 , 322,115 , 322,113 , 324,110 , 326,109 , 328,109 , 329,111 , 332,111 , 335,113 , 338,116 , 340,117 , 340,120 , 336,123 , 332,122 , 329,120 , 326,119 , 329,121 , 331,123 , 331,127 , 333,129 , 335,130 , 333,127 , 336,127 , 339,128 , 338,125 , 341,123 , 343,121 , 344,118 , 345,116 , 343,113 , 347,114 , 348,116 , 346,117 , 347,119 , 350,119 , 350,117 , 352,116 , 355,114 , 357,114 , 360,112 , 359,114 , 362,113 , 364,114 , 367,112 , 367,114 , 370,112 , 369,110 , 375,110 , 379,113 , 381,115 , 382,113 , 381,110 , 378,110 , 379,108 , 379,105 , 381,100 , 382,96 , 386,96 , 388,98 , 387,101 , 387,103 , 387,106 , 387,108 , 387,112 , 389,114 , 388,116 , 387,118 , 386,121 , 382,120 , 386,122 , 389,119 , 390,116 , 390,114 , 393,112 , 394,115 , 394,117 , 397,117 , 395,117 , 395,114 , 392,111 , 389,111 , 389,109 , 390,106 , 389,103 , 389,100 , 391,99 , 391,96 , 392,98 , 391,101 , 393,103 , 396,104 , 395,102 , 393,101 , 394,99 , 396,98 , 399,99 , 403,101 , 402,103 , 402,105 , 402,107 , 402,104 , 403,106 , 403,103 , 403,100 , 401,98 , 399,97 , 399,95 , 399,93 , 406,92 , 408,91 , 407,93 , 408,95 , 407,93 , 410,91 , 407,89 , 407,87 , 409,85 , 412,82 , 419,80" title="Russian Federation" />
-->
<@renderMap code="SA" coords="356,211 , 357,213 , 361,213 , 361,217 , 356,219 , 352,219 , 350,221 , 348,221 , 345,221 , 343,222 , 341,220 , 340,217 , 337,215 , 337,213 , 335,211 , 331,205 , 331,203 , 334,202 , 334,199 , 337,198 , 342,200 , 346,203 , 348,203 , 351,204 , 353,206 , 355,210" title="Saudi Arabia" />
<@renderMap code="SD" coords="325,240 , 323,239 , 320,239 , 318,237 , 316,234 , 314,233 , 313,230 , 312,228 , 312,226 , 313,224 , 315,218 , 316,214 , 325,214 , 334,214 , 335,219 , 336,221 , 333,224 , 333,228 , 331,230 , 330,233 , 331,236 , 332,239 , 330,239 , 328,240 , 326,240" title="Sudan" />
<@renderMap code="SE" coords="296,144 , 297,142 , 298,140 , 298,137 , 297,135 , 297,132 , 298,129 , 300,125 , 300,122 , 303,119 , 304,116 , 306,115 , 309,115 , 309,112 , 312,114 , 314,116 , 314,118 , 315,120 , 315,124 , 313,123 , 311,125 , 311,128 , 308,131 , 306,132 , 305,135 , 305,137 , 305,139 , 307,141 , 306,143 , 304,142 , 306,143 , 303,145 , 304,147 , 303,151 , 301,151 , 299,153 , 297,151 , 297,148 , 297,146 , 295,144" title="Sweden" />
<@renderMap code="SJ" coords="300,58 , 299,56 , 302,56 , 302,59 , 303,61 , 302,55 , 303,52 , 306,55 , 305,58 , 307,59 , 311,61 , 307,64 , 307,67 , 306,70 , 305,73 , 304,75 , 302,74 , 300,70 , 303,71 , 301,69 , 304,68 , 300,69 , 299,67 , 301,67 , 305,64 , 302,64 , 302,61 , 300,64 , 298,66 , 298,64 , 296,62 , 296,60 , 295,56 , 296,54 , 299,54 , 297,56 , 300,58" title="Svalbard and Jan Mayen" />
<@renderMap code="SJ" coords="307,53 , 307,50 , 310,51 , 312,53 , 312,50 , 315,50 , 319,52 , 317,56 , 314,59 , 310,58 , 309,55 , 312,54 , 307,55" title="Svalbard and Jan Mayen" />
<@renderMap code="SO" coords="341,240 , 344,239 , 350,234 , 344,233 , 343,231 , 345,231 , 347,230 , 350,230 , 352,230 , 355,229 , 355,231 , 353,234 , 352,237 , 350,239 , 347,242 , 345,243 , 342,247 , 340,241" title="Somalia" />
<@renderMap code="SY" coords="332,197 , 333,195 , 332,192 , 335,191 , 337,191 , 340,190 , 342,190 , 340,193 , 340,195 , 337,196 , 334,198" title="Syrian Arab Republic" />
<@renderMap code="TD" coords="313,230 , 309,233 , 307,233 , 305,234 , 303,235 , 300,232 , 302,231 , 301,229 , 299,226 , 300,223 , 302,217 , 301,215 , 301,213 , 315,218 , 315,223 , 312,225 , 311,228 , 313,230" title="Chad" />
<@renderMap code="TH" coords="428,236 , 426,234 , 426,231 , 428,229 , 427,226 , 426,224 , 426,222 , 424,219 , 427,217 , 429,217 , 430,219 , 429,221 , 432,220 , 435,221 , 437,223 , 436,226 , 433,225 , 432,228 , 429,228 , 427,231 , 428,233 , 430,236" title="Thailand" />
<@renderMap code="TJ" coords="380,190 , 380,188 , 381,185 , 384,183 , 384,185 , 386,186 , 389,186 , 391,189 , 388,189 , 386,191 , 386,189 , 384,189 , 380,190" title="Tajikistan" />
<@renderMap code="TM" coords="370,193 , 368,190 , 365,189 , 362,188 , 359,190 , 356,189 , 356,186 , 356,183 , 358,181 , 361,181 , 364,183 , 365,180 , 368,181 , 370,183 , 372,185 , 377,188 , 376,190 , 373,192 , 370,193" title="Turkmenistan" />
<@renderMap code="TN" coords="296,197 , 294,199 , 293,201 , 292,198 , 290,196 , 291,193 , 291,191 , 293,190 , 295,190 , 295,192 , 294,195 , 296,197" title="Tunisia" />
<@renderMap code="TR" coords="332,192 , 330,191 , 327,192 , 325,191 , 323,192 , 321,191 , 320,189 , 319,187 , 319,184 , 322,184 , 325,183 , 329,181 , 331,181 , 333,183 , 336,183 , 339,183 , 341,182 , 344,183 , 346,186 , 345,188 , 346,190 , 343,190 , 340,190 , 337,191 , 335,191 , 332,192" title="Turkey" />
<@renderMap code="TZ" coords="331,261 , 330,259 , 328,258 , 326,257 , 324,255 , 323,253 , 324,251 , 324,249 , 329,247 , 335,249 , 337,252 , 337,254 , 337,257 , 338,259 , 336,261 , 334,261 , 331,261" title="United Republic of Tanzania" />
<@renderMap code="UA" coords="331,173 , 333,175 , 330,176 , 327,175 , 329,174 , 326,173 , 323,174 , 321,175 , 322,173 , 322,170 , 320,169 , 318,170 , 315,170 , 313,169 , 313,167 , 315,164 , 314,162 , 316,161 , 319,162 , 322,162 , 324,162 , 326,161 , 329,160 , 330,163 , 333,164 , 336,166 , 338,166 , 338,168 , 336,171 , 334,172 , 331,174" title="Ukraine" />
<@renderMap code="US" coords="178,175 , 176,177 , 174,178 , 173,180 , 173,182 , 170,183 , 168,184 , 168,186 , 165,190 , 165,188 , 165,186 , 165,188 , 165,190 , 166,192 , 164,193 , 163,195 , 160,197 , 158,198 , 157,200 , 158,203 , 159,207 , 159,209 , 157,208 , 155,206 , 155,203 , 151,202 , 149,201 , 146,201 , 144,202 , 146,203 , 144,203 , 142,202 , 139,202 , 137,203 , 135,204 , 133,206 , 134,208 , 131,207 , 129,205 , 127,202 , 125,203 , 123,202 , 120,200 , 117,199 , 113,200 , 107,198 , 104,198 , 102,196 , 100,195 , 97,192 , 96,189 , 94,186 , 93,183 , 93,180 , 94,175 , 93,172 , 92,169 , 95,169 , 95,171 , 96,169 , 108,168 , 137,168 , 140,168 , 142,169 , 145,170 , 152,172 , 154,174 , 156,178 , 155,181 , 161,180 , 164,178 , 167,176 , 172,176 , 174,173 , 175,171 , 177,172 , 177,174" title="United States" />
<@renderMap code="US" coords="33,139 , 33,137 , 31,137 , 32,134 , 33,132 , 35,132 , 38,131 , 38,129 , 38,127 , 36,128 , 32,128 , 30,127 , 31,125 , 28,124 , 30,122 , 33,121 , 37,123 , 36,120 , 38,121 , 36,119 , 34,117 , 29,115 , 30,113 , 34,112 , 35,109 , 37,107 , 40,106 , 43,105 , 45,102 , 46,105 , 48,104 , 50,104 , 52,106 , 56,106 , 62,108 , 65,108 , 68,110 , 68,140 , 71,140 , 73,143 , 76,142 , 79,145 , 81,149 , 84,151 , 85,153 , 83,151 , 81,150 , 79,148 , 79,146 , 77,143 , 77,146 , 75,144 , 75,146 , 72,144 , 70,141 , 67,142 , 64,141 , 62,140 , 60,139 , 58,138 , 57,141 , 55,141 , 52,144 , 53,140 , 56,139 , 54,138 , 51,140 , 50,142 , 48,144 , 48,146 , 46,148 , 44,150 , 42,151 , 39,153 , 37,153 , 39,152 , 41,150 , 44,148 , 43,146 , 44,144 , 42,145 , 40,144 , 37,145 , 37,143 , 36,141 , 34,142 , 32,140" title="United States" />
<@renderMap code="UZ" coords="380,190 , 377,188 , 374,186 , 372,184 , 369,183 , 366,180 , 364,182 , 362,176 , 366,175 , 370,177 , 373,178 , 376,178 , 377,181 , 380,183 , 385,181 , 385,183 , 388,183 , 386,185 , 383,184 , 381,186 , 381,188" title="Uzbekistan" />
<@renderMap code="VE" coords="185,231 , 188,232 , 188,235 , 187,237 , 186,240 , 184,240 , 182,239 , 183,242 , 181,244 , 179,244 , 177,241 , 177,239 , 178,237 , 176,237 , 173,235 , 171,235 , 170,233 , 170,230 , 172,233 , 172,231 , 174,229 , 177,230 , 180,231 , 183,231 , 186,230" title="Bolivarian Republic of Venezuela" />
<@renderMap code="VN" coords="435,231 , 437,230 , 439,228 , 439,226 , 439,223 , 437,220 , 434,218 , 434,216 , 432,215 , 434,213 , 437,213 , 439,215 , 437,217 , 437,219 , 440,223 , 442,226 , 442,228 , 440,230 , 438,231 , 436,233 , 435,231" title="Viet Nam" />
<@renderMap code="YE" coords="343,222 , 345,221 , 348,221 , 350,221 , 352,219 , 355,219 , 358,222 , 352,225 , 350,226 , 347,227 , 344,228 , 343,224 , 343,222" title="Yemen" />
<@renderMap code="ZA" coords="309,281 , 310,284 , 313,283 , 316,282 , 318,280 , 321,277 , 325,277 , 327,280 , 327,282 , 325,284 , 328,284 , 327,286 , 324,291 , 320,294 , 318,295 , 316,295 , 314,295 , 311,296 , 309,296 , 307,296 , 305,293 , 305,289 , 303,286 , 306,287 , 308,286 , 309,281" title="South Africa" />
<@renderMap code="ZM" coords="315,260 , 317,261 , 319,262 , 321,263 , 323,264 , 321,261 , 322,259 , 325,257 , 328,258 , 328,260 , 329,262 , 328,265 , 324,266 , 322,268 , 319,271 , 316,270 , 314,270 , 312,268 , 312,263 , 315,263 , 315,260" title="Zambia" />
<@renderMap code="ZW" coords="324,267 , 326,268 , 328,271 , 328,273 , 327,275 , 323,277 , 321,276 , 319,274 , 316,270 , 319,271 , 322,268 , 324,267" title="Zimbabwe" />

</map>
</div>

</#macro>

<#macro renderMap code coords title>
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
<span class="hidden">[${code} : ${percent} ]</span>
     <area coords="${coords}" shape="poly" title="${title} (${val})" alt="${title} (${val})" href='/search/results?geographicKeywords=${code} (ISO Country Code)' iso="${code}"
     class="{alwaysOn:true,strokeColor:'666666',strokeWidth:'.5',fillColor:'${color}',fillOpacity:1}" ></area>
 </#if>
</#macro>

<#macro cartouch persistable useDocumentType=false>
    <#local cartouchePart><@upperPersistableTypeLabel persistable /></#local>
    <span class="${cartouchePart?replace(" ", "")?lower_case}-color cartouche"><#t>
    <#if (persistable.status)?? && !persistable.active>
        ${persistable.status} <#t>
    </#if> 
    <#if persistable.urlNamespace=='collection'>
        COLLECTION<#t>
    <#elseif (persistable.resourceType.document && useDocumentType && persistable.documentType.label!='Other')>
        ${persistable.documentType.label?upper_case}<#t>
    <#else>
        ${cartouchePart?upper_case}<#t>
    </#if>
    </span><#lt>
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



</#escape>




<#assign host="">
<#if prefixHost>
	<#assign host="//assets.tdar.org/" />
</#if>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/macros/header.ftl" as header>
<#assign mode>horizontal</#assign>
    <div class=" <#if mode == 'vertical'>col-7<#else>col-6 map mapcontainer</#if>">
            <h3>${siteAcronym} Worldwide</h3>
        <script type="application/json" data-mapdata>
			${homepageGraphs.mapJson}
        </script>
        <script type="application/json" data-locales>
			${homepageGraphs.localesJson}
        </script>

             <div id="worldmap" style="height:350px" data-max="">
             </div>
        <#if mode =='vertical'></div></#if>
             <div id="mapgraphdata"  <#if mode == 'vertical'>data-mode="vertical" class="col-4 offset1"<#else>style="width:100%"</#if>>
        <#if mode =='vertical'><br/><br/></#if>
                 <h5 id="mapGraphHeader"></h5>
                 <div id='mapgraphpie'>                 
                 </div>
             </div>
        <#if mode !='vertical'></div></#if>
	<script>
	$(function() {
    	TDAR.worldmap.initWorldMap("worldmap","${mode}");
	});
	</script>
<script id="c3colors">
 [<#list config.barColors as color><#if color_index != 0>,</#if>"${color}"</#list>] 
</script>

<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro head>
    <script id="ontId" type="application/json" >
    <#noescape>
    ${json}
    </#noescape>
    </script>
    
    </#macro>


    <#macro afterBasicInfo>
        <#if codingSheetsWithMappings?has_content>
        <h3>This Ontology is used by the following Coding Sheets</h3>
        <ul>
            <#list codingSheetsWithMappings as cs>
                <li><a href="<@s.url value="${cs.detailUrl}"/>">${cs.title}</a></li>
            </#list>
        </ul>
        </#if>

	<form class="pull-right" onSubmit="return false;">
	    <div class="btn-group">
		    <input type="search" id="search" placeholder="search"/>
			<span id="searchclear" class="remove">X</span>
		</div>
	</form>
    <h3>${resource.title}</h3>
    <div id="d3" class="d3tree" style="min-height:600px">

    </div>
    <script>
    $(function() {
	    $("#btnOntologyShowMore").click(function() {
	        $(".hidden-nodes").removeClass("hidden-nodes");
	        $("#divOntologyShowMore").hide();
	    });
	    TDAR.d3tree.init();    
	});
    
    </script>
    
    <div id="divHints">
        <em>Click on a node to expand children</em>
    </div>
        <@view.ontology />

    </#macro>



    <#macro footer>
    </#macro>
</#escape>

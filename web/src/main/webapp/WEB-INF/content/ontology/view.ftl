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
    
        <div class="section">
        <@view.categoryVariables />
        </div>

        <#if codingSheetsWithMappings?has_content>
        <div class="section">
        <h2>This Ontology is used by the following Coding Sheets</h2>
        <ul>
            <#list codingSheetsWithMappings as cs>
                <li><a href="<@s.url value="${cs.detailUrl}"/>">${cs.title}</a></li>
            </#list>
        </ul>
        </div>
        </#if>

        <div class="section">
	<form class="float-right" onSubmit="return false;">
	    <div class="btn-group">
		    <input type="search" id="search" class="form-control" placeholder="search"/>
			<span id="searchclear" class="remove">X</span>
		</div>
	</form>
    <h2>${resource.title}</h2>
    <div id="d3" class="d3tree" style="min-height:600px">

    </div>
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

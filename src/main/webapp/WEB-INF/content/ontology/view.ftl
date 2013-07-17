<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#macro afterBasicInfo>
<#if codingSheetsWithMappings?has_content>
	<h3>This Ontology is used by the following Coding Sheets</h3>
	<ul>
	<#list codingSheetsWithMappings as cs>
		<li><a href="<@s.url value="/coding-sheet/${cs.id?c}"/>">${cs.title}</a></li>
	</#list>
	</ul>
</#if>
	<h3>Ontology</h3>
	
	<div id="ontologyViewer" class="" style="overflow:scroll;height:400px;">
        <div id="ontologyViewerPan" style="width:100%;">

        </div>
	</div>
	<div id="divHints">
	<em>click and drag to pan across the ontology, darker nodes contain more children, click on a node to expand children</em>
	</div>
	<@view.ontology />


</#macro>


<#macro localJavascript>
<@view.datatableChildJavascript />
TDAR.ontology.view();
</#macro>

<#macro footer>
    <script src="<@s.url value='/includes/tdar.ontology.js'/>"></script>
</#macro>
</#escape>

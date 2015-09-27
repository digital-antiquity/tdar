<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/${themeDir}/settings.ftl" as settings />
    <#macro head>
    <script id="ontId" type="application/json" >
    <#noescape>
    ${json}
    </#noescape>
    </script>

    <style>
    div.orgChart div.hasChildren {
        background-color: #${settings.barColors[1]};
    }
    
    div.orgChart div.node {
        background-color: #${settings.barColors[0]};
    }
    
    div.orgChart div.node.level1 {
        background-color: ${settings.barColors[0]};
    }
    
    div.orgChart div.node.level2 {
        background-color: ${settings.barColors[3]};
    }
    
    div.orgChart div.node.level3 {
        background-color: ${settings.barColors[4]};
    }
        
    </style>
    
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
    <h3>Ontology</h3>

<#if orgchart!false>
    <div id="ontologyViewer" class="" style="overflow:scroll;height:400px;">
        <div id="ontologyViewerPan" style="width:100%;">

        </div>
    </div>
    <#else>
    <form class="pull-right">
    <input type="search" id="search" placeholder="search"/>
    </form>
    <div id="d3" class="d3tree" style="height:600px">
    
    </div>
    <script>
    $(function() {
    TDAR.d3tree.init();    
});
    
    </script>
</#if>
    
    <div id="divHints">
        <em>Click on a node to expand children</em>
    </div>
        <@view.ontology />

    </#macro>



    <#macro footer>
    </#macro>
</#escape>

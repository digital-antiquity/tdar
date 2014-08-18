<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
    <title>Match column values to ontology nodes</title>

    <style type='text/css'>
        .ui-autocomplete {
            max-height: 144pt;
            overflow-y: auto;
            overflow-x: hidden;
            line-height: 1em;
        }

        .ui-menu-item {
            font-family: courier !Important;
            white-space: pre !important;
        }

        .ui-widget {
            font-family: courier;
        }

        /* IE 6 doesn't support max-height
         * we use height instead, but this forces the menu to always be this tall
         */
        * html .ui-autocomplete {
            height: 144pt;
            overflow-x: hidden;
        }

        .ui-menu .ui-menu-item {
            border: 0px;
            margin: 0px !important;
            padding: 0px !important;
        }

        .ui-menu-item a {
            border: 1px solid transparent;
            min-height: 1em;
            margin: 0px !important;
            padding: 0px !important;
        }

    </style>

</head>
<body>

    <@nav.toolbar "coding-sheet" "mapping" />


<h2>Map Codes to Ontology Values</h2>

<div id='display' class="">
    <@s.form method='post' id="mapontologyform" action='save-mapping'>
        <@s.hidden name='id' value='${resource.id?c}'/>
        <#assign isLast = false/>
        <#assign count = 0/>


        <div class="btn-group">
            <button class="btn" type="button" id="autosuggest">Autosuggest Mappings</button>
            <button class="btn" type="button" id="clearAll">Clear all</button>
        </div>

        <div class="control-group">
            <label class="control-label">Mappings</label>
            <#list codingRules as rule>
                <div class="controls controls-row mappingPair ${rule.code}" id="row_${rule.code}">
                    <@s.hidden name='codingRules[${rule_index?c}].id' />
                    <@s.textfield theme="simple" name='codingRules[${rule_index?c}].term' size='50' readonly=true cssClass="span4 codingSheetTerm"/>

                    <div class="span1">
                        <img src="<@s.url value='/images/arrow_right.png' />" alt="right arrow"/>
                    </div>

                    <div>
                        <@s.hidden name="codingRules[${rule_index?c}].ontologyNode.id" id="ontologyNodeId_${rule_index?c}" />
                        <div class="input-append">
                            <@s.textfield theme="simple" name="codingRules[${rule_index?c}].ontologyNode.displayName" id="autocomp_${rule_index?c}"
                            cssClass="manualAutocomplete ontologyValue span4" autocompleteIdElement="#ontologyNodeId_${rule_index?c}"/>
                            <button type="button" class="btn show-all"><i class="icon-chevron-down"></i></button>
                        </div>
                    </div>
                </div>
            </#list>
        </div>

        <@edit.submit "Save" false />
    </@s.form>
</div>

<script type="text/javascript">

        <#noescape>
            <#list codingRules as rule>
            var autocomp_${rule_index?c}Suggestions = [
                {id: "", name: ""}<#t>
                <#list rule.suggestions as suggestion>
                    <#if suggestion_index == 0>,
                        {id: "", name: " -- Suggested Values --"}</#if>
                    ,
                    {id: "${suggestion.id?c}", name: "${suggestion.displayName?js_string}"}
                </#list>
            ];
            </#list>
        </#noescape>

    $(document).ready(function () {
        TDAR.ontologyMapping.initMapping();
    });

        <#macro repeat num=0 value="  |"><#t/>
            <#if ((num?number) &gt; 0)>
                <#list 1..(num?number) as x>${value}</#list><#t/>
            </#if>
        </#macro>


    // Ontologies
    var ontology = [
        {id: "", name: " -- All Values -- "},
        <#list ontologyNodes as ont>
            <#if (ont_index > 0 )>,</#if> {id: "${ont.id?c}", name: "<@repeat num="${ont.numberOfParents-1}" />- <#noescape>${(ont.formattedNameWithSynonyms!ont.iri)?js_string}</#noescape>"}
        </#list>
    ];

</script>
</body>
</#escape>
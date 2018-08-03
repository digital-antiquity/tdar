<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
    <title>Match column values to ontology nodes</title>
</head>
<body class="ontology-mapping">

    <@nav.toolbar "coding-sheet" "mapping" />


<h2>Map Codes to Ontology Values</h2>

<h3>Basic Info</h3>
<dl>
    <dt>Coding Sheet</dt>
    <dd>${resource.title}</dd>
    <dt>Mapped Ontology</dt>
    <dd>${resource.defaultOntology.title}</dd>
</dl>

<h3>Mapped Values</h3>
<div id='display' class="">
    <@s.form method='post' id="mapontologyform" action='save-mapping'>
        <@s.token name='struts.csrf.token' />
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
                <div class="controls controls-row mappingPair ${rule.code}" id="row_${rule.code}" data-idx="${rule_index?c}">
                    <@s.hidden name='codingRules[${rule_index?c}].id' />
                    <@s.textfield theme="simple" name='codingRules[${rule_index?c}].formattedTerm' size='50' readonly=true cssClass="col-4 codingSheetTerm"/>

                    <div class="col-1">
                        <img src="<@s.url value='/images/arrow_right.png' />" alt="right arrow"/>
                    </div>

                    <div>
                        <@s.hidden name="codingRules[${rule_index?c}].ontologyNode.id" id="ontologyNodeId_${rule_index?c}" />
                        <div class="input-append">
                            <@s.textfield theme="simple" name="codingRules[${rule_index?c}].ontologyNode.displayName" id="autocomp_${rule_index?c}"
                            cssClass="manualAutocomplete ontologyValue col-4" autocompleteIdElement="#ontologyNodeId_${rule_index?c}"/>
                            <button type="button" class="btn show-all"><i class="icon-chevron-down"></i></button>
                        </div>
                    </div>
                </div>
            </#list>

        <label class="control-label">Special Mappings</label>
        <i>These mapping rules are not in the dataset, but can be used to map 'special' values to an ontology. There are 3 custom mappings that represent NULLs in the database, entries missing from the coding sheet, and entries that are not mapped to the ontology (unmapped).</i>

            <#list specialRules as rule>
                <div class="controls controls-row mappingPair ${rule.code}" id="row_s_${rule.code}" data-idx="${(10000 + rule_index)?c}">
                    <@s.hidden name='specialRules[${rule_index?c}].id' />
                    <@s.hidden name='specialRules[${rule_index?c}].code'  />
                    <@s.textfield theme="simple" name='specialRules[${rule_index?c}].term' size='50' readonly=true cssClass="col-4 codingSheetTerm"/>

                    <div class="col-1">
                        <img src="<@s.url value='/images/arrow_right.png' />" alt="right arrow"/>
                    </div>

                    <div>
                        <@s.hidden name="specialRules[${rule_index?c}].ontologyNode.id" id="ontologyNodeId_s_${rule_index?c}" />
                        <div class="input-append">
                            <@s.textfield theme="simple" name="specialRules[${rule_index?c}].ontologyNode.displayName" id="autocomp_${(10000 + rule_index)?c}"
                            cssClass="manualAutocomplete ontologyValue col-4" autocompleteIdElement="#ontologyNodeId_s_${rule_index?c}"/>
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
            <#list specialRules as rule>
            var autocomp_${(10000 + rule_index)?c}Suggestions = [];
            </#list>
        </#noescape>

    $(document).ready(function () {
        TDAR.ontologyMapping.initMapping("#mapontologyform", TDAR.loadDocumentData().flattenedOntologyNodes);
    });

        <#macro repeat num=0 value="  |"><#t/>
            <#if ((num?number) &gt; 0)>
                <#list 1..(num?number) as x>${value}</#list><#t/>
            </#if>
        </#macro>


</script>

<#noescape>
<script type="application/json" id="flattenedOntologyNodes">
    [{"id": "", "name": " -- All Values -- "}
    <#list ontologyNodes as ont>
    ,{"id": "${ont.id?c}", "name": "<@repeat num="${ont.numberOfParents-1}" />- ${(ont.formattedNameWithSynonyms!ont.iri)?json_string}"}
    </#list>
    ]
</script>
</#noescape>
</body>
</#escape>

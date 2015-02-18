<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<head>
    <title>Filter Ontology Values</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
</head>
<body class="filter-ontology">
<h1>Filter Ontology Values</h1>

<div class="row">
    <div class="span8">
        <h3>Instructions</h3>

        <p>The complete ontologies for each integration column are listed below, along with columns for each data table that you've chosen to integrated.
            You can filter the data values for each data table listed below that will be used in the data integration. Only the values mapped to an ontology
            will be reported below.
            Select checkboxes next to the values that you would like to be included or aggregated to that level. </p>

        <p>
            Indented unchecked values are aggregated to the next higher level that is checked.
            Unchecked values at the top (leftmost) level are ignored, along with any unchecked
            subdivision categories.
        </p>
        <ul>
            <li>Values that occur in a dataset are indicated with blue checks ( <img src="<@s.url value="/images/checked.gif" />"/> )</li>
            <li>Values that do not occur in a dataset are indicated with red X's ( <img src="<@s.url value="/images/unchecked.gif" />"/>).</li>
        </ul>
    </div>
    <div class="span4">
    <#-- display links with taxonomy expanded -->
        <h3>Copy / Restore Previous Selection</h3>

        <p>If you are regularly performing data integrations, you can save time by reusing previous selections.</p>

        <p><strong>To Copy:</strong> Make your selections, then click the button below and "copy" the codes for future use.</p>

        <p><strong>To Restore:</strong> Click the button below, "paste" the codes into the text box, and finally click "Load my previous selections." </p>

        <button id="btnDisplaySelections" type="button" class="btn btn-mini">Copy/Paste Codes</button>
        </p>
    </div>
</div>
    <#assign integrationcolumn_index =0>

    <@s.form method='post' action='display-filtered-results' id="filterForm">

        <@s.token name='struts.csrf.token' />
        <#assign totalCheckboxCount=0>
        <#list integrationColumns as integrationColumn>
            <#if integrationColumn.displayColumn >

            <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columnType" value="${integrationColumn.columnType}"/>
                <#list integrationColumn.columns as col_ >
                    <#if col_??>
                    <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columns[${col__index}].id" value="${col_.id?c}"/>
                    </#if>
                </#list>
            <#else>
                <#if integrationColumn.sharedOntology??>

                <div class="integration-column">

                    <div class="btn-group pull-right">
                        <span class=" btn " onclick='TDAR.integration.selectAllChildren("onCbId_${integrationColumn.sharedOntology.id?c}_", true);'><i
                                class=" icon-ok-circle"></i> Select All</span>
                        <span class="autocheck btn  "><i class=" icon-ok"></i> Select Shared Values</span>
                        <span class="button  btn " onclick='TDAR.integration.selectAllChildren("onCbId_${integrationColumn.sharedOntology.id?c}_", false);'><i
                                class=" icon-remove-circle"></i> Clear All</span>
                        <span class="button btn  hideElements"><i class=" icon-remove"></i>Hide Empty Ontology Branches</span>
                    </div>
                    <h3>${integrationColumn.sharedOntology.title} [${integrationColumn.name}]</h3>
                    <table class='tableFormat table table-striped integrationTable'>
                        <thead>
                        <tr>
                            <th>Ontology labels</th>
                            <#list integrationColumn.columns as column>
                                <th>${column.name}<br/>
                                    <small>(${column.dataTable.dataset.title})</small>
                                </th>
                            </#list>
                        </tr>
                        </thead>
                        <tbody>
                        <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columnType"
                               value="${integrationColumn.columnType!"integration"}"/>
                            <#list integrationColumn.columns as col>
                            <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columns[${col_index}].id" value="${col.id?c}"/>
                            </#list>

                            <#-- FIXME: lift assignment of 'disabled' to java -->
                            <#list integrationColumn.flattenedOntologyNodeList![] as ontologyNode>
                                <#assign numberOfParents=ontologyNode.numberOfParents>
                                <#assign checkForUser=true />
                                <#if  ontologyNode.legacyColumnHasValueMap?has_content>
                                    <#list ontologyNode.legacyColumnHasValueMap?values as hasValue >
                                        <#if !hasValue>
                                            <#assign checkForUser=false />
                                        </#if>
                                    </#list>
                                </#if>
                                <#assign node_id="onCbId_${integrationColumn.sharedOntology.id?c}_${ontologyNode.index?replace('.', '_')}_${ontologyNode.id?c}" />
                            <tr class="<#if ontologyNode.disabled>disabled</#if>">
                                <td style="white-space: nowrap;">
                                    <#if ontologyNode.parent  && !ontologyNode.disabled ><span class="pull-right">
        &nbsp;(<span class="link" onclick='TDAR.integration.selectChildren("${node_id}", true);'>select all</span>
        | <span class="link" onclick='TDAR.integration.selectChildren("${node_id}", false);'>clear</span>)</span>
                                    </#if>
                                    <label class="inline-label nodeLabel" for='${node_id}'>
                                        <#list 1..numberOfParents as indentationLevel>
                                            &nbsp;&nbsp;&nbsp;&nbsp;
                                        </#list>
                                        <input type='checkbox' id='${node_id}'
                                               name='integrationColumns[${integrationcolumn_index}].filteredOntologyNodes.id'
                                               value='${ontologyNode.id?c}'
                                               <#if checkForUser>canautocheck="true"</#if>     <#if ontologyNode.disabled>disabled="disabled"</#if> />
                                        <#assign totalCheckboxCount=totalCheckboxCount+1>
                                        <#if !ontologyNode.disabled><b></#if>
                                        <span class="nodeName">${ontologyNode.displayName}</span> <!--(${ontologyNode.index})-->
                                        <#if !ontologyNode.disabled></b></#if>
                                    </label>

                                </td>

                                <#list integrationColumn.columns as column>
                                <#assign seenCol=false>
                                <#list ontologyNode.columnHasValueMap?keys as col >
                                    <#if col.id==column.id>
                                            <#assign seenCol = true>
                                           <td> <img src="<@s.url value="/images/checked.gif" />"/></td>
                                    </#if>
                                </#list>
                                    <#if !seenCol>
                                            <td><img src="<@s.url value="/images/unchecked.gif" />"/></td>
                                    </#if>
                                </#list>
       </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>

                <#else>
                These columns do not share a common ontology but ontology integration has not been
                fully implemented yet.
                </#if>
            </#if>
            <#assign integrationcolumn_index = integrationcolumn_index+1>

        </#list>

        <@edit.submit "Next: Apply filter" false/>

        <#list selectedDataTables as table>
        <!-- setting for error condition -->
        <input type="hidden" name="tableIds[${table_index}]" value="${table.id?c}"/>
        </#list>

    </@s.form>


<#noescape>
<script type='text/javascript'>
    $(function () {
        <#-- //var data = ${integrationColumnData}; -->
        var data = [];
        TDAR.integration.initOntologyFilterPage(data);
    })

</script>
</#noescape>
<div id="divModalStore" class="modal modal-big hide fade" tabindex="-1" role="dialog" aria-labelledby="divModalStoreLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="divModalStoreLabel">Ontology Filter Codes</h3>
        <span>The codes in the textbox below represent your current ontology selections. </span>
        <span>To restore ontology filter selections from a previous integration, paste those selection codes into the textbox. </span>
    </div>
    <div class="modal-body">
        <textarea id="txtStr2cb" cols=300 rows=20 style="width:100%; font-family:monospace; font-size:smaller; line-height: normal"
                  spellcheck="false"></textarea>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
        <button id="btnStr2cb" class="btn btn-primary">Load my previous selections</button>
    </div>
</div>
</body>
</#escape>

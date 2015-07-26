<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>

<body class="select-columns">
    <@s.form name='selectDTColForm' method='post' action='filter' id="selectDTColForm">
        <@s.token name='struts.csrf.token' />

    <h3>Data Integration</h3>

    <div class="glide">
        Drag columns from your selected data tables onto the integration table .
    </div>
    <div class="glide">
        <h3>Create your Integration Table:</h3>

        <#macro setupIntegrationColumn column idx=0 init=false>
            <td data-colnum="${idx}" class="<#if column.displayColumn><#if !init>displayColumn<#else>defaultColumn</#if><#else>integrationColumn</#if>">
                <div class="label">Column ${idx + 1} <span class="colType"></span>
                    <input type="hidden" name="integrationColumns[${idx}].columnType" value="<#if column.displayColumn>DISPLAY<#else>INTEGRATION</#if>"
                           class="colTypeField"/>
                    <input type="hidden" name="integrationColumns[${idx}].sequenceNumber" value="${idx}" class="sequenceNumber"/>
                </div>
                <#if column.columns.empty><span
                        class="info">Drag variables from below into this column to setup your integration<br/><br/><br/><br/></span></#if>
                <#list column.columns as col>
                    <input type="hidden" name="integrationColumns[${idx}].columns[${col_index}].id" value="${col.id?c}"/>

                </#list>
            </td>

        </#macro>
        <div id='fixedList' class="affix-top no-indent span12 row navbar-static" data-offset-top="250" data-offset-bottom="250" data-spy="affix">
            <h4>Each Column Below will be a Column In Excel</h4>
            <div class="btn-group pull-right">
                <span class="addAnother btn" id="addColumn"><i class="icon-plus-sign"></i> Add Column</span>
                <span class="btn <#if (sharedOntologies?size < 1)>disabled</#if>" id="autoselect"><i class=" icon-ok-circle"></i> Auto-select integratable columns</span>
                <div class="btn-group">
                      <a class="btn dropdown-toggle <#if (sharedOntologies?size < 1)>disabled</#if>" data-toggle="dropdown" href="#">
                        Add Integration Column
                        <span class="caret"></span>
                      </a>
                    <ul class="dropdown-menu">
                    <#list sharedOntologies as ontology>
                        <li><a href="#" onClick="TDAR.integration.addColumn('${ontology.id?c}')">${ontology.name}</a></li>
                    </#list>
                  </ul>
                </div>
                <span class="btn" id="clear"><i class=" icon-remove-circle"></i>Clear</span>
                <@s.submit value='Next: filter values' id="submitbutton" cssClass="submitbutton submitButton btn button btn-primary" />
            </div>
            <table id="drplist" width="100%">
                <tr>
                    <#if integrationColumns?? && !integrationColumns.empty >
 <#list integrationColumns as integrationColumn>
                        <@setupIntegrationColumn integrationColumn integrationColumn_index />
                    </#list>
<#else>
                        <@setupIntegrationColumn blankIntegrationColumn 0 true/>
                    </#if>

                </tr>
            </table>
            <div class="status"></div>

        </div>
    </div>
    <div class="glide">
        <br/><br/>
        <h2>Select Variables</h2>

        <table width="100%" class="legend">
            <tr>
                <td class="legend displayColumn">&nbsp;</td>
                <td><b>Display Variable</b></td>
                <td class="legend integrationColumn">&nbsp;&nbsp;</td>
                <td><b>Integration Variable with mapped Ontology</b></td>
                <td class="legend measurementColumn">&nbsp;&nbsp;</td>
                <td><b>Measurement Variable</b></td>
                <td class="legend countColumn">&nbsp;&nbsp;</td>
                <td><b>Count Variable</b></td>
            </tr>
        </table>
        <br/>

    <div class="accordion" id="accordion">
        <#assign numCols = 6 />

        <div class="accordion-group">

            <#list selectedDataTables as table>
                <!-- setting for error condition -->
                <input type="hidden" name="tableIds[${table_index}]" value="${table.id?c}"/>

                <div class="accordion-heading">
                    <h4>${table_index  +1}: ${table.dataset.title} : ${table.displayName}
                        <a class="showhide accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapse${table_index}">(show/hide)</a></h4>
                </div>
                <div id="collapse${table_index}" class="accordion-body collapse in">
                    <div class="accordion-inner">
                        <table class="buttontable">
                            <tbody>
                                <#if leftJoinDataIntegrationFeatureEnabled>
                                    <#assign columns = table.leftJoinColumns>
                                <#else>
                                    <#assign columns = table.sortedDataTableColumns>
                                </#if>
                                <#assign count = 0>
                                <#list columns as column>
                                    <#assign description = ""/>
                                    <#if column?? && column.description??>
                                        <#assign description = column.description />
                                    </#if>
                                    <#if count % numCols == 0>
                                    <tr></#if>
                                    <td width="${(100 / numCols)?floor }%">
                                        <div class="drg ui-corner-all" <#if column.actuallyMapped>data-ontology="${column.mappedOntology.id?c}"</#if>
                                             <#if column.measurementUnit??>data-measurement="${column.measurementUnit}"</#if>
                                             title="${description?html}"
                                             <#if column.columnEncodingType?? && column.columnEncodingType=='COUNT'>data-count="true"</#if>
                                             data-table="${table.id?c}"><span class="columnName"><span class="integrationTableNumber">T${table_index +1}
                                            . </span>
                                <span class="name">${column.displayName}</span>
                                            <#if column.mappedOntology??> <span class="ontology">- ${column.mappedOntology.title}</span></#if>
                                            <input type="hidden" name="integrationColumns[{COLNUM}].columns[{CELLNUM}].id" value="${column.id?c}"/></span>
                                            <#assign count = count+1 />
                                        </div>
                                    </td>
                                    <#if count % numCols == 0></tr></#if>
                                </#list>
                                <#if count % numCols != 0></tr></#if>

                            </tbody>
                        </table>

                    </div>
                </div>

            </#list>
        </div>
        <div>

            <br/><br/>
            <@s.submit value='Next: filter values' cssClass="submitbutton btn btn-primary submitButton" />


        </div>
    </@s.form>
    <form name="autosave" style="display:none;visibility:hidden">
        <textarea id="autosave"></textarea>
    </form>

    <script>

        jQuery(document).ready(function ($) {
            TDAR.integration.initDataIntegration();
        });
    </script>

    <div class="modal hide fade" id="columnSave">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h3>Warning</h3>
        </div>
        <div class="modal-body">
            <p>Please use at least one integration column.</p>
        </div>
        <div class="modal-footer">
            <a href="#" id="modalHide" class="btn btn-primary">Ok</a>
        </div>
    </div>

</body>
</#escape>

<#escape _untrusted as _untrusted?html>

    <#import "/WEB-INF/content/${namespace}/view.ftl" as local_ />
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
    <title>${resource.title}</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <#if includeRssAndSearchLinks??>
        <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
        <#assign rssUrl = "/search/rss?groups[0].fieldTypes[0]=PROJECT&groups[0].projects[0].id=${project.id?c}&groups[0].projects[0].name=${(project.name!'untitled')?url}">
        <@search.rssUrlTag url=rssUrl />
        <@search.headerLinks includeRss=false />
    </#if>

    <@_googleScholarSection />

    <@view.canonical resource />

    <#if local_.head?? && local_.head?is_macro>
        <@local_.head />
    </#if>
</head>


    <@nav.toolbar "${resource.urlNamespace}" "view">
        <#if resource.resourceType.dataTableSupported && editable>
            <#assign disabled = resource.dataTables?size==0 />
            <@nav.makeLink "dataset" "columns" "table metadata" "columns" current true disabled "hidden-tablet hidden-phone"/>
            <@nav.makeLink "dataset" "columns" "metadata" "columns" current true disabled "hidden-desktop"/>
        </#if>

        <#if local_.toolbarAdditions?? && local_.toolbarAdditions?is_macro>
            <@local_.toolbarAdditions />
        </#if>
    </@nav.toolbar>


<div id="datatable-child" style="display:none">
    <p class="">
        You have successfully updated the page that opened this window. What would you like to do now?
    </p>
</div>

    <@view.pageStatusCallout />

<h1 itemprop="name" class="view-page-title">${resource.title!"No Title"}</h1>
    <#if resource.project?? && resource.project.id?? && resource.project.id != -1>

    <div id="subtitle">
        <p>Part of the
            <#if resource.projectVisible || editable>
                <a href="<@s.url value='/project/view'><@s.param name="id" value="${resource.project.id?c}"/></@s.url>">${resource.project.coreTitle}</a>
            <#else>
            ${resource.project.coreTitle}
            </#if>
            <#if resource.project.draft>(DRAFT)</#if> project
        </p></div>
    </#if>

    <#if editor>
    <div data-spy="affix" class="affix  screen adminbox rotate-90"><a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}/admin"/>">ADMIN</a></div>
    </#if>

<p class="meta">
    <@view.showCreatorProxy proxyList=authorshipProxies />
    <#if resource.date?has_content && resource.date != -1 >
        <@view.kvp key="Year" val=resource.date?c />
    </#if>

    <#if copyrightMandatory && resource.copyrightHolder?? >
        <strong>Primary Copyright Holder:</strong>
        <@view.browse resource.copyrightHolder "copyrightHolder" />
    </p>
    </#if>
</p>

<p class="visible-phone"><a href="#sidebar-right">&raquo; Downloads &amp; Basic Metadata</a></p>
<hr class="dbl">

<h2>Summary</h2>
    <@common.description resource.description />

<hr/>
    <#noescape>
        <#if resource.url! != ''>
        <p><strong>URL:</strong><a itemprop="url" href="${resource.url?html}" onclick="TDAR.common.outboundLink(this)"
                                   title="${resource.url?html}"><@common.truncate resource.url?html 80 /></a></p><br/>
        </#if>
    </#noescape>


    <#if local_.afterBasicInfo?? && local_.afterBasicInfo?is_macro>
        <@local_.afterBasicInfo />
    </#if>

    <#if ( resource.hasBrowsableImages && resource.visibleFilesWithThumbnails?size > 0)>
        <@view.imageGallery />
    <br/>
    <hr/>
    </#if>

    <#if resource.resourceType.dataTableSupported>
        <#if (dataset.dataTables?has_content)>
            <#if resource.viewable && authenticated >
            <h3>Browse the Data Set</h3>

                <#if (dataset.dataTables?size > 1)>
                <form>
                    <label for="table_select">Choose Table:</label>
                    <select id="table_select" name="dataTableId" onChange="window.location =  '?dataTableId=' + $(this).val()">
                        <#list dataset.dataTables as dataTable_>
                            <option value="${dataTable_.id?c}" <#if dataTable_.id == dataTable.id>selected </#if>
                                    >${dataTable_.displayName}</option>
                        </#list>
                    </select>
                </form>
                </#if>

            <p><@view.embargoCheck /></p>

            <div class="row">
                <div class="span9">
                    <table id="dataTable" class="dataTable table tableFormat table-striped table-bordered"></table>
                </div>
            </div>
                <#if tdarConfiguration.isXmlExportEnabled()>
                <p class="faims_xml_logo"><a href="/dataset/xml?dataTableId=${dataTable.id?c}" target="_blank">XML</a></p>
                </#if>
            </#if>

        <h3>Data Set Structure</h3>
        <div class="row">
            <div class="span3"><span class="columnSquare measurement"></span> Measurement Column</div>
            <div class="span3"><span class="columnSquare count"></span>Count Column</div>
            <div class="span3"><span class="columnSquare coded"></span>Coded Column</div>
        </div>
        <div class="row">
            <div class="span3"><span class="columnSquare mapped"></span>Mapping Column</div>
            <div class="span6"><span class="columnSquare integration"></span>Integration Column (has Ontology)</div>
        </div>

            <#list dataset.dataTables as dataTable>
            <h4>Table Information: <span>${dataTable.displayName}</span></h4>
            <table class="tableFormat table table-striped table-bordered">
                <thead class='highlight'>
                <tr>
                    <th class="guide">Column Name</th>
                    <th>Data Type</th>
                    <th>Type</th>
                    <th>Category</th>
                    <th>Coding Sheet</th>
                    <th>Ontology</th>
                </tr>
                </thead>
                <#list dataTable.dataTableColumns?sort_by("sequenceNumber") as column>
                    <tr>
                        <#assign typeLabel = ""/>
                        <#if column.measurementUnit?has_content><#assign typeLabel = "measurement"/></#if>
                        <#if column.defaultCodingSheet?has_content><#assign typeLabel = "coded"/></#if>
                        <#if column.defaultOntology?has_content || (column.defaultCodingSheet.defaultOntology)?has_content><#assign typeLabel = "integration"/></#if>
                        <#if column.columnEncodingType?has_content && column.columnEncodingType == 'COUNT'><#assign typeLabel = "count"/></#if>
                        <#if column.mappingColumn?has_content && column.mappingColumn ><#assign typeLabel = "mapped"/></#if>
                        <td class="guide" nowrap><span class="columnSquare ${typeLabel}"></span><b>
                        ${column.displayName}
                        </b></td>
                        <td><#if column.columnDataType??>${column.columnDataType.label}&nbsp;</#if></td>
                        <td><#if column.columnEncodingType??>${column.columnEncodingType.label}</#if>
                            <#if column.measurementUnit??> (${column.measurementUnit.label})</#if> </td>
                        <td>
                            <#if column.categoryVariable??>
                                <#if column.categoryVariable.parent??>
                                ${column.categoryVariable.parent} :</#if> ${column.categoryVariable}
                            <#else>uncategorized</#if> </td>
                        <td>
                            <#if column.defaultCodingSheet??>
                                <a href="<@s.url value="/coding-sheet/${column.defaultCodingSheet.id?c}" />">
                                ${column.defaultCodingSheet.title!"no title"}</a>
                            <#else>none</#if>
                        </td>
                        <td>
                            <@_printOntology column />
                        </td>
                    </tr>
                </#list>
            </table>
                <#if dataset.relationships?size != 0>
                <h4>Data Table Relationships:</h4>
                <table class="tableFormat table table-striped table-bordered">
                    <thead class="highlight">
                    <tr>
                        <th>Type</th>
                        <th>Local Table</th>
                        <th>Foreign Table</th>
                        <th>Column Relationships</th>
                    </tr>
                    </thead>
                    <#list dataset.relationships as relationship>
                        <tr>
                            <td>${relationship.type}</td>
                            <td>${relationship.localTable.displayName}</td>
                            <td>${relationship.foreignTable.displayName}</td>
                            <td>
                                <#list relationship.columnRelationships as colRel>
                                ${colRel.localColumn.displayName} <i class="icon-arrow-right"></i> ${colRel.foreignColumn.displayName}
                                </#list>
                            </td>
                        </tr>
                    </#list>
                </table>
                </#if>

            </#list>
        </#if>
    </#if>

    <#macro _printOntology column>
        <#local ont="" />
        <#if column.defaultOntology?? >
            <#local ont = column.defaultOntology/>
        <#elseif (column.defaultCodingSheet.defaultOntology)?has_content>
            <#local ont = column.defaultCodingSheet.defaultOntology />
        </#if>
        <#if ont?has_content>
        <a href="<@s.url value="/ontology/${ont.id?c}"/>">
        ${ont.title!"no title"}</a>
        <#else>
        none
        </#if>
    </#macro>


<h2>Cite this Record</h2>
<div class="citeMe">
	<#assign citation>
${resource.title}. <#if resource.formattedAuthorList?has_content>${resource.formattedAuthorList}.</#if>
${resource.formattedSourceInformation!''} (${siteAcronym} ID: ${resource.id?c})  <#if resource.externalId?has_content>; ${resource.externalId}</#if>
    </#assign>
    <p class="sml">
		${citation}
        <#if !resource.externalId?has_content && resource.lessThanDayOld && !resource.citationRecord>
            <br/>
            <em>Note:</em>A DOI will be generated <#if resource.draft>when this resource is no longer a draft<#else> in the next day for this resource</#if>.
        </#if>
    </p>
   <div class="links">
<ul class="inline">
<#assign url>http://${hostName}<#if hostPort != 80>:${hostPort}</#if>/${currentUrl?url}</#assign>
<li><a href="https://twitter.com/share" onClick="TDAR.common.registerShare('twitter','${currentUrl}','${id?c}')" >Tweet this</a></li>
<li><a href="http://www.facebook.com/sharer/sharer.php?u=${url?url}&amp;t=${resource.title?url}" onClick="TDAR.common.registerShare('facebook','${currentUrl}','${id?c}')">Share on Facebook</a></li>
<#noescape>
<li><a href="mailto:?subject=${resource.title?url}d&amp;body=${citation?trim?url}%0D%0A%0D%0A${url}" onClick="TDAR.common.registerShare('email','${currentUrl}','${id?c}')">Email a link to a Friend</a></li>
</#noescape>
</ul></div>
</div>
<hr/>

    <#if resource.resourceType == 'CODING_SHEET' ||  resource.resourceType == 'ONTOLOGY'>
        <@view.categoryVariables />
    </#if>
    <#if resource.resourceType != 'PROJECT'>
        <#if licensesEnabled?? &&  licensesEnabled>
            <@view.license />
        </#if>
    </#if>

    <@view.coin resource/>

    <#if resource.containsActiveKeywords >
    <h2>Keywords</h2>
        <#if resource.projectVisible?? && !resource.projectVisible && resource.inheritingSomeMetadata>
        <em>Note: Inherited values from this project are not available because the project is not active</em>
        </#if>
    <div class="row">
        <#if (resource.keywordProperties?size > 1)>
        <div class="span45">
        <#elseif resource.keywordProperties?size == 1>
        <div class="span9">
        </#if>

        <#list resource.keywordProperties as prop>
        <#-- FIXME: somehow this should be folded into SearchFieldType to not have all of this if/else -->
            <#if ((resource.keywordProperties?size /2)?ceiling == prop_index)>
            </div>
            <div class="span45">
            </#if>
            <#if prop == "activeSiteNameKeywords">
                <@_keywordSection "Site Name" resource.activeSiteNameKeywords "siteNameKeywords" />
            </#if>
            <#if prop == "activeSiteTypeKeywords">
                <@_keywordSection "Site Type" resource.activeSiteTypeKeywords "uncontrolledSiteTypeKeywords" />
            </#if>
            <#if prop == "activeCultureKeywords">
                <@_keywordSection "Culture" resource.activeCultureKeywords "uncontrolledCultureKeywords" />
            </#if>
            <#if prop == "activeMaterialKeywords">
                <@_keywordSection "Material" resource.activeMaterialKeywords "query" />
            </#if>
            <#if prop == "activeInvestigationTypes">
                <@_keywordSection "Investigation Types" resource.activeInvestigationTypes "query" />
            </#if>
            <#if prop == "activeOtherKeywords">
                <@_keywordSection "General" resource.activeOtherKeywords "query" />
            </#if>
            <#if prop == "activeTemporalKeywords">
                <@_keywordSection "Temporal Keywords" resource.activeTemporalKeywords "query" />
            </#if>
            <#if prop == "activeGeographicKeywords">
                <@_keywordSection "Geographic Keywords" resource.activeGeographicKeywords "query" />
            </#if>
        </#list>
        <#if (resource.keywordProperties?size > 0)>
        </div>
        </#if>
    </div>
    <hr/>
    </#if>


    <#macro _keywordSection label keywordList searchParam>
        <#if keywordList?has_content>
        <p>
            <strong>${label}</strong><br>
            <@view.keywordSearch keywordList searchParam false />
        </p>
        </#if>
    </#macro>

    <#if resource.activeCoverageDates?has_content>
    <h2>Temporal Coverage</h2>
        <#list resource.activeCoverageDates as coverageDate>
            <#assign value>
                <#if coverageDate.startDate?has_content>${coverageDate.startDate?c}<#else>?</#if> to
                <#if coverageDate.endDate?has_content>${coverageDate.endDate?c}<#else>?</#if>
                <#if (coverageDate.description?has_content)> (${coverageDate.description})</#if>
            </#assign>
            <@view.kvp key=coverageDate.dateType.label val=value />
        </#list>
    <hr/>
    </#if>


    <#if (resource.activeLatitudeLongitudeBoxes?has_content )>
    <h2>Spatial Coverage</h2>
    <div class="title-data">
        <p>
            min long: ${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLongitude}; min
            lat: ${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLatitude} ;
            max long: ${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLongitude}; max
            lat: ${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLatitude} ;
            <!-- ${resource.firstActiveLatitudeLongitudeBox.scale } -->
            <!-- ${resource.managedGeographicKeywords } -->
            <#if userAbleToViewUnobfuscatedMap>
                <#if resource.firstActiveLatitudeLongitudeBox.actuallyObfuscated!false> [obfuscated]</#if>
            </#if>
        </p>
    </div>

    <div class="row">
        <div id='large-google-map' class="google-map span9"></div>
    </div>
    <div id="divCoordContainer" style="display:none">
        <input type="hidden" class="ne-lat" value="${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLatitude}" id="maxy"/>
        <input type="hidden" class="sw-lng" value="${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLongitude}" id="minx"/>
        <input type="hidden" class="ne-lng" value="${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLongitude}" id="maxx"/>
        <input type="hidden" class="sw-lat" value="${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLatitude}" id="miny"/>
    </div>
    </#if>
    <#if creditProxies?has_content >
    <h3>Individual &amp; Institutional Roles</h3>
        <@view.showCreatorProxy proxyList=creditProxies />
    <hr/>
    </#if>

    <#if ! resource.activeResourceAnnotations.isEmpty()>
    <h3>Record Identifiers</h3>

        <#list allResourceAnnotationKeys as key>
            <#assign contents = "" />
            <#list resource.activeResourceAnnotations as ra>
                <#if key.id == ra.resourceAnnotationKey.id >
                    <#assign contents><#noescape>${contents}<#t/></#noescape><#if contents?has_content>; </#if>${ra.value}<#t/></#assign>
                </#if>
            </#list>
            <#if contents?has_content>
                <#assign keyLabel>${key.key}(s)</#assign>
                <@view.kvp key=keyLabel val=contents noescape=true />
            </#if>
        </#list>
    </#if>


    <#if resource.activeResourceNotes?has_content>
    <h2>Notes</h2>
        <#list resource.activeResourceNotes.toArray()?sort_by("sequenceNumber") as resourceNote>
            <@view.kvp key=resourceNote.type.label val=resourceNote.note />
        </#list>
    <hr/>
    </#if>

<#-- <@_relatedSimpleItem resource.sourceCitations "Source Citations"/> -->
<#-- <@_relatedSimpleItem resource.relatedCitations "Related Citations"/> -->
    <@_relatedSimpleItem resource.activeSourceCollections "Source Collections"/>
    <@_relatedSimpleItem resource.activeRelatedComparativeCollections "Related Comparative Collections" />
    <#if resource.activeSourceCollections?has_content || resource.activeRelatedComparativeCollections?has_content>
    <hr/>
    </#if>
<#-- display linked data <-> ontology nodes -->
    <#if relatedResources?? && !relatedResources.empty>
    <h3>This ${resource.resourceType.label} is Used by the Following Datasets:</h3>
    <ol style='list-style-position:inside'>
        <#list relatedResources as related >
            <li><a href="<@s.url value="/${related.urlNamespace}/${related.id?c}"/>">${related.id?c} - ${related.title} </a></li>
        </#list>
    </ol>
    </#if>

    <@view.unapiLink resource />
    <#if !viewableResourceCollections.empty>
    <h3>This Resource is Part of the Following Collections</h3>
    <p>
        <#list viewableResourceCollections as collection>
            <a href="<@s.url value="/collection/${collection.id?c}"/>">${collection.name}</a> <br/>
        </#list></p>
    <hr/>
    </#if>

<#--emit additional dataset metadata as a list of key/value pairs  -->
    <#if resource.resourceType != 'PROJECT'>
        <#assign map = resource.relatedDatasetData />
        <#if map?? && !map.empty>
        <h3>Additional Metadata</h3>
            <#list map?keys as key>
                <#if key?? && map.get(key)?? && key.visible?? && key.visible>
                    <@view.kvp key=key.displayName!"unknown field" val=map.get(key)!"unknown value" />
                </#if>
            </#list>
        </#if>
    </#if>
    <#if !resource.resourceType.project>
        <@view.extendedFileInfo />
    </#if>
    <#if local_.afterFileInfo?? && local_.afterFileInfo?is_macro>
        <@local_.afterFileInfo />
    </#if>
    <@view.accessRights>
    <div>
        <#if resource.embargoedFiles?? && !resource.embargoedFiles>
            The file(s) attached to this resource are <b>not</b> publicly accessible.
            They will be released to the public domain in the future</b>.
        </#if>
    </div>
    </@view.accessRights>


<div id="sidebar-right" parse="true">
    <i class="${resource.resourceType?lower_case}-bg-large"></i>
    <#if !resource.resourceType.project>
        <@view.uploadedFileInfo />
    </#if>
    <h3>Basic Information</h3>

    <p>

    <ul class="unstyled-list">
        <#if resource.resourceProviderInstitution?? && resource.resourceProviderInstitution.id != -1>
            <li>
                <strong>Resource Provider</strong><br>
                <@view.browse creator=resource.resourceProviderInstitution />
            </li>
        </#if>

        <#if local_.sidebarDataTop?? && local_.sidebarDataTop?is_macro>
            <@local_.sidebarDataTop />
        </#if>
        <#if (((resource.publisher.name)?has_content ||  resource.publisherLocation?has_content) && !((resource.resourceType.document)!false) )>
            <li><strong>
            <#-- label -->
                <#if resource.documentType?has_content>
                ${resource.documentType.publisherName}
                <#else>
                    Publisher
                </#if></strong><br>
                <#if resource.publisher?has_content><span itemprop="publisher"><@view.browse creator=resource.publisher /></span></#if>
                <#if resource.degree?has_content>${resource.degree.label}</#if>
                <#if resource.publisherLocation?has_content> (${resource.publisherLocation}) </#if>
            </li>
        </#if>
        <#if local_.sidebarDataBottom?? && local_.sidebarDataBottom?is_macro>
            <@local_.sidebarDataBottom />
        </#if>
        <#if resource.resourceLanguage?has_content>
            <li>
                <strong>Language</strong><br>
            ${resource.resourceLanguage.label}
            </li>
        </#if>
        <#if resource.copyLocation?has_content>
            <li>
                <strong>Location</strong><br>
            ${resource.copyLocation}
            </li>
        </#if>
        <li>
            <strong>${siteAcronym} ID</strong><br>
        ${resource.id?c}
        </li>
    </ul>
</div>



    <#if local_.footer?? && local_.footer?is_macro>
        <@local_.footer />
    </#if>


<script type="text/javascript">
    $(function () {
        'use strict';
        TDAR.common.initializeView();
        <#if resource.resourceType.dataTableSupported>
            <#if (dataset.dataTables?has_content)>
                jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = 3;
                $.extend($.fn.dataTableExt.oStdClasses, {
                    "sWrapper": "dataTables_wrapper form-inline"
                });

//        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
                var options = {
                    "sAjaxDataProp": "results.results",
                    "sDom": "<'row'<'span6'l><'span3'>r>t<'row'<'span4'i><'span5'p>>",
                    "bProcessing": true,
                    "bServerSide": true,
                    "bScrollInfinite": false,
                    "bScrollCollapse": true,
                    tableSelector: '#dataTable',
                    sPaginationType: "bootstrap",
                    sScrollX: "100%",
                    //turn off vertical scrolling since we're paging (feels weird to advance through records using two mechanisms)
                    "sScrollY": "",
                    "aoColumns": [
                        <#assign offset=0>
                        <#if viewRowSupported>
                            { "bSortable": false,
                                "sName": "id_row_tdar",
                                "sTitle": '<i class="icon-eye-open  icon-white"></i>',
                                "fnRender": function (obj) {
                                    return '<a href="/${resource.urlNamespace}/view-row?id=${resource.id?c}&dataTableId=${dataTable.id?c}&rowId=' + obj.aData[${offset}] + '" title="View row as page..."><i class="icon-list-alt"></i></a></li>';
                                }
                            },
                            <#assign offset=1>
                        </#if>
                        <#list dataTable.dataTableColumns?sort_by("sequenceNumber") as column>
                            <#if column.visible?? && column.visible>
                                { "bSortable": false,
                                    "sName": "${column.jsSimpleName?js_string}",
                                    "sTitle": "${column.displayName?js_string}",
                                    "fnRender": function (obj) {
                                        var val = obj.aData[${column_index?c} + ${offset}];
                                        var str = TDAR.common.htmlEncode(val);
                                        return str;
                                    }
                                }<#if column_has_next >,</#if>
                            </#if>
                        </#list>
                    ],
                    "sAjaxSource": "<@s.url value="/datatable/browse?id=${dataTable.id?c}" />"
                };
                TDAR.datatable.registerLookupDataTable(options);
            </#if>
        </#if>
        <#if local_.localJavascript?? && local_.localJavascript?is_macro>
            <@local_.localJavascript />
        </#if>

    });
</script>

<#--emit a list of related items (e.g. list of source collections or list of comparative collections -->
    <#macro _relatedSimpleItem listitems label>
        <#if ! listitems.isEmpty()>
        <h3>${label}</h3>
        <table>
            <#list listitems as citation>
                <tr>
                    <td>${citation}</td>
                </tr>
            </#list>
        </table>
        </#if>
    </#macro>

    <#macro _keywordSection label keywordList searchParam>
        <#if keywordList?has_content>
        <p>
            <strong>${label}</strong><br>
            <@view.keywordSearch keywordList searchParam false />
        </p>
        </#if>
    </#macro>

    <#macro _googleScholarSection>
        <#if resource.title?? && resource.resourceCreators?? && resource.date??>
        <meta name="citation_title" content="${resource.title?html}">
            <#list resource.primaryCreators?sort_by("sequenceNumber") as resourceCreator>
            <meta name="citation_author" content="${resourceCreator.creator.properName?html}">
            </#list>
        <meta name="citation_date" content="${resource.date?c!''}">
            <#if resource.dateCreated??>
            <meta name="citation_online_date" content="${resource.dateCreated?date?string('yyyy/MM/dd')}"></#if>
            <#list resource.informationResourceFiles as irfile>
                <#if (irfile.viewable) && irfile.latestPDF?has_content>
                <meta name="citation_pdf_url" content="<@s.url value='/filestore/${irfile.latestPDF.id?c}/get'/>">
                </#if>
            </#list>
            <#assign publisherFieldName = "DC.publisher" />
            <#if resource.resourceType.document>
                <#if document.documentType == 'CONFERENCE_PRESENTATION'>
                    <#assign publisherFieldName="citation_conference_title" />
                <#elseif document.documentType == 'JOURNAL_ARTICLE' && document.journalName??>
                <meta name="citation_journal_title" content="${document.journalName?html}">
                </#if>
                <#if document.volume?has_content>
                <meta name="citation_volume" content="${document.volume}"></#if>
                <#if document.journalNumber?has_content>
                <meta name="citation_issue" content="${document.journalNumber}"></#if>
                <#if document.issn?has_content>
                <meta name="citation_issn" content="${document.issn}"></#if>
                <#if document.isbn?has_content>
                <meta name="citation_isbn" content="${document.isbn}"></#if>
                <#if document.startPage?has_content>
                <meta name="citation_firstpage" content="${document.startPage}"></#if>
                <#if document.endPage?has_content>
                <meta name="citation_lastpage" content="${document.endPage}"></#if>
                <#if document.documentType == 'THESIS'>
                    <#assign publisherFieldName="citation_dissertation_institution" />
                </#if>
            </#if>
            <#if resource.publisher?has_content>
            <meta name="${publisherFieldName}" content="${resource.publisher.name?html}">
            </#if>

        <#else>
        <!--required google scholar fields not available - skipping meta tags -->
        </#if>
    </#macro>
</#escape>
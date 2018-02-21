<#escape _untrusted as _untrusted?html>

    <#import "/WEB-INF/content/${resource.urlNamespace}/view.ftl" as local_ />
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/common-rights.ftl" as rights>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/common.ftl" as common>

<#assign hasProject = (resource.project?? && resource.project.id?? && resource.project.id != -1)/>
<head>
    <title>${resource.title}<#if hasProject> from ${resource.project.title}</#if> <#if (resource.primaryCreators?size > 0)> (${ resource.primaryCreators[0].creator.properName})</#if> | ${siteName}</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <#if includeRssAndSearchLinks??>
        <#import "/WEB-INF/macros/search-macros.ftl" as search>
        <#assign rssUrl = "/api/search/rss?groups[0].fieldTypes[0]=PROJECT&groups[0].projects[0].id=${resource.id?c}&groups[0].projects[0].name=${(resource.name!'untitled')?url}">
        <@search.rssUrlTag url=rssUrl />
        <@search.headerLinks includeRss=false />
    </#if>

    <#noescape>
    ${googleScholarTags}
    </#noescape>

    <link rel="alternate" href="/api/lod/resource/${id?c}" type="application/ld+json" />    
    <@view.canonical resource />

    <#if local_.head?? && local_.head?is_macro>
        <@local_.head />
    </#if>

</head>


    <@nav.toolbar "${resource.urlNamespace}" "view">
        <#if resource.resourceType.dataTableSupported && editable>
            <#assign disabled = (resource.dataTables?size==0 || resource.totalNumberOfActiveFiles == 0) />
            <@nav.makeLink "dataset" "columns/${persistable.id?c}" "table metadata" "columns" current true disabled "hidden-tablet hidden-phone"/>
            <@nav.makeLink "dataset" "columns/${persistable.id?c}" "metadata" "columns" current true disabled "hidden-desktop"/>
            <#if mappingFeatureEnabled >
            <@nav.makeLink "dataset" "resource-mapping" "res. mapping" "columns" current true disabled ""/>
            </#if>
        </#if>

        <#if local_.toolbarAdditions?? && local_.toolbarAdditions?is_macro>
            <@local_.toolbarAdditions />
        </#if>
    </@nav.toolbar>

        <#if local_.notifications?? && local_.notifications?is_macro>
            <@local_.notifications />
        </#if>

<div id="datatable-child" style="display:none">
    <p class="">
        You have successfully updated the page that opened this window. What would you like to do now?
    </p>
</div>

    <@view.pageStatusCallout />

    <h1 class="view-page-title">${resource.title!"No Title"}</h1>
    <#if hasProject>

    <div id="subtitle">
        <p>Part of the
            <#if resource.projectVisible || editable>
                <a href="<@s.url value='${resource.project.detailUrl}'/>">${resource.project.coreTitle}</a>
            <#else>
            ${resource.project.coreTitle}
            </#if>
            <#if resource.project.draft>(DRAFT)</#if> project
        </p></div>
    </#if>

    <#if editor>
    <div data-spy="affix" class="affix  screen adminbox rotate-90"><a href="<@s.url value="/resource/admin?id=${resource.id?c}"/>">ADMIN</a></div>
    </#if>

<p class="meta">
    <@view.showCreatorProxy proxyList=authorshipProxies />
    <#if resource.date?has_content>
	    <#assign dateval = "unknown" />
	    <#if  (resource.date?has_content &&  resource.date > -1 )>
	    	<#assign dateval = resource.date?c />
		</#if>
        <@view.kvp key="Year" val=dateval />
    </#if>

    <#if config.copyrightMandatory && resource.copyrightHolder?? || resource.copyrightHolder?has_content >
        <strong>Primary Copyright Holder:</strong>
        <@view.browse resource.copyrightHolder "copyrightHolder" />
    </p>
    </#if>

</p>

<p class="visible-phone"><a href="#sidebar-right">&raquo; Downloads &amp; Basic Metadata</a></p>

<h2>Summary</h2>
    <@common.description resource.description />
<hr>
    <#list viewableResourceCollections>
    <h3>This Resource is Part of the Following Collections</h3>
    <p>
    <ul class="inline">
        <#items as collection>
        <li>
         <a class="sml moreInfo" data-type="collection" data-size="${((collection.managedResources![])?size!0 + (collection.unmanagedResources![])?size!0)?c}" data-hidden="${collection.hidden?c}" 
        		data-submitter="${collection.submitter.properName}" data-description="<@common.truncate collection.description!'no description' />"
        		data-name="${collection.name!''}" href="<@s.url value="${collection.detailUrl}"/>">${collection.name}</a>
        <#sep>&nbsp;&nbsp;&bull;</#sep></li>
</#items>
</ul>
</p>
<hr>
</#list>


<@view.resourceCitationSection resource />

<hr/>
    <#noescape>
        <#if resource.url! != ''>
        <p><strong>URL: </strong><a href="${resource.url?html}" onclick="TDAR.common.outboundLink(this)" rel="nofollow"
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
        <#if (resource.dataTables?has_content)>
            <#if resource.viewable && authenticated && (resource.publicallyAccessible || ableToViewConfidentialFiles)>
            <h3 id="browseTable" data-namespace="${namespace}">Browse ${resource.title}</h3>

                <#if (resource.dataTables?size > 1)>
                <form>
                    <label for="table_select">Choose Table:</label>
                    <select id="table_select" name="dataTableId">
                        <#list resource.importSortedDataTables as dataTable_>
                            <option value="${dataTable_.id?c}" <#if dataTable_.id == dataTable.id>selected </#if>
                                    >${dataTable_.displayName}</option>
                        </#list>
                    </select>
                </form>
                </#if>

            <p><@view.embargoCheck /></p>

            <div class="row">
                <div class="span9">
                    <table id="dataTable"
                           data-data-table-selector="#table_select"
                           data-default-data-table-id="${dataTable.id?c}"
                           data-resource-id="${resource.id?c}"
                           class="dataTable table tableFormat table-striped table-bordered"></table>
                </div>
            </div>
                <#if config.xmlExportEnabled>
                <p class="faims_xml_logo"><a href="<@s.url value="/dataset/xml?dataTableId=${dataTable.id?c}"/>" target="_blank">XML</a></p>
                </#if>
            <#else>

                <p><@view.embargoCheck /></p>

            </#if>

        <h3>Data Set Structure</h3>
        <div class="row">
            <div class="span3"><span class="columnSquare measurement"></span>Measurement Column</div>
            <div class="span3"><span class="columnSquare count"></span>Count Column</div>
            <div class="span3"><span class="columnSquare coded"></span>Coded Column</div>
        </div>
        <div class="row">
            <div class="span3"><span class="columnSquare mapped"></span>Filename Column</div>
            <div class="span3"><span class="columnSquare integration"></span>Integration Column (has Ontology)</div>
        </div>
    <br/>
            <#list resource.sortedDataTables as dataTable>
            <h4>Table Information: <span>${dataTable.displayName}</span></h4>
            <#if dataTable.description?has_content>
            <p class="tableDescription">${dataTable.description}</p>
            </#if>
            <table class="tableFormat table table-bordered">
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
                <#list dataTable.dataTableColumns as column>
                <#assign oddEven="oddC" />
                <#if column_index % 2 == 0>
                    <#assign oddEven="evenC" />
                </#if>
                    <tr>
                        <#assign typeLabel = ""/>
                        <#if column.measurementUnit?has_content><#assign typeLabel = "measurement"/></#if>
                        <#if column.defaultCodingSheet?has_content><#assign typeLabel = "coded"/></#if>
                        <#if (column.defaultCodingSheet.defaultOntology)?has_content><#assign typeLabel = "integration"/></#if>
                        <#if column.columnEncodingType?has_content && column.columnEncodingType.count><#assign typeLabel = "count"/></#if>
                        <#if column.filenameColumn ><#assign typeLabel = "mapped"/></#if>
                        <#assign hasDescription = false />
                        <#if column.description?has_content >
                            <#assign hasDescription = true />
                        </#if>


                        <td class="guide" nowrap <#if hasDescription>rowspan=2</#if>><span class="columnSquare ${typeLabel}"></span><b>
                        ${column.displayName}
                        </b></td>
                        <#if hasDescription>
                            <td colspan="6" class="${oddEven} descriptionRow" >${column.description}</td></tr><tr>
                        </#if>

                        <td class="${oddEven}"><#if column.columnDataType??>${column.columnDataType.label}&nbsp;</#if></td>
                        <td class="${oddEven}"><#if column.columnEncodingType??>${column.columnEncodingType.label}</#if>
                            <#if column.measurementUnit??> (${column.measurementUnit.label})</#if> </td>
                        <td class="${oddEven}">
                            <#if column.categoryVariable??>
                                <#if column.categoryVariable.parent??>
                                ${column.categoryVariable.parent} :</#if> ${column.categoryVariable}
                            <#else>uncategorized</#if> </td>
                        <td class="${oddEven}">
                            <#if column.defaultCodingSheet??>
                                <a href="<@s.url value="/coding-sheet/${column.defaultCodingSheet.id?c}" />">
                                ${column.defaultCodingSheet.title!"no title"}</a>
                            <#else>none</#if>
                        </td>
                        <td class="${oddEven}">
                            <@_printOntology column />
                        </td>
                    </tr>
                </#list>
            </table>
            </#list>
                <#if resource.relationships?size != 0>
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
                    <#list resource.relationships as relationship>
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

        </#if>
<hr/>    </#if>

    <#macro _printOntology column>
        <#local ont="" />
        <#if (column.defaultCodingSheet.defaultOntology)?has_content>
            <#local ont = column.defaultCodingSheet.defaultOntology />
        </#if>
        <#if ont?has_content>
        <a href="<@s.url value="/ontology/${ont.id?c}"/>">
        ${ont.title!"no title"}</a>
        <#else>
        none
        </#if>
    </#macro>




    <#if resource.resourceType.supporting >
        <@view.categoryVariables />
    </#if>
    <#if !resource.resourceType.project >
        <#if config.licensesEnabled?? &&  config.licensesEnabled || resource.licenseType?has_content >
            <@view.license />
        </#if>
    </#if>

    <span class="Z3988" title="<#noescape>${openUrl!""}</#noescape>"></span>

    <#if resource.containsActiveKeywords >
    <h2>Keywords</h2>
        <#if resource.project?has_content && resource.project.id != -1 && resource.projectVisible?? && !resource.projectVisible && resource.inheritingSomeMetadata>
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
                <@_keywordSection "Site Name" resource.activeSiteNameKeywords "siteNameKeywords" resource.inheritingSiteInformation!false />
            </#if>
            <#if prop == "activeSiteTypeKeywords">
                <@_keywordSection "Site Type" resource.activeSiteTypeKeywords "uncontrolledSiteTypeKeywords" resource.inheritingSiteInformation!false />
            </#if>
            <#if prop == "activeCultureKeywords">
                <@_keywordSection "Culture" resource.activeCultureKeywords "uncontrolledCultureKeywords" resource.inheritingCulturalInformation!false />
            </#if>
            <#if prop == "activeMaterialKeywords">
                <@_keywordSection "Material" resource.activeMaterialKeywords "query" resource.inheritingMaterialInformation!false />
            </#if>
            <#if prop == "activeInvestigationTypes">
                <@_keywordSection "Investigation Types" resource.activeInvestigationTypes "query" resource.inheritingInvestigationInformation!false />
            </#if>
            <#if prop == "activeOtherKeywords">
                <@_keywordSection "General" resource.activeOtherKeywords "query" resource.inheritingOtherInformation!false />
            </#if>
            <#if prop == "activeTemporalKeywords">
                <@_keywordSection "Temporal Keywords" resource.activeTemporalKeywords "query" resource.inheritingTemporalInformation!false />
            </#if>
            <#if prop == "activeGeographicKeywords">
                <@_keywordSection "Geographic Keywords" resource.activeGeographicKeywords "query" resource.inheritingSpatialInformation!false />
            </#if>
        </#list>
        <#assign mks = (resource.activeManagedGeographicKeywords![])?size />
        <#if editor && (mks > 0)>
        <p>
            <strong>System Managed Geographic Keywords (${mks})</strong><br><span class="show red" onClick="$(this).hide();$('#managedKeywords').show()">show</span><span id="managedKeywords" style="display:none">
            <@view.keywordSearch resource.activeManagedGeographicKeywords "query"  /></span>
        </p>
        </#if>
        <#if (resource.keywordProperties?size > 0)>
        </div>
        </#if>
    </div>
    <hr/>
    </#if>


    <#macro _keywordSection label keywordList searchParam inherited=false >
        <#if keywordList?has_content>
        <p class="break-word">
            <strong>${label} <#if editor && inherited><small>(from project)</small></#if> </strong><br>
            <@view.keywordSearch keywordList searchParam false />
        </p>
        </#if>
    </#macro>

        <#list resource.activeCoverageDates>
        <h2>Temporal Coverage <#if editor && resource.inheritingTemporalInformation!false><small>(from project)</small></#if> </h2>
        <#items as coverageDate>
            <#assign value>
                <#if coverageDate.startDate?has_content>${coverageDate.startDate?c}<#else>?</#if> to
                <#if coverageDate.endDate?has_content>${coverageDate.endDate?c}<#else>?</#if>
                <#if (coverageDate.description?has_content)> (${coverageDate.description})</#if>
            </#assign>
            <@view.kvp key=coverageDate.dateType.label val=value />
            </#items>
            <hr/>
        </#list>


    <#if (resource.activeLatitudeLongitudeBoxes?has_content) || (userAbleToViewUnobfuscatedMap && geoJson?has_content)>
    <h2>Spatial Coverage <#if editor && resource.inheritingSpatialInformation!false><small>(from project)</small></#if> </h2>
    <div class="title-data">
        <#if (resource.activeLatitudeLongitudeBoxes?has_content) >
            <#assign llb = resource.firstActiveLatitudeLongitudeBox />
            <p>
            min long: ${llb.obfuscatedWest}; min
            lat: ${llb.obfuscatedSouth} ;
            max long: ${llb.obfuscatedEast}; max
            lat: ${llb.obfuscatedNorth} ;
            <!-- ${llb.scale } -->
            <!-- ${resource.managedGeographicKeywords } -->
            <#if userAbleToViewUnobfuscatedMap>
                <#if llb.obfuscatedObjectDifferent> [obfuscated]</#if>
            </#if>
        </p>
        </#if>
    </div>

    <div class="row">
        <div id='large-map' style="height:300px" class="leaflet-map span9" 
        <#if userAbleToViewUnobfuscatedMap && geoJson?has_content>data-geojson="#localGeoJson"</#if>
        <#if (resource.activeLatitudeLongitudeBoxes?has_content)>
            <#assign llb = resource.firstActiveLatitudeLongitudeBox />
            data-maxy="${llb.obfuscatedNorth}"
            data-minx="${llb.obfuscatedWest}"
            data-maxx="${llb.obfuscatedEast}"
            data-miny="${llb.obfuscatedSouth}"
        <#-- disabled for Obsidian
        <#if resource.confidentialViewable && llb.obfuscatedObjectDifferent >
            data-real-maxy="${llb.east}"
            data-real-minx="${llb.south}"
            data-real-maxx="${llb.north}"
            data-real-miny="${llb.west}"
        </#if> -->
        </#if>
        ></div>
    </div>
    <#if userAbleToViewUnobfuscatedMap && geoJson?has_content>
        <#noescape>
            <script id="localGeoJson" type="application/json">${geoJson}</script>
        </#noescape>
    </#if>
    </#if>
    <#if creditProxies?has_content >
    <h3>Individual &amp; Institutional Roles <#if editor && resource.inheritingIndividualAndInstitutionalCredit!false ><small>(from project)</small></#if> </h3>
        <@view.showCreatorProxy proxyList=creditProxies />
    <hr/>
    </#if>

        <#list allResourceAnnotationKeys>
        <h3>Record Identifiers <#if editor && resource.inheritingIdentifierInformation!false ><small>(from project)</small></#if> </h3>

        <#items as key>
            <#assign contents = "" />
            <#list resource.activeResourceAnnotations as ra>
                <#if key.id == ra.resourceAnnotationKey.id >
                    <#assign contents><#noescape>${contents}<#t/></#noescape><#if contents?has_content>; </#if>${ra.value}<#t/></#assign>
                </#if>
            </#list>
            <#if contents?has_content>
                <#assign keyLabel><#noescape>${key.key}</#noescape>(s)</#assign>
                <@view.kvp key=keyLabel val=contents noescape=true />
            </#if>
            </#items>
        </#list>


    <#list resource.activeResourceNotes.toArray()?sort_by("sequenceNumber")>
    <h2>Notes <#if editor && resource.inheritingNoteInformation!false ><small>(from project)</small></#if> </h2>
        <#items as resourceNote>
            <@view.kvp key=resourceNote.type.label val=resourceNote.note />
        </#items>
    <hr/>
        </#list>

    <@_relatedSimpleItem resource.activeSourceCollections "Source Collections"/>
    <@_relatedSimpleItem resource.activeRelatedComparativeCollections "Related Comparative Collections" />
    <#if resource.activeSourceCollections?has_content || resource.activeRelatedComparativeCollections?has_content>
    <hr/>
    </#if>
<#-- display linked data <-> ontology nodes -->
        <#list relatedResources![]>
        <h3>This ${resource.resourceType.label} is Used by the Following Datasets:</h3>
        <ol style='list-style-position:inside'>
            <#items as related >
            <li><a href="<@s.url value="${related.detailUrl}"/>">${related.id?c} - ${related.title} </a></li>
            </#items>
    </ol>
        </#list>

    <@view.unapiLink resource />

<#--emit additional dataset metadata as a list of key/value pairs  -->
    <#if mappedData?has_content >
        <#assign map = mappedData />
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

<#list visibleUnmanagedCollections>
    <h3>This Resource is Part of the Following User Created Collections</h3>
        <ul class="inline">
    <#items as collection>
    
        <li>
            <a class="sml moreInfo" data-type="collection" data-size="${((collection.managedResources![])?size!0 + (collection.unmanagedResources![])?size!0)?c}" data-hidden="${collection.hidden?c}" 
            data-submitter="${collection.submitter.properName}"
            data-description="<@common.truncate collection.description!'no description' />"
            data-name="${collection.name!''}" 
            
            href="<@s.url value="${collection.detailUrl}"/>">${collection.name}</a>
            <#sep>&nbsp;&nbsp;&bull;</#sep>
        </li>
    </#items>
    </ul>
    </p>
    <hr>
</#list>

    <@view.accessRights>
    <div>
        <#if resource.embargoedFiles?? && !resource.embargoedFiles>
            The file(s) attached to this resource are <b>not</b> publicly accessible.
            They will be released to the public domain in the future</b>.
        </#if>
    </div>
    </@view.accessRights>



   <div class="modal hide fade" id="modal">
                <#include 'vue-collection-widget.html' />
    </div>

<div id="sidebar-right" parse="true">
    <div class="beige white-border-bottom">
        <div class="iconbox">
            <svg class="svgicon white svg-dynamic"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_${resource.resourceType?lower_case}"></use></svg>
        </div>
    </div>
    <#if whiteLabelLogoAvailable>
        <@s.a href="/collection/${whiteLabelCollection.id?c}/${whiteLabelCollection.slug}"
            title="${whiteLabelCollection.title}"
            ><img src="${whiteLabelLogoUrl}" class="whitelabel-logo"></@s.a>
    </#if>
    <#if !resource.resourceType.project>
        <@view.uploadedFileInfo />
    <#else>
        <#if resourceTypeFacets?has_content >
        <p>Project Contents</p>
            <@search.facetBy facetlist=resourceTypeFacets label="" facetParam="selectedResourceTypes" link=false liCssClass="" ulClass="inline" icon=false />
        </#if>
    </#if>
        <ul class="media-list">
            <#assign txt><#if !resource.citationRecord>Request Access,</#if> Submit Correction, Comment</#assign>
            <li class="media">
            <i class="icon-comment pull-left"></i>
                <div class="media-body">
                        <a id="requestAccess" href="<@s.url value="/resource/request/${id?c}"/>">${txt}
                    <#if !(authenticatedUser.id)?has_content>
                             (requires login)
                    </#if>
                </a>
                </div>
            </li>
        <#if (authenticatedUser.id)?has_content && editable>
            <@list.bookmarkMediaLink resource />
            <li class="media "><i class="icon-folder-open pull-left"></i>
                <div class="media-body">
                    <a id="addToCollection" href="#modal" data-toggle="modal">Add to a Collection</a>
                </div>
            </li>
        </#if>

            <@nav.shareSection />
        </ul>
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
                <#if resource.publisher?has_content><span><@view.browse creator=resource.publisher /></span></#if>
                <#if resource.degree?has_content>${resource.degree.label}</#if>
                <#if resource.publisherLocation?has_content> (${resource.publisherLocation}) </#if>
            </li>
        </#if>
        <#if resource.doi?has_content>
            <li><strong>DOI</strong><br><a href="http://dx.doi.org/${resource.doi}">${resource.doi}</a></li>
        <#elseif resource.externalId?has_content>
            <li><strong>DOI</strong><br>${resource.externalId}</li>
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

        if ($("#dataTable")){
                TDAR.datatable.initDataTableBrowser();
        }
        if(window._localJavaScript) {
            _localJavaScript();
        }

        //TDAR.internalEmailForm.init();
        <#if authenticated>
        TDAR.vuejs.collectionwidget.init("#add-resource-form");
        </#if>
})
</script>

<#--emit a list of related items (e.g. list of source collections or list of comparative collections -->
    <#macro _relatedSimpleItem listitems label>
        <#list listitems>
        <h3>${label}</h3>
        <table>
            <#items as citation>
                <tr>
                    <td>${citation}</td>
                </tr>
            </#items>
        </table>
        </#list>
    </#macro>

                <div class="modal hide fade" id="modal">
                  <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h3>Add to a Collection</h3>
                  </div>
                  <div class="modal-body">
                  <ul class="collection-list unstyled">
                  </ul>
                  </div>
                  <div class="modal-footer">
                    <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
                    <a href="#" class="btn btn-primary">Save changes</a>
                  </div>
                </div>

</#escape>

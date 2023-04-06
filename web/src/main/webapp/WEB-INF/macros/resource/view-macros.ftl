<#escape _untrusted as _untrusted?html>

<#--
$Id$
View freemarker macros
-->
<#-- include navigation menu in edit and view macros -->
    <#import "common-resource.ftl" as commonr>
    <#import "../common.ftl" as common>
    <#import "../search-macros.ftl" as searchm>
    <#import "../navigation-macros.ftl" as nav>
    <#import "../common-rights.ftl" as rights>
    <#setting url_escaping_charset='UTF-8'>

<#--Emit rel=canonical element.  The "canonical" url points to the preferred version of a set of pages with similar content -->
    <#macro canonical object>
    <#if object.detailUrl?has_content>
        <link rel="canonical" href="https://${hostName}${object.detailUrl}"/>
    <#else>
        <link rel="canonical" href="https://${hostName}/${object.urlNamespace}/${object.id?c}"/>
    </#if>
    </#macro>

<#-- emit ontology as hierarchical list -->
    <#macro ontology sectionTitle="Parsed Ontology Nodes" previewSize=10 triggerSize=15>
        <#if resource.sortedOntologyNodesByImportOrder?has_content>
            <#local allNodes = resource.sortedOntologyNodesByImportOrder />
            <#local size = allNodes?size />
            <#if (size>0)>
            <h2>${sectionTitle}</h2>
            <div id="ontologyNodesContainer" class="ontology-nodes-container">
                <div id="ontology-nodes">
                    <#list allNodes as node>
                    <@_displayNode node previewSize node_index size/>
            </#list>
                </div>
                <#if (size >= previewSize)>
                    <div id='divOntologyShowMore' class="alert">
                        <span>Showing first ${previewSize?c} ontology nodes.</span>
                        <button type="button" class="btn btn-sm" id="btnOntologyShowMore">Show all ${resource.ontologyNodes?size?c} nodes...</button>
                    </div>
                </#if>
            </#if>
        </div>
        </#if>
    </#macro>
    <#macro _displayNode ontologyNode previewSize count size>
        <#if count == 0>
        <ul id="ontology-nodes-root">
        <li>${resource.name}
        <ul></#if>
        <#if !(numParents?has_content)>
            <#global numParents = 1/>
        </#if>
        <#local parentCount = ontologyNode.numberOfParents />
        <#if ontologyNode.index != ontologyNode.intervalStart?string && numParents == parentCount >
        </li>
        </#if>
        <#if (numParents < parentCount)>
            <#list 1.. (parentCount -numParents) as x >
            <ul></#list>
        </#if>
        <#if (parentCount < numParents )>
            <#list 1..(numParents -parentCount) as x >
                </li></ul>
            </#list>
        </#if>
        <#global numParents = parentCount />
    <li class="<#if (previewSize <=count)>hidden-nodes</#if>">
        <a href="<@s.url value="/ontology/${ontologyNode.ontology.id?c}/node/${ontologyNode.slug}"/>">${ontologyNode.displayName!ontologyNode.iri}
            <#if ontologyNode.synonyms?has_content>
                (<#t>
                <#list ontologyNode.synonyms as synonym><#t>
                ${synonym}<#t>
                    <#sep>, </#sep><#t>
                </#list><#t>
                )<#t>
            </#if><!-- (${ontologyNode.index})-->
        </a>
        <#if count == (size -1)>            <#list 1.. (numParents) as x ></li></ul></#list>
        </li></ul>
        </#if>
    </#macro>

<#-- Emit a download link for an information resource file -->
    <#macro createFileLink irfile newline=false showDownloadCount=true showSize=true >
        <#assign version=irfile />
        <#if version.latestUploadedVersion?? >
            <#assign version=version.latestUploadedVersion />
        </#if>
        <#if (version.viewable)>
        <#-- refactor ? -->
        <#local irid = (irfile.informationResource.id)!-1 />
        <#if !irid?has_content || irid == -1 >
            <#local irid = id />
        </#if>
        <#if !irid?has_content>
            <#local irid = -1 />
        </#if>

        <#local path>/filestore/download/${irid?c}/${version.id?c}</#local>
        <a href="<@s.url value='${path}'/>"
            data-file-id="${version.informationResourceFile.id?c}"
            class="download-link download-file"
           onClick="TDAR.common.registerDownload('${path?js_string}', '${id?c}')"
           title="click to download: ${version.filename}">
            <@common.truncate version.filename 65 />
        </a><#if newline><br/></#if>
        <#else>
            <@common.truncate version.filename 65 />
        </#if>
        <#if version.viewable == false>
        <i class="fas fa-lock"></i>
        <#elseif version.informationResourceFile.public == false >
        <i class="fas fa-unlock"></i>
        </#if>
        <#if showSize>(<@common.convertFileSize version.fileLength />)</#if>
        <#if showDownloadCount><@downloadCount version /></#if>
    </#macro>

<#-- similar to createFileLink,  but creates a download link to a zip of containing all accessible files in a resource -->
    <#macro createArchiveFileLink resource newline=false >
    <#--<a href="<@s.url value='/filestore/downloadAllAsZip?informationResourceId=${resource.id?c}'/>" onClick="TDAR.common.registerDownload('/filestore/informationResourceId=${resource.id?c}', '${id?c}')"-->
    <#-- fixme:should we change the google analytics event name, or will this be a pain? -->
        <#if resource.hasConfidentialFiles() && !ableToViewConfidentialFiles >
            Download All        <i class="fas fa-lock"></i>
        <#else>
        <a class="download-link download-zip" href="<@s.url value='/filestore/download/${resource.id?c}'/>" 
           onclick="TDAR.common.registerDownload('/filestore/download?informationResourceId=${resource.id?c}', '${id?c}')"
           title="download all as zip">Download All</a>
        </#if>
    </#macro>

<#--emit links to admin-only dataset actions
    @requires resource.id
    @requires resource.urlNamespace
-->
    <#macro adminFileActions>
        <#if (resource.totalNumberOfFiles?has_content && resource.totalNumberOfFiles > 0)>
            <#if ableToReprocessDerivatives>
            <h2> Admin File Actions</h2>
            <ul>
                <#if resource.resourceType.dataTableSupported>
                <li><a href="<@s.url value='/resource/reimport?id=${resource.id?c}' />">Reimport this dataset</a></li>
                <li><a href="<@s.url value='/resource/retranslate?id=${resource.id?c}' />">Retranslate this dataset</a></li>
                </#if>
            <li><a href="<@s.url value='/resource/reprocess'><@s.param name="id" value="${resource.id?c}"/></@s.url>">Reprocess all derivatives
                for this resource</a></li>
            </#if>
        </#if>
    </#macro>

<#-- emit a list of files for display in a "file information" section.  This macro calls out to #nested for rendering
    html for each individual file
    @requires resource.informationResourceFiles
    @nested irfile:InformationResourceFile the current irFile in the loop
    @nested showAll:String css class that the #nested section should use when rendering an individual file link
-->
    <#macro fileInfoSection extended windowSize=4>
        <#local showAll = ""/>
        <#local visibleCount = 0>
        <#list resource.informationResourceFiles as irfile>
            <#if (visibleCount > windowSize)><#local showAll = "view-hidden-extra-files"/></#if>
            <#if !irfile.deleted><#local visibleCount = 1 + visibleCount /></#if>
            <#nested irfile, showAll>
        </#list>
    </#macro>

<#-- emit download link for translated dataset file -->
    <#macro translatedFileSection irfile>
        <#if irfile.hasTranslatedVersion >
        </li>
<li class="  media ml-0 pl-3 pr-3 mr-0">
    <i class="iconf page-excel mr-2"></i>
                                <div class="media-body">
            <b>Translated version</b> <@createFileLink irfile.latestTranslatedVersion /></br>
            Data column(s) in this dataset have been associated with coding sheet(s) and translated:
            <#if userAbleToReTranslate>
                <br>
                <small>(<a href="<@s.url value='/resource/retranslate'><@s.param name="id" value="${resource.id?c}"/></@s.url>">Retranslate this dataset</a> -
                    <b>Note: this process may take some time</b>)
                </small>
            </#if>
        </div>
        </#if>
    </#macro>

<#-- emit the correct icon for a particular information resource file -->
    <#macro fileIcon irfile=file extraClass="" >
        <#local extensionMap = {
        'pdf':'page-white-acrobat',
        'doc':'page-white-word',
        'docx':'page-white-word' ,
        'mdb':'page-white-key',
        'mdbx':'page-white-key','accdb':'page-white-key',
        'xls':'page-excel','xlsx':'page-excel',
        'zip':'page-white-zip',
        'tar':'page-white-zip',
        'tgz':'page-white-zip',
        'DOCUMENT':'page-white-text',
        'DATASET':'page-white-text',
        'CODING_SHEET':'page-white-text',
        'IMAGE':'page-white-picture',
        'SENSORY_DATA':'page-white-picture',
        'ONTOLOGY':'page-white-text',
        'GEOSPATIAL':'page-white-picture',
        'ARCHIVE':'page-white-zip',
        'AUDIO':'cd',
        'VIDEO': 'film'
        } />
        <#local ext = "" >
        <#if irfile.latestUploadedOrArchivalVersion??>
            <#local ext = extensionMap[irfile.latestUploadedOrArchivalVersion.extension?lower_case ]!'' />
            <#if !ext?has_content>
                <#local ext = extensionMap[resource.resourceType ] />
            </#if>
        </#if>
    <i class="iconf ${ext} ${extraClass!""}"></i>
    </#macro>

<#--show summary information about the uploaded files for th current resource (appears in the right sidebar)
    file list is truncated if it takes up too much space-->
    <#macro uploadedFileInfo >
        <#local showAll = "">
        <#if (resource.totalNumberOfFiles!0) == 0 >
            <h3 class="downloads">Find a Copy</h3>
            <div id="fileSummaryContainer">
                <ul class="downloads media-list  pl-0 ml-0">
                    <li class="citationNote ml-0 pl-3 pr-3 mr-0"><b>We do not have a copy of this ${resource.resourceType.label?lower_case}, it is a citation.</b><#if resource.copyLocation?has_content><br/><br/> The information that we have indicates that a paper copy may be located
                    at ${resource.copyLocation}.</#if></li>
                </ul>
            </div>
        <#else>
            <h3 class="downloads">
                Downloads
                <span class="downloadNumber d-none d-md-block">${resource.totalNumberOfActiveFiles!0?c}</span>
            </h3>
            <div id="fileSummaryContainer">
                <ul class="downloads media-list  pl-0 ml-0">
                    <#if ((resource.totalNumberOfFiles!0) > 0) >

                        <#if resource.hasConfidentialFiles()>
                            <li class="ml-0 pl-3 pr-3 mr-0"><@embargoCheck/></li></#if>
                        <@fileInfoSection extended=false; irfile, showAll>
                            <#local showAll = showAll>
                            <li class="<#if irfile.deleted>view-deleted-file</#if> ${showAll} media ml-0 pl-3 pr-3 mr-0">
                                <@fileIcon irfile=irfile extraClass="mr-2" />
                                <div class="media-body"><@createFileLink irfile true /></div>
                                <@translatedFileSection irfile />
                            </li>
                        </@fileInfoSection>
                        <#if (resource.totalNumberOfActiveFiles > 1)>
                            <li class="archiveLink media ml-0 pl-3 pr-3 mr-0">
                                <i class="iconf page-white-zip ml-1 mr-2"></i>

                                <div class="media-body"><@createArchiveFileLink resource=resource /></div>
                            </li>
                        </#if>

                    </#if>
                </ul>
                <#if showAll != ''>
                    <div id="downloadsMoreArea">
                        <a href="#allfiles">show all files</a>
                    </div>
                </#if>
            </div>
        </#if>
    <script type="text/javascript">
        function _openModal(){
            console.log("open download modal");
            if(!TDAR.getCookie("dlnotify")){
                $('#modalDownloadReminder').modal();
                $('#modalDownloadReminderLink').click(function(){$('#modalDownloadReminder').modal('hide'); return true;});
            } else {
                console.log("dlnotify cookie set - suppressing download notification")
            }
        }

        $(function(){
            $(".download-link").click(function(){
                setTimeout(_openModal, 1000);
            });

            $("#modalDownloadReminder").on('hidden.bs.modal', function(e){
                console.log("setting dlnotify cookie");
                TDAR.setCookie("dlnotify", 1, 90, "/")
            });
        });
    </script>
    <div class="modal fade" id="modalDownloadReminder" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="card" style="background-color: #f4d35e">
                    <div class="card-body">
                        <h2 class="card-title">A Message From Our Director, Dr. Christopher Nicholson</h2>
                        <p><strong>Please <a href="https://www.asufoundation.org/colleges-and-programs/schools-and-colleges/the-college-of-liberal-arts-and-sciences/center-for-digital-antiquity-fund-CA103777.html" onclick="TDAR.common.outboundAppeal('landing');" target="_blank">make a gift now</a> to ensure ${siteAcronym}'s future.</strong></p>
                        <p>Your gift is invested back into ${siteAcronym}'s infrastructure to ensure this community-supported archive is sustainable!</p>

                        <p>
                            Thank you for your partnership! —
                            Chris
                        </p>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-sm btn-secondary" data-dismiss="modal">Close this message</button>
                    <a href="https://www.asufoundation.org/colleges-and-programs/schools-and-colleges/the-college-of-liberal-arts-and-sciences/center-for-digital-antiquity-fund-CA103777.html" target="_blank" id="modalDownloadReminderLink" class="btn btn-sm btn-primary">Show Me More...</a>
                </div>
            </div>
        </div>
    </div>

    </#macro>

<#macro resourceCitationSection resource>
<div class="card lightbeige">
<div class="card-body">
<h2 class="card-title">Cite this Record</h2>
    <p class="sml card-text">
        <#noescape>${resourceCitation.fullCitation!''}</#noescape>
        <#if !resource.externalId?has_content && resource.lessThanDayOld && !resource.citationRecord>
            <br/>
            <em>Note:</em>A DOI will be generated <#if resource.draft>when this resource is no longer a draft<#else> in the next day for this resource</#if>.
        </#if>
    </p>
    <#if openUrl?has_content>
        <span class="Z3988" title="<#noescape>${openUrl!""}</#noescape>"></span>
    </#if>
</div>
</div>

</#macro>


<#--display more detailed information about the files associated with the current resource -->
    <#macro extendedFileInfo>
        <#if (resource.informationResourceFiles?has_content)>
            <#local showDownloads = authenticatedUser?? />
        <div id="extendedFileInfoContainer" class="section">
            <h2 id="allfiles">File Information</h2>
            <table class="table table-sm table-striped">
                  <thead class="thead-dark">

                <tr>
                    <th>&nbsp;</th>
                    <th>Name</th>
                    <th>Size</th>
                    <th>Creation Date</th>
                    <th>Date Uploaded</th>
                    <th>Access</
                    ></th>
                    <#if showDownloads>
                        <th>Downloads</th>
                    </#if>
                </tr>
                </thead>
                <tbody>
                    <@fileInfoSection extended=true; irfile, showAll, ext>
                        <#if !irfile.deleted || userAbleToViewDeletedFiles >
                            <#local twoRow = (irfile.hasTranslatedVersion || irfile.description?has_content ) />
                        <tr class="${irfile.status!""} ${irfile.deleted?string("DELETED","")}">
                            <td <#if twoRow>rowspan=2</#if>><@fileIcon irfile=irfile /></td>
                            <td><@createFileLink irfile false false false /></td>
                            <td><@common.convertFileSize version.fileLength /></td>
                            <td><#if irfile.fileCreatedDate??>${(irfile.fileCreatedDate!"")?date}</#if></td>
                            <td>${irfile.latestUploadedVersion.dateCreated} </td>
                            <td>${irfile.restriction.label}</td>

                            <#if irfile.transientDownloadCount?? >
                                <td>${((irfile.transientDownloadCount)!0)}</td>
                            </#if>
                        </tr>
                            <#if twoRow>
                            <tr class="${irfile.status!''} ${irfile.deleted?string("DELETED","")}">
                                <td colspan=<#if showDownloads>6<#else>5</#if>>
                                ${irfile.description!""}
                    <@translatedFileSection irfile />
                                </td>
                            </tr>
                            </#if>
                        </#if>
                    </@fileInfoSection>
                </tbody>
            </table>
            <#if (!(resource.publicallyAccessible) && !contactProxies.empty )>
                <div class="well restricted-files-contacts">
                    <h4>Accessing Restricted Files</h4>

                    <p>At least one of the files for this resource is restricted from public view. For more information regarding
                        access to these files, please reference the contact information below</p>
                    <@showCreatorProxy proxyList=contactProxies />
                </div>
            </#if>

        </div>
        </#if>
    </#macro>

<#-- emit div displaying the category/subcategory of the current resource -->
    <#macro categoryVariables>
    <div>
        <#if resource.categoryVariable??>
        <#-- this might be a subcategory variable, check if parent exists -->
            <#if resource.categoryVariable.parent??>
                <@kvp key="Category" val=resource.categoryVariable.parent />
                <#if resource.categoryVariable.parent != resource.categoryVariable >
                    <@kvp key="Subcategory" val=resource.categoryVariable />
                </#if>
            <#else>
            <#-- only the parent category exists -->
                <@kvp key="Category" val=resource.categoryVariable />
            </#if>
        <#else>
            <p class="sml">No categories or subcategories specified.</p>
        </#if>
    </div>
    </#macro>



<#--emit a link to a resource creator information page -->
    <#macro browse creator role=""><#compress>
        <#local c=creator />
        <#if creator.creator?has_content >
            <#local c = creator.creator />
        </#if>
        <#local schemaRole = role />
        <#if creator.role?has_content && creator.role.partOfSchemaOrg >
            <#local schemaRole = creator.role.schemaOrgLabel />
        </#if>
    <#local alt="${c.properName}"/>
    <#if c.institutionName?has_content><#local alt="${c.properName?xhtml} (${c.institutionName?xhtml})"/></#if>
        <#if c?? && ( authenticatedUser?? || c.browsePageVisible ) > <a href="<@s.url value="${c.detailUrl}"/>">${c.properName}</a><#else>${c.properName}</#if>
    </#compress>
    </#macro>

<#--emit a link to a seach page with a predefined query.  The contents of the supplied #nested block become the text node
    of the link and also the value of the "q" querystring parameter for the link URL
    @nested search terms
-->
    <#macro search fieldName="query" quoted=true>
        <#assign q=''>
        <#if quoted>
            <#assign q='"'>
        </#if>
        <#assign term><#nested></#assign>
        <#noescape><a href="<@s.url value="/search/search?${fieldName?url}=${q?url}${term?url}${q?url}"/>">${term}</a></#noescape>
    </#macro>

<#-- emit a link to the search page for the label of the supplied keyword (see @search)-->
    <#macro keywordSearch _keywords fieldName="query" quoted=true>
        <#list _keywords.toArray()?sort_by("label") as _keyword><#t>
            <#if !_keyword.deleted>
            <#if (_keyword_index == 25)>
                <span class="collapse" id="spanShowMore${fieldName}">
            </#if>
                <@searchm.searchFor keyword=_keyword asList=false showOccurrence=false />
                <#sep>&bull;</#sep> 
                <#if (_keyword?index > 25) && _keyword?is_last >
                </span>
                <i><a href="#" id="aShowMore${fieldName}" 
                        class="collapse show" data-toggle="collapse"  
                        data-target="#aShowMore${fieldName}, #spanShowMore${fieldName}">Show More</a></i>
                </#if>
            </#if>
        </#list>
    </#macro>

<#--emit the download count for the supplied information resource file -->
    <#macro downloadCount irfile>
        <#assign downloads = 0 />
        <#if (irfile.transientDownloadCount?has_content && irfile.transientDownloadCount > 0 )>
            <#assign downloads = irfile.transientDownloadCount />
        </#if>

        <#if (irfile.informationResourceFile?has_content && (irfile.informationResourceFile.transientDownloadCount!0) > 0 )>
            <#assign downloads = irfile.informationResourceFile.transientDownloadCount />
        </#if>
        <#if (downloads > 0)>
            <#if (downloads != 1)>
            [downloaded ${downloads} times]
            <#else>
            [downloaded 1 time]
            </#if>
        </#if>
    </#macro>


<#-- emit the access rights section of a view page -->
    <#macro accessRights>
        <#if sessionData?? && sessionData.authenticated>
        <h2>Administrative Information</h2>

        <div>
            <dl class="row">
                <dt class="col-2">
                <strong>Created</strong></dt>
                <dd class="col-4"><a
                        href="<@s.url value="${resource.submitter.detailUrl}"/>">${resource.submitter.properName}</a> <#if resource.submitter.id == resource.uploader.id>
                    on ${resource.dateCreated}</#if></dd>
                <dt class="col-2">
                <strong>Last Updated</strong></dt>
                <dd class="col-4"><a href="<@s.url value="${resource.updatedBy.detailUrl}"/>">${resource.updatedBy.properName!""}</a>
                    on ${resource.dateUpdated?date!""}</dd>
                <#if resource.submitter.id != resource.uploader.id>
                    <dt class="col-2">
                    <strong>Uploaded</strong></dt>
                    <dd class="col-4"><a href="<@s.url value="${resource.uploader.detailUrl}"/>">${resource.uploader.properName}</a> on ${resource.dateCreated}
                    </dd>
                </#if>
                <#if resource.account?has_content && (administrator || editable) >
                    <dt class="col-2">
                    <strong>Account</strong></dt>
                    <dd class="col-4"><a href="<@s.url value="/billing/${resource.account.id?c}"/>">${resource.account.name}</a></dd>
                </#if>

                <#if administrator>
                    <dt class="col-2">
                    <strong>Status</strong></dt>
                    <dd class="col-4">${resource.status.label} <#if resource.previousStatus?has_content && resource.previousStatus != resource.status>
                        (${resource.previousStatus.label})</#if></dd>
                </#if>
                <dt class="col-2">
                <strong>Viewed</strong></dt>
                <dd class="col-4">${resource.transientAccessCount!"0"} time(s)</dd>
            </dl>
        </div>
            <@common.resourceUsageInfo />

            <#nested>
            <@rights.resourceCollectionsRights collections=effectiveShares owner=resource.submitter />
        </#if>
    </#macro>

<#--emit a key/value pair -->
    <#macro kvp key="" val="" noescape=false nested=false>
        <#if val?has_content && val != 'NULL' || nested>
        <p class="sml"><strong>${key}:</strong> <#if noescape><#noescape>${val}</#noescape><#else>${val}</#if><#nested></p>
        </#if>
    </#macro>


<#--emit a list of resouce creator proxies -->
    <#macro showCreatorProxy proxyList=authorshipProxies>
        <#if proxyList?has_content>
            <#list allResourceCreatorRoles as role>
                <#assign contents = "" />
                <#list proxyList as proxy>
                    <#if proxy.valid && proxy.role == role && !proxy.resourceCreator.creator.deleted >
                        <#assign contents><#noescape>${contents}<#t/></#noescape><#if contents?has_content>;</#if>
                        <@browse creator=proxy.resourceCreator /><#t/></#assign>
                    </#if>
                </#list>
                <#if contents?has_content>
                    <#assign key>${role.label}(s)</#assign>
                    <@kvp key=key val=contents noescape=true />
                </#if>
            </#list>
        </#if>
    </#macro>

<#-- emit a warning callout if the current resource is DRAFT or DELETED -->
    <#macro pageStatusCallout>
        <#local status="danger">
        <#if (persistable.status)?has_content && !persistable.active >
            <#if persistable.status.draft >
                <#local status="info"/>
            </#if>

                <@_statusCallout onStatus='${persistable.status?lower_case}' cssClass='${status}'>
            <#if persistable.status.flaggedForBilling && namespace=='/billing'>
                - please add funds to it.
            <#else>
                <#if authorityForDup?has_content> - of
                <a href="<@s.url value="/${authorityForDup.urlNamespace}/${authorityForDup.id?c}"/>">${authorityForDup.name}</a></#if>
            </#if>
                </@_statusCallout>

        </#if>
    </#macro>
    <#macro _statusCallout onStatus cssClass>
        <#if persistable.status.toString().equalsIgnoreCase(onStatus) >
        <div class="badge-${cssClass} badge statusbadge"> ${onStatus}<#nested>
        </div>
        </#if>
    </#macro>

<#-- emit an image gallery for the accessible image/video files for the current resource -->
    <#macro imageGallery>
    <div class="section">
    <div class="slider">
        <#local numThumbnails = resource.visibleFilesWithThumbnails?size!0 />
        <#local numThumbnailsPerSection = 4 />
        <#local numIndicators = ( numThumbnails / numThumbnailsPerSection)?ceiling  />

        <#if (resource.visibleFilesWithThumbnails?size > 1 || !authenticatedUser??)>
            <div id="myCarousel" class="image-carousel carousel slide pagination-centered pt-3 pb-5" data-ride="carousel">
                <#if (numIndicators > 1)>
                    <ol class="carousel-indicators ">
                        <li data-target="#myCarousel" data-slide-to="0" class="active">&nbsp;</li>
                        <#list 1..(numIndicators -1) as x>
                            <li data-target="#myCarousel" data-slide-to="${x}">&nbsp;</li>
                        </#list>
                    </ol>
                    </#if>
                <!-- Carousel items -->
                <div class="carousel-inner px-5">

                    <#list resource.visibleFilesWithThumbnails as irfile>
                        <#local lazyLoad = (irfile_index > (2 * numThumbnailsPerSection)) />
                        <#if (irfile_index % numThumbnailsPerSection) == 0>
                        <div class="carousel-item <#if irfile_index == 0>active</#if>"> <#t>
                        <div class="row"> <#t>
                        </#if>
                        <div class="col"> <#t>
                          <span class="primary-thumbnail thumbnail-border <#if irfile_index == 0>thumbnail-border-selected</#if>"> <#t>
                              <span class="thumbnail-center-spacing "></span> <#t>
                              <img class="thumbnailLink img-polaroid"<#t>
                                   <#if (resource.visibleFilesWithThumbnails?size = 1) && (irfile.description!'') = ''>
                                   alt="<@_altText irfile resource.title />"
                                   title="<@_altText irfile resource.title />"
                                   <#else> <#t>
                                   alt="<@_altText irfile />" <#t>
                                   title="<@_altText irfile />" <#t>
                                   </#if>
                                   <#if lazyLoad>
                                       src="/images/image_unavailable_t.gif"
                                       data-src="<@s.url value="/files/sm/${irfile.latestThumbnail.id?c}"/>" <#t>
                                   <#else>
                                       src="<@s.url value="/files/sm/${irfile.latestThumbnail.id?c}"/>" <#t>
                                   </#if>
                                   onError="this.src = '<@s.url value="/images/image_unavailable_t.gif"/>';" <#t>
                                   data-url="<@s.url value="/filestore/get/${irfile.informationResource.id?c}/${irfile.zoomableVersion.id?c}"/>" <#t>
                                   <#if !irfile.public>data-access-rights="${irfile.restriction.label}"</#if>> <#lt>
                          </span>
                        </div>
                        <#if ((irfile_index + 1) % numThumbnailsPerSection) == 0 || !irfile_has_next>
                        </div><!--/row-fluid-->
                        </div><!--/item-->
                        </#if>
                    </#list>
                </div>
                <!--/carousel-inner-->
                <#if (numIndicators > 1)>
                  <a class="carousel-control-prev" href="#myCarousel" role="button" data-slide="prev">
                    <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                    <span class="sr-only">Previous</span>
                  </a>
                  <a class="carousel-control-next" href="#myCarousel" role="button" data-slide="next">
                    <span class="carousel-control-next-icon" aria-hidden="true"></span>
                    <span class="sr-only">Next</span>
                  </a>
                </#if>
            </div>
            <!--/myCarousel-->
        </#if>
        <br/>

    </div>
        <#if authenticatedUser?? >
        <div class="bigImage justify-content-center">
            <#list resource.visibleFilesWithThumbnails as irfile>
                <div>
            <span id="imageContainer">
            <img id="bigImage" alt="#${irfile_index} - ${irfile.filename!''}" title="#${irfile_index} - ${irfile.filename!''}" class="rounded mx-auto d-block"
                 src="<@s.url value="/filestore/get/${irfile.informationResource.id?c}/${irfile.zoomableVersion.id?c}"/>"/>
            <#if !irfile.public><span id="confidentialLabel">This file is <em>${irfile.restriction.label}</em>, but you have rights to see it.</span></#if>
                </div>
                <div id="downloadText">
                    <@_altText irfile/>
                    </span>
                </div>
                <#break>
            </#list>
        </div>
        </#if>

    <script type="text/javascript">
        $(document).ready(function () {
            TDAR.common.initImageGallery();
        });
    </script>
    </div>
    </#macro>

    <#macro _altText irfile description = irfile.description!"">
    ${irfile.filename} <#if ( description?has_content && (irfile.filename)?has_content ) >- ${description}</#if>
        <#if irfile.fileCreatedDate??>${(irfile.fileCreatedDate!"")?date}</#if>
    </#macro>

<#--emit the unapi 'link' for the specified resource (see: http://unapi.info/specs/) -->
    <#macro unapiLink resource>
        <#if resource.active>
        <abbr class="unapi-id" title="${resource.id?c}"></abbr>
        </#if>
    </#macro>

<#-- return best shot at image description (e.g. for use in alt-text). if image has no description,  go with resource title -->
<#-- FIXME: replace occurances of #_altText in default branch with this -->
<#function _imageDescription irfile resource maxlen=80>
    <#local alt = (resource.title!'')>
    <#if (resource.visibleFilesWithThumbnails?size > 0) && irfile.description?has_content>
    <#local alt = irfile.description>
    </#if>
    <#return common.fnTruncate(alt, maxlen)>
</#function>

<#--emit a warning message  if the current resource we are rendering is under embargo -->
    <#macro embargoCheck showNotice=true>
        <#if resource.totalNumberOfFiles != 0>
        <!-- FIXME: CHECK -->
            <#assign embargoDate='' />
            <#list resource.confidentialFiles as file>
                <#if file.embargoed>
                    <#assign embargoDate = file.dateMadePublic />
                </#if>
            </#list>
            <#if !resource.publicallyAccessible && !ableToViewConfidentialFiles>
                <#if showNotice>
                <span class="label label-inverse">Restricted Access</span>
                Some or all of this resource's attached file(s) are <b>not</b> publicly accessible.
                    <#if embargoDate?has_content>  They will be released on ${embargoDate?date}</#if>
                </#if>
            <#else>
                <#if showNotice && (!resource.publicallyAccessible) && !resource.citationRecord >
                <span class="label label-inverse">Restricted Access</span>
                <em>This resource is restricted from general view; <b>however, you have been granted access to it</b>.</em>
                    <#if embargoDate?has_content>  They will be released on ${embargoDate?date}</#if>
                </#if>
            </#if>
            <#nested/>
        </#if>
    </#macro>

<#-- emit a specified date in our notion of a "short" format -->
    <#macro shortDate _date includeTime=false>
        <#if includeTime>
        ${_date?string.medium}<#t>
        <#else>
        ${_date?date}<#t>
        </#if>
    </#macro>


<#-- because this collection can get quite big,  we conserve output by omitting optional tags and using short attribute values where possible  -->
    <#macro resourceCollectionTable tbid="tblCollectionResources">
    <table class="table table-condensed table-hover" id="${tbid}">
        <colgroup>
            <col style="width:4em">
            <col>
            <col style="width:3em">
        </colgroup>
          <thead class="thead-dark">

        <tr>
            <th style="width: 4em">ID
            <th colspan="2">Name
        </tr>
        </thead>
        <tbody>
            <#list resources as resource>
            <tr id='dtr_${resource.id?c}'>
                <td>${resource.id?c}
                <td><a href="<@s.url value="${resource.detailUrl}"/>" target="_b">${(resource.title)!""}</a>
            <td>
                <button class="btn btn-sm" type="button" data-rid="${resource.id?c}"><i class="icon-trash"></i></button>
            </#list>
        </tbody>
    </table>
    </#macro>

<#-- emit markup for a single thumbnail representing the specified resource (e.g. for use in search results or project/collection contents)  -->
    <#macro firstThumbnail resource_ forceAddSchemeHostAndPort=true>
    <#-- if you don't test if the resource hasThumbnails -- then you start showing the Image Unavailable on Projects, Ontologies... -->
        <#local seenThumbnail = (resource_.supportsThumbnails!false && resource_.primaryThumbnail?has_content) >
        <#t><span class="primary-thumbnail <#if seenThumbnail>thumbnail-border</#if>"><#t>
        <#if seenThumbnail && (resource_.primaryThumbnail?has_content)!false><#t>
            <#t><span class="thumbnail-center-spacing"></span><#t>
            <#t><img src="<@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="/files/sm/${resource_.primaryThumbnail.id?c}" />"
                     title="${resource_.title!''}" alt="${_imageDescription(resource_.primaryThumbnail resource_)}"
                     onError="this.src = '<@s.url value="/images/image_unavailable_t.gif"/>';"/><#t>
        <#else>
	        <div class="iconbox125 beige">
	        <#local type="integration" />
	        <#if (resource_.resourceType?has_content)!false>
	        	<#local type=resource_.resourceType?lower_case />
	        <#elseif (resource_.type?has_content)!false>
	        	<#local type="collection" />
	        </#if>
	            <svg class="svgicon white svg-dynamic125"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_${type}"></use></svg>
	        </div>
<#--
		    
             <#if resource_.resourceType?has_content>
                <#t><i class="${resource_.resourceType?lower_case}-125"></i><#t>
            </#if>
            <#if resource_.type?has_content>
                <#t><i class="collection-125 ${resource_.type?lower_case}-125"></i><#t>
            </#if>
            <#if resource_.jsonData?has_content>
                <#t><i class="integration-125 integration-125"></i><#t>
            </#if>
             -->
        </#if>
        <#t>                </span><#t>
    </#macro>


<#--emit the citation section of a view page (including map depicting bounding box, if bounding box defined) -->
    <#macro tdarCitation resource=resource showLabel=true count=0 forceAddSchemeHostAndPort=false>
    <div class="carousel-item <#if count==0>active</#if>">
        <#local url><@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="${resource.detailUrl}"/></#local>
        <div class="row">
            <div class="col-8">
                <div class = "p-3">
                    <p class="title">
                        <a target="_top" href="${url}">${resource.title} </a><br>
                        <#if resource.formattedAuthorList?has_content>${resource.formattedAuthorList}
                            <br></#if>
                    </p>

                    <p class = "lead"><@common.truncate resource.description 150 /></p>

                    <p class = "lead">
                        <a target="_top" href="${url}" class="tdarButton">View ${resource.resourceType.label}</a> or &nbsp; <a target="_top" href="/search/results">Browse all
                        Resources</a>
                    </p>
                </div>
            </div>
            <div class="col-4">
                <#if resource.firstActiveLatitudeLongitudeBox?has_content>
                    <img title="map" alt="map" class="img-fluid" src="${_staticGoogleMapUrl(resource.firstActiveLatitudeLongitudeBox, config.googleMapsApiKey)}"/>
                <#else>
                    <a href="${url}" target="_top"><@firstThumbnail resource true /></a>
                </#if>
            </div>
        </div>
    </div>

    </#macro>
    <#function _staticGoogleMapUrl boundingBox apikey>
        <#local bb=boundingBox>
        <#local bbvals="[[${bb.obfuscatedWest?c},${bb.obfuscatedSouth?c}],[${bb.obfuscatedWest?c},${bb.obfuscatedNorth?c}],[${bb.obfuscatedEast?c},${bb.obfuscatedNorth?c}],[${bb.obfuscatedEast?c},${bb.obfuscatedSouth?c}],[${bb.obfuscatedWest?c},${bb.obfuscatedSouth?c}]]">
        <#local mapId="abrin.n9j4f56m">
        <#local apikeyval="">
        <#local width=410>
        <#local height=235>
        <#local uri>geojson({"type":"Feature","properties":{"stroke-width":4,"stroke":"#7a1501","stroke-opacity":0.5,"fill-opacity":0.15},"geometry":{"type":"Polygon","coordinates":[${bbvals}]}})</#local>
        <#return "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/${uri?url}/auto/${width?c}x${height?c}?access_token=${config.leafletStaticApiKey}">
    </#function>



<#-- emit license information section -->
    <#macro license>
        <#if (resource.licenseType??) >
    <div class="section">
        <h2>License</h2>
            <#if (resource.licenseType.imageURI != "")>
            <a href="${resource.licenseType.URI}"><img alt="license image" title="license image"
                                                       src="<#if secure>${resource.licenseType.secureImageURI}<#else>${resource.licenseType.imageURI}</#if>"/></a>
            </#if>
            <#if (resource.licenseType.URI != "")>
            <h4>${resource.licenseType.licenseName}</h4>
            <p><@s.property value="resource.licenseType.descriptionText"/></p>
            <p><a href="${resource.licenseType.URI}">view details</a></p>
            <#else>
            <h4>Custom License Type - See details below</h4>
            <p>${resource.licenseText}</p>
            </#if>
        </div>
        </#if>
    </#macro>


<#macro featured header="Featured Content" colspan="12" resourceList=featuredResources indicatorPosition="top">
<#local span = "col-${colspan}">
<div class="tdar-slider slider ${span} w-100 h-100">
    <h3>${header}</h3>

    <div id="slider" class="carousel slide" data-ride="carousel">
        <#if indicatorPosition=="top"><@_featured_indicators /></#if>
      <div class="carousel-inner">
        <#list resourceList as featuredResource>
            <#if featuredResource?has_content>
                <@tdarCitation resource=featuredResource showLabel=false count=featuredResource_index forceAddSchemeHostAndPort=true />
            </#if>
        </#list>
      </div>
      <a class="carousel-control-prev" href="#slider" role="button" data-slide="prev">
        <span class="carousel-control left" aria-hidden="true"></span>
        <span class="sr-only">Previous</span>
      </a>
      <a class="carousel-control-next" href="#slider" role="button" data-slide="next">
        <span class="carousel-control right" aria-hidden="true"></span>
        <span class="sr-only">Next</span>
      </a>
        <#if indicatorPosition=="bottom"><@_featured_indicators /></#if>
    </div>
</div>

</#macro>
<#macro _featured_indicators>
    <ol class="carousel-indicators">
        <li data-target="#slider" data-slide-to="0" class="active"></li>
        <li data-target="#slider" data-slide-to="1"></li>
        <li data-target="#slider" data-slide-to="2"></li>
        <li data-target="#slider" data-slide-to="3"></li>
        <li data-target="#slider" data-slide-to="4"></li>
    </ol>
</#macro>

</#escape>
<#-- NOTHING SHOULD GO AFTER THIS -->

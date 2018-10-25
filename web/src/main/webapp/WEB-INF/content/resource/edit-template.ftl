<#escape _untrusted as _untrusted?html>
<#--
   This template is designed to try and reduce duplicated code in each of the tDAR resource-edit pages.  As we have almost 10 of them, 
   it becomes a challenge to maintain them in parallel without introducing bugs. The goal and function of this template is to (a) centralize logic
   (b) set basic defaults (c) provide variables to turn on/off various settings and (d) allow for methods to override functions as needed.

-->

    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/content${namespace}/edit.ftl" as local_ />
<#-- We define local_ as a reference to the actual template in question. It's going to define for us any functions/methods that get overriden 
	 in the form. if local_.method?? && local_.method?is_macro ... then execute it...  -->

    <#import "/WEB-INF/macros/helptext.ftl" as  helptext>
<#-- helptext can be overriden by the theme so we import it, it, in turn override the default helptext -->
<head>
<#-- expose pageTitle so edit pages can use it elsewhere -->
    <#assign pageTitle>Create a new <@edit.resourceTypeLabel /></#assign>
    <#assign newRecord = true>
    <#if resource.id != -1>
        <#assign newRecord = false/>
        <#assign pageTitle>Editing <@edit.resourceTypeLabel /> Metadata for ${resource.title} (${siteAcronym} id: ${resource.id?c})</#assign>
    </#if>
    <title>${pageTitle}</title>



    <#assign rtLabel = resource.resourceType.label />
    <#if namespace == '/batch' || namespace == '/bulk'>
        <#assign rtLabel = '' />
    </#if>

</head>
<body>
<div class="row">
<div class="col-md-9 col-sm-12 col-lg-9">
    <#assign fileReminder=true />
    <#assign prefix="${resource.resourceType.label?lower_case}" />

    <#-- if new resource, submitter is current user; otherwise, submitter is resource.submitter -->
    <#if (resource.id == -1)>
        <#assign submitterId = authenticatedUser.id>
    <#else>
        <#assign submitterId = resource.submitter.id>
    </#if>
    <#assign validExtensions><@edit.join sequence=validFileExtensions![] delimiter="|"/></#assign>

    <@s.form name='metadataForm' id='metadataForm'   cssClass="form-horizontal tdarvalidate" method='post' enctype='multipart/form-data' action='save'
            dynamicAttributes={"data-submitterid":"${submitterId?c}","data-validate-method":"initBasicForm","data-resource-type","${resource.resourceType.label}",
            "data-total-files","${resource.totalNumberOfFiles!-1}","data-multiple-upload","${(multipleUpload?string)!'false'}","data-datatable","${(resource.resourceType.dataTableSupported?string)!'false'}",
            "data-valid-extensions","${validExtensions}","data-type","${resource.resourceType}"} >
        <@common.jsErrorLog />
        <@s.token name='struts.csrf.token' />
        <@s.hidden name="epochTimeUpdated" />
        <@s.hidden name="doubleSubmitKey" />
    <div class="row">
	<div class="col-12">

<#-- allow for overriding of the page title -->
    <#if  local_.customH1?? && local_.customH1?is_macro>
        <@local_.customH1 />
    <#else>
        <#assign newTitle>New <#noescape>${resource.resourceType.label}</#noescape></#assign>
    <h1><#if resource.id == -1>Creating<#else>Editing</#if>:<span> <#if resource.title?has_content>${resource.title}<#else>${newTitle}</#if> </span></h1>
    </#if>

    <#-- custom section ahead of the basic information -->
        <#if local_.topSection?? && local_.topSection?is_macro>
            <@local_.topSection />
        </#if>
	</div>
    <div class="col-12" id="basicInformationSection">
        <h2>Basic Information
          <@helptext.info title="Basic Information" contentDiv="#basicHelp" /></h2>
            <@helptext.basicHelp />
        <#if resource.id?? &&  resource.id != -1>
            <@s.hidden name="id"  value="${resource.id?c}" />
        </#if>

        <@edit.hiddenStartTime />

        <div id="spanStatus" class="form-group row">
            <#if editor>
        <div class="col-12">
                <p><b>note:</b> because you are an "editor" we've defaulted your default resource status to draft</p>
            </div>
            </#if>
            <label class="col-2 col-form-label">Status</label>

                <#if config.guestUserId != -1 && config.guestUserId == authenticatedUser.id>
                    <select name="status" class="form-control col-3">
                        <option value='DRAFT' selected>Draft</option>
                    </select>
                <#else>
                    <@s.select  value="resource.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}' cssClass="col-3"/>
                </#if>
                <#if resource.resourceType.project>
                <div class="col-10 offset-2">
                <span class="help-block">Note: project status does not affect status of child resources.</span>
                </div>
                </#if>
        </div>

        <@helptext.status />

    <#-- the bulk upload hides some common fields, but the validator requires them, so we set the values to hidden ones -->
        <#if bulkUpload >

            <@s.hidden id='resourceTitle' label='Title' name='image.title' cssClass="" value="BULK_TEMPLATE_TITLE"/>
            <@s.hidden 'dateCreated' placeholder='YYYY' label='Year Created' name='image.date' cssClass="" value="-100"/>
            <@s.hidden id='ImageDescription' name='image.description' value="placeholder description"/>

        <#else>
            <div>
                <@s.textfield label="Title" id="resourceRegistrationTitle" labelposition="left"
                title="A title is required for all ${resource.resourceType.plural}" name='${itemPrefix}.title'
                cssClass="required descriptiveTitle trim" required=true maxlength="512" helptext="If a formal title is given for the resource (as with a report) use this. If no title is supplied, the suggested formula is 'Content, Investigation Type or Site Name, Site Name or Specific Geographic Location'."/>
                <@helptext.helpBelow text="If a formal title is given for the resource (as with a report) use this. If no title is supplied, the suggested formula is 'Content, Investigation Type or Site Name, Site Name or Specific Geographic Location'." />
            </div>
            <#if resource.resourceType != 'PROJECT'>
                <div>
                    <#assign dateVal = ""/>
	        <#if resource.date?? && resource.date != -1>
                    <#assign dateVal = resource.date?c />
                </#if>
	        <@s.textfield label="Year" id='dateCreated' name='${itemPrefix}.date' value="${dateVal}" cssClass="reasonableDate required col-2 trim" required=true labelposition="left"
                maxlength=7 title="Please enter the year this ${resource.resourceType.label} was created" tooltip="Four digit year, e.g. 1966 or 2005. The publication year for a document, or the year a photograph was taken. Otherwise, the year the resource was created."/>
                </div>
            </#if>
        </#if>

    <#-- add custom basic information if needed -->
        <#if local_.basicInformation?? && local_.basicInformation?is_macro>
            <@local_.basicInformation />
        </#if>

    <#-- if we're an editor or administrator, we allow them to set the submitter of the resource to 'not them' -->
        <#if editor>
            <div class="form-group row" id="divSubmitter">
                <label class="col-form-label col-2">Submitter</label>
                    <#if submitter?has_content>
		                <@edit.registeredUserRow person=submitter isDisabled=disabled   _personPrefix="" _indexNumber='' textfieldCssClass="col-8"
		                    prefix="submitter" includeRights=false includeRepeatRow=false />
		 	        <#else>
                        <@edit.registeredUserRow person=authenticatedUser isDisabled=disabled   _personPrefix="" _indexNumber='' textfieldCssClass="col-8 "
                        prefix="submitter" includeRights=false includeRepeatRow=false />
                    </#if>
            </div>
        </#if>
    </div>

        <@_accountSection />

        <#if !resource.resourceType.project>
            <@edit.resourceCreators '${rtLabel} Creators' authorshipProxies 'authorship' />
        </#if>

    <div id="citationInformation" class="col-12">
        <h2>Additional Citation Information</h2>
        <#if local_.citationInformationToggle?? && local_.citationInformationToggle?is_macro>
            <@local_.citationInformationToggle />
        </#if>

        <#if resource.resourceType.hasLanguage && !resource.resourceType.document >
            <@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
        </#if>

    <#-- ontologies and coding sheets have fewer fields -->
        <#if !resource.resourceType.codingSheet && !resource.resourceType.ontology>

            <#if !resource.resourceType.project>
                <div>
	        <span id="publisher-hints" book="Publisher" report="Publisher" book_section="Publisher" journal_article="Publisher" conference_presentation="Conference"
                  thesis="Institution" other="Publisher">
                <@s.textfield id='publisher'  maxlength=255 label="Publisher" name='publisherName' cssClass="institution" labelposition="left"  />
            </span>
	
	        <span id="publisherLocation-hints" book="Publisher Location" report="Publisher Location" book_section="Publisher Location" journal_article="Publisher Location"
                  conference_presentation="Conference Location" thesis="Department" other="Publisher Location">
                <@s.textfield id='publisherLocation'  maxlength=255 label="Publisher Location" name='${itemPrefix}.publisherLocation'  labelposition="left" cssClass="" />
            </span>
                </div>
            </#if>

        <#-- if the resource type include citation information -->
            <#if local_.citationInformation?? && local_.citationInformation?is_macro>
                <@local_.citationInformation />
            </#if>

            <#if !resource.resourceType.project>
            <div id="t-doi">
                <@s.textfield labelposition='left' id='doi' label='DOI' name='${itemPrefix}.doi' cssClass="shortfield doi"  maxlength=255 tooltip="Digital Object Identifier." />
            </div>
            </#if>        

            <div id="divUrl" >
                <@s.textfield name="${itemPrefix}.url"  maxlength=255 id="txtUrl" label="URL" labelposition="left" cssClass="url" placeholder="http://" tooltip="Website address for this resource, if applicable" />
            </div>

        </#if>
    </div>

        <#if !bulkUpload >
        <div class="col-12">
            <h2>Abstract / Description</h2>

            <div id="t-abstract" class="clear">
                <@s.textarea rows="4" cols="80" id='resourceDescription'  label="Abstract / Description" name='${itemPrefix}.description' cssClass='required resizable resize-vertical input-xxlarge trim' required=true title="A description is required" tooltip="Short description of the <@edit.resourceTypeLabel />."/>
            </div>
        </div>
        </#if>


    <#-- if tdarConfiguration has copyright holders enabled -->
        <#if resource.resourceType.label?lower_case != 'project'>
            <@edit.copyrightHolders 'Primary Copyright Holder' copyrightHolderProxies />
        </#if>


    <#-- allow for more content before file uploads -->
        <#if local_.beforeUpload?? && local_.beforeUpload?is_macro>
            <@local_.beforeUpload />
        </#if>

    <#-- if the resource allows for file uploads -- this is set to something that's not null -->
        <#if multipleUpload??>
        <#-- if true -- we use the async file upload / otherwise we use the traditional file field -->
            <#if multipleUpload>
                <@edit.asyncFileUpload  uploadLabel="Attach ${rtLabel} Files" showMultiple=multipleUpload />
            <#else>
                <@edit.upload "${rtLabel} file" />
            </#if>
        </#if>


    <#if local_.afterUpload?? && local_.afterUpload?is_macro>
        <@local_.afterUpload />
    </#if>
    

    <#-- allow for additional content after the file upload -->
        <#if local_.localSection?? && local_.localSection?is_macro>
            <@local_.localSection />
        </#if>


    <#-- Emit choose-project section:  including project dropdown and inheritance checkbox -->
    <div class="col-12" id="organizeSection">
			<#-- use 1 beacause we prepend with a blank for -1 -->
		<#assign showProjects = (potentialParents?has_content && potentialParents?size > 1) />
    	<#if !showProjects && !resource.resourceType.project >
			<input type="hidden" name="projectId" value="-1" id="projectId"/>
		</#if>

        <#if !resource.resourceType.project && showProjects>

            <h2>${siteAcronym} Collections &amp; Project</h2>
            <!-- <h4>Add to a Collection</h4> -->
            <@edit.resourceCollectionSection prefix="shares" label="Collection" list=shares />
            <#assign _projectId = 'project.id' />
            <#if resource.id == -1 >
                <#assign _projectId = request.getParameter('projectId')!'' />
            </#if>

            <h4>Choose a Project</h4>

            <div id="t-project">
                <@s.select title="Please select a project" emptyOption='true' id='projectId' label="Project"  
                labelposition="left" name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
                truncate="70" value='${_projectId}'  cssClass="input-xxlarge" />
                <@helptext.helpBelow text="Select a project with which your resource will be associated. This is an important choice because it will allow metadata to
                be inherited from the project further down this form" />
            </div>

            <@helptext.inheritance />

    <div id="inheritOverwriteAlert" class="modal modal-lg " tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">

			<div class="modal-content">

                <div class="modal-header">
                    <h3 id="inheritOverwriteValidationErrorModalLabel">Overwrite Existing Values?</h3>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                </div>
                <div class="modal-body">
                    <p>Inheriting values from <span class="labeltext">the parent project</span> would overwrite existing information in the following sections
                    </p>

                    <p class="list-container"></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" id="btnInheritOverwriteOkay">Overwrite Existing Values</button>
                    <button type="button" class="btn" id="btnInheritOverwriteCancel" data-dismiss="modal" aria-hidden="true">Cancel</button>
                </div>
            </div>
            </div>
            </div>

            <div class="control-group" data-tiplabel="Inherit Metadata from Selected Project" data-tooltipcontent="#divSelectAllInheritanceTooltipContent"
                 id="divInheritFromProject">
                <div class="controls">
                    <label class="checkbox" for="cbSelectAllInheritance">
                        <input type="checkbox" value="true" id="cbSelectAllInheritance" class="">
                        <span id="spanCurrentlySelectedProjectText">Inherit from project.</span>
                    </label>
                </div>
            </div>
        <#else>
            <h2>${siteAcronym} Collections</h2>
            <@edit.resourceCollectionSection prefix="shares" label="Collections" list=shares />


        <#if !resource.resourceType.project>
            <h4>Create a Project</h4>
			<p><a href="<@s.url value="/project/add"/>">Go here to create a Project</a>.  Projects in ${siteAcronym} are not required, be are useful for creating and managing metadata for large groups of resources.</p>
		</#if>
        </#if>
    </div>

        <#if !resource.resourceType.project>
        <#-- emit resourceProvider section -->
        <div class="col-12" id="divResourceProvider">
            <h2>Institution Authorizing Upload of this <@edit.resourceTypeLabel /></h2>
            <@s.textfield label='Institution' name='resourceProviderInstitutionName' id='txtResourceProviderInstitution' cssClass="institution input-xxlarge"  maxlength='255' labelposition="left"
                tooltip="The institution authorizing ${siteAcronym} to ingest the resource for the purpose of preservation and access." />
            <br/>
        </div>
            <#if config.licensesEnabled?? && config.licensesEnabled || resource.licenseType?has_content>
                <@edit.license />
            </#if>
        </#if>


        <#if !(hideCreditSection??)>
            <@edit.resourceCreators 'Individual and Institutional Roles' creditProxies 'credit' >
                <#if !resource.resourceType.project>
                    <@edit._inheritsection checkboxId="cbInheritingCreditRoles" name='inheritingIndividualAndInstitutionalCredit' sectionId='#creditTable' />
                </#if>
            </@edit.resourceCreators>
        <@helptext.resourceCreator />
        </#if>

        <#if !(hideKeywordsAndIdentifiersSection??)>
            <@edit.identifiers inheritanceEnabled />

            <@edit.spatialContext inheritanceEnabled />

            <@edit.temporalContext inheritanceEnabled />

            <@edit.investigationTypes inheritanceEnabled />

            <@edit.materialTypes inheritanceEnabled />

            <@edit.culturalTerms inheritanceEnabled />

            <@edit.siteKeywords inheritanceEnabled />

            <@edit.generalKeywords inheritanceEnabled />
        </#if>

        <@edit.resourceNoteSection inheritanceEnabled />



        <#if !(hideRelatedCollections??)>
            <@edit.relatedCollections inheritanceEnabled />
        </#if>

<#--         <@edit.fullAccessRights /> -->

        <#assign submitClasses>button btn submitButton btn-primary</#assign>
        
        <#assign reminder = false />
        <#if !resource.resourceType.project>
        	<#assign reminder = ((resource.id == -1) && fileReminder)/>
        </#if>
            <@edit.submit fileReminder=reminder class=submitClasses>
            <p><b>Where to go after save:</b><br/>
            <div class='form-check'>
				<input type="radio" name="alternateSubmitAction" id="alt-submit-view" <#if !newRecord>checked=checked</#if> value="" class="form-check-input" emptyoption="false">
				<label for="alt-submit-view" class="form-check-label">View Page</label>
			</div>
            <div class='form-check'>
				<input type="radio" name="alternateSubmitAction" id="alt-submit-rights" value="Assign Permissions" class="form-check-input" emptyoption="false" >
				<label for="alt-submit-rights" class="form-check-label" <#if newRecord>checked=checked</#if>>Assign Permissions</label>
			</div>
        </p>
            </@edit.submit>
    </@s.form>


<#-- include footer on resource page -->
    <#if local_.footer?? && local_.footer?is_macro>
        <@local_.footer />
    </#if>
<#if test>
<!--
Auth Info
<ul>
<#list persistable.authorizedUsers as user>
  <li> ${user.user.id?c} | ${user.user.properName} | ${user.generalPermission}</li>
</#list>
<#list persistable.managedResourceCollections as share>
    <li>${share.id?c} - ${share.name}
    <ul>
        <#list share.authorizedUsers as user>
          <li> ${user.user.id?c} | ${user.user.properName} | ${user.generalPermission}</li>
        </#list>
    </ul>
    </li>
</#list>
</ul>
-->
</#if>


<script type='text/javascript'>
        <#noescape>

        
        /*

         * FIXME: move to common.js once we figure out how to control and set javascript based on freemarker values that have "Rights" implications.
         */
        $(function () {
            'use strict';

        TDAR.filesJson = ${filesJson!"false"};
        TDAR.inheritance.project = ${projectAsJson!"{}"};
        var props = {
            formSelector: "#metadataForm",
            includeInheritance : ${inheritanceEnabled?string},
            acceptFileTypes : /\.(<@edit.join sequence=validFileExtensions delimiter="|"/>)$/i,
            <#if multipleUpload??>
            multipleUpload : ${multipleUpload?string},
        </#if>
        <#if ableToUploadFiles??>
            ableToUpload : ${ableToUploadFiles?string},
        </#if>
             dataTableEnabled : ${resource.resourceType.dataTableSupported?string}
         };
        var form = $(props.formSelector)[0];
        TDAR.common.initEditPage(form, props);
        TDAR.vuejs.uploadWidget.main();
        <#if local_.localJavascript?? && local_.localJavascript?is_macro>
            <@local_.localJavascript />
        </#if>

        <#if selenium>
            var $up = $("#fileAsyncUpload");
            $up.css("position", "static");
            $up.css("top", "auto");
            $up.css("right", "auto");
            $up.css("margin", 0);
            $up.css("opacity", 1);
            $up.css("transform", "none");
            $up.css("direction", "ltr");
            $up.css("cursor", "auto");
        </#if>

        });
        </#noescape>
</script>

</div>
</div>
<div class="col-md-3 col-lg-3 d-sm-none d-md-block ">
    <@edit.subNavMenu>
    <#-- include local scrollspy menu details -->
        <#if local_.subNavMenu?? && local_.subNavMenu?is_macro>
            <@local_.subNavMenu />
        </#if>
    </@edit.subNavMenu>
</div>
</body>


<#-- emit account information section -->
    <#macro _accountSection>
        <div class="col-12" id="accountsection">
        <#if config.payPerIngestEnabled>
            <#if activeAccounts?size == 1>
                <h2>Billing Account Information</h2>

                <div class="form-group row">
                    <label class="col-form-label col-2">Account Name</label>

                    <div class="controls">
                        <#list activeAccounts as activeAccount>
                            <span class="uneditable-input">${activeAccount.name}</span>
                            <@s.hidden name="accountId" value="${activeAccounts?first.id}" />
                        </#list>
                    </div>
                </div>
            <#else>
                <h2>Choose an account to bill from</h2>
                <@s.select name="accountId" list="%{activeAccounts}" label="Account" title="Choose an account to bill from" listValue="name" listKey="id" emptyOption="true" required=true cssClass="required" labelposition="left" cssClass="col-5"/>
            </#if>
        <#else>
        <i>Charging is disabled (TESTING), re-enable in tdar.properties</i>    
        </#if>
            </div>
    </#macro>
</#escape>


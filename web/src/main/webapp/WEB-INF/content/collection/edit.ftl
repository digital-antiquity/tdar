<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "../collection/common-collection.ftl" as commonCollection>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
    <#if persistable.id == -1>
        <title>Create a Collection</title>
    <#else>
        <title>Editing: ${persistable.name}</title>
    </#if>
    <meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

    <div id='subnavbar' class="subnavbar-scrollspy affix-top subnavbar resource-nav navbar-static  screen" data-offset-top="250" data-spy="affix">
        <div class="">
            <div class="container">
                <ul class="nav">
                    <li class="alwaysHidden"><a href="#top">top</a></li>
                    <li class="active"><a href="#basicInformationSection">Basic</a></li>
                    <li><a href="#divResourcesSesction">Resources</a></li>
                </ul>
                <div id="fakeSubmitDiv" class="pull-right">
                    <button type=button class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</button>
                    <img alt="progress indicator" title="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
                </div>
            </div>
        </div>
    </div>

    <div id="sidebar-right" parse="true">
        <div id="notice">
            <h3>Introduction</h3>
            This is the editing form for a Collection.
        </div>
    </div>


    <h1><#if persistable.id == -1>Creating<#else>Editing</#if>: <span> ${persistable.name!"New Collection"}</span></h1>
        <@s.form name='metadataForm' id='metadataForm'  method='post' cssClass="form-horizontal tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"} enctype='multipart/form-data' action='save'>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
        <h2>Basic Information</h2>

        <div class="" id="basicInformationSection" data-tiplabel="Basic Information"
             data-tooltipcontent="Enter a name and description for this collection.  You may also choose a &quot;parent
    collection&quot; which allows you to inherit all of the access permissions defined by the parent.">
            <#if resourceCollection.id?? &&  resourceCollection.id != -1>
                <@s.hidden name="id"  value="${resourceCollection.id?c}" />
            </#if>
            <@edit.hiddenStartTime />
            <@s.textfield labelposition='left' label='Collection Name' name='resourceCollection.name'  cssClass="required descriptiveTitle input-xxlarge"  title="A title is required for all collections." maxlength="500" />

            <div id="parentIdContainer" class="control-group">
                <label class="control-label">Parent Collection</label>

                <div class="controls">
                    <@s.hidden name="parentId"  id="hdnParentId" cssClass=""
                    autocompleteParentElement="#parentIdContainer"  />
            <@s.textfield theme="simple" name="parentCollectionName" cssClass="input-xxlarge collectionAutoComplete"  autocomplete="off"
                autocompleteIdElement="#hdnParentId" maxlength=255 autocompleteParentElement="#parentIdContainer" autocompleteName="name"
                placeholder="parent collection name" id="txtParentCollectionName"
                />
                </div>
            </div>

                <#if editor>
            <div class="control-group" id="divSubmitter">
                <label class="control-label">Owner</label>

                <div class="controls controls-row">
                    <#if owner?has_content>
                <@edit.registeredUserRow person=owner isDisabled=disabled   _personPrefix="" _indexNumber=''
                    prefix="owner" includeRights=false includeRepeatRow=false />
	 	        <#else>
                        <@edit.registeredUserRow person=authenticatedUser isDisabled=disabled   _personPrefix="" _indexNumber=''
                        prefix="owner" includeRights=false includeRepeatRow=false />
                    </#if>
                </div>
            </div>
            
            <div id="altParentIdContainer" class="control-group">
                <label class="control-label">Secondary Parent Collection (No rights)</label>

                <div class="controls">
                    <@s.hidden name="alternateParentId"  id="hdnAltParentId" cssClass=""
                    autocompleteParentElement="#altParentIdContainer"  />
            <@s.textfield theme="simple" name="alternateParentCollectionName" cssClass="input-xxlarge collectionAutoComplete"  autocomplete="off"
                autocompleteIdElement="#hdnAltParentId" maxlength=255 autocompleteParentElement="#altParentIdContainer" autocompleteName="name"
                placeholder="parent collection name" id="txtAltParentCollectionName"
                />
                </div>
            </div>

        </#if>

            <@s.textarea rows="4" labelposition='top' label='Collection Description' name='resourceCollection.description'  cols="80" 
            cssClass='resizable input-xxlarge' title="Please enter the description " />

        </div>

        <div id="divBrowseOptionsTips" style="display:none">
            <p>Choose whether this collection will be public or private, and how ${siteAcronym} will sort the resources when displaying this collection to other
                users. Marking a collection as "private" does not restrict access to the resources within it.</p>
            <ul>
                <li>Public collections are viewable to all ${siteAcronym} users and accessible from the &quot;Browse Collections&quot; page.</li>
                <li>Private collections are only viewable to the users specified in the <a href="#accessRights">Access Rights</a> section.</li>
            </ul>
        </div>
        <div class="glide" data-tiplabel="Browse and Display Options" data-tooltipcontent="#divBrowseOptionsTips">
            <h2>Browse and Display Options</h2>

        
            <div class="control-group">
                <label class="control-label">Hide this collection?</label>

                <div class="controls">
                    <label for="rdoVisibleTrue" class="radio inline"><input type="radio" id="rdoVisibleTrue" name="resourceCollection.hidden"
                                                                            value="true" <@common.checkedif resourceCollection.hidden true /> />Yes</label>
                    <label for="rdoVisibleFalse" class="radio inline"><input type="radio" id="rdoVisibleFalse" name="resourceCollection.hidden"
                                                                             value="false" <@common.checkedif resourceCollection.hidden false /> />No</label>
                </div>
            </div>
    
            <@s.select labelposition='top' label='When Browsing Sort Resource By' name='resourceCollection.sortBy'
            listValue='label' list='%{sortOptions}' title="Sort resource by" />

            <@s.select labelposition='top' label='Display Collection as' name='resourceCollection.orientation'
            list='%{ResultsOrientations}'  listValue='label'  title="Display as" />
        </div>

        <div id="divCollectionAccessRightsTips" style="display:none">
            <p>Determines who can edit a document or related metadata. Enter the first few letters of the person's last name.
                The form will check for matches in the ${siteAcronym} database and populate the related fields.</p>
            <em>Types of Permissions</em>
            <dl>
                <dt>View and Download</dt>
                <dd>User can view/download all file attachments associated with the resources in the collection.</dd>
                <dt>Modify Record
                <dt>
                <dd>User can edit the resources listed in the collection.
                <dd>
                <dt>Administer Collection
                <dt>
                <dd>User can edit resources listed in the collection, and also modify the contents of the collection.
                <dd>
            </dl>
        </div>

        <div class="glide" id="divResourcesSesction" data-tiplabel="Share Resources with Users" data-tooltipcontent="Check the items in this table to add them to the collection.  Navigate the pages
                    in this list by clicking the left/right arrows at the bottom of this table.  Use the input fields above the table to limit the number
                    of results.">
            <h2>Share Resources with other users</h2>
            <#--only show the 'limit to collection' checkbox when we are editing a resource (it's pointless when creating new collection) -->
            <#assign showLimitToCollection = (actionName=='edit') && (resourceCollection.resources?size > 0)>
            <@edit.resourceDataTable showDescription=false selectable=true limitToCollection=showLimitToCollection >
            </@edit.resourceDataTable>

            <div id="divNoticeContainer" style="display:none">
                <div id="divAddProjectToCollectionNotice" class="alert">
                    <button type="button" class="close" data-dismiss="alert" data-dismiss-cookie="divAddProjectToCollectionNotice">×</button>
                    <em>Reminder:</em> Adding projects to a collection does not include the resources within a project.
                </div>
            </div>

        </div>

        <div id="divAddRemove">
            <h2>Modifications</h2>

            <div id="divToAdd">
                <h4>The following resources will be added to the collection</h4>
                <table id="tblToAdd" class="table table-condensed"></table>
            </div>

            <div id="divToRemove">
                <h4>The following resources will be removed from the collection</h4>
                <table id="tblToRemove" class="table table-condensed"></table>
            </div>
        </div>


            <@edit.submit fileReminder=false />
        </@s.form>

        <#noescape>
        <script type='text/javascript'>
            //selectResourcesFromCollectionid

            $(function () {
                TDAR.datatable.setupDashboardDataTable({
                    isAdministrator: ${(editor!false)?string},
                    limitContext: ${(editor!false)?string},
                    isSelectable: true,
                    showDescription: false,
                    selectResourcesFromCollectionid: $("#metadataForm_id").val()
                });
            });



            $(function () {
                'use strict';
                var form = $("#metadataForm")[0];
                TDAR.common.initEditPage(form);
                TDAR.datatable.registerResourceCollectionDataTable("#resource_datatable", "#tblCollectionResources");
                TDAR.datatable.registerResourceCollectionDataTable("#resource_datatablepublic", "#tblCollectionResourcespublic",false);
                TDAR.autocomplete.applyCollectionAutocomplete($("#txtParentCollectionName"), {showCreate: false}, {permission: "ADMINISTER_SHARE"});
                TDAR.autocomplete.applyCollectionAutocomplete($("#txtAltParentCollectionName"), {showCreate: false}, {permission: "ADMINISTER_SHARE"});
                TDAR.datatable.registerAddRemoveSection(${(id!-1)?c});
                        //remind users that adding a project does not also add the project's contents
        });
        </script>
        </#noescape>
        <@edit.personAutocompleteTemplate />
<div style="display:none"></div>
</body>
</#escape>

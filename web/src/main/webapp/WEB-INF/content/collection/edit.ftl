<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
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



    <#assign newRecord = false>
    <#if persistable.id == -1>
        <#assign newRecord = true />
    </#if>
    <h1><#if persistable.id == -1>Creating<#else>Editing</#if>: <span> ${persistable.name!"New Collection"}</span></h1>
    <div class="row">
    <div class="col-10">
        <div class="row">
        <@s.form name='metadataForm' id='metadataForm'  method='post' cssClass="form-horizontal tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"} enctype='multipart/form-data' action='save'>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
        <h2>Basic Information</h2>

        <div class="col-12" id="basicInformationSection" data-tiplabel="Basic Information"
             data-tooltipcontent="Enter a name and description for this collection.  You may also choose a &quot;parent
    collection&quot; which allows you to inherit all of the access permissions defined by the parent.">
            <#if resourceCollection.id?? &&  resourceCollection.id != -1>
                <@s.hidden name="id"  value="${resourceCollection.id?c}" />
            </#if>
            <@edit.hiddenStartTime />
            <@s.textfield labelposition='left' label='Collection Name' name='resourceCollection.name'  cssClass="required descriptiveTitle input-xxlarge"  title="A title is required for all collections." maxlength="500" />

            <div id="parentIdContainer" class="form-group row">
                <label class="col-form-label col-2">Parent Collection</label>

                <div class="controls col-10">
                <div class="row">
                    <@s.hidden name="parentId"  id="hdnParentId" cssClass=""
                    autocompleteParentElement="#parentIdContainer"  />
            <@s.textfield name="parentCollectionName" cssClass="col-12 collectionAutoComplete"  autocomplete="off"
                autocompleteIdElement="#hdnParentId" maxlength=255 autocompleteParentElement="#parentIdContainer" autocompleteName="name"
                placeholder="parent collection name" id="txtParentCollectionName"
                />
                </div>
                </div>
            </div>

        <@s.textarea rows="4" labelposition='top' label='Collection Description' name='resourceCollection.description'  cols="80" 
            cssClass='resizable input-xxlarge trim' title="Please enter the description " />

        <#if editor>
            <h4>Admin Options</h4>
            <div class="form-group row" id="divSubmitter">
                <label class="control-label col-form-label col-2">Submitter</label>
				<div class="col-10">
                <div class="row">
                    <#if owner?has_content>
                <@edit.registeredUserRow person=owner isDisabled=disabled   _personPrefix="" _indexNumber='' textfieldCssClass="col-8"
                    prefix="owner" includeRights=false includeRepeatRow=false />
	 	        <#else>
                        <@edit.registeredUserRow person=authenticatedUser isDisabled=disabled   _personPrefix="" _indexNumber=''  textfieldCssClass="col-8"
                        prefix="owner" includeRights=false includeRepeatRow=false />
                    </#if>
                </div>
                </div>
            </div>
            
            <div id="altParentIdContainer" class="control-group row">
                <label class="control-label col-form-label col-2">Secondary Parent Collection (No rights)</label>
                <div class="col-10">
                <div class="row">
                    <@s.hidden name="alternateParentId"  id="hdnAltParentId" cssClass=""
                    autocompleteParentElement="#altParentIdContainer"  />
            <@s.textfield  name="alternateParentCollectionName" cssClass="col-12 collectionAutoComplete"  autocomplete="off"
                autocompleteIdElement="#hdnAltParentId" maxlength=255 autocompleteParentElement="#altParentIdContainer" autocompleteName="name"
                placeholder="parent collection name" id="txtAltParentCollectionName"
                />
                </div>
                </div>
            </div>


            <#if administrator>
                <@s.textarea rows="4" labelposition='top' label='Collection Description (allows html)' name='resourceCollection.formattedDescription' cols="80" 
                cssClass='resizable input-xxlarge' title="Please enter the description " />
            </#if>

            <div class="control-group">
                <label class="control-label">Associate an Image/Logo with this Collection</label>
                <div class="controls">
                    <@s.file theme="simple" name='file' cssClass="input-xxlarge profileImage" id="fileUploadField"
                    labelposition='left' size='40' dynamicAttributes={
                        "data-rule-extension":"jpg,tiff,jpeg,png"
                    }/>
                    <button name="clear" type="button" id="clearButton" class="button btn btn-sm">clear</button>
                </div>
            </div>
        </#if>

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

        
            <div class="row">
                <label class="col-form-label col-2">Hide this collection?</label>
                <div class="col-10">
					<div class="form-row">
						<div  class="col-2">    
		                    <label for="rdoVisibleTrue" class="form-check-label radio "><input type="radio" id="rdoVisibleTrue" name="resourceCollection.hidden"
								class="form-check-input" value="true" <@commonr.checkedif resourceCollection.hidden true /> />Yes</label>
		                </div>
						<div  class="col-2">    
		                    <label for="rdoVisibleFalse" class="radio form-check-label"><input type="radio" id="rdoVisibleFalse" name="resourceCollection.hidden"
								class="form-check-input" value="false" <@commonr.checkedif resourceCollection.hidden false /> />No</label>
		                </div>
	                </div>
                </div>
            </div>
    
    		<div class="row">
    		<div class="col">
            <@s.select labelposition='top' label='When Browsing Sort Resource By' name='resourceCollection.sortBy'
            listValue='label' list='%{sortOptions}' title="Sort resource by" />
			</div>
    		<div class="col">
            <@s.select labelposition='top' label='Display Collection as' name='resourceCollection.orientation'
            list='%{ResultsOrientations}'  listValue='label'  title="Display as" />
	        </div>
	        </div>
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

    <#assign useManagedCollections = administrator>
        <div id="divResourcesOptionsTips" style="display:none">
        <ul>
            <li>Use the 'Add' button to add items to the collection.</li>
            <li>Use the 'Remove' button to remove items from the collection.</li>
            <li>Use the 'Undo' link to cancel a pending change.</li>
            <li>Navigate the pages in this list by clicking the left/right arrows at the bottom of this table.</li>
            <li>Display and use the drop down filters by clicking 'More/Less Options' to limit the number
                    of results.</li>
        </ul>
        </div>

        <div class="glide" id="divResourcesSesction" data-tiplabel="Share Resources with Users" data-tooltipcontent="#divResourcesOptionsTips">
            <h2>Resources</h2>
            <#--only show the 'limit to collection' checkbox when we are editing a resource (it's pointless when creating new collection) -->
            <#assign showLimitToCollection = (actionName=='edit') && ((resourceCollection.managedResources![])?size > 0 || (resourceCollection.unmanagedResources![])?size > 0)>
        
    <#if (resourceCollection.id?? &&  resourceCollection.id != -1 && resourceCollection.size > 0)> 
        <ul class="nav nav-tabs" id="tabs">
          <li class="active"><a data-toggle="tab" href="#existingResources" id="existingResourceTab">Resources in this collection</a></li>
          <li><a data-toggle="tab" href="#addResources" id="addResourceTab">Add Resources to this collection</a></li>
        </ul>
        
        <div class="tab-content">
          <div id="existingResources" class="tab-pane fade in active">
          
                   <@s.textfield theme="tdar" name="_tdar.existing.query" id="existing_res_query" cssClass='col-8'
                            placeholder="Enter a full or partial title to filter results" />
          
                <#--The HTML table for resources. -->
                <div class="row">
                    <div class="col-12">
                    <table class="display table table-striped table-bordered tableFormat" id="existing_resources_datatable">
                            <colgroup>
                                <col style="width: 10%">
                                <col style="width: 60%">
                                <col style="">
                                <#if useManagedCollections>
                                <col style="">
                                </#if>
                                <col style="">
                            </colgroup>
                              <thead class="thead-dark">

                                <tr>
                                    <th>ID</th>
                                    <th>Title</th>
                                    <th>Type</th>
                                    <#if useManagedCollections>
                                    <th>Managed</th>
                                    </#if>
                                    <th>Remove</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>&nbsp;</td>
                                    <td>&nbsp;</td>
                                    <td>&nbsp;</td>
                                    <#if useManagedCollections>
                                    <td>&nbsp;</td>
                                    </#if>
                                    <td>&nbsp;</td>
                                </tr>
                            </tbody>
                        </table>
                                    
                    </div>
                </div>
          </div>
          <div id="addResources" class="tab-pane fade">
                <@edit.resourceDataTable showDescription=false clickable=true limitToCollection=showLimitToCollection span="col-12" useUnmanagedCollections=administrator>
                </@edit.resourceDataTable>
          </div>
    </div>
    <#else>
         <@edit.resourceDataTable showDescription=false clickable=true limitToCollection=showLimitToCollection  span="col-12" useUnmanagedCollections=administrator>
         </@edit.resourceDataTable>
    </#if>
    
            <div id="divNoticeContainer" style="display:none">
                <div id="divAddProjectToCollectionNotice" class="alert">
                    <button type="button" class="close" data-dismiss="alert" data-dismiss-cookie="divAddProjectToCollectionNotice">×</button>
                    <em>Reminder:</em> Adding projects to a collection does not include the resources within a project.
                </div>
            </div>

        </div>
    
         <#include 'vue-edit-collection.html' />


            <@edit.submit fileReminder=false class="button btn submitButton btn-primary">
            <p><b>Where to go after save:</b><br/>
				<input type="radio" name="alternateSubmitAction" id="alt-submit-view" <#if !newRecord>checked=checked</#if> value="" class="inline radio" emptyoption="false">
				<label for="alt-submit-view" class="inline radio">View Page</label>
				<input type="radio" name="alternateSubmitAction" id="alt-submit-rights" value="Assign Permissions" class="inline radio" emptyoption="false" >
				<label for="alt-submit-rights" class="inline radio" <#if newRecord>checked=checked</#if>>Assign Permissions</label>
            <br>
            <br>
			</p>
            </@edit.submit>
        </@s.form>
        </div>
        <#noescape>
        <script type='text/javascript'>
        
        //selectResourcesFromCollectionid
        $(document).on('shown.bs.tab', 'a[data-toggle="tab"]', function (e) {
            var table = $.fn.dataTable.fnTables(true);
            if ( table.length > 0 ) {
                  $(table).dataTable().fnAdjustColumnSizing();
            }
        })

    var vm;

    $(function () {
        'use strict';
        TDAR.datatable.setupDashboardDataTable({
            enableUnmanagedCollections : ${(administrator!false)?string},
            isAdministrator: ${(editor!false)?string},
            limitContext: ${((!editor)!true)?string},
            isSelectable: false,
            isClickable: true,
            showDescription: false,
            selectResourcesFromCollectionid: $("#metadataForm_id").val()
        });
        
         TDAR.datatable.setupCollectionResourcesDataTable({
            enableUnmanagedCollections: ${(administrator!false)?string},
            isAdministrator: ${(editor!false)?string},
            limitContext: ${((!editor)!true)?string},
            isSelectable: false,
            showDescription: false,
            selectResourcesFromCollectionid: $("#metadataForm_id").val()
        });
        
        var form = $("#metadataForm")[0];
        vm = TDAR.vuejs.editcollectionapp.init({enableUnmanagedCollections: ${(editor!false)?string}});
        
        TDAR.common.initEditPage(form);
        TDAR.datatable.registerResourceCollectionDataTable("#resource_datatable", "#tblCollectionResources");
        //TDAR.datatable.registerResourceCollectionDataTable("#resource_datatablepublic", "#tblCollectionResourcespublic",false);
        TDAR.autocomplete.applyCollectionAutocomplete($("#txtParentCollectionName"), {showCreate: false}, {permission: "ADMINISTER_COLLECTION"});
        TDAR.autocomplete.applyCollectionAutocomplete($("#txtAltParentCollectionName"), {showCreate: false}, {permission: "ADMINISTER_COLLECTION"});
        TDAR.datatable.registerAddRemoveSection(${(id!-1)?c});
        //remind users that adding a project does not also add the project's contents
		$("#clearButton").click(function() {$('#fileUploadField').val('');return false;});
        });
        </script>
        </#noescape>
<div style="display:none"></div>
</div>
<div class="col-2">
    <nav id='subnavbar'  class="bg-light" >
    <div class=" col-12">
        <p>Jump to Section:</p>
    <ul class="list-unstyled">
                    <li class="active"><a class="nav-link" href="#basicInformationSection">Basic</a></li>
                    <li><a class="nav-link" href="#divResourcesSesction">Resources</a></li>
                    </ul>
                    <div class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</div>
                    <img alt="progress indicator" title="progress indicator"  src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
		</div>
		</div>
    </nav>

</div>
</div>
</body>
</#escape>

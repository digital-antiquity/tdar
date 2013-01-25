<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<#if persistable.id == -1>
<title>Create a Collection</title>
<#else>
<title>Editing: ${persistable.name}</title>
</#if>
<meta name="lastModifiedDate" content="$Date$"/>

<@edit.resourceJavascript formSelector="#metadataForm" />
<@edit.resourceDataTableJavascript false true />
<script type="text/javascript">


$(function(){
    registerResourceCollectionDataTable();
});


</script>
</head>
<body>
<div id="sidebar-right" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the editing form for a Collection.
    </div>
</div>


<h1><#if persistable.id == -1>Creating<#else>Editing</#if>: <span> ${persistable.name!"New Collection"}</span></h1>
<@s.form name='metadataForm' id='metadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>

<h2>Basic Information</h2>
<div class="" tiplabel="Basic Information"  tooltipcontent="Enter a name and description for this collection.  You may also choose a &quot;parent 
    collection&quot; which allows you to inherit all of the access permissions defined by the parent.">
  <#if resourceCollection.id?? &&  resourceCollection.id != -1>
      <@s.hidden name="id"  value="${resourceCollection.id?c}" />
  </#if>
  <@s.hidden name="startTime" value="${currentTime?c}" />

<@s.select labelposition='left' label='Parent Collection' emptyOption='true' name='parentId' 
    listKey='id' listValue='name' list='%{candidateParentResourceCollections}'
    truncate="80" title="Please select a parent collection"
    cssClass="input-xxlarge"
    />
<@s.textfield labelposition='left' label='Collection Name' name='resourceCollection.name'  cssClass="required descriptiveTitle input-xxlarge"  title="A title is required for all collections." maxlength="255" />
<p class='field'>
</p>
<@s.textarea labelposition='top' label='Collection Description' name='resourceCollection.description'  
     cssClass='resizable input-xxlarge' title="Please enter the description " />


</div>

<div id="divBrowseOptionsTips" style="display:none">
    <p>Choose whether this collection will be public or private, and how ${siteAcronym} will sort the resources when displaying this collection to other users.</p>
    <ul>
        <li>Public collections are viewable to all ${siteAcronym} users and accessible from the &quot;Browse Collections&quot page.</li>
        <li>Private collections are only viewable to the users specified in the <a href="#accessRights">Access Rights</a> section.</li>
    </ul>
</div>
<div class="glide" tiplabel="Browse and Display Options" tooltipcontent="#divBrowseOptionsTips">
<h2>Browse and Display Options</h2>
<div class="control-group">
<label class="control-label">Make this collection public?</label>
<div class="controls">
    <label for="rdoVisibleTrue" class="radio inline"><input type="radio" id="rdoVisibleTrue" name="resourceCollection.visible" value="true" <@edit.checkedif resourceCollection.visible true /> />Yes</label> 
    <label for="rdoVisibleFalse" class="radio inline"><input type="radio" id="rdoVisibleFalse" name="resourceCollection.visible" value="false" <@edit.checkedif resourceCollection.visible false /> />No</label>
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
    <dt>View All</dt>
    <dd>User can view/download all file attachments associated with the resources in the collection.</dd>
    <dt>Modify Record<dt>
    <dd>User can edit the resources listed in the collection.<dd>
    <dt>Administer Collection<dt>
    <dd>User can edit resources listed in the collection, and also modify the contents of the collection.<dd>
</dl>
</div>
<@edit.fullAccessRights tipsSelector="#divCollectionAccessRightsTips" />

<div class="glide" tiplabel="Add/Remove Resources" tooltipcontent="Check the items in this table to add them to your collection.  Navigate the pages
                    in this list by clicking the left/right arrows at the bottom of this table.  Use the input fields above the table to limit the number
                    of results.">
    <h2>Add/Remove Resources</h2>
    <@edit.resourceDataTable false true />


    
    <div id="divSelectedResources">
    <#list resources as resource>
        <input type="hidden" name="resources.id" value="${resource.id?c}" id="hdnResourceId${resource.id?c}" />
    </#list>
    </div>
</div>    

<div class="glide" >
    <h2>Selected Resources</h2>
    <@view.resourceCollectionTable true />
</div>


    <@edit.submit fileReminder=false />
</@s.form>


</body>
</#escape>

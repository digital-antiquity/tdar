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

<@edit.resourceJavascript formSelector="#MetadataForm" />
<@edit.resourceDataTableJavascript false true />
<script type="text/javascript">


$(function(){
    //if user is editing existing collection, gather the hidden elements and put them in the 'seleted rows' object
    var selectedRows = {};
    $.each($('input', '#divSelectedResources'), function(ignored, item){
        var elem = this;
        selectedRows[elem.value] = {id:elem.value, title:'n/a', description:'n/a'};
        console.debug('adding id to preselected rows:' + elem.value);  
    });
    $dataTable.data('selectedRows', selectedRows);
    
    //hide the selected items table if server hasn't prepopulated it
    var $table = $('#tblCollectionResources');
    if($table.find('tr').length==1) {
        $table.hide();
    }
});


function rowSelected(obj) {

    //first, add the hidden input tag to the dom
    var tag = '<input type="hidden" name="resources.id" value="' + obj.id + '" id="hdnResourceId' + obj.id + '"/>';
    console.log("adding selected resource:" + tag);
    $('#divSelectedResources').append(tag);

    //next, add a new row to the 'selected items' table.
    var $table = $('#tblCollectionResources');
    var $tbody = $('tbody', $table);
    var resourceTag = '';
        resourceTag += '<tr id="dataTableRow_:id">                                                                   ';
        resourceTag += '    <td>:id</td>                                                                             ';
        resourceTag += '    <td>                                                                                      ';
        resourceTag += '        <a href="/:urlNamespace/:id" target="resourcedetail" >                                    ';
        resourceTag += '            :title        ';
        resourceTag += '        </a>                                                                                  ';
        resourceTag += '    </td>                                                                                     ';
        resourceTag += '    <td><button class="addAnother minus"  type="button" tabindex="-1" onclick="removeResourceClicked(:id, this);false;"><img src="/images/minus.gif" class="minus"></button></td>';
        resourceTag += '</tr>                                                                                         ';

       resourceTag = resourceTag.replace(/:id/g, obj.id);
       resourceTag = resourceTag.replace(/:urlNamespace/g, obj.urlNamespace);
       resourceTag = resourceTag.replace(/:title/g, obj.title);
       resourceTag = resourceTag.replace(/:description/g, obj.description);
       resourceTag = resourceTag.replace(/:status/g, obj.status);
       
       $tbody.append(resourceTag);
       //$table.closest('div').show();
       $table.show();
       applyZebraColors();
}

function rowUnselected(obj) {
    console.log('removing selected reosurce:' + obj.id);
    $('#hdnResourceId' + obj.id).remove();
    
    var $row = $('#dataTableRow_' + obj.id);
    var $table = $row.closest('table');
    //var $div = $row.closest('div');
    $row.remove();
    if($table.find('tr').length == 1) $table.hide(); //FIXME: DRY

}

function removeResourceClicked(id, elem) {
    //delete the element from the selectedrows structure and remove the hidden input tag
    delete $dataTable.data('selectedRows')[id];
    $('#hdnResourceId' + id).remove();
    
    //now delete the row from the table
    var $elem = $(elem);
    var $tr = $elem.closest('tr');  
    var $div = $elem.closest('div');
    $tr.remove();
    
    //if the table is empty,  hide the section
    if($('tr', $div).length == 1) { //one header row
        //$div.hide();
        $table.hide();
    }
    
    //if the datatable is on a page that shows the corresponding checkbox,  clear the checkbox it
    $('#cbEntityId_' + id, $dataTable).prop('checked', false);
    
}


</script>
</head>
<body>

<@edit.toolbar "collection" "edit" />

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>

<div class="glide" tiplabel="Basic Information"  tooltipcontent="Enter a name and description for this collection.  You may also choose a &quot;parent 
    collection&quot; which allows you to inherit all of the access permissions defined by the parent.">
  <#if resourceCollection.id?? &&  resourceCollection.id != -1>
      <@s.hidden name="id"  value="${resourceCollection.id?c}" />
  </#if>
  <@s.hidden name="startTime" value="${currentTime?c}" />

<@s.select labelposition='left' label='Parent Collection' emptyOption='true' name='parentId' 
    listKey='id' listValue='name' list='%{candidateParentResourceCollections}'
    truncate="80" title="Please select a parent collection" />
<br />
<@s.textfield labelposition='left' label='Collection Name' name='resourceCollection.name'  cssClass="required descriptiveTitle input-xxlarge"  title="A title is required for all collections." maxlength="512" />
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
<h3>Browse and Display Options</h3>
<div class="control-group">
<label class="control-label">Make this collection public?</label>
<div class="controls">
	<label for="rdoVisibleTrue" class="radio inline"><input type="radio" id="rdoVisibleTrue" name="resourceCollection.visible" value="true" <@edit.checkedif resourceCollection.visible true /> />Yes</label> 
	<label for="rdoVisibleFalse" class="radio inline"><input type="radio" id="rdoVisibleFalse" name="resourceCollection.visible" value="false" <@edit.checkedif resourceCollection.visible false /> />No</label>
</div>
</div>

<br/>
<@s.select labelposition='top' label='When Browsing Sort Resource By:' name='resourceCollection.sortBy' 
     listValue='label' list='%{sortOptions}'
    truncate="80" title="Sort resource by" />
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
<@edit.fullAccessRights "#divCollectionAccessRightsTips"/>

<div class="glide" tiplabel="Add/Remove Resources" tooltipcontent="Check the items in this table to add them to your collection.  Navigate the pages
                    in this list by clicking the left/right arrows at the bottom of this table.  Use the input fields above the table to limit the number
                    of results.">
    <h3>Add/Remove Resources</h3>
    <@edit.resourceDataTable false true />


    
    <div id="divSelectedResources">
    <#list resources as resource>
        <input type="hidden" name="resources.id" value="${resource.id?c}" id="hdnResourceId${resource.id?c}" />
    </#list>
    </div>
</div>    

<div class="glide" >
    <h3>Selected Resources</h3>
    <@view.resourceCollectionTable true />
</div>


    <@edit.submit fileReminder=false />
</@s.form>

</div>
<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the editing form for a Collection.
    </div>
</div>

</body>
</#escape>

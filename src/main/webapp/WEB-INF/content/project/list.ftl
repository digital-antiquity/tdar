<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<head>
<title>${authenticatedUser.properName}'s Information Resources</title>
<meta name="lastModifiedDate" content="$Date$"/>


<script type="text/javascript">

 function projToolbarItem(link, image, text) {
	return '<li><a href="' + link + '"><img alt="toolbar item" src="' + image + '"/>' + text + '</a></li>';
 }

$(document).ready(function() {
	
	// set the project selector to the last project viewed from this page
	// if not found, then select the first item 
	var prevSelected = $.cookie("tdar_datatable_selected_project");
	if (prevSelected != null) {
        var elem = $('#project-selector option[value=' + prevSelected + ']');
        if(elem.length) {
//            console.debug("auto-selecting last project viewed");
            elem.attr("selected", "selected");
        } else {
//            console.debug("couldn't find previously selected project - picking first from list");
            $("#project-selector").find("option :first").attr("selected", "selected");
        }

	}
	
	// re-apply any resource type filters
	$(":checkbox.resourceTypes").each( function() {
	  	if ($.cookie($(this).attr("id")) != null) {
	  		$(this).attr('checked', false);
	  	}
	});

    drawToolbar($("#project-selector").val());

	// define the datatable
	$("#resource_datatable").dataTable({
	    "bJQueryUI": true,
	    "sPaginationType": "full_numbers",
		"bServerSide": true,
		"sAjaxSource": '/project/datatable',
		"bStateSave": false,
		"bProcessing": true,
		"sScrollY": "650px",
		"fnServerData": function(sSource, aoData, fnCallback) {
			// append the query parameters that the ProjectDatatableController 
			// endpoint expects
			sSource = sSource + "?projectId=" + $("#project-selector").val();
            $("#resource_datatable_wrapper").find("div:first-child").removeClass("ui-corner-tl ui-corner-tr"); 
			$(".resourceTypes:checked").each(function() {
				sSource = sSource + "&types=" + $(this).val();
			});
			$.ajax( {
				"dataType": 'jsonp', 
				"url": sSource, 
				"data": aoData, 
				"success": fnCallback
			});
		},
		"aoColumns": [
			{ "sTitle": "ID", "sWidth": "5%", "bSortable": true },
			{ "sTitle": "Title/Description", "bSortable": true },
			{ "sTitle": "Type", "sWidth": "10%", "bSortable": true }
		]
	});
	
	$("#project-selector").change(function() {
		var projId = $(this).val();
		$.cookie("tdar_datatable_selected_project", projId);
        drawToolbar(projId);
	    $("#resource_datatable").dataTable().fnDraw();
    });
    
    $(":checkbox.resourceTypes").click(function() {
      $("#resource_datatable").dataTable().fnDraw();
      if($(this).is(":checked")) {
        $.cookie($(this).attr("id"), null);
    } else {
      $.cookie($(this).attr("id"), "unchecked");
      }
    });

});


function drawToolbar(projId) {
        var toolbar = $("#proj-toolbar");
        toolbar.empty();
        if (projId != undefined && projId != '') {
            toolbar.append(projToolbarItem('/project/' + projId + '/view', '/images/zoom.png', ' View selected project'));
            toolbar.append(projToolbarItem('/project/' + projId + '/edit', '/images/pencil.png', ' Edit project'));
            toolbar.append(projToolbarItem('/resource/add?projectId=' + projId, '/images/database_add.png', ' Add new resource to project'));
        }
}
</script>

<style type="text/css">
    div.recent-title {display:inline-block; width:600pt;}
    div.recent-nav {display:inline-block; float:right;}
    #recentlyEditedResources li:hover{background-color: #eee9d5} 
    #emptyProjects li:hover{background-color: #eee9d5} 
</style>
</head>

<div class="glide">
Welcome back, ${authenticatedUser.firstName}!  The resources you can access are listed below.  To create a new resource or project, use the "new" menu above.
<br/>
</div>

<#if (activeResourceCount == 0)>
<div class="glide">
<h3>Getting Started</h3>
<ol>
    <li><a href="<@s.url value="/project/add"/>">Start a new Project</a></li>
    <li><a href="<@s.url value="/resource/add"/>">Add a new Resource</a></li>
</ol>
</div>

<#else>
<div class="glide">
<h3>Recently Updated Items</h3>
<ol id='recentlyEditedResources'>
    <@s.iterator value='recentlyEditedResources' status='recentEditStatus' var='res'>
    <li id="li-recent-resource-${res.id?c}">
<!--        <div class="recent-title">-->
            <a href="<@s.url value='/${res.urlNamespace}/view' resourceId='${res.id?c}'/>"><@rlist.abbr>${res.title}</@rlist.abbr></a> [${res.resourceType.label}]
<#--            <#if res.project??>
                <span class="resource-project">(<@rlist.abbr maxlen=50>${res.project.title}</@rlist.abbr>)</span>
            </#if> -->
<!--        </div>-->
        <div class="recent-nav">
            <a href="<@s.url value='/${res.urlNamespace}/edit' resourceId='${res.id?c}'/>">edit</a> |
            <a href="<@s.url value='/${res.urlNamespace}/delete' resourceId='${res.id?c}'/>" onclick='return confirm("Really delete this resource?  This cannot be undone.")' >delete</a>
        </div>
    </li>
    </@s.iterator>
</ol>
</div>

</#if>

<#if (emptyProjects?? && !emptyProjects.empty )>
<div class="glide" id="divEmptyProjects">
    <h3>Empty Projects</h3>
    <ol id="emptyProjects">
    <@s.iterator value='emptyProjects' status='recentEditStatus' var='res'>
    <li id="li-recent-resource-${res.id?c}">
            <a href="<@s.url value='/${res.urlNamespace}/view' resourceId='${res.id?c}'/>">
                <@rlist.abbr>${res.title}</@rlist.abbr>
            </a> 
        <div class="recent-nav">
            <a href="<@s.url value='/resource/add?projectId=${res.id?c}' resourceId='${res.id?c}'/>" title="add a resource to this project">add resource</a> |
            <a href="<@s.url value='/${res.urlNamespace}/edit' resourceId='${res.id?c}'/>">edit</a> |
            <a href="<@s.url value='/${res.urlNamespace}/delete' resourceId='${res.id?c}'/>" onclick='return confirm("Really delete this resource?  This cannot be undone.")' >delete</a>
        </div>
    </li>
    </@s.iterator>
    </ol>
</div>
</#if>

<div class="glide" id="project-list">
<h3>Browse Resources By Project</h3>
<form action=''>
<div class="fg-toolbar ui-toolbar ui-widget-header ui-corner-tl ui-corner-tr ui-helper-clearfix" 
style="border-bottom: 0px solid;">
<label for="project-selector">Project:</label>
<select id="project-selector">
    <option value="" selected='selected'>All Editable Projects</option>
  <#if allSubmittedProjects?? && !allSubmittedProjects.empty>
  <optgroup label="Your Projects">
  	<@s.iterator value='allSubmittedProjects' status='projectRowStatus' var='submittedProject'>
	    <option truncate='80' value="${submittedProject.id?c}">${submittedProject.selectOptionTitle}</option>
    </@s.iterator>
  </optgroup>
   <#else>
     <optgroup label="Projects you created - none"/>   
  </#if>
  
  <optgroup label="Projects you have been given access to">
    <@s.iterator value='fullUserProjects' var='editableProject'>
	    <option value="${editableProject.id?c}">${editableProject.selectOptionTitle}</option>
    </@s.iterator>
  </optgroup>
</select>
<br/>
<div>
    <label>Resource Type:</label>
    <div class="field col4"> 
        <input type="checkbox" class="resourceTypes" value="DOCUMENT" id="rtDocument" checked='checked'/>
        <label for="rtDocument">Documents</label>
        <input type="checkbox" class="resourceTypes" value="DATASET" id="rtDataset" checked='checked'/>
        <label for="rtDataset">Datasets</label>
        <input type="checkbox" class="resourceTypes" value="CODING_SHEET" id="rtCodingSheet" checked='checked'/>
        <label for="rtCodingSheet">Coding Sheets</label><br/>
        <input type="checkbox" class="resourceTypes" value="IMAGE" id="rtImage" checked='checked'/>
        <label for="rtImage">Images</label>
        <input type="checkbox" class="resourceTypes" value="ONTOLOGY" id="rtOntology" checked='checked'/>
        <label for="rtOntology">Ontologies</label>
        
        <input type="checkbox" class="resourceTypes" value="SENSORY_DATA" id="rtSensoryData" checked='checked'/>
        <label for="rtSensoryData">Sensory Data</label>
    </div>
</div>
<ul id="proj-toolbar" class="projectMenu"><li></li></ul>
</div>
<table cellpadding="0" cellspacing="0" border="0" class="display" id="resource_datatable"></table>
</form>
</div>


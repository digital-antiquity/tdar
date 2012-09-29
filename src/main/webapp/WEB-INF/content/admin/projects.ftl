<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<head>
<title>Administrator Dashboard: All Projects</title>
<meta name="lastModifiedDate" content="$Date$"/>
<script type="text/javascript">
$(document).ready(function() {
  // hide the all of the elements with class info_resource unless they were
  // previously expanded in this session.
  jQuery.each($(".info_resources"), function() {
  	if ($.cookie($(this).attr("id")) == null) {
            $(this).hide();
  	} else {
            $(this).prev(".proj").find(".proj_expander").attr("src", "<@s.url value="/images/arrow-expanded.png"/>");
  	}
  });
  
  // toggle the closest component with class info_resource
  $(".proj_expander").click(function() {
    var info_resc = $(this).closest(".proj").next(".info_resources");
    info_resc.toggle();
    
    // set a cookie with the id to save toggle state
    if(info_resc.is(":visible")) {
    	$.cookie(info_resc.attr("id"), "visible")
    	$(this).attr("src", "<@s.url value="/images/arrow-expanded.png"/>");
    } else {
    	$.cookie(info_resc.attr("id"), null);
    	$(this).attr("src", "<@s.url value="/images/arrow-collapsed.png"/>");
    }
  });
});
</script>
</head>

<h2>All resources</h2>
<hr/>
Welcome back, ${authenticatedUser.firstName}.  You have <b>administrator
access</b> and can edit all of the information resources below.  
Click on the green arrow <img src='<@s.url value="/images/arrow-collapsed.png"/>'/> for more information about
a project or information resource.  
<hr/>

<table width="100%">
<tbody>
<@s.iterator value='allProjects' status='projectRowStatus' var='project'>
  <tr class="proj">
  <td valign="middle" style="width:16px;">
  <#if ! project.informationResources.isEmpty() >
    <img src='<@s.url value="/images/arrow-collapsed.png"/>' class="proj_expander" />
  </#if>
  </td>
  <td valign="middle">
  <h3><span class='highlight'>${project.title}</span></h3>
  <div><b>Description:</b> ${project.description!"No description specified."}</div>
  <div><b>Submitted by:</b> ${project.submitter}</div>
  <div><b>Date registered:</b> ${project.dateRegistered}</div>
  </td>
  <td valign="middle" style="width:200px;">
  <ul style="list-style:none;">
    <li><a href="<@s.url value='/project/view' resourceId='${project.id?c}'/>"><img src='<@s.url value="/images/zoom.png"/>' />View metadata</a></li>
    <li><a href="<@s.url value='/project/edit' resourceId='${project.id?c}'/>"><img src='<@s.url value="/images/pencil.png"/>' />Edit metadata</a></li>
    <li><a href="<@s.url value='/resource/add' projectId='${project.id?c}'/>"><img src='<@s.url value="/images/add.png"/>'/>Add resource</a></li>
    <li><a href="<@s.url value='/project/delete' resourceId='${project.id?c}'/>" onclick='return confirm("Really delete this project?  This cannot be undone.")' > <img src='<@s.url value="/images/delete.png"/>'/>Delete project</a></li>
  </ul>        
  </td>
  </tr>
  <tr class="info_resources" id="info_resources_${project.id?c}">
    <td colspan="3">
    <div style="padding-left:29px;"><b>Information Resources:</b></div>
    <div style="padding-left:50px;">
    <#if project.informationResources.isEmpty() >
      No information resources have been associated with this project.
    <#else>
      <@rlist.informationResources iterable="#project.datasets" title="Datasets" displayable=!project.datasets.empty/>
      <@rlist.informationResources iterable="#project.codingSheets" title="Coding sheets" displayable=!project.codingSheets.empty/>
      <@rlist.informationResources iterable="#project.documents" title="Documents" displayable=!project.documents.empty/>
      <@rlist.informationResources iterable="#project.ontologies" title="Ontologies" displayable=!project.ontologies.empty/>

    </#if>
    </div>
    </td>
  </tr>
</@s.iterator>
</tbody>
</table>

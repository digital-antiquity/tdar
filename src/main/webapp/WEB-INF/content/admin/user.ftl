<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "admin-common.ftl" as admin>
<head>
<title>User Info Pages</title>
<script type="text/javascript">

function fnRenderId(oObj) {
    //in spite of name, aData is an object containing the resource record for this row
    var objResource = oObj.aData;
    var html = '<a href="'  + getURI('browse/creators/' + objResource.id) + '" class=\'title\'>' + objResource.id + '</a>';
    return html;
}

$(function() {
    var settings  = {
        tableSelector: '#dataTable',
        sAjaxSource:'/lookup/person',
  		"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span4'i><'span5'p>>",
        sPaginationType:"bootstrap",
        "bLengthChange": true,
        "bFilter": true,
        aoColumns:[
                   {sTitle:"id", bUseRendered: false, mDataProp:"id", tdarSortOption:'ID', bSortable:false, fnRender: fnRenderId, bUseRendered:false},
                   {sTitle:"First", mDataProp:"firstName", tdarSortOption:'FIRST_NAME', bSortable:false},
                   {sTitle:"Last", mDataProp:"lastName", tdarSortOption:'LAST_NAME', bSortable:false},
                   {sTitle:"Email", mDataProp:"email", tdarSortOption:'CREATOR_EMAIL', bSortable:false}],
        sAjaxDataProp: 'people',
        selectableRows: false,
        requestCallback: function() {return {minLookupLength:0,registered:'true',term: $("#dataTable_filter input").val()};}
    };
    
    var dataTable = registerLookupDataTable(settings);
});

    
</script>

</head>

<@admin.header />

<@admin.statsTable historicalUserStats "User Statistics" "userstats" />



<div class="glide">
<h3>Recently Registered Users </h3>
<table class="tableFormat">
<thead>
  <tr>
    <th>User</th>
    <th>Email</th>
    <th>Date Registered</th>
  </tr>
 </thead>
   <#list recentUsers as user>
     <tr>
       <td> <a href="<@s.url value="/browse/creators/${user.id?c}"/>">${user.properName}</a></td>
       <td> ${user.email}</td>
       <td> ${user.dateCreated!""}</td>
       </tr>
       <tr>
       <td colspan=3><div style="padding-left:25px">${user.contributorReason!""}</div></td>
     </tr>
   </#list>
</table>
</div>

<div class="glide">
<h3>Registered Users</h3>
<table id="dataTable"></table>
</div>
</#escape>

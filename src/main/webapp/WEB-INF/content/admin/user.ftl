<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "admin-common.ftl" as admin>
<head>
<title>User Info Pages</title>
<script type="text/javascript">
$(function() {
    var settings  = {
        tableSelector: '#dataTable',
        sAjaxSource:'/lookup/person',
        "bLengthChange": true,
        "bFilter": true,
        aoColumns:[
                   {sTitle:"id", bUseRendered: false, mDataProp:"id", tdarSortOption:'ID', bSortable:false},
                   {sTitle:"First", mDataProp:"firstName", tdarSortOption:'FIRST_NAME', bSortable:false},
                   {sTitle:"Last", mDataProp:"lastName", tdarSortOption:'LAST_NAME', bSortable:false},
                   {sTitle:"Email", mDataProp:"email", tdarSortOption:'CREATOR_EMAIL', bSortable:false}],
        sPaginationType:"full_numbers",
        sAjaxDataProp: 'people',
        selectableRows: false,
        requestCallback: function() {return {minLookupLength:0,registered:'true'};},
        sDom:'<"datatabletop"ilrp>t<>' //omit the search box
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

<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>

<head>
    <title>All Accounts</title>
</head>
<body>
<h1>All Accounts</h1>
<table class="table table-sm table-striped" id="tblAllAccounts">
      <thead class="thead-dark">

        <tr>
            <th>Name</th>
            <th>Owner</th>
            <th>Status</th>
            <th>Total</th>
            <th>Files</th>
            <th>Resources</th>
            <th>Space</th>
        </tr>
    </thead>
    <tbody>
        <#list accounts as account>
        <tr class="${account.status}">
            <td><a href="<@s.url value="/billing/${account.id?c}"/>">${account.name}</a></td>
            <td><a href="<@s.url value="${account.owner.detailUrl}"/>">${account.owner.properName} </a></td>
            <td>${account.status}</td>
            <td>$${account.totalCost}</td>
            <td>${account.totalNumberOfFiles} (${account.filesUsed})</td>
            <td>${account.totalNumberOfResources} (${account.resourcesUsed})</td>
            <td>${account.totalSpaceInMb} (${account.spaceUsedInMb})</td>
        </tr>
        </#list>
    </tbody>
</table>
<script type="text/javascript">
    $(function () {
        TDAR.datatable.extendSorting();
        $("#tblAllAccounts").dataTable({"bPaginate": false});
    });
</script>
</body>
</#escape>
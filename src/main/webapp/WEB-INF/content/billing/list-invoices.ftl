<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>

<head>
	<title>All Invoices</title>
 </head>
<body>
<h1>All Invoices</h1>
<table class="table tableFormat" id="tblAllInvoices">
    <thead>
        <tr>
            <th>Date</th>
            <th>Owner</th>
            <th>Transaction Type</th>
            <th>Status</th>
            <th>Files</th>
            <th>Resources</th>
            <th>Space</th>
            <th>Total</th>
        </tr>
    </thead>
    <tbody>
            <#list invoices as invoice>
              <#if invoice.transactionStatus.complete>
                <tr>
                   <td><a href="<@s.url value="/cart/${invoice.id?c}"/>">${invoice.dateCreated}</a></td>
                   <td>${invoice.owner.properName} </td>
                   <td>${invoice.paymentMethod!""}</td>
                   <td>${invoice.transactionStatus}</td>
                   <td>${invoice.totalNumberOfFiles} </td>
                   <td>${invoice.totalResources}</td>
                   <td>${invoice.totalSpaceInMb}</td>
                   <td>$${invoice.total!0}</td>
                </tr>
               </#if>
            </#list>
    </tbody>
</table>

<script type="text/javascript">
    $(function(){
        $("#tblAllInvoices").dataTable({"bFilter": false, "bInfo": false, "bPaginate":false})
    });
</script>

</body>
</#escape>
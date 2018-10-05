<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>

<head>
    <title>All Invoices</title>
</head>
<body>
<h1>All Invoices</h1>
<table class="table table-sm table-striped" id="tblAllInvoices">
      <thead class="thead-dark">

    <tr>
		<th></th>	
        <th>Date</th>
        <th>Owner</th>
        <th>Transaction Type</th>
        <th>Status</th>
        <th>Files</th>
        <th>Resources</th>
        <th>Space</th>
        <th>Total</th>
        <th>Note</th>
        <th>Coupon?</th>        
        
    </tr>
    </thead>
    <tbody>
        <#list invoices as invoice>
            <#if invoice.transactionStatus.complete>
            <tr class="<#if invoice.cancelled>strikethrough</#if>" >
                <td><a href="<@s.url value="/cart/${invoice.id?c}"/>">view</a></td>
                <td>${invoice.dateCreated}</td>
                <td><a href="<@s.url value="/browse/creators/${invoice.owner.id?c}"/>">${invoice.owner.properName}</a></td>
                <td>${invoice.paymentMethod!""}</td>
                <td>${invoice.transactionStatus}</td>
                <td>${invoice.totalNumberOfFiles} </td>
                <td>${invoice.totalResources}</td>
                <td>${invoice.totalSpaceInMb}</td>
                <td>$${invoice.total!0}</td>
                <td>${invoice.otherReason!""}</td>
                <td>${(invoice.coupon?has_content)?string("yes","")}</td>
            </tr>
            </#if>
        </#list>
    </tbody>
</table>

<script type="text/javascript">
    $(function () {
        TDAR.datatable.extendSorting();
        $("#tblAllInvoices").dataTable({"bFilter": false, "bInfo": false, "bPaginate": false, aaSorting:[]})
    });
</script>

</body>
</#escape>
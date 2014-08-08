<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages - emails </title>
<@admin.header />

<table class="tableFormat table">
<thead>
<tr>
<th>To</th>
<th>From</th>
<th>Date</th>
<th>Status</th>
<th>Subject</th>
</tr>
</thead>
<#list emails as email>
    <tr>
        <td>${email.to}</td>
        <td>${email.from}</td>
        <td>${email.date?string.short}</td>
        <td>${email.status}</td>
        <td>${email.subject}</td>
    </tr>
    <tr>
        <td colspan=5>
        <pre>${email.message}</pre>
        <hr/>
        </td>
    </tr>
</#list>
</table>
</#escape>
<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages - Bounced Email Messages </title>
<@admin.header />

<head>
    <style>
    </style>

</head>

<h3>AWS Queue Messages</h3>

<table class="tableFormat table">
<thead>
<tr>
    <th>Status</th>
    <th>Message</th>
</tr>
</thead>

<#if allMessages?hasContent>
    There are ${allMessages?size} messages
</#if>


<#if allMessages?hasContent && (allMessages?size > 0)>
<#list allMessages as pair>
    <tr>
        <td>${pair.first}</td>
        <td>${pair.second}</td>
    </tr>
</#list>
<#else>
    <tr>
        <td colspan="2">There are no messages in the queue</td>
    <td>

</#if>
</table>

</#escape>
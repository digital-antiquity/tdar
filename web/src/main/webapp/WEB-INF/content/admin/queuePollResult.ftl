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



<#if bouncedMessages?hasContent>
    There are ${allMessages?size} bounced messages<br />
</#if>
<#if allMessages?hasContent>
    There are ${allMessages?size} total messages<br />
</#if>


<#if bouncedMessages?hasContent && (bouncedMessages?size > 0)>
    <h4>Bounced Messages</h4>
    <table class="tableFormat table">
    <thead>
        <tr>
            <th>Status</th>
            <th>Message</th>
        </tr>
    </thead>
    <#list bouncedMessages as pair>
        <tr>
            <td>${pair.first}</td>
            <td>${pair.second}</td>
        </tr>
    </#list>
    </table>
</#if>


<h4>All Messages</h4>
<table class="tableFormat table">
<thead>
    <tr>
        <th>Status</th>
        <th>Message</th>
    </tr>
</thead>
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
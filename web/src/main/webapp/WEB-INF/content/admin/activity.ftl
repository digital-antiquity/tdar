<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
    <title>Administrator Dashboard: Recent Activity</title>
</head>

<h2>Recent activity</h2>
<hr/>
    <@s.actionerror />

<table class="table table-striped">
    <thead>
    <tr>
        <th>Resource ID</th>
        <th>Modified by</th>
        <th>Log message</th>
        <th>Date</th>
    </tr>
    </thead>
    <tbody>
        <@s.iterator value='resourceRevisionLogs' status='rowStatus' var='resourceRevisionLog'>
        <tr>
            <#if resourceRevisionLog.resource??>
                <td>${resourceRevisionLog.resource.id?c}</td>
            <#else>
                <td><em><abbr title="Resource information not available or does not apply">n/a</abbr></em></td>
            </#if>
            <td>${resourceRevisionLog.person}</td>
            <td>${resourceRevisionLog.logMessage}</td>
            <td style="white-space: nowrap">${resourceRevisionLog.timestamp?string.short}</td>
        </tr>
        </@s.iterator>
    </tbody>
</table>
</#escape>

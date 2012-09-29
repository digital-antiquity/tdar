<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Administrator Dashboard: Contributor Requests</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>

<h2>Contributor requests</h2>
<hr/>

<table width="60%" class="zebracolors">
<thead><tr><th>Contributor</th><th>Reason</th><th>Date requested</th></tr></thead>
<tbody>
<@s.iterator value='pendingContributorRequests' status='rowStatus' var='contributorRequest'>
<tr>
<td>${contributorRequest.applicant!"<em>n/a</em>"}</td>
<td>${contributorRequest.contributorReason!"<em>n/a</em>"}</td>
<td><@view.shortDate contributorRequest.timestamp /></td>
</tr>
</@s.iterator>
</tbody>
</table>
<script>$(applyZebraColors);</script>

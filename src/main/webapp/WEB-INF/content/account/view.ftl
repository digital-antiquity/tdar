<!--
vim:sts=2:sw=2:filetype=jsp
-->
<head>
<title>account information for ${person.properName} with the Digital Archaeological Record</title>
<meta name="lastModifiedDate" content="$Date$"/>
<script type='text/javascript'>
</script>
</head>
<body>
<div class="glide">
<h3>Welcome, ${person.properName}  </h3>
Now that you've successfully registered an account with tDAR.  Here are some nice places to start:
<ol>
<li><a href="<@s.url value="/project/add"/>">Create a new project</a>.  (Projects in tDAR are simple, easy ways to organize similar Documents, Images, and Datasets which share metadata.)</li>
<li><a href="<@s.url value="/resource/add"/>">Create a new resource</a>.</li>
<li><a href="http://dev.tdar.org/confluence/display/TDAR/User+Documentation">Review the user's manual</a>.</li>
<li><a href="<@s.url value="/project/list" />">Visit "my resources."</a></li>
</ol>

</div>
<div class="glide">
<h3>Account Details</h3>
<table>
<tr>
<td>Name:</td>
<td>${person.properName}</td>
</tr>
<tr>
<td>Email:</td>
<td>${person.email}</td>
</tr>
<tr>
<td>Institution:</td>
<td>${person.institution}</td>
</tr>
<#if person.contributorReason??>
<tr>
<td>Requested contributor access for the following reason(s):</td>
<td>${person.contributorReason}</td>
</tr>
</#if>
<tr>
<td>RPA?</td>
<#if person.rpa>
<td>Yes</td>
<#else>
<td>No</td>
</#if>
</tr>
</table>
</div>
</body> 

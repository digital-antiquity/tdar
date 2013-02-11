<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>

<head>
	<title>All Accounts</title>
 </head>
<body>
<h1>All Accounts</h1>
<ul>
  <#list accounts as account>
   <li><a href="<@s.url value="/billing/${account.id?c}"/>">${account.name}</a> (${account.owner.properName}) </li>
  </#list>
</ul>
</body>
</#escape>
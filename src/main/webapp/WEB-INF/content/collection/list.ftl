<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>

<head>
<title><#if collection??>${collection.name}<#else>All Collections</#if></title>
</head>
<body>
<#if collection??>
<p>${collection.description!""}</p>
</#if>

<ul>
  <#list collections as collection_>
   <li><a href="<@s.url value="/collection/${collection_.id?c}"/>">${collection_.name}</a></li>
  </#list>
</ul>
<#if collection??>
<@list.listResources results collection.sortBy  />
</#if>
</body>
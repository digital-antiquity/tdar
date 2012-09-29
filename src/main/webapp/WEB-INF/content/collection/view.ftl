<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<title>${resourceCollection.name!"untitled collection"}</title>
<meta name="lastModifiedDate" content="$Date: 2011-07-11 08:30:39 -0700 (Mon, 11 Jul 2011) $"/>

<script type='text/javascript'>
    $(initializeView);
</script>

</head>
<body>
<@view.toolbar "collection" "view" />
<#if resourceCollection.visible || viewable>
<div class="glide">
    <#if resourceCollection.parent??><p><b>Part of:</b> <a href="${resourceCollection.parent.id?c}"/>${resourceCollection.parent.name!"(n/a)"}</a></p></#if>
    <p>${resourceCollection.description!"(n/a)"}</p>

<#if (collections?? && collections.size() > 0) >
<B>Collections Contained in this Collection</B>
<ul>
  <#list collections as collection_>
   <li><a href="<@s.url value="/collection/${collection_.id?c}"/>">${collection_.name}</a></li>
  </#list>
</ul>
</#if>

</div>  
    <#if ( results?? && results.size() > 0 )>
<div class="glide">
      <@list.listResources results resourceCollection.sortBy  />
</div>
    </#if>

<#if editable>
<div class="glide">
  <h3>Administrative Information</h3>
    <p><b>Collection Type:</b> ${resourceCollection.type.label}</p>
    <p><b>Visible:</b> ${resourceCollection.visible?string}</p>
    <p><b>Owner:</b> ${resourceCollection.owner}</p>
    <#if resourceCollection.sortBy??><p><b>Sort by:</b> ${resourceCollection.sortBy.label}</p></#if>

    <p><b>Authorized Users:</b></p>
    <@view.authorizedUsers resourceCollection.authorizedUsers />
</div>
</#if>
<#else>
This collection is not accessible
</#if>
</body>

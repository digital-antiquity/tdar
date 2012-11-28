<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<@search.initResultPagination/>
<@search.headerLinks includeRss=false />

<head>
<title>${resourceCollection.name!"untitled collection"}</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@view.toolbar "collection" "view" />
<h1>${resourceCollection.name!"untitled collection"}</h1>
<#if resourceCollection.visible || viewable>
<!-- Don't show header if header doesn't exist -->
<#if resourceCollection.parent?? || resourceCollection.description?? || collections??>
    <div class="glide">
        <#if resourceCollection.parent??><p><b>Part of:</b> <a href="${resourceCollection.parent.id?c}"/>${resourceCollection.parent.name!"(n/a)"}</a></p></#if>
        <p>${resourceCollection.description!"(n/a)"}</p>
    
    <#if (collections?has_content) >
    <B>Collections Contained in this Collection</B>
    <ul>
      <#list collections as collection_>
       <li><a href="<@s.url value="/collection/${collection_.id?c}"/>">${collection_.name}</a></li>
      </#list>
    </ul>
    </#if>
  </div>
</#if>

    <#if ( results?has_content )>
<div class="search">
    <@search.basicPagination "Records" />
</div>

<hr/>
    <@list.listResources resourcelist=results sortfield=resourceCollection.sortBy  titleTag="h5" orientation=resourceCollection.orientation mapPosition="left" />
    
<div class="glide">
</div>
    </#if>

<#if editable>
<div class="glide">
  <h3>Administrative Information</h3>
  
	<@common.resourceUsageInfo />
  
    <p><b>Collection Type:</b> ${resourceCollection.type.label}</p>
    <p><b>Visible:</b> ${resourceCollection.visible?string}</p>
    <p><b>Owner:</b> <a href="<@s.url value="/browse/creators/${resourceCollection.owner.id?c}"/>">${resourceCollection.owner}</a></p>
    <#if resourceCollection.sortBy??><p><b>Sort by:</b> ${resourceCollection.sortBy.label}</p></#if>

    <@view.authorizedUsers resourceCollection />
</div>
</#if>
<#else>
This collection is not accessible
</#if>
<script type='text/javascript'>
$(document).ready(function(){
    $(initializeView);
});
</script>



</body>
</#escape>
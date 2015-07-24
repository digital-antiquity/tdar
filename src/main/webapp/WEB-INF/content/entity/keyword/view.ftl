<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    
    
    
<title>${keyword.label}</title>
<div class="glide">
    <h1>${keyword.label}</h1>
    <#if keyword.synonyms?has_content>
    <p><#list keyword.synonyms![] as synonym> ${synonym.label} </#list></p>
    </#if>
    <p>${keyword.definition}</p>
</div>

    <#if ( results?? && results?size > 0) >
    <div id="divResultsSortControl">
        <div class="row">
            <div class="span4">
                <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Record"  />
            </div>
        </div>
    </div>
    <@list.listResources resourcelist=results  listTag="span" itemTag="span" titleTag="h3" orientation=orientation mapPosition="top" mapHeight="450"/>
    
</#if>
</#escape>
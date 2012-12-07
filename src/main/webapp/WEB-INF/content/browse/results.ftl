<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<@search.initResultPagination/>
 <@search.headerLinks includeRss=false />


<@nav.creatorToolbar "view" />

<title><#if creator?? && creator.properName??>${creator.properName}<#else>No title</#if></title>
<h1><#if creator?? && creator.properName??>${creator.properName}</#if></h1>
<#if creator??>
    <#if creator.institution??>
    <a href="<@s.url value="${creator.institution.id?c}"/>">${creator.institution}</a>
    </#if>
    <p>${creator.description!''}</p>
    <br/>
        <#if creator.creatorType == 'PERSON'>
           <#if authenticated && (editor ||  id == authenticatedUser.id ) >
                <table class='tableFormat'>
                <tr>
                    <td>
                            <B>Registered Public Archaeologist</B>:${creator.rpaNumber!"no"}
                    </td>
                    <td>
                        <#assign registered = false />
                        <#if creator.registered?has_content>
                            <#assign registered = creator.registered>
                        </#if>
                        <#if registered && (editor || id == authenticatedUser.id)>
                            <#if creator.lastLogin?has_content>
                                <@view.datefield "Last Login"  creator.lastLogin />
                            <#else>
                                <@view.textfield "Last Login"  "No record" />
                            </#if>                    
                        <#else>
                            <@view.boolean "Registered User" registered />
                        </#if>
                    </td>
                </tr>
                <tr>
                    <#if creator.emailPublic || (editor || id == authenticatedUser.id) >
                        <td>
                            <@view.textfield "Email" creator.email />
                        </td>
                    <#else>
                        <td>
                            <@view.textfield "Email" "Not Shown" />
                        </td>
                    </#if>
                    <#if creator.phonePublic || (editor || id == authenticatedUser.id)>
                        <td>
                            <@view.textfield "Phone" creator.phone true />
                        </td>
                    <#else>
                        <td>
                            <@view.textfield "Phone" "Not Shown" />
                        </td>
                    </#if>
                </tr>
                <tr>
                    <td colspan=2>
                    <#escape x as x?html>
                    <@view.textfield "Contributor Reason" creator.contributorReason true />
                    </#escape>
                </td>
                </tr>
                </table>

                <@common.resourceUsageInfo />

<#if (editor || id == authenticatedUser.id) >
<#list creator.addresses  as address>
    <div class="controls-row">
        <@common.printAddress  address=address creatorType=creator.creatorType?lower_case creatorId=creator.id />
    </div>
</#list>

</#if>
            </#if>
<br/>        
        </#if>
</#if>

<@search.basicPagination label="Records" />

<div class="glide">
<#if results??>
<@list.listResources resourcelist=results sortfield="RESOURCE_TYPE" titleTag="h5" />
</#if>
</div>
</#escape>
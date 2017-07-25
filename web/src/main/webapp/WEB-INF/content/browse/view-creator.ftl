<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>

<head>
<#-- @search.initResultPagination/ -->
    <@search.headerLinks includeRss=false />

    <link rel="alternate" href="/api/lod/creator/${id?c}" type="application/ld+json" />    

	<#if creator.creatorType.person >
    	<#assign rssUrl = "/api/search/rss?groups[0].creatorOwner.person.id=${creator.id?c}&groups[0].creatorOwner.person.lastName=${creator.lastName}&groups[0].creatorOwner.person.firstName=${creator.firstName}">
	<#else>
    	<#assign rssUrl = "/api/search/rss?groups[0].creatorOwner.institution.id=${creator.id?c}&groups[0].creatorOwner.institution.name=${creator.name}">
	</#if>
    <@search.rssUrlTag url=rssUrl />

    <title><#if creator?? && creator.properName??>${creator.properName}<#else>No title</#if></title>

</head>


    <@nav.creatorToolbar "view" />

    <@view.pageStatusCallout />

    <#if creator?? >
    
        <#if  creatorFacetMap?has_content || keywordFacetMap?has_content >
        <div id="sidebar-right" parse="true">
            <div class="sidebar-spacer">
                <#list creatorFacetMap?values >
                    <div id="related-creators">
                        <h3>Related Creators</h3>
                        <ul>
                            <#items as collab>
                                <li>
                                   <#if authenticatedUser?has_content>
                                    <a href="<@s.url value="${collab.detailUrl}"/>">${collab.label}</a>
                                    <#else>
                                        ${collab.label}
                                    </#if>
                                </li>
                            </#items>
                        </ul>
                    </div>
                </#list>

                <#list keywordFacetMap?values >
                    <div id="related-keywords">
                        <h3>Related Keywords</h3>
                        <ul>
                            <#items as collab>
                                <li>
                                   <#if authenticatedUser?has_content>
                                    <a href="<@s.url value="${collab.detailUrl}"/>">${collab.label}</a>
                                    <#else>
                                        ${collab.label}
                                    </#if>
                                </li>
                            </#items>
                        </ul>
                    </div>
                </#list>

                <div>
                    <small>Related Keywords and Creators are determined by looking at all of the Creators and Keywords
                        associated with a Creator and highlighting the most commonly used.
                    </small>
                </div>
            </div>
        </div>
        </#if>

    <h1>
    <#if logoUrl?has_content>
		<img class="pull-right"  src="${logoUrl}" alt="logo" title="logo" />
    </#if>
        <#if creator.properName??>${creator.properName}</#if></h1>

        <#assign scope="http://schema.org/Person"/>
        <#if creator.creatorType.institution >
            <#assign scope="http://schema.org/Organization"/>
        </#if>

    <div>
        <#if creator.url?has_content>
            <a href="${creator.url?html}" onclick="TDAR.common.outboundLink(this);" rel="nofollow">${creator.url?html}</a>
        </#if>

        <#if creator.institution??>

            <a href="<@s.url value="${creator.institution.detailUrl}"/>">${creator.institution}</a>
        </#if>

        <@common.description creator.description />

        <#if creator.synonyms?has_content>
            <p class="small"><b>Alternate Names:</b> 
			<#list creator.synonyms as syn> <#if syn_index !=0>,</#if>
			<#if syn.browsePageVisible || editor >
			  <a href="${syn.detailUrl}">${syn.properName}</a>
			<#else>
			  ${syn.properName}
			</#if>
			</#list>
            </p>
        </#if>
        <br/>
        <table class='tableFormat table'>

        <#if creator.creatorType.person>
            <#if creator.url?has_content || creator.orcidId?has_content>
                    <tr>
                        <td><b>URL:</b> <#if creator.url?has_content><a rel="nofollow" href="${creator.url}">${creator.url}</a></#if></td>
                        <td><b>ORCID Identifier:</b> <#if creator.orcidId?has_content><a href="http://orcid.org/${creator.orcidId}">${creator.orcidId}</a></#if>
                        </td>
                    </tr>
            </#if>
            <#if showBasicInfo >
                    <#assign registered = false />
                    <#if (creator.registered)?has_content>
                        <#assign registered = creator.registered>
                    </#if>
                    <#if showAdminInfo>
                        <tr>
                            <td><b>Registered</b>: ${registered?string}</td>
                            <td><b>Username</b>: ${creator.username!"N/A"}</td>
                        </tr>
                    </#if>
                    <tr>
                        <td <#if !showAdminInfo>colspan=2</#if>>
                            <B>Registered Professional Archaeologist</B>:${creator.rpaNumber!"no"}
                        </td>
                        <td>
                            <#if showAdminInfo>
                            <#if (creator.lastLogin)?has_content>
                                <@_datefield "Last Login"  creator.lastLogin /><#if editor> (total: ${creator.totalLogins!0}; Downloads: ${creator.totalDownloads!0})</#if>
                            <#else>
                                <@_textfield "Last Login"  "No record" />
                            </#if>
                        <#else>
                                <@_boolean "Registered User" registered />
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <#if creator.emailPublic || (editor || id == authenticatedUser.id) >
                            <td> <@_textfield "Email" creator.email /></td>
                        <#else>
                            <td><@_textfield "Email" "Not Shown" /></#if>
                        <#if creator.phonePublic || (editor || id == authenticatedUser.id)>
                            <td><@_textfield "Phone" creator.phone true /></td>
                        <#else>
                            <td><@_textfield "Phone" "Not Shown" /></td>
                        </#if>
                    </tr>
                    <tr>
                        <td colspan=2>
                            <#escape x as x?html>
                    <@_textfield "Contributor Reason" creator.contributorReason true />
                    </#escape>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <#escape x as x?html>
                    <@_textfield "TOS Version" creator.tosVersion true />
                    </#escape>
                        </td>
                        <td>
                            <#escape x as x?html>
                                <@_textfield "Agreement Version" creator.contributorAgreementVersion true />
                                <br/>
                                <#assign term><@s.text name="${(creator.affiliation.localeKey)!''}" /></#assign>
                                <@_textfield "Affiliation" term />

                            </#escape>
                        </td>
                    </tr>

            </#if>
        </#if>
        <#if editor>
			<tr>
				<td><b>Total Occurrance Count:</b> ${creator.occurrence!0}</td>
				<td><b>Total Browse Occurrance Count:</b> ${creator.browseOccurrence!0}</td>
			</tr>
			<tr>
				<td><b>Has User Hidden Page?:</b> ${creator.hidden?string}</td>
				<#if creator.creatorType.institution && creator.email?has_content >
				<td><b>Email:</b> ${creator.email}</td>
				
				</#if>
			</tr>
			<#if registered?has_content && registered>
			<tr>
				<td><b>Edit History:</b> <a href="/entity/user/activity/${creator.id?c}">View Edit History</a></td>
				<td></td>
				</tr>
			</#if>
</#if>
</table>

            <#-- Institution addresses can be shown to anybody (but can only be edited by tdar-editor and above) -->
            <#if  (editorOrSelf || creator.creatorType.institution) && creator.addresses?has_content >
                <h3>Addresses</h3>

                <div class="row">
                    <#list creator.addresses  as address>
                        <div class="span3">
                            <@commonr.printAddress  address=address creatorType=creator.creatorType?lower_case creatorId=creator.id />
                        </div>
                    </#list>
                </div>
             </#if>
       </#if>
    </div>

<#if creator.creatorType.person>
                <#if editorOrSelf && (creator.proxyInstitution?has_content || creator.proxyNote?has_content)>
                    <h3>Future Contact Information</h3>
                    <p>The institution or person that should be contacted in the future about draft, or confidential materials you have uploaded if you are un-reachable <i>(edit profile to add or modify)</i><p>
                    <#if creator.proxyInstitution?has_content>
                        <a href="<@s.url value="/browse/creators/${creator.proxyInstitution.id?c}"/>">${creator.proxyInstitution}</a>
                    <#else>
                        None Specified
                    </#if>
                    <p>${creator.proxyNote!""}</p>
                </#if>

                <@common.resourceUsageInfo />
                <#if editorOrSelf>

                    <#if creator.registered?? >
                        <div class="row">
                            <div class="span6">
                                <@common.billingAccountList accounts />
                            </div>
                            <div class="span6">
                            <#if editor>
                                <h2>Group Membership</h2>
                                <ul>
                                    <#list groups as group>
                                        <li>${group}</li>
                                    </#list>
                                </ul>
                            </#if>
                            </div>
                        </div>

                        <#if editor>
                            <#list ownerCollections![]>
                        <div class="row">
                            <div class="span12">
                                <h2>Collection Membership</h2>
                                <ul>
					        		<#items as collection>
                                        <li><a href="${collection.detailUrl}">${collection.name}</a></li>
									</#items>
                                </ul>
                            </div>
                        </div>
                            </#list>
                        </#if>


                    </#if>
                </#if>
                
                <#else>
	                <h2>Institution Members</h2>
	                <@common.listUsers users=people span=8 baseUrl="/browse/creators" well=false />
				</#if>

    <#if ( results?? && results?size > 0) >
    <div id="divResultsSortControl">  
        <div class="row">
            <div class="span4">
                <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Record"/>
            </div>
            <div class="span5">
                <#if !hideFacetsAndSort>
                    <div class="form-horizontal pull-right">
                        <@search.sortFields true/>
                    </div>
                </#if>
            </div>
        </div>
    </div>

        <#if editor && creatorXml?has_content>
        ${creatorXml?html}
        </#if>

    <div class="tdarresults">
        <@list.listResources resourcelist=results sortfield="RESOURCE_TYPE" titleTag="h5" orientation="LIST"/>
    </div>
        <@search.basicPagination "Results"/>
    <#else>
        <#if (creator.properName)?has_content>
            No Resources associated with ${creator.properName}
        </#if>
    </#if>
    <#if editor>
    <#--
    <p><b>This Creator Page was Viewed:</b>${viewCount} times</p>
    
    -->
    </#if>

    <#macro _datefield _label _val="" _alwaysShow=true>
        <#if _alwaysShow || _val?is_date>
        <b>${_label}</b>
            <#if _val?is_date>
                <@view.shortDate _val true/>
            </#if>
        </#if>
    </#macro>

    <#macro _textfield _label _val="" _alwaysShow=true>
        <#if _alwaysShow || _val?has_content >
        <b>${_label}:</b> ${_val}
        </#if>
    </#macro>

<#-- FIXME: jim: this is the worst thing you've ever written.  -->
    <#macro _boolean _label _val _show=true trueString="Yes" falseString="No">
        <#if _show>
        <b>${_label}:</b>
            <#if _val>${trueString}<#else>${falseString}</#if>
        </#if>
    </#macro>
</#escape>
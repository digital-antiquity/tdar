<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/content/${namespace}/view.ftl" as local_ />
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<@view.htmlHeader resourceType=resource.resourceType.label?lower_case>
<meta name="lastModifiedDate" content="$Date$"/>

<#if includeRssAndSearchLinks??>
	<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
	<#assign rssUrl>/search/rss?groups[0].fieldTypes[0]=PROJECT&groups[0].projects[0].id=${project.id?c}&groups[0].projects[0].name=${(project.name!"untitled")?url}</#assign>
	<@search.rssUrlTag url=rssUrl />
	<@search.headerLinks includeRss=false />
</#if>

<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view">
	<#if local_.toolbarAdditions?? && local_.toolbarAdditions?is_macro>
		<@local_.toolbarAdditions />
	</#if>
</@view.toolbar>


<div id="datatable-child" style="display:none">
    <p class="">
        You have successfully updated the page that opened this window.  What would you like to do now?
    </p>
</div>

<@view.projectAssociation resourceType=resource.resourceType.label?lower_case />

<@view.infoResourceBasicInformation />

<#if local_.afterBasicInfo?? && local_.afterBasicInfo?is_macro>
	<@local_.afterBasicInfo />
</#if>


<@view.sharedViewComponents resource />

<#if local_.footer?? && local_.footer?is_macro>
	<@local_.footer />
</#if>


<script type="text/javascript">
$(function() {
    'use strict';
    TDAR.common.initializeView();
	<#if local_.localJavascript?? && local_.localJavascript?is_macro>
		<@local_.localJavascript />
	</#if>

});
</script>

</#escape>
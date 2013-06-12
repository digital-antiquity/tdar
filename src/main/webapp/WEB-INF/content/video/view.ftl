<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
	<#macro afterBasicInfo>
		<@view.showcase />
		<br/>
		<hr/>
	</#macro>
	
	<#macro localJavascript>
	    <@view.datatableChildJavascript />
	</#macro>
</#escape>
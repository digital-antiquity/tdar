<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/common-rights.ftl" as rights>
    <#import "/${config.themeDir}/settings.ftl" as settings>

<head>

    <title>Rights for ${persistable.name}</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Permissions: <span class="red">${persistable.name}</span></h1>
</div>
    <@s.form class="form-horizontal tdarvalidate rightsform" action="rights-save" method="POST" dynamicAttributes={"data-validate-method":"initRightsForm"}>
<div class="row">
    <div class="span12">
    <p><b>This collection is currenrly shared with <#if (proxies?size == 1)>person<#else>people</#if>.</b></p>
    <@s.hidden name="id" />
    
    <#if editor>
    <#--  
	    <div class="control-group" id="divSubmitter">
	        <label class="control-label">Owner</label>
	            <div class="controls controls-row">
	                <#if owner?has_content>
	                <@edit.registeredUserRow person=owner isDisabled=disabled   _personPrefix="" _indexNumber=''
	                    prefix="owner" includeRights=false includeRepeatRow=false />
	 	        <#else>
	                <@edit.registeredUserRow person=authenticatedUser isDisabled=disabled   _personPrefix="" _indexNumber=''
	                    prefix="owner" includeRights=false includeRepeatRow=false />
                </#if>
            </div>
        </div>
     -->
    </#if>

    
	<@rights.addUsersForRights />

	<@rights.invite />

</div>
</div>
<@edit.submit fileReminder=false />
                </@s.form>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>


<@edit.personAutocompleteTemplate />

<@rights.javascript />

</#escape>

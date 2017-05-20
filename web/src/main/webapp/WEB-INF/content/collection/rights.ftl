<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/common-rights.ftl" as rights>
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Sharing: <span class="red">${persistable.name}</span></h1>
</div>
    <@s.form class="form-horizontal tdarvalidate" action="save" method="POST">
<div class="row">
    <div class="span12">
    <p><b>This collection is shared with ${proxies?size} people.</b></p>
    <@s.hidden name="id" />
    
    <#if editor>
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

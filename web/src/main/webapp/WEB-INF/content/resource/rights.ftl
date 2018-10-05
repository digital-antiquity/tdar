<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/common-rights.ftl" as rights>
<#import "/WEB-INF/settings.ftl" as settings>

<head>
    <title>Rights for ${persistable.title}</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Permissions: <span class="red">${persistable.title}</span></h1>
</div>

<@s.form class="form-horizontal tdarvalidate rightsform" action="save" method="POST" dynamicAttributes={"data-validate-method":"initRightsForm"}>
    <div class="row">
        <div class="col-12">
            <p>
                <i>This ${persistable.resourceType.label?lower_case} is currently shared with ${proxies?size} <#if (proxies?size == 1)>person<#else>people</#if>
                <#if (persistable.managedResourceCollections?size > 0)>, and is in
                    ${persistable.managedResourceCollections?size}
                    <#if (persistable.managedResourceCollections?size == 1 )> 
                     collection
                     <#else>
                        collections                     
                     </#if>
                </#if>.</i><br>
            </p>
        
            <@s.hidden name="id" />
            <@edit.resourceCollectionSection prefix="shares" label="Collections" list=shares />
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


<@rights.javascript />

</#escape>
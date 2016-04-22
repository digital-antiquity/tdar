<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#assign deleteable = true>
<#if persistable??>

    <#if !persistable.status?? || persistable.status != 'DELETED'>


        <#if deleteIssue?has_content>
            <#assign deleteable= false />

        </#if>
        <#if persistable.resourceType??>
        <br/>
        <div class="alert alert-info">
            Note: Did you know you don't need to delete a record if you just want to update a file?
        </div>

            <#assign whatamideleting = persistable.resourceType.label?lower_case />
        <#elseif persistable.urlNamespace="collection">
            <#assign whatamideleting = "collection" />
        <#else>
            <#assign whatamideleting = "item" />
        </#if>
    <h2>Confirm deletion of ${whatamideleting}: ${persistable.name?html}</h2>

        <#if !deleteable>
        <h4>This ${whatamideleting} cannot be deleted because it is still referenced by the following: </h4>
            <#if deleteIssue?has_content>
				<p><b>${deleteIssue.issue}</b></p>
		        <ul>
                <#list deleteIssue.relatedItems as rsc>
                        <li>${rsc.id?c} - ${rsc.name?html} </li>
                </#list>
				</ul>
            </#if>
        <#else>
            <@s.form name='deleteForm' id='deleteForm'  method='post' action='delete'>
                <@s.token name='struts.csrf.token' />
                <h4>Please explain why you are deleting this record</h4>
                <textarea name="deletionReason" cols='60' rows='3' class="input-xxlarge" maxlength='255'></textarea>

                <h4>Are you sure you want to delete this <#if persistable.resourceType??>${persistable.resourceType.label?lower_case}</#if>?</h4>

                <@s.submit type="submit" name="delete" value="delete" cssClass="btn button btn-warning"/>
                <@s.hidden name="id" />
            </@s.form>
        </#if>
    <#else>
        <h2>This resource has already been
            deleted <#if persistable.resourceType??>${persistable.resourceType.label?lower_case}</#if> ${persistable.name?html}</h2>

    </#if>

<#else>
    <h2>This resource has already been deleted</h2>

</#if>

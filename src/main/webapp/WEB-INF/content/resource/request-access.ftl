<#escape _untrusted as _untrusted?html>

    <@s.form name='metadataForm' id='metadataForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='process-access-request'>
        <@s.token name='struts.csrf.token' />
        <h3>Grant Access to a user</h3>
        <p>Please specify the level of rights you'd like to grant <b>${requestor.properName}</b> access to <a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></p>
        <@s.hidden name="resourceId" value="${resource.id?c}"/>
        <@s.hidden name="requestorId" value="${requestor.id?c}"/>
        
        <@s.select theme="tdar" name="permission" emptyOption='false'
        listValue='label' list='%{availablePermissions}' disabled=isDisabled />
    <@s.submit />
</@s.form>
</#escape>
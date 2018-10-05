<#escape _untrusted as _untrusted?html>

    <@s.form name='metadataForm' id='metadataForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='process-access-request'>
        <@s.token name='struts.csrf.token' />
        <h1>Respond to user:<span class="red"> ${(type.label)!'Access Request'}</span></h1>
        
        <p>Please specify the level of rights you'd like to grant <b><a href="${requestor.detailUrl}">${requestor.properName}</a></b> access to <a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></p>
        <@s.hidden name="resourceId" value="${resource.id?c}"/>
        <@s.hidden name="requestorId" value="${requestor.id?c}"/>
        <@s.hidden name="type" value="${type!''}"/>

        
    <div class="control-group">
        <label class="control-label" for="permission">Process Request</label>
        <div class="controls">
    <label class="radio">
      <input type="radio" name="reject" id="optionsRadios1" value="false" checked>Accept</label>
    </div>        
        <div class="controls">
    <label class="radio">
      <input type="radio" name="reject" id="optionsRadios2" value="true">Reject</label>        
    </div>        
    </div>        
      <div class="control-group">
        <label class="control-label" for="permission">Grant Permission</label>
        <div class="controls">
            <@s.select theme="tdar" name="permission" id="permission" emptyOption='false' listValue='label' list='%{availablePermissions}' />
        </div>
      </div>

        <br/>
        <@s.textarea name="comment" id="messageBody" rows="4" label="Message" cssClass="col-5" cols="80" />
        
    <@s.submit name="submit" cssClass="button btn btn-primary"/>
</@s.form>
</#escape>
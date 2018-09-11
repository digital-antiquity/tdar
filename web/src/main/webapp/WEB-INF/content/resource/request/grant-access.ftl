<#escape _untrusted as _untrusted?html>

    <@s.form name='metadataForm' id='metadataForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='process-access-request'>
        <@s.token name='struts.csrf.token' />
        <#assign label = "" />
        <#if type?has_content>
            <#assign label=type.label />
            <#if type == 'CUSTOM_CONTACT'>
                <#assign label = custom.name />
            </#if>
        </#if>

        <h1>Respond to user:<span class="red"> ${(label)!'Access Request'}</span></h1>
        
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
      <div class="control-group">
        <label class="control-label" for="dp3">Grant Access Until</label>
    <div class="controls">
        <div class="input-append">
          <input class="span2 datepicker" size="16" type="text" name="expiresString" value="" id="dp3" data-date-format="mm/dd/yyyy" >
          <span class="add-on"><i class="icon-th"></i></span>
        </div>
    </div>
    </div>

        <br/>
        <@s.textarea name="comment" id="messageBody" rows="4" label="Message" cssClass="span5" cols="80" />
        
    <@s.submit name="submit" cssClass="button btn btn-primary"/>
    <script>
	    $(function () {
    	    'use strict';
    	     $('input[type=radio][name=reject]').change(function() {
	        if (this.value == 'true') {
	        	$("#permission").attr("disabled","true");
	        }
	        else if (this.value == 'false') {
	        	$("#permission").removeAttr("disabled");
	        }
    });
		});
    </script>
</@s.form>
</#escape>
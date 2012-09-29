<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
    <#if (person.id > 0 )>
        <title>Editing ${person.name}</title>
    <#else>
        <title>Add a new user</title>
    </#if>
    
    
    <script type="text/javascript">
        var $frmPerson;
        $(function() {
            $frmPerson = $('#frmPerson');
            applyInstitutionAutocomplete($('.institutionAutocomplete'), true);
            initializeView();
            setupEditForm('#frmPerson');
            
            //tack on the confirm-password rules
        });
    </script>
<style type="text/css">
label.error {display:block;}
label {width:14em;}
input[type=text],input[type=password],input[type=checkbox],input[type=radio], select {margin-left:16em !important}
textarea {margin-left:150pt}
input[type=radio]+ label + input[type=radio] {margin-left:4em}
label.radio {width:4em !important}
.field {margin-left:16em !important}
textarea {width:32em}

</style>
</head>
<body>
<@nav.creatorToolbar "edit" />

<@s.form name='personForm' id='frmPerson'  method='post' enctype='multipart/form-data' action='save'>

<div class="glide">
    <h3>Personal Details</h3>
    <div class="width30percent marginLeft10" >
        <#escape x as x?html><@s.hidden name="id" /></#escape>
        <@s.textfield cssClass="required"       labelPosition='left' label="Last Name"   name="person.lastName"  maxlength="255" title="A last name is required" /> 
        <br /><@s.textfield cssClass="required"       labelPosition='left'  label="First Name"  name="person.firstName" maxlength="255"  title="A first name is required" />
        <br /><@s.textfield cssClass="institutionAutocomplete"  labelPosition='left' label="Institution"       name="institutionName"     maxlength="255" value="${person.institution!}"/>
        
        <#if privacyControlsEnabled>
        	<br /><@edit.boolfield label='Make email public?' name="person.emailPublic" id="email-public" value=person.emailPublic!false labelPosition='top' />
			<p class="field"><em><b>NOTE:</b> Making your email address public will display it to anyone who visits tDAR, this includes search engines, spammers, and visitors who are not logged in.</em></p>
		</#if>
		    
        <br /><@s.textfield labelPosition="left" label="RPA Number" name="person.rpaNumber" />
        <br /><@s.textfield labelPosition="left" label="Phone" cssClass="phoneUS" name="person.phone" />
        
        <#if privacyControlsEnabled>
        	<br /><@edit.boolfield label='Make phone public?' name="person.phonePublic" id="phone-public" value=person.phonePublic!false labelPosition="top"/>
			<p class="field"><em><b>NOTE:</b> Making your phone # public will display it to anyone who visits tDAR, this includes search engines, and visitors who are not logged in.</em></p>
		</#if>    
		
        <br /><@edit.boolfield label='tDAR Contributor?' name="person.contributor" id="contributor-id" value=person.contributor!false labelPosition="top" />
        <br />
<br/>
        <div>
        <label for='contributorReasonId' style="line-height: 1.2em;">
        Please briefly describe the geographical areas, time periods, or other
        subjects for which you would like to contribute information:
        </label>
        <@s.textarea rows=6 cols='50' name='person.contributorReason' id='contributorReasonId' />
        </div>            
<br/>
        <div>
        <label for='description-id' style="line-height: 1.2em;">
        Please provide a brief description of yourself:
        </label>
        <@s.textarea rows=6 cols='50' name='person.description' id='description-id' />
        </div>            
    </div>
</div>

<#if editingSelf>
<div class="glide" id="divChangePassword">
    <h3>Change Your Password</h3>
    <@s.password name="password" id="txtPassword" label="New password" labelPosition="left" autocomplete="off" />
    <br /><@s.password name="confirmPassword" id="txtConfirmPassword" label="Confirm password" labelPosition="left" autocomplete="off"  />
</div>
<#else>
<div class="glide" id="divResetPassword">
    <h3>Reset User Password</h3>
    <div class="width30percent marginLeft10">
        <br /><@s.checkbox labelposition='left' label='Reset Password?' name="passwordResetRequested" id="contributor-id" cssStyle='margin-left: 16em;' />
    </div>
</div>
</#if>

<@edit.submit "Save" false />    

</@s.form>
<div id="error"></div>
</body>

</#escape>
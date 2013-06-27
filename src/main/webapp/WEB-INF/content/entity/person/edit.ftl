<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
    <#assign pageTitle = "Add a new User">
    
    <#if (editingSelf)>
        <#assign pageTitle = "Your Profile: ${person.properName!'n/a'}">
        
    <#elseif person.id != -1>
	<#assign pageTitle = "Editing: ${person.properName!'n/a'}" >   
    </#if>
    <title>${pageTitle}</title>
    
<style type="text/css">
label.error {display:block;}
</style>
</head>
<body>
<h1>${pageTitle}</h1>

<@s.form name='personForm' id='frmPerson'  cssClass="form-horizontal"  method='post' enctype='multipart/form-data' action='save'>

<div class="row">
    <h2>Personal Details</h2>
    <div class="" >
		<#if editor>    
        <div id="spanStatus" data-tooltipcontent="#spanStatusToolTip" class="control-group">
            <label class="control-label">Status</label>
            <div class="controls">
                <@s.select theme="tdar" cssClass="input-xlarge" value="person.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
                <span class="label label-important" >admin option</span>
            </div>  
        </div>
		</#if>    
        
        <#if person.username?has_content>
        <div class="control-group">
            <label class="control-label">Username</label>
            <div class="controls">
		        <span class="uneditable-input input-xlarge"> ${person.username}</span>
            </div>
        </div>
        </#if>
        
        <@s.hidden name="id" />
        <@s.textfield cssClass="required input-xlarge"        label="Last Name"   name="person.lastName"  maxlength="255"  title="A last name is required" /> 

        <@s.textfield cssClass="required input-xlarge"         label="First Name"  name="person.firstName" maxlength="255"  title="A first name is required" />
        <@s.textfield cssClass="institutionAutocomplete input-xlarge"  label="Institution"       name="institutionName"     maxlength="255" value="${person.institution!}"/>
		<#assign registered = "" />
        <@s.textfield cssClass="input-xlarge ${(person.registered)?string('registered', '')}"  label="Email"   name="person.email"  maxlength="255"  title="An email is required" /> 
        
        <#if privacyControlsEnabled>
            <@common.boolfield label='Make email public?' name="person.emailPublic" id="email-public" value=person.emailPublic!false  />
            <p class="field"><em><b>NOTE:</b> Making your email address public will display it to anyone who visits ${siteAcronym}, this includes search engines, spammers, and visitors who are not logged in.</em></p>
        </#if>
            
        <#if RPAEnabled><@s.textfield  cssClass="input-xlarge" label="RPA Number" name="person.rpaNumber"  maxlength=255 /></#if>

        <@s.textfield name="person.url" label="Website" id="txtUrl" cssClass="input-xlarge url"  maxlength=255 />

        <@s.textfield  label="Phone" cssClass="phoneUS input-xlarge" name="person.phone"  maxlength=255 />
        
        <#if privacyControlsEnabled>
            <@common.boolfield label='Make phone public?' name="person.phonePublic" id="phone-public" value=person.phonePublic!false />
            <p class="field"><em><b>NOTE:</b> Making your phone # public will display it to anyone who visits ${siteAcronym}, this includes search engines, and visitors who are not logged in.</em></p>
        </#if>    
        
        <@common.boolfield label='${siteAcronym} Contributor?' name="person.contributor" id="contributor-id" value=(person.contributor!false)  />


        
        <@s.textarea label="Please briefly describe the geographical areas, time periods, or other subjects for which you would like to contribute information" 
            rows=6 cols='50' cssClass="input-xxlarge" name='person.contributorReason' id='contributorReasonId'  maxlength=512 />
        <@s.textarea label="Please provide a brief description of yourself" rows=6 cols='50' name='person.description' cssClass="input-xxlarge" id='description-id' />


        <@s.textfield cssClass="institutionAutocomplete input-xlarge"  label="Proxy Institution"       name="proxyInstitutionName"     maxlength="255" value="${person.proxyInstitution!}"/>

        <@s.textarea label="Proxy Note" rows=6 cols='50' name='person.proxyNote' cssClass="input-xxlarge"  />
    </div>
</div>

<h3>Address List</h3>
<@common.listAddresses person />

<@common.billingAccountList accounts />

<#if editingSelf>
<div class="glide" id="divChangePassword">
    <h2>Change Your Password</h2>
    <@s.password name="password" id="txtPassword" label="New password"  autocomplete="off" />
    <@s.password name="confirmPassword" id="txtConfirmPassword" label="Confirm password"  autocomplete="off"  />
</div>
<#else>
<div class="glide" id="divResetPassword">
    <h3>Reset User Password</h3>
        <@s.checkbox  label='Reset Password?' name="passwordResetRequested" id="contributor-id"  />
</div>
</#if>

<@edit.submit "Save" false />    

</@s.form>
<div id="error"></div>
    <script type="text/javascript">
        var $frmPerson;
        $(function() {
            $frmPerson = $('#frmPerson');
            TDAR.autocomplete.applyInstitutionAutocomplete($('.institutionAutocomplete'), true);
            initializeView();
            TDAR.common.initEditPage($('#frmPerson')[0]);
            //tack on the confirm-password rules
        });
    </script>

</body>

</#escape>
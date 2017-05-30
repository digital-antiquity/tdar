<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/content/entity/entity-edit-common.ftl" as entityEdit>
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
        label.error {
            display: block;
        }
    </style>
</head>
<body>
<h1>${pageTitle}</h1>
    <@s.form name='personForm' id='frmPerson'  cssClass="form-vertical tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"}  method='post' enctype='multipart/form-data' action='save'>
    <@common.chromeAutofillWorkaround />
    <@s.token name='struts.csrf.token' />
    <@common.jsErrorLog />
        
    <div id='subnavbar' class="subnavbar-scrollspy affix-top subnavbar resource-nav navbar-static  screen" data-offset-top="250" data-spy="affix">
        <div class="">
            <div class="container">
                <ul class="nav">
                    <li class="alwaysHidden"><a href="#top">top</a></li>
                    <li class="active"><a href="#profile">Basic</a></li>
                    <#if contributor><li><a href="#archive">Archival</a></li></#if>
                    <li><a href="#contact">Contact</a></li>
                    <li><a href="#billingSection">Billing</a></li>
                    <li><a href="#password">Change Password</a></li>
                </ul>
                <div id="fakeSubmitDiv" class="pull-right">
                    <button type=button class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</button>
                    <img alt="progress indicator" title="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
                </div>
            </div>
        </div>
    </div>

    <div class="">
            <@entityEdit.basicInformation emailEditable=(actionName != 'myprofile')>
                <#if person.username?has_content>
                    <div class="control-group">
                        <label class="control-label">Username</label>
    
                        <div class="controls">
                            <span class="uneditable-input input-xlarge"> ${person.username}</span>
                        </div>
                    </div>
                </#if>
            </@entityEdit.basicInformation>
    <@entityEdit.hidden />


        <@entityEdit.uploadForm />
            <h3>Contributor</h3>
            <@s.checkbox label="${siteAcronym} Contributor?" name="contributor" id="contributor-id" />
            <p><i>Note: after selecting this, you will be prompted to review and agree to our contributor's policy</i></p>

            <@s.textarea label="Please briefly describe the geographical areas, time periods, or other subjects for which you would like to contribute information"
            rows=6 cols='50' cssClass="input-xxlarge" name='contributorReason' id='contributorReasonId'  maxlength=512 />

    </div>
    <#if contributor>
    <div class="">
        <h2 id="archive">Archival Information</h2>

        <p>Who should we contact if there's a question or problem in the future with records you've submitted? Please provide the name of an institution we
            can contact if we cannot contact you about a question or issue with a record you uploaded.
            Examples might include:
        <ul>
            <li>A request for access to a confidential record</li>
            <li>Clarification of a copyright or ownership question</li>
            <li>Other issues</li>
        </ul>
        </p>
        <@s.textfield cssClass="institutionAutocomplete input-xlarge"  label="Proxy Institution"       name="proxyInstitutionName"     maxlength="255" value="${person.proxyInstitution!}"/>

        <p>If there are specific instructions, such as a person or position within the organization to contact, please provide additional information
            here</p>
        <@s.textarea label="Proxy Note" rows=6 cols='50' name='proxyNote' cssClass="input-xxlarge"  />
    </div>
    </#if>

    <@entityEdit.contactInfo />
    <div class="">
            <@common.billingAccountList accounts />
    </div>


    <div class="">
    <h2>Preferences</h2>
            <@s.checkbox label='New Resources Default to draft?' name="persistable.newResourceSavedAsDraft" id="new-as-draft"  />

    </div>

<div class="">
        <h2 id="password">Password</h2>

        <#if editingSelf && person.registered >
        <div class="glide" id="divChangePassword">
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
    </div>
    <div class="callout">
        <p>
            <#assign commentEmail = commentUrl?replace("mailto:", "")>
            <em><strong>Account cancellation:</strong>
                If you would like to cancel your ${siteAcronym} account please send an email request to <@s.a href="${commentUrl}">${commentEmail}</@s.a></em>
        </p>
    </div>


    </@s.form>
<div id="error"></div>
<script type="text/javascript">
    var $frmPerson;
    $(function () {
        $frmPerson = $('#frmPerson');
        TDAR.autocomplete.applyInstitutionAutocomplete($('.institutionAutocomplete'), true);
        TDAR.common.initEditPage($('#frmPerson')[0]);
        //tack on the confirm-password rules
    });
</script>

</body>

</#escape>

<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/common.ftl" as common>

    <#macro uploadForm>
        <#if editor>
            <div class="control-group row">
                <label class="col-form-label col-3">Profile Image</label>
                <div class="controls col-9">
                    <@s.file  name='file' cssClass="input-xxlarge profileImage " id="fileUploadField" labelposition='left' size='40' dynamicAttributes={
                        "data-rule-extension":"jpg,tiff,jpeg,png"
                    }/>
                </div>
            </div>
        </#if>
    </#macro>

    <#macro basicInformation>
            <h2 id="profile">Personal Details</h2>
        
            <#if editor>
                <div id="spanStatus" data-tooltipcontent="#spanStatusToolTip" class="control-group ">
                    <label class="control-label col-form-label">Status</label>

                        <@s.select cssClass="input-xlarge  col-5" value="person.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
                        <span class="label label-important">admin option</span>
                </div>
            </#if>

            <#nested>

            <@s.hidden name="id" />
            <div class="row">
                <div class="col-6">
                        <@s.textfield cssClass="required input-xlarge"         label="First Name"  name="person.firstName" maxlength="255"  title="A first name is required" />
                </div>
                <div class="col-6">
                        <@s.textfield cssClass="required input-xlarge"        label="Last Name"   name="person.lastName"  maxlength="255"  title="A last name is required" />
                </div>
            </div>
            <@s.textfield cssClass="institutionAutocomplete input-xlarge"  label="Institution"       name="institutionName"     maxlength="255" value="${person.institution!}"/>
            <#assign registered = "" />


        <div class="row">
            <div class="col-6">
                <@s.textfield cssClass="input-xlarge ${(person.registered??)?string('registered', '')}"  label="Email"   name="email"  maxlength="255"  title="An email is required" />
    
                <#if config.privacyControlsEnabled>
                    <@s.checkbox label='Make email public?' name="person.emailPublic" id="email-public"  />
                    <p class="field"><em><b>NOTE:</b> Making your email address public will display it to anyone who visits ${siteAcronym}, this includes search
                        engines, spammers, and visitors who are not logged in.</em></p>
                </#if>
            </div>
            <div class="col-6">
                <@s.textfield name="person.url" label="Website" id="txtUrl" cssClass="input-xlarge url"  maxlength=255 />
            </div>
        </div>
            <@s.textarea label="Please provide a brief description of yourself" rows=6 cols='50' name='person.description' cssClass="input-xxlarge" id='description-id' />



            <div class="row">
                <div class="col-6">
                    <@s.textfield name="person.orcidId" label="ORCID Id" id="orcidId" cssClass="input-xlarge"  maxlength=50 placeholder="XXXX-XXXX-XXXX-XXXX" />
                    <a href="http://orcid.org/about/what-is-orcid">About ORCID</a>                
                </div>
                <div class="col-6">
                    <#if config.RPAEnabled>
                    <@s.textfield  cssClass="input-xlarge" label="RPA Number" name="person.rpaNumber"  maxlength=255 />
                    <a href="http://rpanet.org/">About RPA</a>                
                    </#if>
                </div>
            </div>
    </#macro>

    <#macro hidden>
            <br/>
            <@s.checkbox label='Hide page from logged-out users' name="persistable.hidden" id="hidden-page"  />
            <br/>
    </#macro>

    <#macro contactInfo>
        <div class="">
                <h2 id="contact">Contact</h2>
                    <@s.textfield  label="Phone" cssClass="phoneUS input-xlarge" name="person.phone"  maxlength=255 />
        
                    <#if config.privacyControlsEnabled>
                        <@s.checkbox label='Make phone public?' name="person.phonePublic" id="phone-public" />
                        <p class="field"><em><b>NOTE:</b> Making your phone # public will display it to anyone who visits ${siteAcronym}, this includes search engines,
                            and visitors who are not logged in.</em></p>
                    </#if>
        
            <h3>Address List</h3>
                <@commonr.listAddresses person />
        </div>

    </#macro>

    <#macro userEditForm person>
        <@s.form name='personForm' id='frmPerson'  cssClass="form-vertical tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"}  method='post' enctype='multipart/form-data' action='save'>
            <@common.chromeAutofillWorkaround />
            <@s.token name='struts.csrf.token' />
            <@common.jsErrorLog />
	<div class="row">
	<div class="col-10">


        <div class="">
            <@basicInformation>
                <#if person.username?has_content>
                    <div class="control-group">
                        <label class="col-form-label">Username</label>

                        <div class="controls">
                        <input type="text" class="disabled form-control col-5" readonly value="${person.username}" />

                        </div>
                    </div>
                </#if>
            </@basicInformation>
            <@hidden />


            <@uploadForm />

            <#--As of Radiocarbon release, all new users are given contributor status, and the "visitor" concept has been deprecated -->
            <#--provide a contributor opt-in for older non-contributor accounts only. Once they opt-in, we no longer show this option. -->
            <#if !contributor>
            <@s.checkbox label="${siteAcronym} Contributor?" name="contributor" id="contributor-id" />
            <p><i>Note: after selecting this, you will be prompted to review and agree to our contributor's policy</i></p>
            </#if>

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

            <@contactInfo />
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
	<br/>
	<br/>
            <span class="help-block">Review our <a href="${config.privacyPolicyUrl}">privacy policy</a>.</span>
	<br/>

            <@edit.submit "Save" false />
        </div>
        <div class="callout">
            <p>
                <em><strong>Account cancellation:</strong></em>
                <a href="/account/delete" class="button btn btn-danger" id="disable-account">delete my account</a>
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
            $("#clearButton").click(function() {$('#fileUploadField').val('');return false;});
        });
    </script>
	</div>
	<div class='col-2'>

    <nav id='subnavbar'  class="bg-light" >
    <div class=" col-12">
        <p>Jump to Section:</p>
    <ul class="list-unstyled">
                        <li class="active"><a class="nav-link" href="#profile">Basic</a></li>
                        <#if contributor><li><a class="nav-link" href="#archive">Archival</a></li></#if>
                        <li><a class="nav-link" href="#contact">Contact</a></li>
                        <li><a class="nav-link" href="#billingSection">Billing</a></li>
                        <li><a class="nav-link" href="#password">Change Password</a></li>
                    </ul>
                    <div class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</div>
                    <img alt="progress indicator" title="progress indicator"  src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
		</div>
		</div>
    </nav>
	
	</div>
	</div>
    </#macro>


</#escape>
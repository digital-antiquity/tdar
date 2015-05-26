<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>

    <#macro uploadForm>
        <#if editor>
            <div class="control-group">
                <label class="control-label">Add an Photo / Logo</label>
                <div class="controls">
                    <@s.file theme="simple" name='file' cssClass="input-xxlarge" id="fileUploadField" labelposition='left' size='40' />
                </div>
            </div>
        </#if>
    </#macro>

    <#macro basicInformation>
            <h2 id="profile">Personal Details</h2>
        
            <#if editor>
                <div id="spanStatus" data-tooltipcontent="#spanStatusToolTip" class="control-group">
                    <label class="control-label">Status</label>

                    <div class="controls">
                        <@s.select theme="tdar" cssClass="input-xlarge" value="person.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
                        <span class="label label-important">admin option</span>
                    </div>
                </div>
            </#if>

            <#nested>

            <@s.hidden name="id" />
            <div class="row">
                <div class="span5">
                        <@s.textfield cssClass="required input-xlarge"         label="First Name"  name="person.firstName" maxlength="255"  title="A first name is required" />
                </div>
                <div class="span5">
                        <@s.textfield cssClass="required input-xlarge"        label="Last Name"   name="person.lastName"  maxlength="255"  title="A last name is required" />
                </div>
            </div>
            <@s.textfield cssClass="institutionAutocomplete input-xlarge"  label="Institution"       name="institutionName"     maxlength="255" value="${person.institution!}"/>
            <#assign registered = "" />


        <div class="row">
            <div class="span5">
                <@s.textfield cssClass="input-xlarge ${(person.registered??)?string('registered', '')}"  label="Email"   name="email"  maxlength="255"  title="An email is required" />
    
                <#if privacyControlsEnabled>
                    <@s.checkbox label='Make email public?' name="person.emailPublic" id="email-public"  />
                    <p class="field"><em><b>NOTE:</b> Making your email address public will display it to anyone who visits ${siteAcronym}, this includes search
                        engines, spammers, and visitors who are not logged in.</em></p>
                </#if>
            </div>
            <div class="span5">
                <@s.textfield name="person.url" label="Website" id="txtUrl" cssClass="input-xlarge url"  maxlength=255 />
            </div>
        </div>
            <@s.textarea label="Please provide a brief description of yourself" rows=6 cols='50' name='person.description' cssClass="input-xxlarge" id='description-id' />



            <div class="row">
                <div class="span5">
                    <@s.textfield name="person.orcidId" label="ORCID Id" id="orcidId" cssClass="input-xlarge"  maxlength=50 placeholder="XXXX-XXXX-XXXX-XXXX" />
                    <a href="http://orcid.org/about/what-is-orcid">About ORCID</a>                
                </div>
                <div class="span5">
                    <#if RPAEnabled>
                    <@s.textfield  cssClass="input-xlarge" label="RPA Number" name="person.rpaNumber"  maxlength=255 />
                    <a href="http://rpanet.org/">About RPA</a>                
                    </#if>
                </div>
            </div>
    </#macro>

    <#macro hidden>
            <@s.checkbox label='Hide page from logged-out users' name="persistable.hidden" id="hidden-page"  />
    </#macro>

    <#macro contactInfo>
        <div class="">
                <h2 id="contact">Contact</h2>
                    <@s.textfield  label="Phone" cssClass="phoneUS input-xlarge" name="person.phone"  maxlength=255 />
        
                    <#if privacyControlsEnabled>
                        <@s.checkbox label='Make phone public?' name="person.phonePublic" id="phone-public" />
                        <p class="field"><em><b>NOTE:</b> Making your phone # public will display it to anyone who visits ${siteAcronym}, this includes search engines,
                            and visitors who are not logged in.</em></p>
                    </#if>
        
            <h3>Address List</h3>
                <@common.listAddresses person />
        </div>

    </#macro>
</#escape>
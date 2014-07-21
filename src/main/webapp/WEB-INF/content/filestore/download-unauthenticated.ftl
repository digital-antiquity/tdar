<#escape _untrusted as _untrusted?html>
<#if informationResourceFileVersion?has_content>
    <#assign title>${informationResourceFileVersion.filename!"undefined"?html}</#assign>
    <#assign filename>${informationResourceFileVersion.filename!"undefined"?html}</#assign>
    <#assign download ="/filestore/get?informationResourceFileVersionId=${informationResourceFileVersion.id?c}" />
<#else>
    <#assign title>${informationResource.title!"undefined"?html}</#assign>
    <#assign filename>${informationResource.id?c}-download.zip</#assign>
    <#assign download ="/filestore/show-download-landing?informationResourceId=${informationResource.id?c}" />
</#if>
<#import "/WEB-INF/macros/common-auth.ftl" as auth>

<html>
<head>
    <title>Download: ${title}</title>
</head>
<body>
<div class="hero-unit hero-condensed">
        <#--<h1>Register as a ${siteAcronym} User Today</h1>-->

        <#--<p>In order to download files from ${siteAcronym}, you must register as a ${siteAcronym} User.  There is no charge for registering.</p>-->
        <#--<p>We ask that you provide some information and affirm that you will abide by the ${siteAcronym} User Agreement, which simply states that you <em>(1)</em> will not use any of the information that you obtain from tDAR in a way that would damage the archaeological resources; and, <em>(2)</em> will give credit to the individual(s) or organization that created the information that you download</p>-->
        <h1>Register as a ${siteAcronym} User Today</h1>
        <p>You Must Register or Login In Order to Download Files from tDAR</p>

        <p>If you already are a registered tDAR User, please Login.</p>

        <p>Otherwise, please register as a tDAR User.  There is no charge for registering.</p>

        <p>We ask that you provide some information and affirm that you will abide by the tDAR User Agreement, which simply states that you <em>(1)</em> will not use any of the information that you obtain from tDAR in a way that would damage the archaeological resources; and, <em>(2)</em> will give credit to the individual(s) or organization that created the information that you download.</p>
    <dl class="dl-horizontal">
        <ul class="inline">
            <#if ((informationResourceFileVersion.informationResourceFile.latestThumbnail.visible)!false) >
                <li><img src="<@s.url value="/filestore/sm?informationResourceFileVersionId=${informationResourceFileVersion.informationResourceFile.latestThumbnail.id?c}" />"
                    title="${informationResourceFileVersion.filename?html}" alt="${informationResourceFileVersion.filename?html}" /></li>
            <#else>
                <#list (informationResource.informationResourceFiles)! as irFile>
                    <li>
                    <#if (irFile.latestThumbnail)?has_content && irFile.latestThumbnail.visible >
                    <img src="<@s.url value="/filestore/sm?informationResourceFileVersionId=${irFile.latestThumbnail.id?c}" />" 
                    title="${irFile.filename!""?html}" alt="${irFile.filename?html}" />
                    <#else>
                        ${irFile.filename}

                    </#if>
                    </li>
                </#list>
            </#if>
        </ul>
        <dt>Filename</dt>
        <dd>${filename!"undefined"?html}</dd>
        </dl>
</div>

    <div class="row">
        <div class="span9" id="divRegistrationSection">
            <@s.form name='registrationForm' id='registrationForm' method="post" cssClass="disableFormNavigate form-condensed"
                    enctype='multipart/form-data' action="/filestore/process-download-registration">
                    <@s.token name='struts.csrf.token' />
                <fieldset>
                    <legend>Register</legend>
                    <div class="authpane">
                        <div class="authfields">
                            <@auth.registrationFormFields detail="minimal" cols=9 beanPrefix="downloadRegistration" source="download" showSubmit=false/>
                            <@commonFields />
                        </div>
                        <div class="form-actions" style="background-color: transparent">
                            <input type="submit" class="btn btn-mini submitButton tdar-button" name="submitAction" value="Register and Download" >
                        </div>
                    </div>
                </fieldset>

            </@s.form>

        </div>

        <div class="span3" id="divLoginSection">
            <@s.form name='loginForm' id='loginForm'  method="post" cssClass="disableFormNavigate"
                    enctype='multipart/form-data' action="process-download-login">
                <fieldset>
                    <legend>Login</legend>
                    <div class="authpane">
                        <div class="authfields">
                            <@auth.login showLegend=false  beanPrefix="downloadUserLogin" >
                                <@commonFields />
                            </@auth.login>
                        </div>
                        <div class="form-actions">
                            <input type="submit" name="submit" class="btn btn-mini submitButton tdar-button" value="Login and Download">
                        </div>
                    </div>
                </fieldset>
            </@s.form>

        </div>
    </div>
</body>
<#macro commonFields>
    <@s.hidden name="informationResourceId" />
    <@s.hidden name="informationResourceFileVersionId" />
</#macro>
</#escape>
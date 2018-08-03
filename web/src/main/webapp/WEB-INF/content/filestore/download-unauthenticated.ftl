<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/helptext.ftl" as  helptext>

<#if informationResourceFileVersion?has_content>
    <#assign title>${informationResourceFileVersion.filename!"undefined"?html}</#assign>
    <#assign filename>${informationResourceFileVersion.filename!"undefined"?html}</#assign>
    <#assign download ="/filestore/get/${informationResource.id?c}/${informationResourceFileVersion.id?c}" />
<#else>
    <#assign title>${informationResource.title!"undefined"?html}</#assign>
    <#assign filename>${informationResource.id?c}-download.zip</#assign>
    <#assign download ="/filestore/confirm/${informationResource.id?c}" />
</#if>
<#import "/WEB-INF/macros/common-auth.ftl" as auth>

<html>
<head>
    <title>Download: ${title} (Login Required)</title>
    <meta name="robots" content="noindex">
</head>
<body>
        <h1>Download: ${title}</h1>
<div class="hero-unit hero-condensed">
<h2 class="red">Log in / Registration Required</h2>
        <p>You must register or Log in to download files from ${siteAcronym}. If you already are a registered ${siteAcronym} User, please log in. Otherwise, please register below.  There is no charge for registering.</p>

        <p>We ask that you provide some information and affirm that you will abide by the <@s.a href="config.tosUrl" target="_blank" title="click to open contributor agreement in another window">${siteAcronym} User Agreement</@s.a>, 
          <@helptext.userAgreementSummary /></p>
        <ul class="inline">
            <#if ((informationResourceFileVersion.informationResourceFile.latestThumbnail.viewable)!false) >
                <li><img src="<@s.url value="/files/sm/${informationResourceFileVersion.informationResourceFile.latestThumbnail.id?c}" />"
                    title="${informationResourceFileVersion.filename?html}" alt="${informationResourceFileVersion.filename?html}" /></li>
            <#else>
                <#list (informationResource.activeInformationResourceFiles)! as irFile>
                    <li>
                    <#if (irFile.latestThumbnail)?has_content && irFile.latestThumbnail.viewable >
                    <img src="<@s.url value="/files/sm/${irFile.latestThumbnail.id?c}" />" 
                    title="${irFile.filename!""?html}" alt="${irFile.filename?html}" />
                    <#else>
                        ${irFile.filename}

                    </#if>
                    </li>
                </#list>
            </#if>
        </ul>
        <dl class="dl-horizontal">
        <dt>Filename</dt>
        <dd>${filename!"undefined"?html}</dd>
        </dl>
</div>
	<@auth.loginWarning />

    <div class="row">
        <div class="col-9" id="divRegistrationSection">
            <@s.form name='registrationForm' id='registrationForm' method="post" cssClass="disableFormNavigate form-condensed tdarvalidate"
                    enctype='multipart/form-data' action="/filestore/process-download-registration" dynamicAttributes={"data-validate-method":"initRegForm"}>

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

        <div class="col-3" id="divLoginSection">
            <@s.form name='loginForm' id='loginForm'  method="post" cssClass="disableFormNavigate form-condensed"
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
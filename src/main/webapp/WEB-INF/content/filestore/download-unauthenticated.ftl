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
<div class="hero-unit">
    <h1>Please Login or Register to download your file</h1>

    <p>The download you requested will begin momentarily</p>
    <dl class="dl-horizontal">
        <ul class="inline">
            <#if informationResourceFileVersion?has_content && (informationResourceFileVersion.informationResourceFile.latestThumbnail)?has_content>
                <li><img src="<@s.url value="/filestore/sm?informationResourceFileVersionId=${informationResourceFileVersion.informationResourceFile.latestThumbnail.id?c}" />"
                    title="${informationResourceFileVersion.filename?html}" alt="${informationResourceFileVersion.filename?html}" /></li>
            <#else>
                <#list informationResource.informationResourceFiles as irFile>
                    <li><img src="<@s.url value="/filestore/sm?informationResourceFileVersionId=${irFile.latestThumbnail.id?c}" />" 
                    title="${irFile.filename!""?html}" alt="${irFile.filename?html}" /></li>
                </#list>
            </#if>
        </ul>
        <dt>File(s)</dt>
        <dd>${filename!"undefined"?html}</dd>
    </dl>
    <p>
        You've reached this page because you requested a file download when you were not logged into ${siteAcronym}. If your download does not begin
        automatically,
        or if you would like to download the file again, please click on the link above.
    </p>
</div>

    <div class="row">
        <div class="span9" id="divRegistrationSection">
            <@s.form name='registrationForm' id='registrationForm' method="post" cssClass="disableFormNavigate"
                    enctype='multipart/form-data' action="/filestore/process-download-registration">
                <@s.token name='struts.csrf.token' />
                <fieldset>
                    <legend>Register</legend>
                    <@auth.registrationFormFields detail="minimal" cols=9 beanPrefix="downloadRegistration" source="download" />
                </fieldset>
            </@s.form>

        </div>

        <div class="span3" id="divLoginSection">
            <@s.form name='loginForm' id='loginForm'  method="post" cssClass="disableFormNavigate"
                    enctype='multipart/form-data' action="process-download-login">
                <@auth.login showLegend=true  beanPrefix="downloadUserLogin" >
                    <div class="form-actions">
                        <input type="submit" name="submit" class="btn btn-large" value="Login and Continue">
                    </div>
                </@auth.login>
            </@s.form>

        </div>
    </div>
</body>
</#escape>
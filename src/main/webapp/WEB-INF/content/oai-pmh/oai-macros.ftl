<#macro response verb>
<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
    <responseDate><#assign aDateTime = .now>${aDateTime?iso_utc}</responseDate>
    <request verb="${verb?html}"
        <#if identifier??> identifier="${identifier?html}"</#if>
        <#if metadataPrefix??> metadataPrefix="${metadataPrefix?html}"</#if>
        <#if from??> from="${from?html}"</#if>
        <#if until??> until="${until?html}"</#if>
        <#if set??> set="${set?html}"</#if>
        <#if resumptionToken?? && resumptionToken.token?? > resumptionToken="${resumptionToken?html}"</#if>
            >${request.requestURL}?${request.queryString?html}</request>
    <${verb?html}>
    <#nested>
    <#if newResumptionToken?? && newResumptionToken.token?? >
        <resumptionToken>${newResumptionToken.token?html}</resumptionToken>
    </#if>
</${verb?html}>
</OAI-PMH>
</#macro>
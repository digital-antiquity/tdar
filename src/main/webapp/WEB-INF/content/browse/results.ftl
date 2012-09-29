<#include "/WEB-INF/macros/resource/list-macros.ftl" />

<title><#if creator?? && creator.properName??>${creator.properName}<#else>No title</#if></title>

<#if creator??>
  <#if creator.institution??>
  <a href="<@s.url value="${creator.institution.id?c}"/>">${creator.institution}</a>
  <br/></#if>
  <#if authenticated && creator.email??><b>email</b>: ${creator.email}</#if>
<p>${creator.description!''}</p>
<br/>
<#--
<#if (results?? && results.size() > 0)>
<h3>Resources related to ${creator.properName}</h3>
</#if> -->
</#if>
<@listResources results "RESOURCE_TYPE" />


<#import "../email-macro.ftl" as mail /> 


<@mail.content>
Dear ${to.properName},<br />
${from.properName} would like to share <#if invite.resourceCollection?has_content>${invite.resourceCollection.name}<#else>${invite.resource.title}</#if> with you on The Digital Archaeological Record (tDAR). To register and acces your shared materials:  go to <a href="http://core.tdar.org/account/new?id=${to.id?c}&email=${to.email?url('ISO-8859-1')}">tDAR</a> and use <b>${to.email}</b> when registering.

<#if invite.note?has_content>
  <br />
  <p>
  -----------------------------------------------
  ${invite.note} 
  </p> 
</#if>
<br />
<br />
Use this this link to
<a href="${baseUrl}<#if invite.resourceCollection?has_content>${invite.resourceCollection.detailUrl}<#else>${invite.resource.detailUrl}</#if>">
preview the resource(s) being shared.
</a>
</@mail.content>
<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear ${to.properName},<br />
<p>
${from.properName} would like to share <a href="${baseUrl}<#if invite.resourceCollection?has_content>${invite.resourceCollection.detailUrl}<#else>${invite.resource.detailUrl}</#if>"><#if invite.resourceCollection?has_content>${invite.resourceCollection.name}<#else>
${invite.resource.title}</#if></a> with you on The Digital Archaeological Record (tDAR). To register and acces your shared materials:  go to <a href="http://core.tdar.org/account/new?id=${to.id?c}&email=${to.email?url('ISO-8859-1')}">tDAR</a> and use <b>${to.email}</b> when registering.
</p>

<#if invite.note?has_content>
  <br />
  <blockquote>
  ${invite.note} 
  </blockquote> 
</#if>
<br />
<br />
Use this this link to
<a href="${baseUrl}<#if invite.resourceCollection?has_content>${invite.resourceCollection.detailUrl}<#else>${invite.resource.detailUrl}</#if>">
preview the resource(s) being shared.
</a>
</@mail.content>
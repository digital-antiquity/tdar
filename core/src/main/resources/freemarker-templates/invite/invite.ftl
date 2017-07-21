Dear ${to.properName},
  ${from.properName} would like to share <#if invite.resourceCollection?has_content>${invite.resourceCollection.name}<#else>${invite.resource.title}</#if> with you on The Digital Archaeological Record (tDAR). To register and acces your shared materials:  go to http://core.tdar.org/account/new?id=${to.id?c}&email=${to.email?url('ISO-8859-1')} and use ${to.email} when registering.

<#if invite.note?has_content>
  -----------------------------------------------
  ${invite.note}  
</#if>

To preview the resource(s) being shared, you can go here:
 <#if invite.resourceCollection?has_content>${invite.resourceCollection.detailUrl}<#else>(${invite.resource.detailUrl}</#if>
<#import "email-macro.ftl" as mail /> 

<@mail.content>
Dear TDAR Admin,

The following users' access has expired:
<ul> 
<#list notes as note>
 <li>${note}</li>
</#list>
</ul>
</@mail.content>
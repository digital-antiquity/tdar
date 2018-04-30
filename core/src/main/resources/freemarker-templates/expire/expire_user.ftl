<#import "../email-macro.ftl" as mail /> 

<@mail.content>
Dear ${user.properName},
<br><br>
Your access to the following collections/shares has expired: 
<ul> 
<#list notes as note>
 <li>${note}</li>
</#list>
</ul>
</@mail.content>
<#list userInvites>
People you've invited:

    <ul>
        <#items as invite>
        <li>${(invite.emailAddress)!'n/a'}</li>
        </#items>
    </ul>

<#else>
No invites found.
</#list>

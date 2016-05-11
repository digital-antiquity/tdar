<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages - emails </title>
<@admin.header />

<@s.form name="emailReviewForm" action="changeEmailStatus" >
<@s.select name="emailAction" list=emailActions listValue=name label="Change Status To"/>
<@s.submit name="submit" />

<h3>Emails to be Reviewed</h3>

<table class="tableFormat table">
<thead>
<tr>
    <th>Id</th>
    <th>To</th>
    <th>From</th>
    <th>Date</th>
    <th>Status</th>
    <th>Subject</th>
</tr>
</thead>
<#list emailsToReview as email>
    <tr>
        <td><label for="cb${email.id?c}">${email.id?c}&nbsp; <input type="checkbox" name="ids" value="${email.id?c}"  id="cb${email.id?c}" /></label> </td>
        <td>${email.to!''}</td>
        <td>${email.from!''}</td>
        <td>${email.date?string.short}</td>
        <td>${email.status}</td>
        <td>${email.subject!'no subject'}</td>
    </tr>
    <tr class="">
        <td colspan=6>
        <pre>${email.message}</pre>
        <hr/>
        </td>
    </tr>
</#list>
</table>

</@s.form>

<h3>All Emails</h3>
<table class="tableFormat table">
<thead>
<tr>
    <th>Id</th>
    <th>To</th>
    <th>From</th>
    <th>Date</th>
    <th>Status</th>
    <th>Subject</th>
</tr>
</thead>
<#list emails as email>
    <tr>
        <td>${email.id?c} </td>
        <td>${email.to!''}</td>
        <td>${email.from!''}</td>
        <td>${email.date?string.short}</td>
        <td>${email.status}</td>
        <td>${email.subject!'no subject'}</td>
        <td><button class="button btn small" onClick="$('#email-${email.id?c}').toggleClass('hidden');return false;">show/hide</button></td>
    </tr>
    <tr id="email-${email.id?c}" class="<#if email.status=='SENT' || !email.userGenerated>hidden</#if>">
        <td colspan=7>
        <pre>${email.message}</pre>
        <hr/>
        </td>
    </tr>
</#list>
</table>

</#escape>
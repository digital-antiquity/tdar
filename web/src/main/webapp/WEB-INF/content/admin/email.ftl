<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages - emails </title>
<@admin.header />

<@s.form name="emailReviewForm" action="changeEmailStatus" cssClass="form-inline">
<div class="row">
<div class="span2">
<@s.select name="emailAction" list=emailActions listValue=name label="Change Status To"/>
</div>
<div class="span2">
<@s.submit name="submit" />
</div>
</div>


<style>
.email-container {
  position: relative;
  height: 0;
  overflow: hidden;
    padding-bottom: 75%;
}

 
.email-container iframe {
  position: absolute;
  top:0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>

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
            <div class="email-container intrinsic-container-4x3">
            <iframe src="/admin/emailContent/${email.id?c}"></iframe>
            </div>
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
    <th></th>
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
            <div class="email-container">
                <iframe src="/admin/emailContent/${email.id?c}"></iframe>
            </div>
        <hr/>
        </td>
    </tr>
</#list>
</table>

</#escape>
<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages - Confirm Resend Emails </title>
<@admin.header />

<head>
    <style>
    </style>

</head>

<@s.form name="emailReviewForm" action="resendEmail"  method="post" cssClass="form-inline">
<@s.hidden name="emailId" />

<div class="row">
    <div class="span10">
        <p>
        Are you sure you want to resend this email <b>${email.subject}</b> to  <b>${email.to}</b>.<br />
        <br />
        To avoid inadvertent or accidental messages being send, please ensure that the recipient is expecting this message
        and that you wish to immediately send it. The message will be marked as sent and will be removed from the send queue.  
        </p>
       <a href="/admin/email" class='btn btn-secondary inline'>Don't Send</a>
       <button type="submit" class="btn btn-primary">Yes, Send Message</button>
    </div>
</div>
</@s.form>
</#escape>
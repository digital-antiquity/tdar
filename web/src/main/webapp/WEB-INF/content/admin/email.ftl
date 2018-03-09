<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages - emails </title>
<@admin.header />

<head>
    <style>
    </style>

</head>

<script type="text/javascript">
function showMessage(id){
    if($("#email-"+id+" iframe").length==0){
        console.log("there's no iframe for "+id);
        var container = $("#email-"+id+" .email-container");
        console.log(container);
        container.html("<iframe src='/admin/emailContent/"+id+"' seamless='seamless' frameborder='0'></iframe>"); 
    }
    
    $("#email-"+id).toggleClass('hidden');
}


function sendMessage(id){
    $("#emailId").val(id);
    $("#resendMessageForm").submit();
}

</script>

<@s.form name="emailReviewForm" action="changeEmailStatus" cssClass="form-inline">
<div class="row">
    <div class="span2">
        <@s.select name="emailAction" list=emailActions listValue=name label="Change Status To"/>
    </div>
    <div class="span2">
        <@s.submit name="submit" />
    </div>
</div>


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
            <td colspan=6  style="background-color:white;border:1px solid #eee;">
                <div class="email-container intrinsic-container-4x3">
                <iframe id="iframe_rev_${email.id}" src="/admin/emailContent/${email.id?c}" seamless='seamless' frameborder='0'></iframe>
                </div>
            </td>
        </tr>
    </#list>
</table>

</@s.form>

<@s.form name="resendMessageForm" id="resendMessageForm" action="/admin/confirmResendEmail" method="post" cssClass="form-inline">
    <input type="text" name="emailId" id="emailId" />
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
            <td>
                <div class="btn-group small">
                <button class="button btn small" onClick="showMessage(${email.id?c})">Show/Hide</button>
                <button class="button btn small" onClick="sendMessage(${email.id?c})">Resend Email</a>
                </div>
            </td>
        </tr>
        <tr id="email-${email.id?c}" class="hidden">
            <td colspan=7  style="background-color:white;border:1px solid #eee;">
                <div class="email-container">
                </div>
            </td>
        </tr>
    </#list>
    </table>
</#escape>
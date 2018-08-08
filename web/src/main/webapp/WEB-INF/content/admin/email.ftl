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
        container.html("<iframe src='/admin/emailContent/"+id+"' seamless='seamless' frameborder='0' sandbox='allow-popups allow-popups-to-escape-sandbox allow-same-origin allow-scripts'></iframe>"); 
    }
    
    $("#email-"+id).toggleClass('hidden');
}


function sendMessage(id){
    $("#emailId").val(id);
    $("#resendMessageForm").submit();
}

</script>

<h3>Check for Bounced E-Mails</h3>
Manually poll the BouncedEmailQueue for bounced message notifications and mark messages as bounced. 
<@s.form name="pollMessageQueueForm" id="pollMessageQueueForm" action="/admin/pollMessageQueue" method="post" cssClass="form-inline">
    <input type="submit" value="Poll AQS Queue" class="btn" />
</@s.form>
<hr />

<@s.form name="emailReviewForm" action="changeEmailStatus" cssClass="form-inline">

<h3>Emails to be Reviewed</h3>

<div class="row">
    <div class="col-2">
        <@s.select name="emailAction" list=emailActions listValue=name label="Change Status To"/>
        <@s.submit name="submit" />
    </div>
</div>

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
    <#list emailsToReview?sort_by("id")?reverse as email>
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
    <input type="hidden" name="emailId" id="emailId" />
</@s.form>


<hr />

    <h3>All Emails</h3>
    <table class="tableFormat table" style="border:1px solid #eee;">
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
        <tr class='row_${email?item_parity}'>
            <td>${email.id?c} </td>
            <td>${email.to!''}</td>
            <td>${email.from!''}</td>
            <td>${email.date?string.short}</td>
            <td>${email.status}</td>
            <td>${email.subject!'no subject'}</td>
            <td>
                <div class="btn-group inline">
                    <a class="btn btn-sm" href="javascript:showMessage(${email.id?c})">Show/Hide</a>
                    <a class="btn btn-sm" href="javascript:sendMessage(${email.id?c})">Resend Email</a>
                </div>
            </td>
        </tr>
        <#if email.status=='BOUNCED'>
            <tr  class='row_${email?item_parity}'>
                <td colspan='7' style="color:#7a1501">
                   ${email.errorMessage}                 
                </td>            
            </tr>
        </#if>
        
        <tr id="email-${email.id?c}" class="hidden">
            <td colspan=7  style="background-color:white;border:1px solid #eee;">
                <div class="email-container">
                </div>
            </td>
        </tr>
    </#list>
    </table>
</#escape>
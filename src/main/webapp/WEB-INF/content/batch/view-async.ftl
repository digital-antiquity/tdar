<body>
<div class="glide">
    <h3>Bulk Upload Status (this may take some time)</h3>
    <span id="asyncErrors" >
        <div id="unspecifiedError" class="alert alert-error" style="display:none">
            <h3>Unspecified Error</h3>
            <p>An error occured while asking the server for an upload status update.   This does not mean that your upload failed.  
            Please check the <a href='<@s.url value="/dashboard"/>'>dashboard</a> to determine
            whether you successfully uploaded your files. Please notify an administrator if this problem persists.
            </p>
            <div id="errorDetails"></div>
        </div>
    <#if !ticketId??>
        The system has not received any files.   Please try again or notify an administrator if the problem persists.
    </#if>
    </span>
    <div>
        <div id="progressbar" style="width:80%"></div>
        <span id="buildStatus"></span>
    </div>
</div>

<div id="divUploadComplete" class="glide" style="display:none">
    <h3>Upload Complete!</h3>
    <p>The upload process is complete.  If ${siteAcronym} experienced any errors they will be displayed at the top of this page. 
    You can visit the <a href='<@s.url value="/dashboard"/>'>dashboard</a> to review your recently uploaded files.</p>
    <div><a href='<@s.url value="/dashboard"/>' id="btnDashboard">Continue to the Dashboard</div></div>
</div>
<#if ticketId??>
<script type="text/javascript">
//stop pinging for info when the process is done
var gPercentDone = 0;
var TIMEOUT = 1000 / 2; //2fps is all we need.


var updateProgress = function() {
    console.log("updating progress");
    if(gPercentDone >= 100) {
        console.log("progress at 100. no need to continue");
        return;
    };
    
    var url = "<@s.url value="checkstatus"><@s.param name="ticketId" value="${ticketId?c}" /></@s.url>";
    $.ajax({
      url: url,
      dataType: 'json',
      type:'POST',
      success: function(data) {
            gPercentDone = data.percentDone;
            console.log("percent complete: %s", data.percentDone);
            if (data.percentDone != 100) {
                $("#progressbar").progressbar("option", "value", data.percentDone);
                $("#buildStatus").empty().append(data.phase);
                setTimeout(updateProgress, TIMEOUT);
            } else {
                $("#progressbar").progressbar("option", "value", 100);
                $('#divUploadComplete').show();
                //$("#progressbar").progressbar("destroy");
                $("#buildStatus").empty().append("Upload complete.");
                $("#btnDashboard").button();
            }
            if (data.errors  != undefined && data.errors != "") {
                $("#asyncErrors").html("<div class='alert alert-error'><ul>"+data.errors+"</ul></div>");
            }        
        },
      error: function(xhr,txtStatus, errorThrown) {
        gPercentDone = 101;
        console.error("error: %s, %s", txtStatus, errorThrown);
        $('#unspecifiedError').show();
        $("#progressbar").progressbar("disable");
      }
    });
    
    console.log("registered ajax callback");
}


$(document).ready(function(){
    $("#progressbar").progressbar({value : 0});
    setTimeout(updateProgress, TIMEOUT);
});

</script>
</#if>

</body>

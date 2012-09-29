<head>
<#if ticketId??>
<script type="text/javascript">
  $(document).ready(function(){
    $("#progressbar").progressbar({value : 0});
    setTimeout(updateProgress, 200);
  });
  
  function updateProgress() {
    $.getJSON("<@s.url value="checkstatus"><@s.param name="ticketId" value="${ticketId?c}" /></@s.url>", function(data) {
        if (data.percentDone != 100) {
            $("#progressbar").progressbar("option", "value", data.percentDone);
            $("#buildStatus").empty().append(data.phase);
            setTimeout(updateProgress, 200);
        } else {
            $("#progressbar").progressbar("option", "value", 100);
            $("#buildStatus").empty().append("Done.");
            $("#idxBtn").removeAttr('disabled');
        }
        if (data.errors  != undefined && data.errors != "") {
          $("#asyncErrors").append("<div class='action-errors ui-corner-all'>"+data.errors+"</div>");
        }        
    });
  }
</script>
</#if>
</head>
<body>
<div class="glide">
 <h2>Processing bulk upload...</h2>
<span id="asyncErrors" >
<#if !ticketId??>
The system has not received any files.   Please try again or notify an administrator if the problem persists.
</#if>
</span>
<div>
<div id="progressbar" style="width:80%"></div>
<span id="buildStatus"></span>
<br/>
<br/>
</div>
</body>

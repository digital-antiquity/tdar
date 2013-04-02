<head>
<title>Build ${siteAcronym} Index</title>
<script type="text/javascript">
var $buildStatus, $buildLog, $progressbar;
var production = ${production?string('true', 'false')};
$(document).ready(function(){
    $buildStatus = $('#buildStatus');
    $buildLog = $('#buildLog');
    $progressbar = $('#progressbar');

    $progressbar.progressbar({value : 0});
    $("#idxBtn").click(
        function(){
            var confirmed = true;
            if(production) {
                confirmed = confirm("You are in production.  are you sure you want to do this?");
            }
            
            if(confirmed) {
                this.disabled = true;
                $buildStatus.empty().append("Building Index...");
                setTimeout(updateProgress, 200);
            }
        }
    );
});

function updateProgress() {


var url = "<@s.url value="checkstatus"/>?userId=${authenticatedUser.id?c}&";
var indx =0;
$('input[type=checkbox]:checked').each(function() {
 url += "indexesToRebuild[" + indx+ "]=" + $(this).val();
 indx++;
});
    $.getJSON(url, function(data) {
        if (data.percentDone != 100) {
            var timeString = (new Date()).toLocaleTimeString();
            $progressbar.progressbar("option", "value", data.percentDone);
            document.title = "(" + data.percentDone + "%) Build ${siteAcronym} Index";
            if(data.errorHtml) {
                $('#errors').show();
                $('#errors').html(data.errorHtml)
            }
            if($buildStatus.text() != data.phase) {
                 $buildLog.prepend("<br>[" + timeString + "] " + $buildStatus.text().replace("Current Status: ", ""));
                 $buildStatus.empty().append(data.phase);
        }
              
        setTimeout(updateProgress, 200);
        } else {
            $progressbar.progressbar("option", "value", 100);
            document.title = "Indexing complete.";
            $buildStatus.empty().append("Done.");
            $("#idxBtn").removeAttr('disabled');
        }
    });
}
</script>
</head>
<body>
<h1>Rebuild Search Indexes</h1>
    <@s.checkboxlist id="sources" name='indexesToRebuild' list='allSources'  label="what to reindex"/>
<div>
<div id="progressbar"></div>
<br/>
<#if reindexing!false>
<div class="alert">
  <button type="button" class="close" data-dismiss="alert">&times;</button>
  <strong>REINDEX IN PROGRESS!</strong> You are already reindexing on this server.
</div>
</#if>
<#if production>
<div class="alert">
  <button type="button" class="close" data-dismiss="alert">&times;</button>
  <strong>Hey!</strong> You are on the production server.  Please do be careful.
</div>
<input type='button' value='Build Index on core.tdar.org' id='idxBtn' class="btn btn-danger" />
<#else>
<input type='button' value='Build Index' id='idxBtn' class="btn"/>
</#if>
<br/>
<br/>
<span id="buildStatus"></span>
<div id="buildLog" style="height:20em; border: 0px dotted #495251; overflow:auto; font-family:sans-serif"></div>
</div>
</body>
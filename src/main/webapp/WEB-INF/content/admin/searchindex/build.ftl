<head>
<title>Build ${siteAcronym} Index</title>
<script type="text/javascript">
var $buildStatus, $buildLog, $progressbar;
$(document).ready(function(){
    $buildStatus = $('#buildStatus');
    $buildLog = $('#buildLog');
    $progressbar = $('#progressbar');

    $progressbar.progressbar({value : 0});
    $("#idxBtn").click(
        function(){
            this.disabled = true;
            $buildStatus.empty().append("Building Index...");
    setTimeout(updateProgress, 200);
        }
    );
});
  
function updateProgress() {


var url = "<@s.url value="checkstatus"/>?";
var indx =0;
$('input[type=checkbox]:checked').each(function() {
 url += "indexesToRebuild[" + indx+ "]=" + $(this).val() + "&";
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
<input type='button' value='Build Index' id='idxBtn' />
<br/>
<br/>
<span id="buildStatus"></span>
<div id="buildLog" style="height:20em; border: 0px dotted #495251; overflow:auto; font-family:sans-serif"></div>
</div>
</body>
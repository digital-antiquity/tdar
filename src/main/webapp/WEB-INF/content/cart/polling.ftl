<#escape _untrusted as _untrusted?html>
<h1>Checking Billing Status<h1>

<h2>Complete Billing Form</h2>
If the payment window does not open automatically <a href="<#noescape>${redirectUrl}</#noescape>" target="_blank">click here</a></h2>
...
<div class="" id="polling-status">

</div>
<div id="async-errors">
</div>
<script>
var TIMEOUT = 1500; //2fps is all we need.
var newWindow;
$(document).ready(function(){
 updateProgress();
});

setTimeout(function(){newWindow = window.open("<#noescape>${redirectUrl}</#noescape>", "payement window");}, 500);

var updateProgress = function() {
    console.log("updating progress");
 
    var url = "<@s.url value="/cart/${invoice.id?c}/polling-check"/>";
    $.ajax({
      url: url,
      dataType: 'json',
      type:'POST',
      success: function(data) {
            if (data.transactionStatus == 'PENDING_TRANSACTION') {
            	$("#polling-status").html("still pending...");
                setTimeout(updateProgress, TIMEOUT);
            } else {
            	$("#polling-status").html("done: " + data.transactionStatus);
            	if (data.transactionStatus == 'TRANSACTION_SUCCESSFUL') {
            		window.document.location = "${successPath}";
            	}
            }
            if (data.errors  != undefined && data.errors != "") {
                $("#asyncErrors").html("<div class='action-errors ui-corner-all'>"+data.errors+"</div>");
            }        
        },
      error: function(xhr,txtStatus, errorThrown) {
        console.error("error: %s, %s", txtStatus, errorThrown);
      }
    });
    
    console.log("registered ajax callback");
};

</script>
</#escape>

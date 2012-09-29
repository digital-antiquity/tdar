<head>
<script type="text/javascript">
  $(document).ready(function(){
  	$("#progressbar").progressbar({value : 0});
  	$("#idxBtn").click(
  		function(){
  			this.disabled = true;
  			$("#buildStatus").empty().append("Building Index...");
			setTimeout(updateProgress, 200);
  		}
  	);
  });
  
  function updateProgress() {
  	$.getJSON("<@s.url value="checkstatus"/>", function(data) {
  		if (data.percentDone != 100) {
			$("#progressbar").progressbar("option", "value", data.percentDone);
			$("#buildStatus").empty().append(data.phase);
			setTimeout(updateProgress, 200);
		} else {
			$("#progressbar").progressbar("option", "value", 100);
			$("#buildStatus").empty().append("Done.");
			$("#idxBtn").removeAttr('disabled');
		}
  	});
  }
</script>
</head>
<body>
<div>
<div id="progressbar"></div>
<br/>
<input type='button' value='Build Index' id='idxBtn' />
<br/>
<br/>
<span id="buildStatus"></span>
</div>
</body>
jQuery.validator.addMethod( 
	"lessThanTo",
	function(value, element){
		var t = $("#toYear").val();
		if (isNaN(value) || isNaN(t)) return false;
		return (parseInt(value) < parseInt(t));
	},
	"From year should be less than To year."
);

calFromRules = { required:true, number:true, lessThanTo:true };
calToRules = { required:true, number:true };
rcFromRules = { required:true, digits:true, lessThanTo:true };
rcToRules = { required:true, digits:true };

function prepareTemporalFields() {
    console.log("preparing temporal fields");
	$("#fromYear").rules("remove");
	$("#toYear").rules("remove");
	$("#fromYear,#toYear").removeAttr("disabled");
	switch ($("#yearTypeSelect").val()) {
		case "CALENDAR_DATE":
			$("#fromYear").rules("add", calFromRules);
			$("#toYear").rules("add", calToRules);
			break;
		case "RADIOCARBON_DATE":
			$("#fromYear").rules("add", rcFromRules);
			$("#toYear").rules("add", rcToRules);
			break;
		case "NONE":
			$("#fromYear,#toYear").attr("disabled", "disabled");
			$("#fromYear,#toYear").val("");
			break;
	}
}

$(document).ready(function(){
	
	$("#searchForm").validate({
		onsubmit: true,
		onfocusout: false,
		onkeyup: false,
		onclick: false,
        errorLabelContainer: $("#error")
	});
	
	prepareTemporalFields();
	$("#yearTypeSelect").change(prepareTemporalFields);
	
	$("#formResetButton").click(function(){ 
		$("#searchForm_query").val("");
		$("#yearTypeSelect").val("NONE");
		prepareTemporalFields();
		$("select,input[type=hidden],input[type=text]").val("");
		$("input[type=checkbox]").prop("checked",false);
		$("#resourceTypes_Document").prop("checked",true);
		$("#resourceTypes_Dataset").prop("checked",true);
		
		try {
			map.removeOverlay(GZoomControl.G.oZoomArea);
		} catch (e) {}
	});
});



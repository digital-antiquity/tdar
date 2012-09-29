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
	prepareDateFields($('.coverageTypeSelect')[0]);
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
	
	$("#formResetButton").click(function(){ 
		$("#searchForm_query").val("");
		$("#yearTypeSelect").val("NONE");
		$("select,input[type=hidden],input[type=text]").val("");
		$("input[type=checkbox]").prop("checked",false)
		$("#resourceTypes_Document").prop("checked",true);
		$("#resourceTypes_Dataset").prop("checked",true);
		
		try {
			map.removeOverlay(GZoomControl.G.oZoomArea);
		} catch (e) {}
	});
	console.log("applying datepickers");
    $('.datepicker').datepicker({dateFormat: 'm/d/y'});
});



//add/remove validation rules depending on which temporal terms the user has chosen
$(function() {
    //this will need to be revisited after we add more kinds of date types
    
});


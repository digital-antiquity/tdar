TDAR.namespace("pricing");

TDAR.pricing = function() {
    'use strict';

var _initPricing = function(form, ajaxUrl) {
	$(form).change(function() { 
	var numFiles = $("#MetadataForm_invoice_numberOfFiles").val();
	var numMb = $("#MetadataForm_invoice_numberOfMb").val();
	
	/* give the user an understanding of size in GB if size is > 1/2 GB */
	var mb = "";
	if (numMb > 512) {
	    var num = numMb / 1024;
	    if (num.toString().indexOf(".") > 0) {
	    	num = num.toFixed(3);
	    }
	 	mb = "(" + (num) + " GB)";
	}
	$("#convert").html(mb);
	
	var $est = $("#estimated");
    var url = ajaxUrl + "?lookupMBCount=" + numMb + "&lookupFileCount=" + numFiles;
    $.ajax({
      url: url,
      dataType: 'json',
      type:'POST',
      success: function(data) {
       
      $est.html("<h4>Suggested Pricing Options</h4><ul>");
      for (var i=0; i < data.length; i++) {
      var line = sprintf("<li><div><h5>Option {0}:{1} -- <span class='red'>${2}</span></h5><p><ul>", (i +1), data[i].model, data[i].subtotal );
      	for (var j=0; j < data[i].parts.length; j++) {
	      	var part = data[i].parts[j];
      		line += sprintf("<li> {0} <b>{1}</b> @ ${2} (${3})",  part.quantity , part.name , part.price  , part.subtotal );
      	}
      line += "</ul> </p></div></li>";
//      console.log(line);
	      $est.append(line);
      };
	$est.append("</ul>");
	//	console.log(data);
        },
      error: function(xhr,txtStatus, errorThrown) {
        console.error("error: %s, %s", txtStatus, errorThrown);
      }
    });
	});
 };

 return {
	    "initPricing": _initPricing,
	};

}();
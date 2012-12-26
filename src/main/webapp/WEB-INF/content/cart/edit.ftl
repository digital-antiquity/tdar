<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>
<style>
#convert {
	margin-left:10px;
}
</style>
</head>
<body>

<h1>What would you like to put into tDAR?</h1>
	<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>
<div class="row">
	<div class="span6">
		<div class="well">
		<@s.textfield name="invoice.numberOfFiles" label="Number of Files" cssClass="integer"/>
		
		<div class="control-group">
		    <label class="control-label">Number of Mb</label>
				    <div class="controls">
			<@s.textfield name="invoice.numberOfMb" label="Number of Mb"  theme="simple" cssClass="integer "/>
			<span id="convert"></span>
		</div>
		<br/>
		<div id="estimated">
		
		</div>
	    <@s.hidden name="id" value="${invoice.id?c!-1}" />
    <@s.hidden name="invoice.id" />
    <#if !production || administrator || editor >
    <hr>
    <p><b>For Testing Only:</b></p>
		<div class="control-group">
    <label class="control-label" for="extraItemQuantity">Quantity</label>
				    <div class="controls">
			    <input type="text" name="extraItemQuantity" id="extraItemQuantity" value="0" label="Quantity" />
		</div>
	</div>
		<div class="control-group">
    <label class="control-label" for="extraItemQuantity">Type</label>
				    <div class="controls">
    <label><input type="radio" name="extraItemName"  value="" id="extraItemName" /> None</label>
    <label><input type="radio" name="extraItemName"  value="error" id="extraItemName_error" /> Error</label>
    <label><input type="radio" name="extraItemName"  value="decline" id="extraItemName_decline" /> Decline</label>
    <label><input type="radio" name="extraItemName"  value="unknown" id="extraItemName_unknown"/> Unknown</label>
		</div>
	</div>

	</#if>
	</div>	
	</div>
	</div>
	<div class="span6">
		<table class="tableFormat">
		    <tr>
		        <th>Level</th>
		        <th># of files</th>
		        <th># of mb</th>
		        <th>cost / file</th>
		    </tr>
		    <#list activities as act>
		    <tr>
		        <td>${act.name}</td>
		        <td>${act.numberOfFiles!0}</td>
		        <td>${act.numberOfMb!0}</td>
		        <td>${act.price} ${act.currency!"USD"}</td>
		    </tr> 
		    
		    </#list>
	    </table>
	</div>
</div>
<div class="row">
    <@edit.submit fileReminder=false />
</div>
</@s.form>

<script>
$(document).ready(function(){
    'use strict';
    TDAR.common.initEditPage($('#MetadataForm')[0]);
});

$("#MetadataForm").change(function() { 
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
    var url = "<@s.url value="/cart/api"/>?lookupMBCount=" + numMb + "&lookupFileCount=" + numFiles;
    $.ajax({
      url: url,
      dataType: 'json',
      type:'POST',
      success: function(data) {
       
      $est.html("<h4>Suggested Pricing Options</h4><ul>");
      for (var i=0; i < data.length; i++) {
      <#noparse>
      var line = sprintf("<li><div><h5>Option {0}:{1} -- <span class='red'>${2}</span></h5><p><ul>", (i +1), data[i].model, data[i].subtotal );
      	for (var j=0; j < data[i].parts.length; j++) {
	      	var part = data[i].parts[j];
      		line += sprintf("<li> {0} <b>{1}</b> @ ${2} (${3})",  part.quantity , part.name , part.price  , part.subtotal );
      	}
      line += "</ul> </p></div></li>";
      </#noparse>
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
</script>

</body>
</#escape>

<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>
<style>
#convert {
margin-left:10px;}
</style>
</head>
<body>

<h1>Your cart</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>

<@s.textfield name="invoice.numberOfFiles" label="Number of Files" cssClass="integer"/>

<div class="control-group">
    <label class="control-label">Number of Mb</label>
	    <div class="controls">
<@s.textfield name="invoice.numberOfMb" label="Number of Mb"  theme="simple" cssClass="integer "/>
<span id="convert"></span>

</div>

</div>


<script>


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
       
      $est.html("<h5>Suggested Pricing Options</h5>");
      for (var i=0; i < data.length; i++) {
      var line = "<p><b>total</b>: $" + data[i].subtotal + " [";
      	for (var j=0; j < data[i].parts.length; j++) {
      	var part = data[i].parts[j];
      		line +=  part.quantity + " " + part.name + " @ $" + part.price  + ": $" + part.subtotal + " ; ";
      	}
      line += "] <i>"+data[i].model+"</i></p>";
//      console.log(line);
	      $est.append(line);
      };
	//	console.log(data);
        },
      error: function(xhr,txtStatus, errorThrown) {
        console.error("error: %s, %s", txtStatus, errorThrown);
      }
    });
});
</script>
<div id="estimated">

</div>
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
    <#if invoice.address?has_content>
    <@s.hidden name="invoice.addresss.id" value="${invoice.address.id?c}" />
    </#if>
    <@s.hidden name="id" value="${invoice.id?c!-1}" />
    <@s.hidden name="invoice.id" />
    <@edit.submit fileReminder=false />
</@s.form>

</div>
<script>
$(document).ready(function(){
    'use strict';
    TDAR.common.initEditPage($('#MetadataForm')[0]);
});
</script>
</body>
</#escape>

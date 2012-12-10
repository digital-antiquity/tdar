<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

<h1>Your cart</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>

<@s.textfield name="invoice.numberOfFiles" label="Number of Files" />
<@s.textfield name="invoice.numberOfMb" label="Number of Mb" />



<script>

var fileData = [];
<#list activities as act>
 fileData[fileData.length] = {'name':'${act.name?js_string}', 'numFiles':${act.numberOfFiles?c},'cost':${act.price}};
</#list>

$("#MetadataForm_numberOfFiles").change(function() { 
	var current=1;
	var val = $(this).val();  
	$('#estimated').html('');
	for (index in fileData) {
		if (val >= fileData[index].numFiles) {
			$('#estimated').append('<p><strong>$' + fileData[index].cost * val + ' ' + (val * 10) + 'MB </strong> (' + fileData[index].name +')</p>');
		}
	}
	
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
        <td>${act.displayNumberOfFiles!act.numberOfFiles}</td>
        <td>${act.displayNumberOfMb!act.numberOfMb}</td>
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

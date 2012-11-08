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
<table class="tableFormat">
	<tr>
		<th>item</th>
		<th># of resources</th>
		<th># of files</th>
		<th># of mb</th>
		<th># of support hours</th>
		<th>cost</th>
		<th>quantity</th>
	</tr>
	<#list activities as act>
	<tr>
		<td>${act.name}</td>
		<td>${act.displayNumberOfResources!act.numberOfResources}</td>
		<td>${act.displayNumberOfFiles!act.numberOfFiles}</td>
		<td>${act.displayNumberOfMb!act.numberOfMb}</td>
		<td>${act.numberOfHours}</td>
		<td>${act.price} ${act.currency!"USD"}</td>
		<td>
			<@s.hidden name="invoice.items[${act_index}].activity.id" value="${act.id}" />
			<@s.textfield name="invoice.items[${act_index}].quantity" cssClass="integer" />
		</td>
	</tr> 
	
	</#list>
	</table>
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

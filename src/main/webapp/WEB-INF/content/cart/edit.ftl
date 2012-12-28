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
    <label><input type="radio" name="extraItemName" checked=checked  value="" id="extraItemName" /> None</label>
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
    TDAR.common.initEditPage($('#MetadataForm')[0]);
    TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/cart/api"/>");
});
</script>

</body>
</#escape>

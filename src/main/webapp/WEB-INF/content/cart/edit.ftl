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
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>
<#if billingManager>
<div class="admin-only">
    <h1>Choose the Invoice Owner</h1>
    <div class="control-group">
        <label class="control-label">Invoice Owner</label>
        <div class="controls">
        <@edit.userRow person=blankAuthorizedUser.user _indexNumber="" isDisabled=false includeRole=false _personPrefix="owner" prefix="invoice" 
                includeRights=false isUser=true includeRepeatRow=true/>

            <@edit.clearDeleteButton id="clearAssignedOwner" />
        </div>
    </div>
</div>
</#if>

<h1>What would you like to put into tDAR?</h1>
<div class="row">
	<div class="span3">
<h3>Rates</h3>
		<table class="tableFormat">
		    <tr>
		        <th>Item</th>
		        <th>Cost</th>
		    </tr>
		    <#list activities as act>
		    <#if act.production >
		    <tr>
		        <td>${act.name} <#if (act.numberOfFiles > 0)>Files</#if></td>
		        <td>${act.price} ${act.currency!"USD"}</td>
		    </tr> 
			</#if>		    
		    </#list>
	    </table>
	</div>

	<div class="span9">
	<ul class="nav nav-tabs" >
	  <li class="active">
	    <a href="#suggested" data-toggle="tab">Suggested</a>
	  </li>
	  <li><a href="#custom" data-toggle="tab">Custom</a></li>
	</ul>
	<div class="tab-content" >
		<div id="suggested" class="tab-pane active">
		<div class="row">
			<div class="span2 well">
			<h3>Small: <span class="red">$50</span></h3>
			<ul>
				<li>1 File</li>
				<li>10 MB</li>
			</ul>

			<span class="tdar-button">Choose</span>
			</div>
			<div class="span2 well">
			<h3>Medium:<span class="red">$500</span></h3>
			<ul>
				<li>10 File</li>
				<li>1200 MB</li>
			</ul>

			<span class="tdar-button">Choose</span>
			
			</div>
			<div class="span2 well">
			<h3>Large <span class="red">$2,500</span></h3>
			<ul>
				<li>100 File</li>
				<li>10500 MB</li>
			</ul>

			<span class="tdar-button">Choose</span>
			</div>
		</div>
		
		</div>
		<div id="custom" class="tab-pane">
			<div class="well">
			<@s.textfield name="invoice.numberOfFiles" label="Number of Files" cssClass="integer span2"/>
			
			<div class="control-group">
			    <label class="control-label">Number of Mb</label>
			    <div class="controls">
				<@s.textfield name="invoice.numberOfMb" label="Number of Mb"  theme="simple" cssClass="integer span2"/>
				<span id="convert"></span>
			</div>
			<br/>
			<div >
				<h4>Cost: $<span class="red" id="price">0.00</span></h4>
				<table class="table tableFormat">
					<tr><th>Item</th><th> # Files</th><th>Space in MB</th><th>Subtotal</th></tr>
					<tbody id="estimated">
					<tr><td colspan=5>enter number of files and mb above</td>	
					</tbody>
					</table>
			</div>
		</div>
	</div>
</div>
</div>

<div class="row">
	<div class="span6">
	    <@s.hidden name="id" value="${invoice.id?c!-1}" />
    <@s.hidden name="invoice.id" />
    <#if !production || administrator || editor >
    <hr>
    <p><b>For Testing Only:</b></p>
		<div class="control-group">
    <label class="control-label" for="extraItemQuantity">Quantity</label>
				    <div class="controls">
			    <input type="text" name="extraItemQuantity" id="extraItemQuantity" value="0" label="Quantity"  class="integer span2"/>
		</div>
	</div>
		<div class="control-group">
	    <label class="control-label" for="extraItemQuantity">Type</label>
	    <div class="controls">
	    <label><input type="radio" name="extraItemName" checked=checked  value="" id="extraItemName" /> None</label>
	    <#list activities as act>
		    <#if !act.production >
			    <label><input type="radio" name="extraItemName"  value="${act.name}" id="extraItemName_${act.name}" /> ${act.name}</label>
	    	</#if>
		</#list>
		</div>
	</div>

	</#if>
	</div>	
	</div>
	</div>
</div>
<div class="row">
    <@edit.submit fileReminder=false label="Next: Review & Choose Payment Method" />
</div>
</@s.form>

<script>
$(document).ready(function(){
    TDAR.common.initEditPage($('#MetadataForm')[0]);
    TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/cart/api"/>");
    applyPersonAutoComplete($(".userAutoComplete"), true, false);
});
</script>

</body>
</#escape>

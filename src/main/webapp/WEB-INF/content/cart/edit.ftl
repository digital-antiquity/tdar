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
<#macro pricingOption label files storage cost id=label?lower_case>
<div class="span2 well" id=div${id}>
    <h3><span class="red">$${cost}</span>: ${label}</h3>
    <ul>
        <li>${files}</li>
        <li>${storage}</li>
    </ul>
    
    <button type="button" class="tdar-button" id="${id}-option">SELECT</button>
</div>
</#macro>
<body>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action='save'>

<h1>What would you like to put into tDAR?</h1>
<p>In tDAR, billing accounts are used to manage resources.  Each resource must be associated with an account. tDAR is run by Digital Antiquity, a not-for-profit organization dedicated to the preservation of archaeological information. The fees related to upload are used to ensure the proper preservation of materials uploaded to tDAR.</p><p><strong>Managing Accounts</strong><br>Accounts can be shared between users, and users can grant access to modify or manage resources to any tDAR user they choose.</p>
<h2>Rates &amp; Calculator</h2>
<div class="row">
	<div class="span12">
	<ul class="nav nav-tabs" >
	  <li class="<#if !administrator>active</#if>">
	    <a href="#suggested" data-toggle="tab">Suggested</a>
	  </li>
	  <li class="<#if administrator>active</#if>"><a href="#custom" data-toggle="tab">Custom &amp; Calculator</a></li>
	</ul>
	<div class="tab-content row" >
		<div id="suggested" class="tab-pane <#if !administrator>active</#if> span12">
		<div class="row">
			<@rates />			
			<div class="span8">
				<h2>Suggested Levels</h2>
				<div class="row">
    			    <@pricingOption label="Small" files="1 File" storage="10 MB" cost=50 />
    			    <@pricingOption label="Medium" files="10 Files" storage="100 MB" cost=400 />
    			    <@pricingOption label="Large" files="100 Files" storage="1 GB" cost=2500 />
				</div>
			</div>
		</div>
		</div>
		<div id="custom" class="tab-pane <#if administrator>active</#if> span12">
			<div class="row">
				<@rates />	
				<div class="span8">
				<h2>Cost Calculator</h2>
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
					<@s.textfield name="code" label="Redeem Code" />
			    <@edit.submit fileReminder=false label="Next: Review & Choose Payment Method" span="span4" />
				
			</div>
		</div>
</div>
</div>

<div class="row">
	<div class="span6">
    <@s.hidden name="id" value="${invoice.id?c!-1}" />
    <#if !production || administrator || editor >
    <hr>
    <p><b>For Admin Use Only:</b></p>
	<table class="table tableFormat">
		<thead>
		<tr>
			<th>Quantity</th>
			<th>Item</th>
		</tr>
		</thead>
	    <#list activities as act>
		    <#if !act.production >
			<tr>
				<td>
				<@s.textfield name="extraItemQuantities[${act_index}]" cssClass="integer span2"/>
				</td>
				<td>
					${act.name}
					<@s.hidden name="extraItemIds[${act_index}]" value="${act.id?c}"/>
				</td>				
			</tr>
	    	</#if>
		</#list>
		</table>
	</div>

	</#if>
	</div>	
	</div>
	</div>
</div>


<#if billingManager>
<div class="admin-only">
    <h1>Choose the Invoice Owner</h1>
    <div class="control-group">
        <label class="control-label">Invoice Owner</label>
        <div class="controls">
        <@edit.userRow person=blankAuthorizedUser.user _indexNumber="" isDisabled=false includeRole=false prefix="owner" 
                includeRights=false isUser=true includeRepeatRow=true/>

            <@edit.clearDeleteButton id="clearAssignedOwner" />
        </div>
    </div>
</div>
</#if>


</@s.form>

<script>
$(document).ready(function(){
    TDAR.common.initEditPage($('#MetadataForm')[0]);
    TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/cart/api"/>");
    applyPersonAutoComplete($(".userAutoComplete"), true, false);
});
</script>

</body>

<#macro rates>
<div class="span3" style="border-right: 1px dashed #bbb;padding-right: 40px;">
	<h2>Rates</h2>
	<table class="tableFormat table">
	    <tr>
	        <th>Item/Service</th>
	        <th>Cost</th>
	    </tr>
	    <#list activities as act>
	    <#if act.production >
	    <tr>
	        <td>${act.name}</td>
	        <td>${act.price} ${act.currency!"USD"}</td>
	    </tr> 
		</#if>		    
	    </#list>
    </table>
    <p><em>* All files come with 10 MB of space</em></p>
</div>


</#macro>

</#escape>

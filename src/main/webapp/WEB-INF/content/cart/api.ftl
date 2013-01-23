<#if callback??>${callback}(</#if>
[<#list pricingOptions as option>
	<#if option_index != 0>,</#if>{
		"subtotal":${option.subtotal?c},
		"model":"${option.type}",
		"model":"${option.label!option.type}",
		"parts":[
		<#assign count = 0 />
			<#list option.items as item>
	<#if count != 0>,</#if>
	<#if item??>
			{
			"quantity":${item.quantity?c},
			"price":${item.activity.price?c},
			"numFiles":${(item.quantity * item.activity.numberOfFiles)?c},
			"numMb":${(item.quantity * item.activity.numberOfMb)?c},
			"subtotal":${item.subtotal?c},
			"name": "<#if item.activity.numberOfFiles == 0>Extra Space<#else>Files</#if>"
			}
		<#assign count = count+1 />
		</#if>
		</#list>
		]
	}
</#list>]
<#if callback??>);</#if>

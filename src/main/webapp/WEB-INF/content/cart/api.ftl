<#if callback??>${callback}(</#if>
[<#list pricingOptions as option>
	<#if option_index != 0>,</#if>{
		"subtotal":${option.subtotal},
		"parts":[
		<#assign count = 0 />
			<#list option.items as item>
	<#if count != 0>,</#if>
	<#if item??>
			{
			"quantity":${item.quantity?c},
			"subtotal":${item.subtotal?c},
			"name": "${item.activity.name?js_string}"
			}
		<#assign count = count+1 />
		</#if>
		</#list>
		]
	}
</#list>]
<#if callback??>);</#if>

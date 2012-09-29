<!-- colors for bar charts -->
<#assign barColors = ['#4B514D', '#2C4D56','#C3AA72','#DC7612','#BD3200','#A09D5B','#F6D86B'] />
<!-- colors for homepage map -->
<#assign mapColors = ["ebd790","D6B84B","C3AA72","A09D5B","909D5B","DC7612","DC5000","BD3200","660000"] />
<#global siteName = "the Australian Historical Archaeological Database" />

<#macro themeColors separator=","><#assign ret=""/><#list barColors as color>${ret}<#assign ret><#if color_index !=0>${separator}</#if>${color?replace("#","")}</#assign></#list>${ret}</#macro>

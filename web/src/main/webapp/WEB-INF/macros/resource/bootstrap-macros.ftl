<#-- bootsrap helper macros and functions -->

<#-- Optionally append '-fluid' to the string if action wants a fluid layout. -->
<#-- Freemarker has no notion of "private" macros, so we're using the honor system here. -->
<#function __ctfmt str>
    <#return ((gridSystemType!"fixed")=="fluid")?string(str + "-fluid", str)>
</#function>

<#assign __ctcontainer = __ctfmt("container")>
<#assign __ctrow = __ctfmt("row")>

<!-- return 'container' or 'container-fluid' -->
<#function container>
    <#return __ctcontainer>
</#function>


<!-- return 'row' or 'row' -->
<#function row>
    <#return __ctrow>
</#function>

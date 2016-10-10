<#setting url_escaping_charset="UTF-8">

<#list topLevelPaths>
<h5>All items in Dropbox</h5>
<ul>
    <#items as item>
    <li><a href="items/${item}">${item}</a></li>
    </#items>
</ul>
</#list>


<#list topLevelManagedPaths>
<h5>Items in a Workflow</h5>
<ul>
    <#items as item>
    <li><a href="items/${item}">${item}</a></li>
    </#items>
</ul>

</#list>
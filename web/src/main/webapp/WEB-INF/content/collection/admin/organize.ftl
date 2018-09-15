<head>
</head>

<body>
<h1>Organize ${collection.name} and its contents</h1>
<p>Drag resources and collections in the tree to reorganize collections and their contents.</p>
<p><b>Note:</b> Changes are made immediately.</p>
<div id="jstree">
<#list tree>
<ul >
    <#items as rc>
        <@_collectionListItem rc />
    </#items>
</ul>
</#list>
</div>

<#macro _collectionListItem rc>
        <li data-collectionid="${rc.id?c}" data-jstree='{"opened":true}'>${rc.name}
            <ul>
                <#list rc.managedResources as resource>
                     <li data-jstree='{"opened":true, "type":"file"}' data-resourceid="${resource.id?c}">${resource.title} (${resource.id?c})<i data-url="${resource.detailUrl}" class="icon-share-alt"></i></li>
                </#list>
                <#list rc.unmanagedResources as resource>
                     <li data-jstree='{"opened":true, "type":"file"}' data-resourceid="${resource.id?c}">${resource.title} (${resource.id?c})<i data-url="${resource.detailUrl}" class="icon-share-alt"></i></li>
                </#list>
                    <#list rc.transientChildren as child>
                        <@_collectionListItem child />
                    </#list>
                
            </ul>
        </li>
</#macro>

</body>
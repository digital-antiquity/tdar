<head>
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/jstree/3.0.9/themes/default/style.min.css" />
<script src="//cdnjs.cloudflare.com/ajax/libs/jstree/3.0.9/jstree.min.js"></script>
</head>

<body>
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
                <#list rc.resources as resource>
                     <li data-jstree='{"opened":true, "icon":"file"}' data-resourceid="${resource.id?c}">${resource.title} (${resource.id?c})</li>
                </#list>
                    <#list rc.transientChildren as child>
                        <@_collectionListItem child />
                    </#list>
                
            </ul>
        </li>
</#macro>

<script>
$(document).ready(function() {
var $tree = $("#jstree");
 var $jstree = $tree.jstree({
  "core" : { "check_callback" :  function (operation, node, parent, position, more) {
      if(operation === "move_node") {
        if(parent.resourceid != undefined ||  parent.data.resourceid != undefined) {
          return false; // prevent moving a child above or below the root
        }
      }
      return true; // allow everything else
    } }, // so that operations work
  "plugins" : ["dnd"]
}).bind("move_node.jstree", function (e, data) {
        // data.rslt.o is a list of objects that were moved
        // Inspect data using your fav dev tools to see what the properties are
    if (data.old_parent != data.parent) {
        console.log(e);
        console.log(data);
        var rid = data.node.data.resourceid;
        var fromid = $jstree.jstree(true).get_node(data.old_parent).data.collectionid;
        var toid = $jstree.jstree(true).get_node(data.parent).data.collectionid;
        console.log($jstree.jstree(true).get_node(data.parent).data.collectionid);
        console.log($jstree.jstree(true).get_node(data.old_parent).data.collectionid);
        $.post("/api/collection/move?resourceId="+rid + "&fromCollectionId="+fromid+"&toCollectionId="+toid);
        }
        });
   // });
});
</script>
</body>
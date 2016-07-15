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
var revert = false;
 var $jstree = $tree.jstree({
  "core" : { "check_callback" :  function (operation, node, parent, position, more) {
          
      if(operation === "move_node") {
        if(parent.resourceid != undefined || (parent.data != undefined && parent.data.resourceid != undefined) || parent.id == node.parent) {
          return false; // prevent moving a child above or below the root
        }
      }
      // ignore reorder
      if(more && more.dnd && more.pos !== "i") { return false; }
      return true; // allow everything else
    } }, // so that operations work
  "plugins" : ["dnd"]
}).bind("move_node.jstree", function (e, data) {
        // data.rslt.o is a list of objects that were moved
        // Inspect data using your fav dev tools to see what the properties are
        var error = false;
        if (revert) {
            revert = false;
            return true;
        }
        var $parent = $jstree.jstree(true).get_node(data.parent);
        if (data.old_parent != data.parent) {
            if (data.node.data.resourceid != undefined) {
                var rid = data.node.data.resourceid;
                var fromid = $jstree.jstree(true).get_node(data.old_parent).data.collectionid;
                var toid = $parent.data.collectionid;
                console.log(rid + " from: " + fromid + " ("+data.old_parent+")" + " --> "+ toid + " ("+data.parent+")"); 
                $.post("/api/collection/moveResource?resourceId="+rid + "&fromCollectionId="+fromid+"&toCollectionId="+toid).done(function() {
                    console.log( " success" );
                }).fail(function() {
                    error = true;
                    console.log("reverting " + data.node.id + " --> " + data.old_parent );
                    revert = true;
                    $tree.jstree('move_node', data.node.id, data.old_parent );
                    alert( "error moving resource" );
                });
            } else if (data.node.data.collectionid  != undefined) {
                var cid = data.node.data.collectionid;
                var toid = $parent.data.collectionid;
                console.log(rid + " --> "+ toid); 
                $.post("/api/collection/moveCollection?resourceId="+rid + "&fromCollectionId="+fromid+"&toCollectionId="+toid).done(function() {
                    console.log( " success" );
                }).fail(function() {
                    error = true;
                    console.log("reverting " + data.node.id + " --> " + data.old_parent );
                    revert = true;
                    $tree.jstree('move_node', data.node.id, data.old_parent );
                    alert( "error moving collection" );
                });
            }

        }
        });
   // });
});
</script>
</body>
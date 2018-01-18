<head>
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/jstree/3.0.9/themes/default/style.min.css" />
<script src="//cdnjs.cloudflare.com/ajax/libs/jstree/3.0.9/jstree.min.js"></script>
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

<script>
$(document).ready(function() {
var $tree = $("#jstree");
var revert = false;
 var $jstree = $tree.jstree({
 
  "types" : {
    "file" : {
      "icon" : "glyphicon glyphicon-file icon-file",
      "valid_children" : []
    },
  },
 
  "core" : { "check_callback" :  function (operation, node, parent, position, more) {
          
      if(operation === "move_node") {
      
        // if we're a resource
        if(node.data.resourceid) {
            console.log("checking move from " + node.parent + " --> " + parent.id + "  for " + node.id); 

            if (parent.resourceid != undefined) {
//                console.log("parent is a resource");
                return false;
            }
            if (parent.data != undefined && parent.data.resourceid != undefined) {
//                console.log("parent appears to be a resource");
                return false; // prevent moving a child above or below the root
            }
        } else if (node.data.collectionid) {
            if (parent.collectionid == undefined && (parent.data != undefined && parent.data.collectionid == undefined)) {
                return false; // prevent moving a child above or below the root
            }
        }
      }
      // ignore reorder
      if(more && more.dnd && more.pos !== "i") { 
  //      console.log("reorder??");
        return false;
         }
//        console.log ("returning true");
      return true; // allow everything else
    } }, // so that operations work
  "plugins" : ["dnd","types"]
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
            console.log("moving from " + data.old_parent + " to " + data.parent);
            if (data.node.data.resourceid != undefined) {
                var rid = data.node.data.resourceid;
                var fromid = $jstree.jstree(true).get_node(data.old_parent).data.collectionid;
                var toid = $parent.data.collectionid;
                console.log(rid + " from: " + fromid + " ("+data.old_parent+")" + " --> "+ toid + " ("+data.parent+")"); 
                $.post("/api/collection/moveResource?resourceId="+rid + "&fromCollectionId="+fromid+"&toCollectionId="+toid).done(function(response) {
                    console.log(response.status);
                }).fail(function(response) {
                    console.log(response.status);
                    error = true;
                    console.log("reverting " + data.node.id + " --> " + data.old_parent );
                    revert = true;
                    $tree.jstree('move_node', data.node.id, data.old_parent );
                    alert( "error moving resource" );
                });
            } else if (data.node.data.collectionid  != undefined) {
                var cid = data.node.data.collectionid;
                var toid = $parent.data.collectionid;
                console.log(cid + " --> "+ toid); 
                $.post("/api/collection/moveCollection?collectionId="+cid + "&toCollectionId="+toid).done(function(response) {
                    console.log( " success" );
                }).fail(function(response) {
                    error = true;
                    console.log("reverting " + data.node.id + " --> " + data.old_parent );
                    revert = true;
                    $tree.jstree('move_node', data.node.id, data.old_parent );
                    alert( "error moving collection" );
                });
            }

        }
    });
    TDAR.common.registerAjaxStatusContainer();
});
</script>
</body>
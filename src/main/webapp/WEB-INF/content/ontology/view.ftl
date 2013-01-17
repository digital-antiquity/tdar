<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<@view.htmlHeader resourceType="ontology">
<meta name="lastModifiedDate" content="$Date$"/>
<style type='text/css'>
  #infovis {
      height:300px;
      overflow:hidden;
  }
</style>
<!-- JIT Library File --> 
<@view.datatableChildJavascript />
<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />


<@view.projectAssociation resourceType="ontology" />

<@view.infoResourceBasicInformation />

<h3>Ontology</h3>


<@view.datatableChild />

<div id="infovis" class="img-polaroid ">
</div>
<!--</fieldset> -->
<div id="divHints">
<em>click and drag to pan across the ontology, darker nodes contain more children, click on a node to expand children</em>
</div>
<@view.ontology />

<@view.sharedViewComponents resource />


<#macro makeNode node_>
  { id: "${node_.id?c}", name: "${node_.displayName?js_string}", data: {},
    children: [
    <@s.iterator value="getChildElements(${node_.index})" var="root_" status="stat">
      <@makeNode root_ /><#if !stat.last>,</#if>
    </@s.iterator>
    ]}
</#macro>


<script>
 <#if rootElements?size == 1>
   var json =     <@s.iterator value="rootElements" var="root_" status="stat">
      <@makeNode root_ />
    </@s.iterator>;
 <#else>
   var json = {id:"root",name: "root", data:{},
   children: [
    <@s.iterator value="rootElements" var="root_" status="stat">
      <@makeNode root_/><#if !stat.last>,</#if>
   </@s.iterator>
   ]};
 </#if>

jQuery(document).ready(function($){

//Create a new ST instance  
var st = new $jit.ST({  
    //id of viz container element  
    injectInto: 'infovis',  
    //set duration for the animation  
    duration: 800,  
    //set animation transition type  
    transition: $jit.Trans.Quart.easeInOut,  
    //set distance between node and its children  
    levelDistance: 50,
    offsetY: 50,
    orientation:'top',
    //enable panning  
    Navigation: {  
      enable:true,  
      panning:true  
    },  
    //set node and edge styles  
    //set overridable=true for styling individual  
    //nodes or edges  
    Node: {  
        height: 40,  
        width: 125,  
        type: 'rectangle',  
        color: '#aaa',  
        overridable: true  
    },  
      
    Edge: {  
        type: 'bezier',  
        overridable: true  
    },  
      
    //This method is called on DOM label creation.  
    //Use this method to add event handlers and styles to  
    //your node.  
    onCreateLabel: function(label, node){  
        label.id = node.id;
        label.innerHTML = node.name;  
        label.onclick = function(){  
            if(true) {  
              st.onClick(node.id);  
            } else {  
            st.setRoot(node.id, 'animate');  
            }  
        };
        //set label styles  
        var style = label.style;  
        style.width = 115 + 'px';  
        style.cursor = 'pointer';  
        style.color = '#333';  
        style.fontSize = '8pt';
        style.lineHeight = "100%";
        style.textAlign= 'center';  
        style.align= 'middle';  
        style.paddingTop = '3px';  
    },  
      
    //This method is called right before plotting  
    //a node. It's useful for changing an individual node  
    //style properties before plotting it.  
    //The data properties prefixed with a dollar  
    //sign will override the global node style properties.  
    onBeforePlotNode: function(node){  
        //add some color to the nodes in the path between the  
        //root node and the selected node.  
        if (node.selected) {  
            node.data.$color = "#ff7";  
        }  
        else {  
            delete node.data.$color;  
            //if the node belongs to the last plotted level  
            if(!node.anySubnode("exist")) {  
                //count children number  
                var count = 0;  
                node.eachSubnode(function(n) { count++; });  
                //assign a node color based on  
                //how many children it has  
                if (count == 0) {
                    node.data.$color = "#eaead5";
                } else if (count > 9) {
                    node.data.$color = "#76763a";
                } else if (count < 10) {
                    node.data.$color = "#949449";
                } else if (count < 8) {
                    node.data.$color = "#b1b163";
                } else if (count < 6) {
                    node.data.$color = "#c7c78d";
                } else if (count < 3) {
                    node.data.$color = "#dbdbb7";
                }                
            }  
        }  
    },  
      
    //This method is called right before plotting  
    //an edge. It's useful for changing an individual edge  
    //style properties before plotting it.  
    //Edge data proprties prefixed with a dollar sign will  
    //override the Edge global style properties.  
    onBeforePlotLine: function(adj){  
        if (adj.nodeFrom.selected && adj.nodeTo.selected) {  
            adj.data.$color = "#eed";  
            adj.data.$lineWidth = 3;  
        }  
        else {  
            delete adj.data.$color;  
            delete adj.data.$lineWidth;  
        }  
    }  
});  
//load json data  
st.loadJSON(json);  
//compute node positions and layout  
st.compute();  
//optional: make a translation of the tree  
//st.geom.translate(new $jit.Complex(-200, 0), "current");  
//emulate a click on the root node.  
st.onClick(st.root);  
});
</script>
</#escape>

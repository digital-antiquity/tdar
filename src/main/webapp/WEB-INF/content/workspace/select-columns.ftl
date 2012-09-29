<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<head>
 <style type="text/css">

.fixed {
    position: fixed;
    top: 0;
    z-index:1000;
    width: 922px !important;
    border: 1px solid #AAA;
    background-color: #DEDEDE;
    padding: 4px;
    opacity:.9;
}

.status {
  color:#660033;
  font-weight:bold;
}
#drplist {border:1px solid #ccc;}
</style>

</head>
 
<body>
    <@s.form name='selectDTColForm' method='post' action='filter'>

<h3>Data Integration</h3>
<div class="glide">
Drag columns from your selected data tables onto the integration table .
</div>
<div class="glide">
<h3>Create your Integration Table:</h3>

<#macro setupIntegrationColumn column idx=0>
<td colNum="${idx}" class="<#if column.displayColumn>displayColumn<#else>integrationColumn</#if>">
  <div class="label">Column ${idx + 1} <span class="colType"></span>
  <input type="hidden" name="integrationColumns[${idx}].columnType" value="<#if column.displayColumn>DISPLAY<#else>INTEGRATION</#if>" class="colTypeField"/>
  <input type="hidden" name="integrationColumns[${idx}].sequenceNumber" value="${idx}" class="sequenceNumber" />
</div>
<#if column.columns.empty><span class="info">Drag variables from below into this column to setup your integration<br/><br/><br/><br/></span></#if>
<#list column.columns as col>
  <input type="hidden" name="integrationColumns[${idx}].columns[${col_index}].id" value="${col.id?c}" />
  
</#list>
</td>

</#macro>

<div id="fixedList">
<h4>Each Column Below will be a Column In Excel</h4>
<table id="drplist" width="100%">
<tr>
<#if integrationColumns?? && !integrationColumns.empty >
 <#list integrationColumns as integrationColumn>
   <@setupIntegrationColumn integrationColumn integrationColumn_index />
 </#list>
<#else>
  <@setupIntegrationColumn blankIntegrationColumn 0 />
</#if>

</tr>
</table>
  <div class="status"></div>
<br/>
<button id="addColumn">Click to add a new Column to the integration table</button>
</div>
</div>
<div class="glide">
<h2>Select Variables</h2>
<table width="100%" class="legend">
<tr>
<td class="legend displayColumn">&nbsp;</td> <td><b>Display Variable</b></td>
<td class="legend integrationColumn">&nbsp;&nbsp;</td> <td><b>Integration Variable with mapped Ontology</b></td>
<td class="legend measurementColumn">&nbsp;&nbsp;</td> <td><b>Measurement Variable</b></td>
<td class="legend countColumn">&nbsp;&nbsp;</td> <td><b>Count Variable</b></td>
</tr>
</table>
<br/>


      <#assign numCols = 6 />
      <#list selectedDataTables as table>
      <!-- setting for error condition -->
       <input type="hidden" name="tableIds[${table_index}]" value="${table.id?c}"/>
<div >
          <h4 class='tablemenu'><span class="arrow ui-icon ui-icon-triangle-1-s"></span>${table.dataset.title}</h4>
<div class="tableContainer">      
  <table class="buttontable">
      <tbody>
        <#assign count = 0>
            <#list table.sortedDataTableColumns as column>
            <#assign description = ""/>
            <#if column?? && column.description??>
                <#assign description = column.description />
            </#if>
              <#if count % numCols == 0><tr></#if>
              <td width="${(100 / numCols)?floor }%"><div class="drg ui-corner-all" <#if column.defaultOntology??>hasOntology="${column.defaultOntology.id?c}"</#if>
              <#if column.measurementUnit??>hasMeasurement="${column.measurementUnit}"</#if> 
              title="${description?html}"
              <#if column.columnEncodingType?? && column.columnEncodingType=='COUNT'>hasCount="true"</#if> 
              table="${table.id?c}"><span class="columnName">${column.displayName} <#if column.defaultOntology??> <span class="ontology">- ${column.defaultOntology.title}</span></#if>
            <input type="hidden" name="integrationColumns[{COLNUM}].columns[{CELLNUM}].id"  value="${column.id?c}"/></span>
                <#assign count = count+1 />
             </div> </td>
              <#if count % numCols == 0></tr></#if>
            </#list>
              <#if count % numCols != 0></tr></#if>
      </tbody>
      </table>
</div>
</div>
      </#list>


<script>

$("h4").click(toggleDiv);

$( ".drg" ).draggable({
  zIndex: 2700,
  revert: true,
  revertDuration:0
});

function setStatus(msg) {
  $(".status").html(msg);
  $(".status").show();
  $(".status").fadeIn(10,true);
  
  $(".status").css("background-color","lightyellow !important");
  $(".status").css("border","1px solid red !important");
  $(".status").delay(3000).fadeOut(3000,  function () {
      $(".status").hide();
  });
}

var drpOptions = {
  drop: function(event, ui) {
    if (ui.draggable.attr("colnum")) {
      return false;
    }
  $(ui.draggable).css("z-index",100);
  var table = ui.draggable.attr("table");
  var ret = true;
    var children = $(this).children("div");
  if (children.length > 0) {
    $(children).each(function() { if ($(this).attr("table") == table) {
      msg = "you cannot add more than one variable from the same table to any column";
      setStatus(msg);
      ret = false;
    } });
  }

if (ret == false) {
  return false;
}
  
  var newChild = $("<div/>").appendTo($(this));
  newChild.attr("hasOntology",ui.draggable.attr("hasOntology"));
  newChild.attr("table",ui.draggable.attr("table"));
  $(this).find(".info").detach();
  newChild.append(ui.draggable.html());
  newChild.append("&nbsp;&nbsp;&nbsp;&nbsp;<button>X</button>");
  var colNum = $(this).attr('colNum');
  var children = $(this).find("div");
  
  newChild.find('*').each(function() {
      var elem = this;
      replaceAttribute(elem, "name", '{COLNUM}', colNum);
      // always have one DIV to start with, so subtract 2
      replaceAttribute(elem, "name", '{CELLNUM}', children.length -2);
  });

  $(newChild).attr("style","");

  validateColumn(this);  
  $(this).draggable( "destroy" );
  $(newChild).css("{}");
   
  $( newChild).children("button").button();
  
  $(this).animate({opacity:.8, borderColor:"#000000"} ,200).animate({opacity:1, borderColor:"#AAAAAA"} ,200).animate({opacity:.8, borderColor:"#000000"} ,200).animate({opacity:1, borderColor:"#AAAAAA"} ,200); 
}
};


function addVariable( ) {

}

function validateColumn(column) {
    var integrate = $(column).find("div[hasOntology]");
    var children = $(column).children("div");
    console.log("children:" + children.length);
    console.log("integrate:" + integrate.length);
   
   var ontology = -1;
   $(integrate).each(function() { 
     if (ontology == -1) {
       ontology = $(this).attr("hasOntology");
     } else if (ontology != $(this).attr("hasOntology")) {
       ontology = -1000;
     } 
   });

  if (integrate.length  == $(".buttontable").length && ontology > 0) {
    $(column).find(".colType").html(" - integration");
    $(column).find(".colTypeField").val("INTEGRATION");
    $(column).addClass("integrationColumn");
    $(column).removeClass("displayColumn");
  } else {
    $(column).find(".colTypeField").val("DISPLAY");
    $(column).find(".colType").html(" - display");
    $(column).removeClass("integrationColumn");
    $(column).addClass("displayColumn");
  }
}
var msg = "";

jQuery(document).ready(function($){

//$('#filter').submit(function() {
//   jQuery.history.load("savedstate");
//   FIXME: COOKIE PLUGIN DOES NOT HANDLE DATA LARGER THAN COOKIE SIZES
//   $.cookie('integrationdata',$("#drplist").html());
//   return true;
//});

/*
//    $.history.init(function(hash){
//        if(hash == "savedstate" && $.cookie('integrationdata') != '') {
//           $("#drplist").html($.cookie('integrationdata'));
//        } else {
//           $.cookie('integrationdata','');
//        }
//    },
//    { unescape: ",/" });
*/


$( "#drplist td" ).droppable( drpOptions );


/*  this is the column adjustment UI, mouseenter is not always right  */
function expandColumn(col) {
  $(col).animate({width: "50%"});
  $(col).removeClass("short");
  $(col).siblings().each(function() {
    $(this).addClass("short");
    var tds = $("#drplist td").length;
    var small = 80 / tds ;
    if (tds > 8) {
       small = 150 / tds;
    };       
    $(this).animate({width: small + "%"});
  });

}; 

$("#drplist").delegate("td", "mouseenter", function(){
  expandColumn(this);
});

$('#drplist').delegate('button','click',function() {
    var column = $(this).parent().parent();
    $(this).parent().remove();
    validateColumn(column);
    return false;
});

$( "#addColumn" ).button().click(function() {
  var colNum = $("#drplist tr").children().length +1;
  $( "<td colNum="+(colNum -1)+" class='displayColumn'><div class='label'>Column " + colNum + "<span class='colType'></span> <input type='hidden' name='integrationColumns["+(colNum -1)+"].columnType' value='DISPLAY' class='colTypeField'/><input type='hidden' name='integrationColumns["+(colNum -1)+"].sequenceNumber' value='"+(colNum -1)+"' class='sequenceNumber'/><button class='removeColumn'>X</button></div></td>" ).droppable( drpOptions ).appendTo( "#drplist tr" );
    var chld = $("#drplist td");
    $("button.removeColumn",$(chld[chld.length -1])).button().click(function() {
    $(this).parent().parent().remove();
    return false;
  });
    expandColumn($(chld[chld.length -1]));
    return false;
});

//autosize the height of the div
$('.buttontable tr').each(function() { 
  var pheight = $(this).height(); $('.drg',this).css('height',pheight) 
});



  var top = $('#fixedList').offset().top - parseFloat($('#fixedList').css('marginTop').replace(/auto/, 0));
  $(window).scroll(function (event) {
    // what the y position of the scroll is
    var y = $(this).scrollTop();
  
    // whether that's below the form
    if (y >= top - 80) {
      // if so, ad the fixed class
      $('#fixedList').addClass('fixed');
    } else {
      // otherwise remove it
      $('#fixedList').removeClass('fixed');
    }
  });


});


</script>
<br/><br/>
        <@s.submit value='Next: filter values' id="submitbutton" />


</div>
</@s.form>
</body>
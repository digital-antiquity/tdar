<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#macro listDataTables tableList>

<script>
function goto(phase,caller) {
$(caller).parent("div").hide();
$(caller).parents("td").find("."+phase).show();

    if (phase == 'display2') {
        $($(caller).parents("td").find("."+phase)[0]).find("select").each(function(index) {
        var tableid = $(this).attr("id").substring(0,$(this).attr("id").indexOf("_"));

          if (tableid != $(caller).val()) {
            $(this).parent().hide();
          } else {
            $(this).parent().show();
          }; 
        });
    }

}

function resetRow(row) {
    $(row).find(".display1").hide();
    $(row).find(".display2").hide();
    $(row).find(".integrate").hide();
    $(row).find(".choose").show();
    $(row).find("span").each(function(index) { $(this).show(); $(this).val("") });
} 

$(document).ready(function() {
    resetRow($("#column_0_"));
    initializeRepeatRow();
});

function setIntegrate(select) {
  var parent = $(select).parents("div")[0];
  var hidden = $(parent).find("input[type=hidden]")[0];
  var newval = "";
  $(parent).find("select").each(function(index) { newval += $(this).val() + "="; });
  $(hidden).val(newval.substring(0,newval.length -1));
}

function setDisplay(select) {
  var parent = $(select).parents("div")[0];
  var hidden = $(parent).find("input[type=hidden]")[0];
  var newval = "";
  $(parent).find("select").each(function(index) { 
    // assume only one value replace ~ -> _ to fix issue with repeating rows
    if ($(this).val() != undefined && $(this).val() != "") newval = $(this).val().replace(/\~/gi,"_"); });
  $(hidden).val(newval);
}

</script>

<table border="0" class="repeatLastRow tableFormat width99percent" id="integrationTable"
 addAnother="add another integration or display column"
 callback="resetRow">
<thead>
<th></th>
<th></th>
</thead>
    <#assign row=0>
    <tr id="column_${row}_">
    <td>
    <div class="choose">
        <b>1. Choose an action below:</b><br/>
        <input type="button" name="action_${row}_" id="action_${row}_i" value="add integration column" onClick="goto('integrate',this)">
        <input type="button" name="action_${row}_" id="action_${row}_d" value="add display column" onClick="goto('display1',this)">
    </div>
    <div class="display1">
        <b>2. Choose a table to display a value from</b><br/>
         <select name="table_${row}_" onchange="goto('display2',this)">
            <option value="">Select a table</option>
            <#list tableList as table>
              <option value="${table.id?c}">${table.dataset.title}</option>
            </#list>
        </select>
    </div>
    <div class="display2">
        <b>3. Choose a value to display</b><br/>
    <input type="hidden" name="displayRules[${row}]" value="" />
            <#list tableList as table>
            <span id="${table.id?c}_${row}_d">
              <b>${table.dataset.title}</b>
              <select id="${table.id?c}_${row}_" name="display" onChange="setDisplay(this)">
                    <option value="">Select a column</option>
                    <#list table.sortedDataTableColumns as column>
                    <#-- replace _ with ~ to fix replace issue with repeating tables and row #s -->
                      <option value="${column.id?c}">${column.name} - ${column.getColumnDataType()} 
                        (<#if ! column.defaultOntology?? >None<#else>${column.getDefaultOntology().title}</#if>)
                      </option>
                    </#list>
                </select>
              </span>
            </#list>
    </div>

    <div class="integrate">
    <b>2. Integrate Columns</b>
    <input type="hidden" name="integrationRules[${row}]" value="" />
    <table>
    <#assign tableNum = 0>
    <#list tableList as table>
    <#assign tableNum = tableNum+1>
        <tr>
        <td>    
    ${table.dataset.title}
    </td><td>
      <select id="${table.name}_row" name="integrate_${tableNum}" onChange="setIntegrate(this)">
            <option value="">Select a column</option>
            <#list table.sortedDataTableColumns as column>
              <#if column.defaultOntology?? >
                <option value="${column.id?c}">${column.name} - ${column.getColumnDataType()} 
                (${column.getDefaultOntology().title})
              </option></#if>
            </#list>
        </select>
        </td></tr>
    </#list>
    </table>
    </div>
    </td>
    <td><@rlist.clearDeleteButton id="column"/></td>
    </tr></table>
</#macro>

<head>
<title>Data Integration</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@rlist.toolbar "select-columns" />
<div class="glide">
<@rlist.showControllerErrors />
<h3>Step 2: Select Columns to Integrate or Display</h3>

<#if tableIds.empty>
    Please <a href="<@s.url value='select-tables' />">select dataset tables to integrate</a> first.
<#else>
    <@s.form name='selectDTColForm' method='post' action='filter'>
        <@listDataTables selectedDataTables />
        
        
                <br/ >
                <br/ >

</div>
<div class="glide">
        <@s.submit value='Next: filter values' onclick='processForm()'/>
        
        </div>
        
   <div class="glide">
<h4>Table Reference</h4>
        
        <table border="0">
    <tr>
    <#list selectedDataTables as table>
      <input type="hidden" name="tableIds[${table_index}]" value="${table.id?c}"/>
      
        <td valign='top'>Table Name: ${table.displayName} (ID=${table.id?c})<br>
        <table class='tableFormat width99percent'>
            <thead>
            <tr>
                <th> Column name </th>
                <th> Column data type</th>
                <th> Attached taxonomy</th>
            </tr>
            </thead>
            <tbody>
            <#list table.sortedDataTableColumns as column>
                <tr>
                <td>${column.name}</td>
                <td>${column.getColumnDataType()}</td>
                <td>                (<#if ! column.defaultOntology?? >None<#else>${column.getDefaultOntology().title}</#if>)
                </td>
                </tr>
            </#list>
            </tbody>
        </table>        
    </td>
    </#list>
    </tr>
</table>
    </@s.form>
</#if>
</div>
</body>

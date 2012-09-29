<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<head>
<title>Filter Ontology Values</title>
<meta name="lastModifiedDate" content="$Date$"/>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
</head>
<style type='text/css'>
input+label {position: relative; }
</style>
<@edit.toolbar "filter-values"/>

<div class='glide'>
You can filter data values for the datasets listed below.  Only checked values mapped to an 
ontology will be reported below.  Select checkboxes next to the
values that you would like to be included or aggregated to that level.  Checkboxes are automatically checked if values are present in ALL datatables.
<br/>
Indented unchecked values are aggregated to the next higher level that is checked.
Unchecked values at the top (leftmost) level are ignored, along with  any unchecked
subdivision categories.  Values that occur in each dataset are indicated with blue
checks, absent values are indicated with red x's.
</div>


<div class="glide">
<@rlist.showControllerErrors />
<!--
<div class="parent-info">
  tDAR has automatically selected values that occur accross all datasets below.  To clear this, please click "clear all" below.
</div>-->
<#-- display links with taxonomy expanded -->
<@s.form method='post' action='display-filtered-results' id="filterForm">

<@s.hidden name='displayAttributeIds' />
<#assign totalCheckboxCount=0>
<@s.iterator value='ontologyDataFilters' var='ontologyDataFilter'>
<#if ontologyDataFilter.commonOntology??>
<table class='tableFormat width99percent zebracolors'>
<thead>
    <tr>
    <th>Ontology labels from ${ontologyDataFilter.commonOntology.title} <br/>
    (<span class="link" onclick='selectAllChildren("${ontologyDataFilter.columnIds}", true);'>Select All</span> 
    | <span class="link"onclick='selectAllChildren("${ontologyDataFilter.columnIds}", false);'>Clear All</span>)</th>
    <@s.iterator value='#ontologyDataFilter.integrationColumns' var='column'>
    <th>${column.name}<br/> <small>(${column.dataTable.dataset.title})</small></th>
    </@s.iterator>
    </tr>
</thead>
<tbody>
<@s.iterator value='#ontologyDataFilter.flattenedOntologyNodeList' var='ontologyNode' status='rowStatus'>
    <tr>
    <#assign numberOfParents=ontologyNode.numberOfParents>
    <td>
    <#list 1..numberOfParents as indentationLevel>
        &nbsp;&nbsp;&nbsp;&nbsp;
    </#list>
    <#assign checkForUser=false/>
    <@s.iterator value='#ontologyNode.columnHasValueArray' var='hasValue'>
        <#if !hasValue>
            <#assign checkForUser=false />
        </#if>
    </@s.iterator>
    
    <input type='checkbox' id='ontologyNodeCheckboxId_${ontologyDataFilter.columnIds}_${ontologyNode.index}'
    name='ontologyNodeFilterSelections[${totalCheckboxCount}]' value='${ontologyDataFilter.columnIds}_${ontologyNode.id?c}'
    <#if checkForUser>checked</#if>
     />
    <label for='ontologyNodeCheckboxId_${ontologyDataFilter.columnIds}_${ontologyNode.index}'>
    <#assign totalCheckboxCount=totalCheckboxCount+1>
    <b>${ontologyNode.displayName} <!--(${ontologyNode.index})--></b>
    </label>
    <#if ontologyNode.parent>
    &nbsp;(<span class="link" onclick='selectChildren("${ontologyDataFilter.columnIds}_${ontologyNode.index}", true);'>all</span>
    | <span class="link" onclick='selectChildren("${ontologyDataFilter.columnIds}_${ontologyNode.index}", false);'>clear</span>)
    </#if>
    </td>
    <@s.iterator value='#ontologyNode.columnHasValueArray' var='hasValue'>
    <td>
        <#if hasValue>
        <@edit.img "/images/checked.gif" />
        <#else>
        <@edit.img "/images/unchecked.gif" />
        </#if>
    </td>
    </@s.iterator>
</tr>
</@s.iterator>
</tbody>
</table>
<#else>
These columns do not share a common ontology but ontology integration has not been
fully implemented yet.  
</#if>

</@s.iterator>
</div>
<@edit.submit "Next: Apply filter" false/>

<@s.iterator value="displayRules" var="displayRule" status="ruleStatus">
  <input type="hidden" name="displayRules[${ruleStatus.index}]" value="${displayRule}" />
</@s.iterator>

<@s.iterator value="integrationRules" var="integrationRule" status="ruleStatus">
  <input type="hidden" name="integrationRules[${ruleStatus.index}]" value="${integrationRule}" />
</@s.iterator>

</@s.form>


<script type='text/javascript'>
function selectAllChildren(id, value) {
    $("input[id*='" + id + "']").prop('checked', value);
    return false;
}
function selectChildren(index, value) {
    $("input[id$='" + index + "']").prop('checked', value);
    $("input[id*='" + index + "\\.']").prop('checked', value);
    return false;
}

$("#filterForm").submit(function() {
  if ($("#filterForm :checked").length < 1) {
    alert("please select at least one variable");
    return false;
  }
});
$(applyZebraColors);
</script>

<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<title>Match column values to ontology nodes</title>
<meta name="lastModifiedDate" content="$Date$"/>

<style type='text/css'>
    .ui-autocomplete {
        max-height: 144pt;
        overflow-y: auto;
        overflow-x: hidden;
        line-height: 1em;
    }
    
    .ui-widget {font-family: courier;}
    /* IE 6 doesn't support max-height
     * we use height instead, but this forces the menu to always be this tall
     */
    * html .ui-autocomplete {
        height: 144pt;
        overflow-x: hidden;
    }

    .ui-menu .ui-menu-item {
        border: 0px;
        margin: 0px !important;
        padding: 0px !important;
    }
    
    .ui-menu-item a {
        border:1px solid transparent;
        min-height: 1em;
        margin:0px !important;
        padding:0px !important;
    }
    
    .ui-state-hover {
        
    } 
</style>

<script type='text/javascript' src='<@s.url value="/includes/jquery.FormNavigate.js"/>'></script> 

<script>
$(document).ready(function() {
    $("#mapontologyform").FormNavigate("Leaving the page will cause any unsaved data to be lost!"); 
    $("#selectColumn").unbind("change");
    applyZebraColors(); 
});


function applyLocalAutoComplete(selector, db) {
    $(selector).autocomplete({
        source: function(request,response) {
            var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
            response($.merge( $.map(db, function(item) {
                if(matcher.test(item.name)){
                    return {
                       value : item.name,
                       label : item.name,
                       id : item.id
                    }
                }
                }), $.map(ontology, function(item) {
                if(matcher.test(item.name)){
                    return {
                       value : item.name.replace(/^([\|\-\s]|&nbsp;)*/ig,""),
                       label : item.name,
                       id : item.id
                    }
                }
                })));
        },
        minLength:0,
        select: function(event, ui) {
            var input = this; //'this' points to the target element 
            //get the hidden input next to the textbox and set the id field
            var elem  = findSibling(input, 'ids');
            global_formNavigate = false;
            elem.val(ui.item.id);
            $(input).removeClass("error");
        },
        open: function(event, ui) {
                $("ul.ui-autocomplete li a").each(function(){
                  var htmlString = $(this).html().replace(/&lt;/g, '<');
                  htmlString = htmlString.replace(/&gt;/g, '>');
                  $(this).html(htmlString);
                  });
                  $("ul.ui-autocomplete").css("width",$(this).parent().width());
        },
        change: function(event, ui) {
            var input = this;  //'this' points to the target element
            //don't allow invalid selection
            //FIXME: per jquery combobox demo,  ui.item should be null when the user manually types in the input value (as opposed to selecting from dropdown).  
            //But I'm not seeing that behavior (ui.item has a value) So,  this block executes even after select() fires,  which is redundant. 

            if(true) { 
                //did they type in an exact match to an elment?
                var matcher = new RegExp( "^([\\|\\-\\s]|&nbsp;)*" + $.ui.autocomplete.escapeRegex( $(this).val() ) + "$", "i" );
                var valid = false;
                $.each(ontology, function(k,v){
                  if ($(input).val() == '') {
                      valid = true;
                      return false;
                  }
                  if(v.name.match(matcher)) {
                      valid = true;
                      findSibling(input, "ids").val(v.id);
                      return false;
                  }
                    
                });
                if(!valid) {
                    console.debug("invalid entry - clearing input box and hidden id value");
                    findSibling(input,"ids").val("");
                    $(input).val("");
                    $(input).addClass("error");
                } 
                
            }
        }
        
    });
    
    //add a button to expand the full list        
    $(selector).each(function(k,v){
        var input = $(v);
        var container = $(input).next();
        container.click(function(){
            var expanded = input.autocomplete("widget").is(":visible");
            console.debug("expanded:" + expanded);
            //close the droplist if already expanded, otherwise show the full list
            if(expanded) {
                input.autocomplete('close');
            } else {
                input.autocomplete('search', '');
                input.focus();
            }
            return false;
        });

        //FIXME: the 'text' option is being ignored, so i'm just having the content of the button be blank
        container.removeClass( "ui-corner-all" );
        container.addClass( "ui-corner-right ui-button-icon" )
    });
}

$(function() {
    $('button.ui-button').hover(
        function() {$(this).addClass("ui-state-hover");},
        function() {$(this).removeClass("ui-state-hover");}
    );
});

<#macro repeat num=0 value="  |"><#t/>
<#if ((num?number) &gt; 0)>
<#list 1..(num?number) as x>${value}</#list><#t/>
</#if>
</#macro>


// Ontologies
var ontology = [
{id:"", name:" -- All Values -- "},
<@s.iterator status="stat" var="ont" value="%{ontologyNodes}">
    {id:"${ont.id?c}",  name:"<@repeat num="${ont.numberOfParents-1}" />- ${ont.displayName?js_string}"} <#if !stat.last>,</#if>
</@s.iterator>
];


</script>




</head>
<body>
<@nav.toolbar "dataset" "column-ontology" />
<div>

<@nav.showControllerErrors/>

<div class='info glide'>
Linking columns for: <b>${dataset.title} (tDAR ID: ${dataset.id?c})</b>
<br/>
<br/>
<#if ontologyMappedColumns.size() gt 1>
There are multiple columns associated with ontologies.  After saving your changes
please remember to use the links below to select the other columns.
</#if>
<br/>
</div>
<div id='display' class="glide">
<@s.form method='post' id="mapontologyform" action='save-data-ontology-mapping'>
<@s.hidden name='resourceId' value='${resource.id?c}'/>
<@s.hidden name='columnId' value='${dataTableColumn.id?c}'/>
<#assign isLast = false/>
<#assign count = 0/>
<label>Select Column:</label>
<select id="selectColumn" name="selectColumn" onChange="window.location='?columnId='+$(this).val()">
<#assign mappingStatus= ontologyMappedColumnStatus />
<@s.iterator value='ontologyMappedColumns' var='column' status="status1">
<#assign mappingStatus_ = "" />
<#if mappingStatus[status1.index]?? && mappingStatus[status1.index] !='' >
  <#assign mappingStatus_ = "[" + mappingStatus[status1.index] + "]" />
</#if>
<#assign count=count+1 />
<option value="${column.id?c}" <#if dataTableColumn?? && column.id == dataTableColumn.id>selected
  <#if status1.last>
  <#assign isLast=true/>
  </#if>
</#if>
>${count}. ${column.displayName} -> ${column.defaultOntology.title}  &nbsp;&nbsp; ${mappingStatus_}</option>
</@s.iterator>
</select>

<table class="tableFormat width99percent zebracolors">
<thead>
<tr>
<th>Data values from <span class='highlight'>${dataTableColumn.displayName}</span></th>
<th></th>
<th>Ontology values from <span class='highlight'>${dataTableColumn.defaultOntology.title}</span></th>
</tr>
</thead>
<tbody>
<@s.iterator value='suggestions' var='suggestionEntry' status='rowStatus'>
<tr>
<td>
<@s.textfield name='dataColumnValues[${rowStatus.index}]' value="${suggestionEntry.key}" size='50' readonly=true/></td>
<td><img src="<@s.url value='/images/arrow_right.png'/>"/></td>
<td>
<script>
$(document).ready(function() {
    applyLocalAutoComplete($("#autocomp_${rowStatus.index}"),suggestionsFor_${rowStatus.index});
});
var suggestionsFor_${rowStatus.index} = [
{id:"", name:""}<#t>
<@s.iterator status="stat" var="ont" value="#suggestionEntry.value">
    <#if stat.first>,{id:"", name:" -- Suggested Values --"}</#if>
    ,{id:"${ont.id?c}",  name:"${ont.displayName?js_string}"}
</@s.iterator>
];
</script>
<@s.textfield name="ontologyNodeNames[${rowStatus.index}]" id="autocomp_${rowStatus.index}" cssClass="manualAutocomplete"/>
        <button title="Expand entire list" tabindex="-1" class="ui-button ui-widget ui-state-default ui-button-icon-only ui-corner-right ui-button-icon" role="button" aria-disabled="false" style="height:18px; position:relative;top:4px;left:-5px;clear:none;">
            <span class="ui-button-icon-primary ui-icon ui-icon-triangle-1-s"></span></button>
<@s.hidden name="ontologyNodeIds[${rowStatus.index}]" />

</td>
</tr>
</@s.iterator>
</tbody>
</table>
</div>
<#assign msg="Save mappings and go to next column"/>
<#if isLast>
  <#assign msg="Save mappings for last column"/>
</#if>
<@s.submit value='${msg}' align='left' />
</@s.form>
</div>

</body>

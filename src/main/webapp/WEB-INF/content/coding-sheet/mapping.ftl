<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
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
    
    .smallauto .down-arrow {
left: -28px !important;
//    left: 289px !important;
	top: 13px !important;  
    }
</style>

<script type="text/javascript">
function autosuggest() {
    $(".manualAutocomplete").each(function() {
        var $element = $(this);
        var json = eval($element.attr("id") + "Suggestions");
        if (json.length == 3 && $element.val() == "") {
            var $idElement = $($element.attr("autocompleteIdElement"));
            $element.val(json[2].name);
            $idElement.val(json[2].id);
        }

    });
}

function clearall() {
    $(".manualAutocomplete").each(function() {
        var $element = $(this);
        var $idElement = $($element.attr("autocompleteIdElement"));
        $element.val("");
        $idElement.val("");
    });
}

$(document).ready(function() {
    $("#autosuggest").click(autosuggest);
    $("#clearAll").click(clearall);
});

$(document).ready(function() {
    $("#mapontologyform").FormNavigate("Leaving the page will cause any unsaved data to be lost!");
    $("#selectColumn").unbind("change");
    applyZebraColors();
});

function applyLocalAutoComplete(selector, db) {

    $(selector).autocomplete({
        source : function(request, response) {
            //var timer = new Timer();
            var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
            var allMatchedItems = $.merge($.map(db, function(item) {
                if (matcher.test(item.name)) {
                    return {
                        value : item.name,
                        label : item.name,
                        id : item.id
                    };
                }
            }), $.map(ontology, function(item) {
                if (matcher.test(item.name)) {
                    return {
                        value : item.name.replace(/^([\|\-\s])*/ig, ""),
                        label : item.name,
                        id : item.id
                    };
                }
            }));
            //console.log("%s\t autocomplete.source::\t suggestiondb.size:%s\t ontology.size:%s", timer.current(), db.length, ontology.length);
            response(allMatchedItems);
            //timer.stop();
        },
        minLength : 0,
        select : function(event, ui) {
            var $input = $(this); //'this' points to the target element 
            //get the hidden input next to the textbox and set the id field
            var $idElement = $($input.attr("autocompleteIdElement"));
            global_formNavigate = false;
            $idElement.val(ui.item.id);
            $input.removeClass("error");
        },
        open : function(event, ui) {
            $("ul.ui-autocomplete").css("width", $(this).parent().width());
        },
        change : function(event, ui) {
        }
    });

    //not to be confused with autocomplete 'change' option,  which is actually a custom 'autocompletechange' event
    //we assume this fires only when you change the textbox and not when via selecting an item from the autocomplete list
    $(selector).change(function() {

        var input = this; //'this' is the text input element
        var $input = $(input);

        //if the textbox is blank,  clear the hidden id field
        if ($.trim(input.value) == '') {
            var $idElement = $($input.attr("autocompleteIdElement"));
            $idElement.val("");
            return;
        }

        //don't allow invalid selection
        if (true) {
            //did they type in an exact match to an elment?
            var matcher = new RegExp("^([\\|\\-\\s]|&nbsp;)*" + $.ui.autocomplete.escapeRegex(input.value) + "$", "i");
            var valid = false;

            //troll through onto ontologies until we find one that matches the value of the input element
            $.each(ontology, function(k, v) {
                if (v.name.match(matcher)) {
                    valid = true;
                    var $idElement = $($input.attr("autocompleteIdElement"));
                    $idElement.val(v.id);
                    return false;
                }
            });

            if (!valid) {
                console.debug("invalid entry - clearing input box and hidden id value");
                var $idElement = $($input.attr("autocompleteIdElement"));
                $idElement.val("");
                $input.addClass("error");
            }

        }
    });

    //add a button to expand the full list        
    $(selector).each(function(k, v) {
        var input = $(v);
        var container = $(input).siblings("button").first();
        container.click(function() {
            var expanded = input.autocomplete("widget").is(":visible");
            console.debug("expanded:" + expanded);
            //close the droplist if already expanded, otherwise show the full list
            if (expanded) {
                input.autocomplete('close');
            } else {
                input.autocomplete('search', '');
                input.focus();
            }
            return false;
        });

        //FIXME: the 'text' option is being ignored, so i'm just having the content of the button be blank
        container.removeClass("ui-corner-all");
        container.addClass("ui-corner-right ui-button-icon");
    });
}

$(function() {
    $('button.ui-button').hover(function() {
        $(this).addClass("ui-state-hover");
    }, function() {
        $(this).removeClass("ui-state-hover");
    });

    $(".down-arrow", "#mapontologyform").click(autocompleteShowAll);

});
</script>


<script type="text/javascript">

<#macro repeat num=0 value="  |"><#t/>
<#if ((num?number) &gt; 0)>
<#list 1..(num?number) as x>${value}</#list><#t/>
</#if>
</#macro>


// Ontologies
var ontology = [
{id:"", name:" -- All Values -- "},
<#list ontologyNodes as ont>
   <#if (ont_index > 0 )>,</#if> {id:"${ont.id?c}",  name:"<@repeat num="${ont.numberOfParents-1}" />- <#noescape>${ont.displayName?js_string}</#noescape>"} 
</#list>
];

</script>




</head>
<body>
<@nav.toolbar "coding-sheet" "mapping" />
<div>


<div id='display' class="glide">
<@s.form method='post' id="mapontologyform" action='save-mapping'>
<@s.hidden name='id' value='${resource.id?c}'/>
<#assign isLast = false/>
<#assign count = 0/>

<table class="tableFormat width99percent zebracolors">
<thead>
<tr>
<th>Coding Rules from <span class='highlight'>${codingSheet.title}</span></th>
<th></th>
<th>Ontology values from <span class='highlight'>${codingSheet.defaultOntology.title}</span></th>
</tr>
</thead>
<tbody>
<tr>
	<td>
		<button type="button" id="autosuggest">Autosuggest Mappings</button>
	</td><td>
	</td><td>
		<button type="button" id="clearAll">Clear all</button>
	</td>
</tr>
<#list codingRules as rule>
<tr>
<td>
	<@s.hidden name='codingRules[${rule_index}].id' />
	<@s.textfield name='codingRules[${rule_index}].term' size='50' readonly=true/>
</td>
<td><img src="<@s.url value='/images/arrow_right.png'/>"/></td>
<td class="smallauto">
<script type="text/javascript">
$(document).ready(function() {
    applyLocalAutoComplete($("#autocomp_${rule_index}"),autocomp_${rule_index}Suggestions);
});
<#noescape>
var autocomp_${rule_index}Suggestions = [
{id:"", name:""}<#t>
	<#list rule.suggestions as suggestion>
    <#if suggestion_index == 0>,{id:"", name:" -- Suggested Values --"}</#if>
    ,{id:"${suggestion.id?c}",  name:"${suggestion.displayName?js_string}"}
    </#list>
];
</#noescape>
</script>
<@s.hidden name="codingRules[${rule_index}].ontologyNode.id" id="ontologyNodeId_${rule_index}" />
<@s.textfield name="codingRules[${rule_index}].ontologyNode.displayName" id="autocomp_${rule_index}"
	 cssClass="manualAutocomplete" autocompleteIdElement="#ontologyNodeId_${rule_index}"/>
<div class="down-arrow"></div>

</td>
</tr>
</#list>
</tbody>
</table>
</div>

	<@edit.submit "Save" false><br/>
	</@edit.submit>
</@s.form>
</div>


</body>
</#escape>
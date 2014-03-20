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
    
</style>

</head>
<body>

<@nav.toolbar "coding-sheet" "mapping" />


<h2>Map Codes to Ontology Values</h2>
<div id='display' class="">
    <@s.form method='post' id="mapontologyform" action='save-mapping'>
    <@s.hidden name='id' value='${resource.id?c}'/>
    <#assign isLast = false/>
    <#assign count = 0/>


    <div class="btn-group">
        <button class="btn" type="button" id="autosuggest">Autosuggest Mappings</button>
        <button class="btn" type="button" id="clearAll">Clear all</button>
    </div>
    
    <div class="control-group">
        <label class="control-label">Mappings</label>
        <#list codingRules as rule>
        <div class="controls controls-row mappingPair">
                <@s.hidden name='codingRules[${rule_index?c}].id' />
                <@s.textfield theme="simple" name='codingRules[${rule_index?c}].term' size='50' readonly=true cssClass="span4 codingSheetTerm"/>
        
            <div class="span1">
                <img src="<@s.url value='/images/arrow_right.png' />" alt="right arrow"/>
            </div>
        
            <div>
                <script type="text/javascript">
                $(document).ready(function() {
                    applyLocalAutoComplete($("#autocomp_${rule_index?c}"),autocomp_${rule_index?c}Suggestions);
                });
                <#noescape>
                var autocomp_${rule_index?c}Suggestions = [
                {id:"", name:""}<#t>
                    <#list rule.suggestions as suggestion>
                    <#if suggestion_index == 0>,{id:"", name:" -- Suggested Values --"}</#if>
                    ,{id:"${suggestion.id?c}",  name:"${suggestion.displayName?js_string}"}
                    </#list>
                ];
                </#noescape>
                </script>
                <@s.hidden name="codingRules[${rule_index?c}].ontologyNode.id" id="ontologyNodeId_${rule_index?c}" />
                <div class="input-append">
                    <@s.textfield theme="simple" name="codingRules[${rule_index?c}].ontologyNode.displayName" id="autocomp_${rule_index?c}"
                         cssClass="manualAutocomplete ontologyValue span4" autocompleteIdElement="#ontologyNodeId_${rule_index?c}"/>
                        <button type="button" class="btn show-all"><i class="icon-chevron-down"></i></button>                    
                </div>
            </div>
        </div>    
        </#list>
    </div>

    <@edit.submit "Save" false />
</@s.form>
</div>

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

}

$(document).ready(function() {
    $("#autosuggest").click(autosuggest);
    $("#clearAll").click(clearall);
    $("#mapontologyform").FormNavigate({message:"Leaving the page will cause any unsaved data to be lost!"});
    $("#selectColumn").unbind("change");
    $('button.ui-button').hover(function() {
        $(this).addClass("ui-state-hover");
    }, function() {
        $(this).removeClass("ui-state-hover");
    });

    $(".show-all").click(function() {
        var $button = $(this);
        var $div = $button.closest('.input-append');
        var $textfield = $div.find("input[type=text]");
        var $widget = $textfield.autocomplete("widget");
        $textfield.focus().autocomplete("search", "");
    });

});

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
</body>
</#escape>
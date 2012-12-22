var drpOptions = {
        drop : dropVariable
};

function initDataIntegration() {
    $("#selectDTColForm").submit(function() {
        var $this = $(this);
        // copy values into data attributes before doing this; then copy back
        $("option:selected", $this).attr("selected", "selected");
        $(":checked", $this).attr("checked", "checked");
        $("input", $this).filter('[type=text],[type=hidden]').each(function() {
            $(this).attr("value", $(this).val());
        });
        $("#autosave").val($this.html());
    });

    if ($("#autosave").val() != '') {
        $("#selectDTColForm").html($("#autosave").val());
    }


//    $("h4").click(toggleDiv);

    $(".drg").draggable({
        zIndex : 2700,
        revert : true,
        revertDuration : 0
    });

    $("#drplist td").droppable(drpOptions);

    $("#drplist").delegate("td", "mouseenter", function() {
        expandColumn(this);
    });

    $('#drplist').delegate('button', 'click', function() {
        var column = $(this).parent().parent();
        $(this).parent().remove();
        validateColumn(column);
        return false;
    });

    $("#clear").click(integrationClearAll);
    $("#autoselect").click(integrationAutoselect);
    $("#addColumn").click(addColumn);
    // autosize the height of the div
    $('.buttontable tr').each(function() {
        var pheight = $(this).height();
        $('.drg', this).css('height', pheight);
    });

    var top = $('#fixedList').offset().top
            - parseFloat($('#fixedList').css('marginTop').replace(/auto/, 0));
    $(window).scroll(function(event) {
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

};

function setStatus(msg) {
    $(".status").html(msg);
    $(".status").show();
    $(".status").fadeIn(10, true);

    $(".status").css("background-color", "lightyellow !important");
    $(".status").css("border", "1px solid red !important");
    $(".status").delay(3000).fadeOut(3000, function() {
        $(".status").hide();
    });
}

function validateColumn(column) {
    var integrate = $(column).find("div[hasOntology]");
    var children = $(column).children("div");
    console.log("children:" + children.length);
    console.log("integrate:" + integrate.length);

    var ontology = -1;
    var ontologyName = "";
    $(integrate).each(function() {
        if (ontology == -1) {
            ontology = $(this).attr("hasOntology");
            ontologyName = $(".ontology", $(this)).html();
        } else if (ontology != $(this).attr("hasOntology")) {
            ontology = -1000;
        }
    });

    if (integrate.length == $(".buttontable").length && ontology > 0) {
        ontologyName = ontologyName.substr(2);
        $(column).find(".colType").html(": integration <span class=\"ontology\">("+ontologyName+")</span>");
        $(column).find(".colTypeField").val("INTEGRATION");
        $(column).addClass("integrationColumn");
        $(column).removeClass("displayColumn");
    } else {
        $(column).find(".colTypeField").val("DISPLAY");
        $(column).find(".colType").html(": display");
        $(column).removeClass("integrationColumn");
        $(column).addClass("displayColumn");
    }
}
var msg = "";

function dropVariable(event, ui) {
    var $target = $(event.target);
    var draggable = ui.draggable;
    if (draggable.attr("colnum")) {
        return false;
    }
    $(draggable).css("z-index", 100);
    var table = draggable.attr("table");
    var ret = true;
    var children = $target.children("div [table]");
    console.log(draggable);
    console.log(table);
    console.log(children);
    if (children.length > 0) {
        $(children)
                .each(
                        function() {
                            console.log($(this));
                            if ($(this).attr("table") == table) {
                                msg = "you cannot add more than one variable from the same table to any column";
                                setStatus(msg);
                                ret = false;
                            }
                        });
    }

    if (ret == false) {
        return false;
    }

    var newChild = $("<div/>").appendTo($target);
    newChild.attr("hasOntology", draggable.attr("hasOntology"));
    newChild.attr("table", draggable.attr("table"));
    $target.find(".info").detach();
    newChild.append(draggable.html());
    newChild.append("&nbsp;&nbsp;&nbsp;&nbsp;<button>X</button>");
    var colNum = $target.attr('colNum');
    var children = $target.find("div");

    newChild.find('*').each(function() {
        var elem = this;
        replaceAttribute(elem, "name", '{COLNUM}', colNum);
        // always have one DIV to start with, so subtract 2
        replaceAttribute(elem, "name", '{CELLNUM}', children.length - 2);
    });

    $(newChild).attr("style", "");

    validateColumn(event.target);
    $target.draggable("destroy");
    $(newChild).css("{}");

    $(newChild).children("button").button();

    $target.animate({
        opacity : .8,
        borderColor : "#000000"
    }, 200).animate({
        opacity : 1,
        borderColor : "#AAAAAA"
    }, 200).animate({
        opacity : .8,
        borderColor : "#000000"
    }, 200).animate({
        opacity : 1,
        borderColor : "#AAAAAA"
    }, 200);
}

/* this is the column adjustment UI, mouseenter is not always right */
function expandColumn(col) {
    var $col = $(col);
    var $tds = $("#drplist td");
    var small = 80 / $tds.length;
	$tds.stop(true,true);
    if ($tds.length > 8) {
        small = 150 / $tds.length;
    }
	$tds.removeClass("short");
	$tds.addClass("short");
	$tds.css({
            width : small + "%"
    });


	$col.stop(true,true).animate({
        width : "50%"
    }).removeClass("short");
};

function addColumn(matches) {
    var colNum = $("#drplist tr").children().length + 1;
    $(
            "<td colNum="
                    + (colNum - 1)
                    + " class='displayColumn'><div class='label'>Column "
                    + colNum
                    + "<span class='colType'></span> <input type='hidden' name='integrationColumns["
                    + (colNum - 1)
                    + "].columnType' value='DISPLAY' class='colTypeField'/><input type='hidden' name='integrationColumns["
                    + (colNum - 1)
                    + "].sequenceNumber' value='"
                    + (colNum - 1)
                    + "' class='sequenceNumber'/><button class='removeColumn'>X</button></div></td>")
            .droppable(drpOptions).appendTo("#drplist tr");
    var $chld = $("#drplist td:last");
    $("button.removeColumn", $chld).button().click(function() {
        $(this).parent().parent().remove();
        return false;
    });
    expandColumn($chld);
    if (matches != undefined && matches.length > 0) {
        var event = {};
        event.target = $chld;
        var tables = $("table.buttontable");
        for ( var i = 0; i < tables.length; i++) {
            // fake the drop function
            var table = tables[i];
            var ui = {};
            ui.draggable = $($("[hasontology=" + matches + "]", $(table))[0]);
            dropVariable(event, ui);
        }
    }
    return false;
};

function integrationClearAll() {
    $("#drplist tbody td")
            .each(
                    function() {
                        var $this = $(this);
                        if ($this.attr("colnum") == 0) {
                            $this.empty();
                            $this
                                    .html(
                                            '<div class="label">Column 1 <span class="colType"></span><input type="hidden" name="integrationColumns[0].columnType" value="DISPLAY" class="colTypeField"/><input type="hidden" name="integrationColumns[0].sequenceNumber" value="0" class="sequenceNumber" /></div><span class="info">Drag variables from below into this column to setup your integration<br/><br/><br/><br/></span>');
                            $this.removeClass("integrationColumn");
                            $this.addClass("displayColumn");
                        } else {
                            $this.remove();
                        }
                    });
    setTimeout(function() {
        $("#clear").attr('checked', false);
    }, 400);
}

function integrationAutoselect() {
    var matches = {};
    var tables = [];
    var totalTables = 0;
    $("[hasontology]").each(function() {
        var ont = $(this).attr('hasontology');
        var table = $(this).attr('table');
        if (matches[ont] == undefined) {
            matches[ont] = [];
        }
        if (-1 == $.inArray(parseInt(table), matches[ont])) {
            matches[ont][matches[ont].length] = parseInt(table);
        }
        // map of ontologyIds -> [unique tableId list]
        if (-1 == $.inArray(parseInt(table), tables)) {
            tables[tables.length] = parseInt(table);
            totalTables++;
        }
    });


    var okay = false;
    for (match in matches) {
        if (matches.hasOwnProperty(match)) {
            console.log(match);
            if (matches[match].length == totalTables) {
                if (!okay) {
                    $("#drplist td").remove();
                }
                okay = true;
                addColumn(match);
            }
        }
    }
    if (!okay) {
        setStatus("no shared integration columns were found.");    
    }

    setTimeout(function() {
        $("#autoselect").attr('checked', false);
    }, 400);
}

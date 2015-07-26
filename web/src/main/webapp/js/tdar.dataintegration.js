(function (TDAR, $) {
    'use strict';

    //HACK jtd - jqueryui relies on $.browser. jQuery 1.10.3 deprecates that.  So,  we add it here w/
    //bogus value
    $.browser = "NCSA Mosaic v0.7";

    var drpOptions = {
        drop: _dropVariable
    };

    //current status message
    var msg = "";

    /**
     * Initialize the data integration UI
     */
    var _initDataIntegration = function () {
        $("#selectDTColForm").submit(function () {
            var $this = $(this);
            // copy values into data attributes before doing this; then copy back
            $("option:selected", $this).attr("selected", "selected");
            $(":checked", $this).attr("checked", "checked");
            $("input", $this).filter('[type=text],[type=hidden]').each(function () {
                $(this).attr("value", $(this).val());
            });
            $("#autosave").val($this.html());
        });

        if ($("#autosave").val() != '') {
            $("#selectDTColForm").html($("#autosave").val());
        }

        _resetDraggable();

        $("#drplist td").droppable(drpOptions);

        $("#drplist").delegate("td", "mouseenter", function () {
            _expandColumn(this);
        });

        $('#drplist').delegate('button', 'click', function () {
            var column = $(this).parent().parent();
            $(this).parent().remove();
            _validateColumn(column);
            return false;
        });

        $("#clear").click(_integrationClearAll);
        $("#autoselect").click(_integrationAutoselect);
        $("#addColumn").click(_addColumn);
        // autosize the height of the div
        $('.buttontable tr').each(function () {
            var pheight = $(this).height();
            $('.drg', this).css('height', pheight);
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

        // Perform cleanup/validation on form submit
        $("#selectDTColForm").submit(function (event) {
            //remove any empty display/integration columns upon submit
            $("#drplist").find("td").each(function(idx, td){
                var $td = $(td);
                console.log(td);
                //we assume an empty column has only one div (with class .label)
                if($td.find(">div:not(.label)").length === 0) {
                    $td.remove();
                }
            });

            //must have at least one integration column
            var isValid = $(".integrationColumn", $("#drplist")).size() > 0;
            if(!isValid) {
                console.warn("Must have at least one integration column selected");
                $('#columnSave').modal({
                    keyboard: false
                });
            }
            return isValid;
        });

        $("#modalHide").click(function (event) {
            $('#columnSave').modal('hide');
        });
    };

    /**
     * Set the current integration status message
     * @param msg
     *
     */
    function _setStatus(msg) {
        $(".status").html(msg)
            .show()
            .css({"background-color": "lightyellow !important",
                    "border": "1px solid red !important"})
                .delay(3000)
                .fadeOut(3000, function () {
                    $(".status").hide();
        });
    }

    /**
     * Inspect contents of integration table column, determine if it should be displayed as "integration" column
     * or "display" column.
     *
     * @param column
     * @private
     */
    function _validateColumn(column) {
        var integrate = $(column).find("div[data-ontology]");
        //console.log(column);
        //console.log(integrate);
        var children = $(column).children("div");
        //console.log("children:" + children.length);
        //console.log("integrate:" + integrate.length);

        var ontology = -1;
        var ontologyName = "";
        $(integrate).each(function () {
            if (ontology == -1) {
                ontology = $(this).data("ontology");
                ontologyName = $(".ontology", $(this)).html();
            } else if (ontology != $(this).data("ontology")) {
                ontology = -1000;
            }
        });

        if (integrate.length == $(".buttontable").length && ontology > 0) {
            ontologyName = ontologyName.substr(2);
            $(column).find(".colType").html(": integration <span class=\"ontology\">(" + ontologyName + ")</span>");
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

    /**
     * Event handler, called when a source dataset column is  dropped on the integration table.  Determine if the placement of
     * the source column placement was valid.  If not, update current status with error message.  If valid,  determine
     * whether to display the integration table column as an "integration" or "display" column.
     *
     * @param event
     * @param ui
     * @returns {boolean} true if drop should be accepted. otherwise false.
     * @private
     */
    function _dropVariable(event, ui) {
        var $target = $(event.target);
        var draggable = ui.draggable;
        if (draggable.data("colnum")) {
            return false;
        }
        $(draggable).css("z-index", 100);
        var table = draggable.data("table");
        var ret = true;
        var children = $target.find("div [data-table]");
        //console.log(draggable);
        //console.log(table);
        //console.log(children);
        if (children.length > 0) {
            $(children).each(function () {
                        //console.log($(this));
                        if ($(this).data("table") == table) {
                            msg = "you cannot add more than one variable from the same table to any column";
                            _setStatus(msg);
                            ret = false;
                        }
                    });
        }

        if (!ret) {
            return false;
        }

        var newChild = $("<div/>").appendTo($target);
        newChild.data("ontology", draggable.data("ontology"));
        newChild.data("table", draggable.data("table"));
        $target.find(".info").detach();
        draggable.clone(true, true).appendTo(newChild);
        newChild.append("&nbsp;&nbsp;&nbsp;&nbsp;<button>X</button>");
        var colNum = $target.data('colnum');
        var children = $target.find("div");

        newChild.find('*').each(function () {
            var elem = this;    
            _replaceAttribute(elem, "name", '{COLNUM}', colNum);
            // always have one DIV to start with, so subtract 2
            _replaceAttribute(elem, "name", '{CELLNUM}', children.length - 2);
        });

        $(newChild).children().removeAttr("style");

        _validateColumn(event.target);
        $target.draggable("destroy");
        $(newChild).css("{}");

        $(newChild).children("button").button();

        $target.animate({
            opacity: .8,
            borderColor: "#000000"
        }, 200).animate({
            opacity: 1,
            borderColor: "#AAAAAA"
        }, 200).animate({
            opacity: .8,
            borderColor: "#000000"
        }, 200).animate({
            opacity: 1,
            borderColor: "#AAAAAA"
        }, 200);
    }

    /**
     * this is the column adjustment UI, mouseenter is not always right
     * @param col integration table column to expand
     * @private
     */
    function _expandColumn(col) {
        var $col = $(col);
        var $tds = $("#drplist td");
        var small = 80 / $tds.length;
        $tds.stop(true, true);
        if ($tds.length > 8) {
            small = 150 / $tds.length;
        }
        $tds.removeClass("short");
        $tds.addClass("short");
        $tds.css({
            width: small + "%"
        });

        $col.stop(true, true).animate({
            width: "50%"
        }).removeClass("short");
    };

    /**
     * Add a new column to the Integration Table section.
     *
     * @param strOntologyId if nonblank, indicates ontology associated with the new column
     * @returns {boolean} false, sometimes.
     * @private
     */
    function _addColumn(strOntologyId) {

        var cols = $("#drplist tr td");
        //console.log(cols);
        //console.log(cols.last());

        //console.log($(".drg", cols.last()).size());

        if ($(".drg", cols.last()).size() == 0 && parseInt(strOntologyId) > 0) {
            cols.last().remove();
        }

        var colNum = $("#drplist td").size() + 1;
        var remove = "<button class='removeColumn'>X</button>";
        if (colNum == 1) {
            remove = "";
        }
        $("<td data-colnum=" + (colNum - 1) + " class='displayColumn'><div class='label'>Column " + colNum + "<span class='colType'></span> <input type='hidden' name='integrationColumns[" + (colNum - 1) + "].columnType' value='DISPLAY' class='colTypeField'/><input type='hidden' name='integrationColumns[" + (colNum - 1) + "].sequenceNumber' value='" + (colNum - 1) + "' class='sequenceNumber'/>" + remove + "</div></td>").droppable(drpOptions).appendTo("#drplist tr");
        var $chld = $("#drplist td:last");
        $("button.removeColumn", $chld).button().click(function () {
            $(this).parent().parent().remove();
            if ($("#drplist td").size() == 0) {
                _addColumn();
            }
            return false;
        });
        _expandColumn($chld);
        if (strOntologyId != undefined && strOntologyId.length > 0) {
            var event = {};
            event.target = $chld;
            var tables = $("table.buttontable");
            for (var i = 0; i < tables.length; i++) {
                // fake the drop function
                var table = tables[i];
                var ui = {};
                ui.draggable = $($("[data-ontology=" + strOntologyId + "]", $(table))[0]);
                _dropVariable(event, ui);
            }
        }
        return false;
    };

    /**
     * Clear all columns in Integration Table section
     */
    function _integrationClearAll() {
        $("#drplist tbody td").each(function () {
                var $this = $(this);
                if ($this.data("colnum") == 0) {
                    $this.empty();
                    $this.html('<div class="label">Column 1 <span class="colType"></span><input type="hidden" name="integrationColumns[0].columnType" value="DISPLAY" class="colTypeField"/><input type="hidden" name="integrationColumns[0].sequenceNumber" value="0" class="sequenceNumber" /></div><span class="info">Drag variables from below into this column to setup your integration<br/><br/><br/><br/></span>');
                    $this.removeClass("integrationColumn");
                    $this.addClass("displayColumn");
                } else {
                    $this.remove();
                }
            });
        _resetDraggable();
    }

    /**
     * Add all integrateable columns to the Integration Table
     * @private
     */
    function _integrationAutoselect() {
        var matches = {};
        var tables = [];
        var totalTables = 0;
        var okay = false;

        $("[data-ontology]").each(function () {
            var ont = $(this).data('ontology');
            var table = $(this).data('table');
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

        for (var match in matches) {
            if (matches.hasOwnProperty(match)) {
                //console.log(match);
                if (matches[match].length == totalTables) {
                    if (!okay) {
                        $("#drplist td").remove();
                    }
                    okay = true;
                    _addColumn(match);
                }
            }
        }
        if (!okay) {
            _setStatus("no shared integration columns were found.");
        }

        //you know, rumors tell of an html element that implements this.. button-like behavior.
    }

    /**
     * Return body of a function as a string.
     * @param func
     * @returns {*}
     * @private
     */
    var _getFunctionBody = function (func) {
        var m = func.toString().match(/\{([\s\S]*)\}/m)[1];
        return m;
    }

    /**
     * replace last occurance of str in attribute with rep
     *
     * @param elem
     * @param attrName
     * @param str
     * @param rep
     * @private
     */
    function _replaceAttribute(elem, attrName, str, rep) {
        if (!$(elem).attr(attrName)) {
            return;
        }
        var oldval = $(elem).attr(attrName);
        if (typeof oldval === "function") {
            oldval = _getFunctionBody(oldval);
            // console.debug("converting function to string:" + oldval );

        }
        if (oldval.indexOf(str) != -1) {
            var beginPart = oldval.substring(0, oldval.lastIndexOf(str));
            var endPart = oldval.substring(oldval.lastIndexOf(str) + str.length, oldval.length);
            var newval = beginPart + rep + endPart;
            $(elem).attr(attrName, newval);
            // console.debug('attr:' + attrName + ' oldval:' + oldval + ' newval:' +
            // newval);
        }
    }

    /**
     * Ontology Filter: Select All Children
     * @param id
     * @param value
     * @returns {boolean}
     */
    function _selectAllChildren(id, value) {
        var prefix = id.substr(0, id.lastIndexOf("_"));
        $("input:enabled[id*='" + prefix + "']").prop('checked', value);
        return false;
    }

    /**
     * Ontology Filter: Select Children
     * @param id
     * @param value
     * @returns {boolean}
     */
    function _selectChildren(id, value) {
        var index = id.substr(0, id.lastIndexOf("_"));
        $("input:enabled[id$='" + index + "']").prop('checked', value);
        $("input:enabled[id*='" + index + "_']").prop('checked', value);
        return false;
    }

    //return serialized list of checked checkboxes (caution: we do not escape css reserved characters (e.g period/tilde)
    function cb2str() {
        var checkboxes = $("[id]:checkbox:checked").get();
        var labelNames = [], elems = [];
        var output ="";
        $.each(checkboxes, function (i, el) {
            elems.push(el.id);
            labelNames.push($(el).parent().find(".nodeName").text());
        });
        if (elems.length) {
            output =  (
                    JSON.stringify(elems)
                    + "\n//selected labels:" + labelNames.join(", "));

        }
        return output;
    }

    //check the boxes from a serialized list of checkboxes
    function str2cb(str) {
        if (str) {
            str = str.replace(/\/\/selected labels:.+/, "");
            $.each(JSON.parse(str), function (i, cbid) {
                $("#" + cbid).prop('checked', true);
            });
        }
    }

    /**
     * Initialize the "Filter Ontology Values" page
     * @private
     */
    var _initOntologyFilterPage = function (data) {

        $("#filterForm").submit(function () {
            var errors = "";
            $(".integrationTable").each(function () {
                if ($(":checked ", $(this)).length == 0) {
                    errors = "At least one integration column does not have any filter values checked";
                }
            });

            if (errors != '') {
                alert(errors);
                return false;
            }
            ;
            if ($("#filterForm :checked").length < 1) {
                alert("please select at least one variable");
                return false;
            }
        });

        $(".autocheck").click(function () {
            $(this).closest(".integration-column").find("[canautocheck]").prop("checked", true);
            //$("[canautocheck]",$(this).closest("table")).prop("checked","checked");
        });

        $(".hideElements").click(function () {
            $(this).closest(".integration-column").find("tr.disabled").toggle();
            //$("tr.disabled",$(this).closest("table")).hide();
        });

        $("#btnStr2cb").click(function () {
            var str = $.trim($("#txtStr2cb").val());
            $("#divModalStore").modal('hide');
            $("#divModalStore").on('hidden', function () {
                str2cb(str);
            });
        });

        $("#btnDisplaySelections").click(function () {
            $("#txtStr2cb").text(cb2str());
            $("#divModalStore").modal();
        });
    };

    function _resetDraggable() {
        $(".drg").draggable({
            zIndex: 2700,
            revert: true,
            revertDuration: 0
        });
    }
    
    //expose public elements
    TDAR.integration = {
        "initDataIntegration": _initDataIntegration,
        "addColumn": _addColumn,
        "setStatus": _setStatus,
        "integrationClearAll": _integrationClearAll,
        "selectChildren": _selectChildren,
        "selectAllChildren": _selectAllChildren,
        "initOntologyFilterPage": _initOntologyFilterPage,
    };

})(TDAR, jQuery);
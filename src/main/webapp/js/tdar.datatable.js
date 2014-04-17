TDAR.namespace("datatable");
TDAR.datatable = function () {
    "use strict";

    /**
     * Register a new dtatable control. By default, the datatable populates with resources editable by the user.
     * Most of the initialization options required by the $.dataTable are handled by this function.
     * However, you can override these functions by via the options argument in the constructor
     * @param parms options object - properties in the object will override the defaults used to create the datatable.
     *          see the DataTables.net documentation for full list of option.  Tdar-specific properties include:
     *          tableSelector: jquery selector which contains the table to initialize/convert into a datatable
     *          requestCallback: callback executed prior to performing ajax request. if it returns an object, those
     *                              property name/value pairs are included in the ajax request,
     *          selectableRows: boolean indicating whether the rows of the data table are selectable,
     *          rowSelectionCallback: callback executed whenever user selects a row ,
     *          "sAjaxSource": '/lookup/resource': url to request rowdata
     *
     * @returns {*|jQuery|HTMLElement} reference to the datatable widget created by this element.
     */
    function _registerLookupDataTable(parms) {
        _extendSorting();
        // tableSelector, sAjaxSource, sAjaxDataProp, aoColumns, requestCallback, selectableRows
        var doNothingCallback = function () {
        };
        var options = {
            tableSelector: '#dataTable',
            requestCallback: doNothingCallback,
            selectableRows: false,
            rowSelectionCallback: doNothingCallback,
            "sAjaxSource": '/lookup/resource',
            "sAjaxDataProp": 'resources',
            "bJQueryUI": false,
            "sScrollY": "350px",
            "sScrollX": "100%",
            fnDrawCallback: function () {
                // if all checkboxes are checked, the 'select all' box should also be checked, and unchecked in all other situations
                if ($(":checkbox:not(:checked)", $dataTable).length == 0) {
                    $('#cbCheckAllToggle').prop('checked', true);
                } else {
                    $('#cbCheckAllToggle').prop('checked', false);
                }
            }
        };

        $.extend(options, parms);
        var $dataTable = $(options.tableSelector);

        // here is where we will store the selected rows (if caller wants to track that stuff)
        $dataTable.data('selectedRows', {});

        var dataTableOptions = {
            "bScrollCollapse": true,
            "bProcessing": true,
            "bServerSide": true,
            // "sAjaxDataProp": sAjaxDataProp,
            // "aoColumns": aoColumns

            // intercept the server request, and translate the parameters to server
            // format. similarly, take the json returned by the jserver
            // and translate to format expected by the client.
            "fnServerData": function _fnServerData(sSource, aoData, fnCallback) {

                $.ajax({
                    traditional: true, // please don't convert my arrays to php arrays. php is dumb.
                    dataType: 'jsonp',
                    url: sSource,
                    data: _convertRequest(aoData, options.aoColumns, options.requestCallback),
                    success: function (_data) {
                        // intercept data returned by server, translate to client format

                        var recordInfo = {
                            iTotalDisplayRecords: _data.status.totalRecords,
                            iTotalRecords: _data.status.totalRecords
                        };
                        $.extend(_data, recordInfo);
                        fnCallback(_data);
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.error("ajax query failed:" + errorThrown);
                    }
                });
            }
        };

        // if user wants selectable rows, render checkbox in the first column (which we assume is an ID field)
        if (options.selectableRows) {
            options.aoColumns[0].fnRender = fnRenderIdColumn;
            options.aoColumns[0].bUseRendered = false;
            dataTableOptions["fnRowCallback"] = function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                // determine whether the user selected this item already (if so check the box)
                var $cb = $(nRow).find('input[type=checkbox]');
                var id = $cb.val();
                if ($dataTable.data('selectedRows')[id]) {
                    $cb.prop('checked', true);
                }
                return nRow;
            };

            // register datatable checkbox changes. maintain a hashtable of all of the currently selected items.
            // call the rowSelectionCallback whenever something changes
            $dataTable.delegate('input[type=checkbox]', 'change', function () {
                var $elem = $(this); // here 'this' is checkbox
                var id = $elem.val();
                var objRowData = $dataTable.fnGetData($elem.parents('tr')[0]);
                if ($elem.prop('checked')) {
                    // get the json data associated w/ the selected row, put it in selectedRows
                    $dataTable.data('selectedRows')[id] = objRowData;
                    options.rowSelectionCallback(id, objRowData, true);
                } else {
                    delete $dataTable.data('selectedRows')[id]; // unchecked, so remove from the hashtable
                    options.rowSelectionCallback(id, objRowData, false);
                }

            });
        }

        // put any user-specified dataTable options that have been specified in the parms into the dataTableOptions
        $.extend(options, dataTableOptions);

        $dataTable.dataTable(options);
        _scrollOnPagination();
        return $dataTable;
    }

    /**
     * Prepare request data to be sent to tdar lookup request. This function will derive the startpage,
     * recordsPerPage, and sortField any additional data to be sent to server should be returned by
     * requestCallback(sSearch) where sSearch is the search term entered in the datatable search box (if any).
     * @param aoData array of objects with "name" and "value" properties.
     * @param aoColumns
     * @param requestCallback
     * @returns {{startRecord: (j.defaults.iDisplayStart|*|int), recordsPerPage: (j.defaults.iDisplayLength|*|int), sortField: (string|g_settingsMap.person.tdarSortOption|g_settingsMap.institution.tdarSortOption|g_settingsMap.keyword.tdarSortOption|tdarSortOption)}}
     * @private
     */
    function _convertRequest(aoData, aoColumns, requestCallback) {
        var oData = {};
        // first convert the request from array of key/val pairs to map<string,string>.
        $.each(aoData, function () {
            oData[this.name] = this.value;
        });

        // derive sort column from the field name and reversed status
        var tdarSortOption = aoColumns[oData["iSortCol_0"]].tdarSortOption;
        var sSortReversed = {desc: 'true'}[oData["sSortDir_0"]];
        if (sSortReversed) {
            tdarSortOption += '_REVERSE';
        }
        var translatedData = {
            startRecord: oData.iDisplayStart,
            recordsPerPage: oData.iDisplayLength,
            sortField: tdarSortOption
        };

        $.extend(translatedData, requestCallback(oData.sSearch));

        return translatedData;
    }

    /**
     * callback that renders the "id" column of the datatable.
     * @param oObj
     * @returns {string}
     */
    function fnRenderIdColumn(oObj) {
        // in spite of the name, aData is an object corresponding to the current row
        var id = oObj.aData.id;
        var attrId = "cbEntityId_" + id;
        var resourceType = oObj.aData.resourceType;
        // not all things are resourceTypes that are rendered like this
        if (resourceType) {
            resourceType = resourceType.toLowerCase();
        }
        //console.log("resource type:%s", resourceType);
        return '<input type="checkbox" class="datatable-checkbox ' + resourceType + '" id="' + attrId + '" value="' + id + '" />' + '<label class="datatable-cell-unstyled" for="' + attrId + '">' + id + '</label>';
    }

    /**
     * datatable cell render callback:  this callback specifically renders a resource title.
     * @param oObj row object
     * @returns {string}  html to place insert into the cell
     */
    function fnRenderTitle(oObj) {
        // in spite of name, aData is an object containing the resource record for this row
        var objResource = oObj.aData;
        var html = '<a href="' + getURI(objResource.urlNamespace + '/' + objResource.id) + '" class=\'title\'>' + TDAR.common.htmlEncode(objResource.title) + '</a>';
        html += ' (ID: ' + objResource.id
        if (objResource.status != 'ACTIVE') {
            html += " " + objResource.status;
        }
        html += ')';
        return html;
    }

    /**
     * datatable cell render callback:  this callback emits the title and decription.
     * @param oObj row object
     * @returns {string}  html to place insert into the cell
     */
    function fnRenderTitleAndDescription(oObj) {
        var objResource = oObj.aData;
        return fnRenderTitle(oObj) + '<br /> <p>' + TDAR.common.htmlEncode(TDAR.common.elipsify(objResource.description, 80)) + '</p>';
    }

    /**
     * initialize the datatable used for the dashboard page, as well as the datatable search controls.
     * @param options
     * @private
     */
    function _setupDashboardDataTable(options) {
        var _options = $.extend({}, options);
        _extendSorting();

        // set the project selector to the last project viewed from this page
        // if not found, then select the first item
        var prevSelected = $.cookie("tdar_datatable_selected_project");
        if (prevSelected != null) {
            var elem = $('#project-selector option[value=' + prevSelected + ']');
            if (elem.length) {
                elem.attr("selected", "selected");
            } else {
                $("#project-selector").find("option :first").attr("selected", "selected");
            }

        }
        var prevSelected = $.cookie("tdar_datatable_selected_collection");
        if (prevSelected != null) {
            var elem = $('#collection-selector option[value=' + prevSelected + ']');
            if (elem.length) {
                elem.attr("selected", "selected");
            } else {
                $("#collection-selector").find("option :first").attr("selected", "selected");
            }

        }

        jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = 3;
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper": "dataTables_wrapper form-inline"
        });
        // sDom:'<"datatabletop"ilrp>t<>', //omit the search box

        var _fnRenderTitle = _options.showDescription ? fnRenderTitleAndDescription : fnRenderTitle;

        var aoColumns_ = [
            { "mDataProp": "title", fnRender: _fnRenderTitle, bUseRendered: false, "bSortable": false},
            { "mDataProp": "resourceTypeLabel", "bSortable": false }
        ];
        // make id the first column when datatable is selectable
        if (_options.isSelectable) {
            aoColumns_.unshift({ "mDataProp": "id", tdarSortOption: "ID", sWidth: '5em', "bSortable": false});
        }
        var $dataTable = _registerLookupDataTable({
            tableSelector: '#resource_datatable',
            sAjaxSource: '/lookup/resource',
            "bLengthChange": true,
            "bFilter": false,
            aoColumns: aoColumns_,
            // "sDom": "<'row'<'span9'l><'span6'f>r>t<'row'<'span4'i><'span5'p>>",
            "sDom": "<'row'<'span6'l><'pull-right span3'r>>t<'row'<'span4'i><'span5'p>>",  // no text filter!
            sAjaxDataProp: 'resources',
            requestCallback: function (searchBoxContents) {
                return {title: searchBoxContents,
                    'resourceTypes': $("#resourceTypes").val() == undefined ? "" : $("#resourceTypes").val(),
                    'includedStatuses': $("#statuses").val() == undefined ? "" : $("#statuses").val(),
                    'sortField': $("#sortBy").val(),
                    'term': $("#query").val(),
                    'projectId': $("#project-selector").val(),
                    'collectionId': $("#collection-selector").val(),
                    useSubmitterContext: !_options.isAdministrator
                }
            },
            selectableRows: _options.isSelectable,
            rowSelectionCallback: function (id, obj, isAdded) {
                if (isAdded) {
                    _rowSelected(obj);
                } else {
                    _rowUnselected(obj);
                }
            }
        });

        $("#project-selector").change(function () {
            var projId = $(this).val();
            $.cookie("tdar_datatable_selected_project", projId);
            $("#resource_datatable").dataTable().fnDraw();
        });

        $("#collection-selector").change(function () {
            var projId = $(this).val();
            $.cookie("tdar_datatable_selected_collection", projId);
            $("#resource_datatable").dataTable().fnDraw();
        });

        $("#resourceTypes").change(function () {
            $("#resource_datatable").dataTable().fnDraw();
            $.cookie($(this).attr("id"), $(this).val());
        });

        $("#statuses").change(function () {
            $("#resource_datatable").dataTable().fnDraw();
            $.cookie($(this).attr("id"), $(this).val());
        });

        $("#sortBy").change(function () {
            $("#resource_datatable").dataTable().fnDraw();
            $.cookie($(this).attr("id"), $(this).val());
        });

        $("#query").change(function () {
            $("#resource_datatable").dataTable().fnDraw();
            $.cookie($(this).attr("id"), $(this).val());
        });

        $("#query").bindWithDelay("keyup", function () {
            $("#resource_datatable").dataTable().fnDraw();
        }, 500);

        _scrollOnPagination();
    }

//
    /**
     * populate the dataTable.data('selectedRows') from the hidden inputs in #divSelectedResources (e.g. when rendering 'edit' or 'input' form)
     * @param dataTable datatable widget
     * @param resourcesTable the table that contains the selected datatable rows.
     * @private
     */
    function _registerResourceCollectionDataTable(dataTable, resourcesTable) {
        // if user is editing existing collection, gather the hidden elements and put them in the 'seleted rows' object
        var $dataTable = $(dataTable);
        var $resourcesTable = $(resourcesTable);
        var selectedRows = {};

        $.each($('input', '#divSelectedResources'), function () {
            var elem = this;
            selectedRows[elem.value] = {id: elem.value, title: 'n/a', description: 'n/a'};
            //console.debug('adding id to preselected rows:' + elem.value);
        });
        $dataTable.data('selectedRows', selectedRows);

        // hide the selected items table if server hasn't prepopulated it
        if ($resourcesTable.find('tr').length == 1) {
            $resourcesTable.hide();
        }

        //bind row delete button
        $resourcesTable.on('click', 'button', function () {
            var button = this, resourceid = $(button).data("rid");
            _removeResourceClicked(resourceid, button, dataTable);
        });
    }

    /**
     * row selected callback.  This callback constructs a table row for the "selected records" table
     * @param obj
     * @private
     */
    function _rowSelected(obj) {

        // first, add the hidden input tag to the dom
        var tag = '<input type="hidden" name="resources.id" value="' + obj.id + '" id="hrid' + obj.id + '"/>';
        //console.log("adding selected resource:" + tag);
        $('#divSelectedResources').append(tag);

        // next, add a new row to the 'selected items' table.
        //FIXME: Really, Jim?  All this to render a button?
        var $table = $('#tblCollectionResources');
        var $tbody = $('tbody', $table);
        var resourceTag = '';
        resourceTag += '<tr id="dtr_:id">                                                                   ';
        resourceTag += '    <td>:id</td>                                                                             ';
        resourceTag += '    <td>                                                                                      ';
        resourceTag += '        <a href="/:urlNamespace/:id" target="_b" >                                    ';
        resourceTag += '            :title        ';
        resourceTag += '        </a>                                                                                  ';
        resourceTag += '    </td>                                                                                     ';
        resourceTag += '    <td><button class="btn btn-mini" data-rid=":id" type="button" tabindex="-1"><i class="icon-trash"></i></button></td>';
        resourceTag += '</tr>                                                                                         ';

        resourceTag = resourceTag.replace(/:id/g, obj.id);
        resourceTag = resourceTag.replace(/:urlNamespace/g, obj.urlNamespace);
        resourceTag = resourceTag.replace(/:title/g, obj.title);
        resourceTag = resourceTag.replace(/:description/g, obj.description);
        resourceTag = resourceTag.replace(/:status/g, obj.status);

        $tbody.append(resourceTag);
        $table.show();
    }

    /**
     *  row unselected callback:  remove the row of the "selected records" table
     * @param obj
     * @private
     */
    function _rowUnselected(obj) {
        //console.log('removing selected reosurce:' + obj.id);
        $('#hrid' + obj.id).remove();

        var $row = $('#dtr_' + obj.id);
        var $table = $row.closest('table');
        // var $div = $row.closest('div');
        $row.remove();
        if ($table.find('tr').length == 1) {
            $table.hide();
        } // FIXME: DRY

    }

    /**
     * This is similar to _rowUnselected, but instead of the data-
     * table,  this callback executes when a user removed a selected entry via the delete button of the "currently
     * selected rows" table.
     *
     * @param id id of the row object removed
     * @param elem the delete button element
     * @param dataTable the resource datatable
     *
     * @private
     */
    function _removeResourceClicked(id, elem, dataTable) {
        var $dataTable = $(dataTable);
        // delete the element from the selectedrows structure and remove the hidden input tag
        delete $dataTable.data('selectedRows')[id];
        $('#hrid' + id).remove();

        // now delete the row from the table
        var $elem = $(elem);
        var $tr = $elem.closest('tr');
        var $div = $elem.closest('div');
        $tr.remove();

        // if the table is empty, hide the section
        if ($('tr', $div).length == 1) { // one header row
            // $div.hide();
            $table.hide();
        }

        // if the datatable is on a page that shows the corresponding checkbox, clear the checkbox it
        $('#cbEntityId_' + id, $dataTable).prop('checked', false);

    }

    /**
     * pagination callback: this callback returns the vertical scroll position to the top of the page when the user
     * navigates to a new page.
     * @private
     */
    function _scrollOnPagination() {
        $(".dataTables_paginate a").click(function () {
            $(".dataTables_scrollBody").animate({scrollTop: 0 });
            return true;
        });
    }

    /**
     * Define sorting behavior when user clicks on datatable columns.
     * @private
     */
    function _extendSorting() {

        jQuery.fn.dataTableExt.aTypes.unshift(function (sData) {
                    if (typeof sData === "number" || $.trim(sData).match(/\$?\-?([\d,\.])*/g)) {
                        return 'tdar-number';
                    }
                    return null;
                });

        jQuery.fn.dataTableExt.oSort['tdar-number-asc'] = function (x_, y_) {
            var x = parseFloat(x_.replace(/([\$|\s|\,]*)/g, ""));
            var y = parseFloat(y_.replace(/([\$|\s|\,]*)/g, ""));
            return x - y;
        };

        jQuery.fn.dataTableExt.oSort['tdar-number-desc'] = function (x_, y_) {
            var x = parseFloat(x_.replace(/([\$|\s|\,]*)/g, ""));
            var y = parseFloat(y_.replace(/([\$|\s|\,]*)/g, ""));
            return y - x;
        };
    }

    return {
        extendSorting: _extendSorting,
        registerLookupDataTable: _registerLookupDataTable,
        setupDashboardDataTable: _setupDashboardDataTable,
        removeResourceClicked: _removeResourceClicked,
        registerResourceCollectionDataTable: _registerResourceCollectionDataTable
    };
}();

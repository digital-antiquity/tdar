TDAR.namespace("datatable");
TDAR.datatable = function() {
    "use strict";
    var link = true;
    /**
     * Register a new dtatable control. By default, the datatable populates with resources editable by the user. Most of the initialization options required by
     * the $.dataTable are handled by this function. However, you can override these functions by via the options argument in the constructor
     * 
     * @param parms
     *            options object - properties in the object will override the defaults used to create the datatable. see the DataTables.net documentation for
     *            full list of option. Tdar-specific properties include: tableSelector: jquery selector which contains the table to initialize/convert into a
     *            datatable requestCallback: callback executed prior to performing ajax request. if it returns an object, those property name/value pairs are
     *            included in the ajax request, selectableRows: boolean indicating whether the rows of the data table are selectable, rowSelectionCallback:
     *            callback executed whenever user selects a row , "sAjaxSource": '/lookup/resource': url to request rowdata
     * 
     * @returns {*|jQuery|HTMLElement} reference to the datatable widget created by this element.
     */
    function _registerLookupDataTable(parms) {
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper" : "dataTables_wrapper "
        });

        _extendSorting();
        // tableSelector, sAjaxSource, sAjaxDataProp, aoColumns, requestCallback, selectableRows
        var doNothingCallback = function() {
        };
        var options = {
            vueModel : null,
            tableSelector : '#dataTable',
            requestCallback : doNothingCallback,
            selectableRows : false,
            isAdmin : false,

            rowSelectionCallback : doNothingCallback,
            "sAjaxSource" : TDAR.uri('api/lookup/resource'),
            "sAjaxDataProp" : 'resources',
            "bJQueryUI" : false,
            "sScrollY" : "350px",
            "sScrollX" : "100%",
            fnDrawCallback : function(settings) {
                var $dt = this; // this-object is the current datatable object (assumption based on observation)
                // if all checkboxes are checked, the 'select all' box should also be checked, and unchecked in all other situations

                var $toggle = $('#cbCheckAllToggle');

                if ($(":checkbox:not(:checked)", $dataTable).length == 0) {
                    $toggle.prop('checked', true);
                } else {
                    $toggle.prop('checked', false);
                }

                // welcome a different message for the "un-filter" message if we have 0 records and the filter checkbox exists
                // show the unfilter message if "checked"
                var $fltr = $("#fltrTxt");
                var $cbx = $("#parentCollectionsIncluded");
                if ($dt.fnSettings()._iRecordsTotal < 1 && $cbx != undefined) {
                    if ($cbx.is(':checked')) {
                        $fltr.show();
                    } else {
                        $fltr.hide();
                    }
                } else {
                    $fltr.hide();
                }
                $dt.trigger("data", [ $dt.fnSettings().aoData ]);
            }
        };

        $.extend(options, parms);

        var $dataTable = $(options.tableSelector);

        // here is where we will store the selected rows (if caller wants to track that stuff)
        $dataTable.data('selectedRows', {});

        var dataTableOptions = {
            "bScrollCollapse" : true,
            "bProcessing" : true,
            "bServerSide" : true,
            // "sAjaxDataProp": sAjaxDataProp,
            // "aoColumns": aoColumns

            // intercept the server request, and translate the parameters to server
            // format. similarly, take the json returned by the jserver
            // and translate to format expected by the client.
            "fnServerData" : function _fnServerData(sSource, aoData, fnCallback) {

                $.ajax({
                    traditional : true, // please don't convert my arrays to php arrays. php is dumb.
                    dataType : 'jsonp',
                    url : sSource,
                    xhrFields : {
                        withCredentials : true
                    },

                    data : _convertRequest(aoData, options.aoColumns, options.requestCallback, $dataTable),

                    success : function(_data) {
                        // assuming the above lists are mutually exclusive, the concatenation is a list of all checkboxes that should be toggled

                        // intercept data returned by server, translate to client format
                        var recordInfo = {
                            iTotalDisplayRecords : _data.totalRecords,
                            iTotalRecords : _data.totalRecords
                        };

                        if (typeof _data.totalRecords === "undefined") {
                            recordInfo = {
                                iTotalDisplayRecords : _data.status.totalRecords,
                                iTotalRecords : _data.status.totalRecords
                            };
                        }

                        $.extend(_data, recordInfo);

                        // similarly, add isSelectedResult property to each result
                        if ((options.selectableRows || options.clickableRows) && _data.resources) {
                            if (!_data.selectedResults) {
                                _data.selectedResults = [];
                            }


                            $.each(_data.resources, function(idx, obj) {
                                obj.isSelectedResult = _data.selectedResults.indexOf(obj.id) > -1;

                                // determining the current selected status is tricky. We need to reconcile the value from the server setting (isSelectedResult)
                                // against any client-side changes made so far.
                                obj.isCurrentlySelected = (obj.isSelectedResult !== obj.isToggled);
                            });
                        }
                        fnCallback(_data);
                    },
                    error : function(jqXHR, textStatus, errorThrown) {
                        console.error("ajax query failed:" + errorThrown);
                    }
                });
            }
        };

        // if user wants selectable rows, render checkbox in the first column (which we assume is an ID field)
        if (options.selectableRows) {
            options.aoColumns[0].render = fnRenderIdColumn;
            options.aoColumns[0].bUseRendered = false;

            dataTableOptions["fnRowCallback"] = function(nRow, obj, iDisplayIndex, iDisplayIndexFull) {
                // determine whether the user selected this item already (if so check the box)
                var $cb = $(nRow).find('input[type=checkbox]');
                var id = $cb.val();
                $(nRow).attr("id", "row-" + id);
                $cb.prop('checked', obj.isCurrentlySelected);
                return nRow;
            };

            // register datatable checkbox changes. maintain a hashtable of all of the currently selected items.
            // call the rowSelectionCallback whenever something changes
            $dataTable.on('change', 'input[type=checkbox]', function() {
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

        // this can be done more elegatly, so the callback can be set in the options.
        if (options.clickableRows) {
            if (options.isClickable) {
                link = false;
            }

            dataTableOptions["fnRowCallback"] = function(nRow, obj, iDisplayIndex, iDisplayIndexFull) {
                // determine whether the user selected this item already (if so check the box)
                // var $button = $(nRow).find('button');
                // var btnId = $button.prop('id');
                // var btnName = btnId.substring(0, btnId.lastIndexOf("_"));
                var id = parseInt(obj.id);

                $(nRow).attr("id", "row-" + id);
                return nRow;
            };
            // The $dataTable is the jQuery object for the table.
            // The click event listener is being bound to all the buttons in the table.

            if (options.collectionClick) {
                $dataTable.on('click', 'button', function() {
                    console.log("Binding add/remove button event handlers");
                    var $elem = $(this); // 'this' is a button
                    var btnId = $elem.prop('id'); // Gets the button ID
                    var btnName = btnId.substring(0, btnId.lastIndexOf("_")); // parse out the
                    var id = parseInt(btnId.substring(btnId.lastIndexOf("_") + 1));
                    var objRowData = $dataTable.fnGetData($elem.parents('tr')[0]);

                    var mode = $elem.val(); // The value attribute has what the button is used for.

                    // The basic function of the a button click is to
                    // 1) Add the ID of the resource to the Table's data object
                    // 2) Invoke the callback method of the row (Add managed/unmanaged)
                    // 3) Disable the button so it cant be clicked.

                    switch (mode) {
                        case "addUnmanaged":
                            _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, true, false);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "addManaged":
                            _arrayAdd($dataTable.data('toAddManaged'), id);
                            options.rowSelectionCallback(id, objRowData, true, true);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "addBoth":
                            _arrayAdd($dataTable.data('toAddManaged'), id);
                            _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, true, true);
                            options.rowSelectionCallback(id, objRowData, true, false);
                            $elem.attr("disabled", "disabled");
                            break;

                        case "removeUnmanaged":
                            _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, false, false);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "removeManaged":
                            _arrayAdd($dataTable.data('toRemoveManaged'), id);
                            options.rowSelectionCallback(id, objRowData, false, true);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "removeBoth":
                            _arrayAdd($dataTable.data('toRemoveManaged'), id);
                            _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, false, false);
                            options.rowSelectionCallback(id, objRowData, false, true);
                            $elem.attr("disabled", "disabled");
                            break;
                    }
                });
            }
            $dataTable.on('click', 'button', function() {
                console.log("Binding add/remove button event handlers");
                var $elem = $(this); // 'this' is a button
                var btnId = $elem.prop('id'); // Gets the button ID
                var btnName = btnId.substring(0, btnId.lastIndexOf("_")); // parse out the
                var id = parseInt(btnId.substring(btnId.lastIndexOf("_") + 1));
                var objRowData = $dataTable.fnGetData($elem.parents('tr')[0]);

                var mode = $elem.val(); // The value attribute has what the button is used for.

                // The basic function of the a button click is to
                // 1) Add the ID of the resource to the Table's data object
                // 2) Invoke the callback method of the row (Add managed/unmanaged)
                // 3) Disable the button so it cant be clicked.

                switch (mode) {
                    case "addUnmanaged":
                        _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, true, false);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "addManaged":
                        _arrayAdd($dataTable.data('toAddManaged'), id);
                        options.rowSelectionCallback(id, objRowData, true, true);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "addBoth":
                        _arrayAdd($dataTable.data('toAddManaged'), id);
                        _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, true, true);
                        options.rowSelectionCallback(id, objRowData, true, false);
                        $elem.attr("disabled", "disabled");
                        break;

                    case "removeUnmanaged":
                        _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, false, false);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "removeManaged":
                        _arrayAdd($dataTable.data('toRemoveManaged'), id);
                        options.rowSelectionCallback(id, objRowData, false, true);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "removeBoth":
                        _arrayAdd($dataTable.data('toRemoveManaged'), id);
                        _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, false, false);
                        options.rowSelectionCallback(id, objRowData, false, true);
                        $elem.attr("disabled", "disabled");
                        break;
                }
            });
        }

        // put any user-specified dataTable options that have been specified in the parms into the dataTableOptions
        $.extend(options, dataTableOptions);
        // console.log(options);

        $dataTable.dataTable(options);
        _scrollOnPagination();
        return $dataTable;
    }

    
    function _registerCollectionLookupDataTable(parms) {
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper" : "dataTables_wrapper "
        });

        _extendSorting();
        // tableSelector, sAjaxSource, sAjaxDataProp, aoColumns, requestCallback, selectableRows
        var doNothingCallback = function() {
        };
        var options = {
            vueModel : null,
            tableSelector : '#dataTable',
            requestCallback : doNothingCallback,
            selectableRows : false,
            isAdmin : false,

            rowSelectionCallback : doNothingCallback,
            "sAjaxSource" : TDAR.uri('api/lookup/resource'),
            "sAjaxDataProp" : 'resources',
            "bJQueryUI" : false,
            "sScrollY" : "350px",
            "sScrollX" : "100%",
            fnDrawCallback : function(settings) {
                var $dt = this; // this-object is the current datatable object (assumption based on observation)
                // if all checkboxes are checked, the 'select all' box should also be checked, and unchecked in all other situations

                var $toggle = $('#cbCheckAllToggle');

                if ($(":checkbox:not(:checked)", $dataTable).length == 0) {
                    $toggle.prop('checked', true);
                } else {
                    $toggle.prop('checked', false);
                }

                // welcome a different message for the "un-filter" message if we have 0 records and the filter checkbox exists
                // show the unfilter message if "checked"
                var $fltr = $("#fltrTxt");
                var $cbx = $("#parentCollectionsIncluded");
                if ($dt.fnSettings()._iRecordsTotal < 1 && $cbx != undefined) {
                    if ($cbx.is(':checked')) {
                        $fltr.show();
                    } else {
                        $fltr.hide();
                    }
                } else {
                    $fltr.hide();
                }
                $dt.trigger("data", [ $dt.fnSettings().aoData ]);
            }
        };

        $.extend(options, parms);

        var $dataTable = $(options.tableSelector);

        $dataTable.data("toAdd", []);
        $dataTable.data("toRemove", []);
        $dataTable.data("toAddManaged", []);
        $dataTable.data("toAddUnmanaged", []);
        $dataTable.data("toRemoveManaged", []);
        $dataTable.data("toRemoveUnmanaged", []);

        // here is where we will store the selected rows (if caller wants to track that stuff)
        $dataTable.data('selectedRows', {});

        var dataTableOptions = {
            "bScrollCollapse" : true,
            "bProcessing" : true,
            "bServerSide" : true,
            // "sAjaxDataProp": sAjaxDataProp,
            // "aoColumns": aoColumns

            // intercept the server request, and translate the parameters to server
            // format. similarly, take the json returned by the jserver
            // and translate to format expected by the client.
            "fnServerData" : function _fnServerData(sSource, aoData, fnCallback) {

                $.ajax({
                    traditional : true, // please don't convert my arrays to php arrays. php is dumb.
                    dataType : 'jsonp',
                    url : sSource,
                    xhrFields : {
                        withCredentials : true
                    },

                    data : _convertRequest(aoData, options.aoColumns, options.requestCallback, $dataTable),

                    success : function(_data) {
                        var addIds = $dataTable.data("toAdd");
                        var removeIds = $dataTable.data("toRemove");

                        // These are IDs of resources that are already in the collection.
                        var managedResults = $dataTable.data("managedResults");
                        var unmanagedResults = $dataTable.data("unmanagedResults");

                        // assuming the above lists are mutually exclusive, the concatenation is a list of all checkboxes that should be toggled
                        var toggleIds = addIds.concat(removeIds);

                        // intercept data returned by server, translate to client format
                        var recordInfo = {
                            iTotalDisplayRecords : _data.totalRecords,
                            iTotalRecords : _data.totalRecords
                        };

                        if (typeof _data.totalRecords === "undefined") {
                            recordInfo = {
                                iTotalDisplayRecords : _data.status.totalRecords,
                                iTotalRecords : _data.status.totalRecords
                            };
                        }

                        $.extend(_data, recordInfo);

                        // update the list of resource id's that belong to the current resource collection

                        $dataTable.data("selectedResults", _data.selectedResults);
                        $dataTable.data("managedResults", _data.unmanagedResourceResults);
                        $dataTable.data("unmanagedResults", _data.managedResourceResults);

                        var addManagedIds = $dataTable.data("toAddManaged");
                        var addUnmanagedIds = $dataTable.data("toAddUnmanaged");
                        var removeManagedIds = $dataTable.data("toRemoveManaged");
                        var removeUnmanagedIds = $dataTable.data("toRemoveUnmanaged");

                        _data["toAddManaged"] = addManagedIds;
                        _data["toAddUnmanaged"] = addUnmanagedIds;
                        _data["toRemoveManaged"] = removeManagedIds;
                        _data["toRemoveUnmanaged"] = removeUnmanagedIds;

                        // similarly, add isSelectedResult property to each result
                        if ((options.selectableRows || options.clickableRows) && _data.resources) {
                            if (!_data.selectedResults) {
                                _data.selectedResults = [];
                            }

                            if (!_data.unmanagedResourceResults) {
                                _data.unmanagedResourceResults = [];
                            }
                            if (!_data.managedResourceResults) {
                                _data.managedResourceResults = [];
                            }

                            $.each(_data.resources, function(idx, obj) {
                                obj.isSelectedResult = _data.selectedResults.indexOf(obj.id) > -1;
                                obj.isToggled = toggleIds.indexOf(obj.id) > -1;

                                // determining the current selected status is tricky. We need to reconcile the value from the server setting (isSelectedResult)
                                // against any client-side changes made so far.
                                obj.isCurrentlySelected = (obj.isSelectedResult !== obj.isToggled);

                                // This will look to see if the object's id exists in the managed resource array.
                                obj.isManagedResult = _data.managedResourceResults.indexOf(obj.id) > -1;

                                // This will look to see if the object's id exists in the unmanaged resource array.
                                obj.isUnmanagedResult = _data.unmanagedResourceResults.indexOf(obj.id) > -1;
                            });
                        }
                        fnCallback(_data);
                    },
                    error : function(jqXHR, textStatus, errorThrown) {
                        console.error("ajax query failed:" + errorThrown);
                    }
                });
            }
        };

        // if user wants selectable rows, render checkbox in the first column (which we assume is an ID field)
        if (options.selectableRows) {
            options.aoColumns[0].render = fnRenderIdColumn;
            options.aoColumns[0].bUseRendered = false;

            dataTableOptions["fnRowCallback"] = function(nRow, obj, iDisplayIndex, iDisplayIndexFull) {
                // determine whether the user selected this item already (if so check the box)
                var $cb = $(nRow).find('input[type=checkbox]');
                var id = $cb.val();
                $(nRow).attr("id", "row-" + id);
                $cb.prop('checked', obj.isCurrentlySelected);
                return nRow;
            };

            // register datatable checkbox changes. maintain a hashtable of all of the currently selected items.
            // call the rowSelectionCallback whenever something changes
            $dataTable.on('change', 'input[type=checkbox]', function() {
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

        // this can be done more elegatly, so the callback can be set in the options.
        if (options.clickableRows) {
            if (options.isClickable) {
                link = false;
            }

            dataTableOptions["fnRowCallback"] = function(nRow, obj, iDisplayIndex, iDisplayIndexFull) {
                // determine whether the user selected this item already (if so check the box)
                // var $button = $(nRow).find('button');
                // var btnId = $button.prop('id');
                // var btnName = btnId.substring(0, btnId.lastIndexOf("_"));
                var id = parseInt(obj.id);

                $(nRow).attr("id", "row-" + id);
                return nRow;
            };
            // The $dataTable is the jQuery object for the table.
            // The click event listener is being bound to all the buttons in the table.

            if (options.collectionClick) {
                $dataTable.on('click', 'button', function() {
                    console.log("Binding add/remove button event handlers");
                    var $elem = $(this); // 'this' is a button
                    var btnId = $elem.prop('id'); // Gets the button ID
                    var btnName = btnId.substring(0, btnId.lastIndexOf("_")); // parse out the
                    var id = parseInt(btnId.substring(btnId.lastIndexOf("_") + 1));
                    var objRowData = $dataTable.fnGetData($elem.parents('tr')[0]);

                    var mode = $elem.val(); // The value attribute has what the button is used for.

                    // The basic function of the a button click is to
                    // 1) Add the ID of the resource to the Table's data object
                    // 2) Invoke the callback method of the row (Add managed/unmanaged)
                    // 3) Disable the button so it cant be clicked.

                    switch (mode) {
                        case "addUnmanaged":
                            _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, true, false);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "addManaged":
                            _arrayAdd($dataTable.data('toAddManaged'), id);
                            options.rowSelectionCallback(id, objRowData, true, true);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "addBoth":
                            _arrayAdd($dataTable.data('toAddManaged'), id);
                            _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, true, true);
                            options.rowSelectionCallback(id, objRowData, true, false);
                            $elem.attr("disabled", "disabled");
                            break;

                        case "removeUnmanaged":
                            _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, false, false);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "removeManaged":
                            _arrayAdd($dataTable.data('toRemoveManaged'), id);
                            options.rowSelectionCallback(id, objRowData, false, true);
                            $elem.attr("disabled", "disabled");
                            break;
                        case "removeBoth":
                            _arrayAdd($dataTable.data('toRemoveManaged'), id);
                            _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                            options.rowSelectionCallback(id, objRowData, false, false);
                            options.rowSelectionCallback(id, objRowData, false, true);
                            $elem.attr("disabled", "disabled");
                            break;
                    }
                });
            }
            $dataTable.on('click', 'button', function() {
                console.log("Binding add/remove button event handlers");
                var $elem = $(this); // 'this' is a button
                var btnId = $elem.prop('id'); // Gets the button ID
                var btnName = btnId.substring(0, btnId.lastIndexOf("_")); // parse out the
                var id = parseInt(btnId.substring(btnId.lastIndexOf("_") + 1));
                var objRowData = $dataTable.fnGetData($elem.parents('tr')[0]);

                var mode = $elem.val(); // The value attribute has what the button is used for.

                // The basic function of the a button click is to
                // 1) Add the ID of the resource to the Table's data object
                // 2) Invoke the callback method of the row (Add managed/unmanaged)
                // 3) Disable the button so it cant be clicked.

                switch (mode) {
                    case "addUnmanaged":
                        _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, true, false);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "addManaged":
                        _arrayAdd($dataTable.data('toAddManaged'), id);
                        options.rowSelectionCallback(id, objRowData, true, true);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "addBoth":
                        _arrayAdd($dataTable.data('toAddManaged'), id);
                        _arrayAdd($dataTable.data('toAddUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, true, true);
                        options.rowSelectionCallback(id, objRowData, true, false);
                        $elem.attr("disabled", "disabled");
                        break;

                    case "removeUnmanaged":
                        _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, false, false);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "removeManaged":
                        _arrayAdd($dataTable.data('toRemoveManaged'), id);
                        options.rowSelectionCallback(id, objRowData, false, true);
                        $elem.attr("disabled", "disabled");
                        break;
                    case "removeBoth":
                        _arrayAdd($dataTable.data('toRemoveManaged'), id);
                        _arrayAdd($dataTable.data('toRemoveUnmanaged'), id);
                        options.rowSelectionCallback(id, objRowData, false, false);
                        options.rowSelectionCallback(id, objRowData, false, true);
                        $elem.attr("disabled", "disabled");
                        break;
                }
            });
        }

        // put any user-specified dataTable options that have been specified in the parms into the dataTableOptions
        $.extend(options, dataTableOptions);
        // console.log(options);

        $dataTable.dataTable(options);
        _scrollOnPagination();
        return $dataTable;
    }

    /**
     * Prepare request data to be sent to tdar lookup request. This function will derive the startpage, recordsPerPage, and sortField any additional data to be
     * sent to server should be returned by requestCallback(sSearch) where sSearch is the search term entered in the datatable search box (if any).
     * 
     * @param aoData
     *            array of objects with "name" and "value" properties.
     * @param aoColumns
     * @param requestCallback
     * @param $dataTable
     *            jquery selection containing the datatable
     * @returns {{startRecord: (j.defaults.iDisplayStart|*|int), recordsPerPage: (j.defaults.iDisplayLength|*|int), sortField:
     *          (string|g_settingsMap.person.tdarSortOption|g_settingsMap.institution.tdarSortOption|g_settingsMap.keyword.tdarSortOption|tdarSortOption)}}
     * @private
     */
    function _convertRequest(aoData, aoColumns, requestCallback, $dataTable) {
        var oData = {};
        // first convert the request from array of key/val pairs to map<string,string>.
        $.each(aoData, function() {
            oData[this.name] = this.value;
        });

        // derive sort column from the field name and reversed status
        var translatedData = {
            startRecord : oData.iDisplayStart,
            recordsPerPage : oData.iDisplayLength,
        };
        if (aoColumns[oData["iSortCol_0"]] != undefined) {

            var tdarSortOption = aoColumns[oData["iSortCol_0"]].tdarSortOption;
            var sSortReversed = {
                desc : 'true'
            }[oData["sSortDir_0"]];
            if (sSortReversed) {
                tdarSortOption += '_REVERSE';
            }
            translatedData.sortField = tdarSortOption;
        }

        $.extend(translatedData, requestCallback(oData.sSearch));
        console.log(translatedData);
        translatedData.data = translatedData.results;
        return translatedData;
    }

    /**
     * callback that renders the "id" column of the datatable.
     * 
     * @param oObj
     * @returns {string}
     */
    function fnRenderIdColumn(a, b, oObj) {
        // in spite of the name, aData is an object corresponding to the current row
        var id = oObj.id;
        var attrId = "cbEntityId_" + id;
        var resourceType = oObj.resourceType;
        // not all things are resourceTypes that are rendered like this
        if (resourceType) {
            resourceType = resourceType.toLowerCase();
        }
        // console.log("resource type:%s", resourceType);
        return ('<label class="datatable-cell-unstyled">' + '<input type="checkbox" class="datatable-checkbox ' + resourceType + '" id="' + attrId +
                '" value="' + id + '" >' + id + '</label>');
    }

    /**
     * callback that renders the "managed" column of the datatable.
     * 
     * @param oObj
     * @returns {string}
     */
    function fnRenderRemoveButtonsColumn(oObj) {
        // in spite of the name, aData is an object corresponding to the current row
        var id = oObj.id;

        var mAttrId = "btnRemoveManagedId_" + id;
        var uAttrId = "btnRemoveUnmanagedId_" + id;
        var bAttrId = "btnRemoveBothId_" + id;

        // this isn't right, becomes tightly coupled, but need a better way of doing it.
        var $dataTable = $("#existing_resources_datatable");

        var addManagedIds = $dataTable.data("toAddManaged");
        var addUnmanagedIds = $dataTable.data("toAddUnmanaged");
        var removeManagedIds = $dataTable.data("toRemoveManaged");
        var removeUnmanagedIds = $dataTable.data("toRemoveUnmanaged");

        // This will set if the resource id is in the array of IDs to be removed/added.
        var isBeingAddedToManaged = addManagedIds.indexOf(id) > -1;
        var isBeingRemovedFromUnmanaged = removeUnmanagedIds.indexOf(id) > -1;

        var isBeingRemovedFromManaged = removeManagedIds.indexOf(id) > -1;
        var isBeingAddedToUnmanaged = addUnmanagedIds.indexOf(id) > -1;

        var isManaged = oObj.isManagedResult == true;
        var isUnmanaged = oObj.isUnmanagedResult == true;

        var output = '<div class="btn-group">';
        var closingUl = "";

        var extraLabel = "";

        if (isManaged) {

            // If the resource is only on one side of the colelction , then the label should just be "Remove", not "Remove Managed" or "Remove Unmanaged",
            // Here we do they check, and if necessary add in the additional label fragment.
            if (isUnmanaged) {
                extraLabel = " Managed";
            }

            // if the resource is manged, but has been put into the remove from managed
            if (isBeingRemovedFromManaged) {
                var sDisabled = ' disabled="disabled" ';
            } else {
                var sDisabled = '';
            }

            output += '<button type="button" id="' + mAttrId + '"' + sDisabled + 'value="removeManaged" class="btn">Remove' + extraLabel + '</button>';
        }

        if (isUnmanaged) {
            extraLabel = "";

            // if the resource is not added as managed, the first button won't show. So just show the text as "removed".
            if (isManaged) {
                output += '<button type="button" class="btn dropdown-toggle" data-toggle="dropdown">   <span class="caret"></span></button>';
                output += '<ul class="dropdown-menu" role="menu"><li>';
                closingUl = "</li></ul>";
                extraLabel = " Unmanaged";
            }

            if (isBeingRemovedFromManaged || !isUnmanaged) {
                var sDisabled = ' disabled="disabled" ';
            } else if (isUnmanaged) {
                var sDisabled = '';
            }

            output += '<button type="button" id="' + uAttrId + '"' + sDisabled + 'value="removeUnmanaged"  class="btn">Remove' + extraLabel + '</button>';
        }

        // This is a stub for a "Remove From Both" button, but it doesn't currently work.
        if (isManaged && isUnmanaged) {
            if (isBeingRemovedFromManaged && isBeingRemovedFromUnmanaged) {
                var sDisabled = ' disabled="disabled" ';
            } else {
                var sDisabled = '';
            }
            // output += '<li><button type="button" id="'+bAttrId+'"'+sDisabled+'value="removeBoth" class="btn">Remove Both</button></li>';
        }

        output += closingUl + "</div>";
        return output;
    }

    /**
     * callback that renders the "managed" column of the datatable.
     * 
     * @param oObj
     * @returns {string}
     */
    function fnRenderAddButtonsColumn(oObj) {
        // in spite of the name, aData is an object corresponding to the current row

        var oSettings = oObj.oSettings;
        var aData = oObj;
        var id = aData.id;
        var mAttrId = "btnAddManagedId_" + id;
        var uAttrId = "btnAddUnmanagedId_" + id;
        var bAttrId = "btnAddBothId_" + id;

        // this isn't right, becomes tightly coupled, but need a better way of doing it.
        var $dataTable = $("#resource_datatable");

        var addManagedIds = $dataTable.data("toAddManaged");
        var addUnmanagedIds = $dataTable.data("toAddUnmanaged");
        var removeManagedIds = $dataTable.data("toRemoveManaged");
        var removeUnmanagedIds = $dataTable.data("toRemoveUnmanaged");

        // This will set if the resource id is in the array of IDs to be removed/added.
        var isBeingAddedToManaged = addManagedIds.indexOf(id) > -1;
        var isBeingAddedToUnmanaged = addUnmanagedIds.indexOf(id) > -1;

        var isManaged = aData.isManagedResult == true;
        var isUnmanaged = aData.isUnmanagedResult == true;

        var output = '<div class="btn-group">';
        var ul = false;
        if (!isManaged) { // If the resource is added as managed, then display the button to allow it to be added.

            // if the resource is manged, but the remove button has been clicked, then the button needs to be disabled so it can't be added again.
            if (isBeingAddedToManaged) {
                var sDisabled = ' disabled="disabled" ';
            }
            // The normal state of the button is to allow it to be clickable.
            else {
                var sDisabled = '';
            }

            // This is the button
            output += '<button type="button" id="' + mAttrId + '"' + sDisabled + 'value="addManaged" class="btn">Add Managed</button>';
            // So if the resource is already added as an unmanaged, then don't give option to add as Unmanaged.
            if (!isUnmanaged) {
                output += '<button type="button" class="btn dropdown-toggle" data-toggle="dropdown">   <span class="caret"></span></button>';
                output += '<ul class="dropdown-menu" role="menu">';
                ul = true;
            }

        }

        if (!isUnmanaged) {
            if (isBeingAddedToUnmanaged) {
                var sDisabled = ' disabled="disabled" ';
            } else {
                var sDisabled = '';
            }
            var button = '<button type="button" id="' + uAttrId + '"' + sDisabled + 'value="addUnmanaged" class="btn">Add Unmanaged</button>';
            if (ul) {
                output += '<li>"+button +"</li></ul>';
            } else {
                output += button;
            }
        }

        // This is supposed to be for a "Add to Both" option, but it doesn't work quite right. This requires additional logic so that when it is removed
        // from only one side of the manage/unmanaged collection, the state should be reset consistently.
        /*******************************************************************************************************************************************************
         * if(!isManaged && !isUnmanaged){ if(!isBeingAddedToManaged && isBeingAddedToUnmanaged){ var sDisabled = ' disabled="disabled" ' ; } else { var
         * sDisabled = ''; } //output += '
         * <li><button type="button" id="'+bAttrId+'"'+sDisabled+'value="addBoth" class="btn">Add Both</button></li>'; }
         ******************************************************************************************************************************************************/

        output += "</div>";
        return output;
    }

    /**
     * The Datatable settings are not being passed correctly to the fnRender callback So there's no way to get know what options were set in.
     * 
     * Instead a different callback was setup to circumvent that issue.
     * 
     * @param oObj
     * @returns {string}
     */
    function fnRenderAddButtonsColumnManagedOnly(oObj) {
        // in spite of the name, aData is an object corresponding to the current row

        var oSettings = oObj.oSettings;
        var aData = oObj;
        var id = aData.id;
        var mAttrId = "btnAddManagedId_" + id;
        var uAttrId = "btnAddUnmanagedId_" + id;
        var bAttrId = "btnAddBothId_" + id;

        // this isn't right, becomes tightly coupled, but need a better way of doing it.
        var $dataTable = $("#resource_datatable");

        var addManagedIds = $dataTable.data("toAddManaged");
        var addUnmanagedIds = $dataTable.data("toAddUnmanaged");
        var removeManagedIds = $dataTable.data("toRemoveManaged");
        var removeUnmanagedIds = $dataTable.data("toRemoveUnmanaged");

        // This will set if the resource id is in the array of IDs to be removed/added.
        var isBeingAddedToManaged = addManagedIds.indexOf(id) > -1;
        var isBeingAddedToUnmanaged = addUnmanagedIds.indexOf(id) > -1;

        var isManaged = aData.isManagedResult == true;
        var isUnmanaged = aData.isUnmanagedResult == true;

        var output = '<div class="btn-group">';
        if (!isManaged) {
            // if the resource is manged, but has been put into the remove from managed
            if (isBeingAddedToManaged) {
                var sDisabled = ' disabled="disabled" ';
            } else {
                var sDisabled = '';
            }

            output += '<button type="button" id="' + mAttrId + '"' + sDisabled + 'value="addManaged" class="btn">Add</button>';
        }
        output += "</div>";
        return output;
    }

    function fnRenderStatusColumn(a, b, oObj) {
        // console.log(a,b,oObj);
        var id = oObj.id;
        var managed = oObj.isManagedResult == true;
        var unmanaged = oObj.isUnmanagedResult == true;

        return (managed ? "Managed " : "") + (unmanaged ? " Unmanaged" : "");
    }

    /**
     * callback that renders the "managed" column of the datatable.
     * 
     * @param oObj
     * @returns {string}
     */
    function fnRenderAddRemoveColumn(a, b, oObj) {
        link = false;
        // in spite of the name, aData is an object corresponding to the current row
        var id = oObj.id;
        var managedAttrId = "btnAddManagedId_" + id;
        var resourceType = oObj.resourceType;
        // not all things are resourceTypes that are rendered like this
        if (resourceType) {
            resourceType = resourceType.toLowerCase();
        }
        // console.log("resource type:%s", resourceType);
        return ('<label class="datatable-cell-unstyled">' + '<input type="checkbox" class="datatable-checkbox ' + resourceType + '" id="' + attrId +
                '" value="' + id + '" >' + '</label>');
    }

    /**
     * datatable cell render callback: this callback specifically renders a resource title.
     * 
     * @param oObj
     *            row object
     * @returns {string} html to place insert into the cell
     */
    function fnRenderTitle(oObj, b, c) {
        // in spite of name, aData is an object containing the resource record for this row
        var objResource = oObj;
        if (objResource == undefined || typeof c === "object") {
            objResource = c;
        }
        var html = TDAR.common.htmlEncode(objResource.title);
        if (link == true) {
            html = '<a href="' + TDAR.uri(objResource.urlNamespace + '/' + objResource.id) + '" class=\'title\'><b>' +
                    TDAR.common.htmlEncode(objResource.title) + '</b></a>';
        }
        html += ' (ID: ' + objResource.id
        if (objResource.status != 'ACTIVE') {
            html += " " + objResource.status;
        }
        html += ')';
        return html;
    }

    /**
     * datatable cell render callback: this callback emits the title and decription.
     * 
     * @param oObj
     *            row object
     * @returns {string} html to place insert into the cell
     */
    function fnRenderTitleAndDescription(a, b, oObj, d) {
        // console.log(a,b,oObj,d);
        var objResource = oObj;
        return fnRenderTitle(oObj) + '<br /> <span>' + TDAR.common.htmlEncode(TDAR.ellipsify(objResource.description, 80)) + '</span>';
    }

    /**
     * initialize the datatable used for the dashboard page, as well as the datatable search controls.
     * 
     * @param options
     * @private
     */
    function _setupDashboardDataTable(options) {

        console.log("Settign up Dashboard Datatable");
        var _options = $.extend({}, options);
        _extendSorting();

        jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = 3;
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper" : "dataTables_wrapper "
        });

        var _fnRenderTitle = _options.showDescription ? fnRenderTitleAndDescription : fnRenderTitle;

        var aoColumns_ = [ {
            "mDataProp" : "title",
            render : _fnRenderTitle,
            bUseRendered : false,
            "bSortable" : false
        }, {
            "mDataProp" : "resourceTypeLabel",
            "bSortable" : false
        } ];
        // make id the first column when datatable is selectable

        if (_options.isClickable) {
            aoColumns_.unshift({
                "mDataProp" : "id",
                tdarSortOption : "ID",
                sWidth : '5em',
                "bSortable" : false
            });
        }

        var $dataTable = $('#resource_datatable');

        _registerLookupDataTable({
            tableSelector : '#resource_datatable',
            sAjaxSource : TDAR.uri('api/lookup/resource'),
            "bLengthChange" : true,
            "bFilter" : false,
            aoColumns : aoColumns_,
            "sDom" : "<'row'<'col-12'l><'float-right col-3'r>>t<'row'<'col-4'i><'col-5'p>>", // no text filter!
            "sAjaxDataProp" : 'resources',
            "oLanguage" : {
                "sZeroRecords" : "No records found. <span id='fltrTxt'>Consider <a id='lnkResetFilters' href='javascript:void(0)'>expanding your search</a></span>"
            },

            requestCallback : function(searchBoxContents) {
                return _initParams(_options, searchBoxContents);
            },

            selectableRows : _options.isSelectable,
            clickableRows : true, // _options.isClickable,

            // this dashboard table method is used in different places, so some may need checkboxes to be rendered,
            // while others need the add and remove buttons.
            rowSelectionCallback : function(id, obj, isAdded, isManaged) {
                if (_options.isClickable) {
                    if (isAdded) {
                        _addResourceToVueModel(obj, $dataTable, isManaged);
                    } else {
                        _removeResourceFromVueModel(obj, $dataTable, isManaged);
                    }
                }

                else if (_options.isSelectable) {
                    if (isAdded) {
                        _rowSelected(obj, $dataTable);
                    } else {
                        _rowUnselected(obj, $dataTable);
                    }
                }
            }

        });

        _initFilters();
        _scrollOnPagination();
    }

    function _initParams(_options, searchBoxContents) {
        var parms = {
                title : searchBoxContents,
                'resourceTypes' : $("#resourceTypes").val() == undefined ? "" : $("#resourceTypes").val(),
                'includedStatuses' : $("#statuses").val() == undefined ? "" : $("#statuses").val(),
                'sortField' : $("#sortBy").val(),
                'query' : $("#query").val(),
                'projectId' : $("#project-selector").val(),
                'collectionId' : $("#collection-selector").val(),
                selectResourcesFromCollectionid : _options.selectResourcesFromCollectionid
            };

            if (!_options.isAdministrator && _options.limitContext == true) {
                parms['useSubmitterContext'] = true;
            } else {
                parms['useSubmitterContext'] = false;
            }
            if ($("#parentCollectionsIncluded").length) {
                parms.parentCollectionsIncluded = (!$("#parentCollectionsIncluded").prop("checked")).toString();
            }
            return parms;
    }
    function _initFilters() {

        //
        // Sets event handlers for the selection filters.
        //
        var $cs = $("#collection-selector");
        var $ps = $("#project-selector");
        var $rdt = $("#resource_datatable");
        $rdt.on("click", "#lnkResetFilters", function() {
            _resetAllFilters()
        });

        // if the user modifies any of the filter controls, execute a new search and update the results
        // fixme: refactor these event bindings. lots of duplication here
        $ps.change(function() {
            var projId = $(this).val();
            $rdt.dataTable().fnDraw();
        });
        $cs.change(function() {
            var colId = $(this).val();
            $rdt.dataTable().fnDraw();
        });

        $("#resourceTypes").change(function() {
            $rdt.dataTable().fnDraw();
        });

        $("#statuses").change(function() {
            $rdt.dataTable().fnDraw();
        });

        $("#sortBy").change(function() {
            $rdt.dataTable().fnDraw();
        });

        $("#query").change(function() {
            $rdt.dataTable().fnDraw();
        });

        $("#query").bindWithDelay("keyup", function() {
            $rdt.dataTable().fnDraw();
        }, 500);

        $("#parentCollectionsIncluded").change(function() {
            var $elem = $(this);
            var collectionId = $("#metadataForm_id").val();
            if ($elem.prop("checked")) {
                $cs.val(collectionId);
            } else {
                // select the first option (all collections)
                $cs.val("");
            }
            $rdt.dataTable().fnDraw();
        });
    }
    

    /**
     * initialize the datatable used for the dashboard page, as well as the datatable search controls.
     * 
     * @param options
     * @private
     */
    function _setupCollectionDataTable(options) {

        console.log("Settign up Collection Datatable");
        var _options = $.extend({}, options);
        _extendSorting();

        jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = 3;
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper" : "dataTables_wrapper "
        });

        var _fnRenderTitle = _options.showDescription ? fnRenderTitleAndDescription : fnRenderTitle;

        var aoColumns_ = [ {
            "mDataProp" : "title",
            render : _fnRenderTitle,
            bUseRendered : false,
            "bSortable" : false
        }, {
            "mDataProp" : "resourceTypeLabel",
            "bSortable" : false
        } ];
        // make id the first column when datatable is selectable

        if (_options.isClickable) {
            aoColumns_.unshift({
                "mDataProp" : "id",
                tdarSortOption : "ID",
                sWidth : '5em',
                "bSortable" : false
            });
        }

        var $dataTable = $('#resource_datatable');
        
        if (_options.enableUnmanagedCollections) {
            console.log("Enabling Unmanaged Collections. Adding column for status");
            console.log("If a rendering error occurs, it may be because the column is not being rendered in freemarker");
            aoColumns_.push({
                "mData" : null,
                render : fnRenderStatusColumn,
                "bSortable" : false
            });
        }

        if (_options.isClickable) {
            console.log("Clickable is enabled. Adding a column for action");
            console.log("If a rendering error occurs, it may be because the column is not being rendered in freemarker");
            aoColumns_.push({
                "mData" : null,
                render : _options.enableUnmanagedCollections ? fnRenderAddButtonsColumn : fnRenderAddButtonsColumnManagedOnly,
                "bSortable" : false,
            });
        }


        _registerCollectionLookupDataTable({
            tableSelector : '#resource_datatable',
            sAjaxSource : TDAR.uri('api/lookup/resource'),
            "bLengthChange" : true,
            "bFilter" : false,
            aoColumns : aoColumns_,
            "sDom" : "<'row'<'col-12'l><'float-right col-3'r>>t<'row'<'col-4'i><'col-5'p>>", // no text filter!
            "sAjaxDataProp" : 'resources',
            "oLanguage" : {
                "sZeroRecords" : "No records found. <span id='fltrTxt'>Consider <a id='lnkResetFilters' href='javascript:void(0)'>expanding your search</a></span>"
            },

            requestCallback : function(searchBoxContents) {
                return _initParams(_options, searchBoxContents);
            },

            selectableRows : _options.isSelectable,
            clickableRows : true, // _options.isClickable,

            // this dashboard table method is used in different places, so some may need checkboxes to be rendered,
            // while others need the add and remove buttons.
            rowSelectionCallback : function(id, obj, isAdded, isManaged) {
                if (_options.isClickable) {
                    if (isAdded) {
                        _addResourceToVueModel(obj, $dataTable, isManaged);
                    } else {
                        _removeResourceFromVueModel(obj, $dataTable, isManaged);
                    }
                }

                else if (_options.isSelectable) {
                    if (isAdded) {
                        _rowSelected(obj, $dataTable);
                    } else {
                        _rowUnselected(obj, $dataTable);
                    }
                }
            }

        });
        _initFilters();
        _scrollOnPagination();
    }

    /**
     * initialize the datatable used for the edit collection page for displaying the resources in the collection.
     * 
     * @param options
     * @private
     */
    function _setupCollectionResourcesDataTable(options) {
        var _options = $.extend({}, options);
        _extendSorting();
        link = false;

        jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = 3;
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper" : "dataTables_wrapper"
        });

        var _fnRenderTitle = _options.showDescription ? fnRenderTitleAndDescription : fnRenderTitle;

        var aoColumns_ = [ {
            "mDataProp" : "id",
            tdarSortOption : "ID",
            sWidth : '5em',
            "bSortable" : false
        },

        {
            "mDataProp" : "title",
            "bSortable" : false
        },

        {
            "mDataProp" : "resourceTypeLabel",
            "bSortable" : false
        }, ];

        // If the user is an admin, they should see an additional column for status.
        // This shows if the resosrce is managed/unmanaged. Regular users don't see this status.
        // see edit.ftl for the HTML.
        console.log("the options are ", _options);

        if (_options.enableUnmanagedCollections) {
            aoColumns_.push({
                "mData" : null,
                render : fnRenderStatusColumn,
                "bSortable" : false
            });
        }

        aoColumns_.push({
            "mData" : null,
            render : fnRenderRemoveButtonsColumn,
            "bSortable" : false,
        });

        var selector = '#existing_resources_datatable';
        var $dataTable = $(selector);
        _registerCollectionLookupDataTable({
            tableSelector : selector,
            sAjaxSource : TDAR.uri('api/lookup/resource'),
            "bLengthChange" : true,
            "bFilter" : false,
            aoColumns : aoColumns_,
            "sDom" : "<'row'<'col-6'l><'pull-right col-3'r>>t<'row'<'col-4'i><'col-5'p>>", // no text filter!
            sAjaxDataProp : 'resources',
            "oLanguage" : {
                "sZeroRecords" : "No records found. <span id='fltrTxt'>Consider <a id='lnkResetFilters' href='javascript:void(0)'>expanding your search</a></span>"
            },

            requestCallback : function(searchBoxContents) {
                var parms = {
                    // These fields aren't used in request. They were used in the advanced options form that
                    // isn't used for existing resources.
                    title : searchBoxContents,
                    'resourceTypes' : "",
                    'includedStatuses' : "",
                    'sortField' : "",
                    'query' : $("#existing_res_query").val(),
                    'projectId' : "",

                    // These are sent.
                    'collectionId' : $("#metadataForm_id").val(),
                    selectResourcesFromCollectionid : options.selectResourcesFromCollectionid,
                    parentCollectionsIncluded : true,
                };

                if (!_options.isAdministrator && _options.limitContext == true) {
                    parms['useSubmitterContext'] = true;
                } else {
                    parms['useSubmitterContext'] = false;
                }

                return parms;
            },

            selectableRows : false,
            clickableRows : true,

            // Used to handle the pre-render
            fnRowCallback : function(nRow, obj, iDisplayIndex, iDisplayIndexFull) {

            },

            rowSelectionCallback : function(id, obj, isAdded, isManaged) {
                if (isAdded) {
                    _addResourceToVueModel(obj, $dataTable, isManaged);
                } else {
                    _removeResourceFromVueModel(obj, $dataTable, isManaged);
                }
            }

        });

        var $rdt = $("#existing_resources_datatable");

        $("#existing_res_query").change(function() {
            $rdt.dataTable().fnDraw();
        });

        $("#existing_res_query").bindWithDelay("keyup", function() {
            $rdt.dataTable().fnDraw();
        }, 500);

        _scrollOnPagination();
    }

    function _removePendingChange(id, isManaged, isAddition, $dataTable) {
        var array = "";
        var btnId = "";

        if (isManaged) {
            if (isAddition) {
                array = 'toAddManaged'
                btnId = "btnAddManagedId_";
            } else {
                array = 'toRemoveManaged';
                btnId = "btnRemoveManagedId_";
            }
        } else {

            if (isAddition) {
                array = 'toAddUnmanaged';
                btnId = "btnAddUnmanagedId_";
            } else {
                var array = 'toRemoveUnmanaged';
                btnId = "btnRemoveUnmanagedId_";
            }
        }

        console.log("Removing pending change from data");
        _arrayRemove($dataTable.data(array), parseInt(id));
        console.debug($dataTable);
        var buttonId = "#" + btnId + id;
        console.log("Reenabling button " + buttonId);
        $(buttonId).removeAttr("disabled");
        $dataTable.dataTable().fnDraw();
    }

    /**
     * Reset all filters, then trigger a 'change' so that we build a new query and render the results
     * 
     * @private
     */
    function _resetAllFilters() {
        // reset all dropdowns, but only trigger change for one of them (otherwise datatable may try to re-render N times)
        var $divSearchFilters = $(".div-search-filters");
        $divSearchFilters.find("input[type=checkbox]").prop("checked", false);
        $divSearchFilters.find("select").prop("selectedIndex", 0).last().change();

    }

    //
    /**
     * populate the dataTable.data('selectedRows') from the hidden inputs in #divSelectedResources (e.g. when rendering 'edit' or 'input' form)
     * 
     * @param dataTable
     *            datatable widget
     * @param resourcesTable
     *            the table that contains the selected datatable rows.
     * @private
     */
    function _registerResourceCollectionDataTable(dataTable, resourcesTable) {
        // if user is editing existing collection, gather the hidden elements and put them in the 'seleted rows' object
        var $dataTable = $(dataTable);
        var $resourcesTable = $(resourcesTable);
        var selectedRows = {};

        $.each($('input', '#divSelectedResources'), function() {
            var elem = this;
            selectedRows[elem.value] = {
                id : elem.value,
                title : 'n/a',
                description : 'n/a'
            };
        });
        $dataTable.data('selectedRows', selectedRows);

        // hide the selected items table if server hasn't prepopulated it
        if ($resourcesTable.find('tr').length == 1) {
            $resourcesTable.hide();
        }
    }

    /**
     * row selected callback. This callback constructs a table row for the "selected records" table
     * 
     * @param obj
     * @private
     */
    function _rowSelected(obj, $dataTable) {
        var $tableAdd = $("#tblToAdd");
        var $tableRemove = $("#tblToRemove");

        // remove tr, hidden field, id from the 'remove' lists, if present
        _arrayRemove($dataTable.data("toRemove"), obj.id);
        $("#trmod_" + obj.id).remove();

        // if the resource was part of the collection to begin with, do nothing
        if (obj.isSelectedResult) {
        } else {
            _addRow($dataTable, $tableAdd, "trmod_" + obj.id, obj, "toAdd");

        }
    }

    /**
     * row unselected callback: remove the row of the "selected records" table
     * 
     * @param obj
     * @private
     */
    function _rowUnselected(obj, $dataTable) {
        var $tableAdd = $("#tblToAdd");
        var $tableRemove = $("#tblToRemove");

        // remove tr, hidden field, id from the 'add' lists, if present
        _arrayRemove($dataTable.data("toAdd"), obj.id);
        $("#trmod_" + obj.id).remove();

        // if resource wasn't part of selection to begin with, do nothing
        if (obj.isSelectedResult) {
            // add the hidden input tag to the dom
            // next, add a new row to the 'selected items' table.
            _addRow($dataTable, $tableRemove, "trmod_" + obj.id, obj, "toRemove");
        } else {

        }
    }

    function _addRow($dataTable, $table, idattr, obj, action) {
        /**
         * Modification to use encapsulation and less dom manipulation -- Row contains hidden input, so removing the row, removes the element entirely
         */
        _arrayAdd($dataTable.data(action), obj.id);

        var $tr = $("<tr><td>" + obj.id + '<input type="hidden" name="' + action + '" value="' + obj.id + '" id="hrid' + obj.id + '">' + "</td><td>" +
                obj.title + "</td></tr>");
        $tr.attr("id", idattr);
        $table.append($tr);
    }

    /**
     * row selected callback. This callback constructs a table row for the "selected records" table
     * 
     * @param obj
     * @private
     */
    function _addResourceToVueModel(obj, $dataTable, isManaged) {
        _arrayRemove($dataTable.data("toRemove"), obj.id);

        if (isManaged && !obj.isManagedResult) {
            console.log("Adding managed resource to the vue model");
            // Instead of directly manipulating the vue model, this should invoke some method to be called.
            vm.managedAdditions.push(obj);
        } else if (!isManaged && !obj.isUnmanagedResult) {
            console.log("Adding umanaged resource to the vue model");
            vm.unmanagedAdditions.push(obj);
        }
    }

    /**
     * row unselected callback: remove the row of the "selected records" table
     * 
     * @param obj
     * @private
     */
    function _removeResourceFromVueModel(obj, $dataTable, isManaged) {
        if (isManaged && obj.isManagedResult) {
            console.log("Removing managed resource " + obj.id + " from the vue model");
            vm.managedRemovals.push(obj);
        } else if (!isManaged && obj.isUnmanagedResult) {
            console.log("Removing umanaged resource " + obj.id + " from the vue model");
            vm.unmanagedRemovals.push(obj);
        }
    }

    /**
     * pagination callback: this callback returns the vertical scroll position to the top of the page when the user navigates to a new page.
     * 
     * @private
     */
    function _scrollOnPagination() {
        $(".dataTables_paginate a").click(function() {
            $(".dataTables_scrollBody").animate({
                scrollTop : 0
            });
            return true;
        });
    }

    /**
     * Define sorting behavior when user clicks on datatable columns. Currency detection courtesy of Allan Jardine, Nuno Gomes
     * (http://legacy.datatables.net/plug-ins/type-detection)
     * 
     * @private
     */
    function _extendSorting() {
        // match anything that is not a currency symbol, seperator, or number (if some of these symbols appear identical then your IDE sucks)
        // if regex matches we assume it is definitly not a currency (e.g. "apple"), if false it *may* be a currency (e.g. "$3")
        var _reDetect = /[^$₠₡₢₣₤₥₦₧₨₩₪₫€₭₮₯₰₱₲₳₴₵¢₶0123456789.,-]/;

        // assuming we have detected a currency string, we use this regex to strip out symbols prior to sort
        var _rePrep = /[^-\d.]/g

        function _fnCurrencyDetect(sData) {
            var ret = null;
            if (typeof sData !== "string" || _reDetect.test(sData)) {
                ret = null;
            } else {
                ret = "tdar-currency";
            }
            return ret;
        }

        function _fnCurrencySortPrep(a) {
            a = (a === "-") ? 0 : a.replace(_rePrep, "");
            return parseFloat(a);
        }

        function _fnCurrencySortAsc(a, b) {
            return a - b;
        }

        function _fnCurrencySortDesc(a, b) {
            return b - a;
        }

        // add our custom type detector to the front of the line
        jQuery.fn.dataTableExt.aTypes.unshift(_fnCurrencyDetect);

        // register our custom sorters
        jQuery.fn.dataTableExt.oSort['tdar-currency-pre'] = _fnCurrencySortPrep;
        jQuery.fn.dataTableExt.oSort['tdar-currency-asc'] = _fnCurrencySortAsc;
        jQuery.fn.dataTableExt.oSort['tdar-currency-desc'] = _fnCurrencySortDesc;
    }

    function _fnRenderPersonId(a, b, oObj) {
        // in spite of name, aData is an object containing the resource record for this row
        var objResource = oObj;
        var html = '<a href="' + TDAR.uri('browse/creators/' + objResource.id) + '" class=\'title\'>' + objResource.id + '</a>';
        return html;
    }
    function _registerUserLookupDatatable() {
        var settings = {
            tableSelector : '#dataTable',
            sAjaxSource : TDAR.uri() + 'api/lookup/person',
            "sDom" : "<'row'<'col-6'l><'col-6'f>r>t<'row'<'col-7'i><'col-5'p>>",
            "bLengthChange" : true,
            "bFilter" : true,
            sAjaxDataProp : 'people',
            selectableRows : false,
            clickableRows : false,
            aoColumns : [ {
                sTitle : "id",
                bUseRendered : false,
                mDataProp : "id",
                tdarSortOption : 'ID',
                bSortable : false,
                render : TDAR.datatable.renderPersonId
            }, {
                sTitle : "First",
                mDataProp : "firstName",
                tdarSortOption : 'FIRST_NAME',
                bSortable : false
            }, {
                sTitle : "Last",
                mDataProp : "lastName",
                tdarSortOption : 'LAST_NAME',
                bSortable : false
            }, {
                sTitle : "Email",
                mDataProp : "email",
                tdarSortOption : 'CREATOR_EMAIL',
                bSortable : false
            } ],
            requestCallback : function() {
                return {
                    minLookupLength : 0,
                    registered : 'true',
                    term : $("#dataTable_filter input").val()
                };
            }
        };

        return TDAR.datatable.registerLookupDataTable(settings);
    }

    function _checkAllToggle() {
        var unchecked = $('#resource_datatable td input[type=checkbox]:unchecked');
        var checked = $('#resource_datatable td input[type=checkbox]:checked');
        if (unchecked.length > 0) {
            $(unchecked).click();
        } else {
            $(checked).click();
        }
    }

    function _registerChild(id, title) {
        var _windowOpener = null;
        // swallow cors exception. this can happen if window is a child but not an adhoc target
        try {
            if (window.opener) {
                _windowOpener = window.opener.TDAR.common.adhocTarget;
            }
        } catch (ex) {
            // console.log("window parent not available - skipping adhoctarget check");
        }

        if (_windowOpener) {
            window.opener.TDAR.common.populateTarget({
                id : id,
                title : title
            });

            $("#datatable-child").dialog({
                resizable : false,
                modal : true,
                buttons : {
                    "Return to original page" : function() {
                        window.opener.focus();
                        window.close();
                    },
                    "Stay on this page" : function() {
                        window.opener.adhocTarget = null;
                        $(this).dialog("close");
                    }
                }
            });
        }
    }

    function _initializeCollectionAddRemove(id) {
        // dont allow submit until collection contents fully initialized.
        // $(".submitButton").prop("disabled", true);
        var $datatable = $("#resource_datatable");
        var $container = $("#divNoticeContainer");

        $datatable.on("change", ".datatable-checkbox.project", function() {
            if ($container.is(":visible")) {
                return;
            }
            if ($(this).is(":checked")) {
                $container.show();
            }
        });
    }

    function _getBasicConfig(url, columns, dataArrayName) {
        var config = {
            "processing" : true,
            "serverSide" : true,
            "destroy" : true,
            "ordering" : false,
            "searching" : false,
            "info" : true,
            "columns" : columns,
            "autoWidth" : false,
            "sDom" : "<'row'<'col-12'l><'float-right col-3'r>><'row'<'col-12't>><'row'<'col-4'i><'col-5'p>>", // no text filter!
            "ajax" : {
                "url" : url,
                "dataType" : "jsonp",

                "data" : function(d, s) {
                    console.log(d, s);
                    var val = '';
                    if (d.search != undefined && d.search.value == undefined) {
                        val = d.search.value;
                    }
                    return {
                        startRecord : d.start,
                        recordsPerPage : d.length,
                        term : val

                    }
                },
                "dataSrc" : function(json) {
                    if (json.status != undefined) {
                        json.recordsTotal = json.status.totalRecords;
                        json.recordsFiltered = json.status.totalRecords;
                    }
                    if (json.totalRecords != undefined) {
                        json.recordsTotal = json.totalRecords;
                        json.recordsFiltered = json.totalRecords;
                    }
                    console.log(json);
                    return json[dataArrayName];

                }
            }

        }
        return config;
    }

    var viewDataTableInitialized = false;
    function _initalizeResourceDatasetDataTable(columns, viewRowSupported, resourceId, namespace, dataTableId) {
        jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = 3;
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper" : "dataTables_wrapper"
        });
        var offset = 0;
        var browseUrl = TDAR.uri("datatable/browse?id=" + dataTableId);
        var size = 0;
        var cols = [];
        var th = "";


        columns.forEach(function(f) {
            var col = {
                simpleName : f.displayName,
                "targets" : offset,
                "data" : offset
            };
            
            if (viewRowSupported) {
                if (f.displayName == 'Row Id') {
                    col.render = function(data, type, row, meta) {
                        row.unshift("");
                        // console.log(row);
                        var url = TDAR.uri(namespace + '/row/' + resourceId + '/' + dataTableId + '/' + row[1]);
                        return '<a href="' + url + '" title="View row as page..."><i class="fas fa-table"></i></a> ' + data +' </li>';
                    };
                }
            }
            cols.push(col);
            offset++;
            size++;
            th = th + "<th>" + f.displayName + "</th>";
        });
        console.log(cols);
        var config = _getBasicConfig(browseUrl, cols, "results");
        config.tableSelector = "#view-data-table";
        var $table = $("#view-data-table");
        console.log(size, $table.DataTable.isDataTable());
        if (size > 0) {
            if (viewDataTableInitialized) {
                console.log("destroying...");
                var dt = $table.dataTable();
                dt.fnDestroy();
            }
        $table.empty();
        $table.append("<tbody>");
        $table.append("<thead class='thead-dark'>");
        $table.children("thead").append(th);
        $table.dataTable(config);
        _scrollOnPagination();
        viewDataTableInitialized = true;
        }
    }

    /**
     * 
     * @param url
     * @param dataTableId
     * @returns {*} promise of array of columnInfo objects
     * @private
     */
    function _loadDatasetTableMetadata(url, dataTableId) {
        var data = {
            id : dataTableId,
            startRecord : 0,
            recordsPerPage : 1
        };
        var promise = $.get(url, data, "jsonp");
        return promise;
    }

    /**
     * add item to array if not found in array. returns undef
     * 
     * @param arr
     * @param item
     * @private
     */
    function _arrayAdd(arr, item) {
        if (arr.indexOf(item) === -1) {
            arr.push(item);
        }
    }

    /**
     * remove item from array if found. return undef
     * 
     * @param arr
     * @param item
     * @private
     */
    function _arrayRemove(arr, item) {
        var idx = arr.indexOf(item);
        if (idx !== -1) {
            arr.splice(idx, 1);
        }
    }

    /**
     * Initialize dataset datatable browser. This method accepts no arguments, but requires configuration data from data-attributes. Requirements:
     *  - <table> must have have ID of 'dataTable' - attribute data-data-table-selector: selector to <select> dropdown that specifies the ID of the dataTable to
     * render. Will update on change events. - attribute data-default-data-table-id: ID of dataTable to render if no <select> dropdown exists
     * 
     * 
     * @private
     */
    function _initDataTableBrowser() {
        var config = $('#view-data-table').data();
        if (!config)
            return;
        var $select = $(config.dataTableSelector);
        var $dataTable = $('#view-data-table');

        var dataTableWidget = null;

        var _loadThenInit = function(dataTableId, namespace) {
            // get the column schema for the default dataTable
            if (dataTableWidget) {
                $dataTable.empty();
                $dataTable.append("tbody");
                $dataTable.append("thead");
                $dataTable.DataTable().destroy();
                dataTableWidget = undefined;
            }
            var columnsPromise = _loadDatasetTableMetadata('/datatable/browse', dataTableId);
            columnsPromise.done(function(data) {
                var columns = data.fields.map(function(item) {
                    return {
                        simpleName : item.name,
                        displayName : item.displayName
                    };
                });
                dataTableWidget = _initalizeResourceDatasetDataTable(columns, true, config.resourceId, namespace, dataTableId);
            });

        }

        // if dataset has many tables, load & init the table every time user changes the dropdown
        if ($select.length) {
            $select.change(function() {
                _loadThenInit($select.val(), $("#browseTable").data("namespace"));
            }).change();
        }
        // if only one dataset, just load+init the table once
        else {
            _loadThenInit(config.defaultDataTableId, $("#browseTable").data("namespace"));
        }

    }

    return {
        extendSorting : _extendSorting,
        registerLookupDataTable : _registerLookupDataTable,
        initUserDataTable : _registerUserLookupDatatable,
        setupDashboardDataTable : _setupDashboardDataTable,
        setupCollectionDataTable : _setupCollectionDataTable,
        setupCollectionResourcesDataTable : _setupCollectionResourcesDataTable,
        registerResourceCollectionDataTable : _registerResourceCollectionDataTable,
        renderPersonId : _fnRenderPersonId,
        checkAllToggle : _checkAllToggle,
        registerChild : _registerChild,
        initalizeResourceDatasetDataTable : _initalizeResourceDatasetDataTable,
        registerAddRemoveSection : _initializeCollectionAddRemove,
        initDataTableBrowser : _initDataTableBrowser,
        removePendingChange : _removePendingChange
    };
}();

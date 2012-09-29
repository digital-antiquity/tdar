
function registerLookupDataTable(parms) {

    //tableSelector, sAjaxSource, sAjaxDataProp,  aoColumns, requestCallback, selectableRows
    var doNothingCallback = function(){};
    var options = {
            tableSelector: '#dataTable',
            requestCallback: doNothingCallback,
            selectableRows: false,
            rowSelectionCallback: doNothingCallback,
            "sAjaxSource": '/lookup/resource',
            "sAjaxDataProp": 'resources',
            "bJQueryUI": false,
            "sScrollY": "350px",
            fnDrawCallback: function(){
            	//if all checkboxes are checked, the 'select all' box should also be checked, and unchecked in all other situations
            	if($(":checkbox:not(:checked)", $dataTable).length == 0) {
                	$('#cbCheckAllToggle').prop('checked', true);
            	} else {
            		$('#cbCheckAllToggle').prop('checked', false);
            	}
            }
    };
    
    $.extend(options, parms);
    var $dataTable = $(options.tableSelector);
    
    //here is where we will store the selected rows (if caller wants to track that stuff)
    $dataTable.data('selectedRows', {});

    var dataTableOptions ={
            "bProcessing": true,
            "bServerSide": true,
            //"sAjaxDataProp": sAjaxDataProp,
            //"aoColumns": aoColumns

            // intercept the server request, and translate the parameters to server
            // format. similarly, take the json returned by the jserver
            // and translate to format expected by the client.
            "fnServerData": function _fnServerData(sSource, aoData, fnCallback) {
                
                $.ajax({
                	traditional: true, //please don't convert my arrays to php arrays.  php is dumb.
                    dataType : 'jsonp',
                    url : sSource,
                    data : _convertRequest(aoData, options.aoColumns, options.requestCallback),
                    success : function(_data) {
                        //intercept data returned by server, translate to client format
                        var recordInfo = {
                                iTotalDisplayRecords: _data.status.totalRecords,
                                iTotalRecords: _data.status.totalRecords    
                        };
                        $.extend(_data, recordInfo);
                        fnCallback(_data);
                    },
                    error : function(jqXHR, textStatus, errorThrown) {
                        console.error("ajax query failed:" + errorThrown);
                    }
                });
            }
    };

    //if user wants selectable rows,   render checkbox in the first column (which we assume is an ID field)
    if(options.selectableRows) {
        options.aoColumns[0].fnRender = fnRenderIdColumn;
        options.aoColumns[0].bUseRendered = false;
        dataTableOptions["fnRowCallback"] =  function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
            //determine whether the user selected this item already (if so check the box)
            var $cb = $(nRow).find('input[type=checkbox]'); 
            var id = $cb.val();
            if($dataTable.data('selectedRows')[id]) {
                $cb.prop('checked', true);
            }
            return nRow;
        };
        
        //register datatable checkbox changes.  maintain a hashtable of all of the currently selected items.
        //call the rowSelectionCallback whenever something changes
        $dataTable.delegate('input[type=checkbox]', 'change', function() {
            var $elem = $(this); //here 'this' is checkbox 
            var id = $elem.val();
            var objRowData = $dataTable.fnGetData($elem.parents('tr')[0]);
            if($elem.prop('checked')) {
                //get the json data associated w/ the selected row, put it in selectedRows
                $dataTable.data('selectedRows')[id] = objRowData;
                options.rowSelectionCallback(id, objRowData, true);
            } else {
                delete $dataTable.data('selectedRows')[id]; //unchecked, so remove from the hashtable
                options.rowSelectionCallback(id, objRowData, false);
            }
            
        });
    }
    
    //put any user-specified dataTable options that have been specified in the parms into the dataTableOptions
    $.extend(options, dataTableOptions);

    $dataTable.dataTable(options);
    
    return $dataTable;
}

//prepare request data to be sent to tdar lookup request.  This function will derive the startpage, recordsPerPage, and sortField
//any additional data to be sent to server should be returned by requestCallback(sSearch)  where sSearch is the search term entered
//in the datatable search box (if any).
function _convertRequest(aoData, aoColumns, requestCallback) {
    var oData = {};
    //first convert the request from array of key/val pairs to map<string,string>.
    $.each(aoData, function(){
        oData[this.name] = this.value;
    });

    //derive sort column from the field name and reversed status
    var tdarSortOption = aoColumns[oData["iSortCol_0"]].tdarSortOption;
    var sSortReversed = {desc:'true'}[oData["sSortDir_0"]];
    if(sSortReversed) tdarSortOption += '_REVERSE';
    var translatedData = {
            startRecord:oData.iDisplayStart,
            recordsPerPage:oData.iDisplayLength,
//            minLookupLength:0,
            sortField: tdarSortOption
    };
    
    $.extend(translatedData, requestCallback(oData.sSearch));

    return translatedData;
}


function fnRenderIdColumn(oObj) {
    //in spite of the name, aData is an object corresponding to the current row 
    var id = oObj.aData.id;
    var attrId = "cbEntityId_" + id;
    return '<input type="checkbox" id="' + attrId + '" value="' + id + '" />' + 
            '<label class="datatable-cell-unstyled" for="' + attrId + '">' + id + '</label>' ;
}





//FIXME: selectableRows is redundant -- let rowSelectionCallback implicitly indicate that we want selectable rows.
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

    var dataTableOptions = {
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


function projToolbarItem(link, image, text) {
    return '<li><a href="' + link + '"><img alt="toolbar item" src="' + image + '"/>' + text + '</a></li>';
 }
 



function drawToolbar(projId) {
    var toolbar = $("#proj-toolbar");
    toolbar.empty();
    if (projId != undefined && projId != '') {
        toolbar.append(projToolbarItem('/project/' + projId + '/view', '/images/zoom.png', ' View selected project'));
        toolbar.append(projToolbarItem('/project/' + projId + '/edit', '/images/pencil.png', ' Edit project'));
        toolbar.append(projToolbarItem('/resource/add?projectId=' + projId, '/images/database_add.png', ' Add new resource to project'));
    }
}

function fnRenderTitle(oObj) {
    //in spite of name, aData is an object containing the resource record for this row
    var objResource = oObj.aData;
    var html = '<a href="'  + getURI(objResource.urlNamespace + '/' + objResource.id) + '" class=\'title\'>' + htmlEncode(objResource.title) + '</a>';
    html += ' (ID: ' + objResource.id 
    if (objResource.status != 'ACTIVE') {
    html += " " + objResource.status;
    }
    html += ')';
    if (datatable_showDescription) {
        html += '<br /> <p>' + htmlEncode(elipsify(objResource.description,80)) + '</p>';
    }; 
    return html;
}


function setupDashboardDataTable() {
    // set the project selector to the last project viewed from this page
    // if not found, then select the first item 
    var prevSelected = $.cookie("tdar_datatable_selected_project");
    if (prevSelected != null) {
        var elem = $('#project-selector option[value=' + prevSelected + ']');
        if(elem.length) {
            elem.attr("selected", "selected");
        } else {
            $("#project-selector").find("option :first").attr("selected", "selected");
        }

    }
    drawToolbar($("#project-selector").val());
    var prevSelected = $.cookie("tdar_datatable_selected_collection");
    if (prevSelected != null) {
        var elem = $('#collection-selector option[value=' + prevSelected + ']');
        if(elem.length) {
            elem.attr("selected", "selected");
        } else {
            $("#collection-selector").find("option :first").attr("selected", "selected");
        }

    }

    jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages =3;
    $.extend( $.fn.dataTableExt.oStdClasses, {
        "sWrapper": "dataTables_wrapper form-inline"
    } );
//        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
        
      var aoColumns_ = [{ "mDataProp": "title",  sWidth: '65%', fnRender: fnRenderTitle, bUseRendered:false ,"bSortable":false},
          { "mDataProp": "resourceTypeLabel",  sWidth: '15%',"bSortable":false }];
          if (datatable_isSelectable) {
          aoColumns_[2] = { "mDataProp": "id", tdarSortOption: "ID", sWidth:'5em' ,"bSortable":false};
          };
    $dataTable = registerLookupDataTable({
        tableSelector: '#resource_datatable',
        sAjaxSource:'/lookup/resource',
        "bLengthChange": true,
        "bFilter": false,
        aoColumns: aoColumns_,
        //"sDom": "<'row'<'span9'l><'span6'f>r>t<'row'<'span4'i><'span5'p>>",  
        "sDom": "<'row'<'span6'l><'pull-right span3'r>>t<'row'<'span4'i><'span5'p>>",  //no text filter!
        sPaginationType:"bootstrap",
        sAjaxDataProp: 'resources',
        requestCallback: function(searchBoxContents){
                return {title: searchBoxContents,
                    'resourceTypes': $("#resourceTypes").val() == undefined ? "" : $("#resourceTypes").val(),
                    'includedStatuses': $("#statuses").val() == undefined ? "" : $("#statuses").val() ,
                    'sortField':$("#sortBy").val(),
                    'term':$("#query").val(),
                    'projectId':$("#project-selector").val(),
                    'collectionId':$("#collection-selector").val(),
                     useSubmitterContext: !datatable_isAdministrator
            }
        },
        selectableRows: datatable_isSelectable,
        rowSelectionCallback: function(id, obj, isAdded){
            if(isAdded) {
                _rowSelected(obj);
            } else {
                _rowUnselected(obj);
            }
        }
    });

    $("#project-selector").change(function() {
        var projId = $(this).val();
        $.cookie("tdar_datatable_selected_project", projId);
        drawToolbar(projId);
        $("#resource_datatable").dataTable().fnDraw();
    });

    $("#collection-selector").change(function() {
        var projId = $(this).val();
        $.cookie("tdar_datatable_selected_collection", projId);
        drawToolbar(projId);
        $("#resource_datatable").dataTable().fnDraw();
    });
    
    $("#resourceTypes").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });


    $("#statuses").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#sortBy").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#query").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#query").bindWithDelay("keyup", function() {$("#resource_datatable").dataTable().fnDraw();} ,500);
}


function registerResourceCollectionDataTable() {
    //if user is editing existing collection, gather the hidden elements and put them in the 'seleted rows' object
    var selectedRows = {};
    $.each($('input', '#divSelectedResources'), function(ignored, item){
        var elem = this;
        selectedRows[elem.value] = {id:elem.value, title:'n/a', description:'n/a'};
        console.debug('adding id to preselected rows:' + elem.value);  
    });
    $dataTable.data('selectedRows', selectedRows);
    
    //hide the selected items table if server hasn't prepopulated it
    var $table = $('#tblCollectionResources');
    if($table.find('tr').length==1) {
        $table.hide();
    }
}


function _rowSelected(obj) {

    //first, add the hidden input tag to the dom
    var tag = '<input type="hidden" name="resources.id" value="' + obj.id + '" id="hdnResourceId' + obj.id + '"/>';
    console.log("adding selected resource:" + tag);
    $('#divSelectedResources').append(tag);

    //next, add a new row to the 'selected items' table.
    var $table = $('#tblCollectionResources');
    var $tbody = $('tbody', $table);
    var resourceTag = '';
        resourceTag += '<tr id="dataTableRow_:id">                                                                   ';
        resourceTag += '    <td>:id</td>                                                                             ';
        resourceTag += '    <td>                                                                                      ';
        resourceTag += '        <a href="/:urlNamespace/:id" target="resourcedetail" >                                    ';
        resourceTag += '            :title        ';
        resourceTag += '        </a>                                                                                  ';
        resourceTag += '    </td>                                                                                     ';
        resourceTag += '    <td><button class="btn btn-mini"  type="button" tabindex="-1" onclick="_removeResourceClicked(:id, this);false;"><i class="icon-trash"></i></button></td>';
        resourceTag += '</tr>                                                                                         ';

       resourceTag = resourceTag.replace(/:id/g, obj.id);
       resourceTag = resourceTag.replace(/:urlNamespace/g, obj.urlNamespace);
       resourceTag = resourceTag.replace(/:title/g, obj.title);
       resourceTag = resourceTag.replace(/:description/g, obj.description);
       resourceTag = resourceTag.replace(/:status/g, obj.status);
       
       $tbody.append(resourceTag);
       //$table.closest('div').show();
       $table.show();
}

function _rowUnselected(obj) {
    console.log('removing selected reosurce:' + obj.id);
    $('#hdnResourceId' + obj.id).remove();
    
    var $row = $('#dataTableRow_' + obj.id);
    var $table = $row.closest('table');
    //var $div = $row.closest('div');
    $row.remove();
    if($table.find('tr').length == 1) $table.hide(); //FIXME: DRY

}

function _removeResourceClicked(id, elem) {
    //delete the element from the selectedrows structure and remove the hidden input tag
    delete $dataTable.data('selectedRows')[id];
    $('#hdnResourceId' + id).remove();
    
    //now delete the row from the table
    var $elem = $(elem);
    var $tr = $elem.closest('tr');  
    var $div = $elem.closest('div');
    $tr.remove();
    
    //if the table is empty,  hide the section
    if($('tr', $div).length == 1) { //one header row
        //$div.hide();
        $table.hide();
    }
    
    //if the datatable is on a page that shows the corresponding checkbox,  clear the checkbox it
    $('#cbEntityId_' + id, $dataTable).prop('checked', false);
    
}

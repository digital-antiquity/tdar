<#setting url_escaping_charset="UTF-8">
<#global angular_version="1.5.5">

<head>
    <title>Dataset Integration: Edit</title>
</head>
<body>

<div id="divIntegrationMain" ng-controller="IntegrationController as ctrl"
     data-read-only="${(!editable)?string}"
     data-max-data-tables="${maxDataTables?c}"
     data-max-output-columns="${maxOutputColumns?c}"
>
    <div id="divIntegrationHeader">
        <h2>Dataset Integration</h2>
    </div>
    <div id="divStatusMessage" class="alert alert-{{alert.kind}}" ng-show="alert.message !==''" >
    {{alert.message}}
    </div>

    <form id="frmIntegrationEdit" class="form-horizontal form-condensed" ng-init="ctrl.loadJSON()">
        <div class="row">
            <div class="col-10">
               <div class="control-group row">
                   <label class="col-form-label col-2">
                       Integration Name
                   </label>
                       <input type="text" class="col-10 form-control" name="integration.title" ng-model="ctrl.integration.title" ng-disabled="isReadOnly()">
               </div>
               <div class="control-group row">
                   <label class="col-form-label col-2">Description</label>
                       <textarea name="integration.description" class="col-10 form-control" cols="80" rows="2" ng-disabled="isReadOnly()"
                                 ng-model="ctrl.integration.description"></textarea>
               </div>
            </div>
            <div class="col-2">
                <button type="button" class="btn btn-primary btn-primary-integrate" ng-disabled="!isValid()" id="btnIntegrate" ng-click="ctrl.submitIntegration()">Integrate</button>
                <#--<button type="button" class="btn btn-primary" ng-disabled="!isValid()" id="btnIntegrate" ng-click="ctrl.integrateClicked()">Integrate</button>-->

                <!-- re enable ignore-ng-disabled when TDAR-4367 is fixed -->
                <!-- Split button -->
                <div class="btn-group">
                  <button type="button" class="btn" ignore-ng-disabled="!isMinimallyValid()" ng-disabled="isReadOnly() || !isValid()"  id="btnSave" ng-click="ctrl.saveClicked()">Save</button>
                  <#--
                  <button type="button" class="btn dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu" role="menu">
                    <li>
                    <a ignore-ng-disabled="!isMinimallyValid()" ng-disabled="!isValid()"  id="btnSaveAs" ng-click="ctrl.saveAsClicked()">Save As</a>
                  </ul>
                  -->
                </div>
            </div>
        </div>

        <div id="divSelectedItemsSection">
            <div class="row">
            	<div class="col-10">
            		<div class="row">
                        <label class="col-form-label col-2">Datasets &amp; Ontologies</label>
                        <div class="col-10">
                            <div class="row">
                            <div class="col-6">
                                <div class="row">
                                <div class="col-12">
                                            <b>Selected Datasets</b>
                                                <div class="alert alert-warn" ng-show="ctrl.integration.dataTables.length > maxDataTables" >
                                                    Please use less than {{maxDataTables}} datasets.
                                                </div>

                                                <button type="button" class="btn btn-sm"  id="btnAddDataset" ng-disabled="isReadOnly() || isBusy()"
                                                        ng-click="ctrl.addDatasetsClicked()">Add Datasets...</button>
                                                        <hr/>
                                    <ul class="list-unstyled">
                                        <li ng-repeat="dt in ctrl.integration.dataTables" class="list-unstyled available-datasets">
                                        <div class="row">  
                                            <div class="col-1">
                                            <span class="badge bg-red ">{{$index + 1}}</span>
                                            </div>
                                            <div class="col-9">
                                                {{dt|dtDisplayName|titleCase}}
                                                <a href="/dataset/{{dt.datasetId}}" target="_blank" >({{dt.datasetId}})</a>
                                            </div>
                                            <div class="col-1">
                                                <a class="btn btn-sm delete-button " href="#" ng-click="ctrl.removeDatatableClicked(dt)" ng-disabled="isReadOnly()">X</a>
                                            </div>
                                        </div>
                                        </li>
                                </ul>
                                </div>
                                </div>
                                
                        </div>
                            <div class="col-6">
                                <div class="row">
                                <div class="col-12">
                                        <b>Available Ontologies</b>
                                        <hr/>
                                        <ul class="list-unstyled">
                                        
                                        <li class="list-unstyled available-ontologies sharedOntologies" ng-repeat="ontology in ctrl.integration.ontologies">
                                                {{ontology | ontDisplayName}}
                                                <a href="/ontology/{{ontology.id}}" target="_blank">({{ontology.id}})</a>
                                        </li>
                                </ul>

                            </div>
                            </div>
                            </div>
                            </div>
                        </div>
                    </div>
            </div>
        </div>
        </div>
        </form>
        <form>
                <div id="divActionsSection">
                    <div class="control-group">
                        <label class="col-form-label">Actions</label>
                        <div class="controls">
                            <div id="divStatusMessage" class="alert alert-warn" ng-show="ctrl.integration.columns.length > maxOutputColumns" >
                                Please use less than {{maxOutputColumns}} output columns.
                            </div>

                            <div class="btn-group">
                                    <a class="btn btn-outline-secondary btn-sm dropdown-toggle" data-toggle="dropdown" href="#" ng-class="{disabled: !ctrl.integration.ontologies.length || isBusy() || isReadOnly() }">
                                        Add Integration Column
                                        <span class="caret"></span>
                                    </a>
                                    <ul class="dropdown-menu" id="btnSetAddIntegrationColumns">
                                        <li ng-repeat="ontology in ctrl.integration.ontologies"
                                                ><a class="dropdown-item" ng-disabled="isReadOnly()" ng-click="ctrl.addIntegrationColumnsMenuItemClicked(ontology)">{{ontology.title}}</a></li>
                                    </ul>
                                <button type="button" class="btn btn-outline-secondary btn-sm" id="btnAddDisplayColumn"
                                        ng-click="ctrl.addDisplayColumnClicked()"
                                        ng-disabled="!ctrl.integration.ontologies.length || isBusy() || isReadOnly()"
                                        >Add Display Column</button>

                                <button type="button" class="btn btn-outline-secondary btn-sm" id="btnAddCountColumn"
                                        ng-click="ctrl.addCountColumnClicked()"
                                        ng-disabled="ctrl.isCountColumnDisabled() || isBusy() || isReadOnly()"
                                        >Add Count Column</button>
                            </div>

                        </div>
                    </div>
        </div>
        
        <div id="divColumnSection">
            <div class="row">
                <div class="col-12">

                    <div class="control-group" ng-show="ctrl.integration.columns.length">
                        <label class="col-form-label">
                            Configure Columns
                        </label>
                        <div class="controls">

                            <div id="tabControl">
                                <ul class="nav nav-tabs" id="myTab" role="tablist">
                                    <li ng-repeat="column in ctrl.integration.columns" ng-click="ctrl.setTab($index)" onclick="return false;" class="nav-item" >
                                        <a href="#tab{{$index}}" id="tabtab{{$index}}" ng-class="{active: ctrl.isTabSet($index), 'nav-link': true}">
                                        {{column.name}}
                                            <input type="hidden" name="column.name{{$index}}" ng-model="column.name" />
                                            <button class="ml-2 close" ng-click="ctrl.closeTab($index)">x</button>
                                        </a>
                                    </li>
                                </ul>

                                <div class="tab-content" >
                                    <div class="tab-pane" id="tab{{$index}}"
                                         ng-repeat="outputColumn in ctrl.integration.columns" ng-class="{active: ctrl.isTabSet($index)}">
                                        <div ng-switch="outputColumn.type">

                                            <div ng-switch-when="integration" class=".integration-pane-content">
                                                <div class="alert" ng-hide="outputColumn.isValidMapping">
                                                    <strong>Invalid Ontology</strong> {{outputColumn.ontology | ontDisplayName}} does not belong to an available ontology.
                                                </div>
                                                <table class="table table-bordered table-condensed table-hover">
                                                      <thead class="thead-dark">

                                                    <tr>
                                                        <th rowspan="2" style="white-space: nowrap;">&nbsp;</th>
                                                        <th rowspan="2" style="width:99%">
                                                            <div class="pull-right">
                                                                Select values that appear in
                                                                <div class="btn-group">
                                                                    <button class="btn btn-sm" type="button" ng-click="selectMatchingNodes('some')">Any column</button>
                                                                    <button class="btn btn-sm" type="button" ng-click="selectMatchingNodes('every')">Every column</button>
                                                                </div>
                                                            </div>
                                                            <div>Node Value</div>
                                                        </th>
                                                        <th rowspan="1" style="white-space: nowrap;" colspan="{{outputColumn.selectedDataTableColumns.length}}">
                                                            Datasets
                                                        </th>
                                                    </tr>
                                                    <tr>
                                                        <th ng-repeat="cc in lookupCompatibleColumns(outputColumn.ontologyId)" style="min-width: 2em;" >
                                                            <!-- suggest using  track by c.name to get at a key that we can more easily use" -->
                                                            <div class="text-center">
                                                                <span data-content="{{cc.dataTable|dtDisplayName|titleCase}}" class="badge bg-red" popover>{{$index + 1}}</span>



                                                                <div ng-switch on="cc.compatCols.length">
                                                                <div ng-switch-when="1">

                                                                    <!-- <span title="{{cc.dataTable.datasetName}} :: {{cc.dataTable.displayName}}">{{cc.compatCols[0].displayName}}</span> -->
                                                                    <!-- FIXME: this is "hidden", but is it even needed? -->
                                                                    <select class="intcol form-control" ng-model="outputColumn.selectedDataTableColumns[$index].dataTableColumn" ng-options="c.displayName for c in cc.compatCols" ng-hide="true"></select>
                                                                </div>
                                                                <div ng-switch-when="0"></div>
                                                                <div ng-switch-default>
                                                                    <select class="form-control" title="{{cc.dataTable.datasetName}} :: {{cc.dataTable.displayName}}" class="intcol" ng-model="outputColumn.selectedDataTableColumns[$index].dataTableColumn" ng-options="c.displayName for c in cc.compatCols"></select>
                                                                </div>
                                                                </div>
                                                            </div>
                                                        </th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    <tr ng-repeat="nodeSelection in outputColumn.nodeSelections" ng-init="nodeIndex = $index">
                                                        <td><input ng-disabled="isReadOnly()" type="checkbox" name="cbont" ng-model="nodeSelection.selected" id="cbont_{{::nodeSelection.node.id}}"></td>
                                                        <td style="white-space: nowrap;">
                                                            <div class="nodechild{{::nodeSelection.node.index.split('.').length}}">
                                                                <label for="cbont_{{::nodeSelection.node.id}}">{{::nodeSelection.node.displayName}}</label>
                                                            </div>
                                                        </td>
                                                        <td ng-repeat="columnSelection in outputColumn.selectedDataTableColumns track by $index">
                                                            <div class="text-center">
                                                                <i class="fas fa-check" id="cbx-{{::columnSelection.dataTableColumn.id}}-{{::nodeSelection.node.id}}" ng-show="::ontologyValuePresent(columnSelection.dataTableColumn, nodeSelection.node)"></i>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                            </div>

                                            <div ng-switch-when="display" class=".display-pane-content">
                                            
                                                <h3>Choose the source columns to include in this display column </h3>
                                                <table class="table table-sm table-striped">
                                                      <thead class="thead-dark">

                                                        <tr>
                                                            <th>Dataset</th>
                                                            <th>Table</th>
                                                            <th>Column</th>
                                                        </tr>
                                                    </thead>
                                                    <#--<tr ng-repeat="columnSelection in outputColumn.dataTableColumnSelections">-->
                                                    <tr ng-repeat="dataTable in ctrl.integration.dataTables" ng-init="columnSelection = outputColumn.dataTableColumnSelections[$index]">
                                                        <td class="">{{dataTable.datasetName}}</td>
                                                        <td class="">{{dataTable.displayName}}</td>
                                                        <td>
                                                            <select ng-model="columnSelection.dataTableColumn" id="dt_{{$parent.$index}}_{{dataTable.id}}"
                                                                    ng-options="c.displayName for c in dataTable.dataTableColumns  | orderBy: 'displayName' ">
                                                                <option value="" class="emptyoption">No column selected</option>
                                                            </select>
                                                        </td>
                                                    </tr>

                                                </table>
                                            </div>


                                            <div ng-switch-when="count" class=".count-pane-content">
                                                <h3>Select count columns </h3>
                                                <table class="table table-sm table-striped">
                                                      <thead class="thead-dark">

                                                        <tr>
                                                            <th>Dataset</th>
                                                            <th>Table</th>
                                                            <th>Column</th>
                                                        </tr>
                                                    </thead>
                                                    <#--<tr ng-repeat="columnSelection in outputColumn.dataTableColumnSelections">-->
                                                    <tr ng-repeat="dataTable in ctrl.integration.dataTables" ng-init="columnSelection = outputColumn.dataTableColumnSelections[$index]">
                                                        <th class="">{{dataTable.datasetName}}</th>
                                                        <th class="">{{dataTable.displayName}}</th>
                                                        <td>
                                                            <select ng-model="columnSelection.dataTableColumn"
                                                                    ng-options="c.displayName for c in dataTable.dataTableColumns  | orderBy: 'displayName'  | filter:filterCount">
                                                                <option value="" class="emptyoption">No column selected</option>
                                                            </select>
                                                        </td>
                                                    </tr>

                                                </table>
                                            </div>


                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
            </div>
        </div>
    </form>

    <script type="application/json" id="jsondata" raw-data>
    ${workflowJson!"{}"}
    </script>

    <!-- FIXME: embedded lookup data like this will be untenable for large datasets - use ajax solution instead -->
    <!-- FIXME: too much crap - we just need ID and title and submitterId -->
    <script type="application/json" id="allProjects" raw-data>
    ${fullUserProjectsJson}
    </script>

    <script type="application/json" id="allCollections" raw-data>
    ${allResourceCollectionsJson}
    </script>

    <script type="application/json" id="allCategories" raw-data>
    ${categoriesJson}
    </script>
    <script src='https://ajax.googleapis.com/ajax/libs/angularjs/${angular_version}/angular.min.js'></script>


</div>

<tdar-modal ng-controller="ModalDialogController"></tdar-modal>

<!-- Note: this modal is about col-10 wide. Form-horizontal labels are ~col-3 wide, leaving you ~col-7 for controls. -->
<script type="text/ng-template" id="workspace/modal-dialog.html">
    <div id="divModalContainer" class="modal  " tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">

		<div class="modal-content">
        <div class="modal-header alert-info">
            <div id="#modalAjaxIndicator" class="fload-right">
                <span class="small" ng-show="modalSearching">Searching</span>
            </div>
            <h3 id="hModalHeader">{{title}}</h3>
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        </div>
        <div class="modal-body">
            <div class="row">
                <div class="col-12">
                    <form id="frmModal" class="form-horizontal form-condensed" ng-model-options="{ updateOn: 'default blur', debounce: {'default': 500, 'blur':0, 'click':0} }">
                        <div>
                            <div class="control-group">
                                <label class="col-form-label">Title contains</label>
                                <div class="controls">
                                    <input type="text" ng-model="filter.title" class="form-control" name="searchFilter.title" ng-change="updateFilter()"> </input>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-form-label">Belongs to</label>
                                <div class="controls form-row">
                                    <div class="col-6">
                                        <select name="searchFilter.projectId" class="form-control"
                                                ng-model="filter.projectId" ng-options="project.id as project.title for project in projects"
                                                ng-change="updateFilter()">
                                            <option value="">Any project</option>
                                        </select>
                                    </div>
                                    <div class="col-6">
                                        <select name="searchFilter.collectionId" class="form-control"
                                                ng-model="filter.collectionId" ng-options="collection.id as collection.title for collection in collections"
                                                ng-change="updateFilter()">
                                            <option value="">Any collection</option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="control-group" ng-show="categoryFilter">
                                <label class="col-form-label">Category</label>
                                <div class="controls">
                                    <select name="searchFilter.categoryId" class="input-xlarge"
                                            ng-model="filter.categoryId" ng-options="category.id as category.name group by category.parent_name for category in categories"
                                            ng-change="updateFilter()">
                                        <option value="">Any category</option>
                                    </select>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="col-form-label">Other Filters</label>
                                <div class="controls">
                                <div class="form-check form-check-inline">
                                    <input type="checkbox" name="searchFilter.bookmarked" ng-model="filter.bookmarked" ng-change="updateFilter()" class="form-check-input">
                                    <label class="checkbox form-check-label">Bookmarked Items</label>
                                 </div>
                                 <div class="form-check form-check-inline">
                                    <input type="checkbox" name="searchFilter.integrationCompatible" ng-model="filter.integrationCompatible" value="true" ng-change="updateFilter()" class="form-check-input">
                                    <label class="checkbox form-check-label">Integration-compatible</label>
                                </div>
                                </div>
                            </div>
                        </div>

                        <div class="table-modal-results-container">
                            <table class="table table-striped table-modal-results table-hover table-condensed" id="modalResults" ng-class="{active: !modalSearching, disabled: modalSearching}">
                                  <thead class="thead-dark">

                                <tr>
                                    <th style="width:1em">&nbsp</th>
                                    <th style="width:40em">Title</th>
                                    <th style="width:10em">Date</th>
                                    <th style="width: 20em">Mapped Ontologies (hover to show all)</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr ng-repeat="result in results" ng-class="{warning: isSelected(result.id)}">
                                    <td style="width:1em">
                                        <input
                                                type="checkbox"
                                                id="cbResult{{result.id}}"
                                                name="selectedItems[]"
                                                value="{{result.id}}"
                                                ng-enabled="!modalSearching"
                                                ng-checked="isSelected(result.id)"
                                                ng-click="toggleSelection(result.id, this)">
                                    </td>
                                    <td style="width:35em"><label for="cbResult{{result.id}}">{{result.title}}</label></td>
                                    <td style="width:10em">{{result.date_created | date }}</td>
                                    <td class="ellipsified" style="max-width: 25em" title="{{result.ontologies.join(', ')}}" >
                                        <span ng-repeat="ontology in result.ontologies" ">{{$first ? '' : ', '}}{{ontology}}</span>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <div class="modal-footer">
                <div class="col-4 text-left">
                    <span ng-show="results.length"> Displaying records {{startRecord() + 1| number}} - {{endRecord()  | number}} of {{modalTotalResults}} </span>
                </div>
                <div class="col-4 text-center">
                    <button type="button" class="btn btn-sm" id="btnPrevious" ng-click="previousPage()" ng-disabled="!hasPreviousPage()">previous</button>
                    <button type="button" class="btn btn-sm" id="btnNext" ng-click="nextPage()" ng-disabled="!hasNextPage()">next</button>

                </div>
                <div class="col-4">
                    <ng-pluralize count="selectedItems.length"
                                  when="{'0': 'No datasets selected',
                                '1': '1 dataset selected',                                'other': '{{selectedItems.length}} datasets selected'}"></ng-pluralize>
                    <span ng-show="selectedItems.length" >(<a href="javascript:void(0)"  ng-click="clearSelectedItems()">clear selections</a>)</span>
                </div>
            </div>





            <div class="row-fluid">
                <div class="col-12">
                    <button class="btn" data-dismiss="modal" aria-hidden="true" ng-click="cancel()">Close</button>
                    <button class="btn btn-primary btn-primary-add" id="btnModalAdd" data-dismiss="modal" ng-click="confirm(selectedItems)">Add selected items</button>
                </div>
            </div>



        </div>
        </div>
    </div>
</script>

<#-- fixme: hack: /workspace/integrate#addDatasets  -->
<script>
    console.warn("Tell jim to remove his auto-open hack");
    $(function() {
        if(window.location.hash === "#addDatasets") {
            $("#btnAddDataset").click();
        }
    })
</script>


<div>

    <div id="divResultContainer" class="modal modal-big fade hide" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
		<div class="modal-content">
        <div class="modal-header alert-info">
            <h3 id="hModalHeader">Integration Results</h3>
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        </div>
        <div class="modal-body">
            <div class="row-fluid">
                <div class="col-12">
                    
                    <div role="tabpanel">
                    
                      <!-- Nav tabs -->
                      <ul class="nav nav-tabs" role="tablist">
                        <li role="presentation" class="active nav-item"><a class="nav-link" href="#pivot" aria-controls="pivot" role="tab" data-toggle="tab">Summary</a></li>
                        <li role="presentation" class="nav-item"><a class="nav-link" href="#preview" aria-controls="preview" role="tab" data-toggle="tab">Preview</a></li>
                        <li role="presentation" class="nav-item"><a class="nav-link" href="#json" aria-controls="preview" role="tab" data-toggle="tab">Raw (request)</a></li>
                      </ul>
                      <!-- Tab panes -->
                      <div class="tab-content">
                        <div role="tabpanel" class="tab-pane active" id="pivot">
                            <table class="table" id="tblPivotData">
                                <thead>
                                    <th ng-repeat="col in download.pivotData.columns">{{col}}</th>
                                </thead>
                            <tbody>
                                <tr ng-repeat="row in download.pivotData.rows">
                                    <td ng-repeat="col in row">{{col}}
                                    </td>
                                </tr>
                            </tbody>
                            </table>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="preview">
                            <table class="table" id="tblPreviewData">
                                <thead>
                                    <th ng-repeat="col in download.previewData.columns">{{col}}</th>
                                </thead>
                            <tbody>
                                <tr ng-repeat="row in download.previewData.rows">
                                    <td ng-repeat="_col in row track by $index">{{_col}}</td>
                                </tr>
                            </tbody>
                            </table>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="json">
                        <div class="card">
                            <div class="card-body">
                            {{ ctrl.getIntegration() }}
                            </div>
                        </div>
                        </div>
                      </div>
                    
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">

            <div class="row-fluid">
                <div class="col-12">
                    <a type="button" class="btn btn-secondary" ng-disabled="downloadDisabled" class="btn" ng-click="downloadClicked()" ng-href="/workspace/download?ticketId={{download.ticketId}}">Download</a>
                    <button class="btn" data-dismiss="modal" aria-hidden="true" ng-click="cancel()">Close</button>
                </div>
            </div>
        </div>
    </div>
    </div>
</div>
</body>

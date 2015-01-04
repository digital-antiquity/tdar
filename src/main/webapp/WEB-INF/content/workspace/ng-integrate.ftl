<head>
    <title>Dataset Integration: Edit</title>
    <link rel="stylesheet" href="/css/tdar-integration.css" media="screen">
</head>
<body>
<div id="divIntegrationMain" ng-controller="IntegrationController as ctrl">
    <div id="divIntegrationHeader">
        <h1 class="compact">Dataset Integration</h1>
    </div>
    <form id="frmIntegrationEdit" class="form-horizontal" ng-init="ctrl.loadJSON()">
        <div class="row">
            <div class="span9">
               <div class="control-group">
                   <label class="control-label">
                       Integration Name
                   </label>
                   <div class="controls">
                       <input type="text" class="input-block-level" name="integration.title" ng-model="ctrl.integration.title">
                   </div>
               </div>
               <div class="control-group">
                   <label class="control-label">Description</label>
                   <div class="controls">
                       <textarea name="integration.description" class="input-block-level" cols="80" rows="2"
                                 ng-model="ctrl.integration.description"></textarea>
                   </div>
               </div>
            </div>
            <div class="span3">
                <div class="btn-group">
                	<!-- re enable ignore-ng-disabled when TDAR-4367 is fixed -->
                    <button type="button" class="btn" ignore-ng-disabled="!isMinimallyValid()" ng-disabled="!isValid()"  id="btnSave" ng-click="ctrl.saveClicked()">Save</button>
                    <button type="button" class="btn btn-primary" ng-disabled="!isValid()" id="btnIntegrate" ng-click="ctrl.integrateClicked()">Integrate</button>
                </div>
            </div>
        </div>
        <div id="divActionsSection">
                    <div class="control-group">
                        <label class="control-label">Actions</label>
                        <div class="controls">
                            <div class="btn-group">
                                <button type="button" class="btn"  id="btnAddDataset"
                                        ng-click="ctrl.addDatasetsClicked()">Add Datasets...</button>
                                <div class="btn-group" >
                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#" ng-class="{disabled: !ctrl.integration.ontologies.length}">
                                        Add Integration Column
                                        <span class="caret"></span>
                                    </a>
                                    <ul class="dropdown-menu" id="btnSetAddIntegrationColumns">
                                        <li ng-repeat="ontology in ctrl.integration.ontologies"
                                                ><a href="#" ng-click="ctrl.addIntegrationColumnsMenuItemClicked(ontology)">{{ontology.title}}</a></li>
                                    </ul>
                                </div>
                                <button type="button" class="btn" id="btnAddDisplayColumn"
                                        ng-click="ctrl.addDisplayColumnClicked()"
                                        ng-disabled="!ctrl.integration.ontologies.length"
                                        >Add Display Column</button>

                                <button type="button" class="btn" id="btnAddCountColumn"
                                        ng-click="ctrl.addCountColumnClicked()"
                                        ng-disabled="ctrl.isCountColumnDisabled()"
                                        >Add Count Column</button>
                            </div>

                        </div>
                    </div>
        </div>

	<div>
	{{statusMessage}} {{$scope.statusMessage}}
	</div>

        <div id="divSelectedItemsSection">
            <div class="row">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label">Datasets & Ontologies</label>
                        <div class="controls controls-row">
                            <div class="span4">
                                <label>Selected Datasets</label>
                                <div>
                                    <select size="5" class="input-block-level" multiple
                                            ng-model="selectedDataTables" name="selectedDatasets" id="selDatasets"
                                            ng-options="dataTable|dtDisplayName|titleCase for dataTable in ctrl.integration.dataTables"></select>
                                </div>
                                <button type="button" class="btn input-block-level" id="rmDatasetBtn"
                                        ng-click="ctrl.removeSelectedDatasetClicked()" ng-disabled="ctrl.integration.dataTables.length === 0">Remove selected dataset</button>
                            </div>
                            <div class="span4">
                                <label>Shared Ontologies</label>
                                <ul>
                                    <li class="sharedOntologies" ng-repeat="ontology in ctrl.integration.ontologies">{{ontology | ontDisplayName}}</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        </form>
        <form>
        <div id="divColumnSection">
            <div class="row">
                <div class="span12">

                    <div class="control-group" ng-show="ctrl.integration.columns.length">
                        <label class="control-label">
                            Configure Columns
                        </label>
                        <div class="controls">

                            <div id="tabControl">
                                <ul class="nav nav-tabs">
                                    <li ng-repeat="column in ctrl.integration.columns" ng-click="ctrl.setTab($index)" onclick="return false;" ng-class="{active: ctrl.isTabSet($index)}" >
                                        <a href="#tab{{$index}}" id="tabtab{{$index}}">
                                        {{column.name}}
                                            <input type="hidden" name="column.name{{$index}}" ng-model="column.name" />
                                            <button class="close" ng-click="ctrl.closeTab($index)">x</button>
                                        </a>
                                    </li>
                                </ul>

                                <div class="tab-content" >
                                    <div class="tab-pane" id="tab{{$index}}"
                                         ng-repeat="outputColumn in ctrl.integration.columns" ng-class="{active: ctrl.isTabSet($index)}">
                                        <div ng-switch="outputColumn.type">
                                            <div ng-switch-when="integration" class=".integration-pane-content">
                                                <div class="alert" ng-hide="outputColumn.isValidMapping">
                                                    <strong>Invalid Ontology</strong> {{outputColumn.ontology | ontDisplayName}} does not belong to a shared ontology.
                                                </div>
                                                <table class="table table-bordered table-condensed">
                                                    <thead>
                                                    <tr>
                                                        <th rowspan="2" style="white-space: nowrap;">&nbsp;</th>
                                                        <th rowspan="2" style="width:99%">
                                                            <div class="pull-right">
                                                                Select values that appear in
                                                                <div class="btn-group">
                                                                    <button class="btn btn-mini" type="button" ng-click="selectMatchingNodes('some')">Any column</button>
                                                                    <button class="btn btn-mini" type="button" ng-click="selectMatchingNodes('every')">Every column</button>
                                                                </div>
                                                            </div>
                                                            <div>Node Value</div>
                                                        </th>
                                                        <th rowspan="1" style="white-space: nowrap;" colspan="{{outputColumn.dataTableColumns.length}}">
                                                            Datasets
                                                        </th>
                                                    </tr>
                                                    <tr>
                                                        <th ng-repeat="cc in lookupCompatibleColumns(outputColumn.ontologyId)" >
                                                        	<!-- suggest using  track by c.name to get at a key that we can more easily use" -->
                                                        	<div ng-switch on="cc.compatCols.length">
                                                        	<div ng-switch-when="1">
                                                        		{{cc.compatCols[0].displayName}}
                                                        		<!-- FIXME: this is "hidden", but is it even needed? -->
                                                        		<!-- FIXME: shouldn't this be the dataset name? -->
	                                                            <select class="intcol" ng-model="outputColumn.selectedDataTableColumns[$index]" ng-options="c.displayName for c in cc.compatCols" ng-hide="true"></select>
														    </div>
														    <div ng-switch-default>
	                                                            <select class="intcol" ng-model="outputColumn.selectedDataTableColumns[$index]" ng-options="c.displayName for c in cc.compatCols"></select>
														    </div>
                                                        	</div>
                                                        </th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    <tr ng-repeat="nodeSelection in outputColumn.nodeSelections" ng-init="nodeIndex = $index">
                                                        <td><input type="checkbox" name="cbont" ng-model="nodeSelection.selected" id="cbont_{{nodeSelection.node.id}}"></td>
                                                        <td style="white-space: nowrap;">
                                                            <div class="nodechild{{nodeSelection.node.index.split('.').length}}">
                                                                <label for="cbont_{{nodeSelection.node.id}}">{{nodeSelection.node.displayName}}</label>
                                                            </div>
                                                        </td>
                                                        <td ng-repeat="dataTableColumn in outputColumn.selectedDataTableColumns">
                                                            <div class="text-center">
                                                                <i class="icon-ok" ng-show="ontologyValuePresent(dataTableColumn, nodeSelection.node)"></i>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                            </div>

                                            <div ng-switch-when="display" class=".display-pane-content">
                                            
                                                <h3>Select Columns</h3>
                                                <br/>
                                                <table>
                                                	<thead>
                                                		<tr>
                                                			<th>Table</th> <th>Column</th>
                                                		</tr>
                                                	</thead>
                                                    <#--<tr ng-repeat="columnSelection in outputColumn.dataTableColumnSelections">-->
                                                    <tr ng-repeat="dataTable in ctrl.integration.dataTables" ng-init="columnSelection = outputColumn.dataTableColumnSelections[$index]">
                                                        <th>{{dataTable.displayName}}</th>
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
                                                <h3>Select Columns</h3>
                                                <br/>
                                                <table>
                                                	<thead>
                                                		<tr>
                                                			<th>Table</th> <th>Column</th>
                                                		</tr>
                                                	</thead>
                                                    <#--<tr ng-repeat="columnSelection in outputColumn.dataTableColumnSelections">-->
                                                    <tr ng-repeat="dataTable in ctrl.integration.dataTables" ng-init="columnSelection = outputColumn.dataTableColumnSelections[$index]">
                                                        <th>{{dataTable.displayName}}</th>
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
</div>


<form ng-controller="LegacyFormController as legacyCtrl" id="frmLegacy" method="post" action="/workspace/display-filtered-results" style="visibility: hidden;">
<button type="button" class="btn" ng-disabled="legacyCtrl.integration.columns.length === 0" ng-click="legacyCtrl.dumpdata()">log to console</button>
<input type="submit" id="btnSubmitLegacyForm" class="btn" ng-disabled="legacyCtrl.integration.columns.length === 0" name="submit" value="submit">


<fieldset>
    <div ng-repeat="col in legacyCtrl.integration.columns" ng-init="columnIndex=$index">
        <input type="hidden" name="integrationColumns[{{$index}}].columnType" value="{{col.type | uppercase}}">
        <input type="hidden" name="integrationColumns[{{$index}}].name" value="{{col.name}}">
        <div ng-switch="col.type">
            <span ng-switch-when="integration">
                <input type="hidden" name="integrationColumns[{{columnIndex}}].columns[{{$index}}].id"
                       value="{{dtc.id}}"  ng-repeat="dtc in col.selectedDataTableColumns">
                <input type="hidden" name="integrationColumns[{{columnIndex}}].filteredOntologyNodes.id"
                       value="{{nodeSelection.node.id}}" ng-repeat="nodeSelection in col.nodeSelections | filter: {selected:true}">
            </span>
            <span ng-switch-when="display">
                <input type="hidden" name="integrationColumns[{{columnIndex}}].columns[{{$index}}].id"
                       value="{{colSelection.dataTableColumn.id}}"
                       ng-repeat="colSelection in col.dataTableColumnSelections | filter: {dataTableColumn: '!!'}">
            </span>
            <span ng-switch-when="count">
                <input type="hidden" name="integrationColumns[{{columnIndex}}].columns[{{$index}}].id"
                       value="{{colSelection.dataTableColumn.id}}"
                       ng-repeat="colSelection in col.dataTableColumnSelections | filter: {dataTableColumn: '!!'}">
            </span>
        </div>
    </div>
    <input ng-repeat="dataTable in legacyCtrl.integration.dataTables" type="hidden" name="tableIds[{{$index}}]" value="{{dataTable.id}}">
</fieldset>
</form>

<span>
<!-- Note: this modal is about span10 wide. Form-horizontal labels are ~span3 wide, leaving you ~span7 for controls. -->
<script type="text/ng-template" id="workspace/modal-dialog.html">
    <div id="divModalContainer" class="modal modal-big hide fade" tabindex="-1" role="dialog">
        <div class="modal-header alert-info">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="hModalHeader">{{title}}</h3>
        </div>
        <div class="modal-body">
            <div class="row">
                <div class="span10">
                    <form id="frmModal" class="form-horizontal form-condensed" ng-model-options="{ updateOn: 'default blur', debounce: {'default': 1000, 'blur':0} }">
                        <fieldset>
                            <legend>Filters</legend>
                            <div class="control-group">
                                <label class="control-label">Title</label>
                                <div class="controls">
                                    <input type="text" ng-model="filter.title" class="input-block-level" name="searchFilter.title"></input>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Project / Collection</label>
                                <div class="controls controls-row">
                                    <div class="span3">
                                        <select name="searchFilter.projectId" class="input-block-level"
                                                ng-model="filter.projectId" ng-options="project.id as project.title for project in projects">
                                            <option value="">Any project</option>
                                        </select>
                                    </div>
                                    <div class="span3">
                                        <select name="searchFilter.collectionId" class="input-block-level"
                                                ng-model="filter.collectionId" ng-options="collection.id as collection.title for collection in collections">
                                            <option value="">Any collection</option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="control-group" ng-show="categoryFilter">
                                <label class="control-label">Category</label>
                                <div class="controls">
                                    <select name="searchFilter.categoryId" class="input-xlarge"
                                            ng-model="filter.categoryId" ng-options="category.id as category.name group by category.parent_name for category in categories" >
                                        <option value="">Any category</option>
                                    </select>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="control-label">Other Filters</label>
                                <div class="controls form-inline">
                                    <label class="checkbox inline"><input type="checkbox" name="searchFilter.bookmarked" ng-model="filter.bookmarked">Bookmarked Items</label>
                                    <label class="checkbox inline"><input type="checkbox" name="searchFilter.integrationCompatible" ng-model="filter.integrationCompatible" value="true">Integration-compatible</label>
                                </div>
                            </div>
                        </fieldset>

                        <fieldset>
                            <legend>Select Results <span class="small" ng-show="modalSearching">Searching...</span></legend>
                            
                            <table class="table table-striped table-modal-results table-hover table-condensed" id="modalResults" ng-class="{active: !modalSearching, disabled: modalSearching}">
                                <thead>
                                <tr>
                                    <th>Select</th>
                                    <th>Title</th>
                                    <th nowrap>Date</th>
                                    <th>Mapped Ontologies</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr ng-repeat="result in results" ng-class="{warning: isSelected(result.id)}">
                                    <td>
                                        <input
                                                type="checkbox"
                                                id="cbResult{{result.id}}"
                                                name="selectedItems[]"
                                                value="{{result.id}}"
                                                ng-enabled="!modalSearching"
                                                ng-checked="isSelected(result.id)"
                                                ng-click="toggleSelection(result.id, this)">
                                    </td>
                                    <td><label for="cbResult{{result.id}}">{{result.title}}</label></td>
                                    <td nowrap>{{result.date_created | date }}</td>
                                    <td>
                                    	<span ng-repeat="ontology in result.ontologies">{{$first ? '' : ', '}}{{ontology}}</span>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </fieldset>
                    </form>
                </div>
            </div>
        </div>
        <div class="modal-footer">
        	<div class="row">
        	Total Results: {{modalTotalResults}}
        	</div>
            <div class="pull-left" ng-show="false">
                <button type="button" class="btn" ng-click="updateFilter()">Update Search</button>
                <small class="muted"><em>Temporary button</em></small>
            </div>

            <button class="btn" data-dismiss="modal" aria-hidden="true" ng-click="cancel()">Close</button>
            <button class="btn btn-primary" id="btnModalAdd" data-dismiss="modal" ng-click="confirm(selectedItems)">Add selected items</button>

        </div>
    </div>
</script>
<script type="application/json" id="jsondata">
${workflowJson!"{}"}
</script>

<!-- FIXME: embedded lookup data like this will be untenable for large datasets - use ajax solution instead -->
<!-- FIXME: too much crap - we just need ID and title and submitterId -->
<script type="application/json" id="allProjects">
${fullUserProjectsJson}
</script>

<script type="application/json" id="allCollections">
${allResourceCollectionsJson}
</script>

<script type="application/json" id="allCategories">
${categoriesJson}
</script>
<script src="/includes/js-emca-5.1-polyfill.js""></script>
<script src='https://ajax.googleapis.com/ajax/libs/angularjs/1.3.0/angular.min.js'></script>
<!-- fixme: cycle.js modifies global JSON object(which makse me nervous). think of a better way to incorporate this -->
<script src="/js/data-integration/app.js"></script>
<script src="/js/data-integration/ng-IntegrationController.js"></script>
<script src="/js/data-integration/ng-IntegrationModel.js"></script>
<script src="/js/data-integration/ng-IntegrationLegacyFormController.js"></script>
<script src="/js/data-integration/ng-IntegrationModalDialogController.js"></script>
<script src="/js/data-integration/ng-IntegrationDataService.js"></script>
<script src="/js/data-integration/ng-IntegrationCustomFilters.js"></script>
<script src="/includes/angular-modal-service-0.4.0/angular-modal-service.js"></script>

<#-- Include the file below to run some barebones tests -->
<#--<script src="/js/data-integration/tests.js"></script>-->
</span>


</body>

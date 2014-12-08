<head>
    <title>Dataset Integration: Edit</title>
    <link rel="stylesheet" href="/css/tdar.integration.css" media="screen">
</head>
<body>
<div id="divIntegrationMain" ng-controller="IntegrationCtrl as ctrl">
    <div id="divIntegrationHeader">
        <h1>Dataset Integration</h1>
        <h2>{{ctrl.integration.title || 'Create New Integration'}}</h2>
        <button type="button" class="btn btn-info" ng-click="loadIntegrationColumnDetails(ctrl.integration)">test intcoldetails</button>
    </div>
    <form id="frmIntegrationEdit" class="form-horizontal">
        <div class="row">
            <div class="span9">
               <div class="control-group">
                   <label class="control-label">
                       Integration Name
                   </label>
                   <div class="controls">
                       <input type="text" class="input-block-level" name="integration.title"
                              ng-model="ctrl.integration.title">
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
                    <button type="button" class="btn" ng-click="ctrl.saveClicked()">Save</button>
                    <button type="button" class="btn" ng-click="ctrl.integrateClicked()">Integrate</button>
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
                                <button type="button" class="btn"  id="btnAddIntegrationColumn"
                                        ignoreme-ng-click="ctrl.addIntegrationColumnsClicked()"
                                        ng-click="ctrl.addToIntegrationColumnsClicked()">Add Integration Columns...</button>
                                <button type="button" class="btn" id="btnAddDisplayColumn"
                                        ng-click="ctrl.addDisplayColumnClicked()">Add Display Column</button>
                            </div>
                        </div>
                    </div>
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
                                    <select size="5" class="input-block-level" multiple  ng-model="selectedDatatables" ng-options="datatable.data_table_name for datatable in ctrl.integration.datatables"></select>
                                </div>
                                <button type="button" class="btn input-block-level"
                                        ng-click="ctrl.removeSelectedDatasetClicked()">Remove selected dataset</button>
                            </div>
                            <div class="span4">
                                <label>Ontologies</label>
                                <div>
                                    <select size="5" class="input-block-level" multiple
                                            ng-model="selectedOntologies"
                                            ng-options="ontology.name for ontology in ctrl.integration.ontologies"
                                            ng-dblclick="ctrl.addToIntegrationColumnsClicked()"></select>
                                </div>

                                <#--<button type="button" class="btn input-block-level"-->
                                        <#--ng-click="ctrl.addToIntegrationColumnsClicked()">Add selected as integration column</button>-->

                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="divColumnSection">
            <div class="row">
                <div class="span12">

                    <div class="control-group">
                        <label class="control-label">
                            Configure Columns
                        </label>
                        <div class="controls">

                            <div id="tabControl">
                                <ul class="nav nav-tabs">
                                    <li ng-repeat="column in ctrl.integration.columns" ng-click="ctrl.setTab($index)" onclick="return false;" ng-class="{active: ctrl.isTabSet($index)}" >
                                        <a href="#tab{{$index}}">
                                            {{column.title}}
                                            <button class="close" ng-click="ctrl.closeTab($index)">x</button>
                                        </a>
                                    </li>
                                </ul>

                                <div class="tab-content" >
                                <#-- FIXME: integrationColumn is a crummy name. some are display, some are integration.  what's a good name for that? -->
                                    <div class="tab-pane" id="tab{{$index}}"
                                         ng-repeat="integrationColumn in ctrl.integration.columns" ng-class="{active: ctrl.isTabSet($index)}" ng-init="columnIndex=$index">
                                        <div ng-switch="integrationColumn.type">
                                            <div ng-switch-when="integration" class=".integration-pane-content">
                                                <table class="table table-bordered table-condensed">
                                                    <thead>
                                                    <tr>
                                                        <th rowspan="2" style="white-space: nowrap;">&nbsp;</th>
                                                        <th rowspan="2" style="width:99%">Node Value</th>
                                                        <th rowspan="1" style="white-space: nowrap;" colspan="{{integrationColumn.dataTableColumns.length}}">
                                                            Datasets
                                                        </th>
                                                    </tr>
                                                    <tr>
                                                        <th ng-repeat="cc in lookupCompatibleColumns(integrationColumn.ontologyId)" >
                                                        	<!-- suggest using  track by c.name to get at a key that we can more easily use" -->
                                                            <select ng-model="integrationColumn.selectedDatatableColumns[$index]" ng-options="c.display_name for c in cc.compatCols"></select>
                                                        </th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    <tr ng-repeat="nodeSelection in integrationColumn.nodeSelections">
                                                        <td><input type="checkbox" name="tbd" ng-model="nodeSelection.selected" id="cbont_{{nodeSelection.node.id}}"></td>
                                                        <td style="white-space: nowrap;">
                                                            <div class="nodechild{{nodeSelection.node.index.split('.').length}}">
                                                                <label for="cbont_{{nodeSelection.node.id}}">{{nodeSelection.node.display_name}}</label>
                                                            </div>
                                                        </td>
                                                        <td ng-repeat="datatableColumn in integrationColumn.selectedDatatableColumns">
                                                            {{nodeSelection.node.participatingDatatableColumnIds.indexOf(datatableColumn.id) === -1 ? '' : 'x' }}
                                                        </td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                            </div>

                                            <div ng-switch-when="display" class=".display-pane-content">
                                                <h3>Choose source columns</h3>
                                                <table>
                                                    <tr ng-repeat="columnSelection in integrationColumn.datatableColumnSelections">
                                                        <td>{{columnSelection.datatable.data_table_name}}</td>
                                                        <td>
                                                            <select ng-model="columnSelection.datatableColumn"
                                                                    ng-options="c.display_name for c in columnSelection.datatable.columns">
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


<form ng-controller="LegacyFormController as legacyCtrl" id="frmLegacy" method="post" action="/workspace/display-filtered-results">
<h2>Debug:  legacy form</h2>
<button type="button" class="btn" ng-disabled="legacyCtrl.integration.columns.length === 0" ng-click="legacyCtrl.dumpdata()">log to console</button>
<input type="submit"  class="btn" ng-disabled="legacyCtrl.integration.columns.length === 0" name="submit" value="submit">


<fieldset>
    <div ng-repeat="col in legacyCtrl.integration.columns" ng-init="columnIndex=$index">
        <input type="hidden" name="integrationColumns[{{$index}}].columnType" value="{{col.type | uppercase}}">
        <div ng-switch="col.type">
            <span ng-switch-when="integration">
                <input type="hidden" name="integrationColumns[{{columnIndex}}].columns[{{$index}}].id"
                       value="{{dtc.id}}"  ng-repeat="dtc in col.selectedDatatableColumns">
                <input type="hidden" name="integrationColumns[{{columnIndex}}].filteredOntologyNodes.id"
                       value="{{nodeSelection.node.id}}" ng-repeat="nodeSelection in col.nodeSelections | filter: {selected:true}">
            </span>
            <span ng-switch-when="display">
                <input type="hidden" name="integrationColumns[{{columnIndex}}].columns[{{$index}}].id"
                       value="{{colSelection.datatableColumn.id}}"
                       ng-repeat="colSelection in col.datatableColumnSelections | filter: {datatableColumn: '!!'}">
            </span>
        </div>
    </div>
    <input ng-repeat="datatable in legacyCtrl.integration.datatables" type="hidden" name="tableIds[{{$index}}]" value="{{datatable.data_table_id}}">
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
                                    <label class="checkbox inline"><input type="checkbox" name="searchFilter.incompatible" ng-model="filter.incompatible" value="true">Integration-compatible</label>
                                </div>
                            </div>
                        </fieldset>

                        <fieldset>
                            <legend>Select Results</legend>
                            <table class="table table-striped table-modal-results table-hover table-condensed">
                                <thead>
                                <tr>
                                    <th>Select</th>
                                    <th>Title</th>
                                    <th>Author</th>
                                    <th>Date</th>
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
                                                ng-checked="isSelected(result.id)"
                                                ng-click="toggleSelection(result.id, this)">
                                    </td>
                                    <td><label for="cbResult{{result.id}}">{{result.title}}</label></td>
                                    <td>{{result.submitter_display_name}}</td>
                                    <td>{{result.date_created | date }}</td>
                                </tr>
                                </tbody>
                            </table>
                        </fieldset>
                    </form>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <div class="pull-left">
                <button type="button" class="btn" ng-click="updateFilter()">Update Search</button>
                <small class="muted"><em>Temporary button</em></small>
            </div>

            <button class="btn" data-dismiss="modal" aria-hidden="true" ng-click="cancel()">Close</button>
            <button class="btn btn-primary" data-dismiss="modal" ng-click="confirm(selectedItems)">Add selected items</button>

        </div>
    </div>
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
<script src='https://ajax.googleapis.com/ajax/libs/angularjs/1.3.0/angular.min.js'></script>
<!-- fixme: cycle.js modifies global JSON object. think of a better way to incorporate this -->
<script src="/includes/json-js-3d7767b/cycle.js"></script>
<script src="/js/tdar.integration.edit-angular.js"></script>
<script src="/includes/angular-modal-service-0.4.0/angular-modal-service.js"></script>
<script src="/js/tdar.pagination.js"></script>
</span>


</body>

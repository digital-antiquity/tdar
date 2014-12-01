<head>
    <title>Dataset Integration: Edit</title>
    <link rel="stylesheet" href="/css/tdar.integration.css" media="screen">
</head>
<body>
<div id="divIntegrationMain" ng-controller="IntegrationCtrl as ctrl">
    <div id="divIntegrationHeader">
        <h1>Dataset Integration</h1>
        <h2>{{ctrl.integration.title || 'Create New Integration'}}</h2>
    </div>
    <form id="frmIntegrationEdit" action="#" method="post" class="form-horizontal">
        <div class="row">
            <div class="span9">
               <div class="control-group">
                   <label class="control-label">
                       Integration Name
                   </label>
                   <div class="controls">
                       <input type="text" class="input-xxlarge" name="integration.title"
                              ng-model="ctrl.integration.title">
                   </div>
               </div>
               <div class="control-group">
                   <label class="control-label">
                       Description
                   </label>
                   <div class="controls">
                       <textarea name="integration.description" class="input-xxlarge" cols="80" rows="4"
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
            <div class="row">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label">Actions</label>
                        <div class="controls">
                            <div class="btn-group">
                                <button type="button" class="btn"  id="btnAddDataset"
                                        ng-click="ctrl.addDatasetsClicked()">Add Datasets...</button>
                                <button type="button" class="btn"  id="btnAddIntegrationColumn"
                                        ng-click="ctrl.addIntegrationColumnsClicked()">Add Integration Columns...</button>
                                <button type="button" class="btn" id="btnAddDisplayColumn"
                                        ng-click="ctrl.addDisplayColumnClicked()">Add Display Column</button>
                            </div>
                        </div>
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
                            <div class="span5">
                                <label>Selected Datasets: {{ctrl.integration.datatables.length}}</label>
                                <div>
                                    <select size="10" class="input-xlarge" multiple  ng-model="selectedDatatables" ng-options="datatable.data_table_name for datatable in ctrl.integration.datatables"></select>
                                </div>
                                <button type="button" class="btn input-xlarge"
                                        ng-click="ctrl.removeSelectedDatasetClicked()">Remove Selected Dataset</button>
                            </div>
                            <div class="span4">
                                <label>Shared Ontologies</label>
                                <div>
                                    <select size="10" class="input-xlarge"
                                            ng-model="selectedOntologies" multiple ng-options="ontology.name for ontology in ctrl.sharedOntologies"></select>
                                </div>
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
                                    <div class="tab-pane" id="tab{{$index}}"
                                         ng-repeat="column in ctrl.integration.columns" ng-class="{active: ctrl.isTabSet($index)}" ng-init="columnIndex=$index">

                                        <div ng-switch="column.type">

                                            <div ng-switch-when="integration" class=".integration-pane-content">

                                                <table class="table table-bordered table-condensed">
                                                    <thead>
                                                    <tr>
                                                        <th rowspan="2" style="white-space: nowrap;">&nbsp;</th>
                                                        <th rowspan="2" style="width:99%">Node Value</th>
                                                        <th rowspan="1" style="white-space: nowrap;" colspan="{{column.dataTableColumns.length || 1}}">
                                                            Datasets
                                                        </th>
                                                    </tr>
                                                    <tr>
                                                        <th ng-repeat="dtc in column.dataTableColumns">{{dtc.data_table_display_name}}</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    <tr ng-repeat="nodeSelection in column.nodeSelections">
                                                        <td><input type="checkbox" name="tbd" ng-model="nodeSelection.selected" id="cbont_{{nodeSelection.node.id}}"></td>
                                                        <td style="white-space: nowrap;">
                                                            <div class="nodechild{{nodeSelection.node.index.split('.').length}}">
                                                                <label for="cbont_{{nodeSelection.node.id}}">{{nodeSelection.node.display_name}}</label>
                                                            </div>
                                                        </td>
                                                        <td ng-repeat="dtc in column.datatableColumnIds track by $index">{{val}}</td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                            </div>

                                            <div ng-switch-when="display" class=".display-pane-content">
                                                <#--<div class="control-group">-->
                                                    <#--<label class="control-label">Display Column Name</label>-->
                                                <#--</div>-->
                                                <#--<div>-->
                                                    <label>Source columns to include in this display column</label>
                                                    <table class="table table-condensed">
                                                        <thead>
                                                            <tr>
                                                                <th></th>
                                                                <th>Column Name</th>
                                                                <th>Table Name</th>
                                                                <th>Description</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody ng-repeat="datatable in ctrl.integration.datatables">
                                                            <tr ng-repeat="column in datatable.columns">
                                                                <td>{{columnIndex}} :: {{$index}}
                                                                    <input type="checkbox" ng-model="ctrl.integration.columns[columnIndex].data[$index].selected" name="displayColumns[]"
                                                                           id="cbDisplayCol{{datatable.data_table_id}}_{{column.id}}">
                                                                </td>
                                                                <td>
                                                                    <label for="cbDisplayCol{{datatable.data_table_id}}_{{column.id}}">{{column.display_name}}</label>
                                                                </td>
                                                                <td>
                                                                    {{datatable.display_name}}
                                                                </td>
                                                                <td>
                                                                    {{datatable.description}}
                                                                </td>
                                                            </tr>
                                                        </tbody>
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
        </div>



    </form>
</div>


<form ng-controller="LegacyFormController as legacyCtrl" id="frmLegacy">
<#--<button type="button" class="btn btn-mini" ng-click="buildLegacyForm()">Build Legacy Form</button>-->
<#--<button type="button" class="btn btn-mini" ng-click="hideForm()">Hide Form</button>-->
<div  og-show="showForm">
    <h2>legacy form goes here</h2>
    <button type="button" ng-click="legacyCtrl.dumpdata()">dump data</button>
    <fieldset>
        <div ng-repeat="col in legacyCtrl.integration.columns" ng-init="columnIndex=$index">
            <input type="hidden" name="integrationColumns[{{$index}}].columnType" value="{{col.type | uppercase}}">
            type: {{col.type}}
            <div ng-switch="col.type">
                <span ng-switch-when="integration">
                    <input type="hidden" name="integrationColumns[{{columnIndex}}].columns[{{$index}}]" value="{{dtc.id}}"  ng-repeat="dtc in col.dataTableColumns">
                    <input type="hidden" name="integrationColumns[{{columnIndex}}].filteredOntologyNodes.id" value="{{node.id}}" ng-repeat="node in col.data.nodes | filter: {selected:true}">
                </span>
                <span ng-switch-when="display">
                    <input type="hidden" name="integrationColumns[{{columnIndex}}].columns[{{$index}}].id" value="{{dtc.id}}" ng-repeat="dtc in col.data | filter: {selected: true}">
                </span>
            </div>
        </div>
        <input ng-repeat="datatable in legacyCtrl.integration.datatables" type="hidden" name="tableIds[$index]" value="{{datatable.data_table_id}}">
    </fieldset>
</div>
</form>

<div>

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
                                    <input type="text" ng-model="filter.title" class="input-block-level" name="filter.title"></input>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Project / Collection</label>
                                <div class="controls controls-row">
                                    <div class="span3">
                                        <select name="filter.projectId" class="input-block-level"
                                                ng-model="filter.projectId" ng-options="project.id as project.title for project in projects">
                                            <option value="">Any project</option>
                                        </select>
                                    </div>
                                    <div class="span3">
                                        <select name="filter.collectionId" class="input-block-level"
                                                ng-model="filter.collectionId" ng-options="collection.id as collection.title for collection in collections">
                                            <option value="">Any collection</option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="control-group" ng-show="categoryFilter">
                                <label class="control-label">Category</label>
                                <div class="controls">
                                    <select name="filter.categoryId" class="input-xlarge"
                                            ng-model="filter.categoryId" ng-options="category.id as category.name group by category.parent_name for category in categories" >
                                        <option value="">Any category</option>
                                    </select>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="control-label">Other Filters</label>
                                <div class="controls form-inline">
                                    <label class="checkbox inline"><input type="checkbox" name="filter.unbookmarked" ng-model="filter.unbookmarked">Bookmarked Items</label>
                                    <label class="checkbox inline"><input type="checkbox" name="filter.incompatible" ng-model="filter.incompatible" value="true">Integration-compatible</label>
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
<script src="/js/tdar.integration.edit-angular.js"></script>
<script src="/includes/angular-modal-service-0.4.0/angular-modal-service.js"></script>
<script src="/js/tdar.pagination.js"></script>
</span>


</body>

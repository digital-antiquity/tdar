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
                              data-bind="{value: integration.title}" ng-model="ctrl.integration.title">
                   </div>
               </div>
               <div class="control-group">
                   <label class="control-label">
                       Description
                   </label>
                   <div class="controls">
                       <textarea name="integration.description" class="input-xxlarge" cols="80" rows="4"
                                 data-bind="{value: integration.description}" ng-model="ctrl.integration.description"></textarea>
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
                                        data-bind="click: addDatasetsClicked" ng-click="ctrl.addDatasetsClicked()">Add Datasets...</button>
                                <button type="button" class="btn"  id="btnAddIntegrationColumn"
                                        data-bind="click: addIntegrationColumnsClicked" ng-click="ctrl.addIntegrationColumnsClicked()">Add Integration Columns...</button>
                                <button type="button" class="btn" id="btnAddDisplayColumn"
                                        data-bind="click: addDisplayColumnClicked" ng-click="ctrl.addDisplayColumnClicked()">Add Display Column</button>
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
                                    <select size="10" class="input-xlarge" multiple  ng-model="selectedDatatables" ng-options="datatable.display_name for datatable in ctrl.integration.datatables"></select>
                                </div>
                                <button type="button" class="btn input-xlarge"
                                        data-bind="click: removeSelectedDatasetClicked" ng-click="ctrl.removeSelectedDatasetClicked()">Remove Selected Dataset</button>
                            </div>
                            <div class="span4">
                                <label>Selected Ontologies</label>
                                <div>
                                    <select size="10" class="input-xlarge"></select>
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

                            <div id="tabControl" data-bind="if: integration.columns().length">
                                <ul class="nav nav-tabs"
                                    data-bind="foreach: integration.columns">
                                    <li data-bind="css:{'integration-column': type === 'integration', active: $parent.currentColumn === $index}"
                                        ng-repeat="column in ctrl.integration.columns" ng-click="ctrl.setTab($index)" onclick="return false;" ng-class="{active: ctrl.isTabSet($index)}" >
                                        <a href="#tab{{$index}}" data-bind="text: name">
                                            {{column.name}}
                                            <button class="close" ng-click="ctrl.closeTab($index)">x</button>
                                        </a>
                                    </li>
                                </ul>

                                <div class="tab-content" data-bind="foreach: integration.columns" >
                                    <div class="tab-pane" id="tab{{$index}}"
                                         data-bind="css: {active: $parent.currentColumn === $index}"
                                         ng-repeat="column in ctrl.integration.columns" ng-class="{active: ctrl.isTabSet($index)}">
                                            <p>Welcome to tab pane {{column.name}}</p>
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

<!-- Note: this modal is about span10 wide. Form-horizontal labels are ~span3 wide, leaving you ~span7 for controls. -->
<script type="text/ng-template" id="workspace/modal-dialog.html">
    <div id="divModalContainer" class="modal modal-big hide fade" tabindex="-1" role="dialog">
        <div class="modal-header alert-info">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="hModalHeader">{{title}}</h3>
        </div>
        <div class="modal-body">
            <#-- we want the modal to be about span10 wide. Use this section as a guide.  If it spills into more than 1 row it means you screwed up your css.  -->
            <#--
            <div class="row">
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
                <div class="span1">span</div>
            </div>
            -->
            <div class="row">
                <div class="span10">
                    <form id="frmModal" class="form-horizontal form-condensed" ng-model-options="{ updateOn: 'default blur', debounce: {'default': 1000, 'blur':0} }">
                        <fieldset>
                            <legend>Filters</legend>
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
                                                name="selectedItems[]"
                                                value="{{result.id}}"
                                                ng-checked="isSelected(result.id)"
                                                ng-click="toggleSelection(result.id, this)">
                                    </td>
                                    <td>{{result.title}}</td>
                                    <td>{{result.submitter_display_name}}</td>
                                    <td>{{result.date | date : 'short'}}</td>
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


</body>

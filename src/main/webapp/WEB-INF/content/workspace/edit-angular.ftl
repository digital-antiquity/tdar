<head>
    <title>Dataset Integration: Edit</title>
    <link rel="stylesheet" href="/css/tdar.integration.css" media="screen">
</head>
<body>

<div id="divBody" ng-controller="IntegrationCtrl as ctrl">
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
                                <label>Selected Datasets</label>
                                <div>
                                    <select size="10" class="input-xlarge"></select>
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


<script src='https://ajax.googleapis.com/ajax/libs/angularjs/1.3.0/angular.min.js'></script>
<script src="/js/tdar.integration.edit-angular.js"></script>

</body>

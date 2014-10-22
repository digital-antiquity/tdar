<head>
    <title>Dataset Integration: Edit</title>
    <link rel="stylesheet" href="/css/tdar.integration.css" media="screen">
</head>
<body>
<div id="divIntegrationHeader">
    <h1>Dataset Integration</h1>
    <h2>Creating new integration</h2>
</div>

<div id="divBody">
    <form id="frmIntegrationEdit" action="#" method="post" class="form-horizontal">
        <div class="row">
            <div class="span9">
               <div class="control-group">
                   <label class="control-label">
                       Integration Name
                   </label>
                   <div class="controls">
                       <input type="text" class="input-xxlarge" name="integration.title" data-bind="{value: integration.title}">
                   </div>
               </div>
               <div class="control-group">
                   <label class="control-label">
                       Description
                   </label>
                   <div class="controls">
                       <textarea name="integration.description" class="input-xxlarge" cols="80" rows="4" data-bind="{value: integration.description}"></textarea>
                   </div>
               </div>
            </div>
            <div class="span3">
                <div class="btn-group">
                    <button type="button" class="btn">Save</button>
                    <button type="button" class="btn">Integrate</button>
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
                                <button type="button" class="btn"  id="btnAddDataset" data-bind="click: addDatasetsClicked">Add Datasets...</button>
                                <button type="button" class="btn"  id="btnAddIntegrationColumn" data-bind="click: integration.addColumn">Add Integration Columns...</button>
                                <button type="button" class="btn" id="btnAddDisplayColumn" data-bind="click: integration.addColumn">Add Display Column</button>
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
                                <button type="button" class="btn input-xlarge" data-bind="click: removeDatasetClicked">Remove Selected Dataset</button>
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
                                <ul class="nav nav-tabs" data-bind="foreach: integration.columns">
                                    <li data-bind="css:{'integration-column': type === 'integration', active: $parent.currentColumn === $index}">
                                        <a href="#" data-bind="text: name"></a>
                                    </li>
                                </ul>

                                <div class="tab-content" data-bind="foreach: integration.columns">
                                    <div class="tab-pane" data-bind="css: {active: $parent.currentColumn === $index}">
                                            This is a tab pane!
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

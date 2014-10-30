<head>
    <title>Dataset Integration: Edit</title>
    <link rel="stylesheet" href="/css/tdar.integration.css" media="screen">
</head>
<body>
<div class="modal-backdrop fade in">
</div>
<@ontologyModal />

<div id="divBody" ng-controller="IntegrationCtrl as ctrl" style="opacity: 0.1">
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
<div id="divTempModal" ng-controller="ModalController as modalctrl">
    <button type="button" class="btn" ng-click="showDatasetsModal()">Show Datasets</button>
</div>


<script type="text/ng-template" id="workspace/add-datasets.html">
    <div id="divModalAddDatasets" class="modal modal-big hide fade" tabindex="-1" role="dialog">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="hModalHeader">Modal header</h3>
        </div>
        <div class="modal-body">
            <p>Modal Body</p>
            <p>Greeting is: {{greeting}}</p>

        </div>
        <div class="row">
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true" ng-click="cancel()">Cancel</button>
            <button class="btn btn-primary" data-dismiss="modal" ng-click="confirm(returnData)">Add</button>
        </div>
    </div>
</script>

<script type="text/ng-template" id="workspace/add-ontology-modal.html">
<@ontologyModal />
</script>

<#macro ontologyModal>
<#--<div id="divModalContainer" class="modal modal-big hide fade" tabindex="-1" role="dialog">-->
<div id="divModalContainer" class="modal modal-big fade in" tabindex="-1" role="dialog" ng-controller="OntologyController">
    <div class="modal-header alert-info">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="hModalHeader">{{title}}</h3>

    </div>
    <div class="modal-body">
        <div class="row">
            <div class="span10">
                <form id="frmModal" class="form-horizontal form-condensed">

                    <fieldset>
                        <legend>Filters</legend>
                        <div class="control-group">
                            <label class="control-label">Project / Collection</label>
                            <div class="controls controls-row">
                                <div class="span4">
                                    <select name="filter.projectId" class="input-block-level"
                                            ng-model="filter.projectId" ng-options="project.id as project.title for project in projects">
                                        <option value="">Any project</option>
                                    </select>
                                </div>
                                <div class="span4">
                                    <select name="filter.collectionId" class="input-block-level"
                                            ng-model="filter.collectionId" ng-options="collection.id as collection.title for collection in collections">
                                        <option value="">Any collection</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="control-group">
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
                                    <td>{{result.author}}</td>
                                    <td>{{result.date}}</td>
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

        <button class="btn" data-dismiss="modal" aria-hidden="true" ng-click="cancel()">Cancel</button>
        <button class="btn btn-primary" data-dismiss="modal" ng-click="confirm(selectedItems)">Add</button>
    </div>
</div>
</#macro>

<!-- FIXME: embedded lookup data like this will be untenable for large datasets - use ajax solution instead -->
<script type="application/json" id="allProjects">
    [
        {"id": 1, "title": "Sample project 1"},
        {"id": 2, "title": "Sample project 2"},
        {"id": 3, "title": "Sample project 3"},
        {"id": 4, "title": "Sample project 4"},
        {"id": 5, "title": "Sample project 5"}
    ]
</script>
<script type="application/json" id="allCollections">
    [
        {"id": 1, "title": "Sample resource collection 1"},
        {"id": 2, "title": "Sample resource collection 2"},
        {"id": 3, "title": "Sample resource collection 3"},
        {"id": 4, "title": "Sample resource collection 4"},
        {"id": 5, "title": "Sample resource collection 5"}

    ]
</script>
<script type="application/json" id="allCategories">
[
    {
        "id": 25,
        "name": "Material",
        "parent_name": "Architecture"
    },
    {
        "id": 26,
        "name": "Measurement",
        "parent_name": "Architecture"
    },
    {
        "id": 27,
        "name": "Style/Type",
        "parent_name": "Architecture"
    },
    {
        "id": 236,
        "name": "Other",
        "parent_name": "Architecture"
    },
    {
        "id": 28,
        "name": "Count",
        "parent_name": "Basketry"
    },
    {
        "id": 29,
        "name": "Design",
        "parent_name": "Basketry"
    },
    {
        "id": 30,
        "name": "Form",
        "parent_name": "Basketry"
    },
    {
        "id": 31,
        "name": "Function",
        "parent_name": "Basketry"
    },
    {
        "id": 32,
        "name": "Material",
        "parent_name": "Basketry"
    },
    {
        "id": 33,
        "name": "Measurement",
        "parent_name": "Basketry"
    },
    {
        "id": 34,
        "name": "Technique",
        "parent_name": "Basketry"
    },
    {
        "id": 35,
        "name": "Weight",
        "parent_name": "Basketry"
    },
    {
        "id": 237,
        "name": "Other",
        "parent_name": "Basketry"
    },
    {
        "id": 36,
        "name": "Composition",
        "parent_name": "Ceramic"
    },
    {
        "id": 37,
        "name": "Count",
        "parent_name": "Ceramic"
    },
    {
        "id": 38,
        "name": "Design/Decorative Element",
        "parent_name": "Ceramic"
    },
    {
        "id": 39,
        "name": "Form",
        "parent_name": "Ceramic"
    },
    {
        "id": 40,
        "name": "Measurement",
        "parent_name": "Ceramic"
    },
    {
        "id": 41,
        "name": "Paint",
        "parent_name": "Ceramic"
    },
    {
        "id": 42,
        "name": "Part",
        "parent_name": "Ceramic"
    },
    {
        "id": 43,
        "name": "Paste",
        "parent_name": "Ceramic"
    },
    {
        "id": 44,
        "name": "Residue",
        "parent_name": "Ceramic"
    },
    {
        "id": 45,
        "name": "Surface Treatment",
        "parent_name": "Ceramic"
    },
    {
        "id": 46,
        "name": "Temper/Inclusions",
        "parent_name": "Ceramic"
    },
    {
        "id": 47,
        "name": "Type",
        "parent_name": "Ceramic"
    },
    {
        "id": 48,
        "name": "Variety/Subtype",
        "parent_name": "Ceramic"
    },
    {
        "id": 49,
        "name": "Ware",
        "parent_name": "Ceramic"
    },
    {
        "id": 50,
        "name": "Weight",
        "parent_name": "Ceramic"
    },
    {
        "id": 238,
        "name": "Other",
        "parent_name": "Ceramic"
    },
    {
        "id": 51,
        "name": "Count",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 52,
        "name": "Form",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 53,
        "name": "Material",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 54,
        "name": "Measurement",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 55,
        "name": "Retouch",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 56,
        "name": "Type",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 57,
        "name": "Weight",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 239,
        "name": "Other",
        "parent_name": "Chipped Stone"
    },
    {
        "id": 58,
        "name": "Method",
        "parent_name": "Dating Sample"
    },
    {
        "id": 59,
        "name": "Date",
        "parent_name": "Dating Sample"
    },
    {
        "id": 60,
        "name": "Error",
        "parent_name": "Dating Sample"
    },
    {
        "id": 240,
        "name": "Other",
        "parent_name": "Dating Sample"
    },
    {
        "id": 61,
        "name": "Age",
        "parent_name": "Fauna"
    },
    {
        "id": 62,
        "name": "Anterior/Posterior",
        "parent_name": "Fauna"
    },
    {
        "id": 63,
        "name": "Bone Artifact Form",
        "parent_name": "Fauna"
    },
    {
        "id": 64,
        "name": "Breakage",
        "parent_name": "Fauna"
    },
    {
        "id": 65,
        "name": "Burning ",
        "parent_name": "Fauna"
    },
    {
        "id": 66,
        "name": "Butchering",
        "parent_name": "Fauna"
    },
    {
        "id": 67,
        "name": "Completeness",
        "parent_name": "Fauna"
    },
    {
        "id": 68,
        "name": "Condition",
        "parent_name": "Fauna"
    },
    {
        "id": 69,
        "name": "Count",
        "parent_name": "Fauna"
    },
    {
        "id": 70,
        "name": "Cultural Modification",
        "parent_name": "Fauna"
    },
    {
        "id": 71,
        "name": "Digestion",
        "parent_name": "Fauna"
    },
    {
        "id": 72,
        "name": "Dorsal/Ventral",
        "parent_name": "Fauna"
    },
    {
        "id": 73,
        "name": "Element",
        "parent_name": "Fauna"
    },
    {
        "id": 74,
        "name": "Erosion",
        "parent_name": "Fauna"
    },
    {
        "id": 75,
        "name": "Fusion",
        "parent_name": "Fauna"
    },
    {
        "id": 76,
        "name": "Gnawing/Animal Modification",
        "parent_name": "Fauna"
    },
    {
        "id": 77,
        "name": "Measurement",
        "parent_name": "Fauna"
    },
    {
        "id": 78,
        "name": "Modification",
        "parent_name": "Fauna"
    },
    {
        "id": 79,
        "name": "Natural Modification",
        "parent_name": "Fauna"
    },
    {
        "id": 80,
        "name": "Pathologies",
        "parent_name": "Fauna"
    },
    {
        "id": 81,
        "name": "Portion/Proximal/Distal",
        "parent_name": "Fauna"
    },
    {
        "id": 82,
        "name": "Sex",
        "parent_name": "Fauna"
    },
    {
        "id": 83,
        "name": "Side",
        "parent_name": "Fauna"
    },
    {
        "id": 84,
        "name": "Spiral Fracture",
        "parent_name": "Fauna"
    },
    {
        "id": 85,
        "name": "Taxon",
        "parent_name": "Fauna"
    },
    {
        "id": 86,
        "name": "Weathering",
        "parent_name": "Fauna"
    },
    {
        "id": 87,
        "name": "Weight",
        "parent_name": "Fauna"
    },
    {
        "id": 88,
        "name": "Zone",
        "parent_name": "Fauna"
    },
    {
        "id": 89,
        "name": "Zone Scheme",
        "parent_name": "Fauna"
    },
    {
        "id": 241,
        "name": "Other",
        "parent_name": "Fauna"
    },
    {
        "id": 90,
        "name": "Count",
        "parent_name": "Figurine"
    },
    {
        "id": 91,
        "name": "Form",
        "parent_name": "Figurine"
    },
    {
        "id": 92,
        "name": "Material",
        "parent_name": "Figurine"
    },
    {
        "id": 93,
        "name": "Measurement",
        "parent_name": "Figurine"
    },
    {
        "id": 94,
        "name": "Style/Type",
        "parent_name": "Figurine"
    },
    {
        "id": 242,
        "name": "Other",
        "parent_name": "Figurine"
    },
    {
        "id": 95,
        "name": "Count",
        "parent_name": "Glass"
    },
    {
        "id": 96,
        "name": "Date",
        "parent_name": "Glass"
    },
    {
        "id": 97,
        "name": "Form",
        "parent_name": "Glass"
    },
    {
        "id": 98,
        "name": "Maker/Manufacturer",
        "parent_name": "Glass"
    },
    {
        "id": 99,
        "name": "Material",
        "parent_name": "Glass"
    },
    {
        "id": 100,
        "name": "Measurement",
        "parent_name": "Glass"
    },
    {
        "id": 101,
        "name": "Weight",
        "parent_name": "Glass"
    },
    {
        "id": 243,
        "name": "Other",
        "parent_name": "Glass"
    },
    {
        "id": 102,
        "name": "Completeness",
        "parent_name": "Ground Stone"
    },
    {
        "id": 103,
        "name": "Count",
        "parent_name": "Ground Stone"
    },
    {
        "id": 104,
        "name": "Form",
        "parent_name": "Ground Stone"
    },
    {
        "id": 105,
        "name": "Material",
        "parent_name": "Ground Stone"
    },
    {
        "id": 106,
        "name": "Measurement",
        "parent_name": "Ground Stone"
    },
    {
        "id": 107,
        "name": "Weight",
        "parent_name": "Ground Stone"
    },
    {
        "id": 244,
        "name": "Other",
        "parent_name": "Ground Stone"
    },
    {
        "id": 108,
        "name": "Count",
        "parent_name": "Historic Other"
    },
    {
        "id": 109,
        "name": "Date",
        "parent_name": "Historic Other"
    },
    {
        "id": 110,
        "name": "Form",
        "parent_name": "Historic Other"
    },
    {
        "id": 111,
        "name": "Maker/Manufacturer",
        "parent_name": "Historic Other"
    },
    {
        "id": 112,
        "name": "Material",
        "parent_name": "Historic Other"
    },
    {
        "id": 113,
        "name": "Measurement",
        "parent_name": "Historic Other"
    },
    {
        "id": 114,
        "name": "Weight",
        "parent_name": "Historic Other"
    },
    {
        "id": 245,
        "name": "Other",
        "parent_name": "Historic Other"
    },
    {
        "id": 115,
        "name": "Body Position/Flexure",
        "parent_name": "Human Burial"
    },
    {
        "id": 116,
        "name": "Body Posture",
        "parent_name": "Human Burial"
    },
    {
        "id": 117,
        "name": "Body Preparation",
        "parent_name": "Human Burial"
    },
    {
        "id": 118,
        "name": "Burial Accompaniment",
        "parent_name": "Human Burial"
    },
    {
        "id": 119,
        "name": "Burial Container ",
        "parent_name": "Human Burial"
    },
    {
        "id": 120,
        "name": "Burial Facility",
        "parent_name": "Human Burial"
    },
    {
        "id": 121,
        "name": "Count",
        "parent_name": "Human Burial"
    },
    {
        "id": 122,
        "name": "Disturbance",
        "parent_name": "Human Burial"
    },
    {
        "id": 123,
        "name": "Facing",
        "parent_name": "Human Burial"
    },
    {
        "id": 124,
        "name": "Measurement",
        "parent_name": "Human Burial"
    },
    {
        "id": 125,
        "name": "Orientation/Alignment ",
        "parent_name": "Human Burial"
    },
    {
        "id": 126,
        "name": "Preservation",
        "parent_name": "Human Burial"
    },
    {
        "id": 127,
        "name": "Type of Interment",
        "parent_name": "Human Burial"
    },
    {
        "id": 246,
        "name": "Other",
        "parent_name": "Human Burial"
    },
    {
        "id": 128,
        "name": "Buccal/Lingual/Occlusal",
        "parent_name": "Human Dental"
    },
    {
        "id": 129,
        "name": "Chemical Assay",
        "parent_name": "Human Dental"
    },
    {
        "id": 130,
        "name": "Count   ",
        "parent_name": "Human Dental"
    },
    {
        "id": 131,
        "name": "Cultural Modification",
        "parent_name": "Human Dental"
    },
    {
        "id": 132,
        "name": "Dental Pathologies",
        "parent_name": "Human Dental"
    },
    {
        "id": 133,
        "name": "Dental Wear",
        "parent_name": "Human Dental"
    },
    {
        "id": 134,
        "name": "Enamel Defects",
        "parent_name": "Human Dental"
    },
    {
        "id": 135,
        "name": "Maxillary/Mandibular",
        "parent_name": "Human Dental"
    },
    {
        "id": 136,
        "name": "Measurement",
        "parent_name": "Human Dental"
    },
    {
        "id": 137,
        "name": "Permanent/Deciduous",
        "parent_name": "Human Dental"
    },
    {
        "id": 138,
        "name": "Tooth (element)",
        "parent_name": "Human Dental"
    },
    {
        "id": 247,
        "name": "Other",
        "parent_name": "Human Dental"
    },
    {
        "id": 139,
        "name": "Age",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 140,
        "name": "Age Criteria",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 141,
        "name": "Articulation",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 142,
        "name": "Bone Segment (proximal/distal)",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 143,
        "name": "Chemical Assay",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 144,
        "name": "Completeness",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 145,
        "name": "Condition",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 146,
        "name": "Count",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 147,
        "name": "Cranial Deformation",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 148,
        "name": "Crematory Burning",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 149,
        "name": "Cultural Modification ",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 150,
        "name": "Diet",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 151,
        "name": "Distrubance",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 152,
        "name": "Disturbance sources",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 153,
        "name": "Element",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 154,
        "name": "Epiphyseal Union",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 155,
        "name": "Fracture/Breakage",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 156,
        "name": "Health ",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 157,
        "name": "Measurement",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 158,
        "name": "Nonmetric Trait",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 159,
        "name": "Pathologies/Trauma",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 160,
        "name": "Preservation",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 161,
        "name": "Sex",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 162,
        "name": "Sex criteria",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 163,
        "name": "Side",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 164,
        "name": "Weight",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 248,
        "name": "Other",
        "parent_name": "Human Skeletal"
    },
    {
        "id": 165,
        "name": "Code",
        "parent_name": "Lookup"
    },
    {
        "id": 166,
        "name": "Description",
        "parent_name": "Lookup"
    },
    {
        "id": 167,
        "name": "Label",
        "parent_name": "Lookup"
    },
    {
        "id": 168,
        "name": "Notes",
        "parent_name": "Lookup"
    },
    {
        "id": 249,
        "name": "Other",
        "parent_name": "Lookup"
    },
    {
        "id": 169,
        "name": "Count",
        "parent_name": "Macrobotanical"
    },
    {
        "id": 170,
        "name": "Taxon",
        "parent_name": "Macrobotanical"
    },
    {
        "id": 250,
        "name": "Other",
        "parent_name": "Macrobotanical"
    },
    {
        "id": 171,
        "name": "Count",
        "parent_name": "Metal"
    },
    {
        "id": 172,
        "name": "Date",
        "parent_name": "Metal"
    },
    {
        "id": 173,
        "name": "Form",
        "parent_name": "Metal"
    },
    {
        "id": 174,
        "name": "Maker/Manufacturer",
        "parent_name": "Metal"
    },
    {
        "id": 175,
        "name": "Material",
        "parent_name": "Metal"
    },
    {
        "id": 176,
        "name": "Measurement",
        "parent_name": "Metal"
    },
    {
        "id": 177,
        "name": "Weight",
        "parent_name": "Metal"
    },
    {
        "id": 251,
        "name": "Other",
        "parent_name": "Metal"
    },
    {
        "id": 178,
        "name": "Count",
        "parent_name": "Mineral"
    },
    {
        "id": 179,
        "name": "Form",
        "parent_name": "Mineral"
    },
    {
        "id": 180,
        "name": "Measurement",
        "parent_name": "Mineral"
    },
    {
        "id": 181,
        "name": "Mineral Type",
        "parent_name": "Mineral"
    },
    {
        "id": 182,
        "name": "Weight",
        "parent_name": "Mineral"
    },
    {
        "id": 252,
        "name": "Other",
        "parent_name": "Mineral"
    },
    {
        "id": 183,
        "name": "Direction",
        "parent_name": "Photograph"
    },
    {
        "id": 184,
        "name": "Film Type",
        "parent_name": "Photograph"
    },
    {
        "id": 185,
        "name": "Frame",
        "parent_name": "Photograph"
    },
    {
        "id": 186,
        "name": "ID",
        "parent_name": "Photograph"
    },
    {
        "id": 187,
        "name": "Roll",
        "parent_name": "Photograph"
    },
    {
        "id": 188,
        "name": "Subject",
        "parent_name": "Photograph"
    },
    {
        "id": 253,
        "name": "Other",
        "parent_name": "Photograph"
    },
    {
        "id": 189,
        "name": "Count",
        "parent_name": "Pollen"
    },
    {
        "id": 190,
        "name": "Taxon",
        "parent_name": "Pollen"
    },
    {
        "id": 254,
        "name": "Other",
        "parent_name": "Pollen"
    },
    {
        "id": 191,
        "name": "Context",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 192,
        "name": "Date",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 193,
        "name": "Depth",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 194,
        "name": "East",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 195,
        "name": "Excavation Method",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 196,
        "name": "Feature ID/Number",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 197,
        "name": "Feature Type",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 198,
        "name": "Horizontal Location",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 199,
        "name": "Inclusions",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 200,
        "name": "Item/Slash ",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 201,
        "name": "Level",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 202,
        "name": "Locus",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 203,
        "name": "Lot",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 204,
        "name": "Measurement",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 205,
        "name": "North",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 206,
        "name": "Project",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 207,
        "name": "Recovery Method",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 208,
        "name": "Sampling",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 209,
        "name": "Screening",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 210,
        "name": "Site",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 211,
        "name": "Soil Color",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 212,
        "name": "Stratum",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 213,
        "name": "Unit",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 214,
        "name": "Vertical Position",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 215,
        "name": "Volume",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 255,
        "name": "Other",
        "parent_name": "Provenience and Context"
    },
    {
        "id": 216,
        "name": "Exposure",
        "parent_name": "Rock Art"
    },
    {
        "id": 217,
        "name": "Form",
        "parent_name": "Rock Art"
    },
    {
        "id": 218,
        "name": "Style",
        "parent_name": "Rock Art"
    },
    {
        "id": 219,
        "name": "Technology",
        "parent_name": "Rock Art"
    },
    {
        "id": 256,
        "name": "Other",
        "parent_name": "Rock Art"
    },
    {
        "id": 220,
        "name": "Completeness",
        "parent_name": "Shell"
    },
    {
        "id": 221,
        "name": "Count",
        "parent_name": "Shell"
    },
    {
        "id": 222,
        "name": "Measurement",
        "parent_name": "Shell"
    },
    {
        "id": 223,
        "name": "Modification",
        "parent_name": "Shell"
    },
    {
        "id": 224,
        "name": "Taxon",
        "parent_name": "Shell"
    },
    {
        "id": 225,
        "name": "Weight",
        "parent_name": "Shell"
    },
    {
        "id": 257,
        "name": "Other",
        "parent_name": "Shell"
    },
    {
        "id": 226,
        "name": "Box Number",
        "parent_name": "Storage"
    },
    {
        "id": 227,
        "name": "Location",
        "parent_name": "Storage"
    },
    {
        "id": 258,
        "name": "Other",
        "parent_name": "Storage"
    },
    {
        "id": 228,
        "name": "Count",
        "parent_name": "Textile"
    },
    {
        "id": 229,
        "name": "Design",
        "parent_name": "Textile"
    },
    {
        "id": 230,
        "name": "Form",
        "parent_name": "Textile"
    },
    {
        "id": 231,
        "name": "Function",
        "parent_name": "Textile"
    },
    {
        "id": 232,
        "name": "Material",
        "parent_name": "Textile"
    },
    {
        "id": 233,
        "name": "Measurement",
        "parent_name": "Textile"
    },
    {
        "id": 234,
        "name": "Technique",
        "parent_name": "Textile"
    },
    {
        "id": 235,
        "name": "Weight",
        "parent_name": "Textile"
    },
    {
        "id": 259,
        "name": "Other",
        "parent_name": "Textile"
    }
]
</script>

<script src='https://ajax.googleapis.com/ajax/libs/angularjs/1.3.0/angular.min.js'></script>
<script src="/js/tdar.integration.edit-angular.js"></script>
<script src="/includes/angular-modal-service-0.4.0/angular-modal-service.js"></script>


</body>

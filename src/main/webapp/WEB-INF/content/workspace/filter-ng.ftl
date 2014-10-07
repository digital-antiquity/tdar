<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "filter-macros.ftl" as edit>

<head>
    <title>Filter Ontology Values</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <!--todo: knockout can do recursive templates & adhoc arrays, but I"m too lazy right now  -->
    <style type="text/css">
    </style>
</head>
<body class="filter-ontology">


<div class="inthead">
    <div id="divIntegrationNav" class="">
        <ol class="breadcrumb">
            <li class="active"><a href="filter-ng">Choose Ontologies & Datasets</a> <span class="divider"> / </span> </li>
            <li><a href="select-columns-ng">Choose Display Columns</a> <span class="divider"> / </span> </li>
            <li><a href="select-columns-ng">Display Integration Results</a></li>
        </ol>
    </div>
    <h1><b>Editing Integration</b>:
    Jim's Cool Dataset integration
    </h1>
    <h2><b>Step 1 of 3</b>: Choose Ontologies and Datasets</h2>
</div>

<ul class="nav nav-tabs"  id="ulOntologyTabs">
    <button class="btn pull-right">Add Ontology</button>
    <!-- ko foreach:ontologies -->
    <li><a data-toggle="tab" data-bind="attr: {href:'#ont' + id}, text:title">Loading...</a></li>
    <!-- /ko -->
</ul>
<div class="tab-content" data-bind="foreach:ontologies">
    <div class="tab-pane" data-bind="attr:{id: 'ont'+id}">
        <h2 data-bind="text:title"></h2>
        <table class="table table-bordered table-condensed">
            <thead>
                <tr>
                    <th rowspan="2" style="white-space: nowrap;">&nbsp;</th>
                    <th rowspan="2" style="width:99%">Node Value</th>
                    <th rowspan="1" style="white-space: nowrap;" data-bind="attr:{colspan: integrationColumns.length}">
                        Datasets
                        <button class="btn btn-mini">Add Dataset</button>
                    </th>
                </tr>
                <tr data-bind="foreach: integrationColumns">
                    <th data-bind="text: data_table_display_name"></th>
                </tr>
            </thead>
            <tbody data-bind="foreach: nodes">
                <tr>
                    <!--
                    <td style="white-space: nowrap;">
                        <label class="radio inline"><input type="radio" value="NOT_SELECTED" data-bind="checked: selectionPolicy" />Never</label>
                        <label class="radio inline"><input type="radio" value="SELECT_IF_SOME" data-bind="checked: selectionPolicy" />If Any</label>
                        <label class="radio inline"><input type="radio" value="SELECT_IF_ALL" data-bind="checked: selectionPolicy" />If All</label>
                    </td>
                    -->
                    <td><input type="checkbox" name="tbd" data-bind="attr:{checked: selected, id: 'cbont_' + id}"></td>
                    <td style="white-space: nowrap;">
                        <div data-bind="text:display_name, attr:{class:'nodechild'+index.split('.').length}"></div>
                    </td>
                    <!-- ko foreach: participation -->
                    <td data-bind="text:$data">0</td>
                    <!-- /ko -->
                </tr>
            </tbody>
        </table>
    </div>
</div>

<script>
//    $(function(){
//        $("#ulOntologyTabs a:first").tab('show');
//    });
</script>
<script src='//cdnjs.cloudflare.com/ajax/libs/knockout/3.2.0/knockout-min.js'></script>
<script src='/includes/knockout.mapping.js'></script>
<script id="jsonFilterData" type="text/plain">
{"ontologies":[{"id":6029,"title":"Fauna Element Ontology","integrationColumns":[{"id":4742,"name":"element","display_name":"Element","default_ontology_id":6029,"dataset_id":420,
"dataset_title":"Ojo Bonito Faunal Database","data_table_id":4735,"data_table_name":"e_321876_obap","data_table_display_name":"OBAP"},{"id":43046,"name":"element","display_name":"element",
"default_ontology_id":6029,"dataset_id":392595,"dataset_title":"Guadalupe Ruin Fauna","data_table_id":7850,"data_table_name":"e_318592_prpfauna","data_table_display_name":"PRPFAUNA"}],"nodes":[
{"id":13055,"iri":"Articulated_Skeleton","display_name":"Articulated Skeleton","interval_start":1,"interval_end":8,"index":"1","import_order":0},
{"id":13070,"iri":"Articulated_Skeleton_Complete","display_name":"Articulated Skeleton Complete","interval_start":2,"interval_end":2,"index":"1.2","import_order":1},
{"id":13012,"iri":"Articulated_Skeleton_Nearly_Complete","display_name":"Articulated Skeleton Nearly Complete","interval_start":3,"interval_end":3,"index":"1.3","import_order":2},
{"id":13104,"iri":"Articulated_Skeleton_Partial","display_name":"Articulated Skeleton Partial","interval_start":4,"interval_end":7,"index":"1.4","import_order":3},
{"id":12983,"iri":"Articulated_Skeleton_Anterior_Portion","display_name":"Articulated Skeleton Anterior Portion","interval_start":5,"interval_end":5,"index":"1.4.5","import_order":4},
{"id":12999,"iri":"Articulated_Skeleton_Posterior_Portion","display_name":"Articulated Skeleton Posterior Portion","interval_start":6,"interval_end":6,"index":"1.4.6","import_order":5},
{"id":10708,"iri":"Head","display_name":"Head","interval_start":9,"interval_end":147,"index":"9","import_order":6},
{"id":10808,"iri":"Skull","display_name":"Skull","interval_start":10,"interval_end":42,"index":"9.10","import_order":7},
{"id":10749,"iri":"Antler_or_Horncore","display_name":"Antler or Horncore","interval_start":11,"interval_end":15,"index":"9.10.11","import_order":8},
{"id":10802,"iri":"Antler","display_name":"Antler","interval_start":12,"interval_end":12,"index":"9.10.11.12","import_order":9},
{"id":10779,"iri":"Horncore","display_name":"Horncore","interval_start":13,"interval_end":13,"index":"9.10.11.13","import_order":10},
{"id":12994,"iri":"Horn_sheath","display_name":"Horn sheath","interval_start":14,"interval_end":14,"index":"9.10.11.14","import_order":11},
{"id":10660,"iri":"Basiooccipital","display_name":"Basiooccipital","interval_start":16,"interval_end":16,"index":"9.10.16","import_order":12},
{"id":10679,"iri":"Bulla","display_name":"Bulla","interval_start":17,"interval_end":17,"index":"9.10.17","import_order":13},
{"id":10711,"iri":"Dentary","display_name":"Dentary","interval_start":18,"interval_end":18,"index":"9.10.18","import_order":14},
{"id":10745,"iri":"Frontal_or_Parietal","display_name":"Frontal or Parietal","interval_start":19,"interval_end":22,"index":"9.10.19","import_order":15},
{"id":10795,"iri":"Frontal","display_name":"Frontal","interval_start":20,"interval_end":20,"index":"9.10.19.20","import_order":16},
{"id":10656,"iri":"Parietal","display_name":"Parietal","interval_start":21,"interval_end":21,"index":"9.10.19.21","import_order":17},
{"id":10673,"iri":"Hyoid","display_name":"Hyoid","interval_start":23,"interval_end":23,"index":"9.10.23","import_order":18},
{"id":10664,"iri":"Interparietal","display_name":"Interparietal","interval_start":24,"interval_end":24,"index":"9.10.24","import_order":19},
{"id":10767,"iri":"Lacrimal","display_name":"Lacrimal","interval_start":25,"interval_end":25,"index":"9.10.25","import_order":20},
{"id":10811,"iri":"Maxillary_or_Mandibular","display_name":"Maxillary or Mandibular","interval_start":26,"interval_end":29,"index":"9.10.26","import_order":21},
{"id":10833,"iri":"Mandible","display_name":"Mandible","interval_start":27,"interval_end":27,"index":"9.10.26.27","import_order":22},
{"id":10741,"iri":"Maxilla","display_name":"Maxilla","interval_start":28,"interval_end":28,"index":"9.10.26.28","import_order":23},
{"id":10828,"iri":"Nasal","display_name":"Nasal","interval_start":30,"interval_end":30,"index":"9.10.30","import_order":24},
{"id":10797,"iri":"Occipital","display_name":"Occipital","interval_start":31,"interval_end":33,"index":"9.10.31","import_order":25},
{"id":10755,"iri":"Occipital_Condyle","display_name":"Occipital Condyle","interval_start":32,"interval_end":32,"index":"9.10.31.32","import_order":26},
{"id":10744,"iri":"Palatinal","display_name":"Palatinal","interval_start":34,"interval_end":34,"index":"9.10.34","import_order":27},
{"id":10799,"iri":"Petrous","display_name":"Petrous","interval_start":35,"interval_end":35,"index":"9.10.35","import_order":28},
{"id":10713,"iri":"Premaxilla","display_name":"Premaxilla","interval_start":36,"interval_end":36,"index":"9.10.36","import_order":29},
{"id":10651,"iri":"Sphenoid","display_name":"Sphenoid","interval_start":37,"interval_end":37,"index":"9.10.37","import_order":30},
{"id":10866,"iri":"Squamous","display_name":"Squamous","interval_start":38,"interval_end":38,"index":"9.10.38","import_order":31},
{"id":10667,"iri":"Temporal","display_name":"Temporal","interval_start":39,"interval_end":39,"index":"9.10.39","import_order":32},
{"id":10781,"iri":"Vomer","display_name":"Vomer","interval_start":40,"interval_end":40,"index":"9.10.40","import_order":33},
{"id":10650,"iri":"Zygomaticus","display_name":"Zygomaticus","interval_start":41,"interval_end":41,"index":"9.10.41","import_order":34},
{"id":10794,"iri":"Tooth","display_name":"Tooth","interval_start":43,"interval_end":146,"index":"9.43","import_order":35},
{"id":10842,"iri":"Incisor","display_name":"Incisor","interval_start":44,"interval_end":61,"index":"9.43.44","import_order":36},
{"id":10760,"iri":"I1","display_name":"I1","interval_start":45,"interval_end":48,"index":"9.43.44.45","import_order":37},
{"id":10670,"iri":"Lower_I1","display_name":"Lower I1","interval_start":46,"interval_end":46,"index":"9.43.44.45.46","import_order":38},
{"id":10756,"iri":"Upper_I1","display_name":"Upper I1","interval_start":47,"interval_end":47,"index":"9.43.44.45.47","import_order":39},
{"id":10761,"iri":"I2","display_name":"I2","interval_start":49,"interval_end":52,"index":"9.43.44.49","import_order":40},
{"id":10668,"iri":"Lower_I2","display_name":"Lower I2","interval_start":50,"interval_end":50,"index":"9.43.44.49.50","import_order":41},
{"id":10757,"iri":"Upper_I2","display_name":"Upper I2","interval_start":51,"interval_end":51,"index":"9.43.44.49.51","import_order":42},
{"id":10762,"iri":"I3","display_name":"I3","interval_start":53,"interval_end":56,"index":"9.43.44.53","import_order":43},
{"id":10672,"iri":"Lower_I3","display_name":"Lower I3","interval_start":54,"interval_end":54,"index":"9.43.44.53.54","import_order":44},
{"id":10759,"iri":"Upper_I3","display_name":"Upper I3","interval_start":55,"interval_end":55,"index":"9.43.44.53.55","import_order":45},
{"id":10763,"iri":"I4","display_name":"I4","interval_start":57,"interval_end":60,"index":"9.43.44.57","import_order":46},
{"id":10671,"iri":"Lower_I4","display_name":"Lower I4","interval_start":58,"interval_end":58,"index":"9.43.44.57.58","import_order":47},
{"id":10753,"iri":"Upper_I4","display_name":"Upper I4","interval_start":59,"interval_end":59,"index":"9.43.44.57.59","import_order":48},
{"id":10855,"iri":"Deciduous_Incisor","display_name":"Deciduous Incisor","interval_start":62,"interval_end":79,"index":"9.43.62","import_order":49},
{"id":13137,"iri":"i1","display_name":"i1","interval_start":63,"interval_end":66,"index":"9.43.62.63","import_order":50},
{"id":13017,"iri":"Lower_i1","display_name":"Lower i1","interval_start":64,"interval_end":64,"index":"9.43.62.63.64","import_order":51},
{"id":13083,"iri":"Upper_i1","display_name":"Upper i1","interval_start":65,"interval_end":65,"index":"9.43.62.63.65","import_order":52},
{"id":13138,"iri":"i2","display_name":"i2","interval_start":67,"interval_end":70,"index":"9.43.62.67","import_order":53},
{"id":13118,"iri":"Lower_i2","display_name":"Lower i2","interval_start":68,"interval_end":68,"index":"9.43.62.67.68","import_order":54},
{"id":13082,"iri":"Upper_i2","display_name":"Upper i2","interval_start":69,"interval_end":69,"index":"9.43.62.67.69","import_order":55},
{"id":13135,"iri":"i3","display_name":"i3","interval_start":71,"interval_end":74,"index":"9.43.62.71","import_order":56},
{"id":13117,"iri":"Lower_i3","display_name":"Lower i3","interval_start":72,"interval_end":72,"index":"9.43.62.71.72","import_order":57},
{"id":13130,"iri":"Upper_i3","display_name":"Upper i3","interval_start":73,"interval_end":73,"index":"9.43.62.71.73","import_order":58},
{"id":13136,"iri":"i4","display_name":"i4","interval_start":75,"interval_end":78,"index":"9.43.62.75","import_order":59},
{"id":13116,"iri":"Lower_i4","display_name":"Lower i4","interval_start":76,"interval_end":76,"index":"9.43.62.75.76","import_order":60},
{"id":13129,"iri":"Upper_i4","display_name":"Upper i4","interval_start":77,"interval_end":77,"index":"9.43.62.75.77","import_order":61},
{"id":10722,"iri":"Canine","display_name":"Canine","interval_start":80,"interval_end":83,"index":"9.43.80","import_order":62},
{"id":10823,"iri":"Lower_C","display_name":"Lower C","interval_start":81,"interval_end":81,"index":"9.43.80.81","import_order":63},
{"id":10800,"iri":"Upper_C","display_name":"Upper C","interval_start":82,"interval_end":82,"index":"9.43.80.82","import_order":64},
{"id":10714,"iri":"Deciduous_Canine","display_name":"Deciduous Canine","interval_start":84,"interval_end":87,"index":"9.43.84","import_order":65},
{"id":13133,"iri":"Lower_c","display_name":"Lower c","interval_start":85,"interval_end":85,"index":"9.43.84.85","import_order":66},
{"id":13071,"iri":"Upper_c","display_name":"Upper c","interval_start":86,"interval_end":86,"index":"9.43.84.86","import_order":67},
{"id":10827,"iri":"Molar_or_Premolar","display_name":"Molar or Premolar","interval_start":88,"interval_end":143,"index":"9.43.88","import_order":68},
{"id":10860,"iri":"Premolar","display_name":"Premolar","interval_start":89,"interval_end":108,"index":"9.43.88.89","import_order":69},
{"id":10640,"iri":"P1","display_name":"P1","interval_start":90,"interval_end":93,"index":"9.43.88.89.90","import_order":70},
{"id":10818,"iri":"Lower_P1","display_name":"Lower P1","interval_start":91,"interval_end":91,"index":"9.43.88.89.90.91","import_order":71},
{"id":10733,"iri":"Upper_P1","display_name":"Upper P1","interval_start":92,"interval_end":92,"index":"9.43.88.89.90.92","import_order":72},
{"id":10642,"iri":"P2","display_name":"P2","interval_start":94,"interval_end":97,"index":"9.43.88.89.94","import_order":73},
{"id":10836,"iri":"Lower_P2","display_name":"Lower P2","interval_start":95,"interval_end":95,"index":"9.43.88.89.94.95","import_order":74},
{"id":10734,"iri":"Upper_P2","display_name":"Upper P2","interval_start":96,"interval_end":96,"index":"9.43.88.89.94.96","import_order":75},
{"id":10774,"iri":"P3_or_P4","display_name":"P3 or P4","interval_start":98,"interval_end":107,"index":"9.43.88.89.98","import_order":76},
{"id":10645,"iri":"P3","display_name":"P3","interval_start":99,"interval_end":102,"index":"9.43.88.89.98.99","import_order":77},
{"id":10835,"iri":"Lower_P3","display_name":"Lower P3","interval_start":100,"interval_end":100,"index":"9.43.88.89.98.99.100","import_order":78},
{"id":10737,"iri":"Upper_P3","display_name":"Upper P3","interval_start":101,"interval_end":101,"index":"9.43.88.89.98.99.101","import_order":79},
{"id":10647,"iri":"P4","display_name":"P4","interval_start":103,"interval_end":106,"index":"9.43.88.89.98.103","import_order":80},
{"id":10837,"iri":"Lower_P4","display_name":"Lower P4","interval_start":104,"interval_end":104,"index":"9.43.88.89.98.103.104","import_order":81},
{"id":10736,"iri":"Upper_P4","display_name":"Upper P4","interval_start":105,"interval_end":105,"index":"9.43.88.89.98.103.105","import_order":82},
{"id":10764,"iri":"Deciduous_Premolar","display_name":"Deciduous Premolar","interval_start":109,"interval_end":126,"index":"9.43.88.109","import_order":83},
{"id":10719,"iri":"dp1","display_name":"dp1","interval_start":110,"interval_end":113,"index":"9.43.88.109.110","import_order":84},
{"id":10766,"iri":"Lower_dp1","display_name":"Lower dp1","interval_start":111,"interval_end":111,"index":"9.43.88.109.110.111","import_order":85},
{"id":10806,"iri":"Upper_DP1","display_name":"Upper DP1","interval_start":112,"interval_end":112,"index":"9.43.88.109.110.112","import_order":86},
{"id":10717,"iri":"dp2","display_name":"dp2","interval_start":114,"interval_end":117,"index":"9.43.88.109.114","import_order":87},
{"id":10771,"iri":"Lower_dp2","display_name":"Lower dp2","interval_start":115,"interval_end":115,"index":"9.43.88.109.114.115","import_order":88},
{"id":10639,"iri":"Upper_dp2","display_name":"Upper dp2","interval_start":116,"interval_end":116,"index":"9.43.88.109.114.116","import_order":89},
{"id":10716,"iri":"dp3","display_name":"dp3","interval_start":118,"interval_end":121,"index":"9.43.88.109.118","import_order":90},
{"id":10769,"iri":"Lower_dp3","display_name":"Lower dp3","interval_start":119,"interval_end":119,"index":"9.43.88.109.118.119","import_order":91},
{"id":10644,"iri":"Upper_dp3","display_name":"Upper dp3","interval_start":120,"interval_end":120,"index":"9.43.88.109.118.120","import_order":92},
{"id":10681,"iri":"dp4","display_name":"dp4","interval_start":122,"interval_end":125,"index":"9.43.88.109.122","import_order":93},
{"id":10772,"iri":"Lower_dp4","display_name":"Lower dp4","interval_start":123,"interval_end":123,"index":"9.43.88.109.122.123","import_order":94},
{"id":10641,"iri":"Upper_dp4","display_name":"Upper dp4","interval_start":124,"interval_end":124,"index":"9.43.88.109.122.124","import_order":95},
{"id":10788,"iri":"Molar","display_name":"Molar","interval_start":127,"interval_end":142,"index":"9.43.88.127","import_order":96},
{"id":10718,"iri":"M1_or_M2","display_name":"M1 or M2","interval_start":128,"interval_end":137,"index":"9.43.88.127.128","import_order":97},
{"id":13026,"iri":"M1","display_name":"M1","interval_start":129,"interval_end":132,"index":"9.43.88.127.128.129","import_order":98},
{"id":10791,"iri":"Lower_M1","display_name":"Lower M1","interval_start":130,"interval_end":130,"index":"9.43.88.127.128.129.130","import_order":99},
{"id":10702,"iri":"Upper_M1","display_name":"Upper M1","interval_start":131,"interval_end":131,"index":"9.43.88.127.128.129.131","import_order":100},
{"id":13024,"iri":"M2","display_name":"M2","interval_start":133,"interval_end":136,"index":"9.43.88.127.128.133","import_order":101},
{"id":10792,"iri":"Lower_M2","display_name":"Lower M2","interval_start":134,"interval_end":134,"index":"9.43.88.127.128.133.134","import_order":102},
{"id":10701,"iri":"Upper_M2","display_name":"Upper M2","interval_start":135,"interval_end":135,"index":"9.43.88.127.128.133.135","import_order":103},
{"id":13023,"iri":"M3","display_name":"M3","interval_start":138,"interval_end":141,"index":"9.43.88.127.138","import_order":104},
{"id":10789,"iri":"Lower_M3","display_name":"Lower M3","interval_start":139,"interval_end":139,"index":"9.43.88.127.138.139","import_order":105},
{"id":10699,"iri":"Upper_M3","display_name":"Upper M3","interval_start":140,"interval_end":140,"index":"9.43.88.127.138.140","import_order":106},
{"id":10851,"iri":"Enamel_Fragment","display_name":"Enamel Fragment","interval_start":144,"interval_end":144,"index":"9.43.144","import_order":107},
{"id":10653,"iri":"Root_Fragment","display_name":"Root Fragment","interval_start":145,"interval_end":145,"index":"9.43.145","import_order":108},
{"id":10657,"iri":"Axial","display_name":"Axial","interval_start":148,"interval_end":165,"index":"148","import_order":109},
{"id":10655,"iri":"Baculum","display_name":"Baculum","interval_start":149,"interval_end":149,"index":"148.149","import_order":110},
{"id":10810,"iri":"Rib","display_name":"Rib","interval_start":150,"interval_end":152,"index":"148.150","import_order":111},
{"id":10858,"iri":"Costal_Cartilage","display_name":"Costal Cartilage","interval_start":151,"interval_end":151,"index":"148.150.151","import_order":112},
{"id":10782,"iri":"Sternum","display_name":"Sternum","interval_start":153,"interval_end":153,"index":"148.153","import_order":113},
{"id":10663,"iri":"Vertebra","display_name":"Vertebra","interval_start":154,"interval_end":164,"index":"148.154","import_order":114},
{"id":10739,"iri":"Cervical_Vertebra","display_name":"Cervical Vertebra","interval_start":155,"interval_end":158,"index":"148.154.155","import_order":115},
{"id":10865,"iri":"Atlas","display_name":"Atlas","interval_start":156,"interval_end":156,"index":"148.154.155.156","import_order":116},
{"id":10659,"iri":"Axis","display_name":"Axis","interval_start":157,"interval_end":157,"index":"148.154.155.157","import_order":117},
{"id":10695,"iri":"Thoracic_Vertebra","display_name":"Thoracic Vertebra","interval_start":159,"interval_end":159,"index":"148.154.159","import_order":118},
{"id":10652,"iri":"Lumbar_Vertebra","display_name":"Lumbar Vertebra","interval_start":160,"interval_end":160,"index":"148.154.160","import_order":119},
{"id":10690,"iri":"Sacrum","display_name":"Sacrum","interval_start":161,"interval_end":161,"index":"148.154.161","import_order":120},
{"id":10780,"iri":"Caudal_Vertebra","display_name":"Caudal Vertebra","interval_start":162,"interval_end":162,"index":"148.154.162","import_order":121},
{"id":10758,"iri":"Vertebral_Pad","display_name":"Vertebral Pad","interval_start":163,"interval_end":163,"index":"148.154.163","import_order":122},
{"id":10724,"iri":"Appendicular","display_name":"Appendicular","interval_start":166,"interval_end":201,"index":"166","import_order":123},
{"id":10723,"iri":"Forelimb","display_name":"Forelimb","interval_start":167,"interval_end":175,"index":"166.167","import_order":124},
{"id":10852,"iri":"Clavicle","display_name":"Clavicle","interval_start":168,"interval_end":168,"index":"166.167.168","import_order":125},
{"id":10680,"iri":"Humerus","display_name":"Humerus","interval_start":169,"interval_end":169,"index":"166.167.169","import_order":126},
{"id":10715,"iri":"Radius_or_Ulna","display_name":"Radius or Ulna","interval_start":170,"interval_end":173,"index":"166.167.170","import_order":127},
{"id":13106,"iri":"Radius_or_Radioulna","display_name":"Radius or Radioulna","interval_start":171,"interval_end":171,"index":"166.167.170.171","import_order":128},
{"id":10812,"iri":"Ulna","display_name":"Ulna","interval_start":172,"interval_end":172,"index":"166.167.170.172","import_order":129},
{"id":10738,"iri":"Scapula","display_name":"Scapula","interval_start":174,"interval_end":174,"index":"166.167.174","import_order":130},
{"id":10793,"iri":"Hindlimb","display_name":"Hindlimb","interval_start":176,"interval_end":200,"index":"166.176","import_order":131},
{"id":10742,"iri":"Femur","display_name":"Femur","interval_start":177,"interval_end":177,"index":"166.176.177","import_order":132},
{"id":10669,"iri":"Patella","display_name":"Patella","interval_start":178,"interval_end":178,"index":"166.176.178","import_order":133},
{"id":10845,"iri":"Pelvis","display_name":"Pelvis","interval_start":179,"interval_end":195,"index":"166.176.179","import_order":134},
{"id":10815,"iri":"Acetabulum","display_name":"Acetabulum","interval_start":180,"interval_end":180,"index":"166.176.179.180","import_order":135},
{"id":10750,"iri":"Ilium","display_name":"Ilium","interval_start":181,"interval_end":181,"index":"166.176.179.181","import_order":136},
{"id":10807,"iri":"Ischium","display_name":"Ischium","interval_start":182,"interval_end":182,"index":"166.176.179.182","import_order":137},
{"id":10824,"iri":"Pubis","display_name":"Pubis","interval_start":183,"interval_end":183,"index":"166.176.179.183","import_order":138},
{"id":13036,"iri":"Ilium_w__Acetabulum","display_name":"Ilium w/ Acetabulum","interval_start":184,"interval_end":184,"index":"166.176.179.184","import_order":139},
{"id":13000,"iri":"Ischium_w__Acetabulum","display_name":"Ischium w/ Acetabulum","interval_start":185,"interval_end":185,"index":"166.176.179.185","import_order":140},
{"id":13002,"iri":"Pubis_w__Acetabulum","display_name":"Pubis w/ Acetabulum","interval_start":186,"interval_end":186,"index":"166.176.179.186","import_order":141},
{"id":13063,"iri":"Ilium___Ischium_w_o_Acetabulum","display_name":"Ilium & Ischium w/o Acetabulum","interval_start":187,"interval_end":187,"index":"166.176.179.187","import_order":142},
{"id":13058,"iri":"Ilium___Ischium_w__Acetabulum","display_name":"Ilium & Ischium w/ Acetabulum","interval_start":188,"interval_end":188,"index":"166.176.179.188","import_order":143},
{"id":13027,"iri":"Ilium___Pubis_w_o_Acetabulum","display_name":"Ilium & Pubis w/o Acetabulum","interval_start":189,"interval_end":189,"index":"166.176.179.189","import_order":144},
{"id":13044,"iri":"Ilium___Pubis_w__Acetabulum","display_name":"Ilium & Pubis w/ Acetabulum","interval_start":190,"interval_end":190,"index":"166.176.179.190","import_order":145},
{"id":12990,"iri":"Ischium___Pubis_w_o_Acetabulum","display_name":"Ischium & Pubis w/o Acetabulum","interval_start":191,"interval_end":191,"index":"166.176.179.191","import_order":146},
{"id":13065,"iri":"Ischium___Pubis_w__Acetabulum","display_name":"Ischium & Pubis w/ Acetabulum","interval_start":192,"interval_end":192,"index":"166.176.179.192","import_order":147},
{"id":13046,"iri":"Ilium__Ischium___Pubis_w_o_Acetabulum","display_name":"Ilium, Ischium & Pubis w/o Acetabulum","interval_start":193,"interval_end":193,"index":"166.176.179.193","import_order":148},
{"id":13086,"iri":"Ilium__Ischium___Pubis_w__Acetabulum","display_name":"Ilium, Ischium & Pubis w/ Acetabulum","interval_start":194,"interval_end":194,"index":"166.176.179.194","import_order":149},
{"id":10687,"iri":"Tibia_or_Fibula","display_name":"Tibia or Fibula","interval_start":196,"interval_end":199,"index":"166.176.196","import_order":150},
{"id":13052,"iri":"Tibia_or_Tibiofibula_or_Tibiotarsus","display_name":"Tibia or Tibiofibula or Tibiotarsus","interval_start":197,"interval_end":197,"index":"166.176.196.197","import_order":151},
{"id":10787,"iri":"Fibula","display_name":"Fibula","interval_start":198,"interval_end":198,"index":"166.176.196.198","import_order":152},
{"id":10830,"iri":"Foot","display_name":"Foot","interval_start":202,"interval_end":268,"index":"202","import_order":153},
{"id":10697,"iri":"Carpal_or_Tarsal","display_name":"Carpal or Tarsal","interval_start":203,"interval_end":228,"index":"202.203","import_order":154},
{"id":10684,"iri":"Carpal","display_name":"Carpal","interval_start":204,"interval_end":215,"index":"202.203.204","import_order":155},
{"id":10822,"iri":"Scaphoid","display_name":"Scaphoid","interval_start":205,"interval_end":205,"index":"202.203.204.205","import_order":156},
{"id":10643,"iri":"Lunate","display_name":"Lunate","interval_start":206,"interval_end":206,"index":"202.203.204.206","import_order":157},
{"id":10662,"iri":"Scaphoid___Lunate","display_name":"Scaphoid & Lunate","interval_start":207,"interval_end":207,"index":"202.203.204.207","import_order":158},
{"id":10773,"iri":"Triquetrum","display_name":"Triquetrum","interval_start":208,"interval_end":208,"index":"202.203.204.208","import_order":159},
{"id":10798,"iri":"Pisiform","display_name":"Pisiform","interval_start":209,"interval_end":209,"index":"202.203.204.209","import_order":160},
{"id":10868,"iri":"Trapezium","display_name":"Trapezium","interval_start":210,"interval_end":210,"index":"202.203.204.210","import_order":161},
{"id":10821,"iri":"Trapezoid","display_name":"Trapezoid","interval_start":211,"interval_end":211,"index":"202.203.204.211","import_order":162},
{"id":10686,"iri":"Capitate","display_name":"Capitate","interval_start":212,"interval_end":212,"index":"202.203.204.212","import_order":163},
{"id":10727,"iri":"Carpato-Trapezoid","display_name":"Carpato-Trapezoid","interval_start":213,"interval_end":213,"index":"202.203.204.213","import_order":164},
{"id":10648,"iri":"Hamate","display_name":"Hamate","interval_start":214,"interval_end":214,"index":"202.203.204.214","import_order":165},
{"id":10721,"iri":"Tarsal","display_name":"Tarsal","interval_start":216,"interval_end":227,"index":"202.203.216","import_order":166},
{"id":10849,"iri":"Astragalus","display_name":"Astragalus","interval_start":217,"interval_end":217,"index":"202.203.216.217","import_order":167},
{"id":10847,"iri":"Calcaneus","display_name":"Calcaneus","interval_start":218,"interval_end":218,"index":"202.203.216.218","import_order":168},
{"id":10848,"iri":"Cuboid","display_name":"Cuboid","interval_start":219,"interval_end":219,"index":"202.203.216.219","import_order":169},
{"id":10777,"iri":"Lateral_Malleolus","display_name":"Lateral Malleolus","interval_start":220,"interval_end":220,"index":"202.203.216.220","import_order":170},
{"id":10820,"iri":"Navicular","display_name":"Navicular","interval_start":221,"interval_end":221,"index":"202.203.216.221","import_order":171},
{"id":10666,"iri":"Navicular___Cuboid","display_name":"Navicular & Cuboid","interval_start":222,"interval_end":222,"index":"202.203.216.222","import_order":172},
{"id":10691,"iri":"_1st_Cuneiform","display_name":"1st Cuneiform","interval_start":223,"interval_end":223,"index":"202.203.216.223","import_order":173},
{"id":10748,"iri":"_2nd_Cuneiform","display_name":"2nd Cuneiform","interval_start":224,"interval_end":224,"index":"202.203.216.224","import_order":174},
{"id":10649,"iri":"_3rd_Cuneiform","display_name":"3rd Cuneiform","interval_start":225,"interval_end":225,"index":"202.203.216.225","import_order":175},
{"id":10804,"iri":"_2nd___3rd_Cuneiform","display_name":"2nd & 3rd Cuneiform","interval_start":226,"interval_end":226,"index":"202.203.216.226","import_order":176},
{"id":10857,"iri":"Metapodial","display_name":"Metapodial","interval_start":229,"interval_end":250,"index":"202.229","import_order":177},
{"id":10796,"iri":"Metacarpal","display_name":"Metacarpal","interval_start":230,"interval_end":239,"index":"202.229.230","import_order":178},
{"id":10819,"iri":"_1st_Metacarpal","display_name":"1st Metacarpal","interval_start":231,"interval_end":231,"index":"202.229.230.231","import_order":179},
{"id":10867,"iri":"_2nd_Metacarpal","display_name":"2nd Metacarpal","interval_start":232,"interval_end":232,"index":"202.229.230.232","import_order":180},
{"id":10829,"iri":"_3rd_Metacarpal","display_name":"3rd Metacarpal","interval_start":233,"interval_end":233,"index":"202.229.230.233","import_order":181},
{"id":10707,"iri":"_2nd___3rd_Metacarpal","display_name":"2nd & 3rd Metacarpal","interval_start":234,"interval_end":234,"index":"202.229.230.234","import_order":182},
{"id":10754,"iri":"_4th_Metacarpal","display_name":"4th Metacarpal","interval_start":235,"interval_end":235,"index":"202.229.230.235","import_order":183},
{"id":10746,"iri":"_3rd___4th_Metacarpal","display_name":"3rd & 4th Metacarpal","interval_start":236,"interval_end":236,"index":"202.229.230.236","import_order":184},
{"id":10809,"iri":"_5th_Metacarpal","display_name":"5th Metacarpal","interval_start":237,"interval_end":237,"index":"202.229.230.237","import_order":185},
{"id":10784,"iri":"Peripheral_Metacarpal","display_name":"Peripheral Metacarpal","interval_start":238,"interval_end":238,"index":"202.229.230.238","import_order":186},
{"id":10728,"iri":"Metatarsal","display_name":"Metatarsal","interval_start":240,"interval_end":248,"index":"202.229.240","import_order":187},
{"id":10689,"iri":"_1st_Metatarsal","display_name":"1st Metatarsal","interval_start":241,"interval_end":241,"index":"202.229.240.241","import_order":188},
{"id":10692,"iri":"_2nd_Metatarsal","display_name":"2nd Metatarsal","interval_start":242,"interval_end":242,"index":"202.229.240.242","import_order":189},
{"id":10685,"iri":"_3rd_Metatarsal","display_name":"3rd Metatarsal","interval_start":243,"interval_end":243,"index":"202.229.240.243","import_order":190},
{"id":10725,"iri":"_3rd___4th_Metatarsal","display_name":"3rd & 4th Metatarsal","interval_start":244,"interval_end":244,"index":"202.229.240.244","import_order":191},
{"id":10864,"iri":"_4th_Metatarsal","display_name":"4th Metatarsal","interval_start":245,"interval_end":245,"index":"202.229.240.245","import_order":192},
{"id":10646,"iri":"_5th_Metatarsal","display_name":"5th Metatarsal","interval_start":246,"interval_end":246,"index":"202.229.240.246","import_order":193},
{"id":10740,"iri":"Peripheral_Metatarsal","display_name":"Peripheral Metatarsal","interval_start":247,"interval_end":247,"index":"202.229.240.247","import_order":194},
{"id":10801,"iri":"Peripheral_Metapodial","display_name":"Peripheral Metapodial","interval_start":249,"interval_end":249,"index":"202.229.249","import_order":195},
{"id":10658,"iri":"Ossified_Tendon","display_name":"Ossified Tendon","interval_start":251,"interval_end":251,"index":"202.251","import_order":196},
{"id":10688,"iri":"Phalanx","display_name":"Phalanx","interval_start":252,"interval_end":266,"index":"202.252","import_order":197},
{"id":10696,"iri":"_1st_or_2nd_Phalanx","display_name":"1st or 2nd Phalanx","interval_start":253,"interval_end":260,"index":"202.252.253","import_order":198},
{"id":10813,"iri":"_1st_Phalanx","display_name":"1st Phalanx","interval_start":254,"interval_end":256,"index":"202.252.253.254","import_order":199},
{"id":10843,"iri":"Peripheral_1st_Phalanx","display_name":"Peripheral 1st Phalanx","interval_start":255,"interval_end":255,"index":"202.252.253.254.255","import_order":200},
{"id":10825,"iri":"_2nd_Phalanx","display_name":"2nd Phalanx","interval_start":257,"interval_end":259,"index":"202.252.253.257","import_order":201},
{"id":10694,"iri":"Peripheral_2nd_Phalanx","display_name":"Peripheral 2nd Phalanx","interval_start":258,"interval_end":258,"index":"202.252.253.257.258","import_order":202},
{"id":10712,"iri":"_3rd_Phalanx","display_name":"3rd Phalanx","interval_start":261,"interval_end":263,"index":"202.252.261","import_order":203},
{"id":10682,"iri":"Peripheral_3rd_Phalanx","display_name":"Peripheral 3rd Phalanx","interval_start":262,"interval_end":262,"index":"202.252.261.262","import_order":204},
{"id":13001,"iri":"Vestigial_Phalanx","display_name":"Vestigial Phalanx","interval_start":264,"interval_end":264,"index":"202.252.264","import_order":205},
{"id":13074,"iri":"Vestigial_Hoof_Core","display_name":"Vestigial Hoof Core","interval_start":265,"interval_end":265,"index":"202.252.265","import_order":206},
{"id":10700,"iri":"Sesamoid","display_name":"Sesamoid","interval_start":267,"interval_end":267,"index":"202.267","import_order":207},
{"id":13689,"iri":"Amphibian___Reptile_Element_Additional","display_name":"Amphibian & Reptile Element Additional","interval_start":269,"interval_end":298,"index":"269","import_order":208},
{"id":10731,"iri":"Carapace_or_Plastron","display_name":"Carapace or Plastron","interval_start":270,"interval_end":285,"index":"269.270","import_order":209},
{"id":10805,"iri":"Carapace","display_name":"Carapace","interval_start":271,"interval_end":271,"index":"269.270.271","import_order":210},
{"id":13093,"iri":"Costal","display_name":"Costal","interval_start":272,"interval_end":272,"index":"269.270.272","import_order":211},
{"id":13076,"iri":"Entoplastron","display_name":"Entoplastron","interval_start":273,"interval_end":273,"index":"269.270.273","import_order":212},
{"id":13034,"iri":"Epiplastron","display_name":"Epiplastron","interval_start":274,"interval_end":274,"index":"269.270.274","import_order":213},
{"id":13030,"iri":"Epural","display_name":"Epural","interval_start":275,"interval_end":275,"index":"269.270.275","import_order":214},
{"id":13094,"iri":"Hyoplastron","display_name":"Hyoplastron","interval_start":276,"interval_end":276,"index":"269.270.276","import_order":215},
{"id":13061,"iri":"Hypoplastron","display_name":"Hypoplastron","interval_start":277,"interval_end":277,"index":"269.270.277","import_order":216},
{"id":12997,"iri":"Neural","display_name":"Neural","interval_start":278,"interval_end":278,"index":"269.270.278","import_order":217},
{"id":13007,"iri":"Nuchal","display_name":"Nuchal","interval_start":279,"interval_end":279,"index":"269.270.279","import_order":218},
{"id":13004,"iri":"Peripheral","display_name":"Peripheral","interval_start":280,"interval_end":280,"index":"269.270.280","import_order":219},
{"id":10730,"iri":"Plastron","display_name":"Plastron","interval_start":281,"interval_end":281,"index":"269.270.281","import_order":220},
{"id":13039,"iri":"Pygal","display_name":"Pygal","interval_start":282,"interval_end":282,"index":"269.270.282","import_order":221},
{"id":12998,"iri":"Suprapygal","display_name":"Suprapygal","interval_start":283,"interval_end":283,"index":"269.270.283","import_order":222},
{"id":13085,"iri":"Xiphiplstron","display_name":"Xiphiplstron","interval_start":284,"interval_end":284,"index":"269.270.284","import_order":223},
{"id":13690,"iri":"Ectopterygoid_Reptile","display_name":"Ectopterygoid Reptile","interval_start":286,"interval_end":286,"index":"269.286","import_order":224},
{"id":13021,"iri":"Episternum","display_name":"Episternum","interval_start":287,"interval_end":287,"index":"269.287","import_order":225},
{"id":13685,"iri":"Jugal_Reptilian","display_name":"Jugal Reptilian","interval_start":288,"interval_end":288,"index":"269.288","import_order":226},
{"id":13056,"iri":"Mesosternum","display_name":"Mesosternum","interval_start":289,"interval_end":289,"index":"269.289","import_order":227},
{"id":13092,"iri":"Omosternum","display_name":"Omosternum","interval_start":290,"interval_end":290,"index":"269.290","import_order":228},
{"id":13126,"iri":"Prootic","display_name":"Prootic","interval_start":291,"interval_end":291,"index":"269.291","import_order":229},
{"id":13016,"iri":"Pterygoid","display_name":"Pterygoid","interval_start":292,"interval_end":292,"index":"269.292","import_order":230},
{"id":13687,"iri":"Quadrate_Amphibian","display_name":"Quadrate Amphibian","interval_start":293,"interval_end":293,"index":"269.293","import_order":231},
{"id":13051,"iri":"Quadratojugal","display_name":"Quadratojugal","interval_start":294,"interval_end":294,"index":"269.294","import_order":232},
{"id":13090,"iri":"Squamossal","display_name":"Squamossal","interval_start":295,"interval_end":295,"index":"269.295","import_order":233},
{"id":10846,"iri":"Urostyle","display_name":"Urostyle","interval_start":296,"interval_end":296,"index":"269.296","import_order":234},
{"id":13006,"iri":"Xiphisternum","display_name":"Xiphisternum","interval_start":297,"interval_end":297,"index":"269.297","import_order":235},
{"id":13691,"iri":"Bird_Element_Additional","display_name":"Bird Element Additional","interval_start":299,"interval_end":321,"index":"299","import_order":236},
{"id":10770,"iri":"Beak","display_name":"Beak","interval_start":300,"interval_end":300,"index":"299.300","import_order":237},
{"id":10854,"iri":"Coracoid","display_name":"Coracoid","interval_start":301,"interval_end":301,"index":"299.301","import_order":238},
{"id":10751,"iri":"Eggshell","display_name":"Eggshell","interval_start":302,"interval_end":302,"index":"299.302","import_order":239},
{"id":10775,"iri":"Furculum","display_name":"Furculum","interval_start":303,"interval_end":303,"index":"299.303","import_order":240},
{"id":13693,"iri":"Jugal_Bird","display_name":"Jugal Bird","interval_start":304,"interval_end":304,"index":"299.304","import_order":241},
{"id":13089,"iri":"Lumbosacral_vertebrae","display_name":"Lumbosacral vertebrae","interval_start":305,"interval_end":305,"index":"299.305","import_order":242},
{"id":13080,"iri":"Orbital_ring","display_name":"Orbital ring","interval_start":306,"interval_end":306,"index":"299.306","import_order":243},
{"id":10743,"iri":"Pollex","display_name":"Pollex","interval_start":307,"interval_end":307,"index":"299.307","import_order":244},
{"id":10665,"iri":"Pygostyle","display_name":"Pygostyle","interval_start":308,"interval_end":308,"index":"299.308","import_order":245},
{"id":13692,"iri":"Quadrate_Bird","display_name":"Quadrate Bird","interval_start":309,"interval_end":309,"index":"299.309","import_order":246},
{"id":13050,"iri":"Scapholunar","display_name":"Scapholunar","interval_start":310,"interval_end":310,"index":"299.310","import_order":247},
{"id":10752,"iri":"Synsacrum","display_name":"Synsacrum","interval_start":311,"interval_end":311,"index":"299.311","import_order":248},
{"id":13064,"iri":"Tracheal_Cartilage","display_name":"Tracheal Cartilage","interval_start":312,"interval_end":312,"index":"299.312","import_order":249},
{"id":10814,"iri":"Wing_Phalanges","display_name":"Wing Phalanges","interval_start":313,"interval_end":320,"index":"299.313","import_order":250},
{"id":13066,"iri":"Digit_1","display_name":"Digit 1","interval_start":314,"interval_end":314,"index":"299.313.314","import_order":251},
{"id":13060,"iri":"Digit_2","display_name":"Digit 2","interval_start":315,"interval_end":318,"index":"299.313.315","import_order":252},
{"id":13031,"iri":"_1st_Phalanx_Digit_2","display_name":"1st Phalanx Digit 2","interval_start":316,"interval_end":316,"index":"299.313.315.316","import_order":253},
{"id":13049,"iri":"_2nd_Phalanx_Digit_2","display_name":"2nd Phalanx Digit 2","interval_start":317,"interval_end":317,"index":"299.313.315.317","import_order":254},
{"id":13062,"iri":"Digit_3","display_name":"Digit 3","interval_start":319,"interval_end":319,"index":"299.313.319","import_order":255},
{"id":13686,"iri":"Fish_Element_Additional","display_name":"Fish Element Additional","interval_start":322,"interval_end":331,"index":"322","import_order":256},
{"id":13072,"iri":"Alisphenoid","display_name":"Alisphenoid","interval_start":323,"interval_end":323,"index":"322.323","import_order":257},
{"id":13101,"iri":"Angular","display_name":"Angular","interval_start":324,"interval_end":324,"index":"322.324","import_order":258},
{"id":10831,"iri":"Articular","display_name":"Articular","interval_start":325,"interval_end":325,"index":"322.325","import_order":259},
{"id":13075,"iri":"Basihyal","display_name":"Basihyal","interval_start":326,"interval_end":326,"index":"322.326","import_order":260},
{"id":13019,"iri":"Basipterigium","display_name":"Basipterigium","interval_start":327,"interval_end":327,"index":"322.327","import_order":261},
{"id":10735,"iri":"Ceratohyal","display_name":"Ceratohyal","interval_start":328,"interval_end":328,"index":"322.328","import_order":262},
{"id":10778,"iri":"Ceratobranchial","display_name":"Ceratobranchial","interval_start":329,"interval_end":329,"index":"322.329","import_order":263},
{"id":10834,"iri":"Cleithrum","display_name":"Cleithrum","interval_start":330,"interval_end":330,"index":"322.330","import_order":264},
{"id":13695,"iri":"Ectopterygoid_Fish","display_name":"Ectopterygoid Fish","interval_start":332,"interval_end":392,"index":"332","import_order":265},
{"id":13100,"iri":"Endocranium","display_name":"Endocranium","interval_start":333,"interval_end":333,"index":"332.333","import_order":266},
{"id":13022,"iri":"Epihyal","display_name":"Epihyal","interval_start":334,"interval_end":334,"index":"332.334","import_order":267},
{"id":13053,"iri":"Epiotic","display_name":"Epiotic","interval_start":335,"interval_end":335,"index":"332.335","import_order":268},
{"id":13087,"iri":"Ethmoid","display_name":"Ethmoid","interval_start":336,"interval_end":336,"index":"332.336","import_order":269},
{"id":13067,"iri":"Fin","display_name":"Fin","interval_start":337,"interval_end":344,"index":"332.337","import_order":270},
{"id":13010,"iri":"Anal_Fin","display_name":"Anal Fin","interval_start":338,"interval_end":338,"index":"332.337.338","import_order":271},
{"id":12988,"iri":"Caudal_Fin","display_name":"Caudal Fin","interval_start":339,"interval_end":339,"index":"332.337.339","import_order":272},
{"id":10844,"iri":"Fin_Ray","display_name":"Fin Ray","interval_start":340,"interval_end":340,"index":"332.337.340","import_order":273},
{"id":10856,"iri":"Fin_Spine","display_name":"Fin Spine","interval_start":341,"interval_end":341,"index":"332.337.341","import_order":274},
{"id":12987,"iri":"Pectoral_Fin","display_name":"Pectoral Fin","interval_start":342,"interval_end":342,"index":"332.337.342","import_order":275},
{"id":13088,"iri":"Ventral_Fin","display_name":"Ventral Fin","interval_start":343,"interval_end":343,"index":"332.337.343","import_order":276},
{"id":10783,"iri":"Hyomandibular","display_name":"Hyomandibular","interval_start":345,"interval_end":345,"index":"332.345","import_order":277},
{"id":13099,"iri":"Hypohyal","display_name":"Hypohyal","interval_start":346,"interval_end":346,"index":"332.346","import_order":278},
{"id":12991,"iri":"Interhyal","display_name":"Interhyal","interval_start":347,"interval_end":347,"index":"332.347","import_order":279},
{"id":10803,"iri":"Interhaemal_Spine","display_name":"Interhaemal Spine","interval_start":348,"interval_end":348,"index":"332.348","import_order":280},
{"id":10683,"iri":"Interopercular","display_name":"Interopercular","interval_start":349,"interval_end":349,"index":"332.349","import_order":281},
{"id":13054,"iri":"Lachrymal","display_name":"Lachrymal","interval_start":350,"interval_end":350,"index":"332.350","import_order":282},
{"id":10710,"iri":"Opercular","display_name":"Opercular","interval_start":351,"interval_end":351,"index":"332.351","import_order":283},
{"id":13025,"iri":"Opisthotic","display_name":"Opisthotic","interval_start":352,"interval_end":352,"index":"332.352","import_order":284},
{"id":12986,"iri":"Otolith","display_name":"Otolith","interval_start":353,"interval_end":353,"index":"332.353","import_order":285},
{"id":13077,"iri":"Metapterygoid","display_name":"Metapterygoid","interval_start":354,"interval_end":354,"index":"332.354","import_order":286},
{"id":10698,"iri":"Parasphenoid","display_name":"Parasphenoid","interval_start":355,"interval_end":355,"index":"332.355","import_order":287},
{"id":10853,"iri":"Pharyngeal_Bone","display_name":"Pharyngeal Bone","interval_start":356,"interval_end":356,"index":"332.356","import_order":288},
{"id":10729,"iri":"Postcleithrum","display_name":"Postcleithrum","interval_start":357,"interval_end":357,"index":"332.357","import_order":289},
{"id":10720,"iri":"Posttemporal","display_name":"Posttemporal","interval_start":358,"interval_end":358,"index":"332.358","import_order":290},
{"id":13005,"iri":"Precaudal","display_name":"Precaudal","interval_start":359,"interval_end":359,"index":"332.359","import_order":291},
{"id":10747,"iri":"Preopercular","display_name":"Preopercular","interval_start":360,"interval_end":360,"index":"332.360","import_order":292},
{"id":13107,"iri":"Proatlas","display_name":"Proatlas","interval_start":361,"interval_end":361,"index":"332.361","import_order":293},
{"id":13688,"iri":"Prootic_Fish","display_name":"Prootic Fish","interval_start":362,"interval_end":391,"index":"332.362","import_order":294},
{"id":13045,"iri":"Pterotic","display_name":"Pterotic","interval_start":363,"interval_end":363,"index":"332.362.363","import_order":295},
{"id":13043,"iri":"Pterygiophore","display_name":"Pterygiophore","interval_start":364,"interval_end":364,"index":"332.362.364","import_order":296},
{"id":13694,"iri":"Quadrate_Prootic","display_name":"Quadrate Prootic","interval_start":365,"interval_end":390,"index":"332.362.365","import_order":297},
{"id":13702,"iri":"Fish_Radial","display_name":"Fish Radial","interval_start":366,"interval_end":366,"index":"332.362.365.366","import_order":298},
{"id":13003,"iri":"Ray","display_name":"Ray","interval_start":367,"interval_end":370,"index":"332.362.365.367","import_order":299},
{"id":10790,"iri":"Branchiostegal_Ray","display_name":"Branchiostegal Ray","interval_start":368,"interval_end":368,"index":"332.362.365.367.368","import_order":300},
{"id":13035,"iri":"Dorsal_Ray_Spine","display_name":"Dorsal Ray/Spine","interval_start":369,"interval_end":369,"index":"332.362.365.367.369","import_order":301},
{"id":12992,"iri":"Scale","display_name":"Scale","interval_start":371,"interval_end":371,"index":"332.362.365.371","import_order":302},
{"id":13038,"iri":"Scute","display_name":"Scute","interval_start":372,"interval_end":376,"index":"332.362.365.372","import_order":303},
{"id":13018,"iri":"Dorsal_Scute","display_name":"Dorsal Scute","interval_start":373,"interval_end":373,"index":"332.362.365.372.373","import_order":304},
{"id":13037,"iri":"Lateral_Scute","display_name":"Lateral Scute","interval_start":374,"interval_end":374,"index":"332.362.365.372.374","import_order":305},
{"id":13048,"iri":"Ventral_Scute","display_name":"Ventral Scute","interval_start":375,"interval_end":375,"index":"332.362.365.372.375","import_order":306},
{"id":12993,"iri":"Sphenotic","display_name":"Sphenotic","interval_start":377,"interval_end":377,"index":"332.362.365.377","import_order":307},
{"id":10732,"iri":"Subopercular","display_name":"Subopercular","interval_start":378,"interval_end":378,"index":"332.362.365.378","import_order":308},
{"id":13105,"iri":"Suborbital","display_name":"Suborbital","interval_start":379,"interval_end":379,"index":"332.362.365.379","import_order":309},
{"id":13078,"iri":"Supracleithrum","display_name":"Supracleithrum","interval_start":380,"interval_end":380,"index":"332.362.365.380","import_order":310},
{"id":13029,"iri":"Supratemporal","display_name":"Supratemporal","interval_start":381,"interval_end":381,"index":"332.362.365.381","import_order":311},
{"id":10709,"iri":"Supraoccipital","display_name":"Supraoccipital","interval_start":382,"interval_end":382,"index":"332.362.365.382","import_order":312},
{"id":13028,"iri":"Symplectic","display_name":"Symplectic","interval_start":383,"interval_end":383,"index":"332.362.365.383","import_order":313},
{"id":12984,"iri":"Urohyal","display_name":"Urohyal","interval_start":384,"interval_end":384,"index":"332.362.365.384","import_order":314},
{"id":13109,"iri":"Vertebra-Abdominal_Anterior","display_name":"Vertebra-Abdominal Anterior","interval_start":385,"interval_end":385,"index":"332.362.365.385","import_order":315},
{"id":13123,"iri":"Vertebra-Abdominal_Posterior","display_name":"Vertebra-Abdominal Posterior","interval_start":386,"interval_end":386,"index":"332.362.365.386","import_order":316},
{"id":13110,"iri":"Vertebra-Penultimate","display_name":"Vertebra-Penultimate","interval_start":387,"interval_end":387,"index":"332.362.365.387","import_order":317},
{"id":13113,"iri":"Vertebra-Ultimate","display_name":"Vertebra-Ultimate","interval_start":388,"interval_end":388,"index":"332.362.365.388","import_order":318},
{"id":13111,"iri":"Vertebra-Weberian","display_name":"Vertebra-Weberian","interval_start":389,"interval_end":389,"index":"332.362.365.389","import_order":319},
{"id":13684,"iri":"Mollusc_Elements","display_name":"Mollusc Elements","interval_start":393,"interval_end":397,"index":"393","import_order":320},
{"id":13033,"iri":"Hinge_plate","display_name":"Hinge plate","interval_start":394,"interval_end":394,"index":"393.394","import_order":321},
{"id":13103,"iri":"Umbo","display_name":"Umbo","interval_start":395,"interval_end":395,"index":"393.395","import_order":322},
{"id":13040,"iri":"Other_shell","display_name":"Other shell","interval_start":396,"interval_end":396,"index":"393.396","import_order":323},
{"id":13059,"iri":"Unidentified_Element","display_name":"Unidentified Element","interval_start":398,"interval_end":402,"index":"398","import_order":324},
{"id":10654,"iri":"Long_Bone_fragment","display_name":"Long Bone fragment","interval_start":399,"interval_end":399,"index":"398.399","import_order":325},
{"id":13068,"iri":"Spongy_bone","display_name":"Spongy bone","interval_start":400,"interval_end":400,"index":"398.400","import_order":326},
{"id":10776,"iri":"Not_Recorded","display_name":"Not Recorded","interval_start":401,"interval_end":401,"index":"398.401","import_order":327}]},{"id":3989,
"title":"Fauna Butchering Ontology","integrationColumns":[{"id":4753,"name":"butchering","display_name":"Butchering","default_ontology_id":3989,"dataset_id":420,
"dataset_title":"Ojo Bonito Faunal Database","data_table_id":4735,"data_table_name":"e_321876_obap","data_table_display_name":"OBAP"},{"id":43057,"name":"bmark",
"display_name":"bmark","default_ontology_id":3989,"dataset_id":392595,"dataset_title":"Guadalupe Ruin Fauna","data_table_id":7850,"data_table_name":"e_318592_prpfauna",
"data_table_display_name":"PRPFAUNA"}],"nodes":[
{"id":5541,"iri":"Butchered","display_name":"Butchered","interval_start":1,"interval_end":8,"index":"1","import_order":0},
{"id":5544,"iri":"Cut_Marks","display_name":"Cut Marks","interval_start":2,"interval_end":2,"index":"1.2","import_order":1},
{"id":5539,"iri":"Chop_Marks","display_name":"Chop Marks","interval_start":3,"interval_end":3,"index":"1.3","import_order":2},
{"id":5543,"iri":"Saw_Marks","display_name":"Saw Marks","interval_start":4,"interval_end":7,"index":"1.4","import_order":3},
{"id":12975,"iri":"Hand_Saw_Marks","display_name":"Hand Saw Marks","interval_start":5,"interval_end":5,"index":"1.4.5","import_order":4},
{"id":12974,"iri":"Mechanical_Saw_Marks","display_name":"Mechanical Saw Marks","interval_start":6,"interval_end":6,"index":"1.4.6","import_order":5},
{"id":5540,"iri":"Probably_Butchered","display_name":"Probably Butchered","interval_start":9,"interval_end":9,"index":"9","import_order":6},
{"id":5542,"iri":"Unbutchered","display_name":"Unbutchered","interval_start":10,"interval_end":10,"index":"10","import_order":7},
{"id":5545,"iri":"Indeterminate","display_name":"Indeterminate","interval_start":11,"interval_end":11,"index":"11","import_order":8},
{"id":5538,"iri":"Not_Recorded","display_name":"Not Recorded","interval_start":12,"interval_end":12,"index":"12","import_order":9}]}],"displayColumns":[
{"id":4738,"name":"unit","display_name":"Unit","default_ontology_id":null,"dataset_id":420,"dataset_title":"Ojo Bonito Faunal Database","data_table_id":4735,
"data_table_name":"e_321876_obap","data_table_display_name":"OBAP"},{"id":4739,"name":"level","display_name":"Level","default_ontology_id":null,"dataset_id":420,
"dataset_title":"Ojo Bonito Faunal Database","data_table_id":4735,"data_table_name":"e_321876_obap","data_table_display_name":"OBAP"},{"id":43039,"name":"context",
"display_name":"Context","default_ontology_id":null,"dataset_id":392595,"dataset_title":"Guadalupe Ruin Fauna","data_table_id":7850,"data_table_name":"e_318592_prpfauna","data_table_display_name":"PRPFAUNA"}]}
</script>
<script src="/js/tdar.filter-ng.js"></script>
<script>
    $(function(){
        $("#ulOntologyTabs a:first").tab('show');
    })
</script>
</body>

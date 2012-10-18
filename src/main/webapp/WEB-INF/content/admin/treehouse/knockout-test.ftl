<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@edit.subNavMenu />


<@edit.toolbar "${resource.urlNamespace}" "edit" />

<@s.form name='ImageMetadataForm' id='ImageMetadataForm'  method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation "image" "image">

    <p id="t-located"  tooltipcontent="Actual physical location of a copy of the image, e.g. an agency, repository, 
        or library." tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Storage Loc.' name='image.copyLocation' cssClass="longfield"/>
    </p>


</@edit.basicInformation>
<@edit.allCreators 'Image Creators' authorshipProxies 'authorship' />

<@edit.citationInfo "image" />

<@edit.asyncFileUpload "Image" true />



<div class="well" id="spatialSection">
    <legend>Spatial Terms</legend>
    <div class="divInheritSection">
<div class="control-groupalwaysEnabled">
<label class="control-label"></label>
<div class="controls">
<label class="checkbox alwaysEnabled" for="cbInheritingSpatialInformation">Inherit this section

<input type="checkbox" name="resource.inheritingSpatialInformation" value="true" id="cbInheritingSpatialInformation" class="alwaysEnabled"><input type="hidden" id="__checkbox_cbInheritingSpatialInformation" name="__checkbox_resource.inheritingSpatialInformation" value="true"></label>    </div> 
</div>
    </div>    
    <div id="divSpatialInformation">
        <div tiplabel="Spatial Terms: Geographic" tooltipcontent="Keyword list: Geographic terms relevant to the document, e.g. &quot;Death Valley&quot; or &quot;Kauai&quot;."></div>
    <div class="control-group">
        <label class="control-label">Geographic Terms</label>
    <div data-bind="foreach: geographicKeywords">
            <div class="controls controls-row  repeat-row" id="geographicKeywordsRow_0_">
                <input data-bind="value:label, attr:{name:'geographicKeywords['+ $index() + ']'}" type="text"  class="input-xxlarge keywordAutocomplete" placeholder="enter keyword">
                <button class="btn  btn-mini repeat-row-delete" type="button" tabindex="-1" onclick="TDAR.repeatrow.deleteRow(this)" title="delete this item from the list"><i class="icon-trash"></i></button>
                    </div>
                    </div><div class="control-group"><div class="controls"><button class="btn" type="button"><i class="icon-plus-sign"></i>add another keyword</button></div></div>
            </div>
    </div>
</div>

</@s.form>

<@edit.sidebar />


<script src="//ajax.aspnetcdn.com/ajax/knockout/knockout-2.1.0.debug.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/knockout.mapping/2.3.2/knockout.mapping.js"></script>
<#noescape>
<script>
var projectModel = ko.mapping.fromJS(${projectAsJson});
projectModel.geographicKeywords.push({label:"hi mom"});
$(function(){
    ko.applyBindings(projectModel)
});
</script>
</#noescape>
</body>
</#escape>
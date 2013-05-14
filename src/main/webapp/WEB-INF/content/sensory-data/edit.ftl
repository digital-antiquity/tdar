<#escape _untrusted as _untrusted?html>
<#global itemPrefix="sensoryData"/>
<#global inheritanceEnabled=true />
<#global multipleUpload=true />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>


<#macro basicInformation>
    <div tiplabel="Object / Monument Number" tooltipcontent="The ID number or code, if applicable, of the object or monument">
    <@s.textfield maxLength="255" name="sensoryData.monumentNumber" cssClass="input-xxlarge" label="Object / Monument #" labelposition="left" />
    </div>
</#macro>

<#macro localSection>
<div id="divScanInfo" style="display:none">
    <#assign _scans=sensoryDataScans />
    <#if _scans.isEmpty()>
    <#assign _scans=blankSensoryDataScan />
    </#if>  
    <h2>Level 1: Original Scan Files</h2>
    <div id='sensoryDataScans' class='repeatLastRow' addAnother='add another scan' callback='scanAdded'>
        <#list _scans as _scan>
        <div id="sensoryDataScanRow_${_scan_index}_" class='repeat-row'>
            <@s.hidden name="sensoryDataScans[${_scan_index}].id" />
            <div class='control-group'>
                <div class='controls controls-row'>
                    <span tiplabel="Scan Filename" tooltipcontent="The name of the scan. A suggested filename for original raw scans for archiving is in this format: ProjectName_scan1.txt.">
                        <@s.textfield theme='simple' maxLength="255" name="sensoryDataScans[${_scan_index}].filename" placeholder="Filename" cssClass="span3 shortfield" />
                    </span>
                    <#assign _scanDate="" />
                    <#if _scan.scanDate?? >
                      <#assign _scanDate><@view.shortDate _scan.scanDate!"" /></#assign>
                    </#if>
                    <span tiplabel="Scan Date" tooltipcontent="Date the object/monument was scanned">
                    <@s.textfield maxLength="255" theme='simple' name="sensoryDataScans[${_scan_index}].scanDate" value="${_scanDate}" placeholder="mm/dd/yyyy" cssClass="span2 date" />
                    </span>
                    <@edit.clearDeleteButton id="sensoryDataScanRow" />
                </div>
                <div class='controls controls-row'>
                    <span tiplabel="Data Resolution" tooltipcontent="Fixed resolution or data resolution at specific range.">
                    <@s.textfield maxLength="255" theme='simple' name="sensoryDataScans[${_scan_index}].resolution" placeholder="Resolution" cssClass="span3" />
                    </span>
                    <span tiplabel="Number of Points in Scan" tooltipcontent="Number of points generated in scan">
                    <@s.textfield theme='simple' maxLength="255" name="sensoryDataScans[${_scan_index}].pointsInScan" placeholder="# points" cssClass="span2 shortfield number" />
                    </span>
                </div>
                <div class='controls controls-row'>
                    <span tiplabel="Scan Transformation Matrix" tooltipcontent="The name of the transformation matrix used in Global Registration. Suggested file name: ProjectName_scan1_mtrx.txt">
                    <@s.textfield maxLength="255" theme='simple' name="sensoryDataScans[${_scan_index}].transformationMatrix" placeholder="Transformation Matrix" cssClass="span3" />
                    </span>
                    <span tooltipcontent="Check this box if this transformation matrix has been applied to the archived scan">
                    <label class='checkbox span2'>
                    <@s.checkbox theme='simple' name="sensoryDataScans[${_scan_index}].matrixApplied" id="cbMatrixApplied_${_scan_index}_" />
                    Matrix Applied to Scan
                    </label>
                    </span>
                </div>
            </div>
            <div class='control-group'>
                <div class='scantech-fields'>
                    <@s.select label='Scanner Technology' headerValue="" headerKey="" name='sensoryDataScans[${_scan_index}].scannerTechnology' listValue="label" list='%{scannerTechnologyTypes}' labelposition="left" cssClass="scannerTechnology shortfield" />
                    <label for='returnType_${_scan_index}' class='control-label scantech-field scantech-fields-tof'>
                    Return Type
                    </label>
                    <div class='controls controls-row'>
                    <@s.select id='returnType_${_scan_index}' name="sensoryDataScans[${_scan_index}].tofReturn" emptyOption='true' list=['First Return','Last Return'] theme='simple' cssClass="scantech-fields-tof scantech-field" />
                    <@s.textfield theme='simple' maxLength="255" name="sensoryDataScans[${_scan_index}].cameraExposureSettings" placeholder="Camera Exposure Settings" 
                    cssClass="shortfield scantech-field scantech-fields-phase scantech-fields-tof" />
                    <@s.textfield theme='simple' maxLength="255" name="sensoryDataScans[${_scan_index}].phaseFrequencySettings" placeholder="Frequency Settings" 
                    cssClass="shortfield scantech-field scantech-fields-phase" />
                    <@s.textfield theme='simple' maxLength="255" name="sensoryDataScans[${_scan_index}].phaseNoiseSettings" placeholder="Noise Settings" 
                    cssClass="shortfield scantech-field scantech-fields-phase" />
                    <@s.textfield theme='simple' maxLength="255"  name="sensoryDataScans[${_scan_index}].triangulationDetails" placeholder="Lens/FOV Details" 
                    cssClass="shortfield scantech-field scantech-fields-tri" />
                    </div>
                </div>
                <div tiplabel="Additional Scan Notes" tooltipcontent="Additional notes related to this scan">
                <@s.textarea name="sensoryDataScans[${_scan_index}].scanNotes" label="Scan Notes" labelposition="top" cssClass="resizable input-xxlarge" rows="5" />
                </div>
            </div>
        </div>
        </#list>
    </div>
</div>
<div id="divImageInfo" tooltipcontent='#imageInfoTooltip' style="display:none">
    <#assign _images=sensoryDataImages />
    <#if _images.isEmpty()>
    <#assign _images=blankSensoryDataImage />
    </#if>  
    <h2>Image Information</h2>
    <div id="sensoryDataImagesDiv" class="repeatLastRow" addAnother="add another image">
        <#list _images as _image>
        <div id="sensoryDataImagesRow_${_image_index}_" class='repeat-row'>
            <@s.hidden name="sensoryDataImages[${_image_index}].id" />
            <div class='control-group'>
                <div class='controls controls-row'>
                <@s.textfield theme='simple' placeholder='Filename' maxLength="255" name="sensoryDataImages[${_image_index}].filename"  />
                <@s.textfield theme='simple' placeholder='Description' maxLength="255" name="sensoryDataImages[${_image_index}].description" /></td>
                <@edit.clearDeleteButton id="sensoryDataImagesRow" />
                </div>
            </div>
        </div>
        </#list>
    </div>
</div>
<div id="imageInfoTooltip" class="hide">
    <h2>Image Information</h2>
    <div>
        Use this section to specify information about reference images included in with this resource.
        <dl>
            <dt>Name</dt>
            <dd>The filename of the reference image</dd>
            <dt>Description</dt>
            <dd>Description of the image</dd>
        </dl>
    </div>    
</div>



<div id="registeredDatasetDiv" style="display:none">
    <h2>Level 2: Registered Dataset</h2>
    <div tiplabel="Name of Registered Dataset" tooltipcontent="Filename for the dataset, a suggested naming structure for registered dataset for archiving: ProjectName_GR.txt">
        <@s.textfield maxLength="255" name="sensoryData.registeredDatasetName" label="Dataset Name" cssClass="input-xxlarge" labelposition="left" />
    </div>
    <br />
    <div tiplabel="Registration Method" tooltipcontent="Provide a brief description of the methods used to register the point cloud (e.g. 'Individual scans were aligned using N Point pairs').">
        <@s.textfield maxLength="255" name="sensoryData.registrationMethod" label="Reg. Method" cssClass="input-xxlarge" labelposition="left" />
    </div>
    <br />
    <div tiplabel="Registration Error" tooltipcontent="Total RMS error from global registration in scan units.">
        <@s.textfield maxLength="255" name="sensoryData.registrationErrorUnits" cssClass="shortfield number" label="Reg. Error" labelposition="left" />
    </div>
    <div tiplabel="Total Number of points In File" tooltipcontent="Total number of points in finalregistered point cloud">
        <@s.textfield maxLength="255" name="sensoryData.finalRegistrationPoints" cssClass="right-shortfield number" label="# Points in File" labelposition="left" />
    </div>
</div>

<div id="polygonalMeshDatasetDiv" style="display:none">
    <h2>Level 3: Polygonal Mesh Dataset</h2>

    <h4>Pre-mesh</h4>
    <div tiplabel="Name of Mesh Dataset" tooltipcontent="The filename, a suggested naming convention for the polygonal mesh dataset is *ProjectName_origmesh">
        <@s.textfield maxLength="255" name="sensoryData.preMeshDatasetName" cssClass="input-xxlarge" label="Dataset Name" labelposition="left" />
    </div>
    <br />
    <div tiplabel="Number of Points in File" tooltipcontent="Total number of points in the edited premesh point cloud">
        <@s.textfield maxLength="255" name="sensoryData.preMeshPoints" cssClass="shortfield number" label="# Points in File" labelposition="left" />
    </div>
    <br />
    <div class='control-group' tiplabel="Processing Operations" tooltipcontent="Check all the processing operations that apply">
        <label class="control-label">Processing Operations</label>
        <div class='controls'>
        <label class='checkbox'>
        <@s.checkbox name="sensoryData.premeshOverlapReduction" theme='simple' label="Overlap Reduction"  />
        Overlap Reduction
        </label>
        <label class='checkbox'>
        <@s.checkbox name="sensoryData.premeshSmoothing" theme='simple' label="Smoothing" />
        Smoothing
        </label>
        <label class='checkbox'>
        <@s.checkbox name="sensoryData.premeshSubsampling" theme='simple'  label="Subsampling"  />
        Subsampling
        </label>
        <label class='checkbox'>
        <@s.checkbox name="sensoryData.premeshColorEditions" theme='simple'  label="Color Editions"  />
        Color Editions
        </label>
        </div>
    </div>
    <div tiplabel="Point Editing Summary" tooltipcontent="Include a description of major editing operations (IE overlap reduction, point deletion, etc...) that have been performed on the dataset">
        <@s.textarea  name="sensoryData.pointDeletionSummary" cssClass="resizable input-xxlarge" label="Point Editing Summary" labelposition="top" rows="5" />
    </div>
    <br />
    
    
    <h4>Polygonal Mesh Metadata</h4>
    <div tiplabel="Name of Mesh Dataset" tooltipcontent="The filename, a suggested naming convention for the polygonal mesh dataset is *ProjectName_origmesh">
        <@s.textfield maxLength="255" name="sensoryData.meshDatasetName" cssClass="input-xxlarge" label="Dataset Name" labelposition="left" />
    </div>
    <br />
    <div tiplabel="Total Triangle Count (post editing, predecimation)" tooltipcontent="Total number of triangles in the mesh file">
        <@s.textfield maxLength="255" name="sensoryData.meshTriangleCount" cssClass="shortfield number" label="# Triangles" labelposition="left" />
    </div>
    <br />
    <div tiplabel="Coordinate System Adjustment" tooltipcontent="If present, the transformation matrix filename">
        <@s.textfield maxLength="255" name="sensoryData.meshAdjustmentMatrix" cssClass="input-xxlarge" label="Adj. Matrix" labelposition="left" />
    </div>
    <br />
    <div class="control-group" tiplabel="Processing Operations" tooltipcontent="Check all the processing operations that apply">
        <label class="control-label">Processing Operations</label>
        <div class="controls">
            <label class='checkbox'>
            <@s.checkbox name="sensoryData.meshRgbIncluded" theme='simple' label="RGB Color Included" />
            RGB Color Included
            </label>
            <label class='checkbox'>
            <@s.checkbox name="sensoryData.meshdataReduction" theme='simple' label="Data Reduction" />
            Data Reduction
            </label>
            <label class='checkbox'>
            <@s.checkbox name="sensoryData.meshSmoothing" theme='simple' label="Smoothing" />
            Smoothing
            </label>
            <label class='checkbox'>
            <@s.checkbox name="sensoryData.meshHolesFilled" theme='simple' label="Holes Filled"  />
            Holes Filled
            </label>
            <label class='checkbox'>
            <@s.checkbox name="sensoryData.meshColorEditions" theme='simple' label="Color Editions"  /> 
            Color Editions
            </label>
            <label class='checkbox'>
            <@s.checkbox name="sensoryData.meshHealingDespiking" theme='simple' label="Healing/De-spiking"  /> 
            Healing/De-spiking
            </label>
        </div>
    </div>
    <br />
    <@s.textarea name="sensoryData.meshProcessingNotes" cssClass="resizable input-xxlarge" label="Additional Processing Notes" labelposition="top" rows="5" />
    <br />
    
    <h4>Decimated Polygonal Mesh Metadata / Triangle Counts</h4>
    <div tiplabel="Name of Decimated Mesh Dataset" tooltipcontent="The file name, a suggested naming convention for the decimated polygonal mesh dataset is ProjectName_decimesh_50pcnt for decimated mesh e.g. by 50%.">
        <@s.textfield maxLength="255" name="sensoryData.decimatedMeshDataset" cssClass="input-xxlarge" label="Mesh Name" labelposition="left" />
    </div>
    <br />
    <div tiplabel="Total Original Triangle Count" tooltipcontent="Total Original Triangle Count">
        <@s.textfield maxLength="255" name="sensoryData.decimatedMeshOriginalTriangleCount" cssClass="shortfield number" label="# Original" labelposition="left" />
    </div>
    <div tiplabel="Decimated Triangle Count" tooltipcontent="Decimated Triangle Count">
        <@s.textfield maxLength="255" name="sensoryData.decimatedMeshTriangleCount" cssClass="right-shortfield number" label="# Decimated" labelposition="left" />
    </div>
    <br />
    <div tiplabel="Processing Operations" tooltipcontent="Check all the processing operations that apply">
    <h4>Processing Operations</h4>
    <@s.checkbox name="sensoryData.rgbPreservedFromOriginal" cssClass="indent" label="RGB Color Included" labelposition="right" />
    </div>
</div>

<div id="divSurveyInfo">
    <h2>Survey Information</h2>
    <div class='control-group' tiplabel="Survey Date(s)" tooltipcontent="Date of survey, or date range of survey.">
    <@s.textfield label="Survey Begin" id="txtSurveyDateBegin" name="sensoryData.surveyDateBegin" cssClass="shortfield date formatUS" placeholder="mm/dd/yyyy" />
    <@s.textfield label="Survey End" id="txtSurveyDateEnd" name="sensoryData.surveyDateEnd" cssClass="right-shortfield date formatUS" placeholder="mm/dd/yyyy" />
<#-- FIXME: need to convert surveyDateEnd and surveyDateBegin to short forms when editing existing sensory data
value="<#if sensoryData.surveyDateEnd??><@view.shortDate sensoryData.surveyDateEnd /></#if>"
-->
    <!-- FIXME: why is this commented out?
    <div tiplabel="" tooltipcontent=""><@s.textfield maxLength="255" name="sensoryData.surveyLocation" cssClass="input-xxlarge" label="Survey Location" labelposition="left" title="Survey Location" /></div>
    -->
    <@s.textfield maxLength="255" name="sensoryData.surveyConditions" 
        tiplabel="Survey Conditions" tooltipcontent="The overall weather trend during survey (sunny, overcast, indoors, etc.)"
        cssClass="input-xxlarge" label="Conditions" labelposition="left" />
    <div tiplabel="Scanner Details" tooltipcontent="Details of the instrument(s) with serial number(s) and scan units">
    <@s.textfield maxLength="255" name="sensoryData.scannerDetails" cssClass="input-xxlarge" label="Scanner Details" labelposition="left" />
    </div>
    <div tiplabel="Company / Operator Name" tooltipcontent="Details of company and scan operator name">
    <@s.textfield maxLength="255" name="sensoryData.companyName" cssClass="input-xxlarge" label="Company Name" labelposition="left" />
    </div>
    <div tiplabel="Estimated Data Resolution" tooltipcontent="The estimated average data resolution across the monument or object">
    <@s.textfield maxLength="255" name="sensoryData.estimatedDataResolution" label="Average Data Resolution" labelposition="left" />
    </div>
    <div tiplabel="Total Number of Scans in Project" tooltipcontent="Total number of scans">
    <@s.textfield maxLength="255" name="sensoryData.totalScansInProject" cssClass="right-shortfield number" label="# Scans" labelposition="left" />
    </div>
    <div tiplabel="Turntable used" tooltipcontent="Check this box if a turntable was used for this survey.">
        <@s.checkbox  label="Turntable Used" name="sensoryData.turntableUsed"  id="cbTurntableUsed"  />
    </div>
    <div tiplabel="Planimetric Map Filename" tooltipcontent="If applicable, then provide the image name.">
    <@s.textfield maxLength="255" name="sensoryData.planimetricMapFilename" cssClass="reallyinput-xxlarge" label="Planimetric Map Filename" labelposition="top" />
    </div>
    <div tiplabel="Control Data Filename" tooltipcontent="If control data was collected, enter the control data filename.">
    <@s.textfield maxLength="255" name="sensoryData.controlDataFilename" cssClass="reallyinput-xxlarge" label="Control Data Filename" labelposition="top" />
    </div>
    <div tiplabel='RGB Data Capture Information' tooltipcontent="Please specify it is (1) internal or external and (2) describe any additional lighting systems used if applicable">
    <@s.textarea name="sensoryData.rgbDataCaptureInfo" id="rgbDataCaptureInfo" cssClass="resizable input-xxlarge" label="RGB Data Capture Information" labelposition="top" rows="5" />
    </div>
    <div tiplabel="Description of Final Datasets for Archive" tooltipcontent="What datasets will be archived (include file names if possible).">
        <@s.textarea name="sensoryData.finalDatasetDescription" cssClass="resizable input-xxlarge" label="Description of Final Datasets for Archive" labelposition="top" rows="5" />
    </div>
    </div>
</div>

<@s.radio name='sensoryData.scannerTechnology' emptyOption='false' listValue="label"  
            list='%{scannerTechnologyTypes}' label="Scan Type" theme="bootstrap" />

<div class="well">
	<h3>Metadata Template</h3>
	<p>Due to the variability and complexity of sensory data scans, we're providing a template you can use to include the details of how your scan was captured and composed.  Please download.</p>
	
	<p><a class="btn btn-success">DOWNLOAD TEMPLATE</a></p>
	<br/>
	<@s.file label="Completed Metadata Template" cssClass="validateFileType" labelposition='top' name='uploadedFiles' size='40'/>
</div>

</#macro>

<#macro localJavascript>
    //return true if any form field in this div are populated  
    TDAR.namespace("sensorydata");
     
    function hasContent(div) {
        var found = false;
        var $div = $(div);
        $div.find("input[type=text],textarea").each(function(idx, elem){
            if($.trim(elem.value).length >0) {
                found = true;
                return false;
            } 
        });
        return found;
    }
    TDAR.sensorydata.hasContent = hasContent;
    
    //show legacy edit fields if they have content
    $('#registeredDatasetDiv, #polygonalMeshDatasetDiv, #divScanInfo,#divImageInfo').each(function(idx, div){
        if(hasContent(div)) {
            $(div).show();
        }
    });
    

    $('#sensoryDataScans').bind('repeatrowadded', function(e, parent, newRow) {
        scanAdded(newRow);
    });
    
    $('.scannerTechnology').each(
        function(i,elem){
            var scannerTechElem = elem;
            showScannerTechFields(scannerTechElem);
            $(scannerTechElem).change(function(){showScannerTechFields(scannerTechElem);});
        }
    );

    $('.scannerTechnology').rules("add", {
        valueRequiresAsyncUpload: {
            possibleValues: ["TIME_OF_FLIGHT", "PHASE_BASED", "TRIANGULATION"],
            fileExt: "xls",
            inputElementId: "fileAsyncUpload"},
        messages: {
            valueRequiresAsyncUpload: "Please include a scan manifest file when choosing this scan type"}
    });
</#macro>
 
</body>
</#escape>

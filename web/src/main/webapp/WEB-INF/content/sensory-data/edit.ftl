<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="sensoryData"/>
    <#global inheritanceEnabled=true />
    <#global multipleUpload=true />
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#assign customUploadPlacement=true>

    <#macro basicInformation>
    <div data-tiplabel="Object / Monument Number" data-tooltipcontent="The ID number or code, if applicable, of the object or monument">
        <@s.textfield maxLength="255" name="sensoryData.monumentNumber" cssClass="input-xxlarge" label="Object / Monument #" labelposition="left" />
    </div>
    </#macro>

    <#macro divImageInfo>
        <#local _images=sensoryDataImages, divclass="" />
        <#if _images.isEmpty()>
            <#local _images=blankSensoryDataImage, divclass="hidden" />
        </#if>
    <div id="divImageInfo" data-tooltipcontent='#imageInfoTooltip' class="${divclass}">
        <h2>Image Information</h2>

        <div id="sensoryDataImagesDiv" class="repeatLastRow" addAnother="add another image">
            <#list _images as _image>
                <div id="sensoryDataImagesRow_${_image_index}_" class='repeat-row'>
                    <@s.hidden name="sensoryDataImages[${_image_index}].id" />
                    <div class='control-group'>
                        <div class='controls controls-row'>
                            <@s.textfield theme='simple' placeholder='Filename' maxLength="255" name="sensoryDataImages[${_image_index}].filename"  />
                <@s.textfield theme='simple' placeholder='Description' maxLength="255" name="sensoryDataImages[${_image_index}].description" />
                <@nav.clearDeleteButton id="sensoryDataImagesRow" />
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
    </#macro>

    <#macro divScanInfo>
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
                    <span data-tiplabel="Scan Filename"
                          data-tooltipcontent="The name of the scan. A suggested filename for original raw scans for archiving is in this format: ProjectName_scan1.txt.">
                        <@s.textfield theme='simple' maxLength="255" name="sensoryDataScans[${_scan_index}].filename" placeholder="Filename" cssClass="span3 shortfield" />
                    </span>
                            <#assign _scanDate="" />
                            <#if _scan.scanDate?? >
                                <#assign _scanDate><@view.shortDate _scan.scanDate!"" /></#assign>
                            </#if>
                            <span data-tiplabel="Scan Date" data-tooltipcontent="Date the object/monument was scanned">
                                <@s.textfield maxLength="255" theme='simple' name="sensoryDataScans[${_scan_index}].scanDate" value="${_scanDate}" placeholder="mm/dd/yyyy" cssClass="span2 date" />
                            </span>
                            <@nav.clearDeleteButton id="sensoryDataScanRow" />
                        </div>
                        <div class='controls controls-row'>
                    <span data-tiplabel="Data Resolution" data-tooltipcontent="Fixed resolution or data resolution at specific range.">
                        <@s.textfield maxLength="255" theme='simple' name="sensoryDataScans[${_scan_index}].resolution" placeholder="Resolution" cssClass="span3" />
                    </span>
                    <span data-tiplabel="Number of Points in Scan" data-tooltipcontent="Number of points generated in scan">
                        <@s.textfield theme='simple' maxLength="255" name="sensoryDataScans[${_scan_index}].pointsInScan" placeholder="# points" cssClass="span2 shortfield number" />
                    </span>
                        </div>
                        <div class='controls controls-row'>
                    <span data-tiplabel="Scan Transformation Matrix"
                          data-tooltipcontent="The name of the transformation matrix used in Global Registration. Suggested file name: ProjectName_scan1_mtrx.txt">
                        <@s.textfield maxLength="255" theme='simple' name="sensoryDataScans[${_scan_index}].transformationMatrix" placeholder="Transformation Matrix" cssClass="span3" />
                    </span>
                    <span data-tooltipcontent="Check this box if this transformation matrix has been applied to the archived scan">
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
                        <div data-tiplabel="Additional Scan Notes" data-tooltipcontent="Additional notes related to this scan">
                            <@s.textarea name="sensoryDataScans[${_scan_index}].scanNotes" label="Scan Notes" labelposition="top" cssClass="resizable input-xxlarge" rows="5"  cols="80" />
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </div>
    </#macro>

    <#macro registeredDatasetDiv>
    <div id="registeredDatasetDiv" style="display:none">
        <h2>Level 2: Registered Dataset</h2>

        <div data-tiplabel="Name of Registered Dataset"
             data-tooltipcontent="Filename for the dataset, a suggested naming structure for registered dataset for archiving: ProjectName_GR.txt">
            <@s.textfield maxLength="255" name="sensoryData.registeredDatasetName" label="Dataset Name" cssClass="input-xxlarge" labelposition="left" />
        </div>
        <br/>

        <div data-tiplabel="Registration Method"
             data-tooltipcontent="Provide a brief description of the methods used to register the point cloud (e.g. 'Individual scans were aligned using N Point pairs').">
            <@s.textfield maxLength="255" name="sensoryData.registrationMethod" label="Reg. Method" cssClass="input-xxlarge" labelposition="left" />
        </div>
        <br/>

        <div data-tiplabel="Registration Error" data-tooltipcontent="Total RMS error from global registration in scan units.">
            <@s.textfield maxLength="255" name="sensoryData.registrationErrorUnits" cssClass="shortfield number" label="Reg. Error" labelposition="left" />
        </div>
        <div data-tiplabel="Total Number of points In File" data-tooltipcontent="Total number of points in finalregistered point cloud">
            <@s.textfield maxLength="255" name="sensoryData.finalRegistrationPoints" cssClass="right-shortfield number" label="# Points in File" labelposition="left" />
        </div>
    </div>
    </#macro>

    <#macro polygonalMeshDatasetDiv>
    <div id="polygonalMeshDatasetDiv" style="display:none">
        <h2>Level 3: Polygonal Mesh Dataset</h2>

        <h4>Pre-mesh</h4>

        <div data-tiplabel="Name of Mesh Dataset"
             data-tooltipcontent="The filename, a suggested naming convention for the polygonal mesh dataset is *ProjectName_origmesh">
            <@s.textfield maxLength="255" name="sensoryData.preMeshDatasetName" cssClass="input-xxlarge" label="Dataset Name" labelposition="left" />
        </div>
        <br/>

        <div data-tiplabel="Number of Points in File" data-tooltipcontent="Total number of points in the edited premesh point cloud">
            <@s.textfield maxLength="255" name="sensoryData.preMeshPoints" cssClass="shortfield number" label="# Points in File" labelposition="left" />
        </div>
        <br/>

        <div class='control-group' data-tiplabel="Processing Operations" data-tooltipcontent="Check all the processing operations that apply">
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
        <div data-tiplabel="Point Editing Summary"
             data-tooltipcontent="Include a description of major editing operations (IE overlap reduction, point deletion, etc...) that have been performed on the dataset">
            <@s.textarea  name="sensoryData.pointDeletionSummary" cssClass="resizable input-xxlarge" label="Point Editing Summary" labelposition="top" rows="5"  cols="80" />
        </div>
        <br/>


        <h4>Polygonal Mesh Metadata</h4>

        <div data-tiplabel="Name of Mesh Dataset"
             data-tooltipcontent="The filename, a suggested naming convention for the polygonal mesh dataset is *ProjectName_origmesh">
            <@s.textfield maxLength="255" name="sensoryData.meshDatasetName" cssClass="input-xxlarge" label="Dataset Name" labelposition="left" />
        </div>
        <br/>

        <div data-tiplabel="Total Triangle Count (post editing, predecimation)" data-tooltipcontent="Total number of triangles in the mesh file">
            <@s.textfield maxLength="255" name="sensoryData.meshTriangleCount" cssClass="shortfield number" label="# Triangles" labelposition="left" />
        </div>
        <br/>

        <div data-tiplabel="Coordinate System Adjustment" data-tooltipcontent="If present, the transformation matrix filename">
            <@s.textfield maxLength="255" name="sensoryData.meshAdjustmentMatrix" cssClass="input-xxlarge" label="Adj. Matrix" labelposition="left" />
        </div>
        <br/>

        <div class="control-group" data-tiplabel="Processing Operations" data-tooltipcontent="Check all the processing operations that apply">
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
        <br/>
        <@s.textarea name="sensoryData.meshProcessingNotes" cssClass="resizable input-xxlarge" label="Additional Processing Notes" labelposition="top" rows="5"  cols="80" />
        <br/>

        <h4>Decimated Polygonal Mesh Metadata / Triangle Counts</h4>

        <div data-tiplabel="Name of Decimated Mesh Dataset"
             data-tooltipcontent="The file name, a suggested naming convention for the decimated polygonal mesh dataset is ProjectName_decimesh_50pcnt for decimated mesh e.g. by 50%.">
            <@s.textfield maxLength="255" name="sensoryData.decimatedMeshDataset" cssClass="input-xxlarge" label="Mesh Name" labelposition="left" />
        </div>
        <br/>

        <div data-tiplabel="Total Original Triangle Count" data-tooltipcontent="Total Original Triangle Count">
            <@s.textfield maxLength="255" name="sensoryData.decimatedMeshOriginalTriangleCount" cssClass="shortfield number" label="# Original" labelposition="left" />
        </div>
        <div data-tiplabel="Decimated Triangle Count" data-tooltipcontent="Decimated Triangle Count">
            <@s.textfield maxLength="255" name="sensoryData.decimatedMeshTriangleCount" cssClass="right-shortfield number" label="# Decimated" labelposition="left" />
        </div>
        <br/>

        <div data-tiplabel="Processing Operations" data-tooltipcontent="Check all the processing operations that apply">
            <h4>Processing Operations</h4>
            <@s.checkbox name="sensoryData.rgbPreservedFromOriginal" cssClass="indent" label="RGB Color Included" labelposition="right" />
        </div>
    </div>
    </#macro>

    <#macro divSurveyInfo>
    <div id="divSurveyInfo">
        <h2>Survey Information</h2>

        <div id="divScannerTechnologyOptions">
            <@s.radio name='sensoryData.scannerTechnology' id="selScannerTechnology" listValue="label"
            list='%{scannerTechnologyTypes}' label="Scan Technology" />
        </div>

        <div class="conditional-scantype phase_based time_of_flight triangulation">
            <div data-tiplabel="Survey Date(s)" data-tooltipcontent="Date of survey, or date range of survey.">
                <@edit.datefield label="Survey Begin" id="txtSurveyDateBegin" name="sensoryData.surveyDateBegin" date=sensoryData.surveyDateBegin!'' cssClass="shortfield date datepicker" placeholder="mm/dd/yyyy" format="MM/dd/yyyy" />
            <@edit.datefield label="Survey End" id="txtSurveyDateEnd" name="sensoryData.surveyDateEnd" date=sensoryData.surveyDateEnd!'' cssClass="right-shortfield date datepicker" placeholder="mm/dd/yyyy" />
            </div>
        <span data-tiplabel="Survey Conditions" data-tooltipcontent="The overall weather trend during survey (sunny, overcast, indoors, etc.)">
            <@s.textfield maxLength="255" name="sensoryData.surveyConditions"
            cssClass="input-xxlarge" label="Conditions" labelposition="left" />
        </span>

            <div data-tiplabel="Company / Operator Name" data-tooltipcontent="Details of company and scan operator name">
                <@s.textfield maxLength="255" name="sensoryData.companyName" cssClass="input-xxlarge" label="Company Name" labelposition="left" />
            </div>
        </div>

        <div class="conditional-scantype phase_based time_of_flight triangulation" data-tiplabel="Scanner Details"
             data-tooltipcontent="Details of the instrument(s) with serial number(s) and scan units">
            <@s.textfield maxLength="255" name="sensoryData.scannerDetails" cssClass="input-xxlarge" label="Scanner Details" labelposition="left" />
        </div>
        <div class="conditional-scantype phase_based time_of_flight triangulation" data-tiplabel="Estimated Data Resolution"
             data-tooltipcontent="The estimated average data resolution across the monument or object">
            <@s.textfield maxLength="255" name="sensoryData.estimatedDataResolution" label="Average Data Resolution" labelposition="left" />
        </div>
        <div class="conditional-scantype phase_based time_of_flight triangulation" data-tiplabel="Total Number of Scans in Project"
             data-tooltipcontent="Total number of scans">
            <@s.textfield maxLength="255" name="sensoryData.totalScansInProject" cssClass="right-shortfield number" label="# Scans" labelposition="left" />
        </div>
        <div class="conditional-scantype triangulation" data-tiplabel="Turntable used"
             data-tooltipcontent="Check this box if a turntable was used for this survey.">
            <@s.checkbox  label="Turntable Used" name="sensoryData.turntableUsed"  id="cbTurntableUsed"  />
        </div>
        <div class="conditional-scantype phase_based time_of_flight" data-tiplabel="Planimetric Map Filename"
             data-tooltipcontent="If applicable, then provide the image name.">
            <@s.textfield maxLength="255" name="sensoryData.planimetricMapFilename" cssClass="input-xxlarge" label="Planimetric Map Filename" labelposition="top" />
        </div>
        <div class="conditional-scantype phase_based time_of_flight" data-tiplabel="Control Data Filename"
             data-tooltipcontent="If control data was collected, enter the control data filename.">
            <@s.textfield maxLength="255" name="sensoryData.controlDataFilename" cssClass="input-xxlarge" label="Control Data Filename" labelposition="top" />
        </div>
        <div class="conditional-scantype phase_based time_of_flight triangulation" data-tiplabel='RGB Data Capture Information'
             data-tooltipcontent="Please specify it is (1) internal or external and (2) describe any additional lighting systems used if applicable">
            <@s.radio name='sensoryData.rgbCapture'  listValue="label"
            list='%{rgbCaptureOptions}' label="RGB Capture" />
        <@s.textarea name="sensoryData.rgbDataCaptureInfo" id="rgbDataCaptureInfo" cssClass="resizable input-xxlarge" label="Lighting Setup Information" labelposition="top" rows="5"  cols="80" />
        </div>
        <div data-tiplabel="Description of Final Datasets for Archive" data-tooltipcontent="What datasets will be archived (include file names if possible).">
            <@s.textarea name="sensoryData.finalDatasetDescription" cssClass="resizable input-xxlarge" label="Description of Final Datasets for Archive" labelposition="top" rows="5"  cols="80" />
        </div>

        <div class="conditional-scantype phase_based time_of_flight triangulation"
             data-tiplabel="Camera Details"
             data-tooltipcontent="If applicable, provide the make/model/lense for the external camera that is is used to capture images for color mapping onto the scanned object">
            <@s.textarea name="sensoryData.cameraDetails" id="cameraDetails" cssClass="phase_based time_of_flight resizable input-xxlarge" label="Camera Details" labelposition="top" rows="5"  cols="80"  />
        </div>

    </div>
    <div id="scantypeFileReminder" style="display:none">
        <div class="well">
            <h4>Scan Metadata Templates</h4>

            <p>
                Metadata collected for 3D scan data will vary based on differences in scan technology and
                instrumentation. Individualized templates are provided below, please select and download the
                template that is appropriate to the scan project. Complete the metadata for the original scan
                files in the project and any additional products that will be archived (only registered point clouds
                and polygonal mesh datasets can be accepted at this time). Include the completed template in the zip
                file upload (See instructions below for suggested upload file structure).
            </p>
            <h5>Available Templates</h5>

            <div class="well">
                <ul id="ulTemplateList">
                    <li class="phase_based time_of_flight" id="liTofPhase">
                  <span class="inlineblock">
                      <a target="_blank" href="<@s.url value="/includes/sensory-data/scan_metadata_tof_phase.xlsx"/>"><i class="icon-file"></i> scan_metadata_tof.xlsx</a>
                          best for time-of-flight and phase-based scans
                  </span>
                    </li>

                    <li class="triangulation" id="liTriangulation">
                  <span class="inlineblock">
                      <a target="_blank" href="<@s.url value="/includes/sensory-data/scan_metadata_triangulation.xlsx"/>"><i class="icon-file"></i>
                          scan_metadata_triangulation.xlsx</a>
                          best for triangulation scans
                  </span>
                    </li>
                    <li class="combined" id="liCombined">
                  <span class="inlineblock"><a target="_blank" href="<@s.url value="/includes/sensory-data/scan_metadata_combined.xlsx"/>"><i class="icon-file"></i>
                      scan_metadata_combined.xlsx</a>
                          best for scans that involve multiple scan technologies
                  </span>
                    </li>
                </ul>
            </div>
        </div>
    </div>

    <div class="well">
        <h5>Upload Structure</h5>

        <p>
            All 3D data and the associated metadata template should be zipped into a single zip file prior to upload.
            Additional images should be uploaded individually and image metadata should be
            completed for each image. The suggested folder structure for the zip
            file is as follows:
        </p>

        <div class="well">
            <ul>
                <li class="folder">
                    <em>SurveyName</em>
                    <ul>
                        <li class="folder">
                            <em>SurveyName_L1</em> &mdash;
                            contains original scan files in .e57 or .txt format
                            <ul>
                                <li class="folder">
                                    <em>Trans_Matrices</em> &mdash;
                                    contains transformation matrices for each original scan file, file
                                    naming should match original scan file names
                                </li>
                            </ul>
                        </li>
                        <li class="folder">
                            <em>SurveyName_L2</em> &mdash;
                            contains registered point cloud or registered polygonal mesh in appropriate
                            format
                        </li>
                        <li class="folder">
                            <em>SurveyName_L3</em> &mdash;
                            contains merged polygonal mesh in appropriate format, if applicable
                        </li>
                        <li class="file">
                            <em>SurveyName_metadata.xls</em> &mdash;
                            completed metadata template for all scan data and final products
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    </#macro>

    <#macro beforeUpload>
        <@divSurveyInfo />
    </#macro>

    <#macro localSection cols=9>
        <#local spanall  = "span${cols}">

        <@divImageInfo />
        <@divScanInfo />
        <@polygonalMeshDatasetDiv />
        <@registeredDatasetDiv />


    </#macro>

    <#macro localJavascript>
        TDAR.sensoryData.initEdit();
    </#macro>

</body>
</#escape>

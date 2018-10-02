<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>


    <#macro fieldval _label _val _show=true>
        <#if _show>
        <tr>
            <td nowrap><p><strong>${_label}:</strong></p></td>
            <td><p>${_val}</p></td>
        </tr>
        </#if>
    </#macro>

    <#macro fieldvalbool _label _val _show=true>
        <#if _show>
        <tr>
            <td nowrap><p><strong>${_label}:</strong></p></td>
            <td>
                <p>    <#if _val>Yes<#else>No</#if></p>
            </td>
        </tr>
        </#if>
    </#macro>

    <#macro afterBasicInfo>
        <#assign _surveyDateBegin = "" />
        <#assign _surveyDateEnd = "" />
        <#if resource.surveyDateBegin??><#assign _surveyDateBegin><@view.shortDate resource.surveyDateBegin /></#assign></#if>
        <#if resource.surveyDateEnd??><#assign _surveyDateEnd><@view.shortDate resource.surveyDateEnd /></#assign></#if>
    <table>
        <@fieldval _label="Object #" _val=resource.monumentNumber!0 _show=resource.monumentNumber??  />
    <@fieldval _val=resource.surveyLocation!""          _show=resource.surveyLocation??  _label="Survey Location" />
    <@fieldval _val=_surveyDateBegin              _show=resource.surveyDateBegin??  _label="Survey Date (start)" />
    <@fieldval _val=_surveyDateEnd              _show=resource.surveyDateEnd??  _label="Survey Date (end)" />
    <@fieldval _val=resource.surveyConditions!""        _show=resource.surveyConditions??  _label="Conditions" />
    <@fieldval _val=resource.scannerDetails!""          _show=resource.scannerDetails??  _label="Scanner Details" />
    <@fieldval _val=resource.companyName!""             _show=resource.companyName??  _label="Company Name" />
    <@fieldvalbool _val=resource.turntableUsed           _show=resource.turntableUsed??  _label="Turntable Used" />
    <@fieldval _val=resource.rgbDataCaptureInfo!""      _show=resource.rgbDataCaptureInfo??  _label="RGB Data Capture Information" />
    <@fieldval _val=resource.estimatedDataResolution!0 _show=resource.estimatedDataResolution??  _label="Data Resolution" />
    <@fieldval _val=resource.totalScansInProject!0     _show=resource.totalScansInProject??  _label="Number of Scans"  />
    <@fieldval _val=resource.controlDataFilename!""      _show=resource.controlDataFilename??  _label="Control Data Filename" />
    <@fieldval _val=resource.planimetricMapFilename!""      _show=resource.planimetricMapFilename??  _label="Planimetric Map Filename" />
    <@fieldval _val=resource.finalDatasetDescription!"" _show=resource.finalDatasetDescription??  _label="Description of Final Datasets for Archive" />
    </table>
    <hr/>

        <#if resource.sensoryDataScans?has_content>
        <h3>Scan Information</h3>
        <table class="table table-striped tableFormat">
              <thead class="thead-dark">

            <tr>
                <th>Filename</th>
                <th title="Transformation Matrix">Transformation Matrix</th>
                <th>Matrix Applied</th>
                <th>Scan Date</th>
                <th>Scanner Technology</th>
                <th>Resolution</th>
                <th>Additional Notes</th>
            </tr>
            </thead>
            <tbody>
                <#list resource.sensoryDataScans as _scan>
                    <#assign _scanDate="" />
                    <#if _scan.scanDate?? >
                        <#assign _scanDate><@view.shortDate _scan.scanDate /></#assign>
                    </#if>

                <tr>
                    <td>${_scan.filename!""}</td>
                    <td>${_scan.transformationMatrix!""}</td>
                    <td><#if (_scan.matrixApplied!false)>Yes</#if></td>
                    <td>${_scanDate}</td>
                    <td>
                        <#if (_scan.scannerTechnology.label)?has_content>
                            <em>Type:</em> ${_scan.scannerTechnology.label}
                            <#if (_scan.tofReturn)?has_content><br><em>Return Type:</em>${_scan.tofReturn}</#if>
                            <#if (_scan.cameraExposureSettings)?has_content><br><em>Exposure
                                Settings: </em>${_scan.cameraExposureSettings}</#if>
                            <#if (_scan.phaseFrequencySettings)?has_content><br><em>Frequency
                                Settings: </em>${_scan.phaseFrequencySettings}</#if>
                            <#if (_scan.phaseNoiseSettings)?has_content><br><em>Noise
                                Settings: </em>${_scan.phaseNoiseSettings}</#if>
                            <#if (_scan.triangulationDetails?has_content)><br><em>Lens/FOV
                                Details: </em>${_scan.triangulationDetails}</#if>
                        </#if>
                    </td>
                    <td><#if _scan.resolution??>${_scan.resolution}</#if></td>
                    <td>${_scan.scanNotes!""}</td>
                </tr>
                </#list>
            </tbody>
        </table>
        </#if>

        <#if resource.sensoryDataImages?has_content>
        <h3>Sensory Data Image Information</h3>
        <table class="table table-striped tableFormat">
              <thead class="thead-dark">

            <tr>
                <th>Filename</th>
                <th>Description</th>
            </tr>
            </thead>
            <tbody>
                <#list resource.sensoryDataImages as _image>
                <tr>
                    <td>${_image.filename!""}</td>
                    <td>${_image.description!""}</td>
                </tr>
                </#list>
            </tbody>
        </table>
        </#if>
        <#if resource.sensoryDataScans?has_content >

        <h3>Registration Information</h3>
        <table>
            <@fieldval _val=resource.registeredDatasetName!""    _show=resource.registeredDatasetName??       _label="Dataset Name"   />
<@fieldval _val=resource.registrationMethod!""    _show=resource.registrationMethod??       _label="Registration Method"   />
<@fieldval _val=resource.registrationErrorUnits!0   _show=resource.registrationErrorUnits??      _label="Registration Error"   />
<@fieldval _val=resource.finalRegistrationPoints!0  _show=resource.finalRegistrationPoints??     _label="# Points in File"   />
        </table>
        <hr/>

        <h3>Mesh Information</h3>

        <h4>Pre-mesh</h4>
        <table>
            <@fieldval          _val=resource.preMeshDatasetName!""              _show=resource.preMeshDatasetName??                   _label="Dataset Name"  />
<@fieldval          _val=resource.preMeshPoints!0                   _show=resource.preMeshPoints??                        _label="# Points in File"  />
<@fieldval          _val=resource.pointDeletionSummary!""   _show=resource.pointDeletionSummary??          _label="Point Editing Summary"   />
<@fieldvalbool      _val=resource.premeshOverlapReduction         _show=resource.premeshOverlapReduction??              _label="Overlap Reduction"  />
<@fieldvalbool      _val=resource.premeshSmoothing                _show=resource.premeshSmoothing??                     _label="Smoothing"  />
<@fieldvalbool      _val=resource.premeshSubsampling              _show=resource.premeshSubsampling??                   _label="Subsampling"  />
<@fieldvalbool      _val=resource.premeshColorEditions            _show=resource.premeshColorEditions??                 _label="Color Editions"  />
        </table>

        <h4>Polygonal Mesh Metadata</h4>
        <table>
            <@fieldval          _val=resource.meshDatasetName!""                _show=resource.meshDatasetName??                        _label="Dataset Name"  />
<@fieldval          _val=resource.meshTriangleCount!0               _show=resource.meshTriangleCount??                      _label="Number Triangles"  />
<@fieldval          _val=resource.meshProcessingNotes!""             _show=resource.meshProcessingNotes??                    _label="Additional Processing Notes"   />
<@fieldvalbool      _val=resource.meshHolesFilled                 _show=resource.meshHolesFilled??                        _label="Holes Filled"  />
<@fieldvalbool      _val=resource.meshSmoothing                   _show=resource.meshSmoothing??                          _label="Smoothing"  />
<@fieldvalbool      _val=resource.meshColorEditions               _show=resource.meshColorEditions??                      _label="Color Editions"  />
<@fieldvalbool      _val=resource.meshHealingDespiking            _show=resource.meshHealingDespiking??                   _label="Healing/Despiking"  />
<@fieldvalbool      _val=resource.meshRgbIncluded                 _show=resource.meshRgbIncluded??                        _label="RGB Color Included"  />
<@fieldvalbool      _val=resource.meshdataReduction               _show=resource.meshdataReduction??                      _label="Data Reduction"  />
<@fieldval          _val=resource.meshAdjustmentMatrix!""            _show=resource.meshAdjustmentMatrix??                   _label="Adjustment Matrix"  />
        </table>

        <h4>Decimated Polygonal Mesh Metadata / Triangle Counts</h4>
        <table>
            <@fieldval          _val=resource.decimatedMeshDataset!""                _show=resource.decimatedMeshDataset??                _label="Mesh Name"  />
<@fieldval          _val=resource.decimatedMeshOriginalTriangleCount!0  _show=resource.decimatedMeshOriginalTriangleCount??  _label="Original Triangle Count"  />
<@fieldval          _val=resource.decimatedMeshTriangleCount!0          _show=resource.decimatedMeshTriangleCount??          _label="Decimated Triangle Count"  />
<@fieldvalbool      _val=resource.rgbPreservedFromOriginal            _show=resource.rgbPreservedFromOriginal??            _label="RGB Color" />
        </table>
        <hr/>
        </#if>
    </#macro>
</#escape>
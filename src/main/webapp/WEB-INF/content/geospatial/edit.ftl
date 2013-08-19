<#escape _untrusted as _untrusted?html>
<#global itemPrefix="geospatial"/>
<#global inheritanceEnabled=true />
<#global multipleUpload=true />
<#global hideRelatedCollections=true/>

<#macro localJavascript>
console.log("adding gis validation rules");
TDAR.fileupload.addGisValidation(TDAR.fileupload.validator);
</#macro>

	<#macro basicInformation>
		<br/>
    
    <span data-tiplabel="Currentness &amp; Update Notes" data-tooltipcontent="Please describe how current this dataset is and what the frequency for updates is">
            <@s.textarea rows="4" label="Currentness & Update Information" name='geospatial.currentnessUpdateNotes' cssClass='resizable resize-vertical input-xxlarge' required=true title="A description is required" />
    </span>

    <span data-tiplabel="Spatial Reference System Id" data-tooltipcontent="Please enter a SRS ID eg: EPSG:3857  or WGS84" class="">
            <@s.textfield label="Spatial Reference System Id" title="A SRS is required"  maxlength=50 required=true
			name='geospatial.spatialReferenceSystem' cssClass="tdartext longfield input-xxlarge" />
    </span>

    <span data-tiplabel="Projection" data-tooltipcontent="Please enter a map projection, in most cases, this will be based on the Spatial Reference System ID, an example might be Mercator Projection" class="">
            <@s.textfield label="Projection" title="A projection is required"  maxlength=50
			name='geospatial.projection' cssClass="tdartext longfield input-xxlarge" />
    </span>


    <span data-tiplabel="Scale" data-tooltipcontent="Please provide the scale for the map, eg: 1:1000" class="">
            <@s.textfield label="Scale" maxlength=50
			name='geospatial.scale' cssClass="tdartext longfield  input-xxlarge" />
    </span>



		<br/>
	</#macro>

<#macro beforeUpload>
<#if payPerIngestEnabled>
<small>Shapefiles, and geo-rectified images often require more than one actual file to work properly. Pricing for these files works as follows: each image, or Shapefile is treated as a single file within tDAR. Thus, if a user uploaded "untitled.shp", "untitled.dbf", "untitled.prj", and "untitled.shp.xml" the combination of these four files would be treated as a single file for pricing. </small>
</#if>
</#macro>

<#macro footer>

</#macro>
</#escape>
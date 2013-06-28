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

    <span data-tiplabel="Spatial Reference Sytstem" data-tooltipcontent="FILL ME IN" class="">
            <@s.textfield label="Spatial Reference System" title="A SRS is required"  maxlength=50
			name='geospatial.spatialReferenceSystem' cssClass="tdartext longfield input-xxlarge" />
    </span>

    <span data-tiplabel="Projection" data-tooltipcontent="FILL ME IN" class="">
            <@s.textfield label="Projection" title="A projection is required"  maxlength=50
			name='geospatial.projection' cssClass="tdartext required longfield input-xxlarge" />
    </span>


    <span data-tiplabel="Scale" data-tooltipcontent="FILL ME IN" class="">
            <@s.textfield label="Scale" maxlength=50
			name='geospatial.scale' cssClass="tdartext longfield input-xxlarge" />
    </span>



		<br/>
	</#macro>



<#macro footer>

</#macro>
</#escape>
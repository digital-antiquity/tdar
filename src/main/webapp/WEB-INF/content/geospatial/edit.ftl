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
    
    <span data-tiplabel="Currentness &amp; Update Notes" data-tooltipcontent="Please describe how current this dataset is and what the frequency for updates is"
    class="">
            <@s.textarea label="Currentness &amp; Update Information" name='${itemPrefix}.currentnessUpdateNotes' cssClass='resizable resize-vertical input-xxlarge' required=true title="A description is required" />
    </div>

    <span data-tiplabel="Spatial Reference Sytstem" data-tooltipcontent="FILL ME IN" class="">
            <@s.textfield label="Spatial Reference System" title="A SRS is required"  maxlength=50
			name='geospatial.spatialReferenceSystem' cssClass="tdartext longfield input-xxlarge" />
    </div>

    <span data-tiplabel="Projection" data-tooltipcontent="FILL ME IN" class="">
            <@s.textfield label="Projection" title="A projection is required"  maxlength=50
			name='geospatial.projection' cssClass="tdartext required longfield input-xxlarge" />
    </div>


    <span data-tiplabel="Scale" data-tooltipcontent="FILL ME IN" class="">
            <@s.textfield label="Scale" maxlength=50
			name='geospatial.scale' cssClass="tdartext longfield input-xxlarge" />
    </div>

    @Column(name = "scale", length = 100)
    private String scale;



		<br/>
	</#macro>



<#macro footer>

</#macro>
</#escape>
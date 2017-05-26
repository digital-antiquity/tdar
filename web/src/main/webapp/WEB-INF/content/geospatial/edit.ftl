<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="geospatial"/>
    <#global inheritanceEnabled=true />
    <#global multipleUpload=true />
    <#global hideRelatedCollections=true/>
    <#import "/${config.themeDir}/local-helptext.ftl" as  helptext>


    <#macro basicInformation>
    <br/>

    <span data-tiplabel="Currentness &amp; Update Notes"
          data-tooltipcontent="Please describe how current this dataset is and what the frequency for updates is">
        <@s.textarea rows="4" label="Currentness & Update Information" name='geospatial.currentnessUpdateNotes' cssClass='resizable resize-vertical input-xxlarge required' required=true title="A description is required"  cols="80" />
    </span>

        <@helptext.srid />

    <span data-tiplabel="Spatial Reference System &amp; Projection" data-tooltipcontent="#sridTip" class="">
        <@s.textfield label="Spatial Reference System & Projection" title="A SRS/Projection is required"  maxlength=50 required=true
        name='geospatial.spatialReferenceSystem' cssClass="tdartext longfield required input-xxlarge" />
    </span>

    <span data-tiplabel="Map Source" data-tooltipcontent="Please describe the source of the map, if possible, include a citation">
        <@s.textfield label="Map Source"  maxlength=500
        name='geospatial.mapSource' cssClass="tdartext longfield input-xxlarge" />
    </span>


    <span data-tiplabel="Scale" data-tooltipcontent="Please provide the scale for the map, e.g. 1:1000" class="">
        <@s.textfield label="Scale" maxlength=50
        name='geospatial.scale' cssClass="tdartext longfield  input-xxlarge" />
    </span>


    <br/>
    </#macro>

    <#macro beforeUpload>
        <#if config.payPerIngestEnabled>
        <small>Shapefiles, and geo-rectified images often require more than one actual file to work properly. Pricing for these files works as follows: each
            image, or Shapefile is treated as a single file within tDAR. Thus, if a user uploaded "untitled.shp", "untitled.dbf", "untitled.prj", and
            "untitled.shp.xml" the combination of these four files would be treated as a single file for pricing.
        </small>
        </#if>
    </#macro>

    <#macro footer>

    </#macro>
</#escape>
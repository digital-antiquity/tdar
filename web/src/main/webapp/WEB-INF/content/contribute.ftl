<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/content/cart/common-invoice.ftl" as invoiceCommon >
<#import "/WEB-INF/settings.ftl" as settings>

<head>
    <title>Upload &amp; contribute content to ${siteAcronym}</title>
    <meta name="lastModifiedDate" content="$Date: 2009-02-13 09:05:44 -0700 (Fri, 13 Feb 2009)$"/>

<style>
ul.unstyled li {
font-size: 14px;
line-height: 20px;
color: #7d7a77;
} 
</style>    
</head>
<body>
<#escape _untrusted as _untrusted?html >

<div class="row mb-2">
    <div class="col-6">
        <a href="/cart/add" class="btn btn-primary btn-lg">Start Your Upload</a>
    </div>
</div>


<h1>What You Can Upload &amp; <span class="red">Contribute</span> to ${siteAcronym}</h1>


<div class="row mb-2">
    <div class="col-6">
            <div class="media mb-0">
                <div class="media-body">
        <h3 class="document-mid-red red">Documents</h3>
<div class="row">
<div class="col">
        <ul class="list-unstyled">
            <li>PDF Documents (.pdf)</li>
            <li>Microsoft Word (.doc, .docx)</li>
        </ul>
</div>
    <div class="col">
        <ul class="list-unstyled">
            <li>Rich Text Documents (.rtf)</li>
            <li>Plain Text Documents (.txt)</li>
        </ul>
</div>
</div>
</div>
                <svg style="width:18%;top:-20px;position:relative" class="my-0 svgicon svg-dynamic red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_document"></use></svg> 
</div>
        <strong>Examples</strong> Reports of archaeological field investigations, articles presentations, field or lab notes, catalogs, dissertations or theses, collections and historical research, and historical documents about archaeological resources, research projects, and organizations
</div>

    <div class="col-6">
            <div class="media mb-0">
                <div class="media-body">
        <h3 class="image-mid-red red">Images</h3>

<div class="row  mb-2">
<div class="col">
        <ul class="list-unstyled">
            <li>Tagged Image File Format (.tiff, .tif)</li>
            <li>JPEG Image (.jpg, .jpeg)</li>
        </ul>
</div>
<div class="col">
        <ul class="list-unstyled">
            <li>Portable Network Graphics (.png)</li>
            <li>Other (.bmp, .gif, .pict)</li>
               </ul>

</div>
</div>
</div>
                <svg style="width:18%;top:-20px;position:relative" class="my-0 svgicon svg-dynamic red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_image"></use></svg> 
</div>
<strong>Examples</strong> Images and illustrations of archaeological resources or related to archaeological investigations</div>


</div>

<div class="row">
    <div class="col-6">
            <div class="media mb-0">
                <div class="media-body">
                    <h3 class="dataset-mid-red red">Data Sets</h3>
                    <div class="row">
                        <div class="col">
                            <ul class="list-unstyled">
                                <li>Comma Separated Values (.csv)</li>
                                <li>Tab Separated Values (.tab)</li>
                            </ul>
                        </div>
                        <div class="col">
                            <ul class="list-unstyled">
                                <li>Microsoft Excel (.xls, .xlsx)</li>
                                <li>Microsoft Access (.accdb, .mdb)</li>
                           </ul>
                        </div>
                    </div>
                </div>
                <svg style="width:18%;top:-20px;position:relative" class="my-0 svgicon svg-dynamic red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_dataset"></use></svg> 
            </div>
                <strong>Examples</strong> Spreadsheets, databases, and coding sheets that describe archaeological data sets about artifacts, features, sites, or other archaeological phenomenon</strong>
        </div>
    <div class="col-6">
            <div class="media mb-0">
            <div class="media-body">
            <h3 class="geospatial-mid-red red">Geospatial Data</h3>
                <div class="row">
                <div class="col">
                        <ul class="list-unstyled">
                            <li>Shapefiles</li>
                            <li>Geodatabases</li>
                        </ul>
                </div>
                <div class="col">
                        <ul class="list-unstyled">
                            <li>Georectified images</li>
                            <li> (GeoTIFF &amp; GeoJPG)</li>
                               </ul>
                
                </div>
        </div>
        </div>
            <svg style="width:18%;top:-20px;position:relative" class="my-0 svgicon svg-dynamic red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_geospatial"></use></svg> 
        </div>
        <strong>Examples</strong> Spatial Data about archaeological resources, e.g.: Maps</div>

</div>
<div class="row mt-2">
    <div class="col-6">
        <div class="media mb-0">
            <div class="media-body">
            <h3 class="sensory_data-mid-red red">Virtual</h3>
            <ul class="list-unstyled">
                <li>Remote Sensing Files</li>
                <li>3D Scans (OBJ &amp; E57)</li>
            </ul>
            </div>
            <svg style="width:18%;top:-20px;position:relative" class="my-0 svgicon svg-dynamic red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_sensory_data"></use></svg> 
        </div>
        <strong>Examples</strong> Data about archaeological resources collected by various sensors, e.g., GPS, GIS, Resisitivity, GPR, and various sonar instruments</div>

</div>




<script>
    $(document).ready(function () {
        TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/api/cart/quote"/>");
});

</script>
</#escape>
</body>

<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/content/cart/common-invoice.ftl" as invoiceCommon >
<#import "/${themeDir}/settings.ftl" as settings>

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

<h1>Upload &amp; <span class="red">Contribute</span> to ${siteAcronym}</h1>


<div class="row">
    <div class="span6">
        <h3 class="document-mid-red red">Documents</h3>
<div class="row">
<div class="span3">
        <ul class="unstyled">
            <li>PDF Documents (.pdf)</li>
            <li>Microsoft Word (.doc, .docx)</li>
        </ul>
</div>
    <div class="span3">
        <ul class="unstyled">
            <li>Rich Text Documents (.rtf)</li>
            <li>Plain Text Documents (.txt)</li>
        </ul>

</div>
</div>
        <strong>Examples</strong> Reports of archaeological field investigations, articles presentations, field or lab notes, catalogs, dissertations or theses, collections and historical research, and historical documents about archaeological resources, research projects, and organizations
</div>

    <div class="span6">
        <h3 class="image-mid-red red">Images</h3>

<div class="row">
<div class="span3">
        <ul class="unstyled">
            <li>Tagged Image File Format (.tiff, .tif)</li>
            <li>JPEG Image (.jpg, .jpeg)</li>
        </ul>
</div>
<div class="span3">
        <ul class="unstyled">
            <li>Portable Network Graphics (.png)</li>
            <li>Other (.bmp, .gif, .pict)</li>
               </ul>

</div>
</div>
<strong>Examples</strong> Images and illustrations of archaeological resources or related to archaeological investigations</div>


</div>

<div class="row">
    <div class="span6">
        <h3 class="dataset-mid-red red">Data Sets</h3>

<div class="row">
<div class="span3">
        <ul class="unstyled">
            <li>Comma Separated Values (.csv)</li>
            <li>Tab Separated Values (.tab)</li>
        </ul>
</div>
<div class="span3">
        <ul class="unstyled">
            <li>Microsoft Excel (.xls, .xlsx)</li>
            <li>Microsoft Access (.accdb, .mdb)</li>
               </ul>

</div>
</div>
                <strong>Examples</strong> Spreadsheets, databases, and coding sheets that describe archaeological data sets about artifacts, features, sites, or other archaeological phenomenon</div>

    <div class="span6">
        <h3 class="geospatial-mid-red red">Geospatial Data</h3>

<div class="row">
<div class="span3">
        <ul class="unstyled">
            <li>Shapefiles</li>
            <li>Georectified images (GeoTIFF &amp; GeoJPG)</li>
        </ul>
</div>
<div class="span3">
        <ul class="unstyled">
            <li>Geodatabases</li>
               </ul>

</div>
</div>
        <strong>Examples</strong> Spatial Data about archaeological resources, e.g.: Maps</div>

</div>
<div class="row">
    <div class="span6">
        <h3 class="sensory_data-mid-red red">Virtual</h3>
        <ul class="unstyled">
            <li>Remote Sensing Files</li>
            <li>3D Scans</li>
        </ul>
        <strong>Examples</strong> Data about archaeological resources collected by various sensors, e.g., GPS, GIS, Resisitivity, GPR, and various sonar instruments</div>

</div>




<hr />



         <div class="row">
            <div class="span12">


                <h2>Preservation Packages
                <span><a class="button pull-right" href="<@s.url value="/cart/add"/>">Start Now</a>&nbsp;<a class="button pull-right" style="margin-left: 5px;margin-right:5px" 
                href="<@s.url value="/cart/add"/>">Redeem Voucher</a></span></h2>
                <div class="row">
                    <div class="span2"></div>
    <@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action='/cart/process-choice'>
                               <@s.hidden name="invoice.numberOfFiles" label="Number of Files" cssClass="integer span2" maxlength=9  />
                               <@s.hidden name="invoice.numberOfMb" label="Number of Mb"  theme="simple" cssClass="integer span2" maxlength=9 />
                    

                    <@invoiceCommon.pricingOption label="Small" files="1 File" storage="10 MB" cost=10 />
                    <@invoiceCommon.pricingOption label="Medium" files="10 Files" storage="100 MB" cost=100 />
                    <@invoiceCommon.pricingOption label="Large" files="100 Files" storage="1 GB" cost=500 />
    </@s.form>
                </div>
            </div>
        </div>

<hr>

<h2>Confidentiality &amp; Access Rights</h2>

<div class="row">
    <div class="span6">Should you have sensitive materials, you have a number of options for protecting it in tDAR:
        <ul>
            <li>Redaction of Lat/Long or coordinate information</li>
            <li>Limiting access to designated users</li>
            <li>Marking files as confidential</li>
            <li>Embargoing access to materials for 4 years</li>
        </ul>
        Once a file ismarked as confidential or is embargoed, only users you specify can view or download files.</div>
    <div class="span6"><a href="http://www.tdar.org/wp-uploads/www.tdar.org//2012/10/bg-rights.png"><img alt="" class="alignnone size-full wp-image-161" height="279" src="http://www.tdar.org/wp-uploads/www.tdar.org//2012/10/bg-rights.png" title="bg-rights" width="477"></a></div>
</div>

<hr>

<h2>Things to Consider</h2>
<div class="row">
    <div class="span6">As you collect your data and digital documents, keep these important things in mind:
        <ul>
            <li>File naming conventions: Make it easy to distinguish different stages, drafts of documents, spreadsheets, databases, etc. Provide the most complete and recent set of data for long-term preservation and access.</li>
            <li>Back up files: Don’t lose important data and have to recreate them.</li>
            <li>Protection: Separate potentially&nbsp;confidential information.</li>
            <li>Once marked as confidential or embargoed, only users you specify can view or download files.</li>
            <li>Consistency is key.</li>
        </ul>
    </div>
    
    <div class="span6">What to put into tDAR:
        <div class="entry page clear">
            <ul>
                <li>various kinds of documents, e.g., reports of archaeological field investigations, articles and presentations, field or lab notes, catalogs, dissertations or theses, collections and historical research,&nbsp;and historical documents and correspondence about archaeological resources, research projects, and organizations;</li>
                <li>spreadsheets, databases, and coding sheets that describe archaeological data sets about artifacts, features, sites, or other archaeological phenomenon;</li>
                <li>photographs, maps, and illustrations of archaeological resources or related to archaeological investigations; and,</li>
                <li>data about archaeological resources collected by various sensors, e.g., GPS, GIS, Resisitivity, GPR, and various sonar instruments.</li>
            </ul>
        </div>
    </div>
    
</div>
<script>
    $(document).ready(function () {
        TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/cart/api"/>");
});

</script>
</#escape>
</body>

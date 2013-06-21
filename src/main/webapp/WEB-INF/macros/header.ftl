<#import "resource/common.ftl" as common>

<#macro scripts combine=false>
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/Jit-2.0.1/Extras/excanvas.js"/>"></script><![endif]--> 

    <#--if not using mergeservlet, use a fake directory name that corresponds to build number so that client will pull up-to-date version -->
    <#local fakeDirectory = combine?string("", "/vc/${common.tdarBuildId}") />

    <#local srcs = [
                     "/includes/jquery.cookie.js",
                     "/includes/jquery.metadata.2.1/jquery.metadata.js",
                     "/includes/jquery.maphighlight.local.js",
                     "/includes/jquery.textarearesizer.js",
                     "/includes${fakeDirectory}/jquery.FormNavigate.js",
                     "/includes/jquery.watermark-3.1.3.min.js",
                     "/includes/jquery.datatables-1.8.2/media/js/jquery.dataTables.js",
                     "/includes/jquery.datatables-1.8.2/extras/bootstrap-paging.js",
                     "/includes/jquery-treeview/jquery.treeview.js"
                     "/includes/blueimp-javascript-templates/tmpl.min.js",
                     "/includes/jquery.aw-showcase.1.1.3/jquery.aw-showcase.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/vendor/jquery.ui.widget.js", 
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.iframe-transport.js", 
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-process.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-ui.js",
                     "/includes/jquery.populate.js",
                     "/includes/jquery.tabby-0.12.js",
                     "/includes/latLongUtil-1.0.js",
                     "/includes/jquery.orgChart/jquery.orgchart.js",
  					 "/includes/jqplot-1.08/jquery.jqplot.js",
					 "/includes/jqplot-1.08/plugins/jqplot.logAxisRenderer.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.highlighter.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.dateAxisRenderer.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.barRenderer.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.categoryAxisRenderer.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasTextRenderer.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasAxisTickRenderer.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.enhancedLegendRenderer.min.js",
					 "/includes/jqplot-1.08/plugins/jqplot.pieRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.pointLabels.js",
                     "/includes${fakeDirectory}/tdar.gmaps.js",
                     "/includes${fakeDirectory}/tdar.common.js",
                     "/includes${fakeDirectory}/tdar.upload.js",
                     "/includes${fakeDirectory}/tdar.repeatrow.js",
                     "/includes${fakeDirectory}/tdar.autocomplete.js",
                     "/includes${fakeDirectory}/tdar.datatable.js",
                     "/includes${fakeDirectory}/tdar.dataintegration.js", 
                     "/includes${fakeDirectory}/tdar.advanced-search.js",
                     "/includes${fakeDirectory}/tdar.authority-management.js",
                     "/includes${fakeDirectory}/tdar.inheritance.js",
                     "/includes${fakeDirectory}/tdar.pricing.js",
                     "/includes${fakeDirectory}/tdar.heightevents.js",
                     "/includes${fakeDirectory}/tdar.contexthelp.js",
	                 "/includes${fakeDirectory}/tdar.formValidateExtensions.js",
	                 "/includes${fakeDirectory}/tdar.jquery-upload-validation.js",
                     "/includes/bindWithDelay.js"
    ] />
<#--                     "/includes/ivaynberg-select2-817453b/select2.js" -->

    <#if !production> <#local srcs = srcs + ["/includes${fakeDirectory}/tdar.test.js"]> </#if> 

<#if combine>
   <!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <script type="text/javascript" src="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".js","")}</#list>.js"></script>
<#else>
<#list srcs as src>
  <script type="text/javascript" src="${src}"></script>
</#list>

</#if>


</#macro>


<#macro css combine=true>
    <#--if not using mergeservlet, use a fake directory name that corresponds to build number so that client will pull up-to-date version -->
    <#local fakeDirectory = combine?string("", "/vc/${common.tdarBuildId}") />
    <#local srcs = [
                    "/css${fakeDirectory}/tdar-bootstrap.css",
                    "/css/famfamfam.css",
                    "/includes/ivaynberg-select2-817453b/select2.css",
                    "/includes/jquery.aw-showcase.1.1.3/css/style.css",
                    "/includes/blueimp-jquery-file-upload-5.31.6/css/jquery.fileupload-ui.css",
                    "/includes/jquery-treeview/jquery.treeview.css",
					 "/includes/jqplot-1.08/jquery.jqplot.min.css",
                     "/includes/jquery.orgChart/jquery.orgchart.css",
                    "/includes/datatables.css"
                    
                    ] />
<#if combine>
<!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <link rel="stylesheet" type="text/css" href="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".css","")}</#list>.css">

<#else>
<#list srcs as src>
  <link rel="stylesheet" type="text/css" href="${src}" data-version="${common.tdarBuildId}">
</#list>

</#if>


</#macro>
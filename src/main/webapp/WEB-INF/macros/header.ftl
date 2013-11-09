<#import "resource/common.ftl" as common>

<#macro scripts combine=false>
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/jqplot-1.08/excanvas.js"/>"></script><![endif]--> 

    <#--if not using mergeservlet, use a fake directory name that corresponds to build number so that client will pull up-to-date version -->
    <#local fakeDirectory = combine?string("", "/vc/${common.tdarBuildId}") />

    <#local srcs = [
                     "/includes/jquery.cookie.js",
    				<#-- LICENSE: MIT /GPL 2.0 -->
                     "/includes/jquery.metadata.2.1/jquery.metadata.js",
    				<#-- LICENSE: MIT /GPL 2.0 -->
                     "/includes/jquery.maphilight-1.2.2.js",
    				<#-- LICENSE: MIT->
                     "/includes/jquery.textarearesizer.js",
    				<#-- LICENSE: MIT->
                     "/js${fakeDirectory}/jquery.FormNavigate.js",
                     "/includes/jquery.watermark-3.1.3.min.js",
    				<#-- LICENSE: MIT->
                     "/includes/jquery.datatables-1.9.4/media/js/jquery.dataTables.js",
    				<#-- LICENSE: BSD /GPL 2.0 -->
                     "/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.js",
    				<#-- LICENSE: BSD /GPL 2.0 -->
                     "/includes/jquery-treeview/jquery.treeview.js",
    				<#-- LICENSE: MIT /GPL 2.0 -->
                     "/includes/blueimp-javascript-templates/tmpl.min.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/vendor/jquery.ui.widget.js", 
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.iframe-transport.js", 
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-process.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-validate.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-ui.js",
    				<#-- LICENSE: MIT->
                     "/includes/jquery.populate.js",
    				<#-- LICENSE: MIT->
                     "/includes/jquery.tabby-0.12.js",
    				<#-- LICENSE: MIT->
                     "/js/latLongUtil-1.0.js",
                     <#-- LICENSE:CC Attribution 3.0 Unported -->
                     "/includes/jquery.orgChart/jquery.orgchart.js",
    				<#-- LICENSE: MIT /GPL 2.0 -->
  					 "/includes/jqplot-1.08/jquery.jqplot.js",
  					 <#-- LICENSE: MIT -->
                     "/includes/jqplot-1.08/plugins/jqplot.cursor.js",
					 "/includes/jqplot-1.08/plugins/jqplot.logAxisRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.highlighter.js",
					 "/includes/jqplot-1.08/plugins/jqplot.dateAxisRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.barRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.categoryAxisRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasTextRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasAxisTickRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasAxisLabelRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.enhancedLegendRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.pieRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.pointLabels.js",
                     "/js${fakeDirectory}/tdar.gmaps.js",
                     "/js${fakeDirectory}/tdar.common.js",
                     "/js${fakeDirectory}/tdar.upload.js",
                     "/js${fakeDirectory}/tdar.repeatrow.js",
                     "/js${fakeDirectory}/tdar.autocomplete.js",
                     "/js${fakeDirectory}/tdar.datatable.js",
                     "/js${fakeDirectory}/tdar.dataintegration.js",
                     "/js${fakeDirectory}/tdar.advanced-search.js",
                     "/js${fakeDirectory}/tdar.authority-management.js",
                     "/js${fakeDirectory}/tdar.inheritance.js",
                     "/js${fakeDirectory}/tdar.pricing.js",
                     "/js${fakeDirectory}/tdar.heightevents.js",
                     "/js${fakeDirectory}/tdar.contexthelp.js",
	                 "/js${fakeDirectory}/tdar.formValidateExtensions.js",
	                 "/js${fakeDirectory}/tdar.jquery-upload-validation.js",
                     "/includes/bindWithDelay.js"
  					 <#-- LICENSE: MIT -->
    ] />
<#--                     "/includes/ivaynberg-select2-817453b/select2.js" -->

    <#if !production> <#local srcs = srcs + ["/js${fakeDirectory}/tdar.test.js"]> </#if> 

<#if combine>
   <!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <script type="text/javascript" src="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".js","")}</#list>.js?build=${common.tdarBuildId}"></script>
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
                    "/includes/blueimp-jquery-file-upload-5.31.6/css/jquery.fileupload-ui.css",
                    "/includes/jquery-treeview/jquery.treeview.css",
					 "/includes/jqplot-1.08/jquery.jqplot.min.css",
                     "/includes/jquery.orgChart/jquery.orgchart.css",
                    "/includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css",
                    "/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.css"
                    
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
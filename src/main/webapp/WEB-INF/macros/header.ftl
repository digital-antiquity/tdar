<#global jquery_version="1.8.2">
<#global jquery_ui_version="1.8.23">

<#macro scripts combine=false>
<!-- fyi -- two excanvas calls aready (flot + jit ) should we use just one? also, use Modernizr -->
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/Jit-2.0.1/Extras/excanvas.js"/>"></script><![endif]--> 

    <#--if not using mergeservlet, use a fake directory name that corresponds to build number so that client will pull up-to-date version -->
    <#local fakeDirectory = combine?string("", "/vc/${common.tdarBuildId}") />

    <#local srcs = [
                     "/includes/jquery.cookie.js",
                     "/includes/jquery.xcolor-1.5.js",
                     "/includes/jquery.metadata.2.1/jquery.metadata.js",
                     "/includes/jquery.maphighlight.local.js",
                     "/includes/jquery.textarearesizer.js",
                     "/includes/jquery.FormNavigate.js",
                     "/includes/jquery.watermark-3.1.3.min.js",
                     "/includes/jquery.datatables-1.8.2/media/js/jquery.dataTables.js",
                     "/includes/jquery.datatables-1.8.2/extras/bootstrap-paging.js",
                     "/includes/jquery-treeview/jquery.treeview.js"
                     "/includes/blueimp-javascript-templates/tmpl.min.js",
                     "/includes/Jit-2.0.1/jit.js",
                     "/includes/jquery.aw-showcase.1.1.1/jquery.aw-showcase/jquery.aw-showcase.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.iframe-transport.js", 
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.fileupload.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.fileupload-fp.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.fileupload-ui.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/locale.js",
                     "/includes/jquery.populate.js",
                     "/includes/latLongUtil-1.0.js",
                     "/includes${fakeDirectory}/tdar.gmaps.js",
                     "/includes${fakeDirectory}/tdar.common.js",
                     "/includes${fakeDirectory}/tdar.upload.js",
                     "/includes${fakeDirectory}/tdar.repeatrow.js",
                     "/includes${fakeDirectory}/tdar.autocomplete.js",
                     "/includes${fakeDirectory}/tdar.datatable.js",
                     "/includes${fakeDirectory}/tdar.dataintegration.js", 
                     "/includes${fakeDirectory}/tdar.advanced-search.js",
                     "/includes${fakeDirectory}/tdar.inheritance.js",
                     "/includes/bindWithDelay.js",
                     "/includes/flot-0.7/jquery.flot.min.js",
                     "/includes/flot-0.7/excanvas.min.js",
                     "/includes/flot-0.7/jquery.flot.pie.min.js",
                     "/includes/ivaynberg-select2-817453b/select2.js"
    ] />

    <#if !production> <#local srcs = srcs + ["/includes${fakeDirectory}/tdar.test.js"]> </#if> 
    

<#if combine>
   <!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <script type="text/javascript" src="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".js,")}</#list>.js"></script>
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
                    "/includes/jquery.aw-showcase.1.1.1/jquery.aw-showcase/css/style.css",
                    "/includes/blueimp-jquery-file-upload-3c5d440/css/jquery.fileupload-ui.css",
                    "/includes/jquery-treeview/jquery.treeview.css",
                    "/includes/datatables.css"
                    
                    ] />
<#if combine>
<!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <link rel="stylesheet" type="text/css" href="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".css","")}</#list>.css"></link>

<#else>
<#list srcs as src>
  <link rel="stylesheet" type="text/css" href="${src}" data-version="${common.tdarBuildId}"></link>
</#list>

</#if>


</#macro>
<#macro scripts combine=false>
<!--[if IE]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/Jit-2.0.1/Extras/excanvas.js"/>"></script><![endif]--> 

  <#-- formnavigate broken, abandoned. pls. fix kthxbye -->
  <#-- "/includes/jquery.FormNavigate.js",  -->
    <#local srcs = [
                 	 "/includes/jquery.cookie.js",
                     "/includes/jquery.xcolor-1.5.js",
                     "/includes/jquery.maphighlight.local.js",
	                 "/includes/jquery.textarearesizer.js",
	                 "/includes/jquery.watermark-3.1.3.min.js",
	                 "/includes/jquery.datatables-1.8.2/media/js/jquery.dataTables.js",
	                 "/includes/jquery.datatables-1.8.2/extras/bootstrap-paging.js",
                     "/includes/jquery-treeview/jquery.treeview.js"
                     "/includes/blueimp-javascript-templates/tmpl.min.js",
                     "/includes/jquery.aw-showcase.1.1.1/jquery.aw-showcase/jquery.aw-showcase.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.iframe-transport.js", 
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.fileupload.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.fileupload-fp.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/jquery.fileupload-ui.js",
                     "/includes/blueimp-jquery-file-upload-3c5d440/js/locale.js",
                     "/includes/jquery.populate.js",
                     "/includes/latLongUtil-1.0.js",
                     "/includes/gmaps.js",
                     "/includes/tdar.common.js",
                     "/includes/tdar.upload.js",
                     "/includes/tdar.repeatrow.js",
                     "/includes/tdar.autocomplete.js",
                 	 "/includes/tdar.datatable.js",
                     "/includes/tdar.dataintegration.js", 
                     "/includes/tdar.advanced-search.js",
                     "/includes/tdar.inheritance.js",
	                 "/includes/bindWithDelay.js",
	                 "/includes/flot-0.7/jquery.flot.min.js",
	                 "/includes/flot-0.7/excanvas.min.js",
	                 "/includes/flot-0.7/jquery.flot.pie.min.js",
                     "/includes/ivaynberg-select2-817453b/select2.js"
    ] />
    
    
    <#--TODO figure out a way to enable individual pages to append to this array -->
	<#--TODONT  this defeats the purpose of the caching -->
	<#--TOTHINKABOUT  yeah, but cached or not this is a lot of javascript for a browser to ingest -->
    
 <!-- this conflicts with JIT when loading in the context of a single script -->
<script type="text/javascript" src="/includes/jquery.tabby-0.12.js"></script>

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

    <#local srcs = [
                    "/css/tdar-bootstrap.css",
                    "/includes/ivaynberg-select2-817453b/select2.css",
                    "/includes/jquery.aw-showcase.1.1.1/jquery.aw-showcase/css/style.css",
                    "/includes/blueimp-jquery-file-upload-3c5d440/css/jquery.fileupload-ui.css",
                    "/includes/jquery-treeview/jquery.treeview.css"
                    
                    ] />
<#if combine>
<!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <link rel="stylesheet" type="text/css" href="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".css","")}</#list>.css"></link>

<#else>
<#list srcs as src>
  <link rel="stylesheet" type="text/css" href="${src}"></link>
</#list>

</#if>


</#macro>
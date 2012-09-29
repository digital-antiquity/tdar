<#macro scripts combine=false>
<!--[if IE]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/Jit-2.0.1/Extras/excanvas.js"/>"></script><![endif]--> 

<#assign srcs = [
				 "/includes/jquery-validation-1.8.1/jquery.validate.min.js",
				 "/includes/Jit-2.0.1/jit.js",
				 "/includes/gmaps.js",
				 "/includes/gzoom_uncompressed.js",
				 "/includes/jquery.metadata.2.1/jquery.metadata.js",
				 "/includes/jquery.maphighlight.local.js", 
				 "/includes/jquery.cookie.js",
				 "/includes/jquery.idTabs.min.js",
				 "/includes/jquery.history.js",
				 "/includes/jquery.xcolor-1.5.js",
				 "/includes/jquery-treeview/jquery.treeview.js",
				 "/includes/jquery.datatables-1.8.2/media/js/jquery.dataTables.js",
				 "/includes/tdar.common.js",
				 "/includes/bindWithDelay.js",
				 "/includes/jquery.aw-showcase.1.1.1/jquery.aw-showcase/jquery.aw-showcase.js",
				 "/includes/flot-0.7/jquery.flot.min.js",
				 "/includes/flot-0.7/excanvas.min.js",
				 "/includes/flot-0.7/jquery.flot.pie.min.js",
				 "/includes/jquery.populate.js",
				 "/includes/jquery-fileupload-4.4.1/jquery.fileupload.tdar.js",
				 "/includes/jquery-fileupload-4.4.1/jquery.fileupload-ui.js",
				 "/includes/jquery.textarearesizer.js",
				 "/includes/latLongUtil-1.0.js",
				 "/struts/utils.js",
				 "/includes/tdar.autocomplete.js",
				 "/includes/tdar.upload.js",
				 "/includes/tdar.inheritance.js", 
				 "/includes/tdar.dataintegration.js", 
				 "/includes/tdar.datatable.js",
				 "/includes/tdar.authority-management.js",
				 "/includes/tdar.advanced-search.js",
				 "/includes/jquery.FormNavigate.js",
				 "/includes/jquery.watermark-3.1.3.min.js"
				 ] />
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

<#assign srcs = ["/css/tdar.css", "/includes/datatables.css", 
				 "/includes/jquery-treeview/jquery.treeview.css",
				 "/includes/jquery-fileupload-4.4.1/jquery.fileupload-ui.css",
				 "/includes/jquery.aw-showcase.1.1.1/jquery.aw-showcase/css/style.css"] />

<#if combine>
<!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
	<link rel="stylesheet" type="text/css" href="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".css","")}</#list>.css"></link>

<#else>
<#list srcs as src>
  <link rel="stylesheet" type="text/css" href="${src}"></link>
</#list>

</#if>


</#macro>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#if featuredResources?? >
<#if request.requestURI?contains("featured") >
    <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/css/bootstrap-combined.min.css" rel="stylesheet">
    <link href="http://tdar.fervorinteractive.com/css/main.css" rel="stylesheet" />
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.1/jquery.min.js"></script>
    <script src="//netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/js/bootstrap.min.js"></script>
    <script src='http://tdar.fervorinteractive.com/js/main.js'></script>
</#if>
<div class="slider span12">
<h3>Featured Content</h3>
	<ul id="slider">
		<#list featuredResources as featuredResource>
		    <@view.tdarCitation featuredResource false />
	    </#list>
	</ul>
	<a href="" class="car-btn car-prev">Previous</a>
	<a href="" class="car-btn car-next">Next</a>
 </div>
<#if request.requestURI?contains("featured") >
	<script src="http://tdar.fervorinteractive.com/js/plugins.js"></script>
</#if>
</#if>

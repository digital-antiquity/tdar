<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#if featuredResources?? >
<#if request.requestURI?contains("featured") >
        <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/css/bootstrap-combined.min.css" rel="stylesheet">
        <link href="<@s.url value="/css/tdar-base.css"/>" rel="stylesheet" />
    <#import "/WEB-INF/macros/header.ftl" as header>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/${jquery_version}/jquery.min.js"></script>
    <@header.css production />

    <script src="//netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/js/bootstrap.min.js"></script>


</#if>
<div class="slider span12">
    <h3>Featured Content</h3>
    <div id="slider" class="carousel slide">
      <!-- Carousel items -->
      <div class="carousel-inner">
            <#list featuredResources as featuredResource>
                <@view.tdarCitation resource=featuredResource showLabel=false count=featuredResource_index />
            </#list>
      </div>
      <!-- Carousel nav -->
      <a class="carousel-control left" href="#slider" data-slide="prev">&lsaquo;</a>
      <a class="carousel-control right" href="#slider" data-slide="next">&rsaquo;</a>
    </div>
</div>
</#if>

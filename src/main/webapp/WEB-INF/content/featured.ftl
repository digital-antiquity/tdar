<title>Featured Item</title>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#if featuredResources?? >

<div class="tdar-slider slider span12">
    <h3>Featured Content</h3>

    <div id="slider" class="carousel slide">
        <!-- Carousel items -->
        <div class="carousel-inner">
            <#list featuredResources as featuredResource>
                <@view.tdarCitation resource=featuredResource showLabel=false count=featuredResource_index forceAddSchemeHostAndPort=true />
            </#list>
        </div>
        <!-- Carousel nav -->
        <a class="carousel-control left" href="#slider" data-slide="prev">&lsaquo;</a>
        <a class="carousel-control right" href="#slider" data-slide="next">&rsaquo;</a>
    </div>
</div>
</#if>

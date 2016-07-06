<title>Map</title>
<head>
<#assign host="">
<#if prefixHost>
	<#assign host="//assets.tdar.org/" />
</#if>
    <!-- BEGIN-PAGE-HEADER -->
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <link rel="unapi-server" type="application/xml" title="unAPI" href="${host}/unapi" />
    <link rel="search" type="application/opensearchdescription+xml" href='/opensearch.xml' title="Search tDAR" />

        <link href="${host}//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="${host}//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap.min.css" rel="stylesheet" media="print">
        <link rel="stylesheet" type="text/css" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/themes/smoothness/jquery-ui.min.css">


    <link href="${host}/css/tdar-base.css" rel="stylesheet">
    <link href="${host}/css/tdar-print.css" rel="stylesheet" media="print">
    <link rel="stylesheet" type="text/css" href="${host}/css/tdar-bootstrap.css" />
    <link rel="stylesheet" type="text/css" href="${host}/css/famfamfam.css" />
    <link rel="stylesheet" type="text/css" href="${host}/css/tdar-svg.css" />
    <link rel="stylesheet" type="text/css" href="${host}/components/leaflet/dist/leaflet.css" />
    <link rel="stylesheet" type="text/css" href="${host}/css/tdar.homepage.css" />
    <link rel="stylesheet" type="text/css" href="${host}/css/tdar.c3graph.css" />
    <link rel="stylesheet" type="text/css" href="${host}/css/tdar.leaflet.css" />
    <link rel="stylesheet" type="text/css" href="${host}/css/tdar.worldmap.css" />
    <link rel="stylesheet" type="text/css" href="${host}/css/tdar.sprites.css" />



    <script>TDAR_jsErrorDelim = "ɹǝʇıɯıןǝp?js_string";</script>
    <script type="text/javascript" src="${host}/js/tdar.errorutils.js?build10029"></script>
    <script src="${host}/includes/modernizr-custom-2.6.2.min.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js"></script>
    <script type="text/javascript" src="${host}//www.google.com/jsapi"></script>
<meta name="google-site-verification" content="rd6Iv58lPY7jDqfNvOLe9-WXRpAEpwLZCCndxR64nSg"/>
<meta name="msvalidate.01" content="121771274977CEA348D5B098DE1F823F"/>
<meta name="msvalidate.01" content="128653025DF7FC6B55A50F268FE1DEA0" />
<Meta name = "baidu-site-verification" content = "QBf6pGiSwd" />

<link rel='shortcut icon' href='/images/tdar-favicon.ico'/>
<link rel="apple-touch-icon" href="${host}/images/touch-icon-iphone.png">
<link rel="apple-touch-icon" sizes="76x76" href="${host}/images/touch-icon-ipad.png">
<link rel="apple-touch-icon" sizes="120x120" href="${host}/images/touch-icon-iphone-retina.png">
<link rel="apple-touch-icon" sizes="152x152" href="${host}/images/touch-icon-ipad-retina.png">

<meta name="description"
      content="The Digital Archaeological Record (tDAR) is the digital repository of Digital Antiquity, an organization devoted to enhancing preservation and access to digital records of archaeological investigations. tDAR is a national/international digital repository for archaeological information, including databases, reports, images, and other kinds of archaeological information. tDAR is a database of archaeological Information"/>
<meta name="keywords" content="tdar, database, archaeology, digital repository, Digital Antiquity, preservation, access"/>
<script type="text/javascript">
    (function(d) {
        var config = {
                    kitId: 'czp6njc',
                    scriptTimeout: 3000
                },
                h=d.documentElement,t=setTimeout(function(){h.className=h.className.replace(
                        /\bwf-loading\b/g,"")+" wf-inactive";},config.scriptTimeout),tk=d.createElement(
                        "script"),f=false,s=d.getElementsByTagName("script")[0],a;h.className+=" wf-loading";
                        tk.src='//use.typekit.net/'+config.kitId+'.js';tk.async=true;tk.onload=tk.onreadystatechange=function(){a=this.readyState;if(f||a&&a!="complete"&&a!="loaded")return;
                        f=true;clearTimeout(t);try{Typekit.load(config)}catch(e){}};s.parentNode.insertBefore(tk,s)
    })(document);
</script>
<style>
    @media screen {
        /* typekit fonts don't seem to work well in print -- we can revisit, but turning off for now */
        h1, h2, h3, header .welcome-drop p, #home .news span, .why article p, .why article ul, .contrib p, .contrib ul, .contrib .span3 li, .searchresults p, .searchresults aside li, .searchresults .sort p, .searchresults .sort form, .searchresults .pagin td.prev, .searchresults .pagin td.next, .searchresults .pagin li, header .welcome-drop p {
            font-family: "soleil", sans-serif !important;
        }

        #home .news li, .cols p, .tableFormat td, header form input.searchbox, header .welcome-drop a, .hero input.searchbox, #slider p, #slider span, .bucket p, #alt aside li, .why article li, .contrib li, .contrib .span3 p, .searchresults aside li, .searchresults aside label, .searchresults article p, footer ul, footer li {
            font-family: "ff-tisa-web-pro", serif !important;
        }

    }

    /* for typekit to prevent flash of unstyled content */
    .wf-loading {
        font-family: "helvetica";
        visibility: hidden;
    }

    .wf-active {
        visibility: visible;
    }
</style>
    

    
    <script type="application/ld+json">
{
   "@context": "http://schema.org",
   "@type": "WebSite",
   "url": "/",
   "potentialAction": {
     "@type": "SearchAction",
     "target": "http://localhost:8080/search/results?query={search_term_string}",
     "query-input": "required name=search_term_string"
   }
}
</script>


    <script type="text/javascript">
        var _gaq = _gaq || [];
            _gaq.push(['_setAccount', 'UA-13102200-5']); // TEST ACCOUNT
            _gaq.push(['_setDomainName', 'none']);

        _gaq.push(['_trackPageview']);

        (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();

        //return basic perf stats (excellent diagram: http://dvcs.w3.org/hg/webperf/raw-file/tip/specs/NavigationTiming/Overview.html#processing-model)
        (function() {
        var _getPerfStats = function() {
        var timing = window.performance.timing;
        return {
        //dns lookup timespan
        dns: timing.domainLookupEnd - timing.domainLookupStart,
        //connection timespan
        connect: timing.connectEnd - timing.connectStart,
        //time to first byte
        ttfb: timing.responseStart - timing.connectEnd,
        //timespan of response load
        basePage: timing.responseEnd - timing.responseStart,
        //time to document.load
        frontEnd: timing.loadEventStart - timing.responseEnd
        };
        };

        var _trackEvent = function() {
        var arr = ["_trackEvent"].concat(Array.prototype.slice.call(arguments));
        _gaq.push(arr);
        };

        var _reportPerfStats = function() {
        if (!(window.performance && window.performance.timing )) return;
        var nav = window.performance.navigation;
        var navtype = undefined;
        //backbutton navigation may skew stats. try to identify and tag w/ label (only supported in IE/chrome for now)
        if(nav && nav.type === nav.TYPE_BACK_FORWARD) {
        navtype = "backbutton";
        }

        if (typeof _gaq === "undefined") return;
        var perf = _getPerfStats();
        var key;
        for(key in perf) {
        _trackEvent("Navigation Timing(ms)", key, navtype, perf[key] ,  true);
        }
        };

        //here we explicitly hook into 'onload' since DOM timing stats are incomplete upon 'ready'.
        $(window).load(_reportPerfStats);
        })();
</script>


</head>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/header.ftl" as header>
<#assign mode>horizontal</#assign>
    <div class=" <#if mode == 'vertical'>span7<#else>span6 map mapcontainer</#if>">
            <h3>${siteAcronym} Worldwide</h3>
        <script type="application/json" data-mapdata>
			${homepageGraphs.mapJson}
        </script>
        <script type="application/json" data-locales>
			${homepageGraphs.localesJson}
        </script>

             <div id="worldmap" style="height:350px" data-max="">
             </div>
        <#if mode =='vertical'></div></#if>
             <div id="mapgraphdata"  <#if mode == 'vertical'>data-mode="vertical" class="span4 offset1"<#else>style="width:100%"</#if>>
        <#if mode =='vertical'><br/><br/></#if>
                 <h5 id="mapGraphHeader"></h5>
                 <div id='mapgraphpie'>                 
                 </div>
             </div>
        <#if mode !='vertical'></div></#if>
	<script>
	$(function() {
    	TDAR.worldmap.initWorldMap("worldmap","${mode}");
	});
	</script>
        <script src="//ajax.aspnetcdn.com/ajax/jquery.validate/1.13.1/jquery.validate.min.js"></script>
        <script src="//ajax.aspnetcdn.com/ajax/jquery.validate/1.13.1/additional-methods.min.js"></script>
        <script src="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js"></script>



        <script type="text/javascript" src="${host}/components/svgxuse/svgxuse.min.js"></script>
        <script type="text/javascript" src="${host}/components/leaflet/dist/leaflet.js"></script>
        <script type="text/javascript" src="${host}/includes/jquery.populate.js"></script>
        <script type="text/javascript" src="${host}/includes/Leaflet.Sleep.js"></script>
        <script type="text/javascript" src="${host}/components/d3/d3.min.js"></script>
        <script type="text/javascript" src="${host}/components/c3/c3.min.js"></script>
        <script type="text/javascript" src="${host}/js/tdar.core.js"></script>
        <script type="text/javascript" src="${host}/js/tdar.c3graphsupport.js"></script>
        <script type="text/javascript" src="${host}/js/tdar.c3graph.js"></script>
        <script type="text/javascript" src="${host}/js/tdar.d3tree.js"></script>
        <script type="text/javascript" src="${host}/js/maps/tdar.worldmap.js"></script>
  	    <script type="text/javascript"  src="${host}/components/leaflet-choropleth/dist/choropleth.js"></script>
<script id="c3colors">
 [<#list barColors as color><#if color_index != 0>,</#if>"${color}"</#list>] 
</script>

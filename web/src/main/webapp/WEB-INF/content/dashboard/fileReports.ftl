<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "common-dashboard.ftl" as dash />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/${config.themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>

<style>
.initials {
  border-radius: 50%;
  width: 24px;
  height: 24px;
  padding: 6px;
  //background: #fff;
  border: 1px solid #AAA;
  text-align: center;
}

.internal {
    border:1px solid #AAA !important;
    color: #999 !important;

}
.comment {
    border:1px solid #AAA;
    padding:10px;
    margin:10px;
}
</style>
</head>


<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">Reports</span></h1>

</div>
<div class="row">
<div class="span2">
    <@dash.sidebar current="files" />
</div>

<#include "balk-reports.html" />
</div>

<#noescape>
<script id="accountJson" type="application/json">
${accountJson}
</script>

</#noescape>


</#escape>
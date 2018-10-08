<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "common-dashboard.ftl" as dash />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>

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
    <h1>Dashboard &raquo; <span class="red">My Scratch Space</span></h1>

</div>
<div class="row">
<div class="col-2">
    <@dash.sidebar current="files" />
</div>
<#include '/components/tdar-autocomplete/template/autocomplete.html' />
<#include "balk.html" />
</div>

<#noescape>
<script id="accountJson" type="application/json">
${accountJson}
</script>

<script id="validFormats" type="application/json">
${validFormats}
</script>
</#noescape>

<script>
$(document).ready(function() {
    TDAR.vuejs.balk.main();
});    
</script>



</#escape>

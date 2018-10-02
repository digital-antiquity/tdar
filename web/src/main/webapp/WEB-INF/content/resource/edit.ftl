<#escape _untrusted as _untrusted?html>

<form method="POST" action="/resource/save">
<#assign uploadConfigId=''/>
<#assign fileProxies=[] />
<#include "vue-file-upload-template.html">
<#include "/components/tdar-autocomplete/template/autocomplete.html">
<#include "/components/tdar-taggging/template/tagging.html">
<#include "/components/tdar-values/template/values.html">
<#include "/WEB-INF/macros/creatorwidget.html">
<#include "/WEB-INF/macros/inheritance.html">

<script id="json" type="text/json">
<#noescape>
${json}
</#noescape>
</script>
<script id="materialTypes" type="text/json">
<#noescape>
${materialTypes}
</#noescape>
</script>
<script id="investigationTypes" type="text/json">
<#noescape>
${investigationTypes}
</#noescape>
</script>

<script id="submitter" type="text/json">
<#noescape>
${submitter}
</#noescape>
</script>

<script id="activeAccounts" type="text/json">
<#noescape>
${activeAccounts}
</#noescape>
</script>
<script id="primaryRoles" type="text/json">
<#noescape>
${primaryCreatorRoles}
</#noescape>
</script>
<script id="otherRoles" type="text/json">
<#noescape>
${creditCreatorRoles}
</#noescape>
</script>

<script id="fileUploadSettings" type="application/json">
<#noescape>
${fileUploadSettings!''}
</#noescape>
</script>
<script id="filesJson" type="text/json">
<#noescape>
<#if filesJson?has_content>${filesJson}<#else>[]</#if>
</#noescape>
</script>
<script>
$(document).ready(function(){

TDAR.vuejs.resourceEdit.init();
});
</script>
</form>
</#escape>
<#escape _untrusted as _untrusted?html>

<form method="POST" action="/document/save">


<#include "/components/tdar-autocomplete/template/autocomplete.html">
<#include "/components/tdar-taggging/template/tagging.html">
<#include "/components/tdar-values/template/values.html">
<#include "/WEB-INF/macros/creatorwidget.html">
<#include "/WEB-INF/macros/inheritance.html">

<button type="button" name="submit">Submit</button>
</form>
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
</#escape>
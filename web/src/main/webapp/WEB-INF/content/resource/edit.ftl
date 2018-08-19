<#escape _untrusted as _untrusted?html>

<form method="POST" action="/document/save">


<#include "/WEB-INF/macros/autocomplete.html">
<#include "/WEB-INF/macros/tagging.html">
<#include "/WEB-INF/macros/creatorwidget.html">
<#include "/WEB-INF/macros/inheritance.html">

<button type="submit" name="submit">Submit</button>
</form>
<script id="json" type="text/json">
<#noescape>
${json}
</#noescape>
</script>
</#escape>
<#escape _untrusted as _untrusted?html>
<#include "/WEB-INF/macros/helptext.ftl">

<#-- custom help text overriding macros here -->
<#macro copyrightHoldersTip>
<div id="divCopyrightHoldersTip" class="hidden">
    <p>Use this field to nominate a primary copyright holder.
    <p>Other information about copyright can be added in the 'notes' section by creating a new 'Rights & Attribution note...
    <dl>
        <dt>Type</dt><dd>Use the toggle at the left to select whether you're adding a Person or Institution</dd>
        <dt>Add Another</dt><dd> Use the '+' sign to add fields for either persons or institutions, and use the trash can to remove them.</dd>
    </dl>
</div>
</#macro>

</#escape>
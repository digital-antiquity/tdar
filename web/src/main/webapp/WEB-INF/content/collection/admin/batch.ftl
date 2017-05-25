<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "../common-collection.ftl" as commonCollection>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<head>
<title>Admin: ${collection.name}</title>
<style>
textarea {height:10rem;}
</style>

</head>
<body>

<h1>Collection Admin: <span class="red small">${collection.name}</span></h1>

<div class="row">
<div class="span12">
            <@common.resourceUsageInfo />
</div>
</div>
<div class="row">
    <div class="span12">
        <@s.form cssClass="form-horizontal" action="save" method="POST">

<div class="row">
    <div id="parentIdContainer" class="control-group">
        <label class="control-label" for="collectionName">Collection</label>
        <div class="controls">
            <input type="hidden" name="collectionId" value="" id="hdnParentId" autocompleteparentelement="#parentIdContainer">
            <input type="text" name="collectionName" maxlength="255" value="" id="txtParentCollectionName" class="input-xxlarge collectionAutoComplete ui-autocomplete-input ui-corner-all" autocompleteparentelement="#parentIdContainer" autocomplete="off" autocompleteidelement="#hdnParentId" autocompletename="name" placeholder="parent collection name">
        </div>
    </div>
</div>

<div class="row">
    <div id="parentIdContainer" class="control-group">
            <@s.select name="accountId" list="%{availableAccounts}" label="Account" title="Choose an account to bill from" listValue="name" listKey="id" emptyOption="true" />
    </div>
</div>

        <@s.hidden name="id" />

        <#list resources>
            <#items as res>
                <p><b>${res.id?c}</b> - <a href="${res.detailUrl}">${res.title}</a> (${res.resourceType} - ${res.status})</p>
                <@s.hidden name="ids[${res_index}]" value="${res.id?c}" cssClass="resource-id-field" />
                
                <svg class="svgicon svg-small red pull-left" style="position:absolute"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_${res.resourceType?lower_case}"></use></svg>                 
                <@s.textfield name="titles[${res_index}]" value="${res.title}" label="Title" cssClass="input-xxlarge span8 resource-title-field" />
                <#if res.resourceType.project>
                    <@s.hidden name="dates[${res_index}]" value="-1" label="Date" cssClass="input" /> 
                <#else>
                    <@s.textfield name="dates[${res_index}]" value="${res.date?c}" label="Date" cssClass="input" /> 
                </#if>
                <@s.textarea name="descriptions[${res_index}]" value="${res.description}" label="Description" cssClass="input-xxlarge span8 resource-description-field" />
            </#items>
        </#list>
        <@edit.submit fileReminder=false />
        </@s.form>
    </div>

</div>
</div>
<script>
$(document).ready(function() {
    TDAR.autocomplete.applyCollectionAutocomplete($("#txtParentCollectionName"), {showCreate: false}, {permission: "ADMINISTER_GROUP"});
});
</script>
</body>
</#escape>
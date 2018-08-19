<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "../common-collection.ftl" as commonCollection>
<#import "/WEB-INF/macros/search-macros.ftl" as search>

<head>
<title>Admin: ${collection.name}</title>
<style>
textarea {height:10rem;}
</style>

</head>
<body>

<h1>Collection Admin: <span class="red small">${collection.name}</span></h1>

<div class="row">
<div class="col-12">
            <@common.resourceUsageInfo />
</div>
</div>
<div class="row">
    <div class="col-12">
        <@s.form cssClass="form-horizontal" action="save" method="POST">

<h5>Changes</h5>
<div class="row mb-4">
        <label class="col-form-label col-2" for="collectionName">Add to Collection</label>
            <input type="text" name="collectionName" maxlength="255" value="" id="txtParentCollectionName" 
            	class="input-xxlarge collectionAutoComplete form-input ui-autocomplete-input col-6 ui-corner-all" autocompleteparentelement="#parentIdContainer" autocomplete="off" autocompleteidelement="#hdnParentId" autocompletename="name" placeholder="collection name">
            <input type="hidden" name="collectionId" value="" id="hdnParentId" autocompleteparentelement="#parentIdContainer">
</div>

        <@s.select name="accountId" list="%{availableAccounts}" label="Change to Account" title="Choose an account to bill from" listValue="name" 
        cssClass="col-6" listKey="id" emptyOption="true" labelposition='left' />

        <@s.hidden name="id" />
<h5>Resources</h5>
        <#list resources>
            <#items as res>
            <div class="row mb-3">
            <div class="col-1">
                <svg class="svgicon svg-small red pull-left" style="position:absolute"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_${res.resourceType?lower_case}"></use></svg>                 
            </div>
        	<div class="col-11">
                <p><b>${res.id?c}</b> - <a href="${res.detailUrl}">${res.title}</a> (${res.resourceType} - ${res.status})</p>
                <@s.hidden name="ids[${res_index}]" value="${res.id?c}" cssClass="resource-id-field" />
                <@s.textfield name="titles[${res_index}]" value="${res.title}" label="Title" cssClass="input-xxlarge col-9 resource-title-field" labelposition="left"/>
                <#if res.resourceType.project>
                    <@s.hidden name="dates[${res_index}]" value="-1" label="Date" cssClass="input" /> 
                <#else>
                    <@s.textfield name="dates[${res_index}]" value="${res.date?c}" label="Date" cssClass="col-3"  labelposition="left" /> 
                </#if>
                <@s.textarea name="descriptions[${res_index}]" value="${res.description}" label="Description" cssClass="input-xxlarge col-9 resource-description-field"  labelposition="left"/>
                </div>
                </div>
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
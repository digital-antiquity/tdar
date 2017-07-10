<#escape _untrusted as _untrusted?html>
    <#import "../dashboard/common-dashboard.ftl" as dash />

<head>
<title>Resource Export Request</title>
</head>
<body>
<h1>Export Resources from ${siteAcronym}</h1>


<div class="row">
<div class="span2">
<@dash.sidebar current="export" />
</div>
<div class="span10">

<h3>Select a Billing Account or Collection to export from</h3>
<br/>
<@s.form method=POST action="perform" class="form-horizontal">
<div class="row">
    <div class="span8">
        <div class="control-group" >
            <label class="control-label" for="collectionName">Account</label>
            <div class="">
                <@s.select name="accountId" list="%{accounts}" listValue="name" listKey="id" emptyOption='true' />
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="span8">
        <div id="parentIdContainer" class="control-group">
            <label class="control-label" for="collectionName">Collection</label>
            <div class="controls">
                <input type="hidden" name="collectionId" value="" id="hdnParentId" autocompleteparentelement="#parentIdContainer">
                <input type="text" name="collectionName" maxlength="255" value="" id="txtParentCollectionName" class="input-xxlarge collectionAutoComplete ui-autocomplete-input ui-corner-all" autocompleteparentelement="#parentIdContainer" autocomplete="off" autocompleteidelement="#hdnParentId" autocompletename="name" placeholder="parent collection name">
            </div>
        </div>
    </div>
</div>

<@s.submit class="button btn-primary" />

</@s.form>
</div>
</div>
<script>
$(document).ready(function() {
    TDAR.autocomplete.applyCollectionAutocomplete($("#txtParentCollectionName"), {showCreate: false}, {permission: "ADMINISTER_GROUP"});
});
</script>
</body>
</#escape>
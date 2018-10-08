<#escape _untrusted as _untrusted?html>
    <#import "../dashboard/common-dashboard.ftl" as dash />

<head>
<title>Resource Export Request</title>
</head>
<body>
<h1>Export Resources from ${siteAcronym}</h1>


<div class="row">
<div class="col-2">
<@dash.sidebar current="export" />
</div>
<div class="col-10">

<h3>Select a Billing Account or Collection to export from</h3>
<br/>
<@s.form method=POST action="perform" class="form-horizontal">
<div class="row">
    <div class="col-12">
        <div class="form-row mb-2" >
            <label class="col-form-label col-2" for="collectionName">Account</label>
            <@s.select name="accountId" list="%{accounts}" listValue="name" listKey="id" emptyOption='true' cssClass="col-6"/>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-12">
        <div id="parentIdContainer" class="form-row mb-2">
            <label class="col-form-label col-2" for="collectionName">Collection</label>
                <input type="text" name="collectionName" maxlength="255" value="" id="txtParentCollectionName" class="col-6 form-control collectionAutoComplete ui-autocomplete-input ui-corner-all" autocompleteparentelement="#parentIdContainer" autocomplete="off" autocompleteidelement="#hdnParentId" autocompletename="name" placeholder="parent collection name">
                <input type="hidden" name="collectionId" value="" id="hdnParentId" autocompleteparentelement="#parentIdContainer">
        </div>
    </div>
</div>

<@s.submit class="btn btn-primary" />

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
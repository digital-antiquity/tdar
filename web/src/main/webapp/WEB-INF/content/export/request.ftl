<head>
<title>Resource Export Request</title>
</head>
<body>
<h1>Export Resources from ${siteAcronym}</h1>

<@s.form method=POST action="perform" class="form-horizontal">

<h3>Select a Billing Account or Collection to export from</h3>
<br/>
<div class="row">
    <div class="control-group" >
        <label class="control-label" for="collectionName">Account</label>
    <div class="controls">
        <@s.select name="accountId" list="%{accounts}" listValue="name" listKey="id" emptyOption='true'   theme="xhtml" />
    </div>
    </div>
</div>
<div class="row">
    <div id="parentIdContainer" class="control-group">
        <label class="control-label" for="collectionName">Collection</label>
        <div class="controls">
            <input type="hidden" name="collectionId" value="" id="hdnParentId" autocompleteparentelement="#parentIdContainer">
            <input type="text" name="collectionName" maxlength="255" value="" id="txtParentCollectionName" class="input-xxlarge collectionAutoComplete ui-autocomplete-input ui-corner-all" autocompleteparentelement="#parentIdContainer" autocomplete="off" autocompleteidelement="#hdnParentId" autocompletename="name" placeholder="parent collection name">
        </div>
    </div>
</div>

<@s.submit class="button btn-primary" />

</@s.form>

<script>
$(document).ready(function() {
    TDAR.autocomplete.applyCollectionAutocomplete($("#txtParentCollectionName"), {showCreate: false}, {permission: "ADMINISTER_GROUP"});
});
</script>
</body>
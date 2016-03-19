<head>
<title>Resource Export Request</title>
</head>
<body>
<h1>Export Resources from ${siteAcronym}</h1>

<@s.form method=POST action="perform" class="form-horizontal">

<h3>Select a Billing Account or Collection to export from</h3>
<br/>
<div class="row">
    <div class="span2" >
        <label class="control-label" for="collectionName">Account</label>
    </div>
    <div class="span10">
        <@s.select name="accountId" list="%{accounts}" listValue="name" listKey="id" theme="xhtml" />
<br/>
    </div>
</div>
<div class="row">
    <div class="span2" >
        <label class="control-label" for="collectionName">Collection</label>
    </div>
    <div id="collection" class="controls-row span10">
                        <@s.hidden name="collectionId"  id="collectionId" />
                <@s.textfield theme="simple" id="collectionName" name="collectionName" cssClass="input-xxlarge collectionAutoComplete "  autocomplete="off"
                    autocompleteIdElement="#collectionId" maxlength=255
                    autocompleteParentElement="#collection" />
    </div>
</div>

<@s.submit class="button btn-primary" />

</@s.form>

<script>
$(document).ready(function() {
TDAR.autocomplete.applyCollectionAutocomplete($("#collection"), {showCreate: false}, {permission: "ADMINISTER_GROUP"});
});
</script>
</body>
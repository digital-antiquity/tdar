<head>
<title>Add a new information resource</title>
<meta name="lastModifiedDate" content="$Date$"/>
<script type='text/javascript' src='<@s.url value="/includes/jquery.validate-1.7.pack.js"/>'></script>
<script type='text/javascript'>
$(document).ready(function() {
    $('#selectResourceTypeForm').validate();
    $('#resourceTypeId').rules("add", {
        required: true,
        messages: {
            required: "Please select the type of resource you wish to add."
        }
    });
    $('#resourceTypeId').focus();
});

</script>
<style type="text/css">
.wwgrp {
    margin-bottom: 5px;
}
.wwgrp label {
    float: left;
    margin-right: 10px;
    font-weight: bolder;
    width: 120px;
}       

</style>
</head>
<div class="glide">
<@s.form id='selectResourceTypeForm' method='post' action='select' theme='css_xhtml'>
<h3>Select resource type</h3>
<@s.hidden name='projectId' value='${projectId}'/>
<@s.select cssStyle='width:150px;' labelposition='left' id='resourceTypeId' label='Resource type' name='resourceType' list='%{resourceTypes}' emptyOption='true' onchange="checkResourceType();"/>
<div>
<@s.submit value='Continue'/>
</div>
</@s.form>

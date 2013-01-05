<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "batch-common.ftl" as batchCommon>
<head>
<title>Batch Upload Template</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<h1>Bulk Upload</h1>
<div class="row">
	<div class="span9">
		<@s.form name='BulkMetadataForm' id='BulkMetadataForm'  cssClass="span8 form-horizontal well"  method='post' enctype='multipart/form-data' action='validate-template'>
		<h2>Basic</h2>
		
		
		<@batchCommon.printTemplate />
		
		<@edit.submit fileReminder=false span="span8" label="Validate Template" />
		
		</@s.form>
	</div>

	<div class="span3">
		<h2>Advanced</h2>
		<p>If you've used the bulk upload tool before, or already have a template that you know works, <a href="<@s.url value="add"/>">skip to the upload form</a>.
	</div>
</div>

</div>

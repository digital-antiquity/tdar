<#import "batch-common.ftl" as batchCommon>
<head>
<title>Batch Upload Template</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>

<@s.form name='BulkMetadataForm' id='BulkMetadataForm'  cssClass="span9 form-horizontal"  method='post' enctype='multipart/form-data' action='validate-template'>


<@batchCommon.printTemplate />

<@s.submit />

</@s.form>
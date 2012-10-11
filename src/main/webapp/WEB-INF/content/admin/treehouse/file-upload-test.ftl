<@s.set name="theme" value="'bootstrap'" scope="request" />
<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<#macro cssLink>
    <link rel="stylesheet" href="<@s.url value='/includes/blueimp-jquery-file-upload-3c5d440/css/jquery.fileupload-ui.css' />" >
</#macro>

<head>
    <@cssLink></@cssLink>
    <title>File Upload Test</title>
</head>
<body>
    <h1>file upload test</h1>
    
    
    
    
    <!-- The file upload form used as target for the file upload widget -->
    <@s.form id="fileupload2" action="/" method="POST" class="form-horizontal" enctype="multipart/form-data">
        <@s.hidden name="ticketId" id="ticketId" />
        <@s.hidden name="informationResourceId" id="id" value="6697" />
        
        <@edit.asyncFileUpload uploadLabel="Attach Document Files" showMultiple=true />
        

    </@s.form>
    <br>
    <div class="well">
        <h3>Demo Notes</h3>
        <p>fight the power</p>
    </div>
    

              <div class="btn-group">
                <button id="btnToggle" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">Action <span class="caret"></span></button>
                <ul class="dropdown-menu" id="tempul">
                  <li><b>Action</b></li>
                  <li><a href="#">Another action</a></li>
                  <li><a href="#">Something else here</a></li>
                  <li class="divider"></li>
                  <li><a href="#">Separated link</a></li>
                </ul>
              </div>    
    
<@edit.asyncUploadTemplates />
    
<script>
    $(function(){
        'use strict';
        TDAR.fileupload.registerUpload({formSelector:'#fileupload2', informationResourceId: $('#id').val()});
    
    });
</script>
</body>
</#escape>
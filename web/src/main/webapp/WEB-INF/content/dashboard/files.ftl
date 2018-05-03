<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "common-dashboard.ftl" as dash />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/${config.themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>

</head>


<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">My Scratch Space</span></h1>

</div>
<div class="row">
<div class="span2">
    <@dash.sidebar current="files" />
</div>
<div class="span10" id="filesTool">

<form class="form-horizontal">
		<@s.select name="account"  id="accountId"   listValue='name' listKey='id'   list=accounts cssClass="span4" dynamicAttributes={"@change":"loadFiles()"}/>
<div class="span4">
<input type="search" class="search input" placeholder="search"/>
</div>

<table class="table" id="filesTable">
<thead>
 <tr>
    <th></th>
    <th>file</th>
    <th>upload</th>
    <th>curate</th>
    <th>review</th>
    <th>status</th>
    <th colspan="2">actions</th>
 </tr>
</thead>
<tr>
 <td colspan="3"><p style='text-align:center;font-weight:bold'>Note: files in this space expire after 1 month.</td>
</tr>
        <tbody v-for="(file,index) in files">
            <tr v-if="index == 0 && parentId != undefined">
            <td></td>
            <td> <span class="link" @click="cd()"><i class="icon-folder-close"></i> .. </span>  </td>
            </tr>
            <tr class="template-download fade existing-file in"
                is="fileEntry" :index="index" :file="file" :editable="ableToUpload"></tr>
        </tbody>
        <tbody v-if="files.length == 0 && parentId != undefined">
        <tr>
            <td></td>
            <td> <span class="link" @click="cd()"><i class="icon-folder-close"></i> .. </span>  </td>
            </tr>
        </tbody>
</table>

    <label class="btn btn-success fileinput-button" :disabled="inputDisabled" for="fileupload" id="fileuploadWrapper"> <i
        class="glyphicon glyphicon-plus"></i> <span>Upload 
            files...</span> <!-- The file input field used as target for the file upload widget -->
        <input id="fileupload" type="file" name="files[]" multiple :disabled="inputDisabled" >
    </label>
    <div class="input-append">
    <input class="span2" id="dirName" name="dirName" type="text">
    <button class="btn" type="button" id="mkdir" @click="mkdir">Add Folder</button>
    </div>

     <br> <br>
     <div id="fileUploadErrors" class="fileuploaderrors controls controls-row">
        
        <ul v-for="(error,index) in errors" >
            <li> <label :for="append('fue',index )">{{ error }}</label>
                 <input type="text" :id="append('fue',index )" required="true" class=" required hidden"  :title="error" /> </li>
        </ul>

    </div>

    <!-- The global progress bar -->
    <div id="progress" class="progress">
        <div class="progress-bar progress-bar-success"></div>
    </div>
    <div id="uploadstatus"></div>


    </div>
</div>

</div>


</div>
<!-- 
    <th></th>
    <th>file</th>
    <th>upload</th>
    <th>curate</th>
    <th>review</th>
    <th>status</th>
    <th colspan="2">actions</th>

-->

<template id="file-entry-template">
<tr v-bind:id="rowId">
    <td>{{ 1 + index}}</td>
    <td> <span v-if="file.size == undefined " class="link" @click="cd(file)"><i class="icon-folder-close"></i> {{file.name}} </span> 
    	 <span v-if="file.size != undefined "><a :href="downloadUrl()">{{file.name }}</a>  </span> </td>
    <td>{{file.uploaderInitials}} {{formatDate(file.dateCreated)}}</td>
    <td>{{file.curatedByInitials}} {{formatDate(file.dateCurated)}} <i v-if="file.curatedByName == undefined && file.size != undefined" @click="markCurated()" class="icon-thumbs-up"></i></td>
    <td>{{file.reviewedByInitials}} {{formatDate(file.dateReviewed)}}<i v-if="file.reviewedByName == undefined && file.size != undefined && file.dateCurated != undefined" @click="markReviewed()" class="icon-thumbs-up"></i></td>
    <td> </td>
    <td><a :href="file.resourceUrl">{{file.resourceId }}</a> 
    </td>
    <td>
        <a :href="fileLink" v-if="file.resourceId == undefined && file.size != undefined"><i class="icon-pencil"></i></a>
        <i  class="icon-comment"></i>
        <a href="#" @click="moveUI()"><i class="icon-folder-open"></i></a>
        <a href="#" @click="deleteFile()"><i class="icon-trash"></i></a>
    </td>
    </tr>
</template>

<script>
$(document).ready(function() {
	TDAR.vuejs.balk.main();
});    
</script>



</#escape>
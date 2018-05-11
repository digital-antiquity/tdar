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

<style>
.initials {
  border-radius: 50%;
  width: 24px;
  height: 24px;
  padding: 6px;
  //background: #fff;
  border: 1px solid #AAA;
  text-align: center;
}

.internal {
    border:1px solid #AAA !important;
    color: #999 !important;

}
.comment {
    border:1px solid #AAA;
    padding:10px;
    margin:10px;
}
</style>
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
<div class="row">
    <div class="span6">
    <label>Account:
        <select name="account" id="accountId" v-model="accountId">
                <option v-for="(option, index) in accounts" v-bind:value="option.id"> {{ option.name }} </option>
        </select>
    </label>
    </div>
    <div class="span4">
        <input type="search" class="search input pull-right" placeholder="search" v-model="search" />
    </div>
</div>

<table class="table" id="filesTable">
<thead>
 <tr>
    <th></th>
    <th>file</th>
    <th>upload</th>
    <th v-if="fullService">curate</th>
    <th v-if="fullService && studentReviewed">student review</th>
    <th v-if="fullService"><span v-if="studentReviewed">staff </span> review</th>
    <th v-if="fullService && externalReviewed">external review</th>
    <th>status</th>
    <th colspan="2">actions</th>
 </tr>
</thead>
<tr>
 <td colspan="20"><p style='text-align:center;font-weight:bold'>Note: files in this space expire after {{daysFilesExpireAfter}} days.</td>
</tr>
        <tbody >
	        <tr v-if="parentId != undefined">
            <td></td>
            <td> <span class="link" @click="cd()"><i class="icon-folder-close"></i> .. </span>  </td>
            </tr>
            <tr class="template-download fade existing-file in" v-for="(file,index) in files"
                is="fileEntry" :index="index" :file="file" :editable="ableToUpload" 
                :fullservice="fullService" :studentreviewed="studentReviewed" :externalreviewed="externalReviewed"></tr>
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


    	<div class="modal hide fade" id="comments-template-modal">
	  <div class="modal-header">
	    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	    <h3 v-if="commentFile != undefined">Comments for {{commentFile.name}}</h3>
	  </div>
	  <div class="modal-body">
	    <div>
	            <div  v-for="(comment,index) in comments" :fileid="commentFile.id"
	                is="comment" :index="index" :comment="comment"></div>
	    </div>
	    <textarea name="comment" v-model="comment"></textarea>
	    <span  class="link" @click="addComment()">add comment</span>
	  </div>
	  <div class="modal-footer">
	    <a href="#" data-dismiss="modal" class="btn">Close</a>
	  </div>
	</div>

    </div>



<template id="part-entry-template">
    <span>
        <span class='initials' v-if="initials != undefined">{{initials}}</span>
        <span class="date">{{formatDate(date)}}</span>
    </span>
</template>


<template  id="comment-entry-template">
    <div :class="commentClass">
	<pentry :initials="comment.commentorInitials" :date="comment.dateCreated" :name="comment.commentorName" ></pentry>
{{comment.comment}} 
   <span v-if="comment.resolved != undefined" class="pull-right">
	    <span v-if="comment.resolved == false"><input type="checkbox" @click="resolveComment()"/> resolve</span>
		<i v-if="comment.resolved">{{comment.resolverName}} {{comment.dateResolved}}</i>
	</span>	
	</div>
</template>


<template id="file-entry-template">
<tr v-bind:id="rowId">
    <td>{{ 1 + index}}</td>
    <td> <span v-if="file.size == undefined " class="link" @click="cd(file)"><i class="icon-folder-close"></i> {{file.name}} </span> 
    	 <span v-if="file.size != undefined "><a :href="downloadUrl">{{file.name }}</a>  </span> </td>
    <td>
        <pentry v-if="file.size != undefined" :initials="file.uploaderInitials" :date="file.dateCreated" :name="file.uploaderName" ></pentry> 
                
        </td>
    <td v-if="fullservice && (file.wontCurate == undefined || file.wontCurate == true)">
        <pentry :initials="file.curatedByInitials" :date="file.dateCurated" :name="file.curatedByName" ></pentry> 
        <i v-if="canCurate" @click="markCurated()" class="icon-thumbs-up"></i>
        <i v-if="!cannotCurate" @click="wontCurate()" class="icon-thumbs-down"></i>
    </td>
    <td v-if="fullservice && studentreviewed"> 
        <pentry :initials="file.studentReviewedByInitials" :date="file.dateStudentReviewed" :name="file.studentReviewedByName" ></pentry> 
        <i v-if="canStudentReview" @click="markStudentReviewed()" class="icon-thumbs-up"></i></td>
    <td v-if="fullservice">
       <pentry :initials="file.reviewedByInitials" :date="file.dateReviewed" :name="file.reviewedByName" ></pentry> 
        <i v-if="canReview" @click="markReviewed()" class="icon-thumbs-up"></i></td>
    <td v-if="fullservice && externalreviewed">
       <pentry :initials="file.externalReviewedByInitials" :date="file.dateExternalReviewed" :name="file.externalReviewedByName" ></pentry> 
        <i v-if="canExternalReview" @click="markExternalReviewed()" class="icon-thumbs-up"></i></td>
    <td v-if="fullservice">  <input name="type" value="" v-model="file.note" /> <span class="link" v-if="noteChanged" @click="updateNote()">save</span> </td>
    <td><a :href="file.resourceUrl">{{file.resourceId }}</a> 
    </td>
    <td>
        <a :href="fileLink" v-if="file.resourceId == undefined && file.size != undefined"><i class="icon-pencil"></i></a>
    </td><td nowrap>
        <a href="#" v-if="fullservice" @click="showComments()"><i class="icon-comment"></i>
        <span class="label"v-if="file.comments != undefined && file.comments.length > 0">{{file.comments.length}}</span></a>
    </td><td>
        <a href="#" @click="moveUI()"><i class="icon-folder-open"></i></a>
    </td><td>
        <a href="#" v-if="file.resourceId == undefined && file.size != undefined" @click="deleteFile()"><i class="icon-trash"></i></a>
    </td>
    </tr>
</template>
</div>

</div>

<#noescape>
<script id="accountJson" type="application/json">
${accountJson}
</script>

<script id="validFormats" type="application/json">
${validFormats}
</script>
</#noescape>

<script>
$(document).ready(function() {
	TDAR.vuejs.balk.main();
});    
</script>



</#escape>
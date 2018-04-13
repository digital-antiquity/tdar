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
<div class="span4">
<div class="control-group">
  <label class="control-label" for="inputInfo">Select Billing Account</label>
  <div class="controls">
    <select name="account" id="account">
        <option>Veterans Curation Program</option>
    </select>
  </div>
</div>
</div>
<div class="span4">
<input type="search" class="search input" placeholder="search"/>
</div>

<table class="table">
<thead>
 <tr>
    <th>file</th>
    <th>status</th>
    <th>action</th>
 </tr>
</thead>
<tr>
 <td colspan="3"><p style='text-align:center;font-weight:bold'>Note: files in this space expire after 1 month.</td>
</tr>
<tr>
{{ files }}
</tr>
</table>



    </div>
</div>

</div>


</div>



<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        
        
        
        var app = new Vue({
            el : "#filesTool",
            data : {
                url : "/api/file/listFiles",
                validFormats : [],
                ableToUpload : true,
                parentId: undefined,
                files : [],
                path : "" 
            },
            computed : {
                inputDisabled : function() {
                    return !this.ableToUpload;
                }
            },
            methods : {
                loadFiles: function (parentId) {
                var _app = this;
                    $.get(this.url, {
                    dataType:'jsonp',
                    data: {"parentId": parentId}
                    }).done(function(msg){
                    console.log(msg);
                    Vue.set(_app,"files", msg);
                    });
                }
            },
            mounted : function() {
                // setup
                if (this.ableToUpload == undefined || this.ableToUpload == false) {
                    console.log('file upload disabled');
                    return;
                }
                this.loadFiles(undefined);
            }
        });
    });
    
    
    
    
</script>



</#escape>
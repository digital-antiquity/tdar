<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "../common-collection.ftl" as commonCollection>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<head>
<title>Admin: ${collection.name}</title>
</head>
<body>

<h1>Collection Admin: <span class="red small">${collection.name}</span></h1>

<div class="row">
<div class="span12">
            <@common.resourceUsageInfo />
</div>
</div>
<div class="row">
    <div class="span12">
        <@s.form cssClass="form-horizontal" action="save" >

		<@s.hidden name="id" />

        <#list collection.resources>
            <#items as res>
                <p><b>${res.id?c}</b> - <a href="${res.detailUrl}">${res.title}</a> (${res.resourceType})</p>
                <@s.hidden name="ids[${res_index}]" value="${res.id?c}" /> 

                <@s.textfield name="titles[${res_index}]" value="${res.title}" label="Title" cssClass="input-xxlarge span8" /> 
                <@s.textfield name="dates[${res_index}]" value="${res.date?c}" label="Date" cssClass="input" /> 
                <@s.textarea name="descriptions[${res_index}]" value="${res.description}" label="Description" cssClass="input-xxlarge span8" /> 
               </tr>
            </#items>
        </#list>
        <@edit.submit fileReminder=false />
    	</@s.form>
    </div>

</div>
</body>
</#escape>
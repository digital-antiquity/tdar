<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<h1>Administrative info for <span>${resource.title}</span></h1>


<h2>Resource Revision History</h2>
<table class="table tableFormat">
    <tr>
        <th>When</th>
        <th>Who</th>
        <th>What</th>
        <th>Duration</th>
        <th>Event</th>
    </tr>
    <#list resourceLogEntries as entry>
        <tr>
            <td>${entry.timestamp}</td>
            <td>${(entry.person.properName)!''}</td>
            <td>${entry.type!''}</td>
            <td>${entry.timeInSeconds!'?'}</td>
            <td>${entry.logMessage}</td>
        </tr>
    </#list>
</table>



    <#if (resource.informationResourceFiles?has_content )>
    <h2>File History</h2>
    <table class="table tableFormat">
        <tr>
            <th colspan="2">Name</th>
            <th>Type</th>
            <th>Version #</th>
            <th>Restriction</th>
            <th>Status</th>
            <th>Size</th>
            <th>Date Uploaded</th>
            <th>MimeType</th>
            <th>Processing Errors?</th>
        </tr>
        <#list resource.informationResourceFiles as file>
            <tr>
                <td colspan="2"><strong>${file.filename!"unnamed file"}</strong>
                    <#if file.latestThumbnail?? && !file.deleted>
                        <br><img src="<@s.url value="/files/sm/${file.latestThumbnail.id?c}"/>">
                    </#if>
                </td>
                <td>${file.informationResourceFileType!'UNKNOWN'}</td>
                <td>${file.latestVersion}</td>
                <td>${file.restriction}</td>
                <td>${file.status!""} ${file.deleted?string("(deleted)", "")}</td>
                <#assign orig = file.latestUploadedVersion />
                <td></td>
                <td></td>
                <td></td>
                <td><#if !file.processed && file.errorMessage?has_content>${file.errorMessage}</#if></td>
            </tr>
            <#list file.informationResourceFileVersions as vers>
                <#if vers.uploaded >
                    <tr>
                        <td></td>
                        <td>
                            <a href="<@s.url value='/filestore/get/${id?c}/${vers.id?c}'/>?coverPageIncluded=false"
                               onClick="TDAR.common.registerDownload('<@s.url value='/filestore/get/${id?c}/${vers.id?c}'/>', '${id?c}')">${vers.filename}</a></td>
                        <td>${vers.fileVersionType} </td>
                        <td>${vers.version}</td>
                        <td></td>
                        <td></td>
                        <#assign orig = file.latestUploadedVersion />
                        <td>${vers.fileLength!0}</td>
                        <td>${vers.dateCreated}</td>
                        <td>${vers.mimeType}</td>
                        <td></td>
                    </tr>
                </#if>
            </#list>
        </#list>
    </table>
    </#if>

	<table class="table">
		<thead>
			<tr><th>filename</th><th>date</th><th>size</th></tr>
		</thead>
		<tbody>
			<#list xmlFiles as file>
				<tr><td><a href="<@s.url value="/resource/admin/xml?id=${id?c}&filename=${file.name}"/>">${file.name}</a></td><td></td><td>${file.totalSpace}</td></tr>
			</#list>
		</tbody>
	</table>
    <@view.accessRights />

    <@view.adminFileActions />
</#escape>
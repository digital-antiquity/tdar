<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<title>Managing citation information for ${resource.title}</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
<h2>Managing citations for ${resource.title}</h2>
<@edit.toolbar "citations" />
<p>
Source and related citations for <b>${resource.title}</b>.
</p>
<@s.form id='addCitationForm' method='post' action='addCitation' >
<fieldset>
<legend><b>Source Citations</b></legend>
<#if resource.sourceCitations?? && ! resource.sourceCitations.isEmpty()>
    <ul list-style='none'>
    <@s.iterator status='rowStatus' value='resource.sourceCitations' var='sourceCitation'>
        <li><a href="<@s.url value='/document/view' resourceId='${sourceCitation.id?c}'/>">${sourceCitation.title} : ${sourceCitation.joinedAuthors}</a></li>
    </@s.iterator>
    </ul>
<#else>
    No source citations.  
</#if>
<hr/>
<a href="<@s.url value='/document/source' linkedResourceId='${resource.id?c}'/>">Add a source citation</a>
</fieldset>
<br/>

<fieldset>
<legend><b>Related citations</b></legend>
<#if resource.relatedCitations?? && ! resource.relatedCitations.isEmpty()>
    <table>
    <@s.iterator status='rowStatus' value='resource.relatedCitations' var='relatedCitation'>
        <tr><td>${relatedCitation}</td></tr>
    </@s.iterator>
    </table>
<#else>
    No related citations.  
</#if>
<hr/>
<a href="<@s.url value='/document/related' linkedResourceId='${resource.id?c}'/>">Add a related citation</a>

</fieldset>
</table>
</fieldset>
</@s.form>
</body>

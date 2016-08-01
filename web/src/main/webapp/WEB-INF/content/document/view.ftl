<#escape _untrusted as _untrusted?html>

    <#macro sidebarDataTop>
        <#if resource.seriesName?has_content>
        <li><strong>Series Name</strong><br>${resource.seriesName}</li>
        </#if>
        <#if resource.seriesNumber?has_content>
        <li><strong>Series Number</strong><br>${resource.seriesNumber}</li>
        </#if>
        <#if resource.journalName?has_content>
        <li><strong>Journal</strong><br>${resource.journalName}</li>
        </#if>
        <#if resource.volume?has_content>
        <li><strong>Volume</strong><br>${resource.volume}</li>
        </#if>
        <#if resource.journalNumber?has_content>
        <li><strong>Issue</strong><br>${resource.journalNumber}</li>
        </#if>

        <#if resource.bookTitle?has_content && resource.documentType.bookTitleDisplayed>
        <li><strong>Book Title</strong><br>${resource.bookTitle}</li>
        </#if>
        <#if resource.numberOfVolumes??>
        <li><strong>Number of Volumes</strong><br>${resource.numberOfVolumes}</li>
        </#if>
        <#if resource.edition?has_content>
        <li><strong>Edition</strong><br><span>${resource.edition}</span></li>
        </#if>
    </#macro>

    <#macro sidebarDataBottom>
        <#if resource.isbn?has_content>
        <li><strong>ISBN</strong><br><span><a href="http://www.worldcat.org/isbn/${resource.isbn?replace("-","")}">${resource.isbn}</a></span>
        </li>
        </#if>
        <#if resource.issn?has_content>
        <li><strong>ISSN</strong><br><span>${resource.issn}</span></li>
        </#if>

        <#if resource.documentType?has_content>
            <#if (resource.startPage?has_content) || (resource.endPage?has_content) || (resource.totalNumberOfPages?has_content)>
            <li>
                <strong>Pages</strong><br>
                <#assign showParen = false/>
                <#if resource.documentType.partOfLargerDocument>
                    <#if resource.startPage?has_content && resource.endPage?has_content>
                    ${resource.startPage} - ${resource.endPage}
                    </#if>
                    <#assign showParen = true/>
                </#if>
            </#if>
            <#if resource.totalNumberOfPages?? >
                <#if showParen >(</#if>
            ${resource.totalNumberOfPages}
                <#if showParen >)</#if>
            <#if resource.numberOfPages?? >
                <#if showParen >(</#if>
            ${resource.numberOfPages}
                <#if showParen >)</#if>
            </li>
            </#if>

            <#if (((resource.publisher.name)?has_content ||  resource.publisherLocation?has_content))>
            <li><strong>
            <#-- label -->
                <#if resource.documentType?has_content>
                ${resource.documentType.publisherName}
                <#else>
                    Publisher
                </#if></strong><br>
                <#if resource.publisher?has_content><span><@view.browse creator=resource.publisher /></span></#if>
                <#if resource.degree?has_content>${resource.degree.label}</#if>
                <#if resource.publisherLocation?has_content> (${resource.publisherLocation}) </#if>
            </li>
            </#if>


        <li>
            <strong>Document Type</strong><br>
        ${resource.documentType.label} <#if resource.documentSubType?has_content>(${resource.documentSubType.label})</#if>
        </li>
        </#if>

    </#macro>

</#escape>
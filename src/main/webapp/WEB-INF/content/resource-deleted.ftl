<head>
<title>Resource Deleted</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<p>
    Sorry, the resource you requested has been marked as <em>deleted</em>.  
    <#if !authenticatedUser??>
    Note that deleted resources remain archived in tDAR.  Note that tDAR users with access rights to deleted documents
    may access these resources by logging into tDAR prior to accessing this URL
    <#else>
    <#-- authenticated users that see this page do not have sufficient access, otherwise they are routed to the view page -->
    Note that deleted resources remain archived in tDAR.  If you feel this deletion was in error, or if you wish to request
    access to the deleted content,  please contact us at <a href="mailto:comments@tdar.org">comments@tdar.org</a> or 
    <a href="tel:4809651369">(480) 965-1369</a>
    </#if>
</p>

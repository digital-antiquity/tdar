<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#escape _untrusted as _untrusted?html >

<head>
<title>${authenticatedUser.properName}'s Workspace</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>

<h2>Your Bookmarked Resources</h2>
<@rlist.toolbar "workspace"  />



<@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' editable=false bookmarkable=true  expanded=true listTag='ol' headerTag="h3" />

</#escape>
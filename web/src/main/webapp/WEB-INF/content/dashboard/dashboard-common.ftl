<#macro sidebar current="dashboard">
    <ul class="nav nav-list nav-stacked dashboard-nav">
      <li <@activeIf current "dashboard" />><a href="/dashboard"> My Resources</a></li>
      <li class="nav-header">My Library</li>
      <li  <@activeIf current "collections" />> <a href="#">Collections</a></li>
      <li  <@activeIf current "bookmarks" />> <a href="#"> Bookmarks</a></li>
      <li class="nav-header">Shares</li>
      <li  <@activeIf current "share" />><a href="/manage">Share</a></li>
      <li><a href="#">Shares</a></li>
      <li><a href="#">Resources</a></li>
      <li><a href="#">With Me</a></li>
      <li class="nav-header">My Account</li>
      <li <@activeIf current "billing" />><a href="/billing">Billing Accounts</a></li>
      <li><a href="/entity/user/myprofile">Profile</a></li>
    </ul>
</#macro>

<#macro activeIf current test>
<#if current == test>class="active"</#if>
</#macro>
<div class='info'>
  <#if !production>
    <p>You are using ${siteAcronym}'s test server.
    </p>
    <p>${siteAcronym} is free to use. You must be logged in to download content, deposit resources or utilise advanced features of the site. 
      All use is governed by the <a class="external" href="http://www.fedarch.org/wordpress/?page_id=634">User Agreement</a>.
    </p>
    <p>We appreciate and encourage you to send comments, suggestions and bug reports to <a
    href="mailto:${contactEmail}?subject=${siteAcronym}-prod%20comments&amp;">${contactEmail}</a>
    </p>
  <#else>
    <p>${siteAcronym} is free to use. You must be logged in to download content, deposit resources or utilise advanced features of the site. 
      All use is governed by the <a class="external" href="http://www.fedarch.org/wordpress/?page_id=634">User Agreement</a>.
    </p> 
    <p>We appreciate and encourage you to send comments, suggestions and bug reports to <a
    href="mailto:${contactEmail}?subject=${siteAcronym}-test%20comments&amp;">${contactEmail}</a></p>
  </#if>
</div>

<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro keywordStats id title stats hier=false>
    <div class="glide compact">
        <h3>${title}</h3>
        <table id="${id}">
              <thead class="thead-dark">

            <tr>
                <th>Keyword</th>
                <#if hier>
                    <th>Index</th></#if>
                <th>Count</th>
            </tr>
            </thead>
            <tbody class="compact">
                <#list stats as stat>
                <tr>
                    <td>${stat.first.label}</td>
                    <#if hier>
                        <th>${stat.first.index!"n/a"}</th></#if>
                    <td>${stat.second}</td>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
    </#macro>


<head>
    <title>Administrator Dashboard: Keyword stats</title>
</head>

<body>
    <#assign selectedStat = request.getParameter("keywordType")!"blank" />
<div class="glide">
    <h3>Pick a Keyword Type</h3>
    <ul>
        <li><a href="keyword-stats?keywordType=all">All</a></li>
        <li><a href="keyword-stats?keywordType=controlledCultureKeywordStats">Culture Keywords (controlled)</a></li>
        <li><a href="keyword-stats?keywordType=uncontrolledCultureKeywordStats">Culture Keywords (uncontrolled)</a></li>
        <li><a href="keyword-stats?keywordType=geographicKeywordStats">Geographic Keywords</a></li>
        <li><a href="keyword-stats?keywordType=investigationTypeStats">Investigation Types</a></li>
        <li><a href="keyword-stats?keywordType=materialKeywordStats">Material Keywords</a></li>
        <li><a href="keyword-stats?keywordType=otherKeywordStats">Other Keywords</a></li>
        <li><a href="keyword-stats?keywordType=siteNameKeywordStats">Site Names</a></li>
        <li><a href="keyword-stats?keywordType=controlledSiteTypeKeywordStats">Site Types (controlled)</a></li>
        <li><a href="keyword-stats?keywordType=uncontrolledSiteTypeKeywordStats">Site Types (uncontrolled)</a></li>
        <li><a href="keyword-stats?keywordType=temporalKeywordStats">Temporal Keywords</a></li>
    </ul>
</div>
    <#if selectedStat="all">
        <@keywordStats "t1" "Controlled Culture Keywords"       controlledCultureKeywordStats       />
        <@keywordStats "t2" "Uncontrolled Culture Keywords"     uncontrolledCultureKeywordStats     />
        <@keywordStats "t3" "Geographic Keywords"               geographicKeywordStats              />
        <@keywordStats "t4" "Investigation Types"               investigationTypeStats              />
        <@keywordStats "t5" "Material Keywords"                 materialKeywordStats                />
        <@keywordStats "t6" "Other Keywords"                    otherKeywordStats                   />
        <@keywordStats "t7" "Site Names"                        siteNameKeywordStats                />
        <@keywordStats "t8" "Controlled Site Type Keywords"     controlledSiteTypeKeywordStats      />
        <@keywordStats "t9" "Uncontrolled Site Type Keywords"   uncontrolledSiteTypeKeywordStats    />
        <@keywordStats "t10" "TemporalKeyword"                  temporalKeywordStats                />

    <#elseif selectedStat ="controlledCultureKeywordStats"  >
        <@keywordStats "t1" "Controlled Culture Keywords"       controlledCultureKeywordStats       />
    <#elseif selectedStat ="uncontrolledCultureKeywordStats"  >
        <@keywordStats "t2" "Uncontrolled Culture Keywords"     uncontrolledCultureKeywordStats     />
    <#elseif selectedStat ="geographicKeywordStats"  >
        <@keywordStats "t3" "Geographic Keywords"               geographicKeywordStats              />
    <#elseif selectedStat ="investigationTypeStats"  >
        <@keywordStats "t4" "Investigation Types"               investigationTypeStats              />
    <#elseif selectedStat ="materialKeywordStats"  >
        <@keywordStats "t5" "Material Keywords"                 materialKeywordStats                />
    <#elseif selectedStat ="otherKeywordStats"  >
        <@keywordStats "t6" "Other Keywords"                    otherKeywordStats                   />
    <#elseif selectedStat ="siteNameKeywordStats"  >
        <@keywordStats "t7" "Site Names"                        siteNameKeywordStats                />
    <#elseif selectedStat ="controlledSiteTypeKeywordStats"  >
        <@keywordStats "t8" "Controlled Site Type Keywords"     controlledSiteTypeKeywordStats      />
    <#elseif selectedStat ="uncontrolledSiteTypeKeywordStats"  >
        <@keywordStats "t9" "Uncontrolled Site Type Keywords"   uncontrolledSiteTypeKeywordStats    />
    <#elseif selectedStat ="temporalKeywordStats"  >
        <@keywordStats "t10" "TemporalKeyword"                  temporalKeywordStats                />
    </#if>
</body>
</#escape>
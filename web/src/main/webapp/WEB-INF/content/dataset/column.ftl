<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<div class="glide">

    <table>
        <tr>
            <th>Value</th>
            <th>Ontology Mapped Value</th>
            <th>Count</th>
        </tr>

        <#list mappedDataValues as value >
            <tr>
                <td>${value.term}</td>
                <td>${value.node.displayName}</td>
                <td>${value.count}</td>
            </tr>
        </#list>
    </table>
</div>

</#escape>

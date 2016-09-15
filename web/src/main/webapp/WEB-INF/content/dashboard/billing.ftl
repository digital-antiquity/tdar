<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>
    <#import "dashboard-common.ftl" as dash />

<head>
    <title>Billing Accounts</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>${authenticatedUser.properName}'s Billing Accounts</h1>

</div>


<div class="row">
    <div class="span2">
        <@dash.sidebar current="billing" />
    </div>
    <div class="span8">
        <@accountSection />
    </div>
    <div class="span2">
      <h5>Add Space</h5>
    <@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action='process-choice'>
                <table class="table pTable">
                    <tbody>
                        <tr>
                            <th class="borderRight">Files</th>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfFiles" theme="simple" cssClass="integer span1 orderinfo" maxlength=9  />
                            </td>
                        </tr>
                        <tr>
                            <th class="borderRight">Space (MB)</th>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfMb"  theme="simple" cssClass="integer span1 orderinfo" maxlength=9 />
                            </td>
                        </tr>
                        <tr>
                            <th class="borderRight">
                                Cost
                            </th>
                            <td class="custom" style="text-align:center">
                                $<span class="red" id="price">0.00</span>
                            </td>
                        </tr>
                        <tr>
                            <th class="borderRight"></th>
                            <td class="custom">
                                <div class="center">
                                    <button class="button btn btn-primary tdar-button">Continue</button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
                </@s.form>
  
    </div>

</div>
<#macro accountSection>
            <#list accounts>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Invoices</th>
                            <th>Space</th>
                            <th>Files</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#items as account>
                        <tr>
                            <td><a href="${account.detailUrl}">${account.name}</a></td>
                            <td>${account.invoices?size}</td>
                            <td id="space${account.id?c}"><span class="glabel">${account.totalSpaceInMb}</span></td>
                            <td id="files${account.id?c}"><span class="glabel">${account.totalNumberOfFiles}</span></td>
                        </tr>
                        <style>
                        .glabel {z-index:100;display:inline-block;position:absolute}
                        <@makeGraphCss "space" account account.availableSpaceInMb / account.totalSpaceInMb  />
                        <@makeGraphCss "files" account account.availableNumberOfFiles / account.totalNumberOfFiles />
                        </style>
                        </#items>

                    </tbody>
                </table>
            <#else>
                <em>You do not have any billing accounts.</em>
            </#list>
</#macro>

<#macro makeGraphCss prefix account percent>
    #${prefix}${account.id?c} {
        position: relative;
    }
    #${prefix}${account.id?c}:after {
        content:'\A';
        position:absolute;
        background:#ddd;
        top:0; bottom:0;
        left:0;
        z-index:0;
        width:${percent}%;
    }

</#macro>
<script>
    $(document).ready(function () {
        TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/api/cart/quote"/>");
        TDAR.autocomplete.applyPersonAutoComplete($(".userAutoComplete"), true, false);
    });
</script>

    </div>
    </div>


<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        TDAR.common.collectionTreeview();
        $("#myCarousel").carousel('cycle');
    });
</script>

</#escape>

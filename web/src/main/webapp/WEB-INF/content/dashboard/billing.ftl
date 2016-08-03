<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>${authenticatedUser.properName}'s Billing Accounts</h1>

</div>
    <div id="accountSection" class="row">
    <div class="span9">
        <div id="divAccountInfo">
                    <@common.billingAccountList accounts />
        
        </div>

    </div>
    <div class="span3">
    <h5>Add Space</h5>

    <@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action='process-choice'>
                <table class="table pTable">
                    <tbody>
                        <tr>
                            <th class="borderRight">
                                # of Files
                            </th>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfFiles" theme="simple" cssClass="integer span2 orderinfo" maxlength=9  />
                            </td>
                        </tr>
                        <tr>
                            <th class="borderRight">
                                Total File Size (MB)
                            </th>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfMb"  theme="simple" cssClass="integer span2 orderinfo" maxlength=9 />
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

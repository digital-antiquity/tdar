<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/content/cart/common-invoice.ftl" as invoicecommon >
<head>
    <title>Your Cart</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <style>
        #convert {
            margin-left: 10px;
        }

        #editFormActions {
            border: none;
            background: none;
        }
    </style>
</head>
    <#macro pricingOption label files storage cost id=label?lower_case>
    <div class="span2 well" id=div${id}>
        <h3><span class="red">$${cost}</span>: ${label}</h3>
        <ul>
            <li>${files}</li>
            <li>${storage}</li>
        </ul>

        <button type="button" class="tdar-button" id="${id}-option">SELECT</button>
    </div>
    </#macro>
<body>
    <@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action='process-choice'>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
        <h1>What would you like to put into tDAR?</h1>

        <h3>About Invoices and Accounts</h3>
        <p>
            In tDAR, billing accounts are used to manage resources. Each resource must be associated with an account. tDAR is run by Digital Antiquity, a
            not-for-profit organization dedicated to the preservation of archaeological information. The fees related to upload are used to ensure the proper
            preservation of materials uploaded to tDAR.
        </p>

        <#--<h3>Account Management</h3>-->
        <p>
            Accounts can be shared between users, and users can grant access to modify or manage resources to any tDAR user
            they choose.
        </p>


    <#assign showSuggested=!administrator && actionName != 'modify'/>

    <div class="row">
        <div class="span4" style="min-height:30em">
            <@rates />

        </div>
        <div class="span8">
            <ul class="nav nav-tabs">
                <li class="<#if showSuggested>active</#if>">
                    <a href="#suggested" data-toggle="tab">Suggested</a>
                </li>
                <li class="<#if !showSuggested>active</#if>"><a href="#custom" data-toggle="tab">Rate Calculator (Customize) &amp; All Rates</a></li>
            </ul>
            <div class="tab-content">
                <div id="suggested" class="tab-pane <#if showSuggested>active</#if> ">
                    <div class="row">
                        <div class="span8">
                            <h2>Suggested Levels</h2>

                            <div class="row">
                                <@pricingOption label="Small" files="1 File" storage="10 MB" cost=50 />
                                <@pricingOption label="Medium" files="10 Files" storage="100 MB" cost=400 />
                                <@pricingOption label="Large" files="100 Files" storage="1 GB" cost=2500 />
                            </div>
                        </div>
                    </div>
                </div>
                <div id="custom" class="tab-pane <#if !showSuggested>active</#if>">
                    <div class="row">
                        <div class="span8">
                            <h2>Cost Calculator</h2>

                            <div class="well">
                                <@s.textfield name="invoice.numberOfFiles" label="Number of Files" cssClass="integer span2" maxlength=9  />

                                <div class="control-group">
                                    <label class="control-label">Number of Mb</label>

                                    <div class="controls">
                                        <@s.textfield name="invoice.numberOfMb" label="Number of Mb"  theme="simple" cssClass="integer span2" maxlength=9 />
                                        <span id="convert"></span>
                                    </div>
                                </div>
                                <br/>

                                <div>
                                    <h4>Cost: $<span class="red" id="price">0.00</span></h4>
                                    <table class="table tableFormat">
                                        <tr>
                                            <th>Item</th>
                                            <th> # Files</th>
                                            <th>Space in MB</th>
                                            <th>Subtotal</th>
                                        </tr>
                                        <tbody id="estimated">
                                        <tr>
                                            <td colspan=5>enter number of files and mb above</td>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div> <!-- End of tab hell. You made it! -->

        </div>
    </div>


    <#if administrator || billingManager>
    <div class="divAdminLand admin-well">
            <@s.hidden name="accountId" value="${(accountId!-1)?c}" />

        <h3>Invoice Owner</h3>

        <div class="control-group">
            <label class="control-label">Invoice Owner</label>
            <div class="controls">
            <#--if no owner specified already, supply a 'blank' user -->
                    <@edit.registeredUserRow prefix="invoice.owner"
            person=((invoice.owner)!blankAuthorizedUser.user)
            _indexNumber=""
            includeRepeatRow=false/>

                <span class="help-block">
                    Use this field to create a <em>proxy invoice</em> on behalf of another user.
                </span>
            </div>
        </div>


        <#if (billingManager && allPaymentMethods?size > 1)>
            <h3>Payment Method</h3>
            <#--<@s.radio list="allPaymentMethods" name="invoice.paymentMethod" label="Payment Method"-->
            <#--listValue="label"    cssClass="transactionType fadeIfZeroed" value="CREDIT_CARD"  />-->
            <@invoicecommon.paymentMethod />
        </#if>

        <#--<@s.textarea name="invoice.otherReason" cols="" rows="" id="txtOtherReason" cssClass="span5"  label="Additional Information" />-->

        <#--<@s.hidden name="id" value="${invoice.id?c!-1}" />-->

        <h3>Nelnet: Extra Parameters</h3>
        <div class="alert alert-warning">
            This lets us send arbitrary parameters to Nelnet, our payment processor.  Please skip this section
                if the previous sentence sounded like gibberish.
        </div>
        <table class="table table-bordered table-compact">
            <thead>
            <tr>
                <th>Item</th>
                <th>Quantity</th>
            </tr>
            </thead>
            <tbody>
            <#list activities as act>
                <#if !act.production >
                    <tr>
                        <td>${act.name} <@s.hidden name="extraItemIds[${act_index}]" value="${act.id?c}"/> </td>
                        <td><@s.textfield name="extraItemQuantities[${act_index}]" cssClass="integer span2" theme="simple"/></td>
                    </tr>
                </#if>
            </#list>
            </tbody>
        </table>

    </div>
    </#if>

    <#if invoice??>
    <div class="well">
        <h3>Current Invoice</h3>
        <@invoicecommon.printSubtotal invoice />
    </div>
    </#if>

    <div class="row">
        <div class="span12">
            <@s.textfield name="code" label="Redeem Code" />
            <input type="submit" class="btn btn-mini submitButton tdar-button" name="submitAction"
                   value="Next: Review & Choose Payment Method">

        </div>
    </div>

    </@s.form>

<script>
    $(document).ready(function () {
// FIXME: removed because of IE8 validation error
//    TDAR.common.initEditPage($('#MetadataForm')[0]);
        TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/cart/api"/>");
        TDAR.autocomplete.applyPersonAutoComplete($(".userAutoComplete"), true, false);
    });
</script>

    <@edit.personAutocompleteTemplate />

</body>

    <#macro rates>
    <div class="tdar-rates" >
        <h2>Rates</h2>
        <table class="tableFormat table">
            <tr>
                <th>Item/Service</th>
                <th>Cost</th>
            </tr>
            <#list activities as act>
            <tr>
                <td>${act.name}</td>
                <td>${act.price} ${act.currency!"USD"}</td>
            </tr>
            </#list>
        </table>
        <p><em>* All files come with 10 MB of space</em></p>
    </div>
    </#macro>

</#escape>

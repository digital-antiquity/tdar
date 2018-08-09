<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/content/cart/common-invoice.ftl" as invoicecommon >
<head>
    <title>tDAR Pricing</title>
    <meta name="description" content="Migrate your papers and files to a discoverable, accessible digital archive.">
    <style>
.price {padding-top:.2em; display:inline-block;}
.small {font-size: 85%;}
.xsmall {font-size: 75%;}

.rateTable {margin-bottom: 0px;}
.borderRight {border-right:1px solid #DDD;}
.pTable input  {margin:0px;}
.topspace {margin-top:2em;}
.pTable th,.table.pTable thead { background-color:#EFEFEF} 
.table.pTable thead { font-size:120%;} 
span.sub {font-size:60%;color:#6e6e6e !important}
.badTable td.highlight { border-left:1px solid black !Important;border-right:1px solid black;border-bottom:1px solid black;background:#eee; }
.pTable th.normal {background-color:#fff}
.pTable th {text-align:center !important}
.badTable th.highlighttop {border-left:1px solid black;border-right:1px solid black; border-top:1px solid black !important; background:#7a1501 !important; }
.badTable th.highlight { border-left:1px solid black;border-right:1px solid black;background:#7a1501; border-top:none !important;color:#eee !Important}

.pTable .custom { border-right:#eee; border-left:#eee; }

.pTable .customtop { border-left:1px solid #333; background:#fff; }
.pTable .clean {border:none !important; background-color:#fff;}
.pTable .custom {background:#FAFAFA; }
td img {display:none; height:0px;}
.subhead {font-size:80%;border:0px !important;}
.media-body p {margin-top:0px !important}
img.media-object {opacity:.8;padding-top:.4em;}
.how-works {color:#555; }
table { border:1px solid #DDD;}
table td {font-family: "ff-tisa-web-pro",  serif !important;}
    </style>
</head>
<body>
    <@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action='process-choice'>
        <@s.token name='struts.csrf.token' />
        <@edit.hiddenStartTime />
        <@common.jsErrorLog />

<h1>Pricing</h1>


        <div class="row">
            <div class="col-5">
                <h3>
                    Digital Preservation Fees
                </h3>
                <table class="table rateTable small">
                    <thead>
                        <tr style="background-color:#EEE">
                            <th>
                                Digital Preservation
                            </th>
                            <th>
                                Rate
                            </th>
                        </tr>
                        <tr>
                            <td>
                                1-99 Files
                            </td>
                            <td>
                                $10 / File*
                            </td>
                        </tr>
                        <tr>
                            <td>
                                100+ Files
                            </td>
                            <td>
                                $5 / File
                            </td>
                        </tr>
                    </thead>
                </table>
                    <span class="xsmall">* Each tDAR file comes with 10MB of space. To upload digital resources larger than 10MB, simply purchase additional tDAR files.
                    </span>
                </ul>
            </div>




<!--                                        <tbody id="estimated">
                                            <td colspan=5>enter number of files and mb above</td> -->

            <div class="col-7">
                <h3>
                    Digital Preservation Fee Calculator
                </h3>
                <table class="table pTable">
                    <tbody>
                        <tr>
                            <th class="borderRight">
                                # of Files
                            </th>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfFiles" theme="simple" cssClass="integer col-2 orderinfo" maxlength=9  />
                            </td>
                        </tr>
                        <tr>
                            <th class="borderRight">
                                Total File Size (MB)
                            </th>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfMb"  theme="simple" cssClass="integer col-2 orderinfo" maxlength=9 />
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
            </div>
        </div>

    <div class="row">
        <div class="col-12">
<h4>Redeem a Coupon Code</h4>
<div class="card">
  <div class="card-body">
<div class="input-append">


            <@s.textfield type="text" id="couponCode" name="code" label="Redeem Code" class="input-xxlarge orderinfo" />
                <input type="submit" class="btn" name="submitAction" value="Redeem">
    </div>
    </div>
</div>


        </div>
    </div>

<!--
    <div class="row">
        <div class="col-12">
            <h3>Choose a Package</h3>
            <table class="table pTable">
            <thead>
             <tr>
            <th>Package</th>
              <th>
                  <div class="center"><b>
                Small
            </b></div></th>
              <th>
                  <div class="center"><b>
                Medium
            </b></div></th>
              <th>
                  <div class="center"><b>
                Large
            </b></div></th>
              </tr>
            </thead>
            <tbody>
                <tr>
                    <th class="borderRight">Files</th>
                    <td style="text-align:center">10</td>
                    <td style="text-align:center">100</td>
                    <td style="text-align:center">500</td>
                </tr>

                <tr>
                    <th class="borderRight">Space</th>
                    <td style="text-align:center">100 MB</td>
                    <td style="text-align:center">1 GB</td>
                    <td style="text-align:center">5 GB</td>
                </tr>

                <tr>
                    <th  class="borderRight">Cost</th>
                    <td style="text-align:center"><span class="price">$100</span></td>
                    <td style="text-align:center"><span class="price">$500</span></td>
                    <td style="text-align:center"><span class="price">$2,500</span></td>
                </tr>

            <tr>
                <th class="borderRight"></th>
                <td><div class="center">
            <button class="btn btn-sm  tdar-button" type="button">select</button></div></td>
                <td><div class="center">
            <button class="btn btn-sm  tdar-button" type="button">select</button></div></td>
                <td><div class="center">
            <button class="btn btn-sm  tdar-button" type="button">select</button>
            </div></td>
            </tr>
            </tbody>
            </table>
                                    <@invoicecommon.pricingOption label="Small" files="1 File" storage="10 MB" cost=50 />
                                <@invoicecommon.pricingOption label="Medium" files="10 Files" storage="100 MB" cost=400 />
                                <@invoicecommon.pricingOption label="Large" files="100 Files" storage="1 GB" cost=2500 />


    
        </div>
    </div>
-->

<br/>
        <div class='row'>
            <div class="col-6">
                <h3>
                    Service Fees
                </h3>
                <table class="table rateTable small">
                    <tr style="background-color:#EEE">
                        <th>
                            Curation Services
                        </th>
                        <th width="35%">
                            Rate
                        </th>
                    </tr>
                    <tr>
                        <td>
                            File Checking, Metadata Drafting, &amp; Basic Quality Control
                        </td>
                        <td>
                            $90 / Hour
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Consulting, Programming, &amp; Project Management
                        </td>
                        <td>
                            $180 / Hour
                        </td>
                    </tr>
                </table>
            </div>
            <div class='col-6'>
                <h3>
                    Additional Services
                </h3>
                <p>
                    Our full service digital curation covers everything necessary to migrate your paper library or born-digital files into a discoverable, accessible (with you in control of who gets access), searchable, and preserved digital archive in tDAR. <a href="mailto:info@digitalantiquity.org">Contact us for a quote</a>.
                </p>
                <p>
                    Service packages coming soon.
                </p>
            </div>
        </div>
        <div class="row">
            <div class="col-12">
                <h3>
                    About
                </h3>
                <p>
                    Digital Antiquity is a not-for-profit center at Arizona State University. Digital preservation fees fund the long-term digital preservation of your files in tDAR; access to your public data and protection of confidential data; data security; customer service; and, advocacy and professional training for proper digital curation of all archaeological materials.
                </p>
                <p>
                    tDAR is a domain repository for archaeology. It preserves data and information about and from archaeological resources, investigations, and related topics. In tDAR data and information are curated, discoverable, accessible, and preserved for future use.&nbsp; tDAR is developed and maintained by the Center for Digital Antiquity.
                </p>
            </div>
        </div>
<br/>


        <div class="row">
            <div class="col-12 well how-works">
                <h4>
                    Learn How tDAR Works
                </h4>
                <div class="row">
                    <div class="col">
                        <div class="media">
                            <a class="align-self-start mr-3" href="#"><img class="media-object" src="<@s.url value="/images/r4/icn-edit.png"/>" width="60" alt="edit" title="edit"></a>
                            <div class="media-body">
                                <h5 class="media-heading">
                                    Create &amp; Edit Metadata
                                </h5>
                                <p>
                                    Easy to use forms to add authors, keywords, location and other archaeological information.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="col">
                        <div class="media">
                            <a class="align-self-start mr-3" href="#"><img class="media-object" src="<@s.url value="/images/r4/icn-institution.png"/>" width="60" alt="institution" title="institution"></a>
                            <div class="media-body">
                                <h5 class="media-heading">
                                    Agency &amp; CRM Ready
                                </h5>
                                <p>
                                    tDAR includes support for agencies and CRM organizations to manage their archaeological and cultural resource information.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row topspace">
                    <div class="col">
                        <div class="media">
                            <a class="align-self-start mr-3" href="#"><img class="media-object" src="<@s.url value="/images/r4/icn-cloud.png"/>" width="60" alt="upload" title="upload"></a>
                            <div class="media-body">
                                <h5 class="media-heading">
                                    Easy Upload
                                </h5>
                                <p>
                                    Drag and drop interface for uploading and managing files.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="col">
                        <div class="media">
                            <a class="align-self-start mr-3" href="#"><img class="media-object" src="<@s.url value="/images/r4/icn-share.png"/>" width="60" alt="share" title="share"></a>
                            <div class="media-body">
                                <h5 class="media-heading">
                                    Share &amp; Cite
                                </h5>
                                <p>
                                    All resources in tDAR with files get a DOI for easy citation. Resources are indexed by Google and other major search engines.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row topspace">
                    <div class="col">
                        <div class="media">
                            <a class="align-self-start mr-3" href="#"><img class="media-object" src="<@s.url value="/images/r4/icn-lock.png"/>" width="60" alt="secure" title="secure"></a>
                            <div class="media-body">
                                <h5 class="media-heading">
                                    Confidential &amp; Secure
                                </h5>
                                <p>
                                    You maintain control over who has access to your materials. Files can be marked as embargoed for a short period, or confidential, allowing you to decide who has access to them.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="col">
                        <div class="media">
                            <a class="align-self-start mr-3" href="#"><img class="media-object" src="<@s.url value="/images/r4/icn-safe.png"/>" width="60" alt="preserve" title="preserve"></a>
                            <div class="media-body">
                                <h5 class="media-heading">
                                    Preserve
                                </h5>
                                <p>
                                    All files in tDAR are constantly checked to ensure that they are valid. Files are migrated to preservation formats automatically to ensure long-term access.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
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
                    <@edit.registeredUserRow prefix="invoice.owner" person=((invoice.owner)!blankAuthorizedUser.user)
                        _indexNumber="" includeRepeatRow=false/>

                <span class="help-block">
                    Use this field to create a <em>proxy invoice</em> on behalf of another user.
                </span>
            </div>
        </div>


        <#if (billingManager && allPaymentMethods?size > 1)>
            <h3>Payment Method</h3>
            <@invoicecommon.paymentMethod />
        </#if>

        <#--<@s.textarea name="invoice.otherReason" cols="" rows="" id="txtOtherReason" cssClass="col-5"  label="Additional Information" />-->

        <#--<@s.hidden name="id" value="${invoice.id?c!-1}" />-->

        <h3>Admin: Extra Parameters</h3>
        <div class="alert alert-warning">
            This lets us send arbitrary parameters.  Please skip this section if the previous sentence sounded like gibberish.
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
                        <td><@s.textfield name="extraItemQuantities[${act_index}]" cssClass="integer col-2 orderinfo" theme="simple"/></td>
                    </tr>
                </#if>
            </#list>
            </tbody>
        </table>
        <button class="button btn btn-primary tdar-button">Continue</button>

    </div>
    </#if>

    <#if invoice??>
    <div class="well">
        <h3>Current Invoice</h3>
        <@invoicecommon.printSubtotal invoice />
    </div>
    </#if>


    </@s.form>

<script>
    $(document).ready(function () {
        TDAR.pricing.initPricing($('#MetadataForm')[0], "<@s.url value="/api/cart/quote"/>");
        TDAR.autocomplete.applyPersonAutoComplete($(".userAutoComplete"), true, false);
    });
</script>


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

<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/settings.ftl" as settings>
    <#import "common-dashboard.ftl" as dash />

<head>
    <title>Billing Accounts</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    
</head>

<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">My Billing Accounts</span></h1>

</div>


<div class="row">
    <div class="col-2">
        <@dash.sidebar current="billing" />
    </div>
    <div class="col-10">
        <@accountSection />
        <br/>
      <h3>Add Space</h3>
      <p>Purchase addtional files or space.</p>
    <@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action='/cart/process-choice'>
                <table class="table pTable">
                    <tbody>
                        <tr>
                            <th class="borderRight">Files</th>
                            <th class="borderRight">Space (MB)</th>
                        </tr>
                        <tr>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfFiles" cssClass="integer orderinfo" maxlength=9  />
                            </td>
                            <td class="custom" style="text-align:center">
                                <@s.textfield name="invoice.numberOfMb"  cssClass="integer  orderinfo" maxlength=9 />
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
                            <td><a href="${account.detailUrl}">${account.name}</a>
                            <#local pspace =0>
                            <#if (account.totalSpaceInMb > 0)>
	                            <#local pspace= (account.totalSpaceInMb - account.availableSpaceInMb) / account.totalSpaceInMb * 100.0 >
                            </#if>
                            <#local pfiles= 0>
                            <#if (account.totalNumberOfFiles > 0)>
	                            <#local pfiles= (account.totalNumberOfFiles - account.availableNumberOfFiles) / account.totalNumberOfFiles * 100.0 >
                            </#if>
                            <#local perc = pfiles />
                            <#if (pspace > pfiles) >
                            	<#local perc = pfiles>
                        	</#if>
                        	<#local chartid>chart${account.id?c}</#local>
                        	<div id="${chartid}" data-val="${perc?string["0.##"]}" data-width=100 data-height=50 data-overridecolors=true class="gaugeChart pull-right" ></div>
                            </td>
                            <td>${account.invoices?size}</td>
                            <td nowrap id="space${account.id?c}"><span class="glabel">${account.totalSpaceInMb}</span></td>
                            <td nowrap id="files${account.id?c}"><span class="glabel">${account.totalNumberOfFiles}</span>
                            

                            </td>

                        </tr>
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
        <#if (percent > 100)>
    #${prefix}${account.id?c} .glabel {
        color:white !important;
    }
		</#if>
    #${prefix}${account.id?c}:after {
        content:'\A';
        position:absolute;
        <#if (percent > 100)>
    		background-color:#7a1501;
        <#else>
	        background-color:#ddd;
        </#if>
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

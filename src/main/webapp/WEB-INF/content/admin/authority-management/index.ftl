<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
    <title>Authority Management - Merge Duplicates</title>
    <script type='text/javascript' src='<@s.url value="/includes/authority-management.js"/>'></script>
    <script type='text/javascript' src='<@s.url value="/includes/datatable-support.js"/>'></script>
    <script type='text/javascript' src='<@s.url value="/includes/jquery.watermark-3.1.3.min.js"/>'></script> 
    <script type='text/javascript'>
    $(initIndexPageJavascript);
    </script>
    
</head>
<body>
    <div id="errors">
    <@view.showControllerErrors />
    </div>



    <div class="glide">
        
        <@s.select  name="entityType" id="selEntityType" label="Select Type" labelposition="left" listValue='label' list="%{dedupeableTypes}" />
        
    </div>
    
    <div class="glide" id="divSearchControl">
        <form method="get" action="#" id="frm">
            <div id="divKeywordSearchControl" class="searchControl">
                <h3>Keyword Search</h3>
                <@s.textfield name="keyword" id="txtKeyword" cssClass="longfield" label="Keyword value" />
            </div>
            <div id="divInstitutionSearchControl" class="searchControl">
                <h3>Institution Search</h3>
                <@s.textfield name="institution" id="txtInstitution" cssClass="longfield" label="Institution" />
            </div>
            <div id="divPersonSearchControl" class="searchControl">
                <h3>Person Search</h3>
                <div class="width30percent marginLeft10" >
                    <@s.textfield cssClass="watermarked" id='txtFirstName' watermark="First Name"name="firstName" maxlength="255"  />
                    <@s.textfield cssClass="watermarked" id='txtLastName' watermark="Last Name" name="lastName" maxlength="255"  /> 
                    <@s.textfield cssClass="watermarked" id='txtEmail' watermark="Email" name="email" maxlength="255"/>
                    <br />
                </div>
                <div class="width99percent marginLeft10">
                    <@s.textfield cssClass="watermarked" id='txtInstitution' watermark="Institution Name" name="institution" maxlength="255"  />
                </div>
            </div>
        </form>
        
        <button id="btnSearch">Search</button>
    </div>
    
    
    <div class="glide" id="divIdentifyDupes">
          <table id='dupe_datatable'></table>
    </div>
    
    
    
    <div class="glide" id="divSelectAuthority"> 
        <h3>Selected Duplicates</h3>
        <@s.form id="frmDupes" method="post" action="select-authority">
            <p id="pDupeInfo" style="display:none">
                <span id="spanDupeCount">0</span>
                item(s) selected. <span class="button">Clear</span> 
                </p>
            <input type="hidden" name="entityType" id="hdnEntityType" />
            <@s.submit /> 
        </@s.form>
    </div>
    
</body>

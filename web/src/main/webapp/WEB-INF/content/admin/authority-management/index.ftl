<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
    <title>Authority Management - Merge Duplicates</title>
    <script type='text/javascript'>
        $(function () {
            TDAR.authority.initAuthTable();
            $("#txtInstitution, #txtFirstName, #txtLastName, #txtInstitution, #txtEmail, #txtKeyword").bindWithDelay("keyup", function () {
                        $("#dupe_datatable").dataTable().fnDraw();
                    }, 500);
        });
    </script>
    <style type="text/css">
        @-webkit-keyframes 'blink' {
            0% {
                background: rgba(255, 0, 0, 0);
            }
            50% {
                background: rgba(255, 0, 0, 00.5);
            }
            100% {
                background: rgba(255, 0, 0, 0);
            }
        }

        .dire-warning {
            color: red !important;
            -webkit-animation-direction: normal;
            -webkit-animation-duration: 0.5s;
            -webkit-animation-iteration-count: 5;
            -webkit-animation-name: blink;
            -webkit-animation-timing-function: ease;

        }
    </style>
</head>
<body>
<div class="glide">
    <h3 class="dire-warning">WARNING</H3>

    <p>Please note that act of de-duping entities is currently <em>irreversable</em> and should only be performed by curators and administrators only
        only after receiving approval to do so. </p>

    <p>If you feel you may have made a mistake and de-duped the wrong enitity (or entities) please notify a ${siteAcronym} administrator immediately.</p>
</div>


<div class="glide">
<@s.select  name="entityType" id="selEntityType" label="Select Type" labelposition="left" listValue='label' list="%{dedupeableTypes}" />
</div>

<div class="glide" id="divSearchControl">
    <form method="post" action="#" id="frm">
        <@s.token name='struts.csrf.token' />
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

            <div class="width30percent marginLeft10">
            <@s.textfield id='txtFirstName' placeholder="First Name"name="firstName" maxlength="255"  />
            <@s.textfield  id='txtLastName' placeholder="Last Name" name="lastName" maxlength="255"  />
            <@s.textfield  id='txtEmail' placeholder="Email" name="email" maxlength="255"/>
                <br/>
            </div>
            <div class="width99percent marginLeft10">
            <@s.textfield  id='txtInstitution' placeholder="Institution Name" name="institution" maxlength="255"  />
            </div>
        </div>
    </form>
</div>


<div class="glide" id="divIdentifyDupes">
    <table id='dupe_datatable' class="table tableFormat"></table>
</div>


<div class="glide" id="divSelectAuthority">
    <h3>Selected Duplicates</h3>
<@s.form id="frmDupes" method="post" action="select-authority">
    <@s.token name='struts.csrf.token' />
    <p id="pDupeInfo" style="display:none">
        <span id="spanDupeCount">0</span>
        item(s) selected. <span class="button">Clear</span>
    </p>
    <input type="hidden" name="entityType" id="hdnEntityType"/>
    <@s.submit cssClass="btn btn-primary" />
</@s.form>
</div>

</body>

<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/macros/common-auth.ftl" as auth>

<head>
    <title>${siteAcronym} Registration</title>
    <meta name="description" content="Registered users can download files and organize content on their personal dashboard">
</head>
<body>

<h1>${siteAcronym} Registration</h1>

<@s.form id="accountForm" method="post" action="/account/register"  
	cssClass="tdarvalidate"
    dynamicAttributes={
        "data-validate-method":"initRegForm",
        "data-validate-errorLabelContainer": "#error"
    } >
	<@s.token name='struts.csrf.token' />

    <input type="hidden" name="url" value="${Parameters.url!''}"/>
    <div class="well">
    <div class="pull-right">
        <b>Already Registered?</b><br/><a href="<@s.url value="/login" />">Login</a>
    </div>
    <p><b>There is no charge to become a registered user of ${siteAcronym}. As a registered user, you can:</b>
    <ul>
        <li>Download Documents, Data sets, Images, and Other Resources</li>
        <li>Bookmark Resources for future use</li>

    </ul>

    <h3>About You</h3>

     <@auth.registrationFormFields beanPrefix="registration" />

</div>

</@s.form>
<script type='text/javascript'>
    $(function () {
        TDAR.auth.initRegister(${registrationTimeout?c});
    });
</script>
</body>

<!--
vim:sts=2:sw=2:filetype=jsp
-->
<head>
    <title>Forgot your password?</title>
    <script type='text/javascript'>
        $(function () {
            $("#recoverForm").validate();
            $('#emailAddress').focus();
        });
    </script>
    <style type="text/css">
        label.error {
            display: block;
        }
    </style>

</head>
<body>
Enter the email address you used to register with ${siteAcronym} and we will send an email containing instructions on how
to reset your password. If you still encounter problems, <a href="<@s.url value='/contact'/>">contact us</a>.
<@s.form id="recoverForm" method="post" action="reminder">
  <@s.token name='struts.csrf.token' />
    <@s.textfield label='Email' size='35' id='emailAddress' label="Email" name="reminderEmail" cssClass="required email"/>
    <@s.submit value="Send password reset instructions"/>
</@s.form>
</body> 


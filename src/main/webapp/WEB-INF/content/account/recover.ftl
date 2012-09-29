<!--
vim:sts=2:sw=2:filetype=jsp
-->
<head>
<title>Forgot your password?</title>
<meta name="lastModifiedDate" content="$Date$"/>
<script type='text/javascript'>
  $(function() {
    $("#recoverForm").validate();
    $('#emailAddress').focus();
  });
</script>
<style type="text/css">
label.error {display:block;}
</style>

</head>
<body>
Enter the email address you used to register with tDAR and we will email a new
password to you.  If you still encounter problems, <a href="<@s.url value='/contact'/>">contact us</a>.
<@s.form id="recoverForm" method="post" action="reminder">
    <@s.textfield label='Email' size='35' id='emailAddress' label="Email" name="reminderEmail" cssClass="required email"/>
    <@s.submit value="Mail me a new password"/>
</@s.form>
</body> 


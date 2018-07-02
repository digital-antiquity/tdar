<head>
    <title>fake-payment-form</title>
</head>
<@s.form action="process-payment" method="post">
<@s.token name='struts.csrf.token' />
<h3>Fake CC Form -- enter fake cc num, must start with </h3>
    <@s.select name="ccnum" list={'4111 - visa':'4111', '5454 - master ':'5454','3782 - amex':'3782','6011 - discover':'6011'}  listKey="value" listValue="key"/>

    <#list params?keys as key>
        <#assign val = "${params.get(key)[0]}" />
        <@s.hidden name="${key}" value = "${val}" />
    </#list>
    <@s.submit />

</@s.form>


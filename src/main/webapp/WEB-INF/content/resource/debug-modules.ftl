<#import "/WEB-INF/macros/header.ftl" as header>
<#assign
    jquery_version="1.8.2",
    jquery_ui_version="1.8.23"
/>
<!DOCTYPE html>
<html>
<head>
    <title>testing modules</title>

    <script src="/includes/requirejs-2.1.6/require.js"></script>
    <script src="/includes/tdar.core.js"></script>
    <script src="${protocol}//ajax.googleapis.com/ajax/libs/jquery/${jquery_version}/jquery.min.js"></script>
    <script src="${protocol}//ajax.googleapis.com/ajax/libs/jqueryui/${jquery_ui_version}/jquery-ui.min.js"></script>
    <script src="${protocol}//ajax.aspnetcdn.com/ajax/jquery.validate/1.11.1/jquery.validate.js"></script>
    <script src="${protocol}//ajax.aspnetcdn.com/ajax/jquery.validate/1.11.1/additional-methods.js"></script>
    <script src="${protocol}//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.js"></script>


    <@header.scripts />
    <script>
        //normally,  this simple define/require pair should work fine,  but one or more of tdar's javascript files
        // and/or inline scripts is mucking up the module lookup system, which causes this function to throw a
        // "module not found" error.
        var testrjs = function() {
            define("foo", {a:1, b:2});
            require(["foo"], function (bar) {
               console.log("if you see this message, requirejs works: %s", bar.a);
            });
        };

        testrjs();
    </script>


</head>
<h1>well hello</h1>
<p></p>
</html>
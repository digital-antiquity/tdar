<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
    <responseDate><#assign aDateTime = .now>${aDateTime?iso_utc}</responseDate>
    <request>${request.requestURL}<#if request.queryString??>?${request.queryString?html}</#if></request>
    <error code="${errorCode.code}"><#if errorCode.message??>${errorCode.message?html}</#if></error>
</OAI-PMH>
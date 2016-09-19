<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:midas="http://www.heritage-standards.org/midas/schema/1.0"
                >
    <xsl:output method="html" indent="yes" version="4.0"/>
    <xsl:template match="/">
        <link rel="stylesheet" type="text/css" href="css/gatewayService.css"  />
        <xsl:variable name="vLowercaseChars_CONST" select="'abcdefghijklmnopqrstuvwxyz'"/>
        <xsl:variable name="vUppercaseChars_CONST" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
        <xsl:variable name="adslogo">http://ads.ahds.ac.uk/ADSTools/WebServices/GatewayService/images/logo_small.gif</xsl:variable>

        <xsl:if test="//Error">
            <p style="font-weight: 800; color: #990000">
                <xsl:value-of select="//Error"/>
            </p>
        </xsl:if>
        <xsl:variable name="type" select="SearchResults/Meta/Stylesheet"/>
        <xsl:variable name="totalRecords" select="SearchResults/Meta/TotalRecords"/>
        <xsl:variable name="returnedRecords" select="SearchResults/Meta/NumberOfRecordsReturned"/>

        <xsl:variable name="displayRecords">
            <xsl:choose>
                <xsl:when test="$totalRecords &lt; 5">
                    <xsl:value-of select="$totalRecords"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$returnedRecords"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="totalPage" select="ceiling($totalRecords div 10)"/>
        <xsl:variable name="resourceID" select="SearchResults/Meta/ResourceID"/>
        <xsl:variable name="urlsuffix">http://ads.ahds.ac.uk/catalogue/search/fr.cfm?
        </xsl:variable>
        <xsl:variable name="space">
            <xsl:value-of select=" ' ' " />
        </xsl:variable>
        <div class="ADS_GatewayService">

            <xsl:if test="$type='TopLevel'">
                <xsl:if test="count(//SearchResults/Results/Result) &gt; 0">
                    <p>Results: Listing top
                        <strong>
                            <xsl:value-of select="$displayRecords"/>
                        </strong> of
                        <strong>
                            <xsl:value-of select="$totalRecords"/>
                        </strong> records hold in our database relevant to this query.
                    </p>
                    <table>
                        <tr>
                            <th>
                                Title
                            </th>
                            <th>
                                Location
                            </th>
                        </tr>
                        <xsl:for-each select="SearchResults/Results/Result[position() &gt; 0 and position() &lt;= 5]">
                            <xsl:variable name="Description" select="midas:monuments/midas:monument/midas:description/midas:full"/>
                            <xsl:variable name="Title_id" select="midas:monuments/midas:monument/midas:appellation/midas:identifier[@type='title_id']"/>
                            <xsl:variable name="resourceid" select="//SearchResults/Meta/ResourceID" />
                            <xsl:variable name="importRCN" select="midas:monuments/midas:monument/midas:appellation/midas:identifier[@type='import_rcn']"/>
                            <xsl:variable name="Title">
                            <xsl:variable name="_Title" select="midas:monuments/midas:monument//midas:appellation/midas:name"/>
                                <xsl:choose>
                                    <xsl:when test="string-length($_Title) &gt; 60">
                                        <xsl:value-of select="substring($_Title,0,60)"/> ...
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$_Title"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:variable name="Location">
                            <xsl:variable name="_Location" select="midas:monuments/midas:monument/midas:characters/midas:character/midas:spatial/midas:place/midas:namedplace/midas:location[@type='Summary']"/>
                                <xsl:choose>
                                    <xsl:when test="string-length($_Location) &gt; 50">
                                        <xsl:value-of select="substring($_Location,0,50)"/> ...
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$_Location"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:if test="position() mod 2 =1">
                                <tr class="odd">
                                    <td class="adsTitle">
                                        <a href="{$urlsuffix}rcn={$importRCN}&amp;resourceID={$resourceID}">
                                            <xsl:value-of select="$Title"/>
                                        </a>
                                    </td>
                                    <td class="adsLocation">
                                        <xsl:if test="string-length($Location) &gt; 0 ">
                                            <xsl:value-of select="$Location"/>
                                        </xsl:if>
                                    </td>
                                </tr>
                            </xsl:if>
                            <xsl:if test="position() mod 2 =0">
                                <tr class="even">
                                    <td class="adsTitle">
                                        <a href="{$urlsuffix}rcn={$importRCN}&amp;resourceID={$resourceID}">
                                            <xsl:value-of select="$Title"/>
                                        </a>
                                    </td>
                                    <td class="adsLocation">
                                        <xsl:if test="string-length($Location) &gt; 0 ">
                                            <xsl:value-of select="$Location"/>
                                        </xsl:if>
                                    </td>
                                </tr>
                            </xsl:if>
                            
                        </xsl:for-each>

                    </table>

                    <p>

                    </p>


                </xsl:if>
            </xsl:if>
            <xsl:if test="$type='Application'">
                <xsl:for-each select="SearchResults/Results/Result[position() &gt; 0 and position() &lt;= 30]">
                    <xsl:variable name="Description" select="midas:monuments/midas:monument/midas:description/midas:full"/>
                    <xsl:variable name="Location" select="midas:monuments/midas:monument/midas:characters/midas:character/midas:spatial/midas:place/midas:namedplace/midas:location[@type='Summary']"/>
                    <xsl:variable name="Title_id" select="midas:monuments/midas:monument/midas:appellation/midas:identifier[@type='title_id']"/>
                    <xsl:variable name="resourceid" select="//SearchResults/Meta/ResourceID" />
                    <xsl:variable name="imgsource" select="//midas:monuments/midas:monument/midas:description/@source"/>
                    <xsl:variable name="imgurl" select="concat('http://ads.ahds.ac.uk/catalogue/collections/hit_count_icons/', $resourceid, '_browse.gif')" />
                    <div style="position:relative; background:#f0f0f0; padding:5px; margin:0px 10px 0px 10px;">
                        <a href="{$urlsuffix}rcn={$Title_id}&amp;resourceID={$resourceID}">
                            <xsl:value-of select="midas:monuments/midas:monument//midas:appellation/midas:name"/>
                        </a>
                        <br />
                        <img style="position:relative; float:left; vertical-align:text-bottom; padding-right: 5px;" src="{$imgurl}" alt="{$imgsource}" />
                        <xsl:if test="string-length($Description) &gt; 200">
                            <xsl:value-of select="substring($Description,0,200)"/>...
                        </xsl:if>
                        <xsl:if test="string-length($Description) &lt;= 201 ">
                            <xsl:value-of select="$Description" />
                        </xsl:if>
                        <xsl:if test="string-length($Description) = 0 ">
                            No Description
                        </xsl:if>
                        <br />
                        <xsl:if test="string-length($Location) &gt; 0 ">
                            <xsl:value-of select="$Location"/>
                            <br />
                        </xsl:if>
                        <span style="color:#666666; font-size:0.9em">
                            <xsl:value-of select="$imgsource"/>
                        </span>
                        <br />
                        <br />
                    </div>
                    <br />
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="$type='SingleResult'">
                <xsl:variable name="resourceid" select="//SearchResults/Meta/ResourceID" />
                <xsl:variable name="imgsource" select="//midas:monuments/midas:monument/midas:description/@source"/>
                <xsl:variable name="imgurl" select="normalize-space(concat('http://ads.ahds.ac.uk/ADSTools/WebServices/GatewayService/images/resources/', $resourceid, '_logo.jpg'))" />
                <xsl:variable name="importRCN" select="normalize-space(//midas:monuments/midas:monument/midas:appellation/midas:identifier[@type='Import RCN'])"/>
                <table>
                    <tr>
                        <th colspan="2">
                            <div class="resourceimage">
                                <img src="{$imgurl}" title="Copyright:&#xA0;  {$imgsource}" alt="Copyright:&#xA0;  {$imgsource}" />
                                <br />
                            </div>
                            <div class="adslogo">
                                <img src="{normalize-space($adslogo)}" title="Data provided by Archaeology Data Service" alt="Data provided by Archaeology Data Service" />
                            </div>
                            <br />
                            <br />
                            <p class="title">
                                <xsl:value-of select="//midas:monuments/midas:monument/midas:appellation/midas:name"/>
                            </p>
                            <p>
                                <a target="_new" title="View this record on the Archaeology Data Service web site" href="http://ads.ahds.ac.uk/catalogue/search/fr.cfm?rcn={$importRCN}">
                                    View this record on the Archaeology Data Service web site
                                </a>
                            </p>
                        </th>
                    </tr>
                </table>
                <br />
                <table class="single" cellspacing="0" cellpadding="3" border="0" width="100%">
                    <xsl:if test="count(//midas:monuments/midas:monument/midas:description/midas:full) &gt; 0 ">
                        <tr>
                            <th>Description:</th>
                            <td>
                                <xsl:value-of select="//midas:monuments/midas:monument/midas:description/midas:full"/>
                            </td>
                        </tr>
                    </xsl:if>
                    <tr>
                        <th><!-- -->
                        </th>
                        <td><!-- -->
                        </td>
                    </tr>
                    <tr>
                        <th><!-- -->
                        </th>
                        <td><!-- -->
                        </td>
                    </tr>
                    <xsl:for-each select="//midas:character/midas:spatial/midas:place/midas:namedplace/midas:location[@type='Locality']">
                        <tr>
                            <th>Locality:</th>
                            <td>
                                <xsl:value-of select="."/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <xsl:for-each select="//midas:character/midas:spatial/midas:place/midas:namedplace/midas:location[@type='Named Location']">
                        <tr>
                            <th>Named Location:</th>
                            <td>
                                <xsl:value-of select="."/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <xsl:for-each select="//midas:character/midas:spatial/midas:place/midas:namedplace/midas:location[@type='Civil Parish']">
                        <tr>
                            <th>Parish:</th>
                            <td>
                                <xsl:value-of select="."/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <xsl:for-each select="//midas:character/midas:spatial/midas:place/midas:namedplace/midas:location[@type='District']">
                        <tr>
                            <th>District:</th>
                            <td>
                                <xsl:value-of select="."/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <xsl:for-each select="//midas:character/midas:spatial/midas:place/midas:namedplace/midas:location[@type='Admin County']">
                        <tr>
                            <th>County:</th>
                            <td>
                                <xsl:value-of select="."/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <xsl:for-each select="//midas:character/midas:spatial/midas:place/midas:gridref">
                        <tr>
                            <th>Grid reference:</th>
                            <td>
                                <xsl:value-of select="."/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <xsl:for-each select="//midas:character/midas:spatial/midas:geometry">
                        <xsl:variable name="method" select="midas:spatialappellation/midas:capturemethod" />
                        <tr>
                            <xsl:choose>
                                <xsl:when test="position() =1">
                                    <th>
                                        <xsl:value-of select="$method"/>:
                                    </th>
                                </xsl:when>
                                <xsl:otherwise>
                                    <th></th>
                                </xsl:otherwise>
                            </xsl:choose>
                            <td>[
                                <xsl:value-of select="midas:spatialappellation/midas:quickpoint/midas:srs"/>]
                                <xsl:value-of select="midas:spatialappellation/midas:quickpoint/midas:x"/>,
                                <xsl:value-of select="midas:spatialappellation/midas:quickpoint/midas:y"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <tr>
                        <th><!-- -->
                        </th>
                        <td><!-- -->
                        </td>
                    </tr>
                    <tr>
                        <th><!-- -->
                        </th>
                        <td><!-- -->
                        </td>
                    </tr>
                    <xsl:choose>
                        <xsl:when test="count(//midas:characters/midas:character/midas:type/midas:monumenttype) = count(//midas:characters/midas:character/midas:type/midas:temporal)">
                            <xsl:for-each select="//midas:characters/midas:character/midas:type">
                                <tr>
                                    <xsl:choose>
                                        <xsl:when test="position() =1">
                                            <th>Period/Subjects:</th>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <th></th>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <td>
                                        <xsl:value-of select="translate(midas:temporal/midas:span/midas:display/midas:appellation, $vLowercaseChars_CONST , $vUppercaseChars_CONST)"/> -
                                        <xsl:value-of select="midas:monumenttype"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:if test="count(//midas:characters/midas:character/midas:type) &gt; 0 ">
                                <xsl:for-each select="//midas:characters/midas:character/midas:type">
                                    <tr>
                                        <xsl:choose>
                                            <xsl:when test="position() =1">
                                                <th>Periods:</th>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <th></th>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <td>
                                            <xsl:value-of select="translate(midas:temporal/midas:span/midas:display/midas:appellation, $vLowercaseChars_CONST , $vUppercaseChars_CONST)"/>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:if test="count(//midas:characters/midas:character/midas:type) &gt; 0 ">
                                <xsl:for-each select="//midas:characters/midas:character/midas:type">
                                    <tr>
                                        <xsl:choose>
                                            <xsl:when test="position() =1">
                                                <th>Subjects:</th>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <th></th>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <td>
                                            <xsl:value-of select="translate(midas:monumenttype, $vLowercaseChars_CONST , $vUppercaseChars_CONST)"/>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                    <tr>
                        <th><!-- -->
                        </th>
                        <td><!-- -->
                        </td>
                    </tr>
                    <tr>
                        <th><!-- -->
                        </th>
                        <td><!-- -->
                        </td>
                    </tr>
                    <xsl:for-each select="//midas:monuments/midas:monument/midas:appellation/midas:identifier">
                        <xsl:if test="not(./@type = 'Import RCN')">
                            <tr>
                                <xsl:choose>
                                    <xsl:when test="position() =1">
                                        <th>Identifiers:</th>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <th></th>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <td>[
                                    <xsl:value-of select="./@namespace"/>]
                                    <xsl:value-of select="./@type"/> -
                                    <xsl:value-of select="."/>
                                </td>
                            </tr>
                        </xsl:if>
                    </xsl:for-each>
                </table>
                <xsl:if test="count(//midas:actor) &gt; 0">
                    <hr />
                    <p class="subtitle">People Involved:
                    </p>
                    <table class="actors">
                        <tr>
                            <td>
                                <ul>
                                    <xsl:for-each select="//midas:actor">
                                        <li>[
                                            <xsl:value-of select="midas:role"/>]
                                            <xsl:value-of select="midas:appellation/midas:name"/>
                                        </li>
                                    </xsl:for-each>
                                </ul>
                            </td>
                        </tr>
                    </table>
                </xsl:if>
                <xsl:if test="count(//midas:references/midas:reference) &gt; 0">
                    <hr />
                    <p class="subtitle">Bibliographic References:
                    </p>
                    <table class="references">
                        <tr>
                            <td>
                                <ul>
                                    <xsl:for-each select="//midas:references/midas:reference">
                                        <li>
                                            <xsl:value-of select="midas:description/midas:full"/>
                                        </li>
                                    </xsl:for-each>
                                </ul>
                            </td>
                        </tr>
                    </table>
                </xsl:if>
            </xsl:if>
        </div>
    </xsl:template>
</xsl:stylesheet> 


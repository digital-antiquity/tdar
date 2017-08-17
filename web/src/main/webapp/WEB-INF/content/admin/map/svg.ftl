<#import "/${config.themeDir}/settings.ftl" as settings>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
     viewBox="${(svgWrapper.minX)?c} ${(svgWrapper.minY)?c} ${(svgWrapper.width)?c} ${(svgWrapper.height)?c}"
     width="100%" height="100%"
     preserveAspectRatio="xMidYMid meet">
    <rect x="${(svgWrapper.minX)?c}" y="${(svgWrapper.minY)?c}" width="${(svgWrapper.width)?c}" height="${(svgWrapper.height)?c}" fill="#9cbdd3" stroke="black"
          stroke-width=".02"/>

<#list svgWrapper.sqlXml as xml>
${xml.string}
</#list>


    <style type="text/css">
        <! [CDATA[
      path {
            fill: #ddd;
            stroke: #111;
        }
        <#list cssValues as values>
            <#compress><#list values as value>
                <#if value_index != 0>,</#if>  ${value} path<#t>
            </#list></#compress>{ fill: #${settings.mapColors[values_index]}; fill-opacity: 0.9 }
        </#list>
        a path:hover {
            fill-opacity: 1.0;
            stroke-width: .1
        }

        ]
        ]
        >
    </style>
<#assign boxSize=4/>
<#assign y = svgWrapper.minY + svgWrapper.height -4 - boxSize>
    <rect x="${(svgWrapper.minX + svgWrapper.width - boxSize*(11.5) - 28)?c }" y="${(y-2)?c}" width="${boxSize*(11.2) + 28}" height="${boxSize+4}" fill="#fff"
          stroke="black" stroke-width=".1"/>

    <text x="${(svgWrapper.minX + svgWrapper.width - boxSize*(11.2) - 28)?c }" y="${(y+boxSize - 0.2 )?c}" font-size="6">0</text>
<#list 0..10 as i>
    <rect x="${(svgWrapper.minX + svgWrapper.width - boxSize*(10-i) - 28)?c }" y="${y?c}" width="${boxSize}" height="${boxSize}"
          fill="#${settings.mapColors[i]}" stroke="black" stroke-width=".1"/>
</#list>
    <text x="${(svgWrapper.minX + svgWrapper.width - 22)?c }" y="${(y+boxSize - 0.2 )?c}" font-size="6">360,000</text>

</svg>
<#import "email-macro.ftl" as mail /> 

<@mail.content>
     <table>
        <tr>
        <td>
        <p style="float:left;">
            Dear ${user.firstName},<br />
            We're so glad that you've entrusted your archaeological to tDAR. 
            We'd love to give you an update on what's new, and what's been most popular.<br />
            <br />
    
            <p><b>tDAR update 10/31/2105:</b> This release focuses on three major areas of the 
            repository: (1) visualization, (2) maps and spatial data, and (3) modularization 
            and infrastructure work. 
            In addition, the development team has improved performance and 
            reliability as well as making a series of smaller enhancements. [read more]</p>
        </p>
        </td>
        <td>
        <p style="float:right;width:300px;">
            <img src="cid:resources.png" />
        </p>
       </td>
       </tr>  
    </table>
    
    <div>
        <span style="font-weight:bold;font-size:14px;text-decoration:underline">
            Your Most Popular Resources
        </span>
        <ul>
        <#list resources as resource>
            <li> ${resource.title}</li>
        </#list>
        </ul>
     </div>
        
    <img src="cid:totalviews.png" />   
    <img src="cid:totaldownloads.png" />   
    
    
    <div>
        <span style="font-weight:bold;font-size:14px;text-decoration:underline">
            Your Account Balance:
        </span>
        You currently have space for ${availableFiles} files or up to ${availableSpace} MB of space available in tDAR.  
        <a href="https://www.tdar.org">Check your balance now</a> or, 
        <a href="https://www.tdar.org">upload something now</a>.
    </div>        
</@mail.content>
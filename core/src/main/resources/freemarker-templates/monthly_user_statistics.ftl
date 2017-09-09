<#import "email-macro.ftl" as mail /> 

<@mail.content>
     
     <div>
        Dear {user.firstName},
        We're so glad that you've entrusted your archaeological to tDAR. 
        We'd love to give you an update on what's new, and what's been most popular.
        
        

        tDAR update 10/31/2105: This release focuses on three major areas of the 
        repository: (1) visualization, (2) maps and spatial data, and (3) modularization 
        and infrastructure work. 
        In addition, the development team has improved performance and 
        reliability as well as making a series of smaller enhancements. [read more]
        
        <img src="cid:resources" />
     </div>   
    
    
    <div>
    
        <span style="font-weight:bold;font-size:14px;text-decoration:underline">
            Your Most Popular Resources
        </span>
        <ul>
        <#list resources as resource>
            <li> resource.title</li>
        </#list>
        </ul>
        
     </div>
        
    <img src="cid:totalviews" />   
    <img src="cid:totaldownloads" />   
 
</@mail.content>
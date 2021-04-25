<div>
    <table style="border: 1px solid #ccc; border-left:0; border-collapse: collapse; width: 100%;">
        <thead>
            <tr>
                <th class="name" width="25%" style="border-left: 1px solid #ccc; padding:10px; position: relative;"></th>
                <th class="status" width="10%" style="border-left: 1px solid #ccc; padding:10px; position: relative;">@@console.governance.status@@</th>
                <th class="details" width="50%" style="border-left: 1px solid #ccc; padding:10px; position: relative;">@@console.governance.details@@</th>
            </tr>    
        </thead>
        <tbody>
            <#list checker?keys as c>
                <tr class="category">
                    <th colspan="3" style="text-align: left; background: #ecf0f1; border-top: 1px solid #ccc; border-bottom: 1px solid #ccc; border-left: 1px solid #ccc; padding:10px; position: relative;">@@console.governance.category@@<span class="category_label">${c}</span></th>
                </tr> 
                <#assign count = 0>
                <#list checker[c] as element>
                    <#if element.properties.deactivated! != 'true'>
                        <tr <#if count % 2 == 1>style="background: #f8f8f8;"</#if>>
                            <#if lastResult[element.className]??>
                                <#assign result = lastResult[element.className]>
                                <td class="name" style="border-left: 1px solid #ccc; padding:10px; position: relative; font-weigth:bold;">
                                    ${element.i18nLabel!}
                                </td>
                                <td class="status" style="border-left: 1px solid #ccc; padding:10px; position: relative; text-align: center; font-weight: bold;">
                                    <#if result.score??>
                                        <#if result.status.toString() == "PASS">
                                            <span style="font-weight: bold; color: #28a745;">${result.score!}</span>
                                        <#elseif  result.status.toString() == "INFO">   
                                            <span style="font-weight: bold; color: #007bff;">${result.score!}</span>    
                                        <#elseif  result.status.toString() == "WARN">   
                                            <span style="font-weight: bold; color: #ffc107;">${result.score!}</span>
                                        <#else>
                                            <span style="font-weight: bold; color: #dc3545;">${result.score!}</span>
                                        </#if>
                                    <#else>
                                        <#if result.status.toString() == "PASS">
                                            <span style="font-weight: bold; color: #28a745;">@@console.governance.pass@@</span>
                                        <#elseif  result.status.toString() == "INFO">   
                                            <span style="font-weight: bold; color: #007bff;">@@console.governance.info@@</span>    
                                        <#elseif  result.status.toString() == "WARN">   
                                            <span style="font-weight: bold; color: #ffc107;">@@console.governance.warn@@</span>
                                        <#else>
                                            <span style="font-weight: bold; color: #dc3545;">@@console.governance.fail@@</span>
                                        </#if>
                                    </#if>
                                </td>
                                <td class="details" style="border-left: 1px solid #ccc; padding:10px; position: relative;">
                                    <#if result.details??>
                                        <ul>
                                            <#list result.details as detail>
                                                <li>${detail.detail!}</li>
                                            </#list>
                                        </ul>
                                    </#if>
                                    <#if result.moreInfo??>
                                        <div style="margin-top: 10px;padding: 10px; border: 1px solid #ccc; margin-top: 5px; border-radius: 8px; background: #fefefe;">
                                            ${result.moreInfo}
                                        </div>
                                    </#if>
                                </td>
                            <#else>
                                <td class="name" style="border-left: 1px solid #ccc; padding:10px; position: relative; font-weigth:bold;">
                                    ${element.i18nLabel!}
                                </td>
                                <td class="status" style="border-left: 1px solid #ccc; padding:10px; position: relative; text-align: center; font-weight: bold;">
                                    <span style="font-weight: normal; color: #6c757d;">-</span>
                                </td>
                                <td class="details" style="border-left: 1px solid #ccc; padding:10px; position: relative;">
                                </td>
                            </#if>
                        </tr>
                        <#assign count = count + 1>
                    </#if>
                </#list>
            </#list>
        </tbody>    
    </table>    
</div>

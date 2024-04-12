<div class="form-cell" ${elementMetaData!}>
    <#if !(includeMetaData!) && element.properties.style! != "" >
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/js/jquery.numberFormatting.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                $('.textfield_${element.properties.elementUniqueKey!}').numberFormatting({
                    format : '${element.properties.style!}',
                    numOfDecimal : '${element.properties.numOfDecimal!}',
                    useThousandSeparator : '${element.properties.useThousandSeparator!}',
                    prefix : '${element.properties.prefix!}',
                    postfix : '${element.properties.postfix!}'
                });
            });
        </script>
    </#if>
    <label field-tooltip="${elementParamName!}" class="label" for="${elementParamName!}">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value"><span>${valueLabel!?html}</span></div>
        <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="hidden" value="${value!?html}" />
    <#elseif ((validator?? && validator == "numeric") || (element.properties.style! != "" && element.properties.readonly! != 'true' && element.properties.readonlyLabel! != 'true'))>
            <style>
                #container_${elementParamName!} {
                    position: relative;
                    display: flex
                }

                #text-container_${elementParamName!} {
                    position: relative;
                    width: 100%;
                }
                
                #incr_${elementParamName!} {
                    position: absolute;
                    top: 0;
                    right: 0;
                    font-size:10px;
                    border: none;
                    background: none;
                    margin-top: 4px;
                    margin-right: 10px;
                }
                #incr_${elementParamName!}:focus {
                    outline: none;
                }

                #decr_${elementParamName!} {
                    position: absolute;
                    bottom: 0;
                    right: 0;
                    margin-bottom: 4px;
                    font-size:10px;
                    border: none;
                    background: none;
                    margin-right: 10px;
                }
                #decr_${elementParamName!}:focus {
                    outline: none;
                }
                #indication {
                    font-size: 10px;
                    position: absolute;
                    top: 100%;
                    left:0;
                    color:light-grey;
                }
            </style>

            <div id="container_${elementParamName!}" 
                <#if element.properties.size?has_content>
                    <#if element.properties.size?number gte 14>
                        style="width: ${element.properties.size!}%;"
                    <#else>
                        style="width: 14%;"
                    </#if>
                </#if>
            >
            <div id="text-container_${elementParamName!}" style="width: 100%;">
                <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="text"
                    value="${value!?html}" 
                    inputmode="numeric"
                    oninput="this.value = this.value.replace(/[^0-9.,-]/g, '');"
                    style="width:100%;"
                    <#if element.properties.placeholder?has_content>placeholder="${element.properties.placeholder!}"
                    <#else>
                        placeholder='0'
                    </#if> 
                    <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> 
                    <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> />
                <#if (element.properties.style! != "" && element.properties.readonly! != 'true' && element.properties.readonlyLabel! != 'true')>
                    <button type="button" id="incr_${elementParamName!}">
                        <img src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/images/chevron-up-solid.svg">
                    </button>
                    <button type="button" id="decr_${elementParamName!}">
                        <img src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/images/chevron-down-solid.svg">
                    </button>
                </#if>
            </div>

            <div id="indication">
                Only numbers are allowed
            </div>
        </div>

        <#if (element.properties.style! != "" && element.properties.readonly! != 'true' && element.properties.readonlyLabel! != 'true')>
            <script>
                function ${elementParamName!}_incrementDecrementValue(text, incrDecrValue){

                    var sum = 0
                    //If us, use . as decimal
                    if ("${element.properties.style!}" === "us"){
                        //If text is empty, assume the original text's value as 0, and then, add/subtract by incrDecrValue
                        if (text === ""){
                            sum = (0 + incrDecrValue).toFixed("${element.properties.numOfDecimal!}");
                        }
                        else{
                            //The next few lines check if prefix and/or postfix is present, the reason for this is because
                            //without catering the number (for example, in a situation, where the number is negative) for
                            //the prefix, it can cause problem

                            //If both prefix and prefix are present
                            if ("${element.properties.prefix!}" !== "" && "${element.properties.postfix!}" !== ""){
                                if(text.split(" ")[0].charAt(0) === '-') {
                                    text = "-" + text.split(" ")[1];    
                                } else{
                                    text = text.split(" ")[1];
                                }
                            }
                            //if only prefix is present
                            else if ("${element.properties.prefix!}" !== ""){
                                if(text.split(" ")[0].charAt(0) === '-') {
                                    text = "-" + text.split(" ")[1];    
                                } else{
                                    text = text.split(" ")[1];
                                }
                            }else if ("${element.properties.postfix!}" !== ""){
                                text = text.split(" ")[0];
                            }
                            sum = (parseFloat(text) + incrDecrValue).toFixed("${element.properties.numOfDecimal!}")
                        }
                    }
                    //If euro, use , as decimal
                    else if ("${element.properties.style!}" === "euro"){
                        //If text is empty, assume the original text's value as 0, and then, add/subtract by incrDecrValue
                        if (text === ""){
                            sum = (0 + incrDecrValue).toFixed("${element.properties.numOfDecimal!}");
                            sum = sum.replace('.',',');
                        }else{
                            //The next few lines check if prefix and/or postfix is present, the reason for this is because
                            //without catering the number (for example, in a situation, where the number is negative) for
                            //the prefix, it can cause problem

                            //If both prefix and prefix are present
                            if ("${element.properties.prefix!}" !== "" && "${element.properties.postfix!}" !== ""){
                                if(text.split(" ")[0].charAt(0) === '-') {
                                    text = "-" + text.split(" ")[1];    
                                } else{
                                    text = text.split(" ")[1];
                                }
                            }
                            //if only prefix is present
                            else if ("${element.properties.prefix!}" !== ""){
                                if(text.split(" ")[0].charAt(0) === '-') {
                                    text = "-" + text.split(" ")[1];    
                                } else{
                                    text = text.split(" ")[1];
                                }
                            }else if ("${element.properties.postfix!}" !== ""){
                                text = text.split(" ")[0];
                            }
                            text = text.replace(',','.');
                            sum = (parseFloat(text) + incrDecrValue).toFixed("${element.properties.numOfDecimal!}")
                            sum = sum.replace('.',',');
                        }
                    }
                    //Return the sum in string format to easily manipulate it
                    return "" + sum
                }

                $('#incr_${elementParamName!}').on("click", function(e){
                    var elementId = "${elementParamName!}"; 
                    var element = document.getElementById(elementId);
                    var text = element.value;
                    var output = "";
                    let incrementVal = 1;

                    if ("${element.properties.incrementValue!}" !== "") {
                        incrementVal = parseFloat("${element.properties.incrementValue!}")
                    }

                    output = ${elementParamName!}_incrementDecrementValue(text, incrementVal)

                    var maxLength = "${element.properties.maxlength!}"
                
                    if (maxLength === undefined || maxLength === "" || isNaN(maxLength) === true) {
                        element.value = output;
                        
                    } else { 
                        if (output.charAt(0) === '-') {
                            if (output.length - parseInt("${element.properties.numOfDecimal!}")-2 <= maxLength) {
                                element.value = output;
                            }
                        }
                        else if (output.length - parseInt("${element.properties.numOfDecimal!}")-1 <= maxLength) {
                            element.value = output;
                        }
                    }  

                    //Reformat back the new value, using the numberFormatting function
                    $('.textfield_${element.properties.elementUniqueKey!}').numberFormatting({
                        format : '${element.properties.style!}',
                        numOfDecimal : '${element.properties.numOfDecimal!}',
                        useThousandSeparator : '${element.properties.useThousandSeparator!}',
                        prefix : '${element.properties.prefix!}',
                        postfix : '${element.properties.postfix!}'
                    });
                })

                $('#decr_${elementParamName!}').on("click", function(e){
                    var elementId = "${elementParamName!}"; 
                    var element = document.getElementById(elementId);
                    var text = element.value;
                    var output = "";
                    let decrementVal = 1;

                    if ("${element.properties.decrementValue!}" !== "") {
                        decrementVal = parseFloat("-" + "${element.properties.decrementValue!}")
                    }else {
                        decrementVal = parseFloat("-1")
                    }
                    
                    output = ${elementParamName!}_incrementDecrementValue(text, decrementVal)

                    var maxLength = "${element.properties.maxlength!}"
                    
                    if (maxLength === undefined || maxLength === "" || isNaN(maxLength) === true) {
                        element.value = output;
                    } else {
                        if (output.charAt(0) === '-') {
                            if (output.length - parseInt("${element.properties.numOfDecimal!}")-2 <= maxLength) {
                                element.value = output;
                            }
                        }
                        else if (output.length - parseInt("${element.properties.numOfDecimal!}")-1 <= maxLength) {
                            element.value = output;
                        }
                    }  

                    //Reformat back the new value, using the numberFormatting function
                    $('.textfield_${element.properties.elementUniqueKey!}').numberFormatting({
                        format : '${element.properties.style!}',
                        numOfDecimal : '${element.properties.numOfDecimal!}',
                        useThousandSeparator : '${element.properties.useThousandSeparator!}',
                        prefix : '${element.properties.prefix!}',
                        postfix : '${element.properties.postfix!}'
                    });
                    
                })

                $('#${elementParamName!}').focus(function() {
                    $(this).on('keydown', function(e) {
                        if (e.keyCode === 13) {
                            e.preventDefault();
                        }
                    })
                });
            </script>
        </#if>
    <#else>
        <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="text" placeholder="${element.properties.placeholder!?html}" <#if element.properties.size?has_content>size="${element.properties.size!}"</#if> value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> />
    </#if>
</div>
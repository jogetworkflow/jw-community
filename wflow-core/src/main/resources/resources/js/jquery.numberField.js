(function($){
    $.fn.extend({
        numberField : function(o){
            var incrementDecrementValue = function(text, incrDecrValue) {
                var sum = 0;
                if (text !== "") {
                    var regexDecimalSeperator = "\\\.";
                    var regexThousandSeparator = ",";
                    if(o.format.toUpperCase() === "EURO"){
                        regexDecimalSeperator = ",";
                        regexThousandSeparator = "\\\.";
                    }

                    var number = text.replace(/\s/g, "");
                    number = number.replace(new RegExp(regexThousandSeparator, 'g'), '');
                    number = number.replace(new RegExp(regexDecimalSeperator, 'g'), '.');
                    if(o.prefix !== ""){
                        number = number.replace(o.prefix, "");
                    }
                    if(o.postfix !== ""){
                        number = number.replace(o.postfix, "");
                    }
                    
                    sum = parseFloat(number);
                }
                
                sum = (sum + incrDecrValue).toFixed(o.numOfDecimal);
                
                //If euro, use , as decimal
                if (o.format === "euro"){
                    sum = sum.replace('.',',');
                }
                
                //Return the sum in string format to easily manipulate it
                return "" + sum;
            };
            
            var initField = function(element) {
                var controls = $(element).next('.numeric_field_controls');
                
                $(controls).find('a').off('click').on("click", function(){
                    var incrDecrValue = 0;
                    
                    //detect it is increase button or crease button by css class
                    if ($(this).hasClass("numeric_field_control_incr")) {
                        if (o.incrementValue !== "") {
                            incrDecrValue = parseFloat(o.incrementValue);
                        }else {
                            incrDecrValue = 1;
                        }
                    } else {
                        if (o.decrementValue !== "") {
                            incrDecrValue = parseFloat("-" + o.decrementValue);
                        }else {
                            incrDecrValue = -1;
                        }
                    }
                    
                    var output = incrementDecrementValue($(element).val(), incrDecrValue);
                    
                    var formatted = FormUtil.numberFormat(output, o);
                    $(element).val(formatted).trigger("change");
                });
            };
            
            this.each(function(){
                var element = $(this);
                initField(element);
            });
        }
    });
})(jQuery);

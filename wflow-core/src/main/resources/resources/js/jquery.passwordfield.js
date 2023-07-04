(function($){
    $.fn.extend({
        passwordField : function(o){
            var input = $("#" + o.paramName + "_" + o.elementUniqueKey)
              , meterContainer = $("#password-meter_" + o.paramName + "_" + o.elementUniqueKey).get(0)
              , meterBar = $("#password-meter-bar_" + o.paramName + "_" + o.elementUniqueKey).get(0)
              , strengthText = $("#strength-text_" + o.paramName + "_" + o.elementUniqueKey).get(0)
              , meterBorderRadius = ["0", "0", input.css("border-radius"), input.css("border-radius")];

            var strengthMessages = {
                0: o.messages["form.passwordfield.strengthChecker.veryWeak"],
                1: o.messages["form.passwordfield.strengthChecker.weak"],
                2: o.messages["form.passwordfield.strengthChecker.medium"],
                3: o.messages["form.passwordfield.strengthChecker.strong"],
                4: o.messages["form.passwordfield.strengthChecker.veryStrong"]
            };
            var strengthColors = {
                0: "red",
                1: "orange",
                2: "#e6c300", // yellow
                3: "#99cc00", // yellow-green
                4: "green"
            };

            // Setting the width of the progress bar based on the input box's width
            $(meterContainer).width(input.outerWidth());
            $(meterContainer).css("max-width", input.css("max-width"));

            // If input has margin-bottom
            if (input.css("margin-bottom") !== "0px" && input.css("margin-bottom") !== "") {
                // Setting bottom for the progress bar
                // Or the progress bar will be below the input box
                $(meterContainer).css("bottom", input.css("margin-bottom"));
                // Setting margin-top for the text
                // Or there will be a blank space between the text and input box
                $(strengthText).css("margin-top", "-" + input.css("margin-bottom"));
            }

            input.on("input", function() {
                if (input.val()) {
                    var password = input.val();
                    var result = zxcvbn(password);
                    var score = result.score;

                    // Set the width of the meter using percentage
                    $(meterBar).width((score+1) * 20 + "%");
                    // Set the colour of the meter
                    $(meterBar).css("background", strengthColors[score]);
                    // Set the strength text
                    $(strengthText).text(strengthMessages[score]);
                    // Set the colour of the text
                    $(strengthText).css("color", strengthColors[score]);

                    // To control the bottom right corner of the meter's border radius
                    if (score == 4) {
                        // When it's not full, the bottom right radius will be rectangular
                        meterBorderRadius[2] = input.css("border-radius");
                        meterBar.style.borderRadius = meterBorderRadius.join(" ");
                    } else {
                        // When it's full, the bottom right radius will be circular
                        meterBorderRadius[2] = "0";
                        meterBar.style.borderRadius = meterBorderRadius.join(" ")
                    }
                } else {
                    // Clear the strength text and reset the meter
                    $(meterBar).width("0%");
                    $(meterBar).css("background", "");
                    $(strengthText).text("");
                }
            });

            return;
        }
    });
})(jQuery);
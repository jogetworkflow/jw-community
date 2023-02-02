/*
    select2 extension for Kecak Workflow
*/

(function($) {
    $.fn.kecakSelect2 = function( arguments ) {
        let ajax = arguments.ajax;
        let $select2 = this.select2(arguments);

        // handle on change values
        $select2.on('change.select2', function(e) {
            if(e && e.params && e.params.value) {
                let newValue = e.params.value;

                // check if new value is available already
                let exists = false;
                $(this).find('option').each(function() {
                    if (this.value == newValue) {
                        exists = true;
                    }
                });

                if(!exists && ajax) {
                    triggerAjaxForValues($(this), ajax, newValue);
                }
            }
        });

        return $select2;
    };

    function triggerAjaxForValues($selector, ajax, value) {
        if(value) {
            let url = ajax.url;
            let data = ajax.data({});

            data.value = value;
            data.page = undefined;

            $.ajax({
                type: 'GET',
                url: url,
                data: data
            }).then(function (data) {
                // create the option and append to Select2
                let results = data.results;
                for(let i in results) {
                    let result = results[i];
                    let option = new Option(result.text, result.id, true, true);
                    $selector.append(option).trigger('change.select2');
                }
            });
        }
    }
})(jQuery);
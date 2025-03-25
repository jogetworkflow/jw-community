<script>
    $(window).on('page_loaded', function() {
        $('body').removeClass('dark-mode'); 
    });
    
    $(document).ready(function(){
        $('body').removeClass('rtl'); 
        $('html').removeAttr('dir'); 

        $('#loginButton').on('click', function(){
            $('input[name="submit"]').click();
        });

        $('#customUsername').on('change', function(e){
            $('#j_username').val($(this).val());
        });

        $('#customPassword').on('change', function(e){
            $('#j_password').val($(this).val());
        });

        $("input").off('keyup').on('keyup', function(e) {
            if (e.which === 13) {
                $('#loginButton').click();
            }
        }); 
        
        $("body#login #main > div").css({maxWidth: "100%"});

        $("html").css({
            "-ms-overflow-style": "none",
            "scrollbar-width": "none",
        });

        $('<style>' +
            'html::-webkit-scrollbar { width: 0px; }' +
            'html::-webkit-scrollbar-thumb { background: transparent; }' +
            '</style>').appendTo('head');
    });
</script>

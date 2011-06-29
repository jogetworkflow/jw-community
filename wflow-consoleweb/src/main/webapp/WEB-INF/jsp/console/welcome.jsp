<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
    
<div id="getting-started">
    <iframe id="frame" style="display:none; height:200px; width:100%; overflow:hidden;" src="http://www.joget.org/updates/welcome?src=v3" frameborder="0"></iframe>
        
    <a href="http://www.joget.org/help?src=wmc" target="www.joget.org" id="link"></a>
</div>
    
<div class="clear"></div>
    
<script type="text/javascript">
    var image = new Image();
    image.src = "http://www.joget.org/images/welcome.png";
    $(image).load(function(){
        $('#link').hide();
        $('#frame').show();
    });
</script>
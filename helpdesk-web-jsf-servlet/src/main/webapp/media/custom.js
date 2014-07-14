"strict";
(function(){
    $(document).ready(Custom.init);
    
    Custom = {
        init: function() {
            Attributes.init();
        }
    };
    
    Attributes = {
        init: function() {
            $.timepicker.setDefaults($.timepicker.regional[$('html').attr('lang')]);
            $('.attribute-datepicker').datepicker();
        }
    };
}());
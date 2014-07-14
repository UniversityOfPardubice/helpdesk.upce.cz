"strict";
(function(){
    var Custom = {
        init: function() {
            Attributes.init();
        }
    };
    
    var Attributes = {
        init: function() {
            $.timepicker.setDefaults($.timepicker.regional[$('html').attr('lang')]);
            $('.attribute-datepicker').datepicker();
        }
    };

    $(document).ready(Custom.init);
}());
"strict";
(function(){
    var Custom = {
        init: function() {
            Attributes.init();
        }
    };
    
    var Attributes = {
        init: function() {
            $.datepicker.setDefaults($.datepicker.regional[$('html').attr('lang')]);
            $('.attribute-datepicker').datepicker();
        }
    };

    $(document).ready(Custom.init);
}());
<script type="text/javascript">
//this function quotes a message in the FCK editor
function insertTextIntoEditor(message) {
    var textAreaId = "ticketActionForm:actionMessage";
    var fckEditor = CKEDITOR.instances[textAreaId];
    if (fckEditor == null) {
        alert("FCK editor [" + textAreaId + "] not found");
    } else {
	    fckEditor.insertHtml(message);
    }
}
function insertResponse() {
	var element = document.getElementById("ticketActionForm:response");
	if (element != null) {
		eval("insertResponse"+element.value+"()");
		element.selectedIndex=0;
	}
}
fadeAndUnfade("ticketActionForm:mainButtonGroup", 1000, 2, 500);
CKEDITOR.on('instanceReady', function(evt){
    evt.editor.focus();
});
</script>

CKEDITOR.editorConfig = function(config) {
    config.toolbar_actionMessage = [
        ['Undo', 'Redo'],
        ['Cut', 'Copy', 'Paste', 'PasteText'],
        ['Format', 'Bold', 'Italic', 'Underline', 'Strike', '-', 'Subscript', 'Superscript', 'TextColor', '-', 'RemoveFormat'],
        ['NumberedList', 'BulletedList', '-', 'Outdent', 'Indent'],
        ['Link', 'Unlink'],
        ['SpecialChar', 'Smiley', '-', 'Maximize', 'Source']
    ];
    config.toolbar_faqContent = [
        ['Undo', 'Redo'],
        ['Cut', 'Copy', 'Paste', 'PasteText'],
        ['Format', 'Bold', 'Italic', 'Underline', 'Strike', '-', 'Subscript', 'Superscript', 'TextColor', '-', 'RemoveFormat'],
        ['NumberedList', 'BulletedList', '-', 'Outdent', 'Indent'],
        ['Link', 'Unlink'],
        ['SpecialChar', 'Smiley', '-', 'Maximize', 'Source']
    ];
    config.entities = false;
    config.entities_latin = false;
    config.entities_greek = false;
    config.protectedSource.push(/<script[\s\S]*?<\/script>/gi);
    config.protectedSource.push(/<iframe[\s\S]*?<\/iframe>/gi);
    config.protectedSource.push(/<frame[\s\S]*?<\/frame>/gi);
    config.enterMode = CKEDITOR.ENTER_BR;	// p | div | br
    config.shiftEnterMode = CKEDITOR.ENTER_BR;	// p | div | br
//config.browserContextMenuOnCtrl = true ;
//config.BrowserContextMenu = true ;???
//config.FirefoxSpellChecker = true ; Not ported
    config.removeDialogTabs = 'link:target;link:advanced';
    config.format_tags = 'div;p;pre;h3;h4;h5;h6';
};
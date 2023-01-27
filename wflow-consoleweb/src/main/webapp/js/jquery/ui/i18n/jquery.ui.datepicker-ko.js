/* Korean initialisation for the jQuery calendar extension. */
/* Written by DaeKwon Kang (ncrash.dk@gmail.com), Edited by Genie. */
jQuery(function($){
	$.datepicker.regional['ko'] = {
		closeText: '\ub2eb\uae30',
		prevText: '\uc774\uc804\ub2ec',
		nextText: '\ub2e4\uc74c\ub2ec',
		currentText: '\uc624\ub298',
		monthNames: ['1\uc6d4','2\uc6d4','3\uc6d4','4\uc6d4','5\uc6d4','6\uc6d4',
		'7\uc6d4','8\uc6d4','9\uc6d4','10\uc6d4','11\uc6d4','12\uc6d4'],
		monthNamesShort: ['1\uc6d4','2\uc6d4','3\uc6d4','4\uc6d4','5\uc6d4','6\uc6d4',
		'7\uc6d4','8\uc6d4','9\uc6d4','10\uc6d4','11\uc6d4','12\uc6d4'],
		dayNames: ['\uc77c\uc694\uc77c','\uc6d4\uc694\uc77c','\ud654\uc694\uc77c','\uc218\uc694\uc77c','\ubaa9\uc694\uc77c','\uae08\uc694\uc77c','\ud1a0\uc694\uc77c'],
		dayNamesShort: ['\uc77c','\uc6d4','\ud654','\uc218','\ubaa9','\uae08','\ud1a0'],
		dayNamesMin: ['\uc77c','\uc6d4','\ud654','\uc218','\ubaa9','\uae08','\ud1a0'],
		weekHeader: 'Wk',
		dateFormat: 'yy-mm-dd',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: true,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ko']);
});


/* Hebrew initialisation for the UI Datepicker extension. */
/* Written by Amir Hardon (ahardon at gmail dot com). */
jQuery(function($){
	$.datepicker.regional['he'] = {
		closeText: '\u05e1\u05d2\u05d5\u05e8',
		prevText: '&#x3C;\u05d4\u05e7\u05d5\u05d3\u05dd',
		nextText: '\u05d4\u05d1\u05d0&#x3E;',
		currentText: '\u05d4\u05d9\u05d5\u05dd',
		monthNames: ['\u05d9\u05e0\u05d5\u05d0\u05e8','\u05e4\u05d1\u05e8\u05d5\u05d0\u05e8','\u05de\u05e8\u05e5','\u05d0\u05e4\u05e8\u05d9\u05dc','\u05de\u05d0\u05d9','\u05d9\u05d5\u05e0\u05d9',
		'\u05d9\u05d5\u05dc\u05d9','\u05d0\u05d5\u05d2\u05d5\u05e1\u05d8','\u05e1\u05e4\u05d8\u05de\u05d1\u05e8','\u05d0\u05d5\u05e7\u05d8\u05d5\u05d1\u05e8','\u05e0\u05d5\u05d1\u05de\u05d1\u05e8','\u05d3\u05e6\u05de\u05d1\u05e8'],
		monthNamesShort: ['\u05d9\u05e0\u05d5','\u05e4\u05d1\u05e8','\u05de\u05e8\u05e5','\u05d0\u05e4\u05e8','\u05de\u05d0\u05d9','\u05d9\u05d5\u05e0\u05d9',
		'\u05d9\u05d5\u05dc\u05d9','\u05d0\u05d5\u05d2','\u05e1\u05e4\u05d8','\u05d0\u05d5\u05e7','\u05e0\u05d5\u05d1','\u05d3\u05e6\u05de'],
		dayNames: ['\u05e8\u05d0\u05e9\u05d5\u05df','\u05e9\u05e0\u05d9','\u05e9\u05dc\u05d9\u05e9\u05d9','\u05e8\u05d1\u05d9\u05e2\u05d9','\u05d7\u05de\u05d9\u05e9\u05d9','\u05e9\u05d9\u05e9\u05d9','\u05e9\u05d1\u05ea'],
		dayNamesShort: ['\u05d0\'','\u05d1\'','\u05d2\'','\u05d3\'','\u05d4\'','\u05d5\'','\u05e9\u05d1\u05ea'],
		dayNamesMin: ['\u05d0\'','\u05d1\'','\u05d2\'','\u05d3\'','\u05d4\'','\u05d5\'','\u05e9\u05d1\u05ea'],
		weekHeader: 'Wk',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: true,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['he']);
});

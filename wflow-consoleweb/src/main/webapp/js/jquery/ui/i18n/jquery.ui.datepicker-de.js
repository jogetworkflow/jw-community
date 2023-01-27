/* German initialisation for the jQuery UI date picker plugin. */
/* Written by Milian Wolff (mail@milianw.de). */
jQuery(function($){
	$.datepicker.regional['de'] = {
		closeText: 'Schlie\u0026\u0023\u0032\u0032\u0033\u003ben',
		prevText: '&#x3C;Zur\u0026\u0023\u0032\u0035\u0032\u003bck',
		nextText: 'Vor&#x3E;',
		currentText: 'Heute',
		monthNames: ['Januar','Februar','M\u0026\u0023\u0032\u0032\u0038\u003brz','April','Mai','Juni',
		'Juli','August','September','Oktober','November','Dezember'],
		monthNamesShort: ['Jan','Feb','M\u0026\u0023\u0032\u0032\u0038\u003br','Apr','Mai','Jun',
		'Jul','Aug','Sep','Okt','Nov','Dez'],
		dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
		dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		weekHeader: 'KW',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['de']);
});

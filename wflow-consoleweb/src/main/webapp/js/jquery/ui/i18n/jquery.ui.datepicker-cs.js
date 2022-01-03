/* Czech initialisation for the jQuery UI date picker plugin. */
/* Written by Tomas Muller (tomas@tomas-muller.net). */
jQuery(function($){
	$.datepicker.regional['cs'] = {
		closeText: '\u005a\u0061\u0076\u0159\u0026\u0023\u0032\u0033\u0037\u003b\u0074',
		prevText: '&#x3C;D\u0159\u0026\u0023\u0032\u0033\u0037\u003bve',
		nextText: 'Pozd\u011bji&#x3E;',
		currentText: 'Nyn\u0026\u0023\u0032\u0033\u0037\u003b',
		monthNames: ['leden','\u0026\u0023\u0032\u0035\u0030\u003bnor','b\u0159ezen','duben','kv\u011bten','\u010derven',
		'\u010dervenec','srpen','z\u0026\u0023\u0032\u0032\u0035\u003b\u0159\u0026\u0023\u0032\u0033\u0037\u003b','\u0159\u0026\u0023\u0032\u0033\u0037\u003bjen','listopad','prosinec'],
		monthNamesShort: ['led','\u0026\u0023\u0032\u0035\u0030\u003bno','b\u0159e','dub','kv\u011b','\u010der',
		'\u010dvc','srp','z\u0026\u0023\u0032\u0032\u0035\u003b\u0159','\u0159\u0026\u0023\u0032\u0033\u0037\u003bj','lis','pro'],
		dayNames: ['ned\u011ble', 'pond\u011bl\u0026\u0023\u0032\u0033\u0037\u003b', '\u0026\u0023\u0032\u0035\u0030\u003bter\u0026\u0023\u0032\u0035\u0033\u003b', 'st\u0159eda', '\u010dtvrtek', 'p\u0026\u0023\u0032\u0032\u0035\u003btek', 'sobota'],
		dayNamesShort: ['ne', 'po', '\u0026\u0023\u0032\u0035\u0030\u003bt', 'st', '\u010dt', 'p\u0026\u0023\u0032\u0032\u0035\u003b', 'so'],
		dayNamesMin: ['ne','po','\u0026\u0023\u0032\u0035\u0030\u003bt','st','\u010dt','p\u0026\u0023\u0032\u0032\u0035\u003b','so'],
		weekHeader: 'T\u0026\u0023\u0032\u0035\u0033\u003bd',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['cs']);
});

/* Arabic Translation for jQuery UI date picker plugin. */
/* Khaled Alhourani -- me@khaledalhourani.com */
/* NOTE: monthNames are the original months names and they are the Arabic names, not the new months name �?براير - يناير and there isn't any Arabic roots for these months */
jQuery(function($){
	$.datepicker.regional['ar'] = {
		closeText: '\u0625\u063a\u0644\u0627\u0642',
		prevText: '&#x3C;\u0627\u0644\u0633\u0627\u0628\u0642',
		nextText: '\u0627\u0644\u062a\u0627\u0644\u064a&#x3E;',
		currentText: '\u0627\u0644\u064a\u0648\u0645',
		monthNames: ['\u0643\u0627\u0646\u0648\u0646\u0020\u0627\u0644\u062b\u0627\u0646\u064a', '\u0634\u0628\u0627\u0637', '\u0622\u0630\u0627\u0631', '\u0646\u064a\u0633\u0627\u0646', '\u0645\u0627\u064a\u0648', '\u062d\u0632\u064a\u0631\u0627\u0646',
		'\u062a\u0645\u0648\u0632', '\u0622\u0628', '\u0623\u064a\u0644\u0648\u0644',	'\u062a\u0634\u0631\u064a\u0646\u0020\u0627\u0644\u0623\u0648\u0644', '\u062a\u0634\u0631\u064a\u0646\u0020\u0627\u0644\u062b\u0627\u0646\u064a', '\u0643\u0627\u0646\u0648\u0646\u0020\u0627\u0644\u0623\u0648\u0644'],
		monthNamesShort: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
		dayNames: ['\u0627\u0644\u0623\u062d\u062f', '\u0627\u0644\u0627\u062b\u0646\u064a\u0646', '\u0627\u0644\u062b\u0644\u0627\u062b\u0627\u0621', '\u0627\u0644\u0623\u0631\u0628\u0639\u0627\u0621', '\u0627\u0644\u062e\u0645\u064a\u0633', '\u0627\u0644\u062e\u0645\u064a\u0633\u0627\u0644\u062c\u0645\u0639\u0629', '\u0627\u0644\u0633\u0628\u062a'],
		dayNamesShort: ['\u0627\u0644\u0623\u062d\u062f', '\u0627\u0644\u0627\u062b\u0646\u064a\u0646', '\u0627\u0644\u062b\u0644\u0627\u062b\u0627\u0621', '\u0627\u0644\u0623\u0631\u0628\u0639\u0627\u0621', '\u0627\u0644\u062e\u0645\u064a\u0633', '\u0627\u0644\u062e\u0645\u064a\u0633\u0627\u0644\u062c\u0645\u0639\u0629', '\u0627\u0644\u0633\u0628\u062a'],
		dayNamesMin: ['\u062d', '\u0646', '\u062b', '\u0631', '\u062e', '\u062c', '\u0633'],
		weekHeader: '\u0623\u0633\u0628\u0648\u0639',
		dateFormat: 'dd/mm/yy',
		firstDay: 6,
  		isRTL: true,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ar']);
});


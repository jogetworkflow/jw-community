/* Turkish initialisation for the jQuery UI date picker plugin. */
/* Written by Izzet Emre Erkan (kara@karalamalar.net). */
jQuery(function($){
	$.datepicker.regional['tr'] = {
		closeText: 'kapat',
		prevText: '&#x3C;geri',
		nextText: 'ileri&#x3e',
		currentText: 'bug\u0026\u0023\u0032\u0035\u0032\u003bn',
		monthNames: ['Ocak','\u015fubat','Mart','Nisan','May\u0131s','Haziran',
		'Temmuz','A?ustos','Eyl\u0026\u0023\u0032\u0035\u0032\u003bl','Ekim','\u004b\u0061\u0073\u0131\u006d','\u0041\u0072\u0061\u006c\u0131\u006b'],
		monthNamesShort: ['Oca','\u015eub','Mar','Nis','May','Haz',
		'Tem','A\u011fu','Eyl','Eki','Kas','Ara'],
		dayNames: ['Pazar','Pazartesi','Sal\u0131','\u0026\u0023\u0031\u0039\u0039\u003bar\u015famba','Per\u015fembe','Cuma','Cumartesi'],
		dayNamesShort: ['Pz','Pt','Sa','\u0026\u0023\u0031\u0039\u0039\u003ba','Pe','Cu','Ct'],
		dayNamesMin: ['Pz','Pt','Sa','\u0026\u0023\u0031\u0039\u0039\u003ba','Pe','Cu','Ct'],
		weekHeader: 'Hf',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['tr']);
});

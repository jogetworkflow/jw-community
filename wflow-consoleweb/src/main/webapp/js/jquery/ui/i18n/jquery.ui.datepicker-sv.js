/* Swedish initialisation for the jQuery UI date picker plugin. */
/* Written by Anders Ekdahl ( anders@nomadiz.se). */
jQuery(function($){
	$.datepicker.regional['sv'] = {
		closeText: 'St\u0026\u0023\u0032\u0032\u0038\u003bng',
		prevText: '&#xAB;F\u0026\u0023\u0032\u0034\u0036\u003brra',
		nextText: 'N\u0026\u0023\u0032\u0032\u0038\u003bsta&#xBB;',
		currentText: 'Idag',
		monthNames: ['Januari','Februari','Mars','April','Maj','Juni',
		'Juli','Augusti','September','Oktober','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNamesShort: ['S\u0026\u0023\u0032\u0034\u0036\u003bn','M\u0026\u0023\u0032\u0032\u0039\u003bn','Tis','Ons','Tor','Fre','L\u0026\u0023\u0032\u0034\u0036\u003br'],
		dayNames: ['S\u0026\u0023\u0032\u0034\u0036\u003bndag','M\u0026\u0023\u0032\u0032\u0039\u003bndag','Tisdag','Onsdag','Torsdag','Fredag','L\u0026\u0023\u0032\u0034\u0036\u003brdag'],
		dayNamesMin: ['S\u0026\u0023\u0032\u0034\u0036\u003b','M\u0026\u0023\u0032\u0032\u0039\u003b','Ti','On','To','Fr','L\u0026\u0023\u0032\u0034\u0036\u003b'],
		weekHeader: 'Ve',
		dateFormat: 'yy-mm-dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['sv']);
});


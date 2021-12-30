/* Hungarian initialisation for the jQuery UI date picker plugin. */
/* Written by Istvan Karaszi (jquery@spam.raszi.hu). */
jQuery(function($){
	$.datepicker.regional['hu'] = {
		closeText: 'bez\u0026\u0023\u0032\u0032\u0035\u003br',
		prevText: 'vissza',
		nextText: 'el\u0151re',
		currentText: 'ma',
		monthNames: ['Janu\u0026\u0023\u0032\u0032\u0035\u003br', 'Febru\u0026\u0023\u0032\u0032\u0035\u003br', 'M\u0026\u0023\u0032\u0032\u0035\u003brcius', '\u0026\u0023\u0032\u0032\u0035\u003bprilis', 'M\u0026\u0023\u0032\u0032\u0035\u003bjus', 'J\u0026\u0023\u0032\u0035\u0030\u003bnius',
		'J\u0026\u0023\u0032\u0035\u0030\u003blius', 'Augusztus', 'Szeptember', 'Okt\u0026\u0023\u0032\u0034\u0033\u003bber', 'November', 'December'],
		monthNamesShort: ['Jan', 'Feb', 'M\u0026\u0023\u0032\u0032\u0035\u003br', '\u0026\u0023\u0032\u0032\u0035\u003bpr', 'M\u0026\u0023\u0032\u0032\u0035\u003bj', 'J\u0026\u0023\u0032\u0035\u0030\u003bn',
		'J\u0026\u0023\u0032\u0035\u0030\u003bl', 'Aug', 'Szep', 'Okt', 'Nov', 'Dec'],
		dayNames: ['Vas\u0026\u0023\u0032\u0032\u0035\u003brnap', 'H\u0026\u0023\u0032\u0033\u0033\u003btf\u0151', 'Kedd', 'Szerda', 'Cs\u0026\u0023\u0032\u0035\u0032\u003bt\u0026\u0023\u0032\u0034\u0036\u003brt\u0026\u0023\u0032\u0034\u0036\u003bk', 'P\u0026\u0023\u0032\u0033\u0033\u003bntek', 'Szombat'],
		dayNamesShort: ['Vas', 'H\u0026\u0023\u0032\u0033\u0033\u003bt', 'Ked', 'Sze', 'Cs\u0026\u0023\u0032\u0035\u0032\u003b', 'P\u0026\u0023\u0032\u0033\u0033\u003bn', 'Szo'],
		dayNamesMin: ['V', 'H', 'K', 'Sze', 'Cs', 'P', 'Szo'],
		weekHeader: 'H\u0026\u0023\u0032\u0033\u0033\u003bt',
		dateFormat: 'yy.mm.dd.',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: true,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['hu']);
});

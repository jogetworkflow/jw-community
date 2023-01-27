/* Romanian initialisation for the jQuery UI date picker plugin.
 *
 * Written by Edmond L. (ll_edmond@walla.com)
 * and Ionut G. Stan (ionut.g.stan@gmail.com)
 */
jQuery(function($){
	$.datepicker.regional['ro'] = {
		closeText: '\u0026\u0023\u0032\u0030\u0036\u003bnchide',
		prevText: '&#xAB; Luna precedent\u0103',
		nextText: 'Luna urm\u0103toare &#xBB;',
		currentText: 'Azi',
		monthNames: ['Ianuarie','Februarie','Martie','Aprilie','Mai','Iunie',
		'Iulie','August','Septembrie','Octombrie','Noiembrie','Decembrie'],
		monthNamesShort: ['Ian', 'Feb', 'Mar', 'Apr', 'Mai', 'Iun',
		'Iul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
		dayNames: ['Duminic\u0103', 'Luni', 'Mar\u0163i', 'Miercuri', 'Joi', 'Vineri', 'S\u0026\u0023\u0032\u0032\u0036\u003bmb\u0103t\u0103'],
		dayNamesShort: ['Dum', 'Lun', 'Mar', 'Mie', 'Joi', 'Vin', 'S\u0026\u0023\u0032\u0032\u0036\u003bm'],
		dayNamesMin: ['Du','Lu','Ma','Mi','Jo','Vi','S\u0026\u0023\u0032\u0032\u0036\u003b'],
		weekHeader: 'S\u0103pt',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ro']);
});

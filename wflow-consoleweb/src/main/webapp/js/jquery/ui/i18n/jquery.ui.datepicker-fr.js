/* French initialisation for the jQuery UI date picker plugin. */
/* Written by Keith Wood (kbwood{at}iinet.com.au),
			  Stéphane Nahmani (sholby@sholby.net),
			  Stéphane Raimbault <stephane.raimbault@gmail.com> */
jQuery(function($){
	$.datepicker.regional['fr'] = {
		closeText: 'Fermer',
		prevText: 'Pr\u0026\u0023\u0032\u0033\u0033\u003bc\u0026\u0023\u0032\u0033\u0033\u003bdent',
		nextText: 'Suivant',
		currentText: 'Aujourd\'hui',
		monthNames: ['Janvier','F\u0026\u0023\u0032\u0033\u0033\u003bvrier','Mars','Avril','Mai','Juin',
		'Juillet','Ao\u0026\u0023\u0032\u0035\u0031\u003bt','Septembre','Octobre','Novembre','D\u0026\u0023\u0032\u0033\u0033\u003bcembre'],
		monthNamesShort: ['Janv.','F\u0026\u0023\u0032\u0033\u0033\u003bvr.','Mars','Avril','Mai','Juin',
		'Juil.','Ao\u0026\u0023\u0032\u0035\u0031\u003bt','Sept.','Oct.','Nov.','D\u0026\u0023\u0032\u0033\u0033\u003bc.'],
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim.','Lun.','Mar.','Mer.','Jeu.','Ven.','Sam.'],
		dayNamesMin: ['D','L','M','M','J','V','S'],
		weekHeader: 'Sem.',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['fr']);
});

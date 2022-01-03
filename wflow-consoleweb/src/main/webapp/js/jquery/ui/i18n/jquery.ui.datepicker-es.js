/* Inicialización en español para la extensión 'UI date picker' para jQuery. */
/* Traducido por Vester (xvester@gmail.com). */
jQuery(function($){
	$.datepicker.regional['es'] = {
		closeText: 'Cerrar',
		prevText: '&#x3C;Ant',
		nextText: 'Sig&#x3E;',
		currentText: 'Hoy',
		monthNames: ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
		'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'],
		monthNamesShort: ['Ene','Feb','Mar','Abr','May','Jun',
		'Jul','Ago','Sep','Oct','Nov','Dic'],
		dayNames: ['Domingo','Lunes','Martes','Mi\u0026\u0023\u0032\u0033\u0033\u003brcoles','Jueves','Viernes','S\u0026\u0023\u0032\u0032\u0035\u003bbado'],
		dayNamesShort: ['Dom','Lun','Mar','Mi\u0026\u0023\u0032\u0033\u0033\u003b','Juv','Vie','S\u0026\u0023\u0032\u0032\u0035\u003bb'],
		dayNamesMin: ['Do','Lu','Ma','Mi','Ju','Vi','S\u0026\u0023\u0032\u0032\u0035\u003b'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['es']);
});

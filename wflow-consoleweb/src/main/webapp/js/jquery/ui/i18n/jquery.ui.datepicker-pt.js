/* Portuguese initialisation for the jQuery UI date picker plugin. */
jQuery(function($){
	$.datepicker.regional['pt'] = {
		closeText: 'Fechar',
		prevText: '&#x3C;Anterior',
		nextText: 'Seguinte',
		currentText: 'Hoje',
		monthNames: ['Janeiro','Fevereiro','Mar\u0026\u0023\u0032\u0033\u0031\u003bo','Abril','Maio','Junho',
		'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
		monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun',
		'Jul','Ago','Set','Out','Nov','Dez'],
		dayNames: ['Domingo','Segunda-feira','Ter\u0026\u0023\u0032\u0033\u0031\u003ba-feira','Quarta-feira','Quinta-feira','Sexta-feira','S\u0026\u0023\u0032\u0032\u0035\u003bbado'],
		dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','S\u0026\u0023\u0032\u0032\u0035\u003bb'],
		dayNamesMin: ['Dom','Seg','Ter','Qua','Qui','Sex','S\u0026\u0023\u0032\u0032\u0035\u003bb'],
		weekHeader: 'Sem',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['pt']);
});

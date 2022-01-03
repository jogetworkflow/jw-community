/* Brazilian initialisation for the jQuery UI date picker plugin. */
/* Written by Leonildo Costa Silva (leocsilva@gmail.com). */
jQuery(function($){
	$.datepicker.regional['pt-BR'] = {
		closeText: 'Fechar',
		prevText: '&#x3C;Anterior',
		nextText: 'Pr\u0026\u0023\u0032\u0034\u0033\u003bximo&#x3E;',
		currentText: 'Hoje',
		monthNames: ['Janeiro','Fevereiro','Mar\u0026\u0023\u0032\u0033\u0031\u003bo','Abril','Maio','Junho',
		'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
		monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun',
		'Jul','Ago','Set','Out','Nov','Dez'],
		dayNames: ['Domingo','Segunda-feira','Ter\u0026\u0023\u0032\u0033\u0031\u003ba-feira','Quarta-feira','Quinta-feira','Sexta-feira','S\u0026\u0023\u0032\u0032\u0035\u003bbado'],
		dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','S\u0026\u0023\u0032\u0032\u0035\u003bb'],
		dayNamesMin: ['Dom','Seg','Ter','Qua','Qui','Sex','S\u0026\u0023\u0032\u0032\u0035\u003bb'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['pt-BR']);
});

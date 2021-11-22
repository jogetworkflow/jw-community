(function($) {
    $.fn.extend({
        acSparkline: function() {
            var elements = this;
            
            var currentScriptPath = $('head script[src$=\'apexcharts-sparkline.js\']').attr("src");
            currentScriptPath = currentScriptPath.replace('apexcharts-sparkline.js', '');
            
            loadScript(currentScriptPath + "apexcharts-3.33.0/apexcharts.min.js", function(){
                
                $(elements).each(function() {
                    var el = $(this);
                    if (!$(el).hasClass("acSparkline-initialized")) {
                        var type = $(el).data("chart-type");
                        var width = $(el).data("chart-width");
                        var height = $(el).data("chart-height");
                        var series = $(el).data("chart-values");
                        var config = $(el).data("chart-config");
                        var custom = $(el).data("chart-custom");
                        var color = $(el).data("chart-color");
                        var colors = $(el).data("chart-colors");
                        var labels = $(el).data("chart-labels");
                        var sparkline = $(el).data("chart-sparkline");
                        
                        if (series !== null && series !== undefined) {
                            try {
                                series = eval(series);
                            } catch (err){}
                        }
                        
                        var isGauge = false;
                        if (type === "gauge") {
                            type = "radialBar";
                            isGauge = true;
                        }

                        var options = {
                            series : series,
                            chart: {
                                type: type,
                                sparkline: {
                                    enabled: true
                                }
                            },
                            dataLabels: {
                                enabled: false
                            },
                            stroke: {
                                width: 3,
                                curve: 'smooth'
                            },
                            tooltip: {
                                show: false,
                                fixed: {
                                    enabled: false
                                }
                            }
                        };

                        if (width !== null && width !== undefined && width !== "") {
                            options['chart']['width'] = width;
                        }
                        if (height !== null && height !== undefined && height !== "") {
                            options['chart']['height'] = height;
                        }
                        if (color !== null && color !== undefined && color !== "") {
                            options['colors'] = [color];
                        } else if (colors !== null && colors !== undefined && colors !== "") {
                            options['colors'] = colors.split(",");
                        }
                        if (sparkline !== null && sparkline !== undefined && sparkline === "false") {
                            options['chart']['sparkline']['enabled'] = false;
                        }
                        
                        if (labels !== null && labels !== undefined && labels !== "") {
                            if (type === "area" || type === "line") {
                                options['xaxis'] = {
                                    categories: labels.split(",")
                                };
                            } else {
                                options['labels'] = labels.split(",");
                            }
                        }

                        //pre-config for each chart type
                        if (type === "area") {    
                            options['fill'] = {
                                opacity: 0.8,
                                type: 'solid'
                            };
                            options['stroke'] = {
                                width: 2,
                                curve: 'straight'
                            };
                        } else if (type === "radialBar" && !isGauge) {
                            options['plotOptions'] = {
                                radialBar: {
                                    hollow: {
                                        margin: 0,
                                        size: '50%'
                                    },
                                    track: {
                                        margin: 1
                                    },
                                    dataLabels: {
                                        show: false
                                    }
                                }
                           };
                        } else if (type === "polarArea") {
                            options['stroke']['colors'] = ['#fff'];
                            options['stroke']['width'] = 1;
                            options['stroke']['curve'] = 'straight';
                            options['fill'] = {
                                opacity: 1
                            };
                        } else if (type === "pie" || type === "donut") {   
                            options['plotOptions'] = {
                                pie: {
                                    customScale: 0.85
                                }
                            };
                            options['stroke']['width'] = 2;
                            options['stroke']['colors'] = ["#ffffff69"];
                        } else if (isGauge) {
                            options['plotOptions'] = {
                                radialBar: {
                                    startAngle: -90,
                                    endAngle: 90,
                                    hollow: {
                                        margin: 0,
                                        size: '50%'
                                    },
                                    track: {
                                        startAngle: -90,
                                        endAngle: 90,
                                        margin: 1
                                    },
                                    dataLabels: {
                                        show: true,
                                        name: {
                                            show: false
                                        },
                                        value: {
                                            color: color
                                        }
                                    }
                                }
                           };
                           options['stroke'] = {
                                width: 5,
                                curve: 'smooth'
                            };
                        }

                        if (config !== null && config !== undefined) {
                            try {
                                config = eval('['+config+']')[0];
                                options = $.extend(true, options, config);
                            } catch (err){}
                        }
                        if (custom !== null && custom !== undefined) {
                            try {
                                custom = eval('['+custom+']')[0];
                                options = $.extend(true, options, custom);
                            } catch (err){}
                        }
                        var chart = new ApexCharts(el[0], options);
                        chart.render();
                        $(el).addClass("acSparkline-initialized")
                    }
                });
            });
            return this;
        }
    });
})(jQuery);

GovernanceUtil = {
    getResultTimeout : null,
    interval : 30000,
    lastResult : {},
    popup : new PopupDialog("", " "),

    msg : null,

    init : function(interval, msg) {
        GovernanceUtil.msg = msg;

        $("#check_interval").val(interval);

        $('.updateBtn').on("click", function(){
            GovernanceUtil.updateInterval();
        });

        $('.checkNowBtn').on("click", function(){
            GovernanceUtil.runCheckNow();
        });

        $('.deleteDataBtn').on("click", function(){
            GovernanceUtil.cleanData();
        });

        $('.alertBtn').on("click", function(){
            GovernanceUtil.popup.src = UI.base + "/web/console/monitor/governance/alert";
            GovernanceUtil.popup.init();
        });

        $('.configBtn').on("click", function(){
            var pluginclass = $(this).closest("tr").attr("plugin-class");
            GovernanceUtil.popup.src = UI.base + "/web/console/monitor/governance/config?pluginclass=" + pluginclass;
            GovernanceUtil.popup.init();
        });

        $('.deactivateBtn').on("click", function(){
            var pluginclass = $(this).closest("tr").attr("plugin-class");
            if ($(this).hasClass("deactivated")) {
                GovernanceUtil.activate(pluginclass, $(this));
            } else {
                GovernanceUtil.deactivate(pluginclass, $(this));
            }
        });

        $('.governance_report table').on("click", ".moreInfoBtn", function(){
            $(this).parent().toggleClass("show");
        });
        
        $('.governance_report table').on("click", "a.btn-suppress", function(){
            GovernanceUtil.suppress($(this).closest("li"));
        });
        
        $('#status_selector').on("change", function(){
            if ($(this).val() === "all") {
                $('.governance_report').addClass("show-suppressed");
            } else {
                $('.governance_report').removeClass("show-suppressed");
            }
        });

        GovernanceUtil.triggerNextRetrieveResult();

        if (!(typeof document.addEventListener === "undefined")) {
            var hidden, visibilityChange;
            if (typeof document.hidden !== "undefined") { // Opera 12.10 and Firefox 18 and later support 
                hidden = "hidden";
                visibilityChange = "visibilitychange";
            } else if (typeof document.msHidden !== "undefined") {
                hidden = "msHidden";
                visibilityChange = "msvisibilitychange";
            } else if (typeof document.webkitHidden !== "undefined") {
                hidden = "webkitHidden";
                visibilityChange = "webkitvisibilitychange";
            }

            document.addEventListener(visibilityChange, function(){
                if (document[hidden]) {
                    clearTimeout(GovernanceUtil.getResultTimeout);
                } else {
                    GovernanceUtil.triggerNextRetrieveResult();
                }
            }, false);
        }
    }, 

    activate: function(pluginclass, link) {
        GovernanceUtil.blockUI();
        if (confirm(GovernanceUtil.msg['activateConfirm'])) {
            ConnectionManager.post(UI.base + "/web/governance/activate", {
                success : function(data) {
                    $(link).removeClass("deactivated");
                    $.unblockUI();
                }
            }, 
            {
                pluginClass : pluginclass
            });
        } else {
            $.unblockUI(); //unblock the UI when cancel
        }
    },

    deactivate: function(pluginclass, link) {
        GovernanceUtil.blockUI();
        if (confirm(GovernanceUtil.msg['deactivateConfirm'])) {
            ConnectionManager.post(UI.base + "/web/governance/deactivate", {
                success : function(data) {
                    $(link).addClass("deactivated");
                    $.unblockUI();
                }
            }, 
            {
                pluginClass : pluginclass
            });
        } else {
            $.unblockUI(); //unblock the UI when cancel
        }
    },
    
    suppress: function(item) {
        GovernanceUtil.blockUI();
        if (confirm(GovernanceUtil.msg['suppressConfirm'])) {
            var pluginclass = $(item).closest("tr").attr("plugin-class");
            var scroll = $("html").scrollTop();
            ConnectionManager.post(UI.base + "/web/governance/suppress", {
                success : function(data) {
                    GovernanceUtil.updateResult(data, scroll);
                    $.unblockUI();
                }
            }, 
            {
                pluginClass : pluginclass,
                detail : $(item).find('.detail').html()
            });
        } else {
            $.unblockUI(); //unblock the UI when cancel
        }
    },

    cleanData: function() {
        GovernanceUtil.blockUI();
        if (confirm(GovernanceUtil.msg['deleteConfirm'])) {
            ConnectionManager.post(UI.base + "/web/governance/deleteData", {
                success : function(data) {
                    alert(GovernanceUtil.msg['dataDeleted']);
                    GovernanceUtil.updateResult("{}");
                    $.unblockUI();
                }
            });
        } else {
            $.unblockUI(); //unblock the UI when cancel
        }
    },

    runCheckNow : function() {
        GovernanceUtil.blockUI();
        clearTimeout(GovernanceUtil.getResultTimeout);
        ConnectionManager.post(UI.base + "/web/governance/checkNow", {
            success : function(data) {
                GovernanceUtil.updateResult(data);
                GovernanceUtil.triggerNextRetrieveResult();
                $.unblockUI();
            }
        });
    },

    updateInterval : function() {
        GovernanceUtil.blockUI();

        ConnectionManager.post(UI.base + "/web/governance/updateInterval", {
            success : function(data) {
                alert(GovernanceUtil.msg['intervalUpdated']);
                $.unblockUI();
            }
        }, 
        {
            interval : $("#check_interval").val()
        });
    },

    timeDifference: function(current, previous) {

        var msPerMinute = 60 * 1000;
        var msPerHour = msPerMinute * 60;
        var msPerDay = msPerHour * 24;
        var msPerMonth = msPerDay * 30;
        var msPerYear = msPerDay * 365;

        var elapsed = current - previous;

        if (elapsed < msPerMinute) {
             return Math.round(elapsed/1000) + ' ' + GovernanceUtil.msg['seconds'];   
        } else if (elapsed < msPerHour) {
             return Math.round(elapsed/msPerMinute) + ' ' + GovernanceUtil.msg['minutes'];   
        } else if (elapsed < msPerDay ) {
             return Math.round(elapsed/msPerHour ) + ' ' + GovernanceUtil.msg['hours'];   
        } else if (elapsed < msPerMonth) {
            return Math.round(elapsed/msPerDay) + ' ' + GovernanceUtil.msg['days'];   
        } else if (elapsed < msPerYear) {
            return Math.round(elapsed/msPerMonth) + ' ' + GovernanceUtil.msg['months'];   
        } else {
            return Math.round(elapsed/msPerYear ) + ' ' + GovernanceUtil.msg['years'];   
        }
    },

    updateResult : function(result, scroll) {
        if (result !== null && result !== undefined && result !== "") {
            if (typeof result === 'string') {
                result = JSON.decode(result);
            }
            GovernanceUtil.lastResult = result;
        }

        var now = (new Date()).getTime();

        $('.governance_report table tr[plugin-class]').each(function() {
            var row = $(this);
            var className = $(row).attr('plugin-class');
            var rowResult = GovernanceUtil.lastResult[className];

            if (rowResult !== undefined) {
                var status = rowResult.status;
                if (status !== undefined) {
                    status = status.toLowerCase();
                }
                if (rowResult.score !== undefined) {
                    $(row).find('.status .noData').remove();
                    GovernanceUtil.updateChart($(row).find('.status'), rowResult.score, status);
                } else {
                    $(row).find('.status').html('<span class="'+status+'">'+GovernanceUtil.msg[status]+'</span>');
                }
                $(row).find('.status').data('score', rowResult.score);
                $(row).find('.status').attr('data-status', status);
                $(row).find('.timestamp').html('<span>'+GovernanceUtil.timeDifference(now, rowResult.timestamp)+'</span>');

                var isShowMore = $(row).find('.details .moreInfo').hasClass("show");

                $(row).find('.details').html('<ul></ul>');
                for (var i in rowResult.details) {
                    var li = $('<li><span class="detail">'+rowResult.details[i].detail+'</span></li>');

                    if (rowResult.details[i].link && rowResult.details[i].link !== "") {
                        var btnLabel = rowResult.details[i].linkLabel;
                        if (btnLabel === undefined || btnLabel === "") {
                            btnLabel = GovernanceUtil.msg["view"];
                        }
                        var link = rowResult.details[i].link;
                        if (link.indexOf("http") !== 0) {
                            link = UI.base + link;
                        }
                        $(li).append('<a href="'+link+'"  target="_blank" class="btn btn-secondary btn-sm">'+btnLabel+'</a>');
                    }
                    
                    if (rowResult.suppressable) {
                        if (rowResult.details[i].suppressed) {
                            $(li).addClass("suppressed");
                            $(li).append('<span class="btn-suppress btn">'+GovernanceUtil.msg["suppressed"]+'</span>');
                        } else {
                            $(li).append('<a class="btn-suppress btn btn-warning btn-sm">'+GovernanceUtil.msg["suppress"]+'</a>');
                        }
                    }

                    $(row).find('.details > ul').append(li);
                }
                if (rowResult.moreInfo) {
                    $(row).find('.details').append('<div class="moreInfo"><a class="moreInfoBtn"><span class="show">More Info</span><span class="hide">Hide Info</span></a><div class="infoContent">'+rowResult.moreInfo+'</div></div>');

                    if (isShowMore) {
                        $(row).find('.details .moreInfo').addClass("show");
                    }
                }
            } else {
                $(row).find('.status, .timestamp').html('<span class="noData">-</span>');
                $(row).find('.details').html("");
            }
        });

        GovernanceUtil.renderSummaries();
        
        //to make sure the suppress action won't jump to page top
        if (scroll) {
            $('html').animate({
                scrollTop: scroll
            }, 1);
        }
    },

    triggerNextRetrieveResult : function() {
        GovernanceUtil.getResultTimeout = setTimeout(GovernanceUtil.retrieveResult, GovernanceUtil.interval);
    },

    retrieveResult : function() {
        clearTimeout(GovernanceUtil.getResultTimeout);

        var lastCheck = "";
        if (GovernanceUtil.lastResult !== null && GovernanceUtil.lastResult['lastCheckDate'] !== undefined) {
            lastCheck = "?lastCheck=" + GovernanceUtil.lastResult['lastCheckDate'].timestamp;
        }
        ConnectionManager.get(UI.base + "/web/governance/lastResult" + lastCheck, {
            success : function(data) {
                GovernanceUtil.updateResult(data);
                GovernanceUtil.triggerNextRetrieveResult();
            },
            error : function() {
                GovernanceUtil.triggerNextRetrieveResult();
            }
        });
    },

    renderSummaries : function() {
        if ($(".governance_report .governance_charts").length === 0) {
            $(".governance_report").prepend('<div class="governance_charts"></div>');

            $(".governance_report table tr.category").each(function(){
                var label = $(this).find('.category_label').text();
                var id = $(this).attr("id");
                var catChart = $('<div id="'+id+'-chart" class="category_chart"></div>');
                 $(".governance_charts").append(catChart);

                 GovernanceUtil.createChart($(catChart), label);

                 $(catChart).on("click", function(){
                    $('html, body').animate({
                           scrollTop: $('#'+id).offset().top - 60
                    }, 1000);
                 });
            });
        }

        $(".governance_report table tr.category").each(function(){
            var id = $(this).attr("id");
            var checkers = $(this).nextUntil("tr.category");

            var status = "";
            var totalScore = 0;
            var count = 0;
            $(checkers).each(function(){
                if ($(this).find(".status .noData").length === 0) {
                    var score = $(this).find(".status").data("score");
                    var cstatus = $(this).find(".status").data("status");
                    if (score === undefined) {
                        if (cstatus === "pass" || cstatus === "info") {
                            score = 100;
                        } else if (cstatus === "warn") {
                            score = 50;
                        } else if (cstatus === "fail") {
                            score = 15;
                        }
                    }
                    totalScore += score;
                    count++;
                }
            });
            var score = "";
            if (count > 0) {
                score = Math.floor(totalScore/count);
            }
            if (score >= 75) {
                status = "pass";
            } else if (score >= 45) {
                status = "warn";
            } else {
                status = "fail";
            }

            GovernanceUtil.updateChart($('#'+id+'-chart'), score, status);
        });
    },

    createChart : function(container, label) {
        if ($(container).find(".progress_cont").length === 0) {
            var html = '<div class="progress_cont" data-pct="">';
            html += '<svg class="progress_svg" viewBox="0 0 100 100">';
            html += '<circle stroke-linecap="round"  cx="50" cy="50" r="47" stroke="#eee" stroke-width="6" fill="transparent" stroke-dasharray="315" stroke-dashoffset="0" stroke-mitterlimit="0" transform="rotate(-90 ) translate(-100 0)" />';
            html += '<circle class="bar" stroke-linecap="round"  cx="50" cy="50" r="47" stroke-width="6" fill="transparent" stroke-dasharray="315" stroke-dashoffset="315" stroke-mitterlimit="0" transform="rotate(-90 ) translate(-100 0)" />'
            html += '</svg></div>';
            $(container).html(html);

            if (label !== undefined && label !== "") {
                $(container).append('<div class="progress_label">'+label+'</div>');
            }
        }
    },

    updateChart : function(container, number, status) {
        GovernanceUtil.createChart(container);

        $(container).attr('data-status', status);

        var score = number;
        if (score === "") { score = 0;}
        if (score < 0) { score = 0;}
        if (score > 100) { score = 100;}

        var pct = ((100-score)/100)*315;

        setTimeout(function(){
            $(container).find(".bar").css({ strokeDashoffset: pct});
            $(container).find(".progress_cont").attr('data-pct',number);
        }, 10);
    },
    
    blockUI() {
        $.blockUI({ css: { 
            border: 'none', 
            padding: '15px', 
            backgroundColor: '#000', 
            '-webkit-border-radius': '10px', 
            '-moz-border-radius': '10px', 
            opacity: .3, 
            color: '#fff' 
        }, message : "<h1><i class=\"fas fa-spinner fa-spin\"></i></h1>" });
    }
};

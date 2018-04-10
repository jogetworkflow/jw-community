DiffMerge = {
    merge: function (original, current, latest, outputElement) {
        // instantiate jsondiffpatch
        var instance = jsondiffpatch.create({
            // used to match objects when diffing arrays, by default only === operator is used
            objectHash: function (obj, index) {
                if (typeof obj.properties !== 'undefined' && typeof obj.properties.id !== 'undefined') {
                    return obj.properties.id;
                }
                return '$$index:' + index;
            },
            arrays: {
                // default true, detect items moved inside the array (otherwise they will be registered as remove+add)
                detectMove: true,
                // default false, the value of items moved is not included in deltas
                includeValueOnMove: false
            }
        });
        // compare current remote data with original
        var delta1 = instance.diff(original, current);
        if (outputElement && delta1 !== undefined) {
            var diff1 = jsondiffpatch.formatters.html.format(delta1, original);
            $(outputElement).find("#diff1").remove();
            var html1 = $('<div id="diff1"></div>');
            $(html1).html(diff1);
            outputElement.append(html1);
        }
        // merge diff to latest
        var merged = jsondiffpatch.patch(latest, delta1);
        // get latest diff
        var delta2 = instance.diff(original, merged);
        if (outputElement && delta2 !== undefined) {
            var diff2 = jsondiffpatch.formatters.html.format(delta2, original);
            $(outputElement).find("#diff2").remove();
            var html2 = $('<div id="diff2"></div>');
            $(html2).html(diff2);
            outputElement.append(html2);
        }
        var mergedJson = JSON.encode(merged);
        return mergedJson;
    }
}

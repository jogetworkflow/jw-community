import * as state from "@codemirror/state";
import * as view from "@codemirror/view";
import * as language from "@codemirror/language";
import * as highlight from "@codemirror/highlight";
import * as commands from "@codemirror/commands";
import * as search from "@codemirror/search";
import * as themeOneDark from "@codemirror/theme-one-dark";
import * as langXml from "@codemirror/lang-xml";
import * as langJson from "@codemirror/lang-json";
import * as langHtml from "@codemirror/lang-html";
import * as langCss from "@codemirror/lang-css";
import * as langJs from "@codemirror/lang-javascript";
import * as langJava from "@codemirror/lang-java";
import * as langSql from "@codemirror/lang-sql";
import * as lint from "@codemirror/lint";
import { tags } from "@lezer/highlight";
import { HighlightStyle, syntaxHighlighting } from "@codemirror/language";


/* --- External Linters --- */
import jsonlint from "jsonlint-mod";
import { HTMLHint } from "htmlhint";


/* --- Set Globals --- */
window.jsonlint = jsonlint;
window.HTMLHint = HTMLHint;


var cm = Object.assign({},
    state, view, language, highlight, commands, search, themeOneDark, 
    langXml, langJson, langHtml, langCss, langJs, langJava, langSql, lint, search.SearchCursor, view.Decoration,
    { linter: lint.linter, lintGutter: lint.lintGutter, Compartment: state.Compartment }
);
window.cm = cm;
window.CM6_READY = true;

const mutedHighlight = HighlightStyle.define([
    {
        tag: tags.keyword,
        color: "#7c6db0"
    },
    {
        tag: tags.string,
        color: "#7a9b6a"
    },
    {
        tag: tags.comment,
        color: "#999999",
        fontStyle: "italic"
    },
    {
        tag: tags.number,
        color: "#b07d4f"
    },
    {
        tag: tags.variableName,
        color: "#333333"
    },
    {
        tag: tags.typeName,
        color: "#5d7ea8"
    },
    {
        tag: tags.function(tags.variableName),
        color: "#4f7da5"
    },
    {
        tag: tags.operator,
        color: "#666666"
    },
    {
        tag: tags.propertyName,
        color: "#5c7fa3"
    }
]);

//  Common Editor Logic
var editorInstance = null, cm6FontSize = 13, themeCompartment = new window.cm.Compartment();

// Helper: Setup Linter Errors
var createError = function(msg, from, to) { return { from: from, to: to, severity: "error", message: msg }; };

// MarkText decoration system
var markTextEffect = cm.StateEffect.define();
var markTextExtension = cm.StateField.define({
    create: function() { return cm.Decoration.none; },
    update: function(value, transaction) {
        value = value.map(transaction.changes);
        for (var i = 0; i < transaction.effects.length; i++) {
            var effect = transaction.effects[i];
            if (effect.is(markTextEffect)) {
                var decorations = effect.value;
                if (decorations && decorations.length > 0) {
                    value = value.update({add: decorations, sort: true});
                }
            }
        }
        return value;
    },
    provide: function(f) { return cm.EditorView.decorations.from(f); }
});


// Initialize Editor
window.initCM6Editor = function (elementId, content, mode, isDarkMode, options) {
    // Handle backward compatibility: options can be a function (old onChange) or object
    var opts = typeof options === 'function' ? { onChange: options } : (options || {});
    
    // Handle different types of element selectors
    var parent;
    if (typeof elementId === 'object' && elementId.nodeType === Node.ELEMENT_NODE) {
        // HTML Element
        parent = elementId;
    } else if (typeof elementId === 'string') {
        // String selector - use querySelector for all cases
        parent = document.querySelector(elementId);
        // Backward compatibility: if querySelector fails and it's a plain ID, try getElementById
        if (!parent && !elementId.includes('#') && !elementId.includes('.') && !elementId.includes('[') && !elementId.includes('>')) {
            parent = document.getElementById(elementId);
        }
    }
    
    if (!parent) return null;


    // DESTROY EXISTING
    if (parent.cm6Editor) {
        try { parent.cm6Editor.destroy(); } catch (e) { }
        parent.cm6Editor = null;
    }

    // Reset container
    parent.innerHTML = "";
    parent.classList.add('cm6-container');
    parent.style.cssText = "overflow: auto; width: 100%; height: auto;";
    var builderBody = document.querySelector(".builder-view-body");
    if (builderBody) builderBody.classList.add('cm6-no-padding');


    // Linters
    var jsonLinter = function(v) {
        if (window.jsonlint) {
            try { window.jsonlint.parse(v.state.doc.toString()); }
            catch (e) {
                var check = function(regex) {
                    var m; while ((m = regex.exec(e.message)) !== null) {
                        var l = parseInt(m[1], 10) - 1;
                        if (l >= 0 && l < v.state.doc.lines) return [createError(e.message, v.state.doc.line(l + 1).from, v.state.doc.line(l + 1).to)];
                    }
                    return [];
                };
                var errs = check(/line (\d+)/gi);
                return errs.length ? errs : [createError(e.message, 0, v.state.doc.length)];
            }
        }
        try { JSON.parse(v.state.doc.toString()); return []; }
        catch (e) { return [createError(e.message, 0, v.state.doc.length)]; }
    };
    // xml linter
    var xmlLinter = function(v) {
        var d = new DOMParser().parseFromString(v.state.doc.toString(), "application/xml");
        var e = d.getElementsByTagName("parsererror");
        if (!e.length) return [];
        var msg = e[0].textContent || "XML syntax error";
        var m = /(?:at line|Line:|line\s)(\d+)/i.exec(msg);
        if (m) {
            var l = parseInt(m[1], 10) - 1;
            if (l >= 0 && l < v.state.doc.lines) return [createError(msg, v.state.doc.line(l + 1).from, v.state.doc.line(l + 1).to)];
        }
        return [createError(msg, 0, v.state.doc.length)];
    };
    // html linter
    var htmlLinter = function(v) {
        var t = v.state.doc.toString();
        if (!t.trim()) return [];
        // Only use HTMLHint if it is already loaded in the environment
        if (window.HTMLHint) {
            return window.HTMLHint.verify(t, { "doctype-first": false, "tag-pair": true, "html-tag-required": false, "html-lang-require": false })
                .map(function(e) { return { from: v.state.doc.line(e.line || 1).from, to: v.state.doc.line(e.line || 1).to, severity: e.type === 'error' ? 'error' : 'warning', message: e.message }; });
        }
        var e = new DOMParser().parseFromString(t, "text/html").querySelector('parsererror');
        return e ? [createError(e.textContent || "HTML syntax error", 0, v.state.doc.length)] : [];
    };
    // js linter
    var jsLinter = function(v) {
        try { new Function(v.state.doc.toString()); return []; }
        catch (e) { return [createError(e.message, 0, v.state.doc.length)]; }
    };
    // set language and lint
    var lang, lintExt;
    switch (mode.toLowerCase()) {
        case "json": lang = cm.json(); lintExt = [cm.linter(jsonLinter), cm.lintGutter()]; break;
        case "xml": lang = cm.xml(); lintExt = [cm.linter(xmlLinter), cm.lintGutter()]; break;
        case "html": lang = cm.html({
            autoCloseTags: true,
            matchClosingTags: true
        }); lintExt = [cm.linter(htmlLinter), cm.lintGutter()]; break;
        case "css": lang = cm.css(); lintExt = [cm.lintGutter()]; break;
        case "sql": lang = cm.sql(); lintExt = [cm.lintGutter()]; break;
        case "javascript": case "js": lang = cm.javascript(); lintExt = [cm.linter(jsLinter), cm.lintGutter()]; break;
        case "java": lang = cm.java(); lintExt = [cm.lintGutter()]; break;
        default: lang = cm.xml(); lintExt = [cm.lintGutter()];
    }
    // Use local 'view' variable to avoid closure issues
    var editorView = new cm.EditorView({
        state: cm.EditorState.create({
            doc: content || "",
            extensions: [
                cm.lineNumbers(), cm.highlightActiveLineGutter(), cm.highlightActiveLine(), cm.foldGutter({
                    openText: "▾", closedText: "▸"}), cm.history(), cm.bracketMatching(),
                markTextExtension,
                // Auto-resize extension
                cm.EditorView.updateListener.of(function(v) {
                    if (v.docChanged || v.geometryChanged) {
                        // Force layout recalculation for proper text wrapping
                        setTimeout(function(){
                            v.view.requestMeasure();
                        }, 150)
                    }
                }),
                lang,
                isDarkMode
                ? cm.syntaxHighlighting(cm.oneDarkHighlightStyle)
                : syntaxHighlighting(mutedHighlight),
                themeCompartment.of(cm.EditorView.theme({
                    "&": {
                        fontSize: cm6FontSize + "px",
                        "--cm-font-size": cm6FontSize + "px",
                        lineHeight: "1.5"
                    },
                    ".cm-content": {
                        fontSize: cm6FontSize + "px",
                        lineHeight: "1.5",
                        whiteSpace: "pre-wrap",
                        wordWrap: "break-word",
                        overflowWrap: "break-word"
                    },
                    ".cm-gutter": {
                        fontSize: cm6FontSize + "px",
                        lineHeight: "1.5"
                    },
                    ".cm-lineNumbers": {
                        fontSize: cm6FontSize + "px",
                        lineHeight: "1.5",
                        minWidth: "3em"
                    },
                    ".cm-line": {
                        height: "1.5em"
                    }
                }))
            ].concat(Array.isArray(lintExt) ? lintExt : [lintExt]).concat([
                cm.syntaxHighlighting(cm.oneDarkHighlightStyle, { fallback: true }), cm.search({ top: true }), cm.highlightSelectionMatches(), cm.EditorView.lineWrapping,
                cm.EditorView.editorAttributes.of({ class: isDarkMode ? "cm-theme-dark" : "cm-theme-light" }),
                isDarkMode ? cm.oneDark : [],
                cm.keymap.of([].concat(cm.defaultKeymap).concat(cm.historyKeymap).concat([
                    { key: "Mod-f", run: cm.openSearchPanel, preventDefault: true },
                    { key: "Escape", run: cm.closeSearchPanel },
                    { key: "F3", run: cm.findNext }, { key: "Shift-F3", run: cm.findPrevious }
                ])),
                // Update global editorInstance on focus so helpers work
                cm.EditorView.domEventHandlers({
                    focus: function(event, v) {
                        editorInstance = v;
                        if (v._onFocus) v._onFocus();
                    },
                    blur: function(event, v) {
                        if (v._onBlur) v._onBlur();
                    },
                    keydown: (event, v) => {
                        if (event.key === 'F1' && v.helpPanel) {

                            event.preventDefault();
                            showHelpPanel(v.dom);
                        } else if (event.key === 'F12' && v.enableFullscreen) {
                            event.preventDefault();
                            toggleFullscreen(v)
                        } else if (
                            (event.ctrlKey || event.metaKey) &&
                            (
                                event.key === "=" ||
                                event.key === "+" ||
                                event.key === "-" ||
                                event.key === "_"
                            )
                        ) {
                            if (event.key === "=" || event.key === "+") {
                                setFontSize(v, 2);
                            } else {
                                setFontSize(v, -2);
                            }

                            event.preventDefault();
                        }
                    }
                }),
                cm.EditorView.updateListener.of(function(u) {
                    if (u.view.hasFocus) editorInstance = u.view;


                    var p = u.view.dom.querySelector('.cm-panel.cm-search');
                    if (p) {
                        ['BR', '.cm-match-counter', '.cm-search-info'].forEach(function(s) { p.querySelectorAll(s).forEach(function(e) { e.remove(); }); });
                        
                        // Make search panel draggable using jQuery UI
                        if (!p.classList.contains('cm-draggable')) {
                            p.classList.add('cm-draggable');
                            

                            // Use jQuery UI draggable for better drag functionality
                            $(p).draggable({
                                cursor: 'move',
                                containment: 'window',
                                zIndex: 10000,
                                start: function() {
                                    $(this).css('position', 'absolute');
                                },
                                stop: function() {
                                    // Ensure position remains absolute after drag
                                    $(this).css('position', 'absolute');
                                }
                            });
                            

                            p.title = 'Drag to move search panel';
                        }

                        var ensureRow = function(cls, contentFn) {
                            if (!p.querySelector('.' + cls)) {
                                var r = document.createElement('div'); r.className = cls;
                                contentFn(r); p.appendChild(r);
                            }
                        };
                        // add search and replace buttons
                        ensureRow('cm-search-row1', function(r) {
                            var toggle = r.querySelector('.cm-search-toggle');
                            if (!toggle) {
                                toggle = document.createElement('button');
                                toggle.className = 'cm-search-toggle';
                                toggle.type = 'button';
                                toggle.innerHTML = '&#9660;';
                                toggle.title = 'Show Replace Options';
                                toggle.onclick = function(e) {
                                    e.stopPropagation();
                                    var row2 = p.querySelector('.cm-search-row2');
                                    if (row2) {
                                        var isHidden = row2.style.display === 'none';
                                        row2.style.setProperty('display', isHidden ? 'flex' : 'none', 'important');
                                        toggle.innerHTML = isHidden ? '&#9650;' : '&#9660;';
                                        toggle.title = isHidden ? 'Hide Replace Options' : 'Show Replace Options';
                                    }
                                };
                                r.appendChild(toggle);
                            }
                            var i = p.querySelector('.cm-textfield');
                            if (i) { r.appendChild(i); setTimeout(function() { i.focus(); }, 0); }
                            ['next', 'prev'].forEach(function(n) { var b = p.querySelector('button[name="' + n + '"]'); if (b) r.appendChild(b); });
                            var close = p.querySelector('button[name="close"]');
                            if (close) r.appendChild(close);
                        });
                        // add search and replace buttons
                        ensureRow('cm-search-row2', function(r) {
                            var i = p.querySelector('input[placeholder="Replace"]');
                            if (i) r.appendChild(i);
                            ['replace', 'replaceAll'].forEach(function(n) { var b = p.querySelector('button[name="' + n + '"]'); if (b) r.appendChild(b); });
                            r.style.setProperty('display', 'none', 'important');
                        });


                        Array.from(p.children).forEach(function(c) {
                            if (c.className.indexOf('cm-search-row') === -1 && (c.tagName === 'BUTTON' || c.tagName === 'INPUT')) c.remove();
                        });


                        var searchField = p.querySelector('.cm-search-row1 .cm-textfield');
                        var term = searchField ? searchField.value : '';
                        var r3 = document.createElement('div'); r3.className = 'cm-search-row3';
                        if (!term) r3.innerHTML = '<div class="cm-search-info">(Use /re/ syntax for regexp search)</div>';
                        else {
                            var matchCount = 0; try { matchCount = (u.view.state.doc.toString().match(new RegExp(term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi')) || []).length; } catch (e) { }
                            r3.innerHTML = '<div class="cm-match-counter">' + matchCount + ' match' + (matchCount === 1 ? '' : 'es') + ' found.</div>';
                        }
                        // remove old row and add new row
                        var old = p.querySelector('.cm-search-row3'); if (old) old.remove();
                        p.appendChild(r3);
                    }
                    // update content
                    if (u.docChanged) {
                        try { if (editorView._onChange) editorView._onChange(); } catch (e) { }
                        if (opts.onChange) opts.onChange(editorView.state.doc.toString());
                    }
                })
            ])
        }),
        parent: parent
    });


    // Attach methods
    editorView.getValue = function() { return editorView.state.doc.toString(); };
    editorView.setValue = function(v) { editorView.dispatch({ changes: { from: 0, to: editorView.state.doc.length, insert: v || "" } }); };
    editorView.on = function(e, cb) {
        if (e === "change") editorView._onChange = cb;
        if (e === "focus") editorView._onFocus = cb;
        if (e === "blur") editorView._onBlur = cb;
    };
    editorView.getCursor = function() { 
        var selection = editorView.state.selection;
        return {
            line: editorView.state.doc.lineAt(selection.main.from).number,
            ch: selection.main.from - editorView.state.doc.lineAt(selection.main.from).from,
            anchor: selection.main.anchor,
            head: selection.main.head
        };
    };
    editorView.getSearchCursor = function(pattern, startPos, endPos) {
        var doc = editorView.state.doc;
        
        // Handle pattern as string or RegExp
        var searchPattern;
        if (typeof pattern === 'string') {
            searchPattern = pattern;
        } else if (pattern instanceof RegExp) {
            searchPattern = pattern;
        } else {
            throw new Error('Pattern must be string or RegExp');
        }
        
        // Default start position
        var start = startPos || {line: 1, ch: 0};
        // Ensure line number is valid (1-based)
        var lineNumber = Math.max(1, Math.min(start.line, doc.lines));
        var startIndex = doc.line(lineNumber).from + Math.max(0, start.ch);
        
        // Default end position (optional)
        var endIndex;
        if (endPos) {
            var endLineNumber = Math.max(1, Math.min(endPos.line, doc.lines));
            endIndex = doc.line(endLineNumber).from + Math.max(0, endPos.ch);
        } else {
            endIndex = doc.length; // Search to end of document
        }
        
        // Return new SearchCursor with correct constructor parameters
        return new cm.SearchCursor(doc, searchPattern, startIndex, endIndex);
    }
    editorView.markText = function(from, to, options) {
        var doc = editorView.state.doc;
        
        // Convert line/ch positions to document positions if needed
        var fromIndex, toIndex;
        
        if (typeof from === 'object' && from.line !== undefined) {
            // Line/ch object format
            fromIndex = doc.line(from.line).from + Math.max(0, from.ch);
        } else {
            // Direct document position
            fromIndex = from;
        }
        
        if (typeof to === 'object' && to.line !== undefined) {
            // Line/ch object format
            toIndex = doc.line(to.line).from + Math.max(0, to.ch);
        } else {
            // Direct document position
            toIndex = to;
        }
        
        // Validate positions
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= doc.length || toIndex > doc.length) {
            throw new Error('Invalid positions for markText');
        }
        
        // Ensure from <= to
        if (fromIndex > toIndex) {
            var temp = fromIndex;
            fromIndex = toIndex;
            toIndex = temp;
        }
        
        // Ensure range is not empty
        if (fromIndex === toIndex) {
            return null; // Empty range, return null instead of error
        }
        
        editorView.dispatch({ effects: markTextEffect.of([
            cm.Decoration.mark({
                class: options['className'] || ''
            }).range(fromIndex, toIndex)
        ]) })
        
        return {
            from: fromIndex,
            to: toIndex,
            clear: function() {
                editorView.dispatch({ effects: markTextEffect.of([]) });
            }
        };
    };
    editorView.scrollIntoView = function(pos) {
        var doc = editorView.state.doc;
        var targetPos;
        
        if (typeof pos === 'object' && pos.line !== undefined) {
            // Line/ch object format
            targetPos = doc.line(pos.line).from + Math.max(0, pos.ch);
        } else if (typeof pos === 'number') {
            // Direct document position
            targetPos = pos;
        } else {
            throw new Error('Invalid position for scrollIntoView');
        }
        
        // Scroll to the position
        editorView.dispatch({
            effects: cm.EditorView.scrollIntoView(targetPos)
        });
    };
    editorView.refresh = function() {editorView.requestMeasure()}
    editorView.getDoc = function() {
        return {
            replaceRange: function(value, from) {
                var fromPos = typeof from === 'object' ? from : {line: 1, ch: 0};
                var doc = editorView.state.doc;
                var fromIndex = doc.line(fromPos.line).from + fromPos.ch;
                editorView.dispatch({
                    changes: {
                        from: fromIndex,
                        to: fromIndex,
                        insert: value || ""
                    }
                });
            },
            getValue: function() {
                return editorView.state.doc.toString();
            },
            getCursor: function() {
                return editorView.getCursor();
            }
        };
    };
    
    // Attach to parent for multi-instance management
    parent.CodeMirror = editorView;
    // Update global instance reference but DO NOT DESTROY previous one globally
    editorInstance = editorView;
    window.cm6Editor = editorInstance;
    return editorView;
};
// get and set content
window.getCM6Content = function() { return editorInstance ? editorInstance.state.doc.toString() : ""; };
window.setCM6Content = function(v) { if (editorInstance) editorInstance.dispatch({ changes: { from: 0, to: editorInstance.state.doc.length, insert: v || "" } }); };


// Functionality Helpers
var showHelpPanel = function(editor) {
    if (!editor) return;

    if (editor.closest('.cm6-container').classList.contains('cm6-maximized')) return;

    // check context
    var label = null, p = editor.parentElement;
    while (p && !label) {
        var lbs = p.querySelectorAll('.property-label');
        for (var idx = 0; idx < lbs.length; idx++) {
            if (lbs[idx].compareDocumentPosition(editor) & Node.DOCUMENT_POSITION_FOLLOWING) { label = lbs[idx]; break; }
        }
        p = p.parentElement;
    }
    
    var panel = document.getElementById('cm6-help-panel');
    if (panel) { panel.remove(); return; }
    // List of keys to try, prioritizing the existing peditor key
    var keys = ['peditor.codemirror6.helpMessage', 'console.codemirror6.helpMessage'];
    var defaultMsg = 'F1 to Help Panel | F12 to Fullscreen | Ctrl+F to Find and Replace | Ctrl + =/Ctrl + - to Font Size';
    var msg = defaultMsg;
    // Helper to try all available message loaders
    var resolveMsg = function(k) {
        var loaders = ['get_cbuilder_msg', 'get_advtool_msg', 'get_peditor_msg', 'get_console_msg'];
        for (var li = 0; li < loaders.length; li++) {
            try {
                if (typeof window[loaders[li]] === 'function') {
                    var res = window[loaders[li]](k);
                    if (res && res !== k && res.trim().indexOf('?') !== 0 && res.indexOf('codemirror.helpMessage') === -1) {
                        return res;
                    }
                }
            } catch (e) { console.error(e); }
        }
        return null;
    };
    // Iterate through keys until a translation is found
    for (var ki = 0; ki < keys.length; ki++) {
        var found = resolveMsg(keys[ki]);
        if (found) { msg = found; break; }
    }

    var editorLabel = resolveMsg("peditor.codemirror6.editor") || resolveMsg("console.codemirror6.editor") || "EDITOR";

    var shortcutsLabel = resolveMsg("peditor.codemirror6.shortcuts") || resolveMsg("console.codemirror6.shortcuts") || "Shortcuts";
    
    // split msg into parts
    var parts = msg.split('|');
    var finalParts = [];
    for (var pi = 0; pi < 4; pi++) { finalParts.push(parts[pi] ? parts[pi].trim() : ""); }
    panel = document.createElement('div');
    panel.id = 'cm6-help-panel';
    panel.innerHTML = `<div class="cm6-help-title">
                            <span class="cm6-help-icon">⌨</span>
                            <div>
                                <small>`+ editorLabel + `</small>
                                <strong>` + shortcutsLabel + `</strong>
                            </div>
                        </div>
                        <div class="cm6-shortcut">
                            <kbd>` + finalParts[0].split(' to ')[0] + `</kbd>
                            <span>` + finalParts[0].split(' to ')[1] + `</span>
                        </div>

                        <div class="cm6-shortcut">
                            <kbd>` + finalParts[1].split(' to ')[0] + `</kbd>
                            <span>` + finalParts[1].split(' to ')[1] + `</span>
                        </div>

                        <div class="cm6-shortcut">
                            <kbd>Ctrl</kbd>
                            <span>+</span>
                            <kbd>F</kbd>
                            <span>` + finalParts[2].split(' to ')[1] + `</span>
                        </div>

                        <div class="cm6-shortcut">
                            <kbd>Ctrl</kbd>
                            <span>+</span>
                            <kbd>=</kbd>
                            <span>/</span>
                            <kbd>Ctrl</kbd>
                            <span>+</span>
                            <kbd>-</kbd>
                            <span>` + finalParts[3].split(' to ')[1] + `</span>
                        </div>

                        <button id="cm6-help-close">&times;</button>
                        </div>
                        `;


    if (editor.parentNode) editor.parentNode.insertBefore(panel, editor);
    else { panel.classList.add('cm6-help-panel-fixed'); document.body.appendChild(panel); }
    document.getElementById('cm6-help-close').onclick = function() { panel.remove(); };
};

// set font size
var setFontSize = function(editorInstance, d) {
    if (editorInstance) {
        cm6FontSize = Math.max(10, Math.min(32, cm6FontSize + d));
        var newFontSize = cm6FontSize + 'px';

        editorInstance.dom.closest('.cm6-container').style.setProperty('--CMfont-size', newFontSize);
        
        // Reconfigure the theme compartment
        editorInstance.dispatch({
            effects: themeCompartment.reconfigure(
                cm.EditorView.theme({
                    "&": {
                        fontSize: newFontSize,
                        "--cm-font-size": newFontSize,
                        lineHeight: "1.5"
                    },
                    ".cm-content": {
                        fontSize: newFontSize,
                        lineHeight: "1.5"
                    },
                    ".cm-gutter": {
                        fontSize: newFontSize,
                        lineHeight: "1.5"
                    },
                    ".cm-lineNumbers": {
                        fontSize: newFontSize,
                        lineHeight: "1.5",
                        minWidth: "3em"
                    },
                    ".cm-line": {
                        height: "1.5em"
                    }
                })
            )
        });
    }
};

// toggle fullscreen
var toggleFullscreen = function(editorView) {
    if (editorView && editorView.dom) {
        var hp = editorView.dom.closest('.code-editor').querySelector('#cm6-help-panel'); if (hp) hp.remove();
        editorView.dom.closest('.code-editor').classList.toggle('cm6-maximized');
    }
};


// Event Listeners
document.addEventListener("mouseover", function(e) {
    if (e.target && e.target.matches && e.target.matches('.cm-search button[name="replace"]')) { e.target.title = "Replace"; e.target.setAttribute('aria-label', 'Replace'); }
    if (e.target && e.target.matches && e.target.matches('.cm-search button[name="replaceAll"]')) { e.target.title = "Replace all"; e.target.setAttribute('aria-label', 'Replace all'); }
}, true);


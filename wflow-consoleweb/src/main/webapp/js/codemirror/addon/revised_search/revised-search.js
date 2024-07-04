// CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: http://codemirror.net/LICENSE

// Revised search plugin written by Jamie Morris
// Define search commands. Depends on advanceddialog.js
(function (mod) {
  if ((typeof exports === "undefined" ? "undefined" : _typeof(exports)) == "object" && (typeof module === "undefined" ? "undefined" : _typeof(module)) == "object") // CommonJS
    mod(require("codemirror"), require("codemirror-advanceddialog"));else if (typeof define == "function" && define.amd) // AMD
    define(["codemirror", "codemirror-advanceddialog"], mod);else // Plain browser env
    mod(CodeMirror);
})(function (CodeMirror) {
  "use strict";

  var replaceDialog = `<button id="collapse-button" type="button" class="btn btn-light" style="height:100%;position:absolute;top:0;width:18px;padding:0"><i class="fas fa-chevron-down"></i></button> <div class="row find" style="margin-top:2px;display: block;align-items: center;flex-wrap:nowrap;flex-direction:column;height: 28px;"> <div class="find-part"> <div id="find-input"> <input id="CodeMirror-find-field" type="text" class="CodeMirror-search-field" placeholder="Find" style="flex: 1;"/> </div> <div class="find-actions"><button type="button" class="btn btn-light" title="Find Previous"><i class="fas fa-arrow-up"></i></button> <button type="button" class="btn btn-light" title="Find Next"><i class="fas fa-arrow-down"></i></button> <button type="button" class="btn btn-light" id="closeDialogButton" title="Close"><i class="fa fa-times"></i></button> </div> </div> <span class="CodeMirror-search-hint" style="font-size: 10px; color: #888; font-weight:400">(Use /re/ syntax for regexp search)</span> </div> <div class="row replace" style="align-items: center;flex-flow: column;height: 28px;display: none;top:41px;margin-top:0px"> <div class="find-part"> <div id="find-input"> <input id="CodeMirror-replace-field" type="text" class="CodeMirror-search-field" placeholder="Replace" style="flex:1"/> </div> <div class="find-actions"> <button type="button" class="btn btn-light" title="Replace"><i class="fas fa-reply"></i></button> <button type="button" class="btn btn-light" title="Replace All"><i class="fas fa-reply-all"></i></button> </div> </div> </div>`; 
  
  var findDialog = `<div style="display: flex; align-items: center; width:100%;"> <div class="height:100%;"> <button class="btn btn-light" style="margin-right: 5px;height:100%">></button> </div> <div class="row find" style="display: flex; align-items: center;flex-wrap:nowrap;flex-direction:column;height:50px"> <div class="find-part" style="display:flex;font-size:12px"> <div id="find-input" style="position:relative;display:flex;flex:1;"> <input id="CodeMirror-find-field" type="text" class="CodeMirror-search-field" placeholder="Find" style="flex: 1;"/> </div> <div class="find-actions"> <span class="CodeMirror-search-count" style="font-weight: bold;min-width:120px;">0/0</span> <button class="btn btn-light" style="margin-right: 5px;"><i class="fa-solid fa-arrow-up"></i></button> <button class="btn btn-light" style="margin-right: 5px;"><i class="fa-solid fa-arrow-down"></i></button> <button class="btn btn-light"><i class="fa fa-times"></i></button> </div> </div> <span class="CodeMirror-search-hint" style="font-size: 0.9em; color: #888;">(Use /re/ syntax for regexp search)</span> </div> </div> `; 
    
  var numMatches = 0;
  var searchOverlay = function searchOverlay(query, caseInsensitive) {
    if (typeof query == "string") query = new RegExp(query.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"), caseInsensitive ? "gi" : "g");else if (!query.global) query = new RegExp(query.source, query.ignoreCase ? "gi" : "g");

    return {
      token: function token(stream) {
        query.lastIndex = stream.pos;
        var match = query.exec(stream.string);
        if (match && match.index == stream.pos) {
          stream.pos += match[0].length || 1;
          return "searching";
        } else if (match) {
          stream.pos = match.index;
        } else {
          stream.skipToEnd();
        }
      }
    };
  };

  function SearchState() {
    this.posFrom = this.posTo = this.lastQuery = this.query = null;
    this.overlay = null;
  }

  var getSearchState = function getSearchState(cm) {
    return cm.state.search || (cm.state.search = new SearchState());
  };

  var queryCaseInsensitive = function queryCaseInsensitive(query) {
    return typeof query == "string" && query == query.toLowerCase();
  };

  var getSearchCursor = function getSearchCursor(cm, query, pos) {
    // Heuristic: if the query string is all lowercase, do a case insensitive search.
    return cm.getSearchCursor(parseQuery(query), pos, queryCaseInsensitive(query));
  };

  var parseString = function parseString(string) {
    return string.replace(/\\(.)/g, function (_, ch) {
      if (ch == "n") return "\n";
      if (ch == "r") return "\r";
      return ch;
    });
  };

  var parseQuery = function parseQuery(query) {
    if (query.exec) {
      return query;
    }
    var isRE = query.indexOf('/') === 0 && query.lastIndexOf('/') > 0;
    if (!!isRE) {
      try {
        var matches = query.match(/^\/(.*)\/([a-z]*)$/);
        query = new RegExp(matches[1], matches[2].indexOf("i") == -1 ? "" : "i");
      } catch (e) {} // Not a regular expression after all, do a string search
    } else {
      query = parseString(query);
    }
    if (typeof query == "string" ? query == "" : query.test("")) query = /x^/;
    return query;
  };

  var startSearch = function startSearch(cm, state, query) {
    if (!query || query === '') return;
    state.queryText = query;
    state.query = parseQuery(query);
    cm.removeOverlay(state.overlay, queryCaseInsensitive(state.query));
    state.overlay = searchOverlay(state.query, queryCaseInsensitive(state.query));
    cm.addOverlay(state.overlay);
    if (cm.showMatchesOnScrollbar) {
      if (state.annotate) {
        state.annotate.clear();
        state.annotate = null;
      }
      state.annotate = cm.showMatchesOnScrollbar(state.query, queryCaseInsensitive(state.query));
    }
  };

  var doSearch = function doSearch(cm, query, reverse, moveToNext) {
    var hiding = null;
    var state = getSearchState(cm);
    if (query != state.queryText) {
      startSearch(cm, state, query);
      state.posFrom = state.posTo = cm.getCursor();
    }
    if (moveToNext || moveToNext === undefined) {
      findNext(cm, reverse || false);
    }
    updateCount(cm);
  };

  var clearSearch = function clearSearch(cm) {  
    cm.operation(function () {
      var state = getSearchState(cm);
      state.lastQuery = state.query;
      if (!state.query) return;
      state.query = state.queryText = null;
      cm.removeOverlay(state.overlay);
      if (state.annotate) {
        state.annotate.clear();
        state.annotate = null;
      }
    });
  };

  var findNext = function findNext(cm, reverse, callback) {
    cm.operation(function () {
      var state = getSearchState(cm);
      var cursor = getSearchCursor(cm, state.query, reverse ? state.posFrom : state.posTo);
      if (!cursor.find(reverse)) {
        cursor = getSearchCursor(cm, state.query, reverse ? CodeMirror.Pos(cm.lastLine()) : CodeMirror.Pos(cm.firstLine(), 0));
        if (!cursor.find(reverse)) return;
      }
      cm.setSelection(cursor.from(), cursor.to());
      cm.scrollIntoView({
        from: cursor.from(),
        to: cursor.to()
      }, 20);
      state.posFrom = cursor.from();
      state.posTo = cursor.to();
      if (callback) callback(cursor.from(), cursor.to());
    });
  };

  var replaceNext = function replaceNext(cm, query, text) {
    var cursor = getSearchCursor(cm, query, cm.getCursor('from'));
    var start = cursor.from();
    var match = cursor.findNext();
    if (!match) {
      cursor = getSearchCursor(cm, query);
      match = cursor.findNext();
      if (!match || start && cursor.from().line === start.line && cursor.from().ch === start.ch) return;
    }
    cm.setSelection(cursor.from(), cursor.to());
    cm.scrollIntoView({
      from: cursor.from(),
      to: cursor.to()
    });
    cursor.replace(typeof query === 'string' ? text : text.replace(/\$(\d)/g, function (_, i) {
      return match[i];
    }));
  };

  var replaceAll = function replaceAll(cm, query, text) {
    cm.operation(function () {
      for (var cursor = getSearchCursor(cm, query); cursor.findNext();) {
        if (typeof query != "string") {
          var match = cm.getRange(cursor.from(), cursor.to()).match(query);
          cursor.replace(text.replace(/\$(\d)/g, function (_, i) {
            return match[i];
          }));
        } else cursor.replace(text);
      }
    });
  };

  var closeSearchCallback = function closeSearchCallback(cm, state) {
    if (state.annotate) {
      state.annotate.clear();
      state.annotate = null;
    }
    clearSearch(cm);
  };

  var getOnReadOnlyCallback = function getOnReadOnlyCallback(callback) {
    var closeFindDialogOnReadOnly = function closeFindDialogOnReadOnly(cm, opt) {
      if (opt === 'readOnly' && !!cm.getOption('readOnly')) {
        callback();
        cm.off('optionChange', closeFindDialogOnReadOnly);
      }
    };
    return closeFindDialogOnReadOnly;
  };

  var updateCount = function updateCount(cm) {
    var state = getSearchState(cm);
    var value = cm.getDoc().getValue();
    var globalQuery = void 0;
    var queryText = state.queryText;

    if (!queryText || queryText === '') {
      resetCount(cm);
      return;
    }

    while (queryText.charAt(queryText.length - 1) === '\\') {
      queryText = queryText.substring(0, queryText.lastIndexOf('\\'));
    }

    if (typeof state.query === 'string') {
      globalQuery = new RegExp(queryText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'ig');
    } else {
      globalQuery = new RegExp(state.query.source, state.query.flags + 'g');
    }

    var matches = value.match(globalQuery);
    var count = matches ? matches.length : 0;

    var countText = count === 1 ? '1 match found.' : count + ' matches found.';
    cm.getWrapperElement().parentNode.querySelector('.CodeMirror-search-hint').innerHTML = countText;
  };

  var resetCount = function resetCount(cm) {
    cm.getWrapperElement().parentNode.querySelector('.CodeMirror-search-hint').innerHTML = '(Use /re/ syntax for regexp search)';
  };

  var openCloseReplace = function openCloseReplace(cm) {
    return {
        callback: function callback(inputs) {
          if (cm.getWrapperElement().parentNode.querySelector('.row.replace').style.display == "block") {
              cm.getWrapperElement().parentNode.querySelector('.row.replace').style.display = "none"
              cm.getWrapperElement().parentNode.querySelector('.CodeMirror-advanced-dialog').style.height = "40px"
              cm.getWrapperElement().parentNode.querySelector('#collapse-button').innerHTML = "<i class=\"fas fa-chevron-down\"></i>"
          }
          else {
              cm.getWrapperElement().parentNode.querySelector('.row.replace').style.display = "block"
              cm.getWrapperElement().parentNode.querySelector('.CodeMirror-advanced-dialog').style.height = "66px"
              cm.getWrapperElement().parentNode.querySelector('#collapse-button').innerHTML = "<i class=\"fas fa-chevron-up\"></i>"
          }
      }
    }
}

  var getFindBehaviour = function getFindBehaviour(cm, defaultText, callback) {
    if (!defaultText) {
      defaultText = '';
    }
    var behaviour = {
      value: defaultText,
      focus: true,
      selectValueOnOpen: true,
      closeOnEnter: false,
      closeOnBlur: false,
      callback: function callback(inputs, e) {
        var query = inputs[0].value;
        if (!query) return;
        doSearch(cm, query, !!e.shiftKey);
      },
      onInput: function onInput(inputs, e) {
        var query = inputs[0].value;
        if (!query) {
          resetCount(cm);
          clearSearch(cm);
          return;
        };
        doSearch(cm, query, !!e.shiftKey, false);
      }
    };
    if (!!callback) {
      behaviour.callback = callback;
    }
    return behaviour;
  };

  var getFindPrevBtnBehaviour = function getFindPrevBtnBehaviour(cm) {
    return {
      callback: function callback(inputs) {
        var query = inputs[0].value;
        if (!query) return;
        doSearch(cm, query, true);
      }
    };
  };

  var getFindNextBtnBehaviour = function getFindNextBtnBehaviour(cm) {
    return {
      callback: function callback(inputs) {
        var query = inputs[0].value;
        if (!query) return;
        doSearch(cm, query, false);
      }
    };
  };

  var closeBtnBehaviour = function closeBtnBehaviour(cm) {
    return {
        callback: function callback(inputs) {
            cm.getWrapperElement().parentNode.querySelector(".CodeMirror-advanced-dialog").style.display = 'none';
            clearSearch(cm);
        }
    }
  };

  CodeMirror.commands.find = function (cm) {
    if (cm.getOption("readOnly")) return;
    clearSearch(cm);
    var state = getSearchState(cm);
    var query = cm.getSelection() || getSearchState(cm).lastQuery;
    var closeDialog = cm.openAdvancedDialog(findDialog, {
      shrinkEditor: true,
      inputBehaviours: [getFindBehaviour(cm, query)],
      buttonBehaviours: [getReplaceBtnBehaviour(cm), getFindPrevBtnBehaviour(cm), getFindNextBtnBehaviour(cm), closeBtnBehaviour(cm)],
      onClose: function onClose() {
        closeSearchCallback(cm, state);
      }
    });

    cm.on("optionChange", getOnReadOnlyCallback(closeDialog));
    startSearch(cm, state, query);
    updateCount(cm);
  };

  function getReplaceBtnBehaviour(cm) {
    return {
      text: "Replace",
      callback: function() {
        CodeMirror.commands.replace(cm);
      }
    };
  }

  CodeMirror.commands.replace = function (cm, all) {
    if (cm.getOption("readOnly")) return;
    clearSearch(cm);

    var replaceNextCallback = function replaceNextCallback(inputs) {
      var query = parseQuery(inputs[0].value);
      var text = parseString(inputs[1].value);
      if (!query) return;
      replaceNext(cm, query, text);
      doSearch(cm, query);
    };

    var state = getSearchState(cm);
    var query = cm.getSelection() || state.lastQuery;
    var closeDialog = cm.openAdvancedDialog(replaceDialog, {
      shrinkEditor: true,
      inputBehaviours: [getFindBehaviour(cm, query, function (inputs) {
        inputs[1].focus();
        inputs[1].select();
      }), {
        closeOnEnter: false,
        closeOnBlur: false,
        callback: replaceNextCallback
      }],
      buttonBehaviours: [openCloseReplace(cm), getFindPrevBtnBehaviour(cm), getFindNextBtnBehaviour(cm), closeBtnBehaviour(cm),
        {
        callback: replaceNextCallback
      }, {
        callback: function callback(inputs) {
          // Replace all
          var query = parseQuery(inputs[0].value);
          var text = parseString(inputs[1].value);
          if (!query) return;
          replaceAll(cm, query, text);
          updateCount(cm);
        }
      }],
      onClose: function onClose() {
        closeSearchCallback(cm, state);
      }
    });

    cm.on("optionChange", getOnReadOnlyCallback(closeDialog));
    startSearch(cm, state, query);
    updateCount(cm);
  };
});
var UndoManager = function () {
    "use strict";

    var undoCommands = [],
		index = -1,
		isExecuting = false,
		callback;

	function execute(command, action) {
		if (!command) {
			return this;
		}
		isExecuting = true;
		
		command[action]();
		isExecuting = false;
		return this;
	}
    
    return {

        // legacy support
        
        register: function (undoObj, undoFunc, undoParamsList, undoMsg, redoObj, redoFunc, redoParamsList, redoMsg) {
            this.add({
                undo: function() {
                    undoFunc.apply(undoObj, undoParamsList);
                },
                redo: function() {
                    redoFunc.apply(redoObj, redoParamsList);
                }
            });
        },
        
		add: function (command) {
			if (isExecuting) {
				return this;
			}
			// if we are here after having called undo,
			// invalidate items higher on the stack
			undoCommands.splice(index + 1, undoCommands.length - index);

			undoCommands.push(command);

			// set the current index to the end
			index = undoCommands.length - 1;
			if (callback) {
				callback();
			}
			return this;
		},

		/*
		Pass a function to be called on undo and redo actions.
		*/
		setCallback: function (callbackFunc) {
			callback = callbackFunc;
		},

		undo: function () {
			var command = undoCommands[index];
			if (!command) {
				return this;
			}
			execute(command, "undo");
			index -= 1;
			if (callback) {
				callback();
			}
			return this;
		},

		redo: function () {
			var command = undoCommands[index + 1];
			if (!command) {
				return this;
			}
			execute(command, "redo");
			index += 1;
			if (callback) {
				callback();
			}
			return this;
		},

		/*
		Clears the memory, losing all stored states.
		*/
		clear: function () {
			var prev_size = undoCommands.length;

			undoCommands = [];
			index = -1;

			if ( callback && ( prev_size > 0 ) ) {
				callback();
			}
		},

		hasUndo: function () {
			return index !== -1;
		},

		hasRedo: function () {
			return index < (undoCommands.length - 1);
		},
		
		getCommands: function () {
			return undoCommands;
		}		
	};
};

/*
LICENSE

The MIT License

Copyright (c) 2010-2013 Arthur Clemens, arthur@visiblearea.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions: 

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
class htmlEditButton {
  constructor(quill, options) {
    // Add button to all quill toolbar instances
    quill.container.parentElement
      .querySelectorAll(".ql-toolbar")
      .forEach(toolbarEl => {
        const buttonContainer = document.createElement("span");
        buttonContainer.setAttribute("class", "ql-formats");
        const button = document.createElement("button");
        button.innerHTML = "&lt;&gt;";
        button.title = "Show HTML source";
        button.onclick = function(e) {
          e.preventDefault();
          launchPopupEditor(quill);
        };
        buttonContainer.appendChild(button);
        toolbarEl.appendChild(buttonContainer);
      });
  }
}

function launchPopupEditor(quill) {
  const htmlFromEditor = quill.container.querySelector(".ql-editor").innerHTML;
  const popupContainer = document.createElement("div");
  const overlayContainer = document.createElement("div");
  overlayContainer.setAttribute(
    "style",
    "background: #0000007d; position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 9999;"
  );
  popupContainer.setAttribute(
    "style",
    "background: #ddd; position: absolute; top: 5%; left: 5%; right: 5%; bottom: 5%; border-radius: 10px;"
  );
  const title = document.createElement("i");
  title.setAttribute("style", "margin: 0; display: block;");
  title.innerText =
    'Edit HTML here, when you click "OK" the quill editor\'s contents will be replaced';
  const textContainer = document.createElement("div");
  textContainer.appendChild(title);
  textContainer.setAttribute(
    "style",
    "position: relative; width: calc(100% - 40px); height: calc(100% - 40px); padding: 20px;"
  );
  const textArea = document.createElement("textarea");
  textArea.setAttribute(
    "style",
    "position: absolute; left:15px; width: calc(100% - 45px); height: calc(100% - 116px);"
  );
  textArea.value = formatHTML(htmlFromEditor);
  const buttonCancel = document.createElement("button");
  buttonCancel.innerHTML = "Cancel";
  buttonCancel.setAttribute("style", "margin-right: 20px;");
  const buttonOk = document.createElement("button");
  buttonOk.innerHTML = "Ok";
  const buttonGroup = document.createElement("div");
  buttonGroup.setAttribute(
    "style",
    "position: absolute; bottom: 20px; transform: scale(1.5); left: calc(50% - 60px)"
  );
  buttonGroup.appendChild(buttonCancel);
  buttonGroup.appendChild(buttonOk);
  textContainer.appendChild(textArea);
  textContainer.appendChild(buttonGroup);
  popupContainer.appendChild(textContainer);
  overlayContainer.appendChild(popupContainer);
  document.body.appendChild(overlayContainer);

  buttonCancel.onclick = function() {
    document.body.removeChild(overlayContainer);
  };
  overlayContainer.onclick = buttonCancel.onclick;
  popupContainer.onclick = function(e) {
    e.preventDefault();
    e.stopPropagation();
  };
  buttonOk.onclick = function() {
    const output = textArea.value.split(/\r?\n/g).map(el => el.trim());
    const noNewlines = output.join("");
    quill.container.querySelector(".ql-editor").innerHTML = noNewlines;
    document.body.removeChild(overlayContainer);
  };
}

// Adapted FROM jsfiddle here: https://jsfiddle.net/buksy/rxucg1gd/
function formatHTML(code) {
  "use strict";
  let stripWhiteSpaces = true;
  let stripEmptyLines = true;
  const whitespace = " ".repeat(2); // Default indenting 4 whitespaces
  let currentIndent = 0;
  const newlineChar = "\n";
  let char = null;
  let nextChar = null;

  let result = "";
  for (let pos = 0; pos <= code.length; pos++) {
    char = code.substr(pos, 1);
    nextChar = code.substr(pos + 1, 1);

    const isBrTag = code.substr(pos, 4) === "<br>";
    const isOpeningTag = char === "<" && nextChar !== "/" && !isBrTag;
    const isClosingTag = char === "<" && nextChar === "/" && !isBrTag;
    if (isBrTag) {
      // If opening tag, add newline character and indention
      result += newlineChar;
      currentIndent--;
      pos += 4;
    }
    if (isOpeningTag) {
      // If opening tag, add newline character and indention
      result += newlineChar + whitespace.repeat(currentIndent);
      currentIndent++;
    }
    // if Closing tag, add newline and indention
    else if (isClosingTag) {
      // If there're more closing tags than opening
      if (--currentIndent < 0) currentIndent = 0;
      result += newlineChar + whitespace.repeat(currentIndent);
    }

    // remove multiple whitespaces
    else if (stripWhiteSpaces === true && char === " " && nextChar === " ")
      char = "";
    // remove empty lines
    else if (stripEmptyLines === true && char === newlineChar) {
      //debugger;
      if (code.substr(pos, code.substr(pos).indexOf("<")).trim() === "")
        char = "";
    }

    result += char;
  }
  console.log("formatHTML", {
    before: code,
    after: result
  });
  return result;
}

window.htmlEditButton = htmlEditButton;
export default htmlEditButton;
export { htmlEditButton };

/**
 * AJAX call management
 */

function makeCall(method, url, formElement, cback, reset = true) {
    var req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function() {
        cback(req)
    }; // closure
    req.open(method, url);
    if (formElement == null) {
        req.send();
    } else {
        req.send(new FormData(formElement));
    }
    if (formElement !== null && reset === true) {
        formElement.reset();
    }
}
//updates form in input with other elements in input: updates all fields
function completeForm(username=null, folderName=null, subFolderName=null, documentName=null, type=null, form){
    var input;
    const names = ["username", "folderName", "subFolderName", "documentName","type" ];
    for(var i=0; i<5; i++){
        input = document.createElement("input");
        input.setAttribute("value", arguments[i] != undefined ? arguments[i] : null);
        input.setAttribute("name", arguments[i] != undefined ? names[i] : null);
        form.appendChild(input);
    }
}

function completeFormForMove(folderName, subFolderName, documentName, type, folderTarget, subFolderTarget, form){
    var input;
    const names = ["folderName", "subFolderName", "documentName","documentType","folderTarget","subFolderTarget" ];
    for(var i=0; i<7; i++){
        input = document.createElement("input");
        input.setAttribute("value",  arguments[i] );
        input.setAttribute("name",  names[i] );
        form.appendChild(input);
    }

}

function compleFormPartial(username=null, folderName=null, subFolderName=null, documentName=null, type=null, output, form){
    var input;
    const names = ["username", "folderName", "subFolderName", "documentName","type" ];
    for(var i=0; i<5; i++) {
        if (form.querySelectorAll("p>input[name=" + names[i] + "]")[0]==undefined || form.querySelectorAll("input[name=" + names[i] + "]")[0].getAttribute("value") == undefined) {
            input = document.createElement("input");
            input.setAttribute("value", arguments[i] != undefined ? arguments[i] : null);
            input.setAttribute("name", arguments[i] != undefined ? names[i] : null);
            output.appendChild(input);
        }
    }
}

function createSubFolderForm(folderName, subFolderName, form){
    var input;
    input = document.createElement("input");
    input.setAttribute("value", folderName);
    input.setAttribute("name", "folderName");
    form.appendChild(input);
    input = document.createElement("input");
    input.setAttribute("value", subFolderName);
    input.setAttribute("name", "subFolderName");
    form.appendChild(input);
}

function createDocumentForm(folderName, subFolderName, documentName, summury, body, form){
    var input;
    const names = ['folderName', 'subFolderName', 'documentName', 'summury'];
    for(var i =0; i<5; i++){
        input = document.createElement("input");
        input.setAttribute("value", arguments[i]);
        input.setAttribute("name", names[i]);
        form.appendChild(input);
    }
    //for body element
    input = body.cloneNode(true);
    form.appendChild(input);
}

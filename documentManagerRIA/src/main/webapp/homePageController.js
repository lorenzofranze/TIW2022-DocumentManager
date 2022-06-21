{
    let startElement, // for drag and drop
        rootElement; //for create content

    let folderTree,
        documentsList,
        documentHandler,
        wizard,
        pageOrchestrator = new PageOrchestrator();

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == undefined) {
            window.location.href = "loginPage.html";
        } else {
            pageOrchestrator.start();
            pageOrchestrator.refresh();
        }
    }, false);

    function Wizard(wizardId) {
        this.alert = document.getElementById("alert_container");
        this.wizard = wizardId;

        //input: an object, show the wizard and send the form according to the user choise
        this.showWizard = function(ogg) {
                if(ogg==null){
                    this.changeVisible('folder_form'); //new folder
                } else if (ogg.folderName!=undefined && ogg.subFolderName==undefined) {
                    this.changeVisible('sub_folder_form', ogg.folderName); //new sub folder
                    rootElement = ogg;
                } else if(ogg.folderName!=undefined && ogg.subFolderName!=undefined){
                    this.changeVisible('document_form', ogg.folderName, ogg.subFolderName); //new document
                    rootElement = ogg;
                }else{
                    alert("error in wizard");
                    return;
                }
                var self = this;

            // submit buttons
            Array.from(document.querySelectorAll("input[type='button']")
            ).forEach(b => {
                b.addEventListener("click", (e) => {
                    e.preventDefault();
                    let form = e.target.closest("form");
                    let url;
                    if(form.checkValidity()) {
                        //complete the form with user's choise and previous attributes saved in startElement:
                        let toSend = document.createElement("form");
                        if (form.id === "folder_form") {
                            url = "CreateFolder";
                            toSend = form.cloneNode(true);
                        } else if (form.id === "sub_folder_form") {
                            url = 'CreateSubFolder';
                            createSubFolderForm(rootElement.folderName, form.querySelector("input[name='subFolderName']").value, toSend);
                        } else if (form.id === "document_form") {
                            url = 'CreateDocument';
                            createDocumentForm(rootElement.folderName, rootElement.subFolderName,
                                form.querySelector("input[name='documentName']").value,
                                form.querySelector("input[name='summury']").value,
                                form.querySelector("input[name='body']"), toSend);
                        } else {
                            alert("invalid form")
                        }
                        form.reset();

                        makeCall("POST", url, toSend ,
                            function (req) {
                                let message;
                                if (req.readyState === XMLHttpRequest.DONE) {
                                    if (req.status === 200) {
                                        pageOrchestrator.refresh();
                                        self.alert.textContent = "new element created !";
                                        return;
                                    } else if (req.status === 403) {
                                        window.location.href = "loginPage.html";
                                        window.sessionStorage.removeItem('username');
                                    } else if (req.status === 400) {
                                        message = req.responseText;
                                        self.alert.textContent = message;
                                        self.reset();
                                        return;
                                    } else if (req.status == 409) {
                                        message = req.responseText;
                                        alert(message);
                                        return;
                                    } else if (req.status == 500) {
                                        message = req.responseText;
                                        alert(message);
                                        self.reset();
                                        return;
                                    }
                                }
                            }
                        );
                        rootElement=undefined;
                    } else{
                        form.reportValidity();
                    }
                    self.reset();
                }
                , false);
            });
        };

        this.reset = function() {
            let field;
            field = document.getElementById("folder_form");
            field.style.display = "none";
            field = document.getElementById("sub_folder_form");
            field.style.display = "none";
            let intro = document.getElementById("intro");
            if(intro!=null){intro.remove()}
            field = document.getElementById("document_form");
            field.style.display = "none";

        };

        this.changeVisible = function(destination, folder=null, subfolder=null) {
            this.reset();
            let field = document.getElementById(destination);
            field.style.display = "block";
            let intro = document.createElement("p");
            intro.id="intro";
            intro.className = "intro";
            if(folder!=null && subfolder==null){
                field.prepend(intro);
                intro.innerHTML = "Folder: " + folder+"<br>";
            }else if(folder!=null && subfolder!=null){
                field.prepend(intro);
                intro.innerHTML = "Folder: " + folder+"<br>"+ "Subfolder: "+ subfolder+"<br>";
            }
        };
    }
    //end wizard


    function FolderTree(_treecontainer) {

        this.alert = document.getElementById("alert_container");
        this.treecontainer = _treecontainer;

        this.reset = function() {
            this.treecontainer.style.display = "none";
        };

        this.show = function(doc) {
            const self = this;
            makeCall("GET", "GetFoldersTree", null,
            function(req) {
                    if (req.readyState === 4) {
                        let message = req.responseText;
                        if (req.status === 200) {
                            let treeToShow = JSON.parse(req.responseText);
                            if (treeToShow.length === 0) {
                                self.alert.textContent = "No folders yet!";
                                return;
                            }
                            self.update(treeToShow);
                        } else if (req.status === 403) {
                            window.location.href = "loginPage.html";
                            window.sessionStorage.removeItem('username');
                        } else {
                            alert(message);
                        }
                    }
                })
        };


        this.update = function(treeToShow) {
            let row, rowsub, namesubcell, sublist, namecell, linkcell, anchor, linkText, span
            const self = this;
            this.treecontainer.innerHTML = ""; // empty the table body
            //--link to add folder
            span = document.createElement("span");
            self.treecontainer.appendChild(span);
            linkText = document.createTextNode("   +"); // set icon
            span.appendChild(linkText);
            span.style="font-size:24px";
            span.style.fontWeight = "bold";
            span.addEventListener("click", (event) => {
                wizard.showWizard(null) ;
            }, false);
            //----
            treeToShow.forEach(function(folder) { // self visible here, not this
                row = document.createElement("li");
                namecell = document.createElement("label");
                namecell.textContent = folder.folderName;
                namecell.draggable=true;
                namecell.addEventListener("dragstart",(event)=> {dragStart(event, folder)});
                row.appendChild(namecell);
                //link to add element in the folder
                span = document.createElement("span");
                row.appendChild(span);
                linkText = document.createTextNode("    +"); // set icon
                span.appendChild(linkText);
                span.style="font-size:24px";
                span.style.fontWeight = "bold";
                span.addEventListener("click", (event) => {
                    wizard.showWizard(folder) ;
                }, false);
                //end link
                sublist = document.createElement("ul");
                folder.childList.forEach(function(subfolder) {
                    rowsub = document.createElement("li");
                    namesubcell = document.createElement("label");
                    namesubcell.textContent = subfolder.subFolderName;
                    namesubcell.setAttribute("draggable", "true");
                    namesubcell.addEventListener("dragstart",(event)=> {dragStart(event, subfolder)});
                    rowsub.appendChild(namesubcell);
                    //----drop in subfolder for move
                    namesubcell.addEventListener("dragover", dragOver);
                    namesubcell.addEventListener("drop", (event)=>drop(event, subfolder));
                    //----
                    //link to add document in the subfolder
                    span = document.createElement("span");
                    rowsub.appendChild(span);
                    linkText = document.createTextNode("   +"); // set icon
                    span.appendChild(linkText);
                    span.style="font-size:24px";
                    span.style.fontWeight = "bold";
                    span.addEventListener("click", (event) => {
                        wizard.showWizard(subfolder) ;
                    }, false);
                    //end link
                    //----link to see content in subfolder
                    linkcell = document.createElement("label");
                    anchor = document.createElement("a");
                    linkcell.appendChild(anchor);
                    linkText = document.createTextNode("  View Content");
                    anchor.appendChild(linkText);
                    anchor.href="#";
                    anchor.addEventListener("click", (event) => {
                        documentsList.show(subfolder);
                    }, false);
                    rowsub.appendChild(linkcell);
                    sublist.appendChild(rowsub);
                });
                row.appendChild(sublist);
                self.treecontainer.appendChild(row);
            }, false);
            // ---- TRASH BIN ----
            row = document.createElement("p");
            row.id = "BIN";
            row.style.backgroundColor = "lightskyblue";
            row.addEventListener("dragover", dragOver);
            row.addEventListener("drop", drop);
            row.textContent = "<----- Trash Bin ----->";;
            self.treecontainer.appendChild(row);
            // -- END TRASH BIN --
            this.treecontainer.style.display = "block";
            documentsList.reset();
        };
    }

    function dragStart(event, ogg) {
        startElement = ogg;
    }

    function dragOver(event) {
        if(event.target.id == "BIN") {
            event.preventDefault();
        }else{
            if(startElement.folderName!=undefined && startElement.subFolderName!=undefined && startElement.documentName!=undefined && startElement.type!=undefined){
                event.preventDefault();
            }
        }
    }

    function drop(event, subFolder){
        var dest=event.target;
        //delete
        if(dest.id == "BIN"){
            if(confirm("Are you sure you want to delete the selected item?")==true){
                let form = document.createElement("form");
                completeForm(startElement.username, startElement.folderName, startElement.subFolderName, startElement.documentName, startElement.type, form);
                makeCall("POST", "DeleteItem",form ,
                    function(req) {
                        if (req.readyState == XMLHttpRequest.DONE) {
                            if (req.status == 200) {
                                pageOrchestrator.refresh();
                                document.getElementById("alert_container").textContent="element deleted"
                            } else if (req.status == 403 || req.status == 401) {
                                window.location.href = "loginPage.html";
                                window.sessionStorage.removeItem('username');
                            }else if(req.status == 400){
                                var message = req.responseText;
                                document.getElementById("alert_container").textContent=message;
                            } else {
                                var message = req.responseText;
                                alert(message);
                            }
                        }
                    });
                startElement=undefined;
            }
            //move
        }else{
            if (confirm("Are you sure you want to move the selected item ?") == true) {
                let form = document.createElement("form");
                completeFormForMove(startElement.folderName, startElement.subFolderName, startElement.documentName, startElement.type, subFolder.folderName, subFolder.subFolderName, form);
                makeCall("POST", "MoveDocument", form,
                    function (req) {
                        if (req.readyState == XMLHttpRequest.DONE) {
                            if (req.status == 200) {
                                pageOrchestrator.refresh();
                                document.getElementById("alert_container").textContent = "element moved"
                            } else if (req.status == 403 || req.status == 401) {
                                window.location.href = "loginPage.html";
                                window.sessionStorage.removeItem('username');
                            } else if (req.status == 400) {
                                var message = req.responseText;
                                document.getElementById("alert_container").textContent = message;
                            } else {
                                var message = req.responseText;
                                alert(message);
                            }
                        }
                    });
                startElement = undefined;
            }
        }
    }

    function DocumentsList(_documentscontainer) {

        this.alert = document.getElementById("alert_container");
        this.documentscontainer = _documentscontainer;

        this.reset = function() {
            this.documentscontainer.style.display = "none";
        };

        this.show = function(subFolder) {
            let self = this;
            makeCall("GET", "GetDocumentsList?username=" + subFolder.username +
                                        "&folderName=" + subFolder.folderName +
                                        "&subFolderName=" + subFolder.subFolderName, null,
                function(req) {
                    if (req.readyState === 4) {
                        let message = req.responseText;
                        if (req.status === 200) {
                            let docsToShow = JSON.parse(req.responseText);
                            self.documentscontainer.style.display = "block";
                            if (docsToShow.length === 0) {
                                self.documentscontainer.innerHTML = "";
                                let row = document.createElement("p");
                                row.className = "intro";
                                self.documentscontainer.appendChild(row);
                                row.innerHTML = "Folder: " + subFolder.folderName+"<br>"+"Subfolder: "+ subFolder.folderName+"<br>"+"No documents yet!";
                                return;
                            }
                            self.update(docsToShow);
                        } else if (req.status === 403) {
                            window.location.href = "loginPage.html";
                            window.sessionStorage.removeItem('username');
                        }
                        else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        };

        this.update = function(docsToShow) {
            let row, namecell, viewcell, movecell, anchorv, anchorm, viewText, moveText;
            let self = this;
            self.documentscontainer.innerHTML = "";
            let intro = document.createElement("p");
            self.documentscontainer.appendChild(intro);
            intro.innerHTML = "Folder: " + docsToShow[0].folderName+"<br>"+"Subfolder: "+ docsToShow[0].folderName+"<br>";
            intro.className = "intro";
            docsToShow.forEach(function (doc) {
                row = document.createElement("li");
                namecell = document.createElement("label");
                namecell.textContent = doc.documentName;
                row.appendChild(namecell);
                // ------------------move-----------------
                namecell.setAttribute("draggable", "true");
                namecell.addEventListener("dragstart",(event)=> {dragStart(event, doc)});
                //-------------------------------------------------
                viewcell = document.createElement("label");
                anchorv = document.createElement("a");
                viewcell.appendChild(anchorv);
                viewText = document.createTextNode("  View");
                anchorv.appendChild(viewText);
                anchorv.documentName = doc.documentName;
                anchorv.setAttribute('folderName', doc.folderName); // set folderName
                anchorv.setAttribute('subFolderName', doc.subFolderName); // set subFolderName
                anchorv.setAttribute('documentName', doc.documentName); // set subFolderName
                anchorv.setAttribute('documentType', doc.type); // set subFolderName
                anchorv.addEventListener("click", () => {
                    documentHandler.show(doc);
                }, false);
                anchorv.href = "#";
                row.appendChild(viewcell);
                self.documentscontainer.appendChild(row);
            }, false);
        this.documentscontainer.style.display = "block";
        documentHandler.reset();
        };
    }

    function DocumentHandler(_doccontainer) {

        this.alert = document.getElementById("alert_container");
        this.doccontainer = _doccontainer;

        this.reset = function() {
            this.doccontainer.style.display = "none";
        };

        this.show = function(doc) {
            let self = this;
            makeCall("GET","Access?username=" + doc.username +
                "&folderName=" + doc.folderName +
                "&subFolderName=" + doc.subFolderName +
                "&documentName=" + doc.documentName +
                "&documentType=" + doc.type, null,
                function(req) {
                    if (req.readyState === 4) {
                        let message = req.responseText;
                        if (req.status === 200) {
                            let detailsToShow = JSON.parse(req.responseText);
                            self.update(detailsToShow);
                            self.doccontainer.style.display = "block";;
                        } else if (req.status === 403) {
                            window.location.href = "loginPage.html";
                            window.sessionStorage.removeItem('username');
                        }
                        else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        };

        this.update = function(doc) {
            let row, subfoldercell, downloadcell, table, th, td
            let self = this;
            self.doccontainer.innerHTML = "";
            table = document.createElement("table");
            table.border = "1px soldid black"

            row = document.createElement("tr");
            th = document.createElement("th");
            th.textContent = "Name: ";
            row.appendChild(th);
            td = document.createElement("td");
            td.textContent = doc.folderName;
            row.appendChild(td);
            table.appendChild(row);

            row = document.createElement("tr");
            th = document.createElement("th");
            th.textContent = "Sub folder : ";
            td = document.createElement("td");
            td.textContent = doc.subFolderName;
            row.appendChild(th);
            row.appendChild(td);
            table.appendChild(row);

            row = document.createElement("tr");
            th = document.createElement("th");
            th.textContent = "Document name : ";
            td = document.createElement("td");
            row.style.backgroundColor="gold";
            td.textContent = doc.documentName;
            row.appendChild(th);
            row.appendChild(td);
            table.appendChild(row);

            row = document.createElement("tr");
            th = document.createElement("th");
            th.textContent = "Type : ";
            td = document.createElement("td");
            td.textContent = doc.type;
            row.appendChild(th);
            row.appendChild(td);
            table.appendChild(row);


            row = document.createElement("tr");
            th = document.createElement("th");
            th.textContent = "Creation : ";
            td = document.createElement("td");
            td.textContent = doc.date;
            row.appendChild(th);
            row.appendChild(td);
            table.appendChild(row);


            row = document.createElement("tr");
            th = document.createElement("th");
            th.textContent = "Summury : ";
            td = document.createElement("td");
            td.textContent = doc.summary;
            row.appendChild(th);
            row.appendChild(td);
            table.appendChild(row);

            row = document.createElement("tr");
            downloadcell=document.createElement("td");
            downloadcell.colSpan="2";
            downloadcell.align="center";
            row.appendChild(downloadcell);
            table.appendChild(row);

            let anchord = document.createElement("a");
            downloadcell.appendChild(anchord);
            let downloadText = document.createTextNode("  Download");
            anchord.appendChild(downloadText);
            anchord.href="GetFile?username=" + doc.username +
                "&folderName=" + doc.folderName +
                "&subFolderName=" + doc.subFolderName +
                "&documentName=" + doc.documentName +
                "&documentType=" + doc.type
            anchord.download=doc.documentName+"."+doc.type;
            // -----------------------------------------
            this.doccontainer.style.display = "block";
            self.doccontainer.appendChild(table);
        };
    }

    function PageOrchestrator() {

        this.start = function() {
            // init folder list
            folderTree = new FolderTree(
                document.getElementById("treecontainer")
            );

            documentsList = new DocumentsList(
                document.getElementById("documentscontainer")
            );

            documentHandler = new DocumentHandler(
                document.getElementById("detailscontainer")
            );

            // register folder_form wizard
            wizard = new Wizard(document.getElementById("create-content"));

            document.querySelector("a[href='LogOut']").addEventListener('click', () => {
                sessionStorage.removeItem('username');
            });
        };

        this.refresh = function() {
            document.getElementById("alert_container").textContent = "";
            folderTree.reset();
            documentsList.reset();
            documentHandler.reset();
            folderTree.show(false);
            wizard.reset();
        };
    }
}
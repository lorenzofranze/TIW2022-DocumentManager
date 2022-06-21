(function(){
    var old_id;
    document.getElementById("subscribeButton").addEventListener("click", (e)=>{
        e.preventDefault();
        if(old_id!=undefined) { document.getElementById(old_id).textContent = ""; }
        var form = e.target.closest("form");
        if(form.checkValidity()){
            if(document.querySelector("input[name=password1]").value == document.querySelector("input[name=password2]").value){
                    makeCall("POST", "Subscribe", e.target.closest("form"), function (x) {
                        if (x.readyState == XMLHttpRequest.DONE) {
                            var message = x.responseText;
                            switch (x.status) {
                                case 200:
                                    sessionStorage.setItem("registrationOK", "REGISTRATION COMPLETED");
                                    window.location.href = "loginPage.html";
                                    break;
                                case 400: // bad request
                                    document.getElementById("Error").textContent = message;
                                    break;
                                case 406: //email or username already in use
                                    var id = message.startsWith("U") ? "nameError" : "emailError";
                                    old_id = id;
                                    document.getElementById(id).textContent = message;
                                    break;
                                case 502: // server error
                                    alert(message);
                                    break;
                            }
                        }
                    }, false);
                    document.getElementById("passwordDifferentError").textContent="";
            } else {
                document.getElementById("passwordDifferentError").textContent="passords have different values"
            }
        }else{
            form.reportValidity();
        }

    } )

    document.querySelector("input[name='email']").addEventListener("blur", function(e){
        var mail = this.value
        if(!mail.includes(".") ||  !mail.includes("@")){
            document.getElementById("emailError").textContent = "Email invalid"
        }else{
            document.getElementById("emailError").textContent = ""
        }
    })

})();
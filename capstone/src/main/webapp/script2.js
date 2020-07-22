// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

var data_for_select_field ={};
//the data_for_autocomplete represents what will be shown on the form:
//the keys of this object is the text, while the value is an image url of the associated text.
//I'm setting the values as null because an image is not required in our case
var data_for_autocomplete = {};

async function getUniversity() {
    const response = await fetch("/university");
    const result = await response.json();
    setUpAutoComplete(result);
}

function setUpAutoComplete(university_data_from_request){
    for(el in university_data_from_request) {
        var university = university_data_from_request[el];
        var university_description = university["name"] + ", " + university["state"];
        data_for_select_field[university["id"]] = university_description;
        data_for_autocomplete[university_description] = null;
    }
}

getUniversity();

document.addEventListener('DOMContentLoaded', function() {
    //Init auto complete.
    const songListIns = (function(){
    const acElms = document.querySelectorAll('.autocomplete');
    const instances = M.Autocomplete.init(acElms, {
        data: data_for_autocomplete
    })
    return instances[0]
    })()
})

function checkUniversityValue() {
    var val = document.getElementById("university").value;
    var error = document.getElementById("university_error");
    error.innerHTML = "";
    for ( university_id in data_for_select_field ){
        if (data_for_select_field[university_id] == val) {
            return university_id;
        }
    }
    const pElement = document.createElement("p");
    pElement.style.color = "red";
    pElement.innerHTML = "Your choice is not valid";
    error.appendChild(pElement);
    return ;
}

function checkFullNameInputValue(){
    var val = document.getElementById("full_name").value;
    var error = document.getElementById("full_name_error");
    error.innerHTML = "";
    if (val == "" || val == " "){
        const pElement = document.createElement("p");
        pElement.style.color = "red";
        pElement.innerHTML = "This is a required field.";
        error.appendChild(pElement);
        return ;
    }
    return val;
}

function checkUserNameInputValue(){
    var val = document.getElementById("user_name").value;
    var error = document.getElementById("user_name_error");
    error.innerHTML = "";
    if (val == "" || val == " "){
        const pElement = document.createElement("p");
        pElement.style.color = "red";
        pElement.innerHTML = "This is a required field.";
        error.appendChild(pElement);
        return ;
    }
    return val;
}

async function registerUserDetails() {
    if (checkUniversityValue() == null) {
        return ;
    }
    if (checkFullNameInputValue() == null) {
        return ;
    }
    if (checkUserNameInputValue() == null) {
        return ;
    }
    
    var university_id = checkUniversityValue();
    var full_name = checkFullNameInputValue();
    var user_name = checkUserNameInputValue();
    data = {"full_name": full_name, "user_name": user_name, "university": university_id};
    const response = await fetch("/users", {method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(data)});
    const result = await response.json();
    window.location.pathname = "/dashboard.html";
 }

function fillUserDashboardPage(response_body) {
    if (response_body.user_status == "true") {
        var full_name_tag = document.getElementById("full_name");
        var user_name_tag = document.getElementById("user_name");
        var univeristy_tag = document.getElementById("university");
        var email_tag = document.getElementById("email");
        var logout_button = document.getElementById("logout_url");
        full_name_tag.textContent = response_body.full_name;
        user_name_tag.textContent = response_body.user_name;
        univeristy_tag.textContent = response_body.university;
        email_tag.textContent = response_body.email;
        logout_button.href = decodeURIComponent(response_body.logoutUrl);
    }
    else{
        window.location.replace("/");
    }
}

function invalidEmailInput(logout_url) {
    var continue_button = document.getElementById("continue");
    continue_button.href = logout_url;
}

async function getCurrentUserInfo(){
    const response = await fetch("/users?query_source=client");
    const response_body = await response.json();
    var log_out_dec = decodeURIComponent(response_body.logoutUrl);
    if (response_body.status_code == "200") {
        if (window.location.pathname == "/") {
            window.location.pathname = "/dashboard.html";
        }
        fillUserDashboardPage(response_body);
    }
    else if (response_body.status_code == "412"){
        invalidEmailInput(log_out_dec);       
    }
    else if (response_body.status_code == "401"){
        if (window.location.pathname == "/dashboard.html"){
            window.location.pathname = "/";
        }
        var home_login = document.getElementById("login");
        home_login.href =  decodeURIComponent(response_body.loginUrl);
    }
    else if(response_body.status_code == "307"){
        if(window.location.pathname == "/"){
            window.location.pathname = "/initialLogin.html";
        }
        document.getElementById("email-field").textContent = response_body.email;

    }
}

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

var dataForSelectField ={};
//the data_for_autocomplete represents what will be shown on the form:
//the keys of this object is the text, while the value is an image url of the associated text.
//I'm setting the values as null because an image is not required in our case
var dataForAutocomplete = {};

async function getUniversity() {
    const response = await fetch("/university");
    const result = await response.json();
    setUpAutoComplete(result);
}

function setUpAutoComplete(universityDataFromRequest){
    for(el in universityDataFromRequest) {
        var university = universityDataFromRequest[el];
        var universityDescription = university["name"] + ", " + university["state"];
        dataForSelectField[university["id"]] = universityDescription;
        dataForAutocomplete[universityDescription] = null;
    }
}

getUniversity();

document.addEventListener('DOMContentLoaded', function() {
    //Init auto complete.
    const universityList = (function(){
        const acElms = document.querySelectorAll('.autocomplete');
        const instances = M.Autocomplete.init(acElms, {
            data: dataForAutocomplete
        });
        return instances[0]
    })();
});

function checkUniversityValue() {
    var val = document.getElementById("university").value;
    var error = document.getElementById("university-error");
    error.innerHTML = "";
    for ( universityId in dataForSelectField){
        if (dataForSelectField[universityId] == val) {
            return universityId;
        }
    }
    const paragraphElement = document.createElement("p");
    paragraphElement.style.color = "red";
    paragraphElement.innerHTML = "Your choice is not valid";
    error.appendChild(paragraphElement);
    return ;
}

function checkFullNameInputValue(){
    var val = document.getElementById("full-name").value;
    var error = document.getElementById("full-name-error");
    error.innerHTML = "";
    if (val.trim() === ""){
        const paragraphElement = document.createElement("p");
        paragraphElement.style.color = "red";
        paragraphElement.innerHTML = "This is a required field.";
        error.appendChild(paragraphElement);
        return ;
    }
    return val;
}

function checkUserNameInputValue(){
    var val = document.getElementById("user-name").value;
    var error = document.getElementById("user-name-error");
    error.innerHTML = "";
    if (val.trim() === ""){
        const paragraphElement = document.createElement("p");
        paragraphElement.style.color = "red";
        paragraphElement.innerHTML = "This is a required field.";
        error.appendChild(paragraphElement);
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
    
    var universityId = checkUniversityValue();
    var fullName = checkFullNameInputValue();
    var userName = checkUserNameInputValue();
    data = {"full_name": fullName, "user_name": userName, "university": universityId};
    const response = await fetch("/users", {method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(data)});
    const result = await response.json();
    window.location.pathname = "/dashboard.html";
 }

function fillUserDashboardPage(responseBody) {
    if (responseBody.user_status == "true") {
        var fullNameTag = document.getElementById("full-name");
        var userNameTag = document.getElementById("user-name");
        var univeristyTag = document.getElementById("university");
        var emailTag = document.getElementById("email");
        var logoutButton = document.getElementById("logout-url");
        fullNameTag.textContent = responseBody.full_name;
        userNameTag.textContent = responseBody.user_name;
        univeristyTag.textContent = responseBody.university;
        emailTag.textContent = responseBody.email;
        logoutButton.href = decodeURIComponent(responseBody.logoutUrl);
    }
    else{
        window.location.replace("/");
    }
}

function invalidEmailInput(logoutUrl) {
    var continueButton = document.getElementById("continue");
    continueButton.href = logoutUrl;
}

// add the redirect:"manual" option to the fetch command, and change the "/users" in userservlet to "/user" and the other one to "/users"
//when you do this you get a blank response wityh no detail at all and the status is 0, type is called opaqueredirect type.

async function getCurrentUserInfo(){
    const response = await fetch("/users?query_source=client");
    const responseBody =  await response.json();
    console.log(response);
    console.log(response.status);
    console.log(response.url);
    console.log(response.redirected);
    console.log(responseBody);
    var logOutDec = decodeURIComponent(responseBody.logoutUrl);
    var userId = responseBody.id;
    console.log(userId);

    if (responseBody.status_code == "200") {
        if (window.location.pathname == "/") {
            window.location.pathname = "/dashboard.html";
        }
        fillUserDashboardPage(responseBody);
        getRecentStudySet(userId);
    }
    else if (responseBody.status_code == "412"){
        if(window.location.pathname == "/"){
            window.location.pathname = "/invalidEmail.html";
        }
        invalidEmailInput(logOutDec);       
    }
    else if (responseBody.status_code == "401"){
        if (window.location.pathname == "/dashboard.html"){
            window.location.pathname = "/";
        }
        var homeLogin = document.getElementById("login");
        homeLogin.href =  decodeURIComponent(responseBody.loginUrl);
    }
    else if(responseBody.status_code == "303"){
        if(window.location.pathname == "/"){
            window.location.pathname = "/initialLogin.html";
        }
        document.getElementById("email-field").textContent = responseBody.email;

    }
}
// <div class="col s12 m4">
//                 <div class="card yellow-grey darken-1">
//                     <div class="card-content black-text">
//                         <span class="card-title">Study Set Title, Subject</span>
//                         <p>
//                             The description of the study set goes here
//                         </p>
//                     </div>
//                     <div class="card-action">
//                         <p>
//                             This study set has 2 cards
//                         </p>
//                     </div>
//                 </div>

function addNewStudySet(responseBody){
    var linkElement = document.createElement("a");
    linkElement.href = "/viewStudySet.html?id="+responseBody.id;
    var colDivElement =document.createElement("div");
    colDivElement.className = "col s12 m4";
    var cardDivElement = document.createElement("div");
    cardDivElement.className = "card yellow-grey darken-1";
    var cardContentDivElement = document.createElement("div");
    cardContentDivElement.className = "card-content black-text";
    var spanElement = document.createElement("span");
    spanElement.className = "card-title";
    spanElement.textContent = responseBody.title + ", " + responseBody.subject;
    cardContentDivElement.append(spanElement);
    var paragraphElement = document.createElement("p");
    paragraphElement.textContent = responseBody.description;
    cardContentDivElement.append(paragraphElement);
    var cardActionDivElement = document.createElement("div");
    cardActionDivElement.className = "card-action";
    var paragraphElementTwo = document.createElement("p");
    paragraphElementTwo.textContent = "This study set has " + responseBody.study_set_length + " cards.";
    cardActionDivElement.append(paragraphElementTwo);
    cardDivElement.append(cardContentDivElement);
    cardDivElement.append(cardActionDivElement);
    colDivElement.append(cardDivElement);
    linkElement.append(colDivElement);
    return linkElement;
}

async function getRecentStudySet(userId){
    const response = await fetch("/study_set/user/"+userId);
    const responseBody = await response.json();
    console.log(responseBody);
    console.log(userId);
    if(!Object.keys(responseBody).length){
     document.getElementById("study-set-text").textContent = "You are yet to create a study set, click above to start now !";
     return ;
    }
    
    var userStudySetElement = document.getElementById("recent-study-set");
    userStudySetElement.innerHTML = "";

    responseBody.forEach(function (studySet) {
    userStudySetElement.append(addNewStudySet(studySet));
  });
}

//Neglect this file now

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

async function redirectUserToRightPage(){
    const response = await fetch("/users/current");
    if (response.status  == 200) {
        window.location.replace("/dashboard.html");
    }
    else if (response.status == 300) {
        window.location.replace("/validate_email");
    }
    else if (response.status == 401){
        const responseBody = await response.json();
        document.getElementById("login").href = decodeURIComponent(responseBody.loginUrl);
    }
}

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
    window.location.pathname = "/dashboard.html";
 }

async function fillUserDashboardPage() {
    const response = await fetch("/users/current");
    if (response.status == 200) {
        const responseBody =  await response.json();
        document.getElementById("full-name").textContent = responseBody.full_name;
        document.getElementById("user-name").textContent = responseBody.user_name;
        document.getElementById("university").textContent = responseBody.university;
        document.getElementById("email").textContent = responseBody.email;
        document.getElementById("logout-url").href = decodeURIComponent(responseBody.logoutUrl);
        getRecentStudySet(responseBody.id);
    }
    else if (response.status == 401) {
        window.location.replace("/");
    }
    else if (response.status == 300) { 
        window.location.replace("/validate_email");
    }
}

function invalidEmailRequest() {
    const params = new URLSearchParams(window.location.search);
    var continueButton = document.getElementById("continue");
    continueButton.href = params.get("logoutUrl");
    
}

function getNewUserEmail(){
    const params = new URLSearchParams(window.location.search);
    document.getElementById("email-field").textContent = params.get("email");

}

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
    spanElement.className = "card-description";
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

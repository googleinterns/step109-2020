//Intial tabs
var elem = document.querySelector(".tabs");
var options = {};
var instance = M.Tabs.init(elem, options);

//Initialize sidenav
const slide_menu = document.querySelectorAll(".sidenav");
M.Sidenav.init(slide_menu, {});

//Get Search Request From User and display result
async function studySetsBySearch() {
  const stringToSearchBy = document.getElementById("search").value;
  const response = await fetch(
    "/study_set?stringToSearchBy=" + stringToSearchBy
  );
  const searchResponse = await response.json();
  var configuredHTMLSearchResponse = configureSearchResponseToHTML(
    searchResponse
  );
  document.getElementById(
    "results-container"
  ).innerHTML = configuredHTMLSearchResponse;
}

function configureSearchResponseToHTML(studySets) {
  var configuredStudySetInfoCards = "";
  if (studySets.length == 0) {
    return ` <div class="no-results"><h3>No Results Found...<i class="large material-icons">sentiment_dissatisfied</i></h3></div>`;
  }

  for (var studySet of studySets) {
    configuredStudySetInfoCards += formatInfoCardInHTML(studySet);
  }
  return configuredStudySetInfoCards;
}

function formatInfoCardInHTML(studySet) {
  var id = studySet.id;
  var title = studySet.title;
  var subject = studySet.subject;
  var length = studySet.study_set_length;
  var description = studySet.description;
  var author = studySet.user_author;
  var university = studySet.university;
  return `
    <a href="/viewStudySet.html?id=${id}">
      <div class="row"> 
        <div class="result-card">
          <div class="col card s12 m12 l12">
            <div class="top-line">
              <span><strong> ${title.toUpperCase()}
              </strong>, ${subject}</span>
            </div>
            <p>${length} cards in set</p>
            <p><strong>DESCRIPTION</strong>, ${description}</p>
            <span> By  ${studySet.user_author} from ${
    studySet.university
  }</span>
          </div>
        </div>
      </div>
    </a>`;
}

//View Study Set
async function loadStudySetToPage() {
  var urlParams = new URLSearchParams(window.location.search);
  const studySetId = urlParams.get("id");
  const response = await fetch("/study_set/" + studySetId);
  const studySet = await response.json();

  document.getElementById(
    "study-set-directory-container"
  ).innerHTML = configureStudySetCardDirectoryHTML(studySet.cards);

  document.getElementById(
    "study-set-details-container"
  ).innerHTML = configureStudySetDetailsHTML(studySet);

  document.getElementById(
    "all-cards-container"
  ).innerHTML = configureAllCardsHTML(studySet.cards);
}

function configureStudySetCardDirectoryHTML(cards) {
  var configuredCardList = "";
  for (var card of cards) {
    var front = card.front;
    var back = card.back;
    configuredCardList += `
    <li class="truncate">
      <a onclick="displayCard('${front}', '${back}')" class="btn-flat">
        ${front.toUpperCase()}
      </a>
    </li>`;
  }
  return ` <ul> ${configuredCardList} </ul>`;
}

function displayCard(front, back) {
  document.getElementById("card-container").innerHTML = formatSelectedCard(
    front,
    back
  );
}

function formatSelectedCard(front, back) {
  return `  
    <div class="card activator large">
      <div class="card-content activator">
        <span class="card-title activator">${front}</span>
      </div>
      <div class="card-reveal">
        <span class="card-title">${front}<i class="material-icons right">close</i></span>
        <p>${back}</p>
      </div>
    </div>`;
}
function configureStudySetDetailsHTML(studySet) {
  var configuredStudySetDetails = "";
  configuredStudySetDetails += formatStudySetDetailsInHTML(studySet);
  return configuredStudySetDetails;
}

function formatStudySetDetailsInHTML(studySet) {
  var title = studySet.title;
  var subject = studySet.subject;
  var description = studySet.description;
  var author = studySet.user_author;
  var university = studySet.university;
  return `
    <h3>${title.toUpperCase()}</h3>
    <span>
      ${subject}<br>
      ${description}<br>
      by ${author} from ${university}
    </span>`;
}

function configureAllCardsHTML(cards) {
  var configuredStudySetCards = "";
  for (var card of cards) {
    var front = card.front;
    var back = card.back;
    configuredStudySetCards += ` 
    <li class="card full-card">
        <div class="front">${front}</div>
        <div class="back">${back}</div>
    </li>`;
  }
  return ` <ul>${configuredStudySetCards}</ul>`;
}

function showAllButton() {
  var allCardsContainer = document.getElementById("all-cards-container");
  if (allCardsContainer.classList.contains("hide")) {
    allCardsContainer.classList.remove("hide");
  } else {
    allCardsContainer.classList.add("hide");
  }
}

// Create Study Set

function addCard() {
  var form = document.getElementById("card_form");

  var newFrontCard = document.createElement("div");
  newFrontCard.setAttribute("class", "input-field col s6");

  var frontCard = document.createElement("input");
  frontCard.setAttribute("id", "front");
  frontCard.setAttribute("type", "text");
  frontCard.setAttribute("class", "validate input-card");

  var frontLabel = document.createElement("label");
  frontLabel.setAttribute("for", "front");
  var frontText = document.createTextNode("Front");
  frontLabel.append(frontText);

  newFrontCard.append(frontCard);
  newFrontCard.append(frontLabel);

  form.appendChild(newFrontCard);

  var newBackCard = document.createElement("div");
  newBackCard.setAttribute("class", "input-field col s6");

  var backCard = document.createElement("input");
  backCard.setAttribute("id", "back");
  backCard.setAttribute("type", "text");
  backCard.setAttribute("class", "validate input-card");

  var backLabel = document.createElement("label");
  backLabel.setAttribute("for", "back");
  var backText = document.createTextNode("Back");
  backLabel.append(backText);

  newBackCard.append(backCard);
  newBackCard.append(backLabel);

  form.appendChild(newBackCard);
}

function addStudySet() {
  var elements = document.getElementsByClassName("input-card");
  var cards = [];

  for (var i = 0; i < elements.length - 1; i = i + 2) {
    var contacts = { front: elements[i].value, back: elements[i + 1].value };
    cards.push(contacts);
  }

  var userId = document.getElementById("user-id").value;
  userId = userId.trim();

  var title = document.getElementById("title").value;
  title = title.trim();

  var subject = document.getElementById("subject").value;
  subject = subject.trim();

  var description = document.getElementById("description").value;
  description = description.trim();

  var universityId = document.getElementById("university-id").value;
  universityId = universityId.trim();

  var professor = document.getElementById("professor").value;
  professor = professor.trim();

  var academicTime = document.getElementById("academic-time").value;
  academicTime = academicTime.trim();

  var courseName = document.getElementById("course-name").value;
  courseName = courseName.trim();

  if (
    !isStudySetInfoFilled(
      userId,
      title,
      subject,
      description,
      universityId,
      professor,
      academicTime,
      courseName
    )
  ) {
    window.alert(
      "Please make sure that all study set information has been filled in"
    );
    return;
  }

  fetch("/study_set", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      user_id: userId,
      title: title,
      subject: subject,
      description: description,
      university_id: universityId,
      professor: professor,
      academic_time: academicTime,
      course_name: courseName,
      cards: cards,
    }),
  });
  window.location.reload();
}

function isStudySetInfoFilled(
  userId,
  title,
  subject,
  description,
  universityId,
  professor,
  academicTime,
  courseName
) {
  if (
    userId == "" ||
    title == "" ||
    subject == "" ||
    description == "" ||
    universityId == "" ||
    professor == "" ||
    academicTime == "" ||
    courseName == ""
  ) {
    return false;
  }
  return true;
}

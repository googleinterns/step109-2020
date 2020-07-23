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
  return `
    <a href="/viewStudySet.html?id=${studySet.id}" class="result-card">
      <div class="row result-card"> 
        <div class="col card s12 m12 l12">
          <div class="top-line">
            <span><strong> ${studySet.title.toUpperCase()}
            </strong>, ${studySet.subject}</span>
          </div>
          <p>${studySet.study_set_length} cards in set</p>
          <p><strong>DESCRIPTION</strong>, ${studySet.description}</p>
          <span> By  ${studySet.user_author} from ${studySet.university}</span>
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
    configuredCardList += `
    <li class="truncate">
      <a onclick="displayCard('${ card.front}', '${card.back}')" class="btn-flat">
        ${card.front.toUpperCase()}
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
  return `
    <h3>${studySet.title.toUpperCase()}</h3>
    <span>
      ${studySet.subject}<br>
      ${studySet.description}<br>
      by ${studySet.user_author} from ${studySet.university}
    </span>`;
}

function configureAllCardsHTML(cards) {
  var configuredStudySetCards = "";
  for (var card of cards) {
    configuredStudySetCards += ` 
    <li class="card">
      <div class="row">
        <div class="col s12 m4 offset-m1 front">${card.front}</div>
        <div class="col s12 m6 offset-m1 back">${card.back}</div>
      </div>
    </li>`;
  }
  return ` <ul>${configuredStudySetCards}</ul>`;
}

function showAllButton() {
  var allCardsContainer = document.getElementById("all-cards-container");
  if (allCardsContainer.style.display == "none") {
    allCardsContainer.style.display = "block";
  } 
  else {
    allCardsContainer.style.display = "none";
  }
}

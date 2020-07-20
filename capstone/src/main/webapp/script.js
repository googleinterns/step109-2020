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
    configuredStudySetInfoCards += formatCardInHTML(studySet);
  }
  return configuredStudySetInfoCards;
}

function formatCardInHTML(studySet) {
  return `<a href="/viewStudySet.html?id=${studySet.id}" class="result-card">
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

  var user_id = document.getElementById("user_id").value;
  user_id = user_id.trim();

  var title = document.getElementById("title").value;
  title = title.trim();

  var subject = document.getElementById("subject").value;
  subject = subject.trim();

  var description = document.getElementById("description").value;
  description = description.trim();

  var university_id = document.getElementById("university_id").value;
  university_id = university_id.trim();

  var professor = document.getElementById("professor").value;
  professor = professor.trim();

  var academic_time = document.getElementById("academic_time").value;
  academic_time = academic_time.trim();

  var course_name = document.getElementById("course_name").value;
  course_name = course_name.trim();

  if (
    !isStudySetInfoFilled(
      user_id,
      title,
      subject,
      description,
      university_id,
      professor,
      academic_time,
      course_name
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
      user_id: user_id,
      title: title,
      subject: subject,
      description: description,
      university_id: university_id,
      professor: professor,
      academic_time: academic_time,
      course_name: course_name,
      cards: cards,
    }),
  });
  window.location.reload();
}

function isStudySetInfoFilled(
  user_id,
  title,
  subject,
  description,
  university_id,
  professor,
  academic_time,
  course_name
) {
  if (
    user_id == "" ||
    title == "" ||
    subject == "" ||
    description == "" ||
    university_id == "" ||
    professor == "" ||
    academic_time == "" ||
    course_name == ""
  ) {
    return false;
  }
  return true;
}

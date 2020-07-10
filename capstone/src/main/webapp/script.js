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
  var configuredSetsToPost = configureCardsForSearchPage(searchResponse);
  document.getElementById("results-container").innerHTML = configuredSetsToPost;
}

function configureCardsForSearchPage(studySets) {
  var configuredCardStudySets = "";
  var counter = 0;

  for (var studySet of studySets) {
    configuredCardStudySets += configureCard(studySet);
    counter++;
  }

  if (counter == 0) {
    return (configuredCardStudySets = ` <div class="no-results"><h3>No Results Found...<i class="large material-icons">sentiment_dissatisfied</i></h3></div>`);
  }
  return configuredCardStudySets;
}

function configureCard(studySet) {
  return (configuredCardStudySet = `<a href="/viewStudySet.html?id= ${
    studySet.id
  }" class="result-card">
      <div class="row result-card"> 
        <div class="col card s12 m12 l12">
          <div class="top-line">
            <span><strong> ${studySet.title.toUpperCase()}</strong>, ${
    studySet.subject
  }</span>
          </div>
          <p>${studySet.study_set_length} cards in set</p>
          <p><strong>DESCRIPTION</strong>, ${studySet.description}</p>
          <span> By  ${studySet.user_author} from ${studySet.university}</span>
         </div>
      </div>
    </a>`);
}

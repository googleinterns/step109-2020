//Intial tabs
var elem = document.querySelector(".tabs");
var options = {};
var instance = M.Tabs.init(elem, options);

//Initialize sidenav
const slide_menu = document.querySelectorAll(".sidenav");
M.Sidenav.init(slide_menu, {});

//Get Most Recent Study Sets To Display
async function most_recent_studysets() {
  const response = await fetch("/study_set");
  const studySets = await response.json();
  var configuredSetsToPost = configureCardsForSearchPage(studySets);

  document.getElementById("results-container").innerHTML = configuredSetsToPost;
}

//Get Search Request From User and display result
async function studysets_by_search() {
  const stringToSearchBy = document.getElementById("search").value;
  const response = await fetch("/study_set?searchbar=" + stringToSearchBy);
  const searchResponse = await response.json();
  var configuredSetsToPost = configureCardsForSearchPage(searchResponse);

  document.getElementById("results-container").innerHTML = configuredSetsToPost;
}

function configureCardsForSearchPage(studySets) {
  var configuredCardStudySets = "";
  for (var studySet of studySets) {
    var title = studySet.title;
    var subject = studySet.subject;
    var numOfCards = studySet.study_set_length;
    var author = studySet.user_author;
    var university = studySet.university;
    configuredCardStudySets += configureCard(
      title,
      subject,
      numOfCards,
      author,
      university
    );
  }
  return configuredCardStudySets;
}

function configureCard(title, subject, numOfCards, author, university) {
  return (configuredCardStudySet = [
    '<a href="#" class="result-card"> ',
    '<div class="row result-card"> ',
    '<div class="col card s12 m12 l12">',
    '<div class=""top-line""> ',
    "<span><strong>" + title + "</strong>, " + subject + "</span>",
    "</div>",
    "<p>" + numOfCards + " cards in set</p>",
    "<span> By " + author + " from " + university + "</span>",
    "</div>",
    "</div>",
    "</a>",
  ].join(""));
}

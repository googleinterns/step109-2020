//Intial tabs
var elem = document.querySelector(".tabs");
var options = {};
var instance = M.Tabs.init(elem, options);

//Initialize sidenav
const slide_menu = document.querySelectorAll(".sidenav");
M.Sidenav.init(slide_menu, {});

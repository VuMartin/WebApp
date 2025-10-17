jQuery("#username").text(sessionStorage.getItem("firstName") + " ▼");
const username = document.getElementById('username');
const dropdownMenu = document.getElementById('dropdown-menu');

username.addEventListener('click', () => {
    dropdownMenu.classList.toggle('visible');
});

const logoutButton = document.getElementById('logout-button');

logoutButton.addEventListener('click', () => {
    $.ajax({
        method: "POST",
        url: "api/logout",
        success: () => {
            window.location.href = "login.html"; // redirect after logout
        },
        error: () => {
            alert("Logout failed. Try again.");
        }
    });
});

const optionsBtn = document.getElementById('search-options-button');
const searchOptions = document.getElementById('search-options');

optionsBtn.addEventListener('click', () => {
    searchOptions.classList.toggle('visible');
});

jQuery("#search-form").submit(function(event) {
    event.preventDefault(); // stop page reload
    // get values
    let title = jQuery("#search-input").val();
    let year = jQuery("#search-year").val();
    let director = jQuery("#search-director").val();
    let star = jQuery("#search-star").val();

    let url = "movies.html?";
    if (title) url += "title=" + encodeURIComponent(title) + "&";
    if (year) url += "year=" + encodeURIComponent(year) + "&";
    if (director) url += "director=" + encodeURIComponent(director) + "&";
    if (star) url += "star=" + encodeURIComponent(star);

    window.location.href = url;
});
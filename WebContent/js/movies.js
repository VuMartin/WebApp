/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

document.querySelectorAll(".sort-group").forEach(group => {
    const options = group.querySelectorAll(".sort-option");
    options.forEach(option => {
        option.addEventListener("click", () => {
            options.forEach(o => o.classList.remove("selected"));
            option.classList.add("selected");

            // jQuery.ajax({
        });
    });
});

let currentPage = 1;
let pageSize = 10;
let totalPages = 1; // set after fetching total count from backend

const pageNumbersContainer = document.getElementById("page-numbers");
const pageSizeSelect = document.getElementById("page-size");

function renderPageNumbers() {
    pageNumbersContainer.innerHTML = "";
    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i;
        btn.className = i === currentPage ? "active" : "";
        btn.addEventListener("click", () => {
            currentPage = i;
            fetchMovies();
        });
        pageNumbersContainer.appendChild(btn);
    }
}

document.getElementById("prev-page").addEventListener("click", () => {
    if (currentPage > 1) {
        currentPage--;
        fetchMovies();
    }
});

document.getElementById("next-page").addEventListener("click", () => {
    if (currentPage < totalPages) {
        currentPage++;
        fetchMovies();
    }
});

pageSizeSelect.addEventListener("change", () => {
    pageSize = parseInt(pageSizeSelect.value);
    currentPage = 1;
    fetchMovies();
});

function fetchMovies() {
    const offset = (currentPage - 1) * pageSize;
    const url = `api/topmovies?pageSize=${pageSize}&offset=${offset}`;

    jQuery.ajax({
        url: url,
        method: "GET",
        dataType: "json",
        // success: (data) => {
        //     totalPages = Math.ceil(data.totalCount / pageSize); // update totalPages
        //     renderPageNumbers();
        // }
        success: (resultData) => handleResult(resultData)
    });
}

pageSizeSelect.addEventListener("change", () => {
    pageSize = parseInt(pageSizeSelect.value);
    currentPage = 1;
    fetchMovies();
});


function handleResult(resultData) {
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie-table-body");
    movieTableBodyElement.empty();
    console.log(movieTableBodyElement);

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<td>" + (i + 1) + ". <a href='movie.html?id=" +
            encodeURIComponent(resultData[i]["movieID"]) + "'>" +
            resultData[i]["movieTitle"] + "</a></td>";
        rowHTML += "<td>" + resultData[i]["movieYear"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movieDirector"] + "</td>";

        let genresData = resultData[i]["movieGenres"].split(", ");
        let genreLinks = "";
        for (let j = 0; j < genresData.length; j++) {
            let genre = genresData[j];
            genreLinks += "<a href='movies.html?genre=" + encodeURIComponent(genre) + "'>" + genre + "</a>";
            if (j + 1 < genresData.length) genreLinks += ", ";
        }
        rowHTML += "<td>" + genreLinks + "</td>";

        let starsData = resultData[i]["movieStars"].split(", ");  // ["Fred Astaire", "nm0000001", "Ginger Rogers", "nm0000002"]
        let starLinks = "";
        console.log(starsData);
        for (let j = 0; j < starsData.length; j += 2) {
            let name = starsData[j];
            let id = starsData[j + 1];
            starLinks += "<a href='star.html?id=" + encodeURIComponent(id) + "'>" + name + "</a>";
            if (j + 2 < starsData.length) starLinks += ", ";
        }
        rowHTML += "<td>" + starLinks + "</td>";
        rowHTML += "<td>" + "⭐️ " + resultData[i]["movieRating"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

let title = getParameterByName("title");
let year = getParameterByName("year");
let director = getParameterByName("director");
let star = getParameterByName("star");
let genre = getParameterByName("genre");
let url;
if (!title && !year && !director && !star && !genre) {
    url = "api/topmovies";
} else {
    url = "api/topmovies?";
    if (title) url += "title=" + encodeURIComponent(title) + "&";
    if (year) url += "year=" + encodeURIComponent(year) + "&";
    if (director) url += "director=" + encodeURIComponent(director) + "&";
    if (genre) url += "genre=" + encodeURIComponent(genre) + "&";
    if (star) url += "star=" + encodeURIComponent(star);
}
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: url, // Setting request url
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});
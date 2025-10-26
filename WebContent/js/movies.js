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
            fetchMovies();
        });
    });
});

let currentPage = 1;
let pageSize = 10;
let totalPages = 1;

const pageNumber = document.getElementById("page-numbers");
const pageSizeSelect = document.getElementById("page-size");

function renderPageNumbers() {
    pageNumber.textContent = `Page ${currentPage} of ${totalPages}`;
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

function handleResult(resultData, cartData) {
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie-table-body");
    movieTableBodyElement.empty();
    console.log(movieTableBodyElement);

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.movies.length; i++) {
        let movie = resultData.movies[i];
        let inCart = Array.isArray(cartData) && cartData.some(item => item.movieID === movie.movieID);
        let buttonText = inCart ? "✔ Added" : "Add";
        let disabledAttr = inCart ? "disabled" : "";
        let rowHTML = "<tr>";
        rowHTML += `<td>$122 <br>
            <button class="btn btn-sm btn-success mt-1" 
                data-movie-id="${movie.movieID}"
                onclick="addToCart('${movie.movieID}', '${movie.movieTitle}', 122)"
                ${disabledAttr}>
                ${buttonText}
            </button>
        </td>`;
        const startIndex = (currentPage - 1) * pageSize;
        rowHTML += "<td>" + (startIndex + i + 1) + ". <a href='movie.html?id=" +
            encodeURIComponent(movie["movieID"]) + "'>" +
            movie["movieTitle"] + "</a></td>";
        rowHTML += "<td>" + movie["movieYear"] + "</td>";
        rowHTML += "<td>" + movie["movieDirector"] + "</td>";

        let genresData = movie["movieGenres"].split(", ");
        let genreLinks = "";
        for (let j = 0; j < genresData.length; j++) {
            let genre = genresData[j];
            genreLinks += "<a href='movies.html?genre=" + encodeURIComponent(genre) + "'>" + genre + "</a>";
            if (j + 1 < genresData.length) genreLinks += ", ";
        }
        rowHTML += "<td>" + genreLinks + "</td>";

        let starsData = movie["movieStars"].split(", ");  // ["Fred Astaire", "nm0000001", "Ginger Rogers", "nm0000002"]
        let starLinks = "";
        console.log(starsData);
        for (let j = 0; j < starsData.length; j += 2) {
            let name = starsData[j];
            let id = starsData[j + 1];
            starLinks += "<a href='star.html?id=" + encodeURIComponent(id) + "'>" + name + "</a>";
            if (j + 2 < starsData.length) starLinks += ", ";
        }
        rowHTML += "<td>" + starLinks + "</td>";
        rowHTML += "<td>" + "⭐️ " + movie["movieRating"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
        renderPageNumbers();
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Makes the HTTP GET request and registers on success callback function handleResult

function fetchMovies() {
    let title = getParameterByName("title");
    let year = getParameterByName("year");
    let director = getParameterByName("director");
    let star = getParameterByName("star");
    let genre = getParameterByName("genre");
    let back = getParameterByName("restore");
    let prefix = getParameterByName("prefix");

    // Page heading & tab title for alphanumeric browsing
    const heading = $("#h3-title");
    if (genre && genre.trim() !== "") {
        heading.text(`${genre} Movies`);
        document.title = `${genre} Movies - Fabflix`;
    } else if (prefix && prefix.trim() !== "") {
        heading.text(`Titles starting with “${prefix.toUpperCase()}”`);
        document.title = `Titles starting with ${prefix.toUpperCase()} - Fabflix`;
    } else {
        heading.text("Top Rated Movies");
        document.title = "Top Rated Movies - Fabflix";
    }

    // Check for selected options (rating or title)
    const selectedSortFieldEl = document.querySelector(".sort-group .sort-option.selected[data-field]:not([data-order])");
    const selectedOrderEl     = document.querySelector(".sort-group .sort-option.selected[data-order]");

    // Fallbacks
    let sortField = (selectedSortFieldEl && selectedSortFieldEl.dataset.field) ? selectedSortFieldEl.dataset.field : "rating";
    let sortOrder = (selectedOrderEl && selectedOrderEl.dataset.order) ? selectedOrderEl.dataset.order : "desc";

    const offset = (currentPage - 1) * pageSize;
    let url;
    if (back === "true") url = "api/topmovies?restore=true";
    else if (!title && !year && !director && !star && !genre && !prefix) {
        url = `api/topmovies?pageSize=${pageSize}&offset=${offset}&sortField=${encodeURIComponent(sortField)}&sortOrder=${encodeURIComponent(sortOrder)}&currentPage=${currentPage}`;
    } else {
        url = "api/topmovies?";
        if (title) url += "title=" + encodeURIComponent(title) + "&";
        if (year) url += "year=" + encodeURIComponent(year) + "&";
        if (director) url += "director=" + encodeURIComponent(director) + "&";
        if (genre) url += "genre=" + encodeURIComponent(genre) + "&";
        if (star) url += "star=" + encodeURIComponent(star) + "&";
        if (prefix) url += "prefix=" + encodeURIComponent(prefix) + "&";
        url += `pageSize=${pageSize}&offset=${offset}&sortField=${encodeURIComponent(sortField)}&sortOrder=${encodeURIComponent(sortOrder)}&currentPage=${currentPage}`;
    }
    jQuery.ajax({
        url: url,
        method: "GET",
        dataType: "json",
        success: (resultData) => {
            currentPage = resultData.currentPage;
            pageSize = resultData.pageSize;
            pageSizeSelect.value = pageSize;

            const serverTotal = Number(resultData.totalCount) || 0;
            totalPages = Math.max(1, Math.ceil(serverTotal / pageSize));

            const currOffset = (currentPage - 1) * pageSize;
            if (currOffset > Math.max(0, serverTotal - 1) && totalPages > 0) {
                currentPage = totalPages;
                return fetchMovies();
            }

            $.getJSON("api/cart", (cartData) => {
                handleResult(resultData, cartData);
                updateCartCount(cartData);
                renderPageNumbers();
            });

            if (back === "true") {
                const offset = (currentPage - 1) * pageSize;
                const newUrl =
                    `?pageSize=${pageSize}&offset=${offset}` +
                    `&sortField=${encodeURIComponent(resultData.sortField || "")}` +
                    `&sortOrder=${encodeURIComponent(resultData.sortOrder || "")}` +
                    `&currentPage=${currentPage}`;
                history.replaceState(null, "", newUrl);
            }
        },
        error: (xhr) => {
            console.error("movies fetch failed:", xhr.status, xhr.responseText);
        }
    });
}
fetchMovies();
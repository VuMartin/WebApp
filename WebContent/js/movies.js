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

let currentPage = 1;
let pageSize = 10;
let totalPages = 1;

const pageNumber = document.getElementById("page-numbers");
const pageSizeSelect = document.getElementById("page-size");

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

function getQueryParams() {
    return {
        title: getParameterByName("title"),
        year: getParameterByName("year"),
        director: getParameterByName("director"),
        star: getParameterByName("star"),
        genre: getParameterByName("genre"),
        prefix: getParameterByName("prefix")
    };
}

function setPageHeading(params) {
    const heading = $("#h3-title");
    if (params.genre) {
        heading.text(`${params.genre} Movies`);
        document.title = `${params.genre} Movies - Fabflix`;
    } else if (params.prefix) {
        heading.text(`Titles starting with "${params.prefix.toUpperCase()}"`);
        document.title = `Titles starting with ${params.prefix.toUpperCase()} - Fabflix`;
    } else {
        heading.text("Top Rated Movies");
        document.title = "Top Rated Movies - Fabflix";
    }
}

function getSorting() {
    const groups = document.querySelectorAll(".sort-group");
    const primarySelected = groups[0].querySelector(".sort-option.selected");
    const ratingSelected  = groups[1].querySelector(".sort-option.selected");
    const titleSelected   = groups[2].querySelector(".sort-option.selected");

    const primaryField = primarySelected.dataset.field;
    const ratingOrder = ratingSelected.dataset.order;
    const titleOrder  = titleSelected.dataset.order;

    let sortPrimaryField, sortPrimaryOrder, sortSecondaryField, sortSecondaryOrder;
    if (primaryField === "rating") {
        sortPrimaryField = "rating"; sortPrimaryOrder = ratingOrder;
        sortSecondaryField = "title"; sortSecondaryOrder = titleOrder;
    } else {
        sortPrimaryField = "title"; sortPrimaryOrder = titleOrder;
        sortSecondaryField = "rating"; sortSecondaryOrder = ratingOrder;
    }

    return { sortPrimaryField, sortPrimaryOrder, sortSecondaryField, sortSecondaryOrder };
}

function buildApiUrl(params, sort, back) {
    const offset = (currentPage - 1) * pageSize;
    const baseParams = new URLSearchParams({
        pageSize,
        offset,
        sortPrimaryField: sort.sortPrimaryField,
        sortPrimaryOrder: sort.sortPrimaryOrder,
        sortSecondaryField: sort.sortSecondaryField,
        sortSecondaryOrder: sort.sortSecondaryOrder,
        currentPage
    }).toString();

    if (back === "true") return `api/topmovies?restore=true`;

    const q = new URLSearchParams();
    if (params.title) q.append("title", params.title);
    if (params.year) q.append("year", params.year);
    if (params.director) q.append("director", params.director);
    if (params.genre) q.append("genre", params.genre);
    if (params.star) q.append("star", params.star);
    if (params.prefix) q.append("prefix", params.prefix);

    const queryString = q.toString();
    return queryString ? `api/topmovies?${queryString}&${baseParams}` : `api/topmovies?${baseParams}`;
}

function restoreSortOptions(data) {
    const groups = document.querySelectorAll(".sort-group");
    const primaryGroup = groups[0];
    const ratingGroup  = groups[1];
    const titleGroup   = groups[2];

    const restoredPrimaryField = data.sortPrimaryField;
    const restoredPrimaryOrder = data.sortPrimaryOrder;
    const restoredSec   = data.sortSecondaryField;
    const restoredSecOrder= data.sortSecondaryOrder;

    // Clear all selections
    document.querySelectorAll(".sort-option").forEach(o => o.classList.remove("selected"));

    // Primary field
    primaryGroup.querySelectorAll(".sort-option").forEach(o => {
        if (o.dataset.field === restoredPrimaryField) o.classList.add("selected");
    });

    // Rating order
    ratingGroup.querySelectorAll(".sort-option").forEach(o => {
        const order = restoredPrimaryField === "rating" ? restoredPrimaryOrder : restoredSecOrder;
        if (o.dataset.order === order) o.classList.add("selected");
    });

    // Title order
    titleGroup.querySelectorAll(".sort-option").forEach(o => {
        const order = restoredPrimaryField === "title" ? restoredPrimaryOrder : restoredSecOrder;
        if (o.dataset.order === order) o.classList.add("selected");
    });
}

function updatePagination(data) {
    const serverTotal = Number(data.totalCount);
    totalPages = Math.max(1, Math.ceil(serverTotal / pageSize));

    const currOffset = (currentPage - 1) * pageSize;
    if (currOffset > Math.max(0, serverTotal - 1) && totalPages > 0) {
        currentPage = totalPages;
        fetchMovies(); // retry with valid page
    }
}

function updateCartCountFromServer() {
    $.getJSON("api/cart", (cartData) => {
        updateCartCount(cartData);
    });
}

function renderPageNumbers() {
    pageNumber.textContent = `Page ${currentPage} of ${totalPages}`;
}

function handleResult(resultData) {
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie-table-body");
    movieTableBodyElement.empty();
    console.log(movieTableBodyElement);

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.movies.length; i++) {
        let movie = resultData.movies[i];
        let rowHTML = "<tr>";
        rowHTML += `<td>$122 <br>
                    <button class="btn btn-sm btn-success mt-1" 
                            data-movie-id="${movie.movieID}"
                            onclick="addToCart('${movie.movieID}', '${movie.movieTitle}', 122)">
                        Add
                    </button>
                    <div class="cart-message" id="message-${movie.movieID}"></div>
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

function fetchMovies() {
    const params = getQueryParams();
    let back = getParameterByName("restore");
    setPageHeading(params);
    const sort = getSorting();
    const url = buildApiUrl(params, sort, back);

    jQuery.ajax({
        url: url,
        method: "GET",
        dataType: "json",
        success: (resultData) => {
            currentPage = resultData.currentPage;
            pageSize = resultData.pageSize;
            pageSizeSelect.value = pageSize;

            restoreSortOptions(resultData);
            updatePagination(resultData);
            handleResult(resultData);
            updateCartCountFromServer();
            renderPageNumbers();

            if (back === "true") {
                const newUrl = `?${baseParams}`;
                history.replaceState(null, "", newUrl);
            }
        },
        error: (xhr) => {
            console.error("movies fetch failed:", xhr.status, xhr.responseText);
        }
    });
}
fetchMovies();
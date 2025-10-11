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

function handleResult(resultData) {
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie-table-body");
    console.log(movieTableBodyElement);

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<td>" + (i + 1) + ". <a href='movie.html?id=" +
            encodeURIComponent(resultData[i]["movieID"]) + "'>" +
            resultData[i]["movieTitle"] + "</a></td>";
        rowHTML += "<td>" + resultData[i]["movieYear"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movieDirector"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movieGenres"] + "</td>";

        let starsData = resultData[i]["movieStars"].split(", ");  // ["Fred Astaire", "nm0000001", "Ginger Rogers", "nm0000002"]
        let starLinks = "";
        console.log(starsData);
        for (let i = 0; i < starsData.length; i += 2) {
            let name = starsData[i];
            let id = starsData[i + 1];
            starLinks += "<a href='star.html?id=" + encodeURIComponent(id) + "'>" + name + "</a>";
            if (i + 2 < starsData.length) starLinks += ", ";
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

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/topmovies", // Setting request url
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});
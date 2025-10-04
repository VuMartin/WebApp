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
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");

    // Populate the movie info
    jQuery("#movieTitle").text(resultData["movieTitle"]);
    jQuery("#movieYear").text(resultData["movieYear"]);
    jQuery("#movieDirector").text(resultData["movieDirector"]);
    jQuery("#movieGenres").text(resultData["movieGenres"]);
    jQuery("#movieStars").text(resultData["movieStars"]);
    // jQuery("#movieRating").text(resultData["movieRating"]);

    let starsContainer = jQuery("#movieStars");
    starsContainer.empty();

    let stars = resultData["movieStars"].split(", ");
    stars.forEach((star, index) => {
        let link = jQuery("<a></a>")
            .text(star)
            .attr("href", "singleStar.html?id=" + encodeURIComponent(star));  // link to star page
        starsContainer.append(link);
        if (index < stars.length - 1) {
            starsContainer.append(", ");
        }
    });
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
// let movieId = getParameterByName('id');
let movieID = "tt0421974";

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie?id=" + movieID, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});
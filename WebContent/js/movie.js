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
    jQuery("#pageTitle").text(
        resultData["movieTitle"] + " (" + resultData["movieYear"] + ") - Fabflix");
    jQuery("#name").text(resultData["movieTitle"] + " (" + resultData["movieYear"] + ")");
    // jQuery("#movieRating").text(resultData["movieRating"] + "/10");
    jQuery("#movieDirector").text(resultData["movieDirector"]);
    jQuery("#movieGenres").text(resultData["movieGenres"]);
    jQuery("#movieStars").text(resultData["movieStars"]);

    let starsData = resultData["movieStars"].split(", ");  // ["Fred Astaire", "nm0000001", "Ginger Rogers", "nm0000002"]
    let starsContainer = jQuery("#movieStars");
    starsContainer.empty();
    console.log(starsData);

    for (let i = 0; i < starsData.length; i += 2) {
        let name = starsData[i];
        let id = starsData[i + 1];

        let link = jQuery("<a></a>")
            .text(name)
            .attr("href", "star.html?id=" + encodeURIComponent(id));

        starsContainer.append(link);

        if (i + 2 < starsData.length) {
            starsContainer.append(", ");
        }
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieID = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie?id=" + movieID, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});
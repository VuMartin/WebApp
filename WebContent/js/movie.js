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

function handleResult(resultData, cartData) {
    console.log("handleResult: populating movie info from resultData");

    // Populate the movie info
    jQuery("#pageTitle").text(
        resultData["movieTitle"] + " (" + resultData["movieYear"] + ") - Fabflix");
    jQuery("#name").text(resultData["movieTitle"] + " (" + resultData["movieYear"] + ")");
    jQuery("#movieRating").text(resultData["movieRating"] + "/10");
    jQuery("#movieDirector").text(resultData["movieDirector"]);

    // Genres (link to browse-by-genre on movies.html later)
    const genresEl = $("#movieGenres").empty();
    (resultData.movieGenres || []).forEach((g, i) => {
        $("<a>").text(g.name)
            .attr("href", "movies.html?genre=" + encodeURIComponent(g.name))
            .appendTo(genresEl);
        if (i < resultData.movieGenres.length - 1) genresEl.append(", ");
    });

    // Stars (sorted server-side)
    const starsEl = $("#movieStars").empty();
    (resultData.movieStars || []).forEach((s, i) => {
        $("<a>").text(s.name)
            .attr("href", "star.html?id=" + encodeURIComponent(s.id))
            .appendTo(starsEl);
        if (i < resultData.movieStars.length - 1) starsEl.append(", ");
    });
    const cardEl = $(".card");

    // Check if movie is already in cart
    let inCart = cartData.some(item => item.movieID === resultData.movieID);
    let buttonText = inCart ? "✔ Added" : "Add to Cart";
    let disabledAttr = inCart ? "disabled" : "";

    // Create the button
    let button = $(`<button id="add-to-cart" class="btn" ${disabledAttr}>${buttonText}</button>`);
    button.css({
        "display": "block",
        "margin": "1rem auto",  // center the button
        "padding": "0.25rem 0.5rem",
        "font-size": "15px",
        "background-color": "#28a745",
        "color": "white",
        "border-radius": "0.25rem",
        "width": "120px"
    });

    // Add click handler
    button.on("click", () => {
        addToCart(resultData.movieID, resultData.movieTitle, 122);
        button.text("✔ Added");
        button.prop("disabled", true);
    });

    // Append button to card
    cardEl.append(button)
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
    success: (resultData) => {
        $.getJSON("api/cart", (cartData) => {
            handleResult(resultData, cartData);
            updateCartCount(cartData);
        });
    }
});
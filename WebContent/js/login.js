/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    if (resultData.status === "success") {
        // Store first name in sessionStorage (optional, for later pages)
        sessionStorage.setItem("firstName", resultData.username);
        // Redirect to main page
        window.location.href = "main.html";
    } else {
        // Login failed → show error on the same page
        jQuery("#error-msg").text(resultData.message);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Capture the login form submission
jQuery("#login-form").submit(function(event) {
    event.preventDefault(); // prevent normal form submit

    // Collect form data
    let email = jQuery("#email").val();
    let password = jQuery("#password").val();

    // AJAX POST request to LoginServlet
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/login",
        data: { email: email, password: password },
        success: (resultData) => handleResult(resultData),
        error: () => {
            jQuery("#error-msg").text("Server error. Try again later.");
        }
    });
});
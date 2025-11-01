/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    if (resultData.status === "success") {
        // Store first name in sessionStorage
        sessionStorage.setItem("fullname", resultData.username);
        // Redirect to main page
        window.location.href = "employee.html";
    } else {
        // Login failed → show error on the same page
        jQuery("#error-msg").text(resultData.message);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Capture the login form submission
jQuery("#employee-login-form").submit(function(event) {
    event.preventDefault(); // prevent normal form submit

    let email = jQuery("#email").val();
    let password = jQuery("#password").val();
    let gRecaptchaResponse = grecaptcha.getResponse(); // get token
    if (!gRecaptchaResponse) {
        jQuery("#error-msg").text("Please complete the reCAPTCHA.");
        return;
    }

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/_dashboard",
        data: { email: email, password: password, "g-recaptcha-response": gRecaptchaResponse },
        success: (resultData) => {
            grecaptcha.reset();
            handleResult(resultData)
        },
        error: () => {
            grecaptcha.reset();
            jQuery("#error-msg").text("Server error. Try again later.");
        }
    });
});
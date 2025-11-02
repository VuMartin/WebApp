$("#payment-form").submit(function(event) {
    event.preventDefault();

    let firstName = $("#firstName").val();
    let lastName = $("#lastName").val();
    let cardNumber = $("#cardNumber").val();
    let expiration = $("#expiration").val();

    $.ajax({
        url: "/2025_fall_cs_122b_marjoe_war/api/payment",
        method: "POST",
        dataType: "json",
        data: { firstName, lastName, cardNumber, expiration },
        success: (resultData) => {
            if (resultData.status === "success") {
                window.location.href = "/2025_fall_cs_122b_marjoe_war/html/customer/onfirmation.html";
            } else {
                $("#error-message").text(resultData.message).show();
            }
        },
        error: () => {
            $("#error-message").text("Server error. Please try again later.").show();
        }
    });
});

$.ajax("/2025_fall_cs_122b_marjoe_war/api/cart", {
    method: "GET",
    success: (resultData) => {
        updateCartCount(resultData);
        $("#total-price").text(resultData.total.toFixed(2));
    }
});
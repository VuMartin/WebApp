function emptyCart() {
    $.ajax({
        type: "POST",
        url: "/2025_fall_cs_122b_marjoe_war/api/cart",
        data: { action: "empty" }
    });
}

$.ajax({
    type: "GET",
    url: "/2025_fall_cs_122b_marjoe_war/api/cart",
    dataType: "json",
    success: function(resultData) {
        // Render the confirmation
        $("#customerName").text(resultData.firstName);
        $("#orderNumber").text(resultData.orderNumber);
        $("#totalPrice").text(resultData.total.toFixed(2));
        $("#cardLast2").text(resultData.cardNumber.slice(-2));

        let orderList = $("#orderList");
        orderList.empty();
        resultData.items.forEach(item => {
            orderList.append(`<li>${item.title} × ${item.quantity}</li>`);
        });
        emptyCart();
    },
    error: function() {
        alert("Error processing order.");
    }
});
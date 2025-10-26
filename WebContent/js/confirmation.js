jQuery.ajax({
    type: "POST",
    url: "api/payment",
    success: function(resultData) {
        $("#customerName").text(resultData.firstName);
        $("#totalPrice").text(resultData.total.toFixed(2));
        $("#cardLast2").text(resultData.cardNumber.slice(-2));

        let orderList = $("#orderList");
        orderList.empty();
        resultData.items.forEach(item => {
            orderList.append(`<li>${item.title} × ${item.quantity}</li>`);
        });
    },
    error: function() {
        alert("Error processing order.");
    }
});
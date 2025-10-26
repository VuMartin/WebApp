$.ajax({
    type: "GET",
    url: "api/cart",
    dataType: "json",
    success: function(resultData) {
        $("#customerName").text(resultData.firstName);
        $("#totalPrice").text(resultData.total.toFixed(2));
        $("#cardLast2").text(resultData.cardNumber.slice(-2));

        let orderList = $("#orderList");
        orderList.empty();
        resultData.items.forEach(item => {
            orderList.append(`<li>${item.title} × ${item.quantity}</li>`);
        });
        $.ajax({
            type: "POST",
            url: "api/cart",
            data: { action: "empty" },
        });
    },
    error: function() {
        alert("Error processing order.");
    }
});
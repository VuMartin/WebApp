function addToCart(movieID, title, price = 122) {
    $.ajax("/api/cart", {
        method: "POST",
        data: { movieID, title, price, action: "add" },
        success: (resultData) => {
            updateCartCount(resultData);

            // Show the message under the button
            const messageEl = document.getElementById(`message-${movieID}`);
            if (messageEl) {
                messageEl.textContent = "✔ Added to cart!";
                messageEl.style.display = "block";
                setTimeout(() => {
                    messageEl.style.display = "none";
                }, 1000);
            }
        }
    });
}
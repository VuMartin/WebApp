function addToCart(movieID, title, price = 122) {
    const button = document.querySelector(`button[data-movie-id='${movieID}']`);
    $.ajax("api/cart", {
        method: "POST",
        data: { movieID, title, price, action: "add" },
        success: (resultData) => {
            updateCartCount(resultData);
            if (button) {
                button.textContent = "✔ Added";
                button.disabled = true;
            }
        }
    });
}
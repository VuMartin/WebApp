document.addEventListener("DOMContentLoaded", () => {
    $.getJSON("/api/cart", (cartData) => {
        updateCartCount(cartData);
    });
});
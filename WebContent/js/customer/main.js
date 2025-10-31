$.getJSON("api/cart", (cartData) => {
    updateCartCount(cartData);
});
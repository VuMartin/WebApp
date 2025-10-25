// Render the cart table
function displayCart(cartData) {
    let tbody = $("#cart-items");
    tbody.empty();

    let totalPrice = 0;
    cartData.forEach(item => {
        let movieTotal = item.price * item.quantity;
        totalPrice += movieTotal;

        let row = `<tr data-movie-id="${item.movieId}">
            <td>${item.title}</td>
            <td><input type="number" class="form-control shop-quantity-input" value="${item.quantity}" min="1"></td>
            <td>$${item.price}</td>
            <td>$${movieTotal}</td>
            <td>
                <button class="btn btn-danger btn-sm remove-btn">Remove</button>
            </td>
        </tr>`;
        tbody.append(row);
    });

    $("#shop-total").text(`Total: $${totalPrice}`);
}

// Update quantity or remove item
$("#cart-items").on("change", ".shop-quantity-input", function() {
    let row = $(this).closest("tr");
    let movieId = row.data("movie-id");
    let newQty = parseInt($(this).val());

    $.ajax("api/cart", {
        method: "POST",
        data: { movieId, quantity: newQty, action: "update" },
        success: (resultData) => {
            displayCart(resultData);
        }
    });
});

$("#cart-items").on("click", ".remove-btn", function() {
    let row = $(this).closest("tr");
    let movieId = row.data("movie-id");

    $.ajax("api/cart", {
        method: "POST",
        data: { movieId, action: "remove" },
        success: (resultData) => {
            displayCart(resultData);
        }
    });
});

function loadCart() {
    $.ajax("api/cart", {
        method: "GET",
        success: (resultData) => {
            displayCart(resultData);
        }
    });
}

// let testCartData = [
//     { movieId: "m1", title: "Inception", quantity: 1, price: 10 },
//     { movieId: "m2", title: "Interstellar", quantity: 2, price: 12 }
// ];
//
// // call the function to test UI
// displayCart(testCartData);

loadCart();

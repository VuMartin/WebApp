// Render the cart table
function displayCart(resultData) {
    let tbody = $("#cart-items");
    tbody.empty();

    let totalPrice = 0;
    resultData.items.forEach(item => {
        let movieTotal = item.price * item.quantity;
        totalPrice += movieTotal;

        let row = `<tr data-movie-id="${item.movieID}">
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

    $("#shop-total").text(`Total: $${resultData.total.toFixed(2)}`);
}

// Update quantity or remove item
$("#cart-items").on("change", ".shop-quantity-input", function() {
    let row = $(this).closest("tr");
    let movieID = row.data("movie-id");
    let newQty = parseInt($(this).val());

    $.ajax("/2025_fall_cs_122b_marjoe_war/api/cart", {
        method: "POST",
        data: { movieID, quantity: newQty, action: "update" },
        success: (resultData) => {
            updateCartCount(resultData)
            displayCart(resultData);
        }
    });
});

$("#cart-items").on("click", ".remove-btn", function() {
    let row = $(this).closest("tr");
    let movieID = row.data("movie-id");

    $.ajax("/2025_fall_cs_122b_marjoe_war/api/cart", {
        method: "POST",
        data: { movieID, action: "remove" },
        success: (resultData) => {
            updateCartCount(resultData)
            displayCart(resultData);
        }
    });
});

function loadCart() {
    $.ajax("/2025_fall_cs_122b_marjoe_war/api/cart", {
        method: "GET",
        success: (resultData) => {
            updateCartCount(resultData);
            displayCart(resultData);
        }
    });
}

// let testCartData = [
//     { movieID: "m1", title: "Inception", quantity: 1, price: 10 },
//     { movieID: "m2", title: "Interstellar", quantity: 2, price: 12 }
// ];
//
// // call the function to test UI
// displayCart(testCartData);

loadCart();

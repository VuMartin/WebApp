import React, { useState, useEffect } from 'react';
import HeaderSection from './HeaderSection';
import BrowseSection from './BrowseSection';
import { useCart } from './CartContext';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import { Link } from 'react-router-dom';

function CartPage() {
    const {
        cartItems,
        cartTotal,
        fetchCart,
        updateQuantity,
        removeFromCart
    } = useCart();

    useEffect(() => {
        fetchCart();
    }, []);

    return (
        <>
            <HeaderSection />
            <div className="container">
                <h1 className="mb-4">Your Shopping Cart</h1>
                <table className="table table-striped">
                    <thead>
                    <tr>
                        <th>Movie Title</th>
                        <th id="shop-quantity">Quantity</th>
                        <th>Price</th>
                        <th>Total</th>
                        <th id="shop-actions">Actions</th>
                    </tr>
                    </thead>
                    <tbody id="cart-items">
                    {cartItems.map(item => {
                        const movieTotal = item.price * item.quantity;
                        return (
                            <tr key={item.movieID} data-movie-id={item.movieID}>
                                <td>{item.title}</td>
                                <td>
                                    <input
                                        type="number"
                                        className="form-control shop-quantity-input"
                                        value={item.quantity}
                                        min="1"
                                        onChange={(e) => updateQuantity(item.movieID, parseInt(e.target.value))}
                                    />
                                </td>
                                <td>${item.price}</td>
                                <td>${movieTotal.toFixed(2)}</td>
                                <td>
                                    <button
                                        className="btn btn-danger btn-sm remove-btn"
                                        onClick={() => removeFromCart(item.movieID)}
                                    >
                                        Remove
                                    </button>
                                </td>
                            </tr>
                        );
                    })}
                    </tbody>
                </table>

                <div className="d-flex justify-content-end mb-3">
                    <h4 id="shop-total">Total: ${cartTotal.toFixed(2)}</h4>
                </div>

                <div id="shop-proceed" className="d-flex justify-content-end">
                    <Link to="/payment" className="btn btn-success">
                        Proceed to Payment
                    </Link>
                </div>
            </div>
            <BrowseSection />
        </>
    );
}

export default CartPage;
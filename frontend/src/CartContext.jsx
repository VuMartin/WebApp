import { createContext, useState, useContext } from "react";

const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [cartCount, setCartCount] = useState(0);
    const [cartItems, setCartItems] = useState([]);
    const [cartTotal, setCartTotal] = useState(0);

    const fetchCart = async () => {
        try {
            const res = await fetch("/api/cart");
            const data = await res.json();
            setCartCount(data.totalCount || 0);
            setCartItems(data.items || []);
            setCartTotal(data.total || 0);
        } catch (err) {
            console.error("Fetch cart failed", err);
        }
    };

    const addToCart = async (movieID, title, price = 122) => {
        try {
            const res = await fetch("/api/cart", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ movieID, title, price, action: "add" }),
            });
            const data = await res.json();
            setCartCount(data.totalCount || 0);
            setCartItems(data.items || []);
            setCartTotal(data.total || 0);
        } catch (err) {
            console.error("Add to cart failed", err);
        }
    };

    const updateQuantity = async (movieID, quantity) => {
        try {
            const res = await fetch("/api/cart", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ movieID, quantity, action: "update" }),
            });
            const data = await res.json();
            setCartCount(data.totalCount || 0);
            setCartItems(data.items || []);
            setCartTotal(data.total || 0);
        } catch (err) {
            console.error("Update cart failed", err);
        }
    };

    const removeFromCart = async (movieID) => {
        try {
            const res = await fetch("/api/cart", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ movieID, action: "remove" }),
            });
            const data = await res.json();
            setCartCount(data.totalCount || 0);
            setCartItems(data.items || []);
            setCartTotal(data.total || 0);
        } catch (err) {
            console.error("Remove from cart failed", err);
        }
    };

    return (
        <CartContext.Provider value={{
            cartCount,
            cartItems,
            cartTotal,
            fetchCart,
            addToCart,
            updateQuantity,
            removeFromCart
        }}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => useContext(CartContext);
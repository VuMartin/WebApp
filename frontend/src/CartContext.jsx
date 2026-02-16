import { createContext, useState, useContext } from "react";

const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [cartCount, setCartCount] = useState(0);

    const addToCart = async (movieID, title, price = 122) => {
        try {
            const res = await fetch("/api/cart", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ movieID, title, price, action: "add" }),
            });
            const resultData = await res.json();
            setCartCount(resultData.totalCount || 0);
        } catch (err) {
            console.error("Add to cart failed", err);
        }
    };

    return (
        <CartContext.Provider value={{ cartCount, addToCart }}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => useContext(CartContext);
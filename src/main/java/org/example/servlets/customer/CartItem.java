package main.java.org.example.servlets.customer;

public class CartItem {
    private final String movieID;
    private final String title;
    private final double price;
    private int quantity;

    public CartItem(String movieId, String title, double price, int quantity) {
        this.movieID = movieId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    // getters
    public String getMovieID() { return movieID; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    // setters
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
# README

## Prepared Statement Files
- /src/main/java/org/example/servlets/customer/MovieListServlet.java
- /src/main/java/org/example/servlets/customer/GenresServlet.java
- /src/main/java/org/example/servlets/customer/LoginServlet.java
- /src/main/java/org/example/servlets/customer/PaymentServlet.java
- /src/main/java/org/example/servlets/customer/SingleMovieServlet.java
- /src/main/java/org/example/servlets/customer/SingleStarServlet.java
- /src/main/java/org/example/servlets/customer/AddStarServlet.java
- /src/main/java/org/example/servlets/customer/EmployeeLoginServlet.java
- /src/main/java/org/example/servlets/customer/InsertEmployee.java
- /src/main/java/org/example/servlets/customer/MetadataServlet.java

## Optimization Report

1. Batch Database Operations
   Before: Each movie, star, genre, or star-movie relationship was inserted individually into the database.
   After: Records were grouped into batches of 1000 using .addBatch() and executeBatch(), reducing the number of database round-trips.

2. Cache on IDs and Already Seen Data
   Before: Every new movie or star required a SELECT MAX(id) and repeated checks to see if it existed in the database.
   After: Maximum IDs were queried once at the start and incremented locally. Maps were used to cache IDs and track which movies, stars, genres, and relationships had already been processed, avoiding repeated database queries.

Performance Improvement:
Before Optimization: Parsing time was at least 15 minutes (stopped because it took too long).
After Optimization: Parsing completed in approximately 5–6 minutes.


import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import HeaderSection from './HeaderSection';
import BrowseSection from './BrowseSection';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function MoviePage() {
    const { id } = useParams();
    const [movie, setMovie] = useState(null);

    useEffect(() => {
        const loadMovie = async () => {
            const res = await fetch(`/api/movie?id=${id}`);
            const resultData = await res.json();

            setMovie(resultData);
            document.title = `${resultData.movieTitle} (${resultData.movieYear}) - Fabflix`;
        };
        loadMovie();
    }, [id]);

    const handleAddToCart = () => {
        // Add to cart logic
        fetch('/api/cart', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
                movieID: id,
                title: movie.movieTitle,
                price: 122,
                action: 'add'
            })
        })
            .then(res => res.json());
    };

    if (!movie) return;

    return (
        <>
            <HeaderSection />
            <div className="movie-container">
                <div className="card">
                    <h3 id="name">{movie.movieTitle} ({movie.movieYear})</h3>
                    <p>⭐️ <span id="movieRating">
                        {movie.movieRating !== "N/A" ? `${movie.movieRating}/10` : "N/A"}
                    </span></p>
                    <p>Director: <span id="movieDirector">{movie.movieDirector}</span></p>
                    <p>Genres: <span id="movieGenres">
                        {movie.movieGenres?.map((genre, index) => (
                            <React.Fragment key={genre.name}>
                                <a href={`/genre=${encodeURIComponent(genre.name)}`}>
                                    {genre.name}
                                </a>
                                {index < movie.movieGenres.length - 1 && ', '}
                            </React.Fragment>
                        )) || 'N/A'}
                    </span></p>
                    <p>Stars: <span id="movieStars">
                        {movie.movieStars?.map((star, index) => (
                            <React.Fragment key={star.star_id}>
                                <a href={`/star/${star.star_id}`}>
                                    {star.star_name}
                                </a>
                                {index < movie.movieStars.length - 1 && ', '}
                            </React.Fragment>
                        )) || 'N/A'}
                    </span></p>

                    <button
                        id="add-to-cart"
                        className="btn btn-primary"
                        onClick={handleAddToCart}
                    >
                        Add to Cart
                    </button>
                    <div className="cart-message" id={`message-${movie.movieID}`}></div>
                </div>
            </div>
            <BrowseSection />
        </>
    );
}

export default MoviePage;
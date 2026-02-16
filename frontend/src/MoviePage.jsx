import React, { useState, useEffect } from 'react';
import { useParams, Link} from 'react-router-dom';
import HeaderSection from './HeaderSection';
import BrowseSection from './BrowseSection';
import { useCart } from './CartContext';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function MoviePage() {
    const { id } = useParams();
    const [movie, setMovie] = useState(null);
    const { addToCart } = useCart();
    const [addedId, setAddedId] = useState(null);

    useEffect(() => {
        const loadMovie = async () => {
            const res = await fetch(`/api/movie?id=${id}`);
            const resultData = await res.json();

            setMovie(resultData);
            document.title = `${resultData.movieTitle} (${resultData.movieYear}) - Fabflix`;
        };
        loadMovie();
    }, [id]);

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
                                <Link to={`/movies?genre=${encodeURIComponent(genre.name)}`}>
                                    {genre.name}
                                </Link>
                                {index < movie.movieGenres.length - 1 && ', '}
                            </React.Fragment>
                        )) || 'N/A'}
                    </span></p>
                    <p>Stars: <span id="movieStars">
                        {movie.movieStars?.map((star, index) => (
                            <React.Fragment key={star.star_id}>
                                <Link to={`/star/${star.star_id}`}>
                                    {star.star_name}
                                </Link>
                                {index < movie.movieStars.length - 1 && ', '}
                            </React.Fragment>
                        )) || 'N/A'}
                    </span></p>

                    <button
                        id="add-to-cart"
                        className="btn btn-primary"
                        onClick={async () => {
                            await addToCart(movie.movieID, movie.movieTitle, 122);
                            setAddedId(movie.movieID);
                            setTimeout(() => setAddedId(null), 1000);
                        }}
                    >
                        Add to cart
                    </button>
                    <div className="cart-message">
                        {addedId === movie.movieID && "✔ Added to cart!"}
                    </div>
                </div>
            </div>
            <BrowseSection />
        </>
    );
}

export default MoviePage;
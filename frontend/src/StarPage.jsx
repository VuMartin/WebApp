import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import HeaderSection from './HeaderSection';
import BrowseSection from './BrowseSection';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function StarPage() {
    const { id } = useParams();
    const [star, setStar] = useState(null);

    useEffect(() => {
        fetch(`/api/single-star?id=${id}`)
            .then(res => res.json())
            .then(resultData => {
                setStar(resultData);
                document.title = `${resultData.star_name} - Fabflix`;
            });

        // fetch('/api/cart')
        //     .then(res => res.json())
        //     .then(data => setCartCount(data.totalCount || 0));
    }, [id]);

    if (!star) return;

    return (
        <>
            <HeaderSection />
            <div className="movie-container">
                <div className="card">
                    <h3 id="star-name">{star.star_name}</h3>
                    <p>Birth Year: <span id="birthYear">{star.star_dob || 'N/A'}</span></p>
                    <p>Movies: <span id="movies">
                        {star.movies?.length > 0 ? (
                            star.movies.map((movie, index) => (
                                <React.Fragment key={movie.movieID}>
                                    <a href={`/movie/${movie.movieID}`}>
                                        {movie.movieTitle}
                                    </a>
                                    {index < star.movies.length - 1 && ', '}
                                </React.Fragment>
                            ))
                        ) : 'No movies on record'}
                    </span></p>
                </div>
            </div>
            <BrowseSection />
        </>
    );
}

export default StarPage;
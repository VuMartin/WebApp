// Browse.jsx
import React, { useState, useEffect } from 'react';
import $ from 'jquery';
import './App.css';

function Browse() {
    const [genres, setGenres] = useState([]);

    useEffect(() => {
        // Fetch genres
        fetch('/api/genres')
            .then(res => res.json())
            .then(data => setGenres(data || []));
    }, []);

    // Generate A-Z, 0-9 links
    const alphaLinks = [];
    for (let d = 0; d <= 9; d++) {
        alphaLinks.push(String(d));
    }
    for (let c = 65; c <= 90; c++) {
        alphaLinks.push(String.fromCharCode(c));
    }

    return (
        <div className="browse-section">
            <div className="main-title">Browse By:</div>

            <div className="main-title">Genres</div>
            <div className="genre-section">
                <ul id="genresList" className="genre-list">
                    {genres.map((genre, index) => (
                        <li key={index}>
                            <a href={`/html/customer/movies.html?genre=${encodeURIComponent(genre.name)}`}>
                                {genre.name}
                            </a>
                        </li>
                    ))}
                </ul>
            </div>

            <div className="browse-section">
                <div className="main-title">Titles</div>
                <ul id="alphaList" className="alpha-list">
                    {alphaLinks.map((letter, index) => (
                        <li key={index}>
                            <a href={`/html/customer/movies.html?prefix=${encodeURIComponent(letter)}`}>
                                {letter}
                            </a>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default Browse;
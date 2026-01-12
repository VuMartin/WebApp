// Header.jsx
import React, { useState, useEffect, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import $ from 'jquery';
import './App.css';

function Header() {
    const [cartCount, setCartCount] = useState(0);
    const [firstName, setFirstName] = useState('');
    const [dropdownVisible, setDropdownVisible] = useState(false);
    const [searchOptionsVisible, setSearchOptionsVisible] = useState(false);
    const searchInputRef = useRef();
    const location = useLocation();
    const showBackArrow = !(location.pathname === '/main' || location.pathname === '/movies');

    useEffect(() => {
        // Load cart count
        fetch('/api/cart')
            .then(res => res.json())
            .then(data => setCartCount(data.totalCount || 0));
        setFirstName(sessionStorage.getItem('firstName') || 'User');
    }, []);

    const handleLogout = () => {
        fetch('/api/logout', { method: 'POST' })
            .then(() => {
                window.location.href = '/';
            })
            .catch(() => alert('Logout failed. Try again.'));
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();

        const searchInput = document.getElementById('search-input');
        const selectedSuggestion = $(searchInput).data('autocomplete')?.selectedItem;

        // if (selectedSuggestion)
        //     window.location.href = `/html/customer/movie.html?id=${selectedSuggestion.data.movieId}`;
        handleNormalSearch();
    };

    const handleNormalSearch = () => {
        const title = document.getElementById('search-input').value;
        const year = document.getElementById('search-year').value;
        const director = document.getElementById('search-director').value;
        const star = document.getElementById('search-star').value;

        let url = "/html/customer/movies.html?";
        if (title) url += `title=${encodeURIComponent(title)}&`;
        if (year) url += `year=${encodeURIComponent(year)}&`;
        if (director) url += `director=${encodeURIComponent(director)}&`;
        if (star) url += `star=${encodeURIComponent(star)}`;

        // Remove trailing & or ?
        url = url.replace(/[&?]$/, '');

        window.location.href = url;
    };

    // useEffect(() => {
    //     if (searchInputRef.current) {
    //         $(searchInputRef.current).autocomplete({
    //             lookup: function(query, doneCallback) {
    //                 handleLookup(query, doneCallback);
    //             },
    //             onSelect: function(suggestion) {
    //                 handleSelectSuggestion(suggestion);
    //             },
    //             deferRequestBy: 300,
    //             minChars: 3,
    //             triggerSelectOnValidInput: false
    //         });
    //     }
    // }, []);

    const handleLookup = (query, doneCallback) => {
        const cached = getCachedSuggestions(query);
        if (cached) {
            doneCallback({ suggestions: cached });
            return;
        }

        fetch(`/api/autocomplete?query=${encodeURIComponent(query)}`)
            .then(res => res.json())
            .then(data => {
                setCacheSuggestions(query, data.suggestions);
                doneCallback({ suggestions: data.suggestions });
            })
            .catch(error => {
                console.log("autocomplete error:", error);
                doneCallback({ suggestions: [] });
            });
    };

    const handleSelectSuggestion = (suggestion) => {
        window.location.href = `/html/customer/movie.html?id=${suggestion.data.movieId}`;
    };

    const getCachedSuggestions = (query) => {
        const cache = JSON.parse(sessionStorage.getItem('autocompleteCache') || '{}');
        return cache[query];
    };

    const setCacheSuggestions = (query, suggestions) => {
        const cache = JSON.parse(sessionStorage.getItem('autocompleteCache') || '{}');
        cache[query] = suggestions;
        sessionStorage.setItem('autocompleteCache', JSON.stringify(cache));
    };

    return (
        <header id="movie-page-header">
            {showBackArrow && <span className="arrow">←</span>}
            {showBackArrow && <a href="/html/customer/movies.html?restore=true" className="back-link">Back</a>}

            <h1 id="site-name">
                <a href="/main">FABFLIX</a>
            </h1>

            <form id="search-form" onSubmit={handleSearchSubmit}>
                <button type="button" id="search-options-button"
                        onClick={() => setSearchOptionsVisible(!searchOptionsVisible)}>
                    ▼
                </button>

                <div id="search-options" className={searchOptionsVisible ? 'visible' : 'hidden'}>
                    <span> Search by: </span>
                    <input type="text" id="search-year" placeholder="Year" />
                    <input type="text" id="search-director" placeholder="Director" />
                    <input type="text" id="search-star" placeholder="Star Name" />
                </div>

                <input
                    ref={searchInputRef}
                    type="text"
                    id="search-input"
                    placeholder="Search movies by title..."
                    autoComplete="off"
                />
                <button type="submit">
                    <img id="button-icon" src="/images/search-icon.png" alt="Search" />
                </button>
            </form>

            <div id="user-dropdown">
                <a href="/html/customer/cart.html" id="cart-link">
                    <img id="cart-icon" src="/images/cart-icon.png" alt="Cart" />
                    <span id="cart-count">{cartCount}</span>
                </a>

                <span id="username" onClick={() => setDropdownVisible(!dropdownVisible)}>
                    {firstName} ▼
                </span>

                <div id="dropdown-menu" className={dropdownVisible ? 'visible' : 'hidden'}>
                    <button id="logout-button" onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </div>
        </header>
    );
}

export default Header;
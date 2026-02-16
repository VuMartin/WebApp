import HeaderSection from './HeaderSection'
import BrowseSection from './BrowseSection'
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function MainPage() {
    return (
        <>
            <HeaderSection />
            <div id="login-header">
                <div id="header-placeholder"></div>
                <p id="welcome-main">
                    Welcome to Fabflix! <br/>
                    Search and browse your favorite movies
                </p>
                <p className="main-title"> Top Rated Movies </p>
                <div className="top-movies-container-main-page">
                    <div className="movie-card">
                        <img src="/images/loma-lynda.jpeg" alt="Movie 1"/>
                        <p>Movie 1</p>
                    </div>
                    <div className="movie-card">
                        <img src="/images/addo.jpeg" alt="Movie 2"/>
                        <p>Movie 2</p>
                    </div>
                    <div className="movie-card">
                        <img src="/images/footlong.jpeg" alt="Movie 3"/>
                        <p>Movie 3</p>
                    </div>
                </div>
                <div className="more-link">
                    <Link to="/movies">More &raquo;</Link>
                </div>
                <div id="browse-placeholder"></div>
            </div>
            <BrowseSection />
        </>
    );
}

export default MainPage;
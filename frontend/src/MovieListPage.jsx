import React, { useState, useEffect, useCallback } from 'react';
import HeaderSection from './HeaderSection'
import BrowseSection from './BrowseSection'
import './App.css';
import {Link} from "react-router-dom";

function MovieListPage() {
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [sortConfig, setSortConfig] = useState({
        primaryField: 'rating',
        primaryOrder: 'desc',
        secondaryField: 'title',
        secondaryOrder: 'asc'
    });
    const [movies, setMovies] = useState([]);
    const [pageHeading, setPageHeading] = useState("Top Rated Movies");

    const getQueryParam = (name) => {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    };

    const queryParams = {
        title: getQueryParam("title"),
        year: getQueryParam("year"),
        director: getQueryParam("director"),
        star: getQueryParam("star"),
        genre: getQueryParam("genre"),
        prefix: getQueryParam("prefix")
    };

    const handleSortClick = (groupIndex, field, order) => {
        if (groupIndex === 0) {
            setSortConfig({
                primaryField: field,
                primaryOrder: field === 'rating' ? 'desc' : 'asc',
                secondaryField: field === 'rating' ? 'title' : 'rating',
                secondaryOrder: field === 'rating' ? 'asc' : 'desc'
            });
        } else if (groupIndex === 1) {
            setSortConfig(prev => ({
                ...prev,
                primaryOrder: prev.primaryField === 'rating' ? order : prev.primaryOrder,
                secondaryOrder: prev.secondaryField === 'rating' ? order : prev.secondaryOrder
            }));
        } else if (groupIndex === 2) {
            setSortConfig(prev => ({
                ...prev,
                primaryOrder: prev.primaryField === 'title' ? order : prev.primaryOrder,
                secondaryOrder: prev.secondaryField === 'title' ? order : prev.secondaryOrder
            }));
        }
    };

    const handlePrevPage = () => {
        if (currentPage > 1) {
            setCurrentPage(prev => prev - 1);
        }
    };

    const handleNextPage = () => {
        if (currentPage < totalPages) {
            setCurrentPage(prev => prev + 1);
        }
    };

    const handlePageSizeChange = (e) => {
        setPageSize(parseInt(e.target.value));
        setCurrentPage(1);
    };

    // Set page heading based on query params
    useEffect(() => {
        let heading = "Top Rated Movies";
        let title = "Top Rated Movies - Fabflix";

        if (queryParams.genre) {
            heading = `${queryParams.genre} Movies`;
            title = `${queryParams.genre} Movies - Fabflix`;
        } else if (queryParams.prefix) {
            heading = `Titles starting with "${queryParams.prefix.toUpperCase()}"`;
            title = `Titles starting with ${queryParams.prefix.toUpperCase()} - Fabflix`;
        }

        document.title = title;
        setPageHeading(heading);
    }, [queryParams.genre, queryParams.prefix]);

    const buildApiUrl = useCallback((params, sort, back) => {
        const offset = (currentPage - 1) * pageSize;
        const baseParams = new URLSearchParams({
            pageSize,
            offset,
            sortPrimaryField: sort.primaryField,
            sortPrimaryOrder: sort.primaryOrder,
            sortSecondaryField: sort.secondaryField,
            sortSecondaryOrder: sort.secondaryOrder,
            currentPage
        }).toString();

        if (back === "true") return `/api/topmovies?restore=true`;

        const q = new URLSearchParams();
        if (params.title) q.append("title", params.title);
        if (params.year) q.append("year", params.year);
        if (params.director) q.append("director", params.director);
        if (params.genre) q.append("genre", params.genre);
        if (params.star) q.append("star", params.star);
        if (params.prefix) q.append("prefix", params.prefix);

        const queryString = q.toString();
        return queryString ? `/api/topmovies?${queryString}&${baseParams}` : `/api/topmovies?${baseParams}`;
    }, [currentPage, pageSize]);

    const updatePagination = useCallback((data) => {
        const serverTotal = Number(data.totalCount);
        const calculatedTotalPages = Math.max(1, Math.ceil(serverTotal / pageSize));
        setTotalPages(calculatedTotalPages);

        const currOffset = (currentPage - 1) * pageSize;
        if (currOffset > Math.max(0, serverTotal - 1) && calculatedTotalPages > 0) {
            setCurrentPage(calculatedTotalPages);
            // fetchMovies is triggered by useEffect
        }
    }, [currentPage, pageSize]);

    const updateCartCountFromServer = () => {
        fetch('/api/cart')
            .then(res => res.json())
            .then(cartData => {
                // updateCartCount function would update state
            });
    };

    const renderPageNumbers = useCallback(() => {
        return `Page ${currentPage} of ${totalPages}`;
    }, [currentPage, totalPages]);

    const handleResult = (resultData) => {
        setMovies(resultData.movies || []);
        setCurrentPage(resultData.currentPage || 1);
        setPageSize(resultData.pageSize || 10);

        renderPageNumbers();
    };

    const fetchMovies = useCallback(async () => {
        const params = {
            title: getQueryParam("title"),
            year: getQueryParam("year"),
            director: getQueryParam("director"),
            star: getQueryParam("star"),
            genre: getQueryParam("genre"),
            prefix: getQueryParam("prefix"),
            restore: getQueryParam("restore")
        };

        const sort = {
            primaryField: sortConfig.primaryField,
            primaryOrder: sortConfig.primaryOrder,
            secondaryField: sortConfig.secondaryField,
            secondaryOrder: sortConfig.secondaryOrder
        };

        const url = buildApiUrl(params, sort, params.restore);
        try {
            const res = await fetch(url);
            const resultData = await res.json();

            handleResult(resultData);
            updatePagination(resultData);
            updateCartCountFromServer();

            if (params.restore === "true") {
                const newParams = new URLSearchParams({
                    pageSize: resultData.pageSize,
                    offset: resultData.offset,
                    sortPrimaryField: resultData.sortPrimaryField,
                    sortPrimaryOrder: resultData.sortPrimaryOrder,
                    sortSecondaryField: resultData.sortSecondaryField,
                    sortSecondaryOrder: resultData.sortSecondaryOrder,
                    currentPage: resultData.currentPage
                }).toString();

                window.history.replaceState(null, "", `?${newParams}`);
            }
        } catch (error) {
            console.error("Movies fetch failed:", error);
        }
    }, [sortConfig, buildApiUrl, handleResult, updatePagination, updateCartCountFromServer]);
    useEffect(() => {
        fetchMovies();
    }, [sortConfig, currentPage, pageSize]);
    return (
        <>
            <HeaderSection />
            <div className="main-content">
                <div className="container">
                    <div className="header-row">
                        <h3 id="h3-title" className="h3-title">{pageHeading}</h3>

                        <div className="sort-container">
                            <div className="sort-group">
                                <span className="sort-label">Sorted By:</span>
                                <span
                                    className={`sort-option ${sortConfig.primaryField === 'rating' ? 'selected' : ''}`}
                                    onClick={() => handleSortClick(0, 'rating', sortConfig.primaryOrder)}
                                >
                                    Rating
                                </span>
                                <span
                                    className={`sort-option ${sortConfig.primaryField === 'title' ? 'selected' : ''}`}
                                    onClick={() => handleSortClick(0, 'title', sortConfig.primaryOrder)}
                                >
                                Title
                            </span>
                            </div>
                            <div className="sort-group">
                                <span className="sort-label">Rating:</span>
                                <span
                                    className={`sort-option ${
                                        (
                                            (sortConfig.primaryField === 'rating' && sortConfig.primaryOrder === 'asc') ||
                                            (sortConfig.secondaryField === 'rating' && sortConfig.secondaryOrder === 'asc')
                                        )
                                            ? 'selected' : ''
                                    }`}
                                    onClick={() => handleSortClick(1, 'rating', 'asc')}
                                >
                                    Ascending ↑
                                </span>
                                <span
                                    className={`sort-option ${
                                        (
                                            (sortConfig.primaryField === 'rating' && sortConfig.primaryOrder === 'desc') ||
                                            (sortConfig.secondaryField === 'rating' && sortConfig.secondaryOrder === 'desc')
                                        )
                                            ? 'selected' : ''
                                    }`}
                                    onClick={() => handleSortClick(1, sortConfig.primaryField, 'desc')}
                                >
                                Descending ↓
                            </span>
                            </div>

                            <div className="sort-group">
                                <span className="sort-label">Title:</span>
                                <span
                                    className={`sort-option ${
                                        (
                                            (sortConfig.primaryField === 'title' && sortConfig.primaryOrder === 'asc') ||
                                            (sortConfig.secondaryField === 'title' && sortConfig.secondaryOrder === 'asc')
                                        )
                                            ? 'selected' : ''
                                    }`}
                                    onClick={() => handleSortClick(2, sortConfig.primaryField, 'asc')}
                                >
                                  Ascending ↑
                                </span>
                                <span
                                    className={`sort-option ${
                                        (
                                            (sortConfig.primaryField === 'title' && sortConfig.primaryOrder === 'desc') ||
                                            (sortConfig.secondaryField === 'title' && sortConfig.secondaryOrder === 'desc')
                                        )
                                            ? 'selected' : ''
                                    }`}
                                    onClick={() => handleSortClick(2, sortConfig.primaryField, 'desc')}
                                >
                                    Descending ↓
                                </span>
                            </div>

                            <div className="sort-group">
                                <span className="sort-label">Movies per page:</span>
                                <select id="page-size" value={pageSize} onChange={handlePageSizeChange}>
                                    <option value={10}>10</option>
                                    <option value={25}>25</option>
                                    <option value={50}>50</option>
                                    <option value={100}>100</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <table className="table table-striped">
                        <thead>
                        <tr>
                            <th></th>
                            <th>Movie Title</th>
                            <th>Release Year</th>
                            <th>Director</th>
                            <th>Genres</th>
                            <th>Stars</th>
                            <th className="ratings-col">Rating</th>
                        </tr>
                        </thead>
                        <tbody id="movie-table-body">
                        {movies.map((movie, index) => (
                            <tr key={movie.movieID}>
                                <td>
                                    $122 <br />
                                    <button
                                        className="btn btn-sm btn-success mt-1"
                                        // onClick={() => addToCart(movie.movieID, movie.movieTitle, 122)}
                                    >
                                        Add
                                    </button>
                                    <div className="cart-message" id={`message-${movie.movieID}`}></div>
                                </td>
                                <td>
                                    {((currentPage - 1) * pageSize + index + 1)}.{' '}
                                    <Link to={`/movie/${movie.movieID}`}>
                                        {movie.movieTitle}
                                    </Link>
                                </td>
                                <td>{movie.movieYear}</td>
                                <td>{movie.movieDirector}</td>
                                <td>
                                    {movie.movieGenres?.map((g, i) => (
                                        <React.Fragment key={g.name}>
                                            <a href={`/genre/${g.name}`}>
                                                {g.name}
                                            </a>
                                            {i < movie.movieGenres.length - 1 && ', '}
                                        </React.Fragment>
                                    )) || 'N/A'}
                                </td>
                                <td>
                                    {movie.movieStars?.map((s, i) => (
                                        <React.Fragment key={s.star_id}>
                                            <a href={`/star/${s.star_id}`}>
                                                {s.star_name}
                                            </a>
                                            {i < movie.movieStars.length - 1 && ', '}
                                        </React.Fragment>
                                    )) || 'N/A'}
                                </td>
                                <td>
                                    ⭐️ {movie.movieRating !== "N/A" ? `${movie.movieRating}/10` : "N/A"}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                <div className="pagination">
                    <button id="prev-page" onClick={handlePrevPage}>‹</button>
                    <span id="page-numbers">{renderPageNumbers()}</span>
                    <button id="next-page" onClick={handleNextPage}>›</button>
                </div>
            </div>
            <BrowseSection />
        </>
    );
}

export default MovieListPage;

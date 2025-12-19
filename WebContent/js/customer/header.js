function updateCartCount(cartData) {
    var cartElement = document.getElementById("cart-count");
    if (cartElement) {
        cartElement.textContent = cartData.totalCount || 0;
    }
}
document.addEventListener('DOMContentLoaded', () => {
    fetch("/html/customer/header.html")
        .then(res => res.text())
        .then(data => {
            document.getElementById("header-placeholder").innerHTML = data
            jQuery("#username").text(sessionStorage.getItem("firstName") + " ▼");
            const username = document.getElementById('username');
            const dropdownMenu = document.getElementById('dropdown-menu');

            username.addEventListener('click', () => {
                dropdownMenu.classList.toggle('visible');
            });

            const pathname = window.location.pathname;
            const backLink = document.querySelector(".back-link");
            const arrow = document.querySelector("#movie-page-header .arrow");

            if ((pathname.endsWith("/movies.html") || (pathname.endsWith("/main.html")))
                && !window.location.href.includes("genre")) {
                if (backLink) backLink.style.display = "none";
                if (arrow) arrow.style.display = "none";
            }

            const logoutButton = document.getElementById('logout-button');

            logoutButton.addEventListener('click', () => {
                $.ajax({
                    method: "POST",
                    url: "/api/logout",
                    success: () => {
                        window.location.href = "html/customer/login.html"; // redirect after logout
                    },
                    error: () => {
                        alert("Logout failed. Try again.");
                    }
                });
            });

            const optionsBtn = document.getElementById('search-options-button');
            const searchOptions = document.getElementById('search-options');

            optionsBtn.addEventListener('click', () => {
                searchOptions.classList.toggle('visible');
            });

            /*
     * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
     *
     * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
     *
     */


            /*
             * This function is called by the library when it needs to lookup a query.
             *
             * The parameter query is the query string.
             * The doneCallback is a callback function provided by the library, after you get the
             *   suggestion list from AJAX, you need to call this function to let the library know.
             */

            // Set up autocomplete on the search input
            // $('#search-input') is to find element by the ID "autocomplete"
            $('#search-input').autocomplete({
                lookup: function (query, doneCallback) {
                    handleLookup(query, doneCallback);
                },
                onSelect: function (suggestion) {
                    handleSelectSuggestion(suggestion);
                },
                deferRequestBy: 300,
                minChars: 3,
                triggerSelectOnValidInput: false
            });

            function handleLookup(query, doneCallback) {
                console.log("autocomplete initiated");
                const cached = getCachedSuggestions(query);
                if (cached) {
                    console.log("using cached results for: " + query);
                    doneCallback({suggestions: cached});
                    return;
                }

                jQuery.ajax({
                    method: "GET",
                    url: "/api/autocomplete?query=" + encodeURIComponent(query),
                    success: function (data) {
                        handleLookupAjaxSuccess(data, query, doneCallback);
                    },
                    error: function (errorData) {
                        console.log("autocomplete error:", errorData);
                        doneCallback({suggestions: []});
                    }
                });
            }

            /*
             * This function is used to handle the ajax success callback function.
             * It is called by our own code upon the success of the AJAX request
             *
             * data is the JSON data string you get from your Java Servlet
             *
             */
            function handleLookupAjaxSuccess(data, query, doneCallback) {
                console.log("lookup ajax successful");
                setCacheSuggestions(query, data.suggestions);
                doneCallback({suggestions: data.suggestions});
            }

            /*
         * This statement binds the autocomplete library with the input box element and
         *   sets necessary parameters of the library.
         *
         * The library documentation can be find here:
         *   https://github.com/devbridge/jQuery-Autocomplete
         *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
         *
         */
            // Handle form submission
            $('#search-form').on('submit', function (event) {
                event.preventDefault(); // Prevent default form submission

                const searchInput = $('#search-input');
                const selectedSuggestion = searchInput.data('autocomplete').selectedItem;

                if (selectedSuggestion) {
                    // If suggestion is selected, go directly to movie page
                    handleSelectSuggestion(selectedSuggestion);
                } else {
                    // Otherwise, do normal full-text search
                    handleNormalSearch();
                }
            });

// Cache functions (same as before)
            function getCachedSuggestions(query) {
                const cache = JSON.parse(sessionStorage.getItem('autocompleteCache') || '{}');
                return cache[query];
            }

            function setCacheSuggestions(query, suggestions) {
                const cache = JSON.parse(sessionStorage.getItem('autocompleteCache') || '{}');
                cache[query] = suggestions;
                sessionStorage.setItem('autocompleteCache', JSON.stringify(cache));
            }

            function handleNormalSearch() {
                let title = jQuery("#search-input").val();
                let year = jQuery("#search-year").val();
                let director = jQuery("#search-director").val();
                let star = jQuery("#search-star").val();

                let url = "/html/customer/movies.html?";
                if (title) url += "title=" + encodeURIComponent(title) + "&";
                if (year) url += "year=" + encodeURIComponent(year) + "&";
                if (director) url += "director=" + encodeURIComponent(director) + "&";
                if (star) url += "star=" + encodeURIComponent(star);

                window.location.href = url;
            }

            function handleSelectSuggestion(suggestion) {
                window.location.href = "/html/customer/movie.html?id=" + suggestion.data.movieId;
            }
        });
});
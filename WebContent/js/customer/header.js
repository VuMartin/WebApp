function updateCartCount(cartData) {
    document.getElementById("cart-count").textContent = cartData.totalCount || 0;
}

fetch("/2025_fall_cs_122b_marjoe_war/html/customer/header.html")
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
                url: "/2025_fall_cs_122b_marjoe_war/api/logout",
                success: () => {
                    window.location.href = "/2025_fall_cs_122b_marjoe_war/html/customer/login.html"; // redirect after logout
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

        jQuery("#search-form").submit(function(event) {
            event.preventDefault(); // stop page reload
            // get values
            let title = jQuery("#search-input").val();
            let year = jQuery("#search-year").val();
            let director = jQuery("#search-director").val();
            let star = jQuery("#search-star").val();

            let url = "/2025_fall_cs_122b_marjoe_war/html/customer/movies.html?";
            if (title) url += "title=" + encodeURIComponent(title) + "&";
            if (year) url += "year=" + encodeURIComponent(year) + "&";
            if (director) url += "director=" + encodeURIComponent(director) + "&";
            if (star) url += "star=" + encodeURIComponent(star);

            window.location.href = url;
        });
    });
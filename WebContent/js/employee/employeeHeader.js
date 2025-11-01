fetch("employeeHeader.html")
    .then(res => res.text())
    .then(data => {
        document.getElementById("employeeHeader-placeholder").innerHTML = data;

        // 🔹 Hide dropdown on employee login page
        if (window.location.pathname.includes("employeeLogin.html")) {
            document.getElementById("user-dropdown").style.display = "none";
            return; // stop running the rest (no username or logout needed)
        }

        // 🔹 Otherwise show username and set up dropdown
        jQuery("#username").text(sessionStorage.getItem("fullname") + " ▼");

        const username = document.getElementById('username');
        const dropdownMenu = document.getElementById('dropdown-menu');

        username.addEventListener('click', () => {
            dropdownMenu.classList.toggle('visible');
        });

        const logoutButton = document.getElementById('logout-button');
        logoutButton.addEventListener('click', () => {
            $.ajax({
                method: "POST",
                url: "api/logout",
                success: () => {
                    window.location.href = "employeeLogin.html";
                },
                error: () => {
                    alert("Logout failed. Try again.");
                }
            });
        });
    });
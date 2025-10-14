jQuery("#username").text(sessionStorage.getItem("firstName") + " ▼");
const username = document.getElementById('username');
const dropdownMenu = document.getElementById('dropdown-menu');

username.addEventListener('click', () => {
    dropdownMenu.style.display = dropdownMenu.style.display === 'block' ? 'none' : 'block';
});

const logoutButton = document.getElementById('logout-button');

logoutButton.addEventListener('click', () => {
    $.ajax({
        method: "POST",
        url: "api/logout",
        success: () => {
            window.location.href = "login.html"; // redirect after logout
        },
        error: () => {
            alert("Logout failed. Try again.");
        }
    });
});
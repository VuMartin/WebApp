jQuery("#add-movie-form").submit(function(event) {
    event.preventDefault(); // prevent normal form submit
    // jQuery("#movie-msg").text("");

    let title = jQuery("#movie-title").val();
    let year = jQuery("#movie-year").val();
    let director = jQuery("#movie-director").val();
    let star = jQuery("#movie-star").val();
    let genre = jQuery("#movie-genre").val();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "/2025_fall_cs_122b_marjoe_war/api/add_movie",
        data: { title: title, year: year, director: director, star: star, genre:genre },
        success: (resultData) => {
            jQuery("#movie-msg").text(resultData.message);
            if (resultData.status === "success") {
                jQuery("#movie-msg").css("color", "green");
                jQuery("#add-movie-form")[0].reset();
            } else {
                jQuery("#movie-msg").css("color", "red");
            }
        },
        error: () => {
            jQuery("#movie-msg").text("Server error. Try again later.");
        }
    });
});

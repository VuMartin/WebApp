jQuery("#add-star-form").submit(function (e) {
    e.preventDefault();
    const name = jQuery("#star-name").val();
    const birthYear = jQuery("#star-birth").val();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "/2025_fall_cs_122b_marjoe_war/api/add_star",

        data: { name, birthYear },
        success: (res) => {
            jQuery("#star-msg").text(res.message);
            if (res.status === "success") {
                jQuery("#star-msg").removeClass("text-danger").addClass("text-success");
                this.reset();
            } else {
                jQuery("#star-msg").removeClass("text-success").addClass("text-danger");
            }
        },
        error: () => jQuery("#star-msg").text("Server error. Try again later."),
    });
});
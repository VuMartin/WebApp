
fetch("/2025_fall_cs_122b_marjoe_war/html/customer/browse.html")
    .then(res => res.text())
    .then(data => {
        document.getElementById("browse-placeholder").innerHTML = data;
        $.ajax({
            url: "/2025_fall_cs_122b_marjoe_war/api/genres",
            dataType: "json",
            success: function (genres) {
                const list = $("#genresList").empty();
                (genres || []).forEach((g) => {
                    $("<li>").append(
                        $("<a>")
                            .text(g.name)
                            .attr("href", "/2025_fall_cs_122b_marjoe_war/html/customer/movies.html?genre=" + encodeURIComponent(g.name))
                    ).appendTo(list);
                });
            }
        });

        const $ul = $("#alphaList").empty();
        const makeItem = (txt) =>
            $("<li>").append($("<a>").text(txt).attr("href", "/2025_fall_cs_122b_marjoe_war/html/customer/movies.html?prefix=" + encodeURIComponent(txt)));

        for (let d = 0; d <= 9; d++) $ul.append(makeItem(String(d)));
        for (let c = 65; c <= 90; c++) $ul.append(makeItem(String.fromCharCode(c)));
    });
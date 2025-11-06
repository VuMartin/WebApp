$(function () {
    // Auto-load on page ready
    $.ajax({
        method: "GET",
        url: "api/employee/metadata",
        dataType: "json"
    })
        .done(function (res) {
            const $host = $("#metadata-table").empty();

            if (res.status !== "success") {
                $host
                    .addClass("text-danger")
                    .text(res.message || "Failed to load metadata.");
                return;
            }

            // Build an accordion of tables
            const dbName = res.database || "(current)";
            const tables = res.tables || [];

            if (!tables.length) {
                $host.text(`No tables found in database ${dbName}.`);
                return;
            }

            $host.append($(`<div class="mb-2"><strong>Database:</strong> ${dbName}</div>`));

            const $grid = $('<div class="meta-grid"></div>');
            $host.append($grid);

            tables.forEach((t) => {
                const tableName = t.table;
                const cols = t.columns || [];

                const $card = $(`
    <div class="card mb-2">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span>${tableName}</span>
        <span class="badge badge-secondary">${cols.length} columns</span>
      </div>
      <div class="card-body p-2">
        <div class="table-responsive">
          <table class="table table-sm table-striped mb-0">
            <thead><tr><th>Attribute</th><th>Type</th></tr></thead>
            <tbody></tbody>
          </table>
        </div>
      </div>
    </div>
  `);

                const $tbody = $card.find("tbody");
                cols.forEach(c => $tbody.append(
                    `<tr><td>${c.name}</td><td><code>${c.type}</code></td></tr>`
                ));

                $grid.append($card);
            });

        })
        .fail(function () {
            $("#metadata-table")
                .addClass("text-danger")
                .text("Network/Server error loading metadata.");
        });
});

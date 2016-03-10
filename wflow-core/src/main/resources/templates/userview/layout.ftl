<!DOCTYPE html>
<html lang="${locale!}">
    ${html_inner_before!}
    <head>
        ${head_inner_before!}
        ${head!}
        ${head_inner_after!}
    </head>

    <body id="${body_id!}" class="${body_classes!}">
        ${body_inner_before!}
        ${page_before!}
        <div id="page">
            ${page_inner_before!}
            ${header!}

            ${content_container!}

            ${footer!}
            ${page_inner_after!}
        </div>
        ${page_after!}

        ${joget_footer!}
        ${body_inner_after!}
    </body>
    ${html_inner_after!}
</html>
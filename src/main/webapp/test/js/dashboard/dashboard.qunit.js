(function(){
    "use strict";

    //this isn't a very good test since we aren't certain there are two pages worth of resources on the dashboard, but it's mostly just
    //testing that an async domtest will work
    asyncTest("test that the next button loads some records",  function() {
        $('dataTables_paginate li.next').click();

        setTimeout(function() {
            ok( $('#resource_datatable tr').length > 0, "expecting more than one result on next page");
            start();
        }, 2000);
    });
})();

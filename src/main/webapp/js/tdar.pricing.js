(function (TDAR, $) {
    'use strict';

    var _initPricing = function (form, ajaxUrl) {
        var $form = $(form);
        $("#small-option").click(function () {
            $("#MetadataForm_invoice_numberOfFiles").val(1);
            $("#MetadataForm_invoice_numberOfMb").val("");
            $form.data("formNavigate", true);
            $form.submit();
        });
        $("#medium-option").click(function () {
            $("#MetadataForm_invoice_numberOfFiles").val(10);
            $("#MetadataForm_invoice_numberOfMb").val("");
            $form.data("formNavigate", true);
            $form.submit();
        });
        $("#large-option").click(function () {
            $("#MetadataForm_invoice_numberOfFiles").val(100);
            $("#MetadataForm_invoice_numberOfMb").val("");
            $form.data("formNavigate", true);
            $form.submit();
        });

        $form.change(function () {
            var numFiles = $("#MetadataForm_invoice_numberOfFiles").val();
            var numMb = Math.ceil($("#MetadataForm_invoice_numberOfMb").val());

            /* give the user an understanding of size in GB if size is > 1/2 GB */
            var mb = "";
            if (numMb > 512) {
                var num = numMb / 1024;
                if (num.toString().indexOf(".") > 0) {
                    num = num.toFixed(3);
                }
                mb = "(" + (num) + " GB)";
            }
            $("#convert").html(mb);

            var $est = $("#estimated");
            $est.val("");
            var url = ajaxUrl + "?lookupMBCount=" + numMb + "&lookupFileCount=" + numFiles;
            $.ajax({
                url: url,
                dataType: 'jsonp',
                crossDomain: true,
                type: 'POST',
                success: function (data) {
                    var checked = "checked";
                    $est.html("");
                    var defaultsubtotal = 100000000000000000;
                    var subtotal = defaultsubtotal;
                    var item = {};
                    for (var i = 0; i < data.length; i++) {
                        var internal_name, label, num_files, num_space, extra_space, total_cost = "";
                        internal_name = data[i].model;
                        label = data[i].model;
                        total_cost = data[i].subtotal;
                        if (subtotal > total_cost) {
                            item = data[i];
                            subtotal = data[i].subtotal;
                        }
                    }
                    if (subtotal == defaultsubtotal) {
                        $("#price").html("0.00");
                        var line = TDAR.common.sprintf("<tr><td colspan=4>{0}</td></tr>", "Please enter a number of files and MB above.");
                        $est.append(line);


                    } else {
                        $("#price").html(item.subtotal);

                        checked = "";
                        //(i +1), data[i].model, data[i].subtotal );
                        var total_files = 0;
                        var total_mb = 0;
                        if (item.parts && item.parts.length > 0) {
                            for (var j = 0; j < item.parts.length; j++) {
                                var part = item.parts[j];
                                var line = TDAR.common.sprintf("<tr><td>{0}</td><td>{1}</td><td>{2} MB</td><td>${3}</td></tr>", part.name, part.numFiles * part.quantity, part.numMb * part.quantity, part.subtotal);
                                total_files += part.numFiles * part.quantity;
                                total_mb += part.numMb * part.quantity;
                                $est.append(line);
                            }
                        }
                        var line = TDAR.common.sprintf("<tr class='table-row-separator-above'><td></td><td class='subtotal'>{0}</td><td class='subtotal'>{1} MB</td><td class='red'>${2}</td></tr>", total_files, total_mb, subtotal);
                        $est.append(line);
                    }
                },
                error: function (xhr, txtStatus, errorThrown) {
                    console.error("error: %s, %s", txtStatus, errorThrown);
                }
            });
        });
        $(form).change();
    };

    TDAR.pricing = {
        "initPricing": _initPricing
    };

})(TDAR, jQuery);

const common = require("./tdar.common");
const core = require("./tdar.core.js");
var POLLING_INTERVAL = 1500; // poll every 1.5s

// parse string to integer. replace blank and NaN with 0.
var _parse = function(num) {
    var _num = num.replace(",", "");
    _num = Math.ceil(_num);
    _num = isNaN(_num) ? 0 : _num;
    return _num;
};

var _initPricing = function(form, ajaxUrl) {
    var $form = $(form);
    $("#small-option").click(function() {
        $("#MetadataForm_invoice_numberOfFiles").val(1);
        $("#MetadataForm_invoice_numberOfMb").val("");
        $form.data("formNavigate", true);
        $form.submit();
    });
    $("#medium-option").click(function() {
        $("#MetadataForm_invoice_numberOfFiles").val(10);
        $("#MetadataForm_invoice_numberOfMb").val("");
        $form.data("formNavigate", true);
        $form.submit();
    });
    $("#large-option").click(function() {
        $("#MetadataForm_invoice_numberOfFiles").val(100);
        $("#MetadataForm_invoice_numberOfMb").val("");
        $form.data("formNavigate", true);
        $form.submit();
    });

    $form.submit(function(f) {
        var numFiles = _parse($("#MetadataForm_invoice_numberOfFiles").val());
        var numMb = _parse($("#MetadataForm_invoice_numberOfMb").val());

        $("#MetadataForm_invoice_numberOfFiles, #MetadataForm_invoice_numberOfMb").each(function() {
            if (this.value.length) {
                this.value = _parse(this.value).toFixed(0);
            }
        });
        return true;
    });

    $form.change(function() {
        var numFiles = _parse($("#MetadataForm_invoice_numberOfFiles").val());
        var numMb = _parse($("#MetadataForm_invoice_numberOfMb").val());

        if (isNaN(numMb)) {
            numMb = 0;
        }

        if (isNaN(numFiles)) {
            numFiles = 0;
        }
        /* give the user an understanding of size in GB if size is > 1/2 GB */
        var mb = "";
        if (numMb > 512) {
            $("#convert").html(common.humanFileSize(numMb * 1024 * 1024));
        }

        var $est = $("#estimated");
        $est.val("");
        var url = ajaxUrl + "?lookupMBCount=" + numMb + "&lookupFileCount=" + numFiles;
        $.ajax({
            url : url,
            dataType : 'jsonp',
            crossDomain : true,
            type : 'POST',
            xhrFields : {
                withCredentials : true
            },
            success : function(data) {
                // var data = _parseApidata(rawdata);
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
                var $price = $("#price");
                if (subtotal == defaultsubtotal) {
                    $price.html("0.00");
                    var line = common.sprintf("<tr><td colspan=4>{0}</td></tr>", "Please enter a number of files and MB above.");
                    $est.append(line);

                } else {

                    checked = "";
                    // (i +1), data[i].model, data[i].subtotal );
                    var total_files = 0;
                    var total_mb = 0;
                    if (item.items && item.items.length > 0) {
                        for (var j = 0; j < item.items.length; j++) {
                            var part = item.items[j];
                            part.name = part.activity.numberOfFiles === 0 ? "Extra Space" : "Files";
                            var line = common.sprintf("{0}  ( {1} Files / {2} )", common.formatNumber(item.subtotal), common
                                    .formatNumber(part.activity.numberOfFiles * part.quantity), common.humanFileSize(part.activity.numberOfMb * part.quantity *
                                    1024 * 1024, ''));
                            total_files += part.activity.numberOfFiles * part.quantity;
                            total_mb += part.activity.numberOfMb * part.quantity;
                            $price.html(line);

                        }
                    }
                }
            },
            error : function(xhr, txtStatus, errorThrown) {
                console.error("error: %s, %s", txtStatus, errorThrown);
            }
        });
    });
    $(form).change();

    _setupPaymentMethodPivot();

    // console.log("appying rules");
    var validator = $(form).validate({
        rules : {
            "invoice.otherReason" : {
                required : "#MetadataForm_invoice_paymentMethodMANUAL:checked"
            },
            "invoice.invoiceNumber" : {
                required : "#MetadataForm_invoice_paymentMethodINVOICE:checked"
            },
            "invoice.paymentMethod" : {
                required : true
            },

            "invoice.numberOfFiles" : {
                min : 1,
                require_from_group : [ 1, ".orderinfo" ]
            },

            "invoice.numberOfMb" : {
                min : 1,
                require_from_group : [ 1, ".orderinfo" ]
            }
        },
        messages : {
            "invoice.invoiceNumber" : "Specify the customer invoice/work-order number.",
            "invoice.otherReason" : "For manual invoices, describe why you are creating this invoice."
        },
        errorClass : "text-error",
        errorPlacement : function($error, $element) {
            $element.after($error);
        }
    });
    // console.log("validtor:", validator);
};

/**
 * setup the pivot pane toggle for the paymentMethod field
 * 
 * @private
 */
var _setupPaymentMethodPivot = function() {
    $(".transactionType[type=radio]").click(function() {
        common.switchType(this, '#MetadataForm');
    });
    // switch to the right pane on init
    common.switchType($(".transactionType[type=radio]:checked", $('#MetadataForm')), "#MetadataForm");
}

var _initPolling = function() {
    _updateProgress();
};

var _updateProgress = function() {
    var invoiceid = $("#polling-status").data("invoiceid");
    if (typeof invoiceid === "undefined") {
        throw "invoiceid not specified";
    }
    var url = "/api/cart/" + invoiceid + "/polling-check";

    $.ajax({
        url : core.uri(url),
        dataType : 'json',
        type : 'POST',
        xhrFields : {
            withCredentials : true
        },
        success : function(data) {
            if (data.transactionStatus == 'PENDING_TRANSACTION') {
                $("#polling-status").html("checking status ...");
                setTimeout(_updateProgress, POLLING_INTERVAL);
            } else {
                $("#polling-status").html("done: " + data.transactionStatus);
                if (data.transactionStatus == 'TRANSACTION_SUCCESSFUL') {
                    window.document.location = core.uri("/dashboard");
                }
            }
            if (data.errors != undefined && data.errors != "") {
                $("#asyncErrors").html("<div class='action-errors ui-corner-all'>" + data.errors + "</div>");
            }
        },
        error : function(xhr, txtStatus, errorThrown) {
            console.error("error: %s, %s", txtStatus, errorThrown);
        }
    });

    console.log("registered ajax callback");
};

var _initBillingChoice = function() {
    $(document).ready(function() {
        var $slct = $("#select-existing-account");
        $slct.change(function() {
            var $addnew = $(".add-new");
            if ($slct.val() == -1) {
                $addnew.removeClass("hidden");
            } else {
                $addnew.addClass("hidden");
            }
        });
        if ($slct.val() == -1) {
            $(".add-new").removeClass("hidden");
        }
    });
}

module.exports = {
    "initPricing" : _initPricing,
    "updateProgress" : _updateProgress,
    "initPolling" : _initPolling,
    "initBillingChoice" : _initBillingChoice
};
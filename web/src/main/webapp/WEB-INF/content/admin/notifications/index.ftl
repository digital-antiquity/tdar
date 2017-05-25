<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/content/admin/admin-common-resource.ftl" as admin>
<head>
<title>Administrator Dashboard: User Notifications</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<@admin.header/>
<h2>User Notifications</h2>
<div id='notifications'>
    <table class="table table-striped table-bordered">
        <thead>
            <tr>
                <th>User</th>
                <th>Date Created</th>
                <th>Expiration Date</th>
                <th>Type</th>
                <th>Message Key</th>
                <th>Message</th>
                <th></th>
            </tr>
        </thead>
        <tbody data-bind='foreach: notifications'>
        <tr>
            <td data-bind='text: tdarUser'></td>
            <td data-bind='text: shortFormatDateCreated'></td>
            <td data-bind='text: shortFormatExpirationDate'></td>
            <td data-bind='text: messageType'></td>
            <td data-bind='text: messageKey'></td>
            <td><textarea readonly data-bind='text: message'></textarea></td>
            <td>
                <div class='btn-group btn-group-vertical'>
                    <button data-bind='click: $parent.editNotification.bind($data, false)' class='btn btn-info'><i class='icon-edit'></i></a>
                    <button data-bind='click: sendDelete' class='btn btn-danger'><i class='icon-trash'></i></button>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
    <a data-bind='click: editNotification.bind($data, true)' role='button' class='btn btn-success'><i class='icon-plus-sign'></i> add a notification</a>
    <#-- modal form -->
    <div id='modalNotificationForm' class='modal hide fade' data-bind='showModal: selectedNotification, with: selectedNotification' tabindex='-1' role='dialog' aria-labelledby='createNotificationFormLabel' aria-hidden='true'>
        <div class='modal-header'>
            <button type='button' class='close' data-dismiss='modal' aria-hidden='true'>&times;</button>
            <h3 id='createNotificationFormLabel'>Create User Notification</h3>
        </div>
        <div class='modal-body'>
            <form id='notificationForm' class='form-horizontal'>
                <input type='hidden' name='id' data-bind='value: id'>
                <input type='hidden' name='notification.id' data-bind='value: id'>
                <input type='hidden' name='notification.expirationDate' data-bind='value: shortFormatExpirationDate'>
                <div class='control-group'>
                    <label class='control-label'>Expiration date</label>
                    <div class='controls'><input type='date' data-bind='value: expirationDate' placeholder='Expires on'></div>
                </div>
                <div class='control-group'>
                    <label class='control-label'>Type</label>
                    <div class='controls'><select name='notification.messageType' data-bind='options: $root.messageTypes, value: messageType'></select></div>
                </div>
                <div class='control-group'>
                    <label class='control-label'>Message</label>
                    <div class='controls'><textarea rows="5" name='notification.messageKey' data-bind='value: messageKey' placeholder='Message / key'></textarea></div>
                </div>
                <div class='control-group'>
                    <label class='control-label'>Message</label>
                    <div class='controls'><textarea readonly data-bind='value: message'></textarea></div>
                </div>
            </form>
        </div>
        <div class='modal-footer'>
            <button class='btn' data-dismiss='modal' aria-hidden='true'>Cancel</button>
            <button class='btn btn-primary' data-dismiss='modal' data-bind='click: $parent.sendSave'>Save</button>
        </div>
    </div>
</div>
</#escape>
<#-- FIXME: how to place these in the footer? -->
<script src='//cdnjs.cloudflare.com/ajax/libs/knockout/3.2.0/knockout-min.js'></script>
<script src='//cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.3/moment.min.js'></script>
<script src='/includes/knockout.mapping.js'></script>
<script>
ko.bindingHandlers.showModal = {
    init: function (element, valueAccessor) {},
    update: function(element, valueAccessor) {
        var value = valueAccessor();
        if (ko.utils.unwrapObservable(value)) {
            $(element).modal('show');
            $('input', element).focus();
        }
        else {
            $(element).modal('hide');
        }
    }
};
function UserNotificationModel(data) {
    var self = this;
    if (data) {
        ko.mapping.fromJS(data, {}, self);
    }
    else {
        self.id = ko.observable(-1);
        self.dateCreated = ko.observable();
        self.expirationDate = ko.observable();
        self.tdarUser = ko.observable("None");
        self.messageType = ko.observable("SYSTEM_BROADCAST");
        self.messageKey = ko.observable();
        self.message = ko.observable();
    }
    self.lookupMessageKey = function() {
        if (! self.messageKey()) {
            self.message("");
            return;
        }
        // cache ajax lookup?
        $.get("/admin/notifications/lookup", { "notification.messageKey": self.messageKey() }, function(response) {
            console.debug("SUCCESSFUL LOOKUP");
            console.debug(response);
            self.message(response.message);
        })
        .fail(function (response) {
            console.debug("FAILED LOOKUP: ");
            console.debug(response);
            self.message(self.messageKey());
        });
    };
    self.shortFormatDateCreated = ko.computed(function() {
        return moment(self.dateCreated()).format('L');
    });
    self.shortFormatExpirationDate = ko.computed(function() {
        var d = self.expirationDate();
        if (d) {
            return moment(d).format('L');
        }
    });
    self.messageKey.subscribe(self.lookupMessageKey);
    return self;
}
function NotificationsViewModel(initialModelData) {
    var mapping = {
        'notifications': {
            create: function(options) {
                return new UserNotificationModel(options.data);
            }
        }
    }
    var model = ko.mapping.fromJS(initialModelData, mapping);
    model.selectedNotification = ko.observable();
    model.editNotification = function(isNew, notification) {
        if (isNew) {
            notification = new UserNotificationModel();
        }
        model.selectedNotification(notification);
        var modalForm = document.getElementById('modalNotificationForm');
        $(modalForm).modal();
    }
    model.sendSave = function(notification) {
        console.debug("posting json:");
        var data = $('#notificationForm').serialize();
        console.debug(data);
        $.post("/admin/notifications/update", data, function(data) {
            console.debug("SUCCESS returned data");
            console.debug(data);
            // update KO model with data from the server.
            if (notification.id() != -1) {
                model.notifications.remove(notification);
            }
            model.notifications.push(new UserNotificationModel(data));
        })
        .fail(function(response) {
            console.debug("ERROR: ");
            console.debug(response);
        })
        .always(function() {
            model.selectedNotification(null);
        });
    };
    self.sendDelete = function(notification) {
        console.debug("deleting " + notification.id());
        // var data = $('#deleteNotificationForm_' + notification.id()).serialize();
        $.post("/admin/notifications/delete", { id: notification.id() }, function(response) {
            console.debug("SUCCESSFULLY deleted notification");
            console.debug(response);
            model.notifications.remove(notification);
        })
        .fail(function(response) {
            console.debug("ERROR while deleting notification: ");
            console.debug(response);
        });
    };
    return model;
}
$(function() {
    var initialModelData = { notifications: ${notificationsJson!"[]"}, messageTypes: ${allMessageTypesJson} };
    ko.applyBindings(new NotificationsViewModel(initialModelData), document.getElementById("notifications"));
});
</script>

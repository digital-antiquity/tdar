<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/content/admin/admin-common.ftl" as admin>
<head>
    <title>Administrator Dashboard: User Notifications</title>
    <meta name="lastModifiedDate" content="$Date$"/>
</head>
<@admin.header/>
<h2>User Notifications</h2>
<table id='userNotifications' class="table table-striped table-bordered">
    <thead>
    <tr>
        <th>User</th>
        <th>Date Created</th>
        <th>Message Type</th>
        <th>Message</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
        <#list allNotifications as notification>
        <@s.form id='notificationForm_${notification.id}'>
        <@s.token name='struts.csrf.token' />
        <@s.hidden name='notificationId' value='${notification.id}' />
        <tr>
            <td>${notification.tdarUser!"None"}</td>
            <td>${notification.dateCreated}</td>
            <td>${notification.messageType}</td>
            <td>${notification.messageKey}</td>
            <td><a class='btn btn-info'><i class='icon-edit'></i>Edit</a> <button type='submit' class='btn btn-danger'><i class='icon-trash'></i> Delete</button></td>
        </tr>
        </@s.form>
        </#list>
    </tbody>
</table>
<a class='btn btn-success'><i class='icon-plus-sign'></i> add a notification</a>
<script src='//cdnjs.cloudflare.com/ajax/libs/knockout/3.1.0/knockout-min.js'></script>
<script src='/includes/knockout.mapping.js'></script>
<script>
function UserNotificationModel(data) {
    var self = this;
    var model = ko.mapping.fromJS(data);
    model.delete = function() {
        console.debug("deleting " + self.id);
    }
    model.update = function() {
        console.debug("updating " + self.id);
    }
    return model;
}
function NotificationsViewModel(viewModelJson) {
    var mapping = {
        'notifications': {
            create: function(options) {
                return new UserNotificationModel(options.data);
            }
        }
    }
    var model = ko.mapping.fromJS(viewModelJson, mapping);
    return model;
}
$(function() {
    var json = $.parseJSON("${allNotificationsJson!"{}"}");
    ko.applyBindings(new NotificationsViewModel(json));
});
</script>
</#escape>

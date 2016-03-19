/**
 * @author Robin Duda
 */

var application = {
    handlers: {},
    key: "",
    id: "",

    subscribe: function (event, callback) {
        if (this.handlers[event] == null)
            this.handlers[event] = [];

        this.handlers[event].push(callback);
    },

    publish: function (event, data) {
        for (var subscriber = 0; subscriber < this.handlers[event].length; subscriber++)
            this.handlers[event][subscriber](data);
    }
};

application.subscribe('card-event', function (event) {
    application.key = event.key;
    application.id = event.id;
    $('#user-panel').show();
});

application.subscribe('vote-event', function () {
    application.key = '';

    setTimeout(function () {
        $('#user-panel').hide();
    }, 3500);
});

$(document).ready(function () {
    $('#user-panel').hide();
});
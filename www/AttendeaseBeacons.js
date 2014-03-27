var exec = require('cordova/exec');

/**
 * Helpers
 */
function isString(value) {
    return (typeof value == 'string' || value instanceof String);
}

function isInt(value) {
    return !isNaN(parseInt(value, 10)) && (parseFloat(value, 10) == parseInt(value, 10));
}

/**
 * Constructor
 */
function AttendeaseBeacons() {
}

AttendeaseBeacons.prototype.monitor = function (uuids, successCallback) {
    if (typeof uuids !== "object") {
        console.error("AttendeaseBeacons.monitor failure: uuids must be an array of beacon uuids to monitor");
        return;
    }

    if (typeof successCallback !== "function") {
        console.error("AttendeaseBeacons.monitor failure: success callback parameter must be a function");
        return;
    }

    exec(successCallback,
        function () {},
        "AttendeaseBeacons",
        "monitor",
        [uuids]
    );
};

AttendeaseBeacons.prototype.getBeacons = function (successCallback) {
    if (typeof successCallback !== "function") {
        console.error("AttendeaseBeacons.getBeacons failure: success callback parameter must be a function");
        return;
    }

    exec(successCallback,
        function () {},
        "AttendeaseBeacons",
        "getBeacons",
        []
    );
};

AttendeaseBeacons.prototype.notifyServer = function (url, interval) {
    if (typeof url !== "string") {
        console.error("AttendeaseBeacons.notifyServer failure: url must be the host you with to post beacon data to");
        return;
    }

    if (typeof interval !== "number") {
        interval = 3600;
    }

    exec(function () {},
        function () {},
        "AttendeaseBeacons",
        "notifyServer",
        [url, interval]
    );
};

AttendeaseBeacons.prototype.notifyServerAuthToken = function (token) {
    if (typeof token !== "string") {
        console.error("AttendeaseBeacons.notifyServerAuthToken failure: token must be the the attendee's auth token");
        return;
    }

    exec(function () {},
        function () {},
        "AttendeaseBeacons",
        "notifyServerAuthToken",
        [token]
    );
};

AttendeaseBeacons.prototype.fireEvent = function (eventName, data) {
    event = document.createEvent("HTMLEvents");
    event.initEvent(eventName, true, true);
    event.data = data;
    document.dispatchEvent(event);
};

var attendeaseBeacons = new AttendeaseBeacons();
module.exports = attendeaseBeacons;
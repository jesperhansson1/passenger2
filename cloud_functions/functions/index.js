const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.pushNotifications = functions.database.ref('notifications/{notificationId}')
  .onWrite((change, context) => {

    const DRIVE_REQUEST = 0;
    const PASSENGER_ACCEPT = 1;
    const PASSENGER_REJECT = 2;

    var notificationId = context.params.notificationId;

    var notificationData = change.after.val();
    var driveId = notificationData.driveId;
    var driveRequestId = notificationData.driveRequestId;

    var payload = {};
    var notificationSound = "default";
    var recieverTokenId;

    var driveRequestTitle = "Drive request";
    var driveRequestBody = "want's to share your ride";
    var acceptedDriveRequestTitle = "Accepted drive request";
    var acceptedDriveRequestBody = "has accepted your drive request";

    const drivePromise = admin.database().ref(`/drives/${driveId}`).once('value');
    const driveRequestPromise = admin.database().ref(`/driveRequests/${driveRequestId}`).once('value');

    return Promise.all([drivePromise, driveRequestPromise]).then(results => {
      var drive = results[0].val();
      var driveRequest = results[1].val();

      const driverPromise = admin.database().ref(`/users/${drive.driverId}`).once('value');
      const passengerPromise = admin.database().ref(`/users/${driveRequest.passenger}`).once('value');

      return Promise.all([driverPromise, passengerPromise]).then(results => {
        var driver = results[0].val();
        var passenger = results[1].val();

        if (notificationData.type === DRIVE_REQUEST) {
          recieverTokenId = driver.notificationTokenId;

          payload = {
            notification: {
              title: driveRequestTitle,
              body: `${passenger.fullName} ${driveRequestBody}`,
              sound: notificationSound
            },
            data: {
              type: DRIVE_REQUEST.toString(),
              driveId: driveId.toString(),
              driveRequest: driveRequestId.toString(),

            }
          };

          console.log(payload);
        }

        if (notificationData.type === PASSENGER_ACCEPT) {
          recieverTokenId = passenger.notificationTokenId;

          payload = {
            notification: {
              title: acceptedDriveRequestTitle,
              body: `${driver.fullName} ${acceptedDriveRequestBody}`,
              sound: notificationSound
            },
            data: {
              type: PASSENGER_ACCEPT.toString(),
              driveId: driveId.toString(),
              driveRequest: driveRequestId.toString()
            }
          };

          console.log(payload);
        }

        if (notificationData.type === PASSENGER_REJECT) {
          recieverTokenId = passenger.notificationTokenId;

          payload = {
            data: {
              type: PASSENGER_REJECT.toString(),
              driveId: driveId.toString(),
              driveRequest: driveRequestId.toString()
            }
          };

          console.log(payload);
        }
        return admin.messaging().sendToDevice(recieverTokenId, payload)
          .then((response) => {
            const error = response.results[0].error;
            if (error) {
              console.error("Couldn't send notification", error);
            }
           return "hej"; //admin.database().ref(`/notifications/${notificationId}`).remove();
          });
      });
    });
  });
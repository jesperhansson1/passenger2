const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.pushNotifications = functions.database.ref('notifications/{notificationId}')
  .onCreate((snapshot, context) => {

    const DRIVE_REQUEST = 0;
    const PASSENGER_ACCEPT = 1;
    const PASSENGER_REJECT = 2;

    var notificationId = context.params.notificationId;

    var notificationData = snapshot.val();
    var drive = notificationData.drive;
    var driveRequest = notificationData.driveRequest;

    var payload = {};
    var notificationSound = "default";
    var recieverTokenId;

    var driveRequestTitle = "Drive request";
    var driveRequestBody = "want's to share your ride";
    var acceptedDriveRequestTitle = "Accepted drive request";
    var acceptedDriveRequestBody = "has accepted your drive request";

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
            driveDriverId: drive.driverId,
            driveAvailableSeats: drive.availableSeats.toString(),
            driveStartLocationLatitude: drive.startLocation.latitude.toString(),
            driveStartLocationLongitude: drive.startLocation.longitude.toString(),
            driveEndLocationLatitude: drive.endLocation.latitude.toString(),
            driveEndLocationLongitude: drive.endLocation.longitude.toString(),
            driveTime: drive.time.toString(),

            driveRequestPassenger: driveRequest.passenger,
            driveRequestExtraPassengers: driveRequest.extraPassengers.toString(),
            driveRequestStartLocationLatitude: driveRequest.startLocation.latitude.toString(),
            driveRequestStartLocationLongitude: driveRequest.startLocation.longitude.toString(),
            driveRequestEndLocationLatitude: driveRequest.endLocation.latitude.toString(),
            driveRequestEndLocationLongitude: driveRequest.endLocation.longitude.toString(),
            driveRequestTime: driveRequest.time.toString()
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
            driveDriverId: drive.driverId,
            driveAvailableSeats: drive.availableSeats.toString(),
            driveStartLocationLatitude: drive.startLocation.latitude.toString(),
            driveStartLocationLongitude: drive.startLocation.longitude.toString(),
            driveEndLocationLatitude: drive.endLocation.latitude.toString(),
            driveEndLocationLongitude: drive.endLocation.longitude.toString(),
            driveTime: drive.time.toString(),

            driveRequestPassenger: driveRequest.passenger,
            driveRequestExtraPassengers: driveRequest.extraPassengers.toString(),
            driveRequestStartLocationLatitude: driveRequest.startLocation.latitude.toString(),
            driveRequestStartLocationLongitude: driveRequest.startLocation.longitude.toString(),
            driveRequestEndLocationLatitude: driveRequest.endLocation.latitude.toString(),
            driveRequestEndLocationLongitude: driveRequest.endLocation.longitude.toString(),
            driveRequestTime: driveRequest.time.toString()
          }
        };
      }

      if (notificationData.type === PASSENGER_REJECT) {
        recieverTokenId = passenger.notificationTokenId;

        payload = {
          data: {
            type: PASSENGER_REJECT.toString(),
            drive: notificationData.drive.userId,
            driveRequest: notificationData.driveRequest.userId
          }
        };
      }
      return admin.messaging().sendToDevice(recieverTokenId, payload)
        .then((response) => {
          const error = response.results[0].error;
          if(error){
            console.error("Couldn't send notification", error);
          }
          return admin.database().ref(`/notifications/${notificationId}`).remove();
        });
    });
  });
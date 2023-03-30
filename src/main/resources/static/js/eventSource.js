function subscribe(widgetSessionId) {
  console.log("Subscribe on events for " + widgetSessionId);
  const eventSource = new EventSource("/api/v1/events/" + widgetSessionId);
  eventSource.addEventListener("success", function (event) {
    console.log(
      "Received success event for " + widgetSessionId + ": " + event.data
    );
    // TODO: Specify targetOrigin properly
    window.parent.postMessage(JSON.parse(event.data).refreshAddress, "*");
    eventSource.close();
    console.log("Unsubscribe on events for " + widgetSessionId);
  });

  eventSource.addEventListener("error", function (event) {
    console.log(
      "Received error event for " + widgetSessionId + ": " + event.data
    );
  });

  eventSource.addEventListener("authenticate", function (event) {
    console.log(
      "Received authenticate event for " + widgetSessionId + ": " + event.data
    );

    document
      .getElementById("start-webauthn-auth-button")
      .addEventListener("click", () => {
        authenticateWithWebAuthnCredentials(event);
      });
    document.getElementById("start-webauthn-auth").style.display = "block";
    document.getElementById("qrcode-viewer").style.display = "none";
  });

  eventSource.onerror = async (err) => {
    console.error(
      "Error occurred while listening on events for " + widgetSessionId + ": ",
      err
    );
  };
}

function uuidv4() {
  return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
    (
      c ^
      (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
    ).toString(16)
  );
}

let widgetSessionId = uuidv4();
subscribe(widgetSessionId);

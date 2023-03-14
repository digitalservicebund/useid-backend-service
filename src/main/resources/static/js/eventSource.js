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

    let authenticationEvent = JSON.parse(event.data);
    let credentialId = authenticationEvent.credentialId;
    let credentialGetJson = JSON.parse(authenticationEvent.credentialGetJson);

    console.log(credentialGetJson);
    credentialGetJson.publicKey.allowCredentials[0].id = base64urlToUint8Array(
      credentialGetJson.publicKey.allowCredentials[0].id
    );
    credentialGetJson.publicKey.challenge = base64urlToUint8Array(
      credentialGetJson.publicKey.challenge
    );
    console.log(credentialGetJson);

    navigator.credentials.get(credentialGetJson).then((credentials) => {
      console.log(credentials);
      finishAuthentication(
        credentialId,
        convertAuthenticationCredentials(credentials)
      )
        .then((r) => r.json())
        .then((response) => {
          console.log(response.refreshAddress);
          window.parent.postMessage(response.refreshAddress, "*");
          eventSource.close();
          console.log("Unsubscribe on events for " + widgetSessionId);
        });
    });
  });

  eventSource.onerror = async (err) => {
    console.error(
      "Error occurred while listening on events for " + widgetSessionId + ": ",
      err
    );
  };
}

function finishAuthentication(credentialId, flatCred) {
  return fetch(`/api/v1/credentials/${credentialId}/authentications`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(flatCred),
  });
}

function convertAuthenticationCredentials(credentials) {
  let flatCred = flatten(credentials);
  flatCred.response = flatten(flatCred.response);
  flatCred.clientExtensionResults = credentials.getClientExtensionResults();
  flatCred.rawId = decodeArrayBuffer(credentials.rawId);
  flatCred.response.clientDataJSON = decodeArrayBuffer(
    credentials.response.clientDataJSON
  );
  flatCred.response.authenticatorData = decodeArrayBuffer(
    credentials.response.authenticatorData
  );
  flatCred.response.signature = decodeArrayBuffer(
    credentials.response.signature
  );
  flatCred.response.userHandle = decodeArrayBuffer(
    credentials.response.userHandle
  );
  return flatCred;
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

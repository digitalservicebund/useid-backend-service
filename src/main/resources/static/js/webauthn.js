function registerWebAuthnCredentials(widgetSessionId) {
  let credentialId;

  fetch(`/api/v1/credentials`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      widgetSessionId: widgetSessionId,
      refreshAddress: "https://digitalservice.bund.de",
    }),
  })
    .then((r) => r.json())
    .then((startRegistrationResponse) => {
      credentialId = startRegistrationResponse.credentialId;
      return createWebAuthnCredentialsViaBrowser(startRegistrationResponse);
    })
    .then((credentials) =>
      updateCredentials(
        credentialId,
        convertRegistrationCredentials(credentials)
      )
    );
}

function createWebAuthnCredentialsViaBrowser(startRegistrationResponse) {
  let pkcCreationOptions = JSON.parse(
    startRegistrationResponse.pkcCreationOptions
  );

  pkcCreationOptions.publicKey.challenge = base64urlToUint8Array(
    pkcCreationOptions.publicKey.challenge
  );
  pkcCreationOptions.publicKey.user.id = base64urlToUint8Array(
    pkcCreationOptions.publicKey.user.id
  );

  return navigator.credentials.create(pkcCreationOptions);
}

function convertRegistrationCredentials(credentials) {
  let flatCred = flatten(credentials);
  flatCred.response = flatten(flatCred.response);
  flatCred.response.transports = credentials.response.getTransports();
  flatCred.clientExtensionResults = credentials.getClientExtensionResults();
  flatCred.rawId = decodeArrayBuffer(credentials.rawId);
  flatCred.response.clientDataJSON = decodeArrayBuffer(
    credentials.response.clientDataJSON
  );
  flatCred.response.attestationObject = decodeArrayBuffer(
    credentials.response.attestationObject
  );
  return flatCred;
}

function updateCredentials(credentialId, flatCred) {
  return fetch(`/api/v1/credentials/${credentialId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(flatCred),
  });
}

function authenticateWithWebAuthnCredentials(event) {
  let authenticationEvent = JSON.parse(event.data);
  let credentialId = authenticationEvent.credentialId;
  let credentialGetJson = JSON.parse(authenticationEvent.credentialGetJson);

  credentialGetJson.publicKey.allowCredentials[0].id = base64urlToUint8Array(
    credentialGetJson.publicKey.allowCredentials[0].id
  );
  credentialGetJson.publicKey.challenge = base64urlToUint8Array(
    credentialGetJson.publicKey.challenge
  );

  navigator.credentials.get(credentialGetJson).then((credentials) => {
    finishAuthentication(
      credentialId,
      convertAuthenticationCredentials(credentials)
    )
      .then((r) => r.json())
      .then((response) => {
        window.parent.postMessage(response.refreshAddress, "*");
      });
  });
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

function flatten(obj) {
  let result = {};
  for (let key in obj) {
    result[key] = obj[key];
  }
  return result;
}

function decodeArrayBuffer(buffer) {
  let uint8Array = new Uint8Array(buffer);
  let string = String.fromCharCode.apply(null, uint8Array);
  let b64 = btoa(string);
  let b64url = b64.replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
  return b64url;
}

function base64urlToUint8Array(base64url) {
  let base64 = base64url
    .replace(/-/g, "+")
    .replace(/_/g, "/")
    .replace(/\s/g, "");
  let string = atob(base64);
  let charCodeArray = string.split("").map((c) => c.charCodeAt(0));
  return new Uint8Array(charCodeArray);
}

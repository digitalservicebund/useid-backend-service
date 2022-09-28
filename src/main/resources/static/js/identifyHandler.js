const eidClientButton = document.getElementById("eid-client-button");
eidClientButton.setAttribute(
  "href",
  "eid://127.0.0.1:24727/eID-Client?" + window.location.hash.replace("#", "")
);

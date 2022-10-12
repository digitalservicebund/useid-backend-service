const eidClientButton = document.getElementById("eid-client-button");
const eidClientBaseUrl = `${
  eidClientButton.baseURI
}eID-Client?${window.location.hash.replace("#", "")}`;
eidClientButton.setAttribute("href", eidClientBaseUrl);

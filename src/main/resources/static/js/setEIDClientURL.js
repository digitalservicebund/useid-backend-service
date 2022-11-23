const hash = new URL(window.location).searchParams.get("hash");
const tcTokenURLParam = window.location.hash.replace("#", "");

const eidClientButton = document.getElementById("eid-client-button");
const eidClientURL = `${eidClientButton.baseURI}eID-Client?${tcTokenURLParam}&hash=${hash}`;
eidClientButton.setAttribute("href", eidClientURL);

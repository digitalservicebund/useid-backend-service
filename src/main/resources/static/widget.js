function getIframe() {
  let tcTokenURL = new URL(document.currentScript.src).searchParams.get(
    "tcTokenURL"
  );
  if (tcTokenURL === null) {
    tcTokenURL = document.getElementById("useid-widget").dataset.tcTokenUrl;

    if (tcTokenURL === undefined) {
      const error = document.createElement("div");
      error.innerHTML = "Fehlerhafte Konfiguration: TC Token nicht definiert.";
      return error;
    }
  }

  const iframe = document.createElement("iframe");
  iframe.setAttribute(
    "src",
    `https://useid.dev.ds4g.net/widget?tcTokenURL=${tcTokenURL}`
  );
  iframe.style.border = "2px solid #B8BDC3";
  iframe.style.borderRadius = "20px";
  iframe.style.width = "100%";
  iframe.style.minHeight = "600px"; // TODO: Set height to fit
  return iframe;
}

const container = document.getElementById("useid-widget-container");
container === null
  ? document.write(getIframe().outerHTML)
  : container.appendChild(getIframe());

window.addEventListener("message", (e) => {
  if (e.origin === "https://useid.dev.ds4g.net") {
    location.href = e.data;
  }
});

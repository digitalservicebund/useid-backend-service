const useIdUrl = new URL(document.currentScript.src).origin;
const widgetContainerId = "useid-widget-container";

const widgetContainer =
  document.getElementById(widgetContainerId) ??
  (() => {
    const container = document.createElement("div");
    container.id = widgetContainerId;
    document.write(container.outerHTML);
    return container;
  })();

(async () => {
  let tcTokenURL =
    widgetContainer?.dataset.tcTokenUrl ??
    new URLSearchParams(
      new URL(document.currentScript.src).hash.substring(1)
    ).get("tcTokenURL");

  if (!tcTokenURL) {
    const error = document.createElement("div");
    error.innerHTML =
      "Fehlerhafte Konfiguration: TC-Token-URL nicht definiert.";
    return error;
  }

  const hashBuffer = await crypto.subtle.digest(
    "SHA-256",
    new TextEncoder().encode(tcTokenURL)
  );
  const hashString = Array.from(new Uint8Array(hashBuffer))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
  tcTokenURL = encodeURIComponent(tcTokenURL);

  const iframe = document.createElement("iframe");
  iframe.setAttribute(
    "src",
    `${useIdUrl}/qrcode-widget?hostname=${location.host}&hash=${hashString}#tcTokenURL=${tcTokenURL}`
  );
  iframe.setAttribute(
    "allow",
    "publickey-credentials-get *" // TODO only allow for specific domains
  );
  iframe.name = "Mit BundesIdent online ausweisen";
  iframe.style.width = "100%";
  iframe.style.height = "100%";
  return iframe;
})().then((child) => widgetContainer.appendChild(child));

window.addEventListener("message", (e) => {
  if (e.origin === useIdUrl && e.data.type !== "shortCuts") {
    location.href = e.data;
  }
});

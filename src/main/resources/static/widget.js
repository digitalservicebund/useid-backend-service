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
// TODO: Default styling for container? Discuss with design team.

widgetContainer.appendChild(
  (() => {
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

    tcTokenURL = encodeURIComponent(tcTokenURL);

    const iframe = document.createElement("iframe");
    iframe.setAttribute(
      "src",
      `${useIdUrl}/widget?hostname=${location.hostname}#tcTokenURL=${tcTokenURL}`
    );
    iframe.style.width = "100%";
    iframe.style.minHeight = "600px"; // TODO: Adjust to design? Discuss with design team.
    return iframe;
  })()
);

window.addEventListener("message", (e) => {
  if (e.origin === useIdUrl) {
    location.href = e.data;
  }
});

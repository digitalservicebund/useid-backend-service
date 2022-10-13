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
      `${useIdUrl}/widget?hostname=${location.host}#tcTokenURL=${tcTokenURL}`
    );
    iframe.style.width = "100%";
    iframe.style.minHeight = "600px";
    return iframe;
  })()
);

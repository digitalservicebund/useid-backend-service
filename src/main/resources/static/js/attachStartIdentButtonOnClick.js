document
  .getElementById("eid-client-button")
  .addEventListener("click", async function () {
    await fetch("/start-ident-button-clicked", {
      cache: "no-store",
      method: "POST",
      keepalive: true,
    });
  });

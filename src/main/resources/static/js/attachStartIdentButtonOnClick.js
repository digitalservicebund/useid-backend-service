document
  .getElementById("eid-client-button")
  .addEventListener("click", async function () {
    const hash = new URL(window.location).searchParams.get("hash");
    await fetch(`/start-ident-button-clicked?hash=${hash}`, {
      cache: "no-store",
      method: "POST",
      keepalive: true,
    });
  });

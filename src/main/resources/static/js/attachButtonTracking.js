document
  .getElementById("eid-client-button")
  .addEventListener("click", async function () {
    await fetch("/app-opened", {
      cache: "no-store",
      method: "POST",
      keepalive: true,
    });
  });

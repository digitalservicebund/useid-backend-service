const eventSource = new EventSource("/v1/sse");

const getQRCode = document.getElementById("downloadQR");

eventSource.addEventListener("ready", () => {
  getQRCode.src =
    "/v1/qrcode/180?url=http%3A%2F%2Fwww.google.com&uuid=" + event.data;
});
eventSource.addEventListener("ping", (event) => console.log(event));
eventSource.addEventListener("close", (event) => {
  console.log(event.data);
  eventSource.close();
});

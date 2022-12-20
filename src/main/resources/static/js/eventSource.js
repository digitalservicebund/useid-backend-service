function subscribe(widgetSessionId) {
  console.log("Subscribe on events for " + widgetSessionId);
  const eventSource = new EventSource("/api/v1/events/" + widgetSessionId);
  eventSource.addEventListener("success", function (event) {
    console.log("Received event for " + widgetSessionId + ": " + event.data);

    const eventData = JSON.parse(event.data);
    if (eventData.success !== undefined) {
      if (eventData.success) {
        // TODO: Specify targetOrigin properly
        window.parent.postMessage(eventData.refreshAddress, "*");
      } else {
        console.error("Error event received: ", eventData.message);
      }
    }
  });

  eventSource.addEventListener("close", (event) => {
    console.log(
      "Received close event for " + widgetSessionId + ": " + event.data
    );
    eventSource.close();
  });
}

function uuidv4() {
  return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
    (
      c ^
      (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
    ).toString(16)
  );
}

let widgetSessionId = uuidv4();
subscribe(widgetSessionId);

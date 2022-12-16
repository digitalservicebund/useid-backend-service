function subscribe(widgetSessionId) {
  console.log("Subscribe on events for " + widgetSessionId);
  const eventSource = new EventSource("/api/v1/events/" + widgetSessionId);
  eventSource.addEventListener("success", function (event) {
    console.log(
      "Received success event for " + widgetSessionId + ": " + event.data
    );
    console.log(event);
    // TODO do stuff
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

subscribe(uuidv4());

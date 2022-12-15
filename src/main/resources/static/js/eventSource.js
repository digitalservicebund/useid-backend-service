function subscribe(consumer) {
  const eventSource = new EventSource("/api/v1/events/" + consumer);
  eventSource.addEventListener("success", function (event) {
    console.log("Received success event for " + consumer + ": " + event.data);
    console.log(event);
    // TODO do stuff
  });

  eventSource.addEventListener("close", (event) => {
    console.log("Received close event for " + consumer + ": " + event.data);
    eventSource.close();
  });
}

subscribe("00000000-0000-0000-0000-000000000001");
subscribe("00000000-0000-0000-0000-000000000002");

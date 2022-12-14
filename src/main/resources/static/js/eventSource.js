const eventSource = new EventSource("/v1/sse");

eventSource.addEventListener("close", (event) => {
  console.log(event.data);
  eventSource.close();
});

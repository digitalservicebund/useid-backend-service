# 8. Server Sent Events for success event from backend server to specific client

Date: 2022-07-05

## Status

Accepted

## Context

After a successful identification via mobile app, we need to inform the widget where the user initially scanned the QR Code to proceed with the identification flow. We want to ensure a unidirectional connection from our backend to the widget (e.g. no websocket connection) and avoid polling by the widget to minimize network traffic.

## Decision

We use [server-sent event](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events) (SSE) to inform the client (eService) about the status of the identification (failure or success) from backend.

## Consequences

We benefit from the advantages of SSE. 

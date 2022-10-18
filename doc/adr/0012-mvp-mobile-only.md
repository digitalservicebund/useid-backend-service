# 12. MVP mobile only

Date: 2022-10-18

## Status

Accepted

Supercedes:

- [6. URL format in QR code](0006-url-format-in-qr-code.md)
- [7. Generate QR Code on the client instead of on the backend](0007-generate-qr-code-on-client.md)
- [8. Server Sent Events for success event from backend server to specific client](0008-server-sent-event.md)

## Context

The device switch from mobile to desktop opens a door for phishing attacks in the current setup.

For the first use case, the goal is to develop an MVP.

## Decision

To reduce the complexity of the product, we focus on a mobile only flow.

Instead of a QR code, the widget displays a button for the user to click which opens the BundesIdent app.

There is no back channel to the widget.

## Consequences

The mobile only flow makes all phishing attacks regarding the device switch impossible.

No QR codes will be needed.

Server-Sent events are not needed anymore and will not be implemented.

Cross-document messaging between the `widget.js` script and the parent page as described in
[4. Embedding widget via JS wrapper](0004-embedding-widget-via-js-wrapper.md)
is not needed anymore and will not be implemented.

# 7. Generate QR Code on the client instead of on the backend

Date: 2022-06-30

## Status

Accepted

Superseded by [12. MVP mobile only](0012-mvp-mobile-only.md)

## Context

A QR code is embedded in the eService page via iFrame widget, and it enables user to start identification process via camera app on a mobile device. (For details see [ADR 0006](LINK TO 0006).)

The current QR Code generation is handled on the server, the decision was not documented in an ADR, but it was a verbal decision with the assumptions that server-side rendering would reduce the render time and the data transfer between client and server would be secured with HTTPS.

## Decision

We decided to move QR Code render to client-side instead of server side with the following reasons:

Since the QR Code includes sensitive data which should not be known to our backend server (e.g. `encryptionKey` and `tcTokenURL`), the rendering must happen on client-side.

## Consequences

1. Increase in page load time.
   - Client side rendering means, we would need a Javascript library to transform the URL into a matrix barcode, and the transformation requires another third-party Javascript library in the frontend.
2. Security issue
   - We introduce third-party Javascript library, and we should mitigate the quality attributes, mainly the security aspect of the library.
3. UseID backend will not have any knowledge of `tcTokenUrl` and `encryptionKey`

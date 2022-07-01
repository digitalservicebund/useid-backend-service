# 7. Generate QR Code on the client instead of on the backend

Date: 2022-06-30

## Status

Accepted

## Context

A QR code is embedded in the eService page via iFrame widget, and it enables user to start identification process via camera app on a mobile device. (For details see [ADR 0006](LINK TO 0006).)

The current QR Code generation is handled in the server `/v1/qrcode/{imageSize}`, the decision was not written in ADR, but it was a verbal decision with assumptions that Server-Side Rendering would reduce QR Code render time and data exchange between client and server would be secured with http over TLS.

## Decision

We decided to move QR Code render to client-side instead of server side with the following reasons:

1. We want to increase security aspect of the `tcTokenURL` since it's very crucial, and it is used to trigger identification in app
2. We include `encryptionKey` in the QR Code, so that the mobile app can encrypt the `RefreshAddress` (the address where the users will be taken to, after a successful authentication)

## Consequences

1. Increase in page load time.
   - Client side rendering means, we would need a Javascript library to transform the URL into a matrix barcode, and the transformation requires another third-party Javascript library in the frontend.
2. Security issue
   - We introduce third-party Javascript library, and we should mitigate the quality attributes, mainly the security aspect of the library.
3. UseID backend will not have any knowledge of `tcTokenUrl` and `encryptionKey`

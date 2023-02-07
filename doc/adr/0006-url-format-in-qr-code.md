# 6. URL format in QR code

Date: 2022-06-24

## Status

Superseded by [12. MVP mobile only](0012-mvp-mobile-only.md) and [13. Format of the eID-Client URL](0013-format-of-eid-client-url.md)

## Context

The web widget displays a QR code, which transfers data to the mobile device, i.e. into the eID client.

The QR code needs to include the ClientURL, including the `tcTokenURL`, (see [TR-03124 part 1](https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR03124/TR-03124-1.pdf) for more information).

Additionally, some kind of widget session id needs to be added, so that the proxy message server can identify this exact widget when the mobile app wants to send a message to the widget.

Lastly, an [encryption key](https://github.com/digitalservicebund/useid-architecture/blob/df7596d0725fdbfab70f08d3b29a5641ac711fde/doc/adr/0004-e2ee-for-refresh-address.md) needs to be included. This should never be sent to any server and only be known to the widget and the mobile app.

On mobile devices, the user won't see a QR code, but only a link. The app behaves differently on mobile devices (opening the refresh address instead of informing the UseID Backend Server), so the mobile app needs to know if the user started from mobile device or not.

## Decision

The URL format follows this schema:

```
eid://127.0.0.1:24727/eID-Client?tcTokenURL={{tcTokenURL}}&widgetSessionId={{widgetSessionId}}&mobile={{isMobileDevice}}#{{encryptionKey}}
```

The encryption key (`{{encryptionKey}}`) is base64 encoded.

The mobile flag (`{{isMobileDevice}}`) has boolean values (`true` or `false`).

## Consequences

The URL follows the Technical Guideline and is compatible to other eID clients.

The URL contains all the needed data, including the `widgetSessionId` and the encryption key.

This is a URL, which is never evaluated or opened in a browser. With regard to possible future changes, the encryption key is added as fragment id, so it won't ever be sent to any server.

The length will be around 295 characters:

- static URL parts: 70
- uuid as `widgetSessionId`: 36
- mobile flag: 4-5
- 256 bit encryption key (in base64): 44
- `tcTokenURL`: around 120-140 (depending on the specific token format)

This would fit e.g. in a QR code version 10 with error correction level M (max. 311 characters).

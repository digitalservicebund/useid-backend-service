# 13. Format of the eID-Client URL

Date: 2022-10-18

## Status

Accepted

Supersedes [6. URL format in QR code](0006-url-format-in-qr-code.md)

## Context

The widget needs to link to the eID-Client URL, including the `tcTokenURL`,
(see [TR-03124 part 1](https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR03124/TR-03124-1.pdf)
for more information).

It shall be ensured that the BundesIdent app is opened and not another eID-Client.

## Decision

The URL format follows this schema:

```
https://{{useidDomain}}/eID-Client?tcTokenURL={{tcTokenURL}}
```

The `useidDomain` depends on the environment. The BundesIdent app registers this URL as
[universal link (iOS)](https://developer.apple.com/ios/universal-links/) /
[app link (iOS)](https://developer.android.com/training/app-links).

## Consequences

Calling the URL (user clicks the button in the widget) the BundesIdent app and no other eID-Client will be opened.

The eID-Client URL does not follow the
[Technical Guideline for eID-Client](https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR03124/TR-03124-1.pdf)
anymore and is therefore not compatible with other eID-Clients.

The server needs to implement additional means for universal links / app links to work (i.e. provide files `apple-app-site-association` and `assetlinks.json`, resp.).

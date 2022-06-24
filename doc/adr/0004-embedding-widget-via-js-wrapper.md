# 4. Embedding widget via JS wrapper

Date: 2022-06-24

## Status

Accepted

## Context

The eService (Service Provider) embeds a widget page via iframe displaying a QR code, which will be scanned on a mobile device. After finishing the eID process on the mobile device, the user needs to be redirected to the `refreshAddress`, which the eID client app received from the TC token.

The refresh address is sent via a message proxy server from the eID client app to the widget page.

Iframes can't update the parent's `location.href` due to the same-origin policy, so redirecting the user out of the iframe is not possible.

## Decision

We provide a JavaScript wrapper file which will be included in the eService page and which creates the widget iframe. This allows us to communicate between the widget and the parent page via cross-document messaging (`postMessage` and `window.onmessage`) and therefore update the `location.href`.

To embed the widget, the JS file inserts the iframe to the DOM. Therefore, we require a `div` container with a specific id, in which the iframe is inserted. With this, the eServer page can add styles to the container.

## Consequences

A file `widget.js` needs to be provided.

The eService (Service Provider) page needs to set the Content-Security-Policy (CSP) header (`script-src`) to allow embedding external JavaScript from the UseID domain.

The iframe code can be updated without a need to enforce a change by the eService (e.g. if the widget endpoint changes).

The `widget.js` file needs to be served from a fixed URL.

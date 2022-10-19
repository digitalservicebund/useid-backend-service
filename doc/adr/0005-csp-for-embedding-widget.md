# 5. CSP for embedding widget

Date: 2022-06-24

## Status

Accepted

## Context

The web widget for communication with the eID mobile client is embedded as iframe.

Pages embedding the widget need to register at UseId before they're allowed to use the service.

## Decision

We use the [`frame-ancestors` directive at the CSP header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/frame-ancestors). The browsers only allow pages to embed an iframe, whose domain is listed there.

We set this header only to the one domain, which is actually embedding the iframe and don't provide a full list of all allowed domains.

The page inside the iframe can't get the domain of the parent page. Therefore the [widget.js file](0004-embedding-widget-via-js-wrapper.md) adds the domain of the parent page as query parameter to the iframe url.

The backend can check the domain from the query parameter with the list of registered domains and if the domain is found, it is added into the CSP header.

We evaluated using a kind of api key (which would then be public) and decided to use the domain name as quasi public api key.

## Consequences

Only the registered domains are allowed to embed widgets and this behaviour is enforced by the browsers, when a CSP header with `frame-ancestors` is present.

By adding the parent domain as query parameter, the server can check this domain in the list. We therefore need to have a list of registered domains in the backend server.

It is possible to automatically check, whether a domain name is registered for this service or not. If this information should not be leaked, a switch to randomly chosen (public) API keys could be done.

It's not possible to MITM this embedding (in default browsers). The domain can't be chosen arbitrarily, since we check it in the list of registered domains on the backend server. So we have a direct dependency/connection between embedding and embedded page.

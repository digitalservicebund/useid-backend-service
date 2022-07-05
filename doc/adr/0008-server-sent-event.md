# 7. Server Sent Events for success event from backend server to specific client

Date: 2022-07-05

## Status

Accepted

## Context

After a success identification via mobile app, we want to inform user on the website (the start of the identification process), specifically on the widget where the user initially scanned the QR Code. We have to inform our backend that identification process has been finished, so that backend can relay the message to the page (eService), also we need to keep in mind that eService shall not request to our backend to get the status of the identification every time, because the backend will be overloaded with multiple requests from clients (e.g. eService)

## Decision

We use server-sent event to inform client (eService) the status of the identification (failure or success) from backend.

## Consequences

User will be redirected to a correct address after a successful/unsuccessful identification and continue the journey as expected

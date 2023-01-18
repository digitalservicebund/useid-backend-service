# 16. Widget as iFrame

Date: 2023-01-10

## Status

Accepted

## Context

We want to provide a standardized entry point for the users in order for them to recognize
the eID process across different eServices. We believe that a unified user-facing entry point, served as a widget,
will benefit our customers. To achieve this, there are multiple options. We consider the following three as most feasible:

1. provide framework-specific web components (e.g. react, vue) as a library

   - benefits
     - easy to import and easily reusable across eServices
     - easy to ship updated components to the eServices
     - highly reliable with expected behaviour within the framework
   - costs
     - difficult to integrate into legacy frontend stacks (likely to face in our governmental context)
     - high maintenance / development effort from our side
     - might over-complex a simple case

2. bundle code into an iFrame and ship through a JavaScript <script> tag

   - benefits
     - low maintenance / development effort from our side, since one solution "fits all"
     - easy to ship updated components to the eServices
     - reliable provision, because JavaScript inside the iframe is running in the context of another page and main page rendering does not block the iFrame rendering
     - no interference with main page’s CSS results in a standardized styling across eServices
     - JavaScript's communication to main page is possible
   - costs
     - customers have to provide width and height values. Otherwise, the browser is rendering nothing
     - clickjacking with iframes is possible, though can be reduced with Content-Security-Policy (in our case frame-ancestor)
     - not responsive by design
     - customers could have a no-iFrame-policy
     - customers with front-end frameworks like react or vue might face extra work due to the script interfering with the virtual DOM
     - iFrame and main page are using the same connection pool, might block the onLoad event

3. prepare HTML and CSS files and ship through a JavaScript <script> tag

   - benefits
     - easy to integrate, since it is the solution with the least amount of dependencies (e.g. no libraries, iFrames, special components)
     - low maintenance effort since one solution "fits all"
     - no specific width and height needed, will adjust within the main page ideally automatically
   - costs
     - main page's CSS will apply for the widget, customers will need to take styling and layout in their own hands
     - users of multiple eServices might see different layouts at each eService
     - testing is difficult, because every eService has different HTML trees and a certain set of CSS rules

## Decision

We choose Option 2 and ship an iFrame through a JavaScript <script> tag,
because it is the simplest and safest way to ensure that the styling of our widget stays
the same across various eServices that might have implemented different front end stacks with different CSS rules.

## Consequences

We provide a specific integration guide where necessary steps, like setting a value for width and height, are stated in detail.
Our iFrame will be served with various security measures like a CSP header, so only domains that are allowed by us will be able to integrate it.
There might be eServices that restrict the usage of iFrames. In our current setup this is not the case and will be dealt with by providing individual solutions if need be.

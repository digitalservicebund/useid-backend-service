# Load testing

## Installing

```sh
brew install k6
```

## Running tests

Testing local running instance (`localhost:8080` with API key `foobar`):

```sh
k6 run test.js
```

### Custom instance settings

Base URL and API key can be configured using environment variables `USEID_BASEURL` and `USEID_APIKEY` resp.:

```sh
USEID_BASEURL="https://useidinstance" USEID_APIKEY="secretapikey" k6 run test.js
```

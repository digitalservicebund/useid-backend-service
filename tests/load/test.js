import http from "k6/http";
import { check, fail, group } from "k6";
import { Rate, Trend } from "k6/metrics";

export const errorRate = new Rate("errors");
export const sessionCompletionTime = new Trend("session_duration", true);
export const eidServerDuration = new Trend("eid_server_duration", true);

export let options = {
  scenarios: {
    // 6 sessions per second for 1 minute
    constant_request_rate: {
      executor: "constant-arrival-rate",
      rate: 6,
      timeUnit: "1s",
      duration: "1m",
      preAllocatedVUs: 6,
      maxVUs: 20,
    },
  },
  thresholds: {
    errors: ["rate<=0"], // no errors
    session_duration: ["p(95)<1000"], // 95% of all sessions should take max 1s
  },
};

const baseUrl = "http://localhost:8080";
const apiKey = "foobar";

function abort(msg) {
  errorRate.add(1);
  fail(msg);
}

export default function () {
  const params = {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${apiKey}`,
    },
  };

  let startTime = Date.now();
  let tcTokenUrl;

  group("create session", function () {
    let response = http.post(
      `${baseUrl}/api/v1/identification/sessions`,
      {},
      params
    );
    check(response, {
      "status is 200": (r) => r.status === 200,
      "response body contains tcTokenUrl": (r) =>
        r.json("tcTokenUrl") !== undefined,
    }) || abort(response.status);
    tcTokenUrl = response.json("tcTokenUrl");
  });

  group("fetch tc tokens", function () {
    const requestStartTime = Date.now();

    let response = http.get(`${tcTokenUrl}`);
    check(response, {
      "status is 200": (r) => r.status === 200,
    }) || abort(response.status);

    errorRate.add(0);
    sessionCompletionTime.add(Date.now() - startTime);
    eidServerDuration.add(Date.now() - requestStartTime);
  });
}

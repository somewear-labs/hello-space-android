## Webhook Sample

The Somewear SDK and Somewear mobile app can be configured to forward a variety of events to your backend application in near real-time via a webhook. Somewear will send these events either over traditional networks or satellite. Below is a sample webhook configuration and JSON payload.

Url: `POST {baseUrl}/somewear`

Content-type: `application/json`

Sample body:
```json
{
  "data":[
    {
      "deviceId":"abcdef123456",
      "userId":"abcd",
      "events":[
        {
          "type":"location",
          "eventId":"bddab869-5da8-4ff7-b5ee-b3441611d987",
          "latitiude":"37.750007",
          "longitude":"-122.411573",
          "timestamp":"2019-03-13T14:43:36Z"
        },
        {
          "type":"deviceInfo",
          "eventId":"cddab869-5da8-4ff7-b5ee-b3441611d988",
          "battery":91,
          "trackingInterval":300
        },
        {
          "type":"message",
          "eventId":"dddab869-5da8-4ff7-b5ee-b3441611d989",
          "content":"Hello from space!",
          "timestamp":"2019-03-13T14:43:36Z"
        },
        {
          "type":"sos",
          "eventId":"eddab869-5da8-4ff7-b5ee-b3441611d980",
          "event":"alarm",
          "timestamp":"2019-03-13T14:43:36Z"
        },
        {
          "type":"data",
          "eventId":"fdab869-5da8-4ff7-b5ee-b3441611d981",
          "payload":"SGVsbG8gZnJvbSBzcGFjZSE=",
          "timestamp":"2019-03-13T14:43:36Z"
        },
        {
          "type":"health",
          "eventId":"addab869-5da8-4ff7-b5ee-b3441611d982",
          "heartRate":"72",
          "activity":"walking"
        }
      ]
    }
  ]
}
```

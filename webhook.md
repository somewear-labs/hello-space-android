## Webhook Sample

The Somewear SDK and Somewear mobile app can be configured to forward a variety of events to your backend application in near real-time via a webhook. Somewear will send these events either over traditional networks or satellite. Below is a sample webhook configuration and JSON payload.

Url: `POST {baseUrl}/somewear`

Content-type: `application/json`

Sample body:
```json
[
  {
    "deviceId":"abcdef123456",
    "events":[
      {
        "type":"location",
        "latitiude":"37.750007",
        "longitude":"-122.411573",
        "timestamp":"2019-03-13T14:43:36Z"
      },
      {
        "type":"deviceInfo",
        "battery":91,
        "trackingInterval":300
      },
      {
        "type":"message",
        "content":"Hello from space!",
        "timestamp":"2019-03-13T14:43:36Z"
      },
      {
        "type":"sos",
        "event":"alarm",
        "timestamp":"2019-03-13T14:43:36Z"
      },
      {
        "type":"data",
        "payload":"SGVsbG8gZnJvbSBzcGFjZSE=",
        "timestamp":"2019-03-13T14:43:36Z"
      },
      {
        "type":"health",
        "heartRate":"72",
        "activity":"walking"
      }
    ]
  }
]
```

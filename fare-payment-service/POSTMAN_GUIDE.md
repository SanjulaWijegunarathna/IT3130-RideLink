# Fare Payment Service Postman Guide

The service listens at `http://localhost:8084`. Start MongoDB at `localhost:27017`, then start the Spring Boot application from this module using its Maven wrapper.

Create these Postman environment variables:

| Variable | Value |
|---|---|
| `baseUrl` | `http://localhost:8084` |
| `rideId` | `101` |
| `paymentId` | Save the `id` from a payment response |

For JSON requests, set `Content-Type: application/json` and `Accept: application/json`. These endpoints currently do not require authentication.

## Create a fare estimate

`POST {{baseUrl}}/api/fares/estimate`

Body (raw, JSON):

```json
{
  "pickup": "SLIIT Malabe",
  "destination": "Colombo Fort"
}
```

Optional coordinate example; provide all four coordinates together:

```json
{
  "pickup": "SLIIT Malabe",
  "destination": "Colombo Fort",
  "pickupLatitude": 6.9147,
  "pickupLongitude": 79.9729,
  "destinationLatitude": 6.9271,
  "destinationLongitude": 79.8612
}
```

Success: `201 Created`. The fare record is written to MongoDB's `fares` collection. Response fields include `id`, `rideId`, `pickup`, `destination`, `distanceKm`, `estimatedFare`, `finalFare`, `ruleDescription`, and `createdAt`.

Legacy aliases are also available: `POST {{baseUrl}}/api/v1/fares-payments/estimate`, `POST {{baseUrl}}/api/v1/fares-payments/final`, and `GET {{baseUrl}}/api/v1/fares-payments`.

## Create a final fare

`POST {{baseUrl}}/api/fares/final`

```json
{
  "rideId": {{rideId}},
  "pickup": "SLIIT Malabe",
  "destination": "Colombo Fort"
}
```

Success: `201 Created`. `rideId` must be a positive JSON number. Coordinates are optional but must be supplied as a complete set.

## Read fare records

`GET {{baseUrl}}/api/fares`

Success: `200 OK` with saved fare records as a JSON array.

## Create a payment

`POST {{baseUrl}}/api/payments`

```json
{
  "rideId": {{rideId}},
  "passengerAccountId": 1,
  "amount": 420.0,
  "paymentMethod": "CARD",
  "simulateFailure": false
}
```

Required: positive numeric `rideId` and `amount`. Optional `paymentMethod` values are `CASH`, `CARD`, or `WALLET` (defaults to `CASH`). Set `simulateFailure` to `true` to save a failed payment. The service prevents creating a second payment for a ride and returns the existing one. Success: `201 Created`; payment is saved to the `payments` collection. Save the returned `id` as the `paymentId` variable.

## Read payments

- `GET {{baseUrl}}/api/payments` returns all payments.
- `GET {{baseUrl}}/api/payments/{{paymentId}}` returns a payment by MongoDB ID.
- `GET {{baseUrl}}/api/payments/ride/{{rideId}}` returns a payment by ride ID.

Successful reads return `200 OK`. A missing payment returns `404 Not Found`.

## Delete a payment

`DELETE {{baseUrl}}/api/payments/{{paymentId}}`

Success: `204 No Content`. There is no fare-delete endpoint.

## Submit a ride-completed event

`POST {{baseUrl}}/api/events/ride-completed`

```json
{
  "rideId": {{rideId}},
  "passengerAccountId": 1,
  "amount": 420.0,
  "paymentMethod": "CASH",
  "simulateFailure": false
}
```

Success: `202 Accepted`. Processing is asynchronous; wait briefly, then use `GET /api/payments/ride/{{rideId}}` to read the result.

## Fare calculation

Fare rule: `LKR 150 base fare + LKR 45 per km`. Complete coordinate sets use Haversine distance. If no coordinates are supplied, distance is estimated from pickup and destination text lengths, with a minimum of 2 km and maximum of 50 km.

## Common errors

- `400 Bad Request`: required fields are missing/blank, numeric values are not positive, or coordinates are incomplete/out of range.
- `404 Not Found`: requested payment does not exist.
- Connection refused: start MongoDB and the Spring Boot service; confirm the service is listening on port `8084`.

No `PUT` or `PATCH` fare/payment update endpoints are implemented.

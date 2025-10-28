<h1><img src="/src/main/resources/images/logo.svg" width="30"> Voyager API</h1>
<h3>REST services for Voyager project</h3>
A personal project I took on to relearn full-cycle development and to better utilize interairline flight benefits. Manages caching, authorization tokens, and request limits to external services.

### https://api.voyagerapp.org

### Project Repos:
- Voyager UI: https://github.com/maxinefonua/voyager-ui
  - mapped requests and web feature functions
  - dynamic page injection and  targeted fragment reloads
- Voyager API: https://github.com/maxinefonua/voyager-api
  - standalone backend services
  - caching, request limits, auth tokens
- Voyager Commons: https://github.com/maxinefonua/voyager-commons
  - an SDK for API services
  - scripts and jars for data syncing
- Voyager Tests: https://github.com/maxinefonua/voyager-tests
  - functional tests built with JUnit 5
  - an uber jar deployed and used for application deployments


### Tech Stack:
- Geolocation Data
  - GeoNames - https://www.geonames.org.com/
  - OpenStreetMap - https://www.openstreetmap.org/

- Flight/Airport Data
  - AirportsData - https://github.com/mborsetti/airportsdata/
  - FlightRadar24 - https://www.flightradar24.com/about
  - OpenStreetMap - https://www.openstreetmap.org/

- Development
  - GitHub - https://github.com/
  - IntelliJ - https://www.jetbrains.com/idea/download/?section=mac
  - Spring - https://spring.io/

- Data Storage and Hosting
  - AWS EC2 Instance - https://docs.aws.amazon.com/ec2/
  - PostgeSQL - https://www.postgresql.org/about/
  - pgAdmin - https://www.pgadmin.org/

Full README and LICENSE coming soon.
<hr>

### Required Authentication Token
Calls to all endpoints require an authorization token or API key in the request headers. Auth token registration process coming soon.

|   Header    | Datatype | Required | Description                        |
|:-----------:|:--------:|:--------:|------------------------------------|
| `X-API-KEY` |  String  | **Yes**  | registered API key to enable usage |


### Public API Endpoints
[/flights](#flightservice)
<br>[/flights/{id}](#flightid)
<br>[/flight](#flight)
<br>
<br>[/airports](#airports)
<br>[/airports/{iata}](#airportsiata)
<br>[/nearby-airports](#nearby-airports)
<br>
<br>[/airlines]()
<br>
<br>[/countries]()
<br>
<br>[/routes]()
<br>[/routes/{id}]()
<br>[/route]()
<br>
<br>[/airline-path]()
<br>[/route-path]()

### Admin API Endpoints
[/search]()
<br>
<br>[/locations](#airportsiata)
<br>[/location](#nearby-airports)

<hr>

## FlightService

| Parameter      | Datatype                            |      Required      | Default | Description                                     |
|:---------------|:------------------------------------|:------------------:|:-------:|:------------------------------------------------|
| `size`         | integer                             |         No         |   10    | returns # flights per page<br>capped at **100** |
| `page`         | integer                             |         No         |    1    | returns page # of flights                       |
| `routeId`      | comma-separated list of integers    |         No         |    -    | returns flights with given route ids            |
| `flightNumber` | alphanumeric string                 |         No         |    -    | returns flights with given flight number        |
| `airline`       | string value of[`Airline.enum`]()   |         No         |    -    | returns flights with given airline              |
| `isActive`     | boolean                             |         No         |  true   | returns flights with given status               |


## `/flights`
Returns paginated list of flights. Supplying parameters filters list accordingly.

### Request:

```bash
  curl --location "${VOYAGER_API_URL}/flights?airline=hawaiian&isActive=true&routeId=11388%2C11416&size=2" \
--header "x-api-key: ${VOYAGER_API_KEY}"
```
### Response:

```json
[
  {
    "id": 42832,
    "flightNumber": "HA15",
    "routeId": 11388,
    "zonedDateTimeDeparture": "2025-10-25T15:36:00Z",
    "zonedDateTimeArrival": "2025-10-25T21:51:00Z",
    "isActive": true,
    "airline": "HAWAIIAN"
  },
  {
    "id": 42833,
    "flightNumber": "HA611",
    "routeId": 11388,
    "zonedDateTimeDeparture": "2025-10-25T19:30:00Z",
    "zonedDateTimeArrival": "2025-10-26T01:45:00Z",
    "isActive": true,
    "airline": "HAWAIIAN"
  }
]
```
## `/flight/{id}`
Returns flight of the matching`id`.
### Request:

```bash
  curl --location "${VOYAGER_API_URL}/flights/44920" \
--header "x-api-key: ${VOYAGER_API_KEY}"
```
#### Response:

```json
{
  "id": 44920,
  "flightNumber": "AY19",
  "routeId": 11780,
  "zonedDateTimeDeparture": "2025-10-27T10:30:00Z",
  "zonedDateTimeArrival": "2025-10-27T21:10:00Z",
  "isActive": true,
  "airline": "FINNAIR"
}
```

## `/flight`
Returns matching flight of **required parameters**: `routeId`,`flightNumber`.

### Request:

```bash
  curl --location "${VOYAGER_API_URL}/flight?routeId=15247&flightNumber=DL5133" \
--header "x-api-key: ${VOYAGER_API_KEY}"
```
### Response:

```json
{
  "id": 56546,
  "flightNumber": "DL5133",
  "routeId": 15247,
  "zonedDateTimeDeparture": "2025-10-27T15:05:00Z",
  "zonedDateTimeArrival": "2025-10-27T16:36:00Z",
  "isActive": true,
  "airline": "DELTA"
}
```
## AirportService
| Parameter     | Datatype                                                       |      Required      | Default | Description                                      |
|:--------------|:---------------------------------------------------------------|:------------------:|:-------:|:-------------------------------------------------|
| `size`        | integer                                                        |         No         |   10    | returns # airports per page<br>capped at **100** |
| `page`        | integer                                                        |         No         |    1    | returns page # of airports                       |
| `countryCode` | alpha-2 string of country code                                 |         No         |    -    | returns airports of given country                |
| `type`        | comma-separated list of string values of[`AirportType.enum`]() |         No         |  `CIVIL`  | returns airports of given types                  |
| `airline`     | string value of[`Airline.enum`]()                              |         No         |    -    | returns airports of given airline                |


## `/airports`
Returns paginated list of airports. Query parameters filter list accordingly.

### Request:
```bash
  curl --locationEntity "${VOYAGER_API_URL}/airports?countryCode=gu&type=military" \
--header "X-API-KEY: ${VOYAGER_API_KEY}"
```
### Response:

```json
[
  {
    "iata": "UAM",
    "name": "Andersen Afb Airport",
    "city": "Yigo",
    "subdivision": "",
    "countryCode": "GU",
    "latitude": 13.58389,
    "longitude": 144.93005,
    "type": "MILITARY",
    "zoneId": "Pacific/Guam"
  }
]
```

## `/airports/{iata}`
Returns aiport of given IATA code.

### Request:
```bash
  curl --locationEntity "${VOYAGER_API_URL}/airports/itm" \
--header "X-API-KEY: ${VOYAGER_API_KEY}"
```
### Response:
```json
{
  "iata": "ITM",
  "name": "Osaka International Airport",
  "city": "Osaka",
  "subdivision": "Hyogo",
  "countryCode": "JP",
  "latitude": 34.7855,
  "longitude": 135.438,
  "type": "CIVIL",
  "zoneId": "Asia/Tokyo"
}
```
## `/nearby-airports`
Returns nearest airports to **required parameters**:`latitude`,`longitude`.

| Parameter     | Datatype                                                       | Required | Default | Description                |
|:--------------|:---------------------------------------------------------------|:--------:|:-------:|:---------------------------| 
| `latitude`    | double                                                         | **Yes**  |    -    | valid latitude coordinate  |
| `longitude`   | double                                                         |   **Yes**    |    -    | valid longitude coordinate |

### Request:
```bash
  curl --locationEntity "${VOYAGER_API_URL}/nearby-airports?latitude=21.64547&longitude=-157.9225&limit=2&type=civil" \
--header "X-API-KEY: ${VOYAGER_API_KEY}"
```
### Response:
```json
[
  {
    "iata": "HDH",
    "name": "Kawaihapai Airfield",
    "city": "Mokuleia",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.6,
    "longitude": -158.2,
    "type": "CIVIL",
    "zoneId": "Pacific/Honolulu",
    "distance": 29.12739744834094
  },
  {
    "iata": "HNL",
    "name": "Daniel K Inouye International Airport",
    "city": "Honolulu",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.31782,
    "longitude": -157.92023,
    "type": "CIVIL",
    "zoneId": "Pacific/Honolulu",
    "distance": 36.43377481925254
  }
]
```

<h1><img src="/src/main/resources/images/logo.svg" width="30"> Voyager API</h1>
<h3>REST services for Voyager project</h3>
A personal project I took on to relearn full-cycle development, and to better organize my travel wish list. Built entirely on open-sourced data. Manages caching, authorization tokens, request limits to external APIs.

## API endpoints
[/search](#search)
<br>[/locations](#locations)
<br>[/locations/{id}](#locations/{id})

<br>[/airports](#airports)
<br>[/airports/{iata}](#airportsiata)
<br>[/nearby-airports](#nearby-airports)

<br>[/routes]()
<br>[/routes/{id}]()
<br>[/path/{origin}/to/{destination}]()



## Required Headers
Calls to all endpoints require an authorized API key in the request headers. See details below. Process to request an authorized key coming soon.

| Header    | Datatype | Required | Description                          |
|-----------|----------|-------|--------------------------------------|
| 'X-API-KEY' | string   |      yes | pre-approved API key to enable usage |

## /search
Fetches locations returned by search query. Due to resource cost and data volume, the interface, [*SearchLocationService*](src/main/java/org/voyager/service/SearchLocationService.java), has three separate implementations built on external APIs with caching to manage request limits. The first is powered by <a href="https://www.geonames.org/" target="_blank" rel="nofollow noreferrer noopener">GeoNames</a>, with a designated username, the second is powered by <a href="https://nominatim.org/" target="_blank" rel="nofollow noreferrer noopener">Nominatim</a>, and the third is powered by <a href="https://photon.komoot.io/" target="_blank" rel="nofollow noreferrer noopener">Photon</a>. <b>Coming Soon</b>: implementation built on GeoNames <a href="https://download.geonames.org/export/dump/" target="_blank" rel="nofollow noreferrer noopener">daily data export</a>.

<h3>GET Request</h3>
Calling GET with this endpoint requires a query as a parameter. The query is used to find all matching place names. Adding more terms like country or provincial names will better narrow the search.

Example request:

<code>curl --locationEntity 'http://localhost:3000/search?q=Laie+Hawaii' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Parameter | Datatype |      Required      | Description |
|-----------|-----|:------------------:|---|
| q         | string  |        yes         | query used to match locations
| skipRowCount  | integer | no<br>*Default: 0* | number of rows to skip in search (for pagination)     

<h3>GET Response</h3>
Calling GET with this endpoint returns a JSON string of two fields: the total count of results matching the search query, and the first or next ten results starting from the number of skipped rows in the GET request. Coming Soon: limit parameter to increase the maximum number of fetched results.

Example response:

```json
{
  "resultCount": 1,
  "results": [
    {
      "name": "Lā‘ie",
      "adminName": "Hawaii",
      "countryCode": "US",
      "countryName": "United States",
      "southBound": 21.63541603088379,
      "westBound": -157.93331909179688,
      "northBound": 21.655527114868164,
      "eastBound": -157.91168212890625,
      "longitude": -157.92250061035156,
      "latitude": 21.645469665527344,
      "type": "city, village,..."
    }
  ]
}
```

Full Response JSON Schema
```json
{
  "title": "Search Response",
  "description": "The response schema for a GET request to /serach",
  "type": "object",
  "properties": {
    "resultCount" : {
      "description": "The total result count matching requested query",
      "type": "integer"
    },
    "results": {
      "description": "An array of at maximum ten results starting from the requested skip row count",
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": {
            "description": "The place name in the English-US language",
            "type": "string"
          },
          "adminName": {
            "description": "Name of the administrative governing body, such as the state or province",
            "type": "string"
          },
          "countryCode": {
            "description": "The ISO3166 country code alpha-2",
            "type": "string"
          },
          "countryName": {
            "description": "The country name in the English-US language",
            "type": "string"
          },
          "southBound": {
            "description": "The south bound of a bounding box to view the place in a map",
            "type": "double"
          },
          "westBound": {
            "description": "The west bound of a bounding box to view the place in a map",
            "type": "double"
          },
          "northBound": {
            "description": "The north bound of a bounding box to view the place in a map",
            "type": "double"
          },
          "eastBound": {
            "description": "The east bound of a bounding box to view the place in a map",
            "type": "double"
          },
          "latitude": {
            "description": "The latitude coordinate value",
            "type": "double"
          },
          "longitude": {
            "description": "The longitude coordinate value",
            "type": "double"
          },
          "type": {
            "description": "A place descriptor by the provided API",
            "type": "string"
          }
        },
        "required": [ "name", "countryCode", "countryName","southBound","westBound","northBound","eastBound","latitude","type"]
      }
    }
  },
  "required": [ "resultCount", "results" ]
}
```

## /airports
Fetches confirmed active military and civil airports with optional query parameters for filtering. Built on raw csv file pulled daily from public <a href="https://github.com/mborsetti/airportsdata" target="_blank" rel="nofollow noreferrer noopener">airportsdata GitHub</a> and Wikipedia list of active and upcoming <a href="https://en.wikipedia.org/wiki/List_of_Delta_Air_Lines_destinations" target="_blank" rel="nofollow noreferrer noopener">Delta Air Lines airports</a> ingested into PostgreSQL. Ingestion triggers cache invalidation for API to serve updated resources. Ingestion script found in voyager-datasync module of <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>Voyager Commons GitHub</a>.

<h3>Request</h3>
Example GET command:

<code>curl --locationEntity 'http://localhost:3000/airports?countryCode=br&airline=deltaEntity' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Parameter   | Datatype        | Required | Description |
|-------------|-----------------|:---------|---|
| countryCode | 2-letter string | no       | ISO 3166-1 alpha-2 country code |
| airline | string          | no       | name of airline <br><i>currently only accepts </i>'deltaEntity'

<h3>Response</h3>
Example GET response:

```json
[
  {
    "iata": "GIG",
    "name": "Galeao - Antonio Carlos Jobim International AirportEntity",
    "city": "Rio De Janeiro",
    "subdivision": "Rio de Janeiro",
    "countryCode": "BR",
    "latitude": -22.81,
    "longitude": -43.25056,
    "type": "CIVIL"
  },
  {
    "iata": "GRU",
    "name": "Guarulhos - Governador Andre Franco Montoro International AirportEntity",
    "city": "Sao Paulo",
    "subdivision": "São Paulo",
    "countryCode": "BR",
    "latitude": -23.43556,
    "longitude": -46.47306,
    "type": "CIVIL"
  }
]
```

## /airports/{iata}
Fetches airportEntity by IATA code.

<h3>Request</h3>
Example GET command:

<code>curl --locationEntity 'http://localhost:3000/airports/itm' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

<h3>Response</h3>
Example GET response:

```json
{
  "iata": "ITM",
  "name": "Osaka International AirportEntity",
  "city": "Osaka",
  "subdivision": "Hyogo",
  "countryCode": "JP",
  "latitude": 34.7855,
  "longitude": 135.438,
  "type": "CIVIL"
}
```

## /nearby-airports
Fetches nearest airports to a given point using the <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine</a> formula. Built on raw csv file pulled daily from <a href="https://github.com/mborsetti/airportsdata" target="_blank" rel="nofollow noreferrer noopener">airportsdata</a> and ingested into PostgreSQL. Ingestion triggers cache invalidation for API to serve updated resources. Ingestion script found in voyager-datasync module of <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>Voyager Commons GitHub</a>.

<h3>Request</h3>
Example GET command:

<code>curl --locationEntity 'http://localhost:3000/nearby-airports?latitude=21.64547&longitude=-157.9225&limit=5&airline=deltaEntity' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Parameter | Datatype |      Required       | Description |
|-----------|----------|:-------------------:|---|
| latitude  | double   |         yes         | latitude for start point
| longitude | double   |         yes         | longitude for start point  
| limit     | integer  | no<br><i>Default: 5 | limits nearest airports to return
| type      | string   | no<br> | filters airports by type<br><i>accepts</i>: 'civil', 'military', 'historical', 'other'
| airline   | string   | no<br> | filters airports by airline<br><i>currently only accepts </i>'deltaEntity'

<h3>Response</h3>
Example GET response:

```json
[
  {
    "iata": "HNL",
    "name": "Daniel K Inouye International AirportEntity",
    "city": "Honolulu",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.31782,
    "longitude": -157.92023,
    "type": "CIVIL",
    "distance": 36.43377481925254
  },
  {
    "iata": "LIH",
    "name": "Lihue AirportEntity",
    "city": "Lihue",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.97598,
    "longitude": -159.33896,
    "type": "CIVIL",
    "distance": 150.7752511179687
  },
  {
    "iata": "OGG",
    "name": "Kahului AirportEntity",
    "city": "Kahului",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 20.89865,
    "longitude": -156.43047,
    "type": "CIVIL",
    "distance": 175.4918635521313
  },
  {
    "iata": "KOA",
    "name": "Ellison Onizuka Kona International At Keahole AirportEntity",
    "city": "Kailua/Kona",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 19.7,
    "longitude": -156.0,
    "type": "CIVIL",
    "distance": 294.6087548064909
  },
  {
    "iata": "SFO",
    "name": "San Francisco International AirportEntity",
    "city": "San Francisco",
    "subdivision": "California",
    "countryCode": "US",
    "latitude": 37.61881,
    "longitude": -122.37542,
    "type": "CIVIL",
    "distance": 3835.5769311912377
  }
]
```

## Project Repos
- Voyager UI <a href='https://github.com/maxinefonua/voyager-ui' target='_blank' rel='noopener noreferrer nofollow'>GitHub</a>
  - mapped requests and web feature functions
  - dynamic page injection
- Voyager Commons <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>GitHub</a>
  - an SDK for API services
  - scripts and jars for syncing data

<h3>Back-End Tech Stack:</h3>
- Spring
- PostgreSQL
- GeoNames
- OpenStreetMap
- Lombok
- Java Code Coverage (coming soon)
- IntelliJ

<h3>External API and Data</h3>
- GeoNames Full Text Search (Location Lookup)
- Airport data from csv download at: https://github.com/mborsetti/airportsdata
- Open Currency Exchange
- Open-Meteo Weather

Full README and LICENSE coming soon.

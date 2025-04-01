<h1><img src="/src/main/resources/images/logo.svg" width="30"> Voyager API</h1>
<h3>REST services for Voyager project</h3>
A personal project I took on to relearn full-cycle development, and to better organize my travel wish list. Built entirely on open-sourced data. Manages caching, authorization tokens, request limits to external APIs.

## Required Headers
Calls to all endpoints require an authorized API key in the request headers. See details below. Process to request an authorized key coming soon.

| Header    | Datatype | Required | Description                          |
|-----------|----------|-------|--------------------------------------|
| 'X-API-KEY' | string   |      yes | pre-approved API key to enable usage |

## API endpoints
[/search](#search)
<br>[/airports](#airports)
<br>[/airports/{iata}](#airports/{iata})
<br>[/nearby-airports](#nearby-airports)
### Coming Soon
[/locations](#locations)
<br>[/locations/{id}](#locations/{id})
<br>[/locations/{id}/weather](#locations/{id}/weather)


## /search
Fetches locations returned by search query. Due to resource cost and data volume, the service is built on a <a href="https://www.geonames.org/export/geonames-search.html" target="_blank" rel="nofollow noreferrer noopener">GeoNames external API</a> with caching to manage request limits. Coming soon: a new version built on GeoNames <a href="https://download.geonames.org/export/dump/" target="_blank" rel="nofollow noreferrer noopener">daily data export</a>.

<h3>Request</h3>
Example GET command:

<code>curl --location 'http://localhost:3000/search?q=Laie+Hawaii' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Parameter | Datatype | Required | Description |
|-----------|-----|:----|---|
| q         | string  | yes  | query used to match locations
| skipRowCount  | integer | no  | number of rows to skip in search (for pagination)<br><b>default value is 0     

<h3>Response</h3>
Example GET response:

```json
{
  "resultCount": 1,
  "results": [
    {
      "timezone": {
        "gmtOffset": -10,
        "timeZoneId": "Pacific/Honolulu",
        "dstOffset": -10
      },
      "geonameId": 5850027,
      "name": "Lā‘ie",
      "population": 6138,
      "countryId": "6252001",
      "countryCode": "US",
      "countryName": "United States",
      "continentCode": "NA",
      "adminName1": "Hawaii",
      "lng": -157.9225,
      "lat": 21.64547,
      "fcl": "P",
      "fclName": "city, village,...",
      "bbox": {
        "east": -157.91168,
        "south": 21.635416,
        "north": 21.655527,
        "west": -157.93332
      }
    }
  ]
}
```

## /airports
Fetches all airports, optionally filtered by country. Built on raw csv file pulled daily from <a href="https://github.com/mborsetti/airportsdata" target="_blank" rel="nofollow noreferrer noopener">airportsdata</a> and ingested into PostgreSQL. Ingestion triggers cache invalidation for API to serve updated resources. Ingestion script found in voyager-datasync module of <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>Voyager Commons GitHub</a>.

<h3>Request</h3>
Example GET command:

<code>curl --location 'http://localhost:3000/airports?countryCode=to' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Parameter   | Datatype        | Required | Description |
|-------------|-----------------|:---------|---|
| countryCode | 2-letter string | no       | ISO 3166-1 alpha-2 country code

<h3>Response</h3>
Example GET response:

```json
{
  "resultCount": 6,
  "results": [
    {
      "name": "Kaufana Airport",
      "iata": "EUA",
      "city": "Eua Island",
      "subdivision": "ʻEua",
      "countryCode": "TO",
      "latitude": -21.3783,
      "longitude": -174.958
    },
    {
      "name": "Lifuka Island Airport",
      "iata": "HPA",
      "city": "Lifuka",
      "subdivision": "Ha‘apai",
      "countryCode": "TO",
      "latitude": -19.777,
      "longitude": -174.341
    },
    {
      "name": "Mata'aho Airport",
      "iata": "NFO",
      "city": "Angaha",
      "subdivision": "Vava‘u",
      "countryCode": "TO",
      "latitude": -15.5708,
      "longitude": -175.633
    },
    {
      "name": "Kuini Lavenia Airport",
      "iata": "NTT",
      "city": "Niuatoputapu",
      "subdivision": "Niuas",
      "countryCode": "TO",
      "latitude": -15.97734,
      "longitude": -173.79103
    },
    {
      "name": "Fua'amotu International Airport",
      "iata": "TBU",
      "city": "Nuku'alofa",
      "subdivision": "Tongatapu",
      "countryCode": "TO",
      "latitude": -21.2412,
      "longitude": -175.15
    },
    {
      "name": "Vava'u International Airport",
      "iata": "VAV",
      "city": "Vava'u Island",
      "subdivision": "Vava‘u",
      "countryCode": "TO",
      "latitude": -18.5853,
      "longitude": -173.962
    }
  ]
}
```

## /airports/{iata}
Fetches airport by IATA code.

<h3>Request</h3>
Example GET command:

<code>curl --location 'http://localhost:3000/airports/itm' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

<h3>Response</h3>
Example GET response:

```json
{
  "name": "Osaka International Airport",
  "iata": "ITM",
  "city": "Osaka",
  "subdivision": "Hyogo",
  "countryCode": "JP",
  "latitude": 34.7855,
  "longitude": 135.438
}
```

## /nearby-airports
Fetches nearest airports to a given point using the <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine</a> formula. Built on raw csv file pulled daily from <a href="https://github.com/mborsetti/airportsdata" target="_blank" rel="nofollow noreferrer noopener">airportsdata</a> and ingested into PostgreSQL. Ingestion triggers cache invalidation for API to serve updated resources. Ingestion script found in voyager-datasync module of <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>Voyager Commons GitHub</a>.

<h3>Request</h3>
Example GET command:

<code>curl --location 'http://localhost:3000/nearby-airports?latitude=-36.84853&longitude=174.76349&limit=2' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Parameter | Datatype | Required | Description |
|-----------|----------|:---------|---|
| latitude  | double   | yes      | latitude for start point
| longitude | double   | yes      | longitude for start point  
| limit     | integer  | no       | limits count of nearest airports; default value is 5  

<h3>Response</h3>
Example GET response:

```json
{
  "resultCount": 2,
  "results": [
    {
      "name": "Auckland International Airport",
      "iata": "AKL",
      "city": "Auckland",
      "subdivision": "Auckland",
      "countryCode": "NZ",
      "latitude": -37.0081,
      "longitude": 174.792
    },
    {
      "name": "Ardmore Airport",
      "iata": "AMZ",
      "city": "Manurewa",
      "subdivision": "Auckland",
      "countryCode": "NZ",
      "latitude": -37.0297,
      "longitude": 174.973
    }
  ]
}
```

## Project Repos
- Voyager UI <a href='https://github.com/maxinefonua/voyager-ui' target='_blank' rel='noopener noreferrer nofollow'>GitHub</a>
  - request mapping for web domain
  - dynamic page injection
- Voyager Commons <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>GitHub</a>
  - shared models and utils
  - scripts for data syncing

<h4>Built using:</h4>
- Spring Boot
- Lombok
- Java Code Coverage (coming soon)
- IntelliJ
- PostgreSQL

<h4>External API and Data</h4>
- GeoNames Full Text Search (Location Lookup)
- Airport data from csv download at: https://github.com/mborsetti/airportsdata
- Open Currency Exchange
- Open-Meteo Weather

Full README and LICENSE coming soon.
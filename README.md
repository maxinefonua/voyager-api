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
<br>[/nearby-airports](#nearby-airports)
<br>[/airports](#airports)
### Coming Soon
[/airports](#airports)
<br>[/airports/{iata}](#airports/{iata})
<br>[/airports/{iata}/weather](#airports/{iata}/weather)
<br>[/locations](#locations)
<br>[/locations/{id}](#locations/{id})
<br>[/locations/{id}/weather](#locations/{id}/weather)


## /search
Fetches locations returned by search query. Due to resource cost and data volume, the service is built on a <a href="https://www.geonames.org/export/geonames-search.html" target="_blank" rel="nofollow noreferrer noopener">GeoNames external API</a> with caching to manage request limits. Coming soon: a new version built on GeoNames <a href="https://download.geonames.org/export/dump/" target="_blank" rel="nofollow noreferrer noopener">daily data export</a>.

<h3>Request</h3>
Example GET command:

<code>curl --location 'http://localhost:3000/search?searchText=Laie+Hawaii' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Header    | Datatype | Required | Description                          |
|-----------|----------|-------|--------------------------------------|
| 'X-API-KEY' | string   |      yes | pre-approved API key to enable usage |

| Parameter | Datatype | Required | Description |
|----|-----|:----|---|
| searchText | string  | yes  | query used to match to locations
| startRow | integer | no  | number of rows to skip in search (for pagination); default value is 0      

<h3>Response</h3>
Example GET response:

```json
{
    "totalResultsCount": 1,
    "geonames": [
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
Fetches all airports with an IATA code. Built on raw csv file pulled daily from <a href="https://github.com/mborsetti/airportsdata" target="_blank" rel="nofollow noreferrer noopener">airportsdata</a> and ingested into PostgreSQL. Ingestion triggers cache invalidation for API to serve updated resources. Ingestion script found in voyager-datasync module of <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>Voyager Commons GitHub</a>.

<h3>Request</h3>
Example GET command:

<code>curl --location 'http://localhost:3000/nearby-airports?latitude=21.64547&longitude=-157.9225&limit=5' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Parameter | Datatype | Required | Description |
|-----------|----------|:---------|---|
| latitude  | double   | yes      | latitude for start point
| longitude | double   | yes      | longitude for start point  
| limit     | integer  | no       | limits count of nearest airports; default value is 5  

<h3>Response</h3>
Example GET response:

```json
[
  {
    "name": "Wheeler Army Air Field",
    "iata": "HHI",
    "city": "Wahiawa",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.48144,
    "longitude": -158.03783,
    "distance": 21.79254500887767
  },
  {
    "name": "Kaneohe Bay Mcas (Marion E Carl Field) Airport",
    "iata": "NGF",
    "city": "Kaneohe",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.45046,
    "longitude": -157.76794,
    "distance": 26.939296012634017
  },
  {
    "name": "Kawaihapai Airfield",
    "iata": "HDH",
    "city": "Mokuleia",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.6,
    "longitude": -158.2,
    "distance": 29.12739744834094
  },
  {
    "name": "Daniel K Inouye International Airport",
    "iata": "HNL",
    "city": "Honolulu",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.31782,
    "longitude": -157.92023,
    "distance": 36.43377481925254
  },
  {
    "name": "Kalaeloa (John Rodgers Field) Airport",
    "iata": "JRF",
    "city": "Kapolei",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.30735,
    "longitude": -158.0703,
    "distance": 40.588700464938626
  }
]
```

## /nearby-airports
Fetches nearest airports to a given point using the <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine</a> formula. Built on raw csv file pulled daily from <a href="https://github.com/mborsetti/airportsdata" target="_blank" rel="nofollow noreferrer noopener">airportsdata</a> and ingested into PostgreSQL. Ingestion triggers cache invalidation for API to serve updated resources. Ingestion script found in voyager-datasync module of <a href='https://github.com/maxinefonua/voyager-commons' target='_blank' rel='noopener noreferrer nofollow'>Voyager Commons GitHub</a>.

<h3>Request</h3>
Example GET command:

<code>curl --location 'http://localhost:3000/nearby-airports?latitude=21.64547&longitude=-157.9225&limit=5' \
--header 'X-API-KEY: {DEV_API_KEY}'</code>

| Header    | Datatype | Required | Description                          |
|-----------|----------|-------|--------------------------------------|
| 'X-API-KEY' | string   |      yes | pre-approved API key to enable usage |

| Parameter | Datatype | Required | Description |
|-----------|----------|:---------|---|
| latitude  | double   | yes      | latitude for start point
| longitude | double   | yes      | longitude for start point  
| limit     | integer  | no       | limits count of nearest airports; default value is 5  

<h3>Response</h3>
Example GET response:

```json
[
  {
    "name": "Wheeler Army Air Field",
    "iata": "HHI",
    "city": "Wahiawa",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.48144,
    "longitude": -158.03783,
    "distance": 21.79254500887767
  },
  {
    "name": "Kaneohe Bay Mcas (Marion E Carl Field) Airport",
    "iata": "NGF",
    "city": "Kaneohe",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.45046,
    "longitude": -157.76794,
    "distance": 26.939296012634017
  },
  {
    "name": "Kawaihapai Airfield",
    "iata": "HDH",
    "city": "Mokuleia",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.6,
    "longitude": -158.2,
    "distance": 29.12739744834094
  },
  {
    "name": "Daniel K Inouye International Airport",
    "iata": "HNL",
    "city": "Honolulu",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.31782,
    "longitude": -157.92023,
    "distance": 36.43377481925254
  },
  {
    "name": "Kalaeloa (John Rodgers Field) Airport",
    "iata": "JRF",
    "city": "Kapolei",
    "subdivision": "Hawaii",
    "countryCode": "US",
    "latitude": 21.30735,
    "longitude": -158.0703,
    "distance": 40.588700464938626
  }
]
```


## /locations

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
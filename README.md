# ✈️ Flight Tracker (OpenSky Network API)

Kotlin + Jetpack Compose sample implementing Clean Architecture and MVVM.

## Features

- Live flight tracking on Google Maps (OpenSky Network API)
- Edge-to-edge full-screen map with custom plane markers (rotated by trueTrack)
- Select area by zoom; only visible bounds are queried
- Auto refresh every 10s when camera is idle, manual refresh button
- Country filter via bottom sheet (Map Layers button)
- Zoom controls (bottom-right), refresh + layers (top-right)

## Architecture

Two-module layout focused on clarity and reuse:

```
app/
├─ data/
│  ├─ remote/
│  │  ├─ api/            # Retrofit interfaces (OpenSkyApi, OpenSkyAuthApi)
│  │  ├─ dto/            # FlightDto, FlightStateDto, OAuth2TokenDto
│  │  └─ adapter/        # FlightDtoAdapter (array → object)
│  ├─ repository/        # FlightRepositoryImpl (in-memory cache)
│  └─ di/                # NetworkModule, RepositoryModule
├─ domain/
│  ├─ model/             # Flight, MapBounds
│  ├─ repository/        # FlightRepository (suspend)
│  └─ usecase/           # GetFlightsUseCase (Flow<ApiResult>), Filter, Countries
└─ presentation/
   └─ map/               # FlightMapScreen, FlightMapViewModel, FlightMapState

core/
├─ model/                # ApiResult (Success, Error(code/message), Exception)
├─ json/                 # ArrayMappingAdapter + Factory (Moshi)
├─ constants/            # AppConstants (Refresh/Display/Cache)
└─ ui/components/        # LoadingIndicator, ErrorCard, etc.
```

Key decisions:
- Repository API stays simple: `suspend fun ...: ApiResult<T>`
- UseCases expose `Flow<ApiResult<T>>` for retry/catch composition
- ViewModel manages UI state (StateFlow) and rate-limiting/debounce/auto-refresh

## Tech Stack

- Jetpack Compose + Material3
- Hilt (DI)
- Coroutines + Flow
- Retrofit + Moshi (with a generic array adapter factory)
- Google Maps Compose
- KSP (annotation processing)

## Setup

This project loads secrets from one of (in order): Gradle -P props → ENV vars → `local.properties`.

Provide at least `MAPS_API_KEY`. For higher OpenSky limits, add OAuth2 client credentials.

Options:
- Gradle properties:
  ```bash
  ./gradlew assembleDebug \
    -PMAPS_API_KEY=YOUR_KEY \
    -POPENSKY_CLIENT_ID=your_client_id \
    -POPENSKY_CLIENT_SECRET=your_client_secret
  ```
- Environment variables (Linux/macOS):
  ```bash
  export MAPS_API_KEY=YOUR_KEY
  export OPENSKY_CLIENT_ID=your_client_id
  export OPENSKY_CLIENT_SECRET=your_client_secret
  ./gradlew assembleDebug
  ```
- local.properties (not committed; see `local.properties.example`):
  ```properties
  MAPS_API_KEY=YOUR_KEY
  OPENSKY_CLIENT_ID=your_client_id
  OPENSKY_CLIENT_SECRET=your_client_secret
  ```

## Build & Run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

If you share a prebuilt APK, the Maps key is embedded at build time; end users can run the APK without a local.properties.

## Networking & Parsing

- OAuth2 (client credentials) supported; falls back to Basic/anonymous if not configured
- OkHttp logging: BODY in debug, NONE in release
- Moshi factory maps array-like payloads to DTOs (`core/json/ArrayMappingAdapterFactory`)

## Error Handling

`ApiResult<T>`
- `Success(data)`
- `Error(code, message)` for API errors
- `Exception(throwable)` for unexpected failures

ViewModel uses a single `formatError(code, message, throwable)` to produce user-friendly messages (401/429/timeout/no-internet), and shows an ErrorCard with a Retry action.

## Performance

- Max 200 markers to keep rendering smooth in dense areas
- Debounce + min request interval to protect API
- 10s in-memory cache in repository
- Manual refresh bypasses cache and rate-limits

## Testing

```bash
./gradlew testDebugUnitTest
```

Included examples:
- `FilterFlightsByCountryUseCaseTest`
- `GetFlightsUseCaseTest` (fake repository success/error)

## Security & Secrets

- `local.properties` is ignored by VCS; commit `local.properties.example` only
- For CI (GitHub Actions), create `local.properties` from repository secrets or pass `-P` values
- If you restrict your Maps key to Android apps, ensure package `com.alihiz.filterflightscase` and the signing SHA‑1 match the APK being shipped

## Minimum Requirements

- minSdk 26, targetSdk 34
- Kotlin 1.9.x, Gradle 8.7

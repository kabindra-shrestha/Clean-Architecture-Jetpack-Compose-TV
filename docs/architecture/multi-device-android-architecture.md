# Multi-device Android Architecture

This document describes the target architecture for evolving the current Android TV-only IPTV app
into a unified Android platform that supports Android TV and Android Mobile while preserving clean
architecture, shared ViewModels, and shared business logic.

## Current Baseline

The repository currently has:

- `:core:common`: pure Kotlin shared primitives for app result and serializable error models.
- `:domain`: pure Kotlin entities, repository contracts, and use cases.
- `:data`: Android data library containing DTOs, Room, remote/local data sources, repository
  implementations, fake data sources, Ktor error parsing, and data Koin modules.
- `:feature:presentation`: shared Android presentation library containing ViewModels, screen states,
  events, and use-case/ViewModel Koin modules reused by TV and mobile.
- `:app-tv`: Android TV application shell mapped to the existing `app/` directory, containing TV
  Compose UI, TV navigation, socket service, receivers, workers, and TV platform DI.
- `:app-mobile`: Android Mobile application shell with Material 3 mobile login, dashboard, Live TV,
  and movie list surfaces backed by shared ViewModels.
- `:player`: reusable player library with Media3/Compose support.
- Passing compile baselines: `./gradlew :app-tv:compileDebugKotlin :app-mobile:compileDebugKotlin`.

Important current boundaries to fix during migration:

- Shared presentation components and screens import `androidx.tv.material3`, so they cannot be
  reused directly by mobile.
- DI is centralized in the app module, which makes module ownership and dependency direction harder
  to enforce.

Pre-extraction hardening already completed:

- Domain source has been moved from `:app` into `:domain`.
- Shared result/error primitives have been moved from `:app` into `:core:common`.
- Data source has been moved from `:app` into `:data`.
- Shared ViewModels/states/events have been moved from `:app` into `:feature:presentation`.
- The current TV shell is exposed as `:app-tv`, and a separate `:app-mobile` shell has been added.
- Login input is now represented as the domain model `LoginCredentials` instead of a data-layer
  request.
- `MovieXtreamRepository.getMovieDetail()` and `MovieDetailXtreamUseCase` now return domain
  `MovieDetail` instead of `MovieDetailDTO`.
- Shared media/player ViewModels now depend on domain `CurrentUserRepository` instead of the
  data-layer credentials provider contract.
- `DashboardViewModel` now depends on domain `NotificationStreamRepository`, and foreground intent
  payloads use domain `AlertPayload`.

## Architecture Principles

1. App modules are thin shells.
    - They own manifests, activities, platform startup, device-specific navigation hosts, app-level
      Koin startup, permissions, services, workers, and receivers.

2. Domain is pure Kotlin.
    - Domain owns entities, repository interfaces, use cases, and business rules.
    - Domain must not depend on Android, Room, Ktor, DTOs, Compose, navigation libraries, app
      modules, or data implementation classes.

3. Data implements domain contracts.
    - Data owns DTOs, Room entities, DAOs, remote/local data sources, mappers, repository
      implementations, and Ktor/Xtream integration.
    - Data maps DTOs and database entities into domain models before crossing into domain or
      presentation.

4. Feature presentation is shared.
    - ViewModels, state, events/actions, and one-off effects are shared between TV and mobile when
      behavior is the same.
    - ViewModels depend on use cases and small interfaces, not UI, activities, services, navigation
      controllers, or Android `Context`.

5. UI is device-specific by default.
    - TV UI owns focus, D-pad behavior, TV Material components, overscan-safe spacing, and
      remote-first navigation.
    - Mobile UI owns Material 3 mobile patterns, touch-first flows, phone/tablet adaptive layouts,
      bottom bars, lists, sheets, and window-size handling.
    - Shared composables are allowed only when they do not import TV-only or mobile-only material
      APIs.

6. Dependency direction is one way.
    - App -> feature UI -> feature presentation -> domain.
    - Data -> domain.
    - Core modules can be depended on by domain/data/features where appropriate.
    - Domain never points outward.

## Recommended Module Structure

```text
.
├── app-mobile
├── app-tv
├── core
│   ├── common
│   ├── model
│   ├── network
│   ├── database
│   ├── datastore
│   ├── navigation
│   ├── designsystem
│   ├── designsystem-mobile
│   └── designsystem-tv
├── domain
├── data
├── feature
│   ├── auth
│   │   ├── presentation
│   │   ├── ui-mobile
│   │   └── ui-tv
│   ├── dashboard
│   │   ├── presentation
│   │   ├── ui-mobile
│   │   └── ui-tv
│   ├── livetv
│   │   ├── presentation
│   │   ├── ui-mobile
│   │   └── ui-tv
│   └── movie
│       ├── presentation
│       ├── ui-mobile
│       └── ui-tv
└── player
```

Use this graph as the long-term target:

```text
app-tv
  -> feature:*:ui-tv
  -> core:designsystem-tv
  -> data

app-mobile
  -> feature:*:ui-mobile
  -> core:designsystem-mobile
  -> data

feature:*:ui-tv
  -> feature:*:presentation
  -> core:designsystem-tv
  -> core:navigation
  -> player

feature:*:ui-mobile
  -> feature:*:presentation
  -> core:designsystem-mobile
  -> core:navigation
  -> player

feature:*:presentation
  -> domain
  -> core:common
  -> core:navigation

data
  -> domain
  -> core:network
  -> core:database
  -> core:datastore
  -> core:common

domain
  -> core:model
  -> core:common
```

## Module Responsibilities

### `core:common`

Owns small platform-neutral primitives:

- `AppResult` or current `Result`
- error models that are not tied to Ktor
- dispatchers abstraction
- time providers
- simple extensions
- coroutine helpers

Do not put Compose components, icons, Android `Context`, or TV/mobile dependencies here.

### `core:model`

Owns cross-layer immutable models if you want entities outside `domain`. For this app, either keep
entities in `domain` or move stable shared models here. Do not mix DTOs or Room entities into this
module.

### `core:network`

Owns Ktor client creation, serializers, logging, timeout policy, and request configuration.

Use a small interface for device headers:

```kotlin
interface DeviceInfoProvider {
    val userAgent: String
    val deviceId: String
    val platform: String
    val version: String
    val brand: String
    val model: String
    val appVersion: String
    val appVersionCode: String
}
```

The Android implementation can live in `core:network` if this remains Android-only, or in
`core:platform-android` if preparing for Compose Multiplatform.

### `core:database`

Owns Room database, entities, DAOs, migrations, converters, and database DI.

The current `allowMainThreadQueries()` should be removed before production. Keep schema exports and
add migration tests.

### `core:datastore`

Owns preferences/token/session persistence. Prefer this for lightweight session values and feature
flags. Keep Room for structured media caches.

### `core:navigation`

Owns route contracts, not UI navigation implementation:

```kotlin
sealed interface AppRoute {
    data object Splash : AppRoute
    data object Login : AppRoute
    data object Dashboard : AppRoute
    data object LiveTvPlayer : AppRoute
    data class MovieDetail(val movieId: String) : AppRoute
    data class MoviePlayer(val movieId: String) : AppRoute
}

sealed interface UiEffect {
    data class Navigate(val route: AppRoute) : UiEffect
    data object NavigateUp : UiEffect
}
```

TV and mobile can map the same `AppRoute` to different graph implementations.

### `core:designsystem`

Owns tokens and platform-neutral composables:

- colors
- typography contracts
- spacing
- shape tokens
- loading/error/empty state contracts
- image loading wrappers that do not assume TV focus or mobile touch

### `core:designsystem-tv`

Owns TV-specific components:

- D-pad focus containers
- focus restoration helpers
- TV cards/buttons/tabs
- overscan-safe layout primitives
- TV Material wrappers

### `core:designsystem-mobile`

Owns mobile-specific components:

- Material 3 scaffold patterns
- top app bars, bottom navigation, navigation rail/adaptive layout helpers
- mobile cards/buttons/text fields
- phone/tablet window size behavior

### `domain`

Owns:

- entities: `User`, `LiveTV`, `Movie`, `MovieDetail`, `NotificationMessage`
- repository interfaces
- use cases
- domain commands such as `LoginCredentials`
- sync result models such as `MediaSyncSummary`

Already fixed before module extraction:

- Login credentials use the domain model `LoginCredentials`.
- Xtream movie-detail repositories/use cases return `MovieDetail`, not `MovieDetailDTO`.
- Current-user access is exposed through domain `CurrentUserRepository`.

Fix during module extraction:

- Move all DTO-to-domain mapping behind data repositories.

### `data`

Owns:

- repository implementations
- remote data sources
- local data sources
- DTOs and mappers
- Room entity mappers
- Xtream/Ktor integrations
- `UserCredentialsProviderImpl`

Expose only domain interfaces to presentation.

### `feature:*:presentation`

Owns:

- ViewModel
- screen state
- UI event/action
- one-off effect
- presentation mappers when needed

Example:

```kotlin
data class MovieDetailState(
    val isLoading: Boolean = false,
    val movie: VODDetail? = null,
    val errorMessage: String? = null,
)

sealed interface MovieDetailAction {
    data class Load(val movieId: String) : MovieDetailAction
    data object Play : MovieDetailAction
}
```

The shared ViewModel should not know whether it is being rendered on TV or mobile.

### `feature:*:ui-tv`

Owns:

- TV Compose screens
- D-pad focus requesters/restoration
- `androidx.tv.material3`
- TV navigation graph entries
- TV-specific screen layout

### `feature:*:ui-mobile`

Owns:

- mobile Compose screens
- touch interactions
- adaptive phone/tablet layouts
- `androidx.compose.material3`
- mobile navigation graph entries

## Gradle Setup Strategy

Keep the version catalog and add convention plugins once the first module split lands. Recommended
convention plugins:

```text
build-logic
├── convention
│   ├── KabindraAndroidApplicationConventionPlugin
│   ├── KabindraAndroidLibraryConventionPlugin
│   ├── KabindraAndroidComposeConventionPlugin
│   ├── KabindraKotlinLibraryConventionPlugin
│   ├── KabindraRoomConventionPlugin
│   └── KabindraKoinConventionPlugin
```

Example dependencies:

```kotlin
// feature/movie/presentation/build.gradle.kts
plugins {
    id("kabindra.android.library")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
```

```kotlin
// feature/movie/ui-tv/build.gradle.kts
plugins {
    id("kabindra.android.library")
    id("kabindra.android.compose")
}

dependencies {
    implementation(project(":feature:movie:presentation"))
    implementation(project(":core:designsystem-tv"))
    implementation(project(":core:navigation"))
    implementation(project(":player"))
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.tv.foundation)
}
```

```kotlin
// feature/movie/ui-mobile/build.gradle.kts
plugins {
    id("kabindra.android.library")
    id("kabindra.android.compose")
}

dependencies {
    implementation(project(":feature:movie:presentation"))
    implementation(project(":core:designsystem-mobile"))
    implementation(project(":core:navigation"))
    implementation(project(":player"))
    implementation(libs.androidx.material3)
}
```

Prefer:

- `implementation` by default.
- `api` only for types intentionally exposed by a module contract.
- no dependency from `domain` to `data`, `feature`, `app`, Android, Room, Ktor, or Compose.
- no dependency from shared presentation to TV/mobile UI modules.

Align Java/Kotlin toolchains across modules. The app currently uses Java 21 while `:player` uses
Java 17. Pick one target intentionally, preferably Java 21 if all build environments support it.

## Dependency Injection Strategy

Split Koin into module-owned definitions:

```text
core:network          networkModule
core:database         databaseModule
data                  repositoryModule, dataSourceModule
domain                useCaseModule
feature:auth          authPresentationModule
feature:dashboard     dashboardPresentationModule
feature:livetv        liveTvPresentationModule
feature:movie         moviePresentationModule
app-tv                tvPlatformModule, tvAppModule
app-mobile            mobilePlatformModule, mobileAppModule
```

App shells compose them:

```kotlin
startKoin {
    androidContext(this@TvApplication)
    modules(
        networkModule,
        databaseModule,
        dataSourceModule,
        repositoryModule,
        useCaseModule,
        authPresentationModule,
        dashboardPresentationModule,
        liveTvPresentationModule,
        moviePresentationModule,
        tvPlatformModule,
    )
}
```

Register platform-specific implementations only in app modules. For example, TV can provide a
foreground-service-backed notification stream, while mobile can provide a no-op implementation or a
mobile notification implementation.

## Navigation Strategy

Use shared route contracts and device-specific graph renderers.

TV:

- Use a TV navigation host optimized for remote navigation.
- Keep focus restoration per destination.
- Prefer stable route keys over raw string routes.
- Avoid mobile back-stack patterns that fight D-pad behavior.

Mobile:

- Use Material 3 mobile navigation patterns.
- Bottom navigation for major sections on phones.
- Navigation rail/list-detail for tablets.
- Use adaptive layouts based on window size classes.

ViewModels emit effects, not navigation calls:

```kotlin
private val _effects = MutableSharedFlow<UiEffect>()
val effects = _effects.asSharedFlow()

fun onMovieClicked(movieId: String) {
    viewModelScope.launch {
        _effects.emit(UiEffect.Navigate(AppRoute.MovieDetail(movieId)))
    }
}
```

The TV and mobile screen binders collect the same effect and navigate through their own controller.

## Shared ViewModel Strategy

Use one shared ViewModel when:

- data loading is identical
- validation and error handling are identical
- screen state is device-neutral
- user actions represent intent, not UI mechanics

Split ViewModels only when:

- the product behavior genuinely differs
- platform lifecycle behavior is fundamentally different
- the ViewModel needs a platform capability that cannot be represented by a small interface

Remaining refactor before moving presentation modules:

- Move the TV-specific `SocketNotificationStreamRepository` binding into the future `app-tv`
  platform module.

## State Management Improvements

Keep the existing MVI-like state direction, but tighten contracts:

```text
UiAction -> ViewModel -> UseCase -> Repository -> StateFlow<UiState>
                                -> SharedFlow<UiEffect>
```

Recommended per screen:

- `State`: persistent renderable state.
- `Action` or `Event`: user/system inputs.
- `Effect`: one-time work such as navigation, snackbar, toast, permission request.

Avoid in state:

- `Context`
- resource ids
- Compose `MutableState`
- navigation controllers
- focus requesters
- platform activity/service payloads

Prefer immutable collections or stable models for large lists to reduce unnecessary recomposition.

## Package Organization

Use package names that mirror ownership:

```text
com.kabindra.iptv.core.common
com.kabindra.iptv.core.network
com.kabindra.iptv.core.database
com.kabindra.iptv.core.navigation
com.kabindra.iptv.domain.model
com.kabindra.iptv.domain.repository
com.kabindra.iptv.domain.usecase
com.kabindra.iptv.data.remote
com.kabindra.iptv.data.local
com.kabindra.iptv.data.mapper
com.kabindra.iptv.data.repository
com.kabindra.iptv.feature.movie.presentation
com.kabindra.iptv.feature.movie.ui.mobile
com.kabindra.iptv.feature.movie.ui.tv
com.kabindra.iptv.app.mobile
com.kabindra.iptv.app.tv
```

The current `com.kabindra.tv.iptv` package can be kept temporarily during migration to reduce churn,
then renamed after modules are stable.

## Reusable UI Component Strategy

Extract in this order:

1. Design tokens: colors, typography, spacing, dimensions.
2. Stateless primitives with no TV/mobile dependency.
3. Shared media cards only if focus/touch behavior is injected as slots or modifiers.
4. TV wrappers around TV Material.
5. Mobile wrappers around Material 3.

Example shape:

```kotlin
@Composable
fun MediaPoster(
    title: String,
    posterUrl: String,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
)
```

Then TV/mobile wrap it:

- TV adds focus border, scale, D-pad click, `CompactCard`.
- Mobile adds ripple, touch click, long press if needed, Material 3 card treatment.

Do not put `androidx.tv.material3` imports in shared UI modules.

## Testing Strategy

Add tests by layer:

- Domain: pure unit tests for use cases and business rules.
- Data: repository tests with fake remote/local data sources.
- Network: Ktor client tests with mock engine.
- Database: Room migration tests and DAO tests.
- ViewModel: coroutine tests plus Turbine for state/effect flows.
- TV UI: Compose UI tests for D-pad focus, selected item, remote click, overlay behavior.
- Mobile UI: Compose UI tests for touch navigation, adaptive layout, empty/error/loading states.
- Screenshot tests: key TV and mobile screens at representative sizes.

CI should run:

```bash
./gradlew :domain:test
./gradlew :data:testDebugUnitTest
./gradlew :feature:movie:presentation:testDebugUnitTest
./gradlew :app-tv:compileDebugKotlin
./gradlew :app-mobile:compileDebugKotlin
```

## Migration Plan

### Phase 0: Stabilize the TV Baseline

- Keep the current `:app` building.
- Add CI for `:app-tv:compileDebugKotlin` and `:app-mobile:compileDebugKotlin`.
- Remove hardcoded credentials and host values from DI. Move them to build config, encrypted local
  properties, remote config, or secure user/session storage.
- Remove `allowMainThreadQueries()` before release.

### Phase 1: Extract Pure Domain

- Create `:domain`.
- Move entities, repository interfaces, use cases, and domain commands.
- Keep the existing `LoginCredentials` and Xtream `MovieDetail` domain contracts as the first files
  moved.
- Keep package names temporarily if needed.
- Compile `:domain` without Android, Room, Ktor, or Compose.

### Phase 2: Extract Core Common and Data

- Create `:core:common`, `:core:network`, `:core:database`, and `:data`.
- Move `Result`, error models, and pure utilities to `core:common`.
- Move Ktor setup to `core:network`.
- Move Room setup to `core:database`.
- Move repository implementations, DTOs, data sources, and mappers to `:data`.
- App still renders the existing TV UI, but now depends on shared data/domain modules.

### Phase 3: Extract Shared Feature Presentation

- Create `feature:*:presentation` modules.
- Move ViewModels, state, actions, and effects out of app.
- Introduce interfaces for platform behavior:
    - `NotificationStream`
    - `CurrentUserRepository` or `GetCurrentUserUseCase`
    - `MediaSyncCoordinator` if sync orchestration grows
- Replace app/service/activity references in ViewModels.

### Phase 4: Rename Existing App to `app-tv`

- Move the current app shell and TV UI into `:app-tv` and `feature:*:ui-tv`.
- Keep current TV behavior unchanged while the module graph changes.
- Verify D-pad focus, player controls, notification overlay, service startup, boot receiver, and
  worker behavior.

### Phase 5: Add `app-mobile`

- Add mobile manifest, activity, application, Koin startup, and navigation host.
- Reuse the same feature presentation modules.
- Implement mobile UI modules using Material 3.
- Start with login, dashboard, movie browsing/detail/player, then Live TV.

### Phase 6: Harden and Optimize

- Add module boundary checks.
- Add unit/UI/screenshot tests.
- Add baseline profiles for startup and critical navigation.
- Add release variants and signing for TV/mobile separately.
- Prepare optional Compose Multiplatform migration by keeping domain/core pure and isolating
  Android-only implementations.

## Performance Considerations

- Use stable keys in lazy lists and grids.
- Keep large media lists paged or locally cached.
- Avoid rebuilding player instances across recompositions.
- Use `collectAsStateWithLifecycle()` in UI modules.
- Use `stateIn`/`shareIn` carefully to avoid duplicate cache sync work.
- Use immutable state models and avoid exposing mutable lists.
- Prefetch images differently for TV rows and mobile grids.
- Keep TV focus state local to TV UI, not shared ViewModels.
- Use baseline profiles for cold start, dashboard, media browsing, and player launch.

## Pitfalls To Avoid

- Big-bang moving every package at once. Split layer by layer and keep the TV app compiling after
  each phase.
- Letting shared ViewModels reference `Activity`, `Service`, `Context`, `NavController`, or TV focus
  APIs.
- Returning DTOs from domain repositories.
- Placing TV Material components in shared UI modules.
- Duplicating ViewModels just because UI differs.
- Using raw route strings across modules.
- Registering the same Koin binding from multiple app/device modules.
- Sharing mobile touch assumptions with TV UI, or sharing TV focus assumptions with mobile UI.
- Letting app modules become data/service owners again after extraction.

## Target Outcome

After migration:

- TV and mobile ship as separate app modules.
- Domain, data, use cases, repositories, models, mappers, networking, caching, and shared ViewModels
  are reused.
- Only UI composition, navigation graph binding, focus/touch behavior, app shell, and platform
  services differ by device.
- The architecture remains compatible with future Compose Multiplatform migration because pure
  layers and Android-specific adapters are isolated.

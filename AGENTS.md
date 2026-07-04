# SimpleBudget - Project Instructions

## Technology

This project uses Kotlin and Compose Multiplatform.

Each `expect` variable/function/class should have an `actual` representation in Android, iOS, and desktop — even if it's just a mock.

## Module Structure

- `shared` — App entry point, depends on `core:components`, `feature:*`
- `androidApp` — Android entry point, depends on `shared`
- `desktopApp` — Desktop entry point, depends on `shared`, used for hot-reload
- `iosApp` — iOS entry point, imports `SharedUI` framework from `shared`
- `core:utils` — Preview annotations, Log wrapper, extensions
- `core:resources` — Compose Multiplatform resources (strings, fonts, drawables). **Must be included in every module** that uses `Res` (strings, fonts, etc.) — feature modules, core:components, shared. Uses `publicResClass = true`.
- `core:components` — Theme, base Compose UI components
- `core:domain` — Repository interfaces, domain models. No implementation details. Depends on nothing except `kotlinx.coroutines`.
- `core:data` — Room database, DAOs, entities, repository implementations. Depends on `core:domain` and `core:utils` (for `AppScope`). Uses `@ContributesBinding` to bind repo implementations.
- `feature:y` — singular screens / specific functionality (e.g., `feature:home`). Depends only on `core:domain` for repo interfaces.

Each feature module uses this directory structure:
```
feature:home/
  src/commonMain/kotlin/prayit/simplebudget/feature/home/
    state/          # ViewModel, sealed state interface, data classes
      HomeState.kt
      HomeViewModel.kt
    ui/             # Screen composable, Content composable, sub-content composables
      Home.kt
```

- `state/` — separate files for state (`XxxState.kt`) and ViewModel (`XxxViewModel.kt`)
- `ui/` — `XxxScreen` (connects ViewModel to content), `XxxContent` (pure rendering), sub-content composables

`core:data` directory structure:
```
core:data/
  src/commonMain/kotlin/prayit/simplebudget/core/data/
    dbSetup/        # AppDatabase, DatabaseFactory (expect/actual)
    dao/            # Room @Dao interfaces
    entity/         # Room @Entity data classes
    repository/     # Repository implementations (@ContributesBinding)
```

- `dbSetup/` — `AppDatabase` (with `@ConstructedBy`), `DatabaseFactory` (`expect/actual` for platform-specific builders). DAOs and entities are in separate packages.
- `dao/` — Room DAO interfaces. Must import their entity types.
- `entity/` — Room entity data classes.
- `repository/` — implementations bound to `core:domain` interfaces via `@ContributesBinding`.

Feature modules require `alias(libs.plugins.metro)` in their `plugins {}` block for `@Inject` on ViewModels.

New modules **must** be added to `settings.gradle.kts` in `moduleList`:

```kotlin
val moduleList = listOf(
    ":shared",
    ":androidApp",
    ":desktopApp",
    ":core:utils",
    ":core:resources",
    ":core:components",
    ":feature:home",
    // ...
)
```

And to `shared/build.gradle.kts` in `sourceSets`:

```kotlin
sourceSets {
    commonMain.dependencies {
        api(project(":core:utils"))
        api(project(":core:resources"))
        api(project(":core:components"))
        api(project(":feature:home"))
        // ...
    }
}
```

## Dependency Graph

```
androidApp → shared → feature:home → core:domain
                       shared → core:data → core:domain
desktopApp → shared → feature:home → core:domain
                       shared → core:data → core:domain
iosApp     → shared → feature:home → core:domain (via framework)
                       shared → core:data → core:domain
```

Features depend only on `core:domain` (repo interfaces). `shared` depends on `core:data` (repo implementations) and wires DI via `@ContributesBinding`.

## Conventions

- Package: `prayit.simplebudget.core.*` for core modules
- Package: `prayit.simplebudget` for shared module
- Use `@AllDevicePreviews` etc. from `core:utils` for previews — **required** for each screen, each subcomponent of that screen, and each base component
- AGP 9.2 — use `android {}` not `androidLibrary {}` in KMP library modules. `androidLibrary {}` is deprecated since AGP 9.1.
- **No hardcoded SDK values** — use `project.property("ANDROID_COMPILE_SDK")` and `project.property("ANDROID_MIN_SDK")` from `gradle.properties`
- **CMP resources workaround** — every KMP library module using CMP resources needs `androidResources.enable = true` inside `android {}` block (CMP-9547). Only `core:resources` and `shared` have `compose.multiplatform` plugin — `core:components` must NOT have it.
- **Optional composable parameters** — default to `null`, not `{}`. Avoids composing empty blocks on every recomposition.
- **Icons** — use Lucide icons (`com.composables:icons-lucide-cmp`) everywhere. Import from `com.composables.icons.lucide`. Access via `Lucide.IconName` (e.g., `Lucide.Church`, `Lucide.User`, `Lucide.BookOpen`). Provided by `core:components` via `api`.
- **Strings** — each feature's string group should be separated by a blank newline in `strings.xml` for readability. Use `feature_context_description` naming (e.g., `home_parishes_title`, `login_button`).
- **Accessibility** — every composable with a `contentDescription` parameter must have a string. If there's already a meaningful string (label, title, etc.), use that. Otherwise, use `Res.string.missing_accessibility` as a placeholder until a proper string is added.
- **`onBack`** — every screen receives `onBack: () -> Unit = {}` as a parameter
- **Previews** — use `@PhonePreviews` and `@TabletPreviews` from `core:utils`. `@AllDevicePreviews` for components that appear everywhere. Content composables are previewed directly (no ViewModel wrapper needed).
- **ViewModel state** — use `MutableStateFlow` sealed interface pattern, never `mutableStateOf`. Each ViewModel exposes `val state: StateFlow<XxxState>` collected via `collectAsState()` in Screen composables. Always use `_state.update { }` (not `_state.value =`) for atomic updates.

```kotlin
class HomeViewModel @Inject constructor() {
    private val _state = MutableStateFlow<XxxState>(XxxState.Loading)
    val state: StateFlow<XxxState> = _state.asStateFlow()

    fun onTabSelected(index: Int) {
        _state.update { current ->
            (current as? XxxState.Content)?.copy(selectedTab = index) ?: current
        }
    }
}
```

## Screens — BaseScreen

**Every screen** must use `BaseScreen` from `core:components`. It wraps `Scaffold` in both branches (compact and expanded) so IME/system-bar insets are always handled. Never build Scaffold or set up padding manually in a screen.

**FloatingActionButton** — use `AppFloatingActionButton` from `core:components`, never raw `FloatingActionButton`. It switches between regular and Large on expanded screens automatically.

### Screen / Content Split

Every screen must be split into two composables:

1. **`XxxScreen`** — connects ViewModel to content. Receives ViewModel as parameter, collects state, passes it down. Used in production (NavHost).
2. **`XxxContent`** — pure rendering function. Receives state as plain parameters, no ViewModel. Trivially previewable.

**Why:** Content composables are pure functions with no runtime dependencies. They can be previewed with any state (loading, error, empty, content) without DI, fakes, or ViewModel wiring. This makes previews reliable and useful — you can see all UI states without running the app.

Content composables should have **default parameters** for ease of previewing — previews can omit all parameters and just render:

```kotlin
@Composable
fun HomeContent(
    state: HomeState = HomeState.Content(),  // default state
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,  // default device
    onTabSelected: (Int) -> Unit = {},  // empty callbacks
    onParishEntryClick: (id: String) -> Unit = {},
) { ... }

// Preview — no parameters needed
@PhonePreviews
@Composable
fun HomeContentPreview() {
    MParafiaTheme {
        HomeContent()
    }
}
```

```kotlin
// 1. Screen composable — thin, connects ViewModel to content
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onParishEntryClick: (id: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    HomeContent(
        state = state,
        deviceClass = deviceClass,
        onTabSelected = viewModel::onTabSelected,
        onParishEntryClick = onParishEntryClick,
    )
}

// 2. Content composable — pure rendering, no ViewModel, trivially previewable
@Composable
fun HomeContent(
    state: HomeState,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onTabSelected: (Int) -> Unit = {},
    onParishEntryClick: (id: String) -> Unit = {},
) {
    // pure UI, no dependencies
}

// Preview — no ViewModel, no DI, just pass state
@PhonePreviews
@Composable
fun HomeContentPreview() {
    HomeContent(
        state = HomeState.Content(selectedTab = 0),
        deviceClass = DeviceClass.PhonePortrait,
    )
}
```

Tabbed screen — pass items:
```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onParishEntryClick: (id: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    HomeContent(
        state = state,
        deviceClass = deviceClass,
        onTabSelected = viewModel::onTabSelected,
        onParishEntryClick = onParishEntryClick,
    )
}

@Composable
fun HomeContent(
    state: HomeState,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onTabSelected: (Int) -> Unit = {},
    onParishEntryClick: (id: String) -> Unit = {},
) {
    val selectedTab = (state as? HomeState.Content)?.selectedTab ?: 0

    val items = listOf(
        NavigationItem(label = { Text("Parishes") }, selected = selectedTab == 0) { onTabSelected(0) },
        NavigationItem(label = { Text("Bible") },    selected = selectedTab == 1) { onTabSelected(1) },
        NavigationItem(label = { Text("Profile") },  selected = selectedTab == 2) { onTabSelected(2) },
    )

    BaseScreen(deviceClass = deviceClass, items = items) { modifier ->
        when (selectedTab) {
            0 -> ParishesContent(modifier)
            1 -> BibleContent(modifier)
            2 -> ProfileContent(modifier)
        }
    }
}
```

Non-tabbed screen — pass empty list:
```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onGuest: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    LoginContent(
        state = state,
        deviceClass = deviceClass,
        onGuest = {
            viewModel.loginAsGuest()
            onGuest()
        },
    )
}

@Composable
fun LoginContent(
    state: LoginState,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onGuest: () -> Unit = {},
) {
    BaseScreen(deviceClass = deviceClass, items = emptyList()) { modifier ->
        Column(modifier.fillMaxSize()) {
            // screen content — modifier already has Scaffold padding
        }
    }
}
```

## Spacing & Shapes

### Spacing

Spacing uses a `@Immutable data class` via CompositionLocal — **never hardcode `X.dp` for padding/margin**.

Access via:
```kotlin
val spacing = LocalAppSpacing.current
Modifier.padding(spacing.md)
```

Scale: `none(0)` → `xxs(2)` → `xs(4)` → `sm(8)` → `md(16)` → `lg(24)` → `xl(32)` → `xxl(48)` → `xxxl(64)`

Defined in `core:components/.../theme/Spacing.kt`. Provided by `AppTheme` automatically.

### Shapes

Shapes are centralized in `core:components/.../theme/Shapes.kt`. Use via `MaterialTheme.shapes`:

| Level | Value |
|-------|-------|
| extraSmall | 4.dp |
| small | 8.dp |
| medium | 12.dp |
| large | 16.dp |
| extraLarge | 24.dp |

## Gradle Variables

Defined in `gradle.properties` — **never hardcode** `compileSdk` or `minSdk` in build files:

```properties
ANDROID_COMPILE_SDK=37
ANDROID_MIN_SDK=23
```

Use in `build.gradle.kts`:
```kotlin
compileSdk = project.property("ANDROID_COMPILE_SDK").toString().toInt()
minSdk = project.property("ANDROID_MIN_SDK").toString().toInt()
```

## Adaptive Layouts

`DeviceClass` enum in `core:utils` determines layout variant:

| Value | Condition |
|-------|-----------|
| `PhonePortrait` | Width < 600dp (compact) |
| `TabletPortrait` | 600dp ≤ width < 840dp (medium) |
| `TabletLandscape` | 840dp ≤ width < 1200dp (expanded) |
| `LargeTabletDesktop` | width ≥ 1200dp (expanded) |

`BaseScreen` handles the switch automatically. For screen-specific adaptive logic (e.g., showing a side panel), use `deviceClass.isCompact`, `.isMedium`, or `.isExpanded`.

Obtained via `rememberDeviceClass()` in `shared/navigation/DeviceClass.kt` (uses CMP adaptive library).

## Desktop

Desktop is a **dev-only** target used for hot-reload during development.

- Spawns 2 windows: phone (400×850dp) and tablet (1024×768dp)
- Has dark/light theme toggle shared between both windows via `mutableStateOf`
- Entry point: `desktopApp/src/main/kotlin/main.kt`

## Coil (Image Loading)

Coil 3.x with Ktor for network image loading. Add `implementation(libs.coil)` and `implementation(libs.coil.network.ktor)` to a feature module's `commonMain` dependencies. Use `AsyncImage` from `coil3.compose`:

```kotlin
AsyncImage(
    model = "https://example.com/image.jpg",
    contentDescription = stringResource(Res.string.some_title),
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.medium),
)
```

### Function parameter ordering

Parameters must be declared in this order:

```kotlin
fun FriendsList(
    // Mandatory parameters
    currentUser: User,

    // Mandatory lambda parameters (including Compose slots)
    filterFriend: (User) -> Boolean,
    befriending: @Composable (User) -> Unit,

    // Varargs
    vararg friends: User,

    // Optional lambda parameters
    key: (User) -> Any = { it },

    // Optional parameters
    maxNumberOfItems: Int = 10,

    // Main lambda / Compose content
    content: @Composable () -> Unit,
)
```

## DI (Metro)

- `AppScope` is defined in `core:utils` (`prayit.simplebudget.di.AppScope`)
- `@DependencyGraph` and `Graph.app` live in `shared` (`prayit.simplebudget.di.AppGraph`)
- ViewModels use `@Inject` on constructor (no `@ContributesTo` — that's only for interfaces/binding containers)
- ViewModels are exposed as properties in `AppGraph` and accessed via `Graph.app.xxxViewModel`
- Use `@SingleIn(AppScope::class)` for singletons
- Use `@ContributesBinding(AppScope::class)` when binding implementation to interface
- Feature modules need `alias(libs.plugins.metro)` in their `plugins {}` block

`AppGraph` interface shape:

```kotlin
@DependencyGraph(AppScope::class)
interface AppGraph {
    val httpClient: HttpClient
    val loginViewModel: LoginViewModel
    val homeViewModel: HomeViewModel
    // ...
}
```

Accessed via `Graph.app.xxxViewModel` in `NavHost`.

## Navigation

Uses Navigation 3 (`androidx.navigation3`) with `@Serializable` data class routes.

- **Routes** — defined in `shared/navigation/NavigationRoutes.kt`. New route types must be added to `navConfig` polymorphic serializer.
- **NavHost** — lives in `shared/NavHost.kt`. Wires ViewModels from `Graph.app.xxxViewModel`, passes `deviceClass` and `onBack` callbacks.
- **`rememberDeviceClass()`** — in `shared/navigation/DeviceClass.kt`. Uses CMP adaptive library to resolve `DeviceClass` from `WindowAdaptiveInfo`.
- **`NavigationItem`** — data class in `core:components/navigation/BaseScreen.kt`.

## Previews

`core:components` provides `api(libs.compose.ui.tooling.preview)` — the `@Preview` annotation propagates transitively to all downstream modules.

Each module with `@Preview` functions only needs in `androidMain`:

```kotlin
androidMain.dependencies {
    implementation(libs.compose.ui.tooling) // Android Studio renderer
}
```

> Note: `ui.tooling.preview` is provided by `core:components` via `api` (transitive). `ui.tooling` must be added by each module individually in `androidMain` — it is Android-only and NOT transitive.

Without it, previews throw `ClassNotFoundException: ComposeViewAdapter`.

Use `@AllDevicePreviews` (from `core:utils`) instead of bare `@Preview` — **required** for each screen, each subcomponent, and each base component.

Preview wrappers must wrap content in `MParafiaTheme { ... }` to provide `LocalAppSpacing`, `LocalThemeIsDark`, and `MaterialTheme`:

```kotlin
@PhonePreviews
@Composable
fun XxxContentPreview() {
    MParafiaTheme {
        XxxContent()
    }
}
```

## Data / Domain

Repository interfaces live in `core:domain`. Implementations live in `core:data`. Features depend only on `core:domain`.

```
feature:* → core:domain ← core:data → shared (DI wiring)
```

- **`core:domain`** — repo interfaces, domain models. No Room, no Ktor, no implementation details.
- **`core:data`** — Room database, DAOs, entities, repo implementations. Uses `@ContributesBinding(AppScope::class)` to bind implementations to interfaces.
- **`shared`** — provides `AppDatabase` and DAO instances via `DatabaseBindings` (`@BindingContainer`). Room plugins (room, ksp) are configured in `core:data`, NOT in `shared`.

New repo pattern:

```kotlin
// core:domain/.../repository/ParishRepository.kt
interface ParishRepository {
    fun getParishes(): Flow<List<Parish>>
    suspend fun getParish(id: String): Parish?
}

// core:data/.../repository/ParishRepositoryImpl.kt
@ContributesBinding(AppScope::class)
@Inject
class ParishRepositoryImpl(
    private val parishDao: ParishDao,
) : ParishRepository { ... }

// shared/.../di/DatabaseBindings.kt
@ContributesTo(AppScope::class)
@BindingContainer
object DatabaseBindings {
    @Provides @SingleIn(AppScope::class)
    fun provideDatabase(): AppDatabase = createDatabase()

    @Provides @SingleIn(AppScope::class)
    fun provideParishDao(database: AppDatabase): ParishDao = database.parishDao()
}
```

Room uses `expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>` — Android takes `Context` via `AppContext.instance`, JVM/iOS use file path. `createDatabase()` in common adds `BundledSQLiteDriver` and `Dispatchers.IO`.

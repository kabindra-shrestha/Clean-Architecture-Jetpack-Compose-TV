# Skill: Navigation

## When to apply
Defining new screens or handling screen transitions.

## Rules
- Use `navigation3` library.
- Define routes as `object` or `data class` ending in `Route`.
- Centralize route handling in `MainScreen.kt` using `NavDisplay`.
- Use `backStack.add(Route)` to navigate and `backStack.removeLastOrNull()` for back.
- Pass navigation logic to screens via lambda callbacks (e.g., `onNavigateDetail: (String) -> Unit`).

## Example
```kotlin
// Screens.kt
@Serializable
data class DetailRoute(val id: String)

// MainScreen.kt
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider {
        entry<DetailRoute> { route ->
            DetailScreen(id = route.id, onBack = { backStack.removeLastOrNull() })
        }
    }
)
```

## Never do
- Hardcode navigation logic deep inside nested Composables.
- Use string-based routes manually; rely on serializable classes/objects.

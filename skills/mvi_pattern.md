# Skill: MVI Pattern

## When to apply
Creating or modifying interactive screens and ViewModels.

## Rules
- `UiState`: A single data class representing the entire screen state. Default values for initial state.
- `Event`: A sealed class/object for user actions (e.g., `RefreshData`, `OnItemClick`).
- `ViewModel`:
  - Exposes state via `StateFlow` (e.g., `uiState`).
  - Handles actions in a single `onEvent(event: ScreenEvent)` function.
  - Uses `_uiState.update { ... }` or `.value = ...` to mutate state.
- `UI`: Collects state using `collectAsStateWithLifecycle()` in Compose.

## Example
```kotlin
data class MyUiState(val isLoading: Boolean = false)

sealed class MyEvent {
    object LoadData : MyEvent()
}

class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: MyEvent) {
        when (event) {
            MyEvent.LoadData -> { /* handle */ }
        }
    }
}
```

## Never do
- Expose multiple `MutableStateFlow`s for different state pieces.
- Perform navigation directly inside the ViewModel (use callbacks).

# Skill: Module Structure

## When to apply
Adding new code or organizing existing logic across the project.

## Rules
- `:core:common`: Shared utilities, constants, and base classes (e.g., `Result`, `ErrorResponse`).
- `:domain`: Pure Kotlin business logic. Entities, Repository interfaces, and UseCases. No Android dependencies.
- `:data`: Data implementations. Networking (Ktor), Database (Room), and Repository implementations.
- `:feature:presentation`: Shared UI components, ViewModels, and state logic.
- `:app-tv` / `:app-mobile`: Entry points, platform-specific UI, and DI initialization.
- `:player`: Unified video player component.

## Example
```kotlin
// domain/src/main/java/.../domain/repository/MyRepository.kt
interface MyRepository { ... }

// data/src/main/java/.../data/repository/MyRepositoryImpl.kt
class MyRepositoryImpl(...) : MyRepository { ... }
```

## Never do
- Reference Android APIs in the `:domain` module.
- Put API DTOs in the `:domain` module (use Entities instead).

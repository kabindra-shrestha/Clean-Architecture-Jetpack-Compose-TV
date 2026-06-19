# Skill: Repository & UseCase

## When to apply
Implementing business logic or data access.

## Rules
- Repository interfaces live in `:domain/repository`.
- UseCase classes live in `:domain/usecase` and typically wrap one or more repositories.
- Repository implementations live in `:data/repository`.
- Map DTOs to Entities in the Repository layer before passing data to the Domain.
- Use `Flow` for real-time updates (e.g., observing Room database).

## Example
```kotlin
// domain/repository/LiveTVRepository.kt
interface LiveTVRepository {
    suspend fun getCategories(): List<LiveTVCategory>
}

// domain/usecase/LiveTVRoomUseCase.kt
class LiveTVRoomUseCase(private val repo: LiveTVRepository) {
    suspend fun getCategories() = repo.getCategories()
}
```

## Never do
- Expose Room `@Entity` or Ktor DTO classes in the Domain layer.
- Leak implementation details (like Ktor client) into UseCases.

# Skill: Networking (Ktor & Result)

## When to apply
Implementing API calls or handling remote data.

## Rules
- Use Ktor `HttpClient` with `ContentNegotiation` and `Serialization`.
- Return a `Flow<Result<T>>` from UseCases/Repositories for operations.
- `Result` states: `Initial`, `Loading`, `Success<T>`, `Error(ErrorResponse)`.
- Use `ignoreUnknownKeys = true` in Json configuration.
- IPTV API (Xtream) base URLs are built dynamically with username/password params.

## Example
```kotlin
suspend fun getData(): List<DataDTO> = client.get("endpoint").body()

// UseCase flow
fun execute(): Flow<Result<Data>> = flow {
    emit(Result.Loading)
    try {
        val data = repo.getData()
        emit(Result.Success(data))
    } catch (e: Exception) {
        emit(Result.Error(ErrorResponse(message = e.message)))
    }
}
```

## Never do
- Use `Result` from the Kotlin standard library (use the project's custom `Result`).
- Perform network calls directly in the ViewModel (use UseCases/Repositories).

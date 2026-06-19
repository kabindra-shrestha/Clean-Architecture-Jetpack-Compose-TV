# Skill: DI Setup (Koin)

## When to apply
Adding new dependencies or registering modules.

## Rules
- Define modules using `val myModule = module { ... }`.
- Naming convention: `[Feature/Layer]Module` (e.g., `dataRepositoryModule`, `presentationViewModelModule`).
- Register modules in an `initKoin` function in the app entry point.
- Use `viewModel { MyViewModel(get(), ...) }` for ViewModels.
- Use `single { MyRepoImpl(...) as MyRepo }` for singleton implementations.

## Example
```kotlin
val presentationViewModelModule = module {
    viewModel { DashboardViewModel(get(), get(), get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(presentationViewModelModule, dataRepositoryModule)
    }
}
```

## Never do
- Use `inject()` or `get()` inside Composables (use `koinViewModel()` or constructor injection).
- Forget to cast implementation to interface when providing repositories.

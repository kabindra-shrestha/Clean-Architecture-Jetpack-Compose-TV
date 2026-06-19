# Skill: Compose UI Conventions

## When to apply
Building or styling UI components.

## Rules
- Use `sdp` (Scalable DP) units for all dimensions (`8.sdp`) to ensure multi-device compatibility.
- Use `ssp` for text sizes (`16.ssp`).
- Prefer project-specific wrapper components: `TextComponent`, `ButtonComponent`, `LoadingIndicator`.
- `BaseLazy`: Use for list/grid logic. It abstracts TV vs. Mobile platform differences.
- Specify `BaseLazyPlatform.Android` for mobile and `AndroidTv` for TV modules.

## Example
```kotlin
TextComponent(
    text = "Hello",
    type = TextType.Title,
    size = TextSize.Medium,
    modifier = Modifier.padding(16.sdp)
)

BaseLazy(
    items = items,
    platform = BaseLazyPlatform.Android,
    layout = BaseLazyLayout.Grid,
    spanCount = 2
) { index, item, modifier ->
    MyItem(item, modifier)
}
```

## Never do
- Use hardcoded `dp` or `sp` values (always use `.sdp` / `.ssp`).
- Duplicate list logic; use `BaseLazy` to ensure correct focus handling on TV.

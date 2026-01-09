# NewsFeed App

NewsFeed application built with modern Android technologies.

## Build

Before the main build, you need to run:

```shell
./gradlew :app:generateDebugProto
```

## Implementation Details

### General

1. **API Configuration**: The `API_KEY` is configured in `app/build.gradle.kts` via `buildConfigField`.
2. **Tech Stack**:
   - Navigation 3
   - Material 3
   - Jetpack Compose
   - Mockk
   - Hilt
   - Ktor
3. **Architecture**: Clean Architecture principles.

### List Screen

1. **State Preservation**: Scroll position is maintained seamlessly across screen rotations.
2. **Content Options**: Supports fetching both 'top-headlines' and 'everything' news categories.
3. **Localization**: Fetches news exclusively from the US region.

### Detail Screen

1. **State Preservation**: WebView content is cached and does not reload upon screen rotation.
2. **Known Issues**:
   - Some web links may not load correctly in the current WebView configuration, resulting in a blank page. In these cases, standard browser consoles usually indicate internal errors with those specific pages.

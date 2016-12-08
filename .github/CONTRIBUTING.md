# Contributing to Rocket.Chat

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

The following is a set of guidelines for contributing to Rocket.Chat and its packages, which are hosted in the [Rocket.Chat Organization](https://github.com/RocketChat) on GitHub.

__Note:__ If there's a feature you'd like, there's a bug you'd like to fix, or you'd just like to get involved please raise an issue and start a conversation. We'll help as much as we can so you can get contributing - although we may not always be able to respond right away :)

## Coding standards

We are following / moving to [Google Java Style](https://google.github.io/styleguide/javaguide.html). There's a nice IntelliJ style file for the project in `config/quality/style-guide` for code formatting. (Note: We'll be hosting the style guide file until it's updated in the official repo)

We are evaluating the Google's IntelliJ (and therefore Android Studio) [plugin](https://plugins.jetbrains.com/plugin/8527) for code formatting.

We acknowledge all the code does not meet these standards but we are working to change this over time.

### Syntax check

Before submitting a PR you should get no errors on `checkstyle`.

Just run:

```
./gradlew checkstyle
```

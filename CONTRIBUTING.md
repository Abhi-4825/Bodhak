# Contributing to Anuviya

First off, thank you for considering contributing to Anuviya! We welcome contributions from everyone and are grateful for your time and effort.

## Getting Started

1. **Fork** the repository on GitHub.
2. **Clone** your fork locally: `git clone https://github.com/YOUR_USERNAME/Anuviya.git`
3. **Create a branch** for your work.

## Branch Strategy

We follow a standard Git Flow-inspired branching model:
- `main`: The stable production branch.
- `develop`: The integration branch for upcoming releases.
- `feature/*`: For new features (branch off `develop`).
- `bugfix/*`: For fixing bugs (branch off `develop` or `main` depending on urgency).

## Coding Standards

- **Java Version:** We use Java 24 features. Please leverage modern syntax.
- **Immutability:** Use `record` types extensively for immutable data models.
- **Interfaces:** Utilize `sealed` interfaces to enforce domain boundaries and exhaustiveness in switch expressions.
- **Documentation:** Write meaningful Javadoc for all public APIs, compiler passes, and complex logic.

## Package Structure

Anuviya is organized into 16 core packages:

| Package | Description |
| :--- | :--- |
| `analyzer` | Core orchestration of the analysis process. |
| `classification` | Component and project type classification logic. |
| `compiler` | The 6-pass compiler pipeline implementation. |
| `context` | Shared context objects passed through the compiler pipeline. |
| `endpoint` | Discovery and analysis of REST/RPC endpoints. |
| `event` | Event bus for decoupling UI and background tasks. |
| `frontend` | Language frontends (JavaParser, Tree-sitter). |
| `infra` | Infrastructure concerns (logging, configuration, file I/O). |
| `ir` | Language-neutral Intermediate Representation models. |
| `metrics` | Calculation engines for complexity, cohesion, etc. |
| `model` | Domain models and data transfer objects. |
| `orchestration` | High-level workflow managers. |
| `platform` | Application lifecycle and dependency injection. |
| `quality` | Engines for detecting smells, hotspots, and god classes. |
| `ui` | JavaFX views, controllers, and RichTextFX components. |
| `workspace` | Session state, persistence, and file watching. |

## Pull Request Process

1. Ensure your code compiles and all tests pass.
2. Update documentation (README, Javadoc) if applicable.
3. Push your branch and open a Pull Request against the `develop` branch.
4. Provide a clear and descriptive PR title and description.

## Code Review Checklist

- [ ] Does the code follow the styling guidelines?
- [ ] Are tests included for new features/bug fixes?
- [ ] Are `record` types used appropriately?
- [ ] Is there proper error handling?
- [ ] Does it maintain the separation of concerns between packages?

## Reporting Issues

If you find a bug or have a feature request, please use the GitHub Issues tracker. Provide as much detail as possible, including steps to reproduce bugs.

Please review our [Code of Conduct](./CODE_OF_CONDUCT.md) before participating.

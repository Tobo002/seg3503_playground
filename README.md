# seg3503_playground

Course workspace for SEG3503. Each subdirectory is a **standalone** project (its own build, `lib/`, and IDE settings).

| Folder | Description |
|--------|-------------|
| `Devoir 1/` | Devoir 1 — ISBN validation (Java + JUnit) |
| `Lab1/` | Lab 1 materials (Java, Elixir, etc.) |

## Working on a project

1. Open the **project folder** in Cursor/VS Code (e.g. `Devoir 1`), or use a multi-root workspace.
2. Build and test from that folder: `./bin/compile`, `./bin/run-tests`.
3. Follow `.cursor/rules/seg3503-java-project.mdc` when adding a new Java project.

Do not configure Java source paths for one project in the repo-root `.vscode/settings.json`.

# SEG3503 — Devoir 1

Projet Java autonome (tests de validation ISBN). Ne dépend d'aucun autre dossier du dépôt.

## Structure

```
Devoir 1/
  src/          code de production (les paquets suivent l'arborescence)
  test/         tests JUnit
  lib/          jar JUnit autonome (versionné)
  dist/         classes compilées (généré)
  bin/compile   compile src + test
  bin/run-tests compile et exécute JUnit
```

## Compilation et tests

À partir de ce répertoire (`Devoir 1`) :

```bash
./bin/compile
./bin/run-tests
```

## IDE

Ouvrez le dossier **`Devoir 1`** (ou ajoutez-le comme racine d'espace de travail) pour que `.vscode/settings.json` soit pris en compte. N'utilisez pas la configuration Java à la racine du dépôt.

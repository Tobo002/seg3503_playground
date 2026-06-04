# SEG3503 - Devoir 1

# Partie 1: appendCheckDigitToISBN12

## 1.1

| Condition d'entrée | Classes valides | Classes invalides | Valeurs aux bornes |
|-------------------|-----------------|-------------------|-------------------|
| Longueur de la chaîne | **CEV1** : chaîne contenant exactement 12 caractères (Heuristique 2 : nombre fixe de valeurs valides) | **CEI1** : moins de 12 caractères <br> **CEI2** : plus de 12 caractères | 11, **12**, 13 caractères |
| Nature des caractères | **CEV2** : tous les caractères sont des chiffres de 0 à 9 (Heuristique 3 : ensemble de valeurs valides) | **CEI3** : au moins un caractère n'est pas un chiffre | Premier caractère non numérique, dernier caractère non numérique |
| Format de la chaîne | **CEV3** : aucun caractère supplémentaire avant ou après les 12 chiffres (Heuristique 4 : condition « doit être ») | **CEI4** : présence de caractères supplémentaires avant ou après les 12 chiffres | Caractère supplémentaire au début ou à la fin |
| Valeur numérique des chiffres | **CEV4** : valeur figurant inclusivement entre `000000000000` et `999999999999` | **CEI5** : chiffres supérieurs à `999999999999` <br> **CEI6** : chiffres inférieurs à `000000000000` | `000000000000` & `999999999999` |


### Justification des classes d'équivalence

#### CEV1 / CEI1 / CEI2 (Longueur)

La spécification exige exactement 12 caractères. Selon l'heuristique 2 :

- 1 classe valide : exactement 12 caractères.

- 2 classes invalides : moins de 12 et plus de 12.


#### CEV2 / CEI3 (Nature des caractères)

La spécification exige uniquement des chiffres. Selon l'heuristique 3 :

- 1 classe valide : caractère appartenant à l'ensemble {0..9}.

- 1 classe invalide : au moins un caractère hors de cet ensemble.


#### CEV3 / CEI4 (Format)

La spécification indique qu'il ne doit pas y avoir de caractères supplémentaires avant ou après les 12 chiffres. Selon l'heuristique 4 :

- 1 classe valide : respecte la condition.

- 1 classe invalide : ne respecte pas la condition.


#### CEV4 / CEI5 / CEI6 (Valeur numérique)

La valeur interprétée comme entier non signé sur 12 chiffres doit être dans l'intervalle [0 ; 999999999999]. Selon l'heuristique 5 (subdivision des bornes) :

- 1 classe valide : **CEV4** (incluant les bornes `000000000000` et `999999999999`).

- **CEI5** : valeur strictement supérieure à `999999999999` (en pratique, toute chaîne de plus de 12 chiffres décimaux ; voir note ci-dessous).

- **CEI6** : valeur strictement inférieure à `000000000000` (p. ex. représentation négative avec signe `-`).


### Valeurs aux bornes retenues

| Condition | Valeur juste en dessous | Valeur à la borne | Valeur juste au-dessus |
|------------|-------------------------|------------------|------------------------|
| Longueur | 11 caractères | 12 caractères | 13 caractères |
| Valeur du nombre | `-000000000001` <br> ou <br> `-00000000001` | `000000000000` <br> & <br> `999999999999` | `100000000000` |

> Pour **CEI5**, les cas de test avec plus de 12 caractères (p. ex. CT5) suffisent : on ne peut pas dépasser `999999999999` sans ajouter un chiffre.


## 1.2

| # du cas de test | Données de test | Résultat attendu | Classes d'équivalence couvertes | Valeurs aux bornes |
|-----------------|----------------|------------------|---------------------------------|-------------------|
| CT1 | `"978061868000"` | Retourne `"9780618680009"` | CEV1, CEV2, CEV3, CEV4 | Longueur = 12 |
| CT2 | `"000000000000"` | Retourne `"0000000000000"` | CEV1, CEV2, CEV4 | Valeur numérique minimale |
| CT3 | `"999999999999"` | Retourne `"9999999999994"` (ISBN-13 valide) | CEV1, CEV2, CEV4 | Valeur numérique maximale |
| CT4 | `"97806186800"` | Exception `IllegalArgumentException` | CEI1 | 11 caractères (borne inférieure) |
| CT5 | `"9780618680009"` | Exception `IllegalArgumentException` | CEI2, CEI5 | 13 caractères (borne supérieure) |
| CT6 | `"97806-18-68-"` | Rejet de l'entrée / erreur | CEI3 | Tirets (caractères non numériques) |
| CT7 | `" 978061868000"` | Rejet de l'entrée / erreur | CEI4 | Caractère supplémentaire avant |
| CT8 | `"978061868000 "` | Rejet de l'entrée / erreur | CEI4 | Caractère supplémentaire après |
| CT9 | `"-00000000001"` | Rejet de l'entrée / erreur | CEI6 | Valeur numérique juste en dessous de 0 (`-00000000001`, 12 caractères) |

-------

# Partie 2: tidyISBN10or13InsertingDashes

Méthode analysée : `ISBNValidate.tidyISBN10or13InsertingDashes(String rawISBN10or13)` (boîte noire).

Approche : **partitionnement en catégories** (cours 3) — sous chaque **paramètre**, plusieurs **catégories** ; chaque catégorie propose des **choix** annotés (`[error]`, `[single]`, `[property …]`, `[if …]`). Les choix `[error]` et `[single]` génèrent un **cadre isolé** (non combinés avec les autres catégories).

## 2.1 — Plan de test (partitionnement en catégories)

### Modèle (notation du cours)

**Paramètre :** `rawISBN10or13`

**Présence de l'entrée :**
- `null` [error]
- chaîne vide `""` [error]
- chaîne non vide [property NonEmpty]

**Longueur utile après `strip` :**
- exactement 10 caractères [property Len10] [if NonEmpty]
- exactement 13 caractères [property Len13] [if NonEmpty]
- autre longueur (p. ex. 9, 14, aucun chiffre extrait) [error] [if NonEmpty]

**Format de la chaîne brute :**
- aucune décoration (chiffres / `X` seuls) [if Len10 or Len13]
- tirets ou espaces intégrés [if Len10 or Len13]
- bruit en tête ou en queue [if Len10 or Len13]

**Validité ISBN-10 :** [if Len10]
- ISBN-10 entièrement valide [property ISBN10OK]
- somme de contrôle invalide [error]
- numéro de groupe invalide [error]
- chiffre de contrôle `X` [single]

**Validité ISBN-13 :** [if Len13]
- préfixe `978` et ISBN valide [property ISBN13_978]
- préfixe `979` et ISBN valide [single]
- préfixe hors {978, 979} [error]
- somme de contrôle invalide [error]

**Environnement :**
- JVM ; classe `ISBNValidate` chargée (table des groupes initialisée) — nominal, non varié.

**Propriétés :** `NonEmpty` ; `Len10` ⊕ `Len13` ; `ISBN10OK` / `ISBN13_978` ⇒ succès (ISBN avec tirets).

---

### Tableau (format du devoir)

| Paramètres / conditions d'environnement | Catégories | Choix | Contraintes |
|----------------------------------------|------------|-------|-------------|
| **`rawISBN10or13`** | Présence de l'entrée | `null` [error] ; `""` [error] ; non vide [property NonEmpty] | [error] → cadre isolé ; autres catégories [if NonEmpty]. |
| **`rawISBN10or13`** | Longueur après `strip` | 10 [property Len10] ; 13 [property Len13] ; autre [error] | [if NonEmpty] ; Len10 ⊕ Len13. |
| **`rawISBN10or13`** | Format brut | sans décoration ; tirets/espaces intégrés ; bruit tête/queue | [if Len10 or Len13]. |
| **`rawISBN10or13`** | Validité ISBN-10 | valide [property ISBN10OK] ; mauvaise somme de contrôle [error] ; mauvais groupe [error] ; contrôle `X` [single] | [if Len10] ; [error]/[single] → cadre isolé. |
| **`rawISBN10or13`** | Validité ISBN-13 | préfixe 978 valide [property ISBN13_978] ; préfixe 979 valide [single] ; préfixe invalide [error] ; mauvaise somme de contrôle [error] | [if Len13] ; [error]/[single] → cadre isolé. |
| **Environnement d'exécution** | Plateforme Java | JVM + bibliothèque chargée | Non combiné aux choix fonctionnels. |

### Règles de génération des cadres

1. **`[error]`** : un cadre par choix, sans combiner les autres catégories.
2. **`[single]`** : un cadre par choix jugé suffisant seul.
3. **Autres choix** : combinaisons ciblées (format × ISBN valide) pour éviter la redondance.

## 2.2 — Cadres de test

| Numéro du cadre de test | Catégorie : choix |
|-------------------------|-------------------|
| **F1** | Présence : `null` [error] |
| **F2** | Présence : `""` [error] |
| **F3** | Longueur : autre — 9 chiffres [error] |
| **F4** | Longueur : autre — 14 chiffres [error] |
| **F5** | Validité ISBN-10 : somme de contrôle invalide [error] |
| **F6** | Validité ISBN-10 : groupe invalide [error] |
| **F7** | Validité ISBN-13 : préfixe invalide [error] |
| **F8** | Validité ISBN-13 : somme de contrôle invalide [error] |
| **F9** | Validité ISBN-10 : contrôle `X` [single] |
| **F10** | Validité ISBN-13 : préfixe `979` valide [single] |
| **F11** | Non vide · Len13 · sans décoration · ISBN13_978 |
| **F12** | Non vide · Len13 · tirets intégrés · ISBN13_978 |
| **F13** | Non vide · Len13 · bruit tête/queue · ISBN13_978 |
| **F14** | Non vide · Len10 · sans décoration · ISBN10OK |
| **F15** | Non vide · Len10 · tirets intégrés · ISBN10OK |

## 2.3 — Correspondance cadres / cas JUnit

| Cadre | Méthode de test | Donnée (borne lorsque applicable) | Résultat attendu |
|-------|-----------------|-----------------------------------|------------------|
| F1 | `f1_nullInput` | `null` | `IllegalArgumentException` |
| F2 | `f2_emptyInput` | `""` | `IllegalArgumentException` |
| F3 | `f3_nineDigitsAfterStrip` | `061868000` | `IllegalArgumentException` |
| F4 | `f4_fourteenDigitsAfterStrip` | `97806186800099` | `IllegalArgumentException` |
| F5 | `f5_isbn10BadCheckDigit` | `0618680000` | `IllegalArgumentException` |
| F6 | `f6_isbn10BadGroup` | `6950000006` | `IllegalArgumentException` |
| F7 | `f7_isbn13InvalidPrefix` | `9770618680009` | `IllegalArgumentException` |
| F8 | `f8_isbn13BadCheckDigit` | `9780618680000` | `IllegalArgumentException` |
| F9 | `f9_isbn10CheckDigitX` | `999019601X` | `99901-9601-X` |
| F10 | `f10_isbn13Prefix979` | `9790618680008` | `979-0-618-68000-8` |
| F11 | `f11_isbn13Plain` | `9780618680009` | `978-0-618-68000-9` |
| F12 | `f12_isbn13WithDashes` | `978-0-618-68000-9` | `978-0-618-68000-9` |
| F13 | `f13_leadingAndTrailingJunk` | ` 9780618680009 ` | `978-0-618-68000-9` |
| F14 | `f14_isbn10Plain` | `0618680004` | `0-618-68000-4` |
| F15 | `f15_isbn10WithDashes` | `0-618-68000-4` | `0-618-68000-4` |

Implémentation : `test/ISBNValidateTest.java`, classe imbriquée `TidyISBN10or13InsertingDashesTest`.

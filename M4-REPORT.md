# TrailLog M4 – Rapport de débogage

Branche : `fix/m4-debugging` (créée depuis `m4-student`).

## Environnement et vérification

- Build : `gradlew.bat clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL**. `app-debug.apk` est généré.
- Tests unitaires : 6/6 réussis (`TrailTest` : 3, `TrailFormatterTest` : 3).
- **Vérification sur émulateur :** AVD `Medium_Phone_API_37.0` (API 37). L'APK `app-debug.apk`, construit depuis
  le commit des corrections, a été installé avec `adb install -r`. L'application a été pilotée avec `adb`
  (`input`, `uiautomator dump`) et Logcat a été lu avec `adb logcat -s TrailLogSave AndroidRuntime:E`.
  Le catalogue importé se charge (Sentier 01 à 09…). Aucun crash `AndroidRuntime` n'a été observé.
  Captures d'écran : `docs/m4/`.
- **Non fait :** la session de debug avec breakpoint dans Android Studio (Step Over, Resume, Variables). Les valeurs
  indiquées pour le Lab 2 viennent de la lecture du code et des tests, pas d'une session de débogueur.
- Note de configuration : le premier build échouait avec `SDK location not found`. J'ai créé un fichier `local.properties`
  (`sdk.dir=C:/Users/Hassan/AppData/Local/Android/Sdk`). Ce fichier est ignoré par git et n'est pas commité.

## Lab 1 – Sauvegarde refusée (dénivelé)

- **Symptôme :** « Mountain Test », 8.0 km et 750 m sont refusés (Snackbar « champs invalides »), alors que le même trail avec 250 m est accepté.
- **Entrée Logcat attendue** (filtre `tag:TrailLogSave`), lue dans le code source :
  `E/TrailLogSave: Save rejected during elevation validation: elevationGainM=750`
- **Cause :** `EditTrailActivity` contenait une règle propre à l'écran :
  `if (elevation > 500) { Log.e("TrailLogSave", ...); Snackbar...; return }`.
  Le modèle (`Trail.isValid()`) et les repositories (`require(trail.isValid())`, import JSON) exigent seulement
  `elevationGainM >= 0` et ne fixent aucune limite haute. La condition `> 500` contredit donc la validation réelle
  et bloque des données valides.
- **Correction :** suppression de ce bloc uniquement. Les contrôles légitimes restent en place : nom vide,
  distance nulle, non finie ou négative, dénivelé nul ou négatif.
- **Fichier modifié :** `app/src/main/java/fr/estiam/traillog/ui/EditTrailActivity.kt`. Test ajouté dans `app/src/test/.../model/TrailTest.kt`.
- **Vérification :**
  - Tests unitaires : 750 m et 250 m sont valides, -1 m est invalide.
  - Émulateur : « Mountain Test », 8.0 km, 750 m est **enregistré** et apparaît dans la liste. Aucune ligne
    `TrailLogSave` dans Logcat. « Mountain Test 250 », 8.0 km, 250 m est aussi enregistré.
  - Après `am force-stop` et réouverture, les deux trails sont toujours présents (persistance Room). Le détail
    affiche « Mountain Test », « 8.0 km », « ↑ 750 m » (`docs/m4/lab1_detail.png`).
  - Contrôle de non-régression : la sauvegarde avec un nom vide est toujours refusée
    (Snackbar « Enter a name and non-negative numbers »).

## Lab 2 – Conversion km vers miles

- **Symptôme :** en unité MI, 10.0 km s'affiche « 5.0 mi » au lieu d'environ « 6.2 mi ». L'affichage en KM est correct.
- **Code concerné :** `TrailFormatter.distance(km, unit)` dans `model/UserSettings.kt`. Il est appelé par la liste
  (`TrailPagingAdapter.kt:26`) et par le détail (`TrailDetailActivity.kt:70`).
- **Breakpoint :** sur la ligne `else "%.1f mi".format(...)` de `TrailFormatter.distance`, dans `UserSettings.kt`.
  On peut aussi le poser sur l'appel dans `TrailDetailActivity.kt:70` puis faire *Step Into*.
- **Valeurs à observer (avant correction) :**
  - `km` (distance d'entrée) = `10.0`
  - `unit` = `DistanceUnit.MI`
  - facteur de conversion = `0.5` (faux, il faut 0.621371)
  - Evaluate Expression `km * 0.5` = `5.0`, d'où la sortie formatée `"5.0 mi"`
  - Après correction : `km * MILES_PER_KM` = `6.21371`, d'où `"6.2 mi"`
- **Cause :** le facteur de conversion était faux (`0.5` au lieu de `0.621371`, car 1 mi = 1.609344 km).
- **Correction :** ajout de la constante `TrailFormatter.MILES_PER_KM = 0.621371`, utilisée dans la branche MI.
  La valeur `distanceKm` persistée (Room) et la branche KM ne changent pas.
- **Fichier modifié :** `app/src/main/java/fr/estiam/traillog/model/UserSettings.kt`. Test ajouté :
  `app/src/test/.../model/TrailFormatterTest.kt`.
- **Vérification :**
  - Tests unitaires : 10.0 km donne « 6.2 mi », 10.0 km reste « 10.0 km », 0 km donne « 0.0 mi ».
  - Émulateur, unité MI : un trail « Ten Km Test » de 10.0 km affiche **6.2 mi** dans la liste et dans le détail
    (`docs/m4/lab2_list_mi.png`, `docs/m4/lab2_detail_mi.png`). Autres valeurs de la liste : 3.6 km donne 2.2 mi,
    4.9 km donne 3.0 mi, 8.0 km donne 5.0 mi, 9.0 km donne 5.6 mi, 10.3 km donne 6.4 mi.
  - Après `am force-stop` et réouverture, Settings affiche toujours « MI » (préférence DataStore conservée).
  - Retour à l'unité KM : la liste et le détail affichent de nouveau « 10.0 km ». La valeur `distanceKm` stockée
    n'a pas changé.

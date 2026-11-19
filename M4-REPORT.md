# TrailLog M4 – Rapport de débogage

Branche : `fix/m4-debugging` (créée depuis `m4-student`).

## Environnement et vérification

- Build : `gradlew.bat clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL**. `app-debug.apk` est généré.
- Tests unitaires : 6/6 réussis (`TrailTest` : 3, `TrailFormatterTest` : 3).
- **Pas de vérification sur l'émulateur.** L'AVD `Medium_Phone_API_37.0` existe, mais l'émulateur refuse de démarrer :
  `FATAL | Your device does not have enough disk space to run avd`. Il ne reste qu'environ 1,8 Go sur C:.
  Les étapes manuelles des labs (Logcat, breakpoint, persistance après redémarrage) restent donc à faire dans Android Studio.
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
- **Vérification :** les tests du modèle passent (750 m et 250 m valides, -1 m invalide). Le test UI (sauvegarde,
  puis arrêt forcé et réouverture) n'a pas été fait faute d'émulateur.

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
- **Vérification :** les tests unitaires passent (10.0 km donne « 6.2 mi », 10.0 km reste « 10.0 km », 0 km donne « 0.0 mi »).
  La vérification visuelle de la liste et du détail, ainsi que celle de la préférence d'unité après redémarrage,
  n'a pas été faite faute d'émulateur.

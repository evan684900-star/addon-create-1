# Create: Locksmithing

Addon pour **Create 6.0.10** sur **NeoForge 1.21.1** : des portes blindées qui ne s'ouvrent qu'avec leur clé,
et toute la chaîne pour les fabriquer (bois compressé, métal en fusion, moule, taille de clés).

## Contenu

| Quoi | Comment ça marche |
|---|---|
| **Planche pressée** | N'importe quelle planche passée sous une **Presse mécanique** (sur un dépôt ou un tapis). |
| **Bois compressé** | **4 planches pressées** compactées (Presse mécanique + Bassin). |
| **Four à moufle** | Posé sur un **Brûleur à Blaze allumé**, fait fondre les lingots de **fer, or, cuivre, zinc** : 1 lingot = **90 mB**. Brûleur surchauffé = 2× plus rapide. Clic droit avec des lingots pour les charger, avec un **seau** (1000 mB) ou une **fiole** (90 mB) pour récupérer le métal. Les entonnoirs/trémies chargent, les pompes/tuyaux de Create vident. |
| **Métal en fusion** (×4) | Se met en seau et en fiole, passe dans les tuyaux/réservoirs/becs verseurs de Create, **ne se pose pas par terre**. |
| **Enclume de moulage** | Clic droit = interface : seau/fiole de métal en fusion à gauche, flèche de progression, **barre** à droite (90 mB par barre). Remplissable par tuyau, barres récupérables par entonnoir. |
| **Barres** de fer, or, cuivre, zinc | Sortent de l'enclume de moulage. |
| **Machine à tailler les clés** | A besoin de **rotation** (axe à l'arrière). Clic droit = interface : clé vierge à gauche, choix de la clé à tailler, clé taillée à droite. Chaque clé taillée est **unique**. |
| **Porte en cuivre blindée** | Ne s'ouvre ni à la main, ni à la redstone : **la première clé utilisée dessus devient sa seule clé**. Casser la porte remet la serrure à zéro. **42 s à la hache en fer** (minimum pour la récupérer), plus rapide avec une meilleure hache/Efficacité. Résiste aux explosions. S'oxyde comme le cuivre, **rayon de miel** pour la cirer, **hache** pour gratter. |

Chaque machine a un **tuto Ponder** (touche **W** au-dessus de l'objet), en français et en anglais.

## Recettes

```
Moule              Enclume de moulage     Lame de scie         Machine à tailler
A . A              B M B                  . A .                S B B
A P A              . B .                  A A A                . B .
                   P P P                  . A .                P P P

Clé vierge         Porte blindée          Four à moufle
A B B              C C C                  P P P
                   C W C                  P F P
                   C W C                  K K K
```
`A` alliage d'andésite · `P` plaque de fer · `B` bloc de fer (enclume, machine) / barre de fer (clé vierge) ·
`M` moule · `S` lame de scie · `C` barre de cuivre · `W` bois compressé · `F` fourneau · `K` briques.

## Compiler

Il faut **Java 21**. Gradle est téléchargé tout seul par le wrapper.

```bash
./gradlew build
```

Le jar se trouve dans `build/libs/`. Sur GitHub, chaque push lance l'action **Build** : le jar est dans les
« Artifacts » de l'exécution (onglet *Actions*).

Autres commandes utiles :

| Commande | Rôle |
|---|---|
| `./gradlew runClient` | Lance Minecraft avec le mod et Create |
| `./gradlew runGameTestServer` | Lance les tests automatiques en jeu (four, moule, clés, serrure…) |
| `./gradlew runScreenshots` | Visite automatique qui prend des captures dans `run/screenshots-run/screenshots/` |

## Organisation

```
src/main/java/.../locksmithing/
  content/door        porte blindée (serrure, oxydation)
  content/furnace     four à moufle
  content/casting     enclume de moulage (bloc, interface, rendu du métal)
  content/keycutter   machine à tailler les clés (bloc, interface, rendu de la lame)
  content/fluid       métaux en fusion, seaux, fioles
  recipe              recettes "fonte", "moulage", "taille de clé"
  ponder              tutos Ponder
  registry            enregistrement des blocs, objets, fluides…
src/gametest/         tests automatiques (pas inclus dans le jar)
tools/                scripts Python qui génèrent textures, modèles, traductions, recettes et schémas Ponder
```

Les textures et les JSON sont **générés** : pour les changer, modifier `tools/gen_textures.py` ou
`tools/gen_resources.py` puis relancer :

```bash
python3 tools/gen_textures.py
python3 tools/gen_resources.py
```

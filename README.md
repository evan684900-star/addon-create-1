# Create: Locksmithing

Addon pour **Create 6.0.10** sur **NeoForge 1.21.1** : des portes blindées qui ne s'ouvrent qu'avec leur clé,
et toute la chaîne pour les fabriquer (bois compressé, métal en fusion, moule, taille de clés).

## Contenu

| Quoi | Comment ça marche |
|---|---|
| **Planche pressée** | N'importe quelle planche passée sous une **Presse mécanique** (sur un dépôt ou un tapis). |
| **Bois compressé** | **4 planches pressées** compactées (Presse mécanique + Bassin). |
| **Four à moufle** | Boîtier en cuivre de Create avec une bouche de four. Posé sur un **Brûleur à Blaze allumé**, fait fondre les lingots de **fer, or, cuivre, zinc** : 1 lingot = **90 mB**. Brûleur surchauffé = 2× plus rapide. Clic droit avec des lingots pour les charger, avec un **seau** vide (1000 mB) pour récupérer le métal. Les entonnoirs/trémies chargent, les pompes/tuyaux de Create vident. |
| **Métal en fusion** (×4) | Se met dans le **seau** de Minecraft (on voit le métal dedans), passe dans les tuyaux/réservoirs/becs verseurs de Create, **ne se pose pas par terre**. |
| **Enclume de moulage** | L'enclume de Minecraft avec un **moule incrusté** au milieu. Clic droit = interface : seau de métal en fusion à gauche, flèche de progression, **barre** à droite (90 mB par barre). Remplissable par tuyau, barres récupérables par entonnoir. |
| **Barres** de fer, or, cuivre, zinc | Sortent de l'enclume de moulage. |
| **Machine à tailler les clés** | L'enclume de Minecraft avec une **lame de scie sur le dessus**. A besoin de **rotation** (l'axe traverse le pied). Clic droit = interface : clé vierge à gauche, choix de la clé à tailler, clé taillée à droite. Chaque clé taillée est **unique**. |
| **Clés vierges et clés** en cuivre, fer, zinc, or | La clé vierge de chaque métal se fait avec ses **barres** ; la machine la taille en clé de ce métal. Une clé n'ouvre que les portes de **son métal**. |
| **Portes blindées** en cuivre, fer, zinc, or | Ne s'ouvrent ni à la main, ni à la redstone : **la première clé utilisée dessus devient sa seule clé**. Casser la porte remet la serrure à zéro. Il faut au moins une **hache en fer**, et plus le minerai est rare, plus c'est long : **cuivre 42 s, fer 56 s, zinc 70 s, or 84 s** (plus rapide avec une meilleure hache/Efficacité). Résistent aux explosions. S'ouvrent avec une **animation** (elles pivotent sur leurs gonds en 1 s) et leur **contour/collision suit le mouvement**. Le cuivre s'oxyde : **rayon de miel** pour le cirer, **hache** pour gratter. Clic droit avec une **tapisserie** : elle remplace le bois de la porte (des deux côtés, avec ses motifs) ; des **cisailles** la reprennent, et elle revient aussi quand on casse la porte. |
| **Tapisserie en cuir** | Se colle **à plat sur un mur**, comme du **papier peint** : 1 bloc de large, 2 de haut, à partir du bloc visé vers le haut. Côte à côte, elles tapissent un mur. Se décore dans le **métier à tisser** de Minecraft (motifs et couleurs de bannière). Garde ses motifs quand on la casse ; l'icône montre les motifs. Se pose aussi **sur une porte blindée** à la place du bois. |

Chaque machine a un **tuto Ponder** (touche **W** au-dessus de l'objet), en français et en anglais.

Avec **JEI** installé : catégories *Fonte*, *Moulage* et *Taille de clé*, plus une fiche d'info sur chaque porte et chaque clé
(les recettes de presse et de compactage sont dans les catégories de Create).

Sons : serrure, clés, taille, coulée… tous trouvés sur OpenGameArt en licence CC0, voir [SOUNDS_CREDITS.md](SOUNDS_CREDITS.md).

## Recettes

```
Moule              Enclume de moulage     Lame de scie         Machine à tailler
A . A              B M B                  . A .                S B B
A P A              . B .                  A A A                . B .
                   P P P                  . A .                P P P

Clé vierge         Porte blindée          Four à moufle          Tapisserie
A X X              X X X                  P P P                  L L L
                   X W X                  P F P                  L L L
                   X W X                  K K K                  L L L
```
`A` alliage d'andésite · `P` plaque de fer · `B` bloc de fer · `M` moule · `S` lame de scie ·
`X` barre du métal de la clé/porte (cuivre, fer, zinc ou or) · `W` bois compressé · `F` fourneau · `K` briques · `L` cuir.

## Télécharger

Dernière version (compilée et testée automatiquement à chaque mise à jour) :
**[create_locksmithing-1.21.1-0.1.0.jar](https://github.com/evan684900-star/addon-create-1/releases/download/dev/create_locksmithing-1.21.1-0.1.0.jar)**
(page : [Releases › dev](https://github.com/evan684900-star/addon-create-1/releases/tag/dev)).

À mettre dans le dossier `mods` avec **Create 6.0.10** sur **NeoForge 1.21.1**. JEI est facultatif.

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
  content/door        porte blindée (serrure, oxydation, animation, tapisserie posée dessus)
  content/tapestry    tapisserie en cuir (métier à tisser, rendu des motifs)
  content/furnace     four à moufle
  content/casting     enclume de moulage (bloc, interface, rendu du métal)
  content/keycutter   machine à tailler les clés (bloc, interface, rendu de la lame)
  content/fluid       métaux en fusion, seaux
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

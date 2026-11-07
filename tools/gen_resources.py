#!/usr/bin/env python3
"""
Génère les fichiers JSON (modèles, états de bloc, traductions FR/EN, recettes, butins, tags)
et les schémas des tutos Ponder (.nbt) de Create: Locksmithing.

    python3 tools/gen_resources.py

Modifier ce fichier puis le relancer plutôt que d'éditer les JSON à la main.
"""
import gzip
import io
import json
import os
import struct

MOD = "create_locksmithing"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", MOD)
DATA = os.path.join(ROOT, "data")

METALS = ["iron", "gold", "copper", "zinc"]
STAGES = ["", "exposed", "weathered", "oxidized"]
FACING_Y = {"north": 0, "east": 90, "south": 180, "west": 270}


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def ml(path):
    return f"{MOD}:{path}"


# ============================================================================================
# Modèles et états de bloc
# ============================================================================================
def face(tex, cull=None, uv=None):
    f = {"texture": tex}
    if uv:
        f["uv"] = uv
    if cull:
        f["cullface"] = cull
    return f


def box(frm, to, tex="#side", up=None, down=None, faces=None, cull_down=False):
    fs = {d: face(tex) for d in ("north", "south", "east", "west")}
    fs["up"] = face(up or tex)
    fs["down"] = face(down or tex, "down" if cull_down else None)
    if faces:
        fs.update(faces)
    return {"from": frm, "to": to, "faces": fs}


def anvil_model():
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ml("block/casting_anvil_side"),
            "side": ml("block/casting_anvil_side"),
            "top": ml("block/casting_anvil_top"),
            "cavity": ml("block/casting_anvil_cavity"),
        },
        "elements": [
            box([2, 0, 2], [14, 4, 14], up="#top", cull_down=True),
            box([4, 4, 3], [12, 5, 13]),
            box([6, 5, 4], [10, 10, 12]),
            box([0, 10, 3], [16, 13, 13], up="#cavity"),
            box([0, 13, 3], [16, 16, 6], up="#top", faces={"south": face("#cavity")}),
            box([0, 13, 10], [16, 16, 13], up="#top", faces={"north": face("#cavity")}),
            box([0, 13, 6], [4, 16, 10], up="#top", faces={"east": face("#cavity")}),
            box([12, 13, 6], [16, 16, 10], up="#top", faces={"west": face("#cavity")}),
        ],
    }


def cutter_body_elements():
    return [
        box([2, 0, 2], [14, 4, 14], up="#top", cull_down=True),
        box([5, 4, 3], [11, 10, 14]),
        box([1, 10, 2], [15, 14, 14], up="#top"),
        box([4, 14, 6], [7, 16, 10], up="#top"),
        box([9, 14, 6], [12, 16, 10], up="#top"),
    ]


def saw_elements(south=True):
    """Lame + moyeu. Le modèle tournant est dessiné vers le SUD (convention de Create)."""
    blade = [0, 0, 14.5], [16, 16, 15.5]
    hub = [6.5, 6.5, 13], [9.5, 9.5, 16]
    if not south:
        blade = [0, 0, 0.5], [16, 16, 1.5]
        hub = [6.5, 6.5, 0], [9.5, 9.5, 3]
    full = [0, 0, 16, 16]
    return [
        {"from": blade[0], "to": blade[1],
         "faces": {"north": face("#saw", uv=full), "south": face("#saw", uv=full)}},
        {"from": hub[0], "to": hub[1],
         "faces": {d: face("#hub", uv=[0, 0, 3, 3]) for d in ("north", "south", "east", "west", "up", "down")}},
    ]


CUTTER_TEXTURES = {
    "particle": ml("block/casting_anvil_side"),
    "side": ml("block/casting_anvil_side"),
    "top": ml("block/key_cutter_top"),
    "saw": ml("block/key_cutter_saw"),
    "hub": ml("block/key_cutter_hub"),
}


def models():
    m = os.path.join(ASSETS, "models")
    bs = os.path.join(ASSETS, "blockstates")

    # Enclume de moulage
    write_json(os.path.join(m, "block", "casting_anvil.json"), anvil_model())
    write_json(os.path.join(bs, "casting_anvil.json"), {"variants": {
        f"facing={f}": {"model": ml("block/casting_anvil"), "y": y} if y else {"model": ml("block/casting_anvil")}
        for f, y in FACING_Y.items()}})
    write_json(os.path.join(m, "item", "casting_anvil.json"), {"parent": ml("block/casting_anvil")})

    # Machine à tailler les clés : corps fixe + lame tournante (modèle partiel)
    write_json(os.path.join(m, "block", "key_cutter", "block.json"), {
        "parent": "minecraft:block/block",
        "textures": {k: v for k, v in CUTTER_TEXTURES.items() if k in ("particle", "side", "top")},
        "elements": cutter_body_elements(),
    })
    write_json(os.path.join(m, "block", "key_cutter", "saw.json"), {
        "render_type": "minecraft:cutout",
        "textures": {"particle": ml("block/key_cutter_saw"), "saw": ml("block/key_cutter_saw"),
                     "hub": ml("block/key_cutter_hub")},
        "elements": saw_elements(south=True),
    })
    write_json(os.path.join(m, "item", "key_cutter.json"), {
        "parent": "minecraft:block/block",
        "render_type": "minecraft:cutout",
        "textures": CUTTER_TEXTURES,
        "elements": cutter_body_elements() + saw_elements(south=False),
    })
    write_json(os.path.join(bs, "key_cutter.json"), {"variants": {
        f"facing={f}": {"model": ml("block/key_cutter/block"), "y": y} if y else {"model": ml("block/key_cutter/block")}
        for f, y in FACING_Y.items()}})

    # Four à moufle
    for lit in (False, True):
        name = "muffle_furnace_on" if lit else "muffle_furnace"
        write_json(os.path.join(m, "block", f"{name}.json"), {
            "parent": "minecraft:block/orientable_with_bottom",
            "textures": {
                "top": ml("block/muffle_furnace_top"),
                "bottom": ml("block/muffle_furnace_bottom"),
                "side": ml("block/muffle_furnace_side"),
                "front": ml(f"block/{name.replace('muffle_furnace', 'muffle_furnace_front')}"),
            },
        })
    variants = {}
    for f, y in FACING_Y.items():
        for lit in ("false", "true"):
            v = {"model": ml("block/muffle_furnace_on" if lit == "true" else "block/muffle_furnace")}
            if y:
                v["y"] = y
            variants[f"facing={f},lit={lit}"] = v
    write_json(os.path.join(bs, "muffle_furnace.json"), {"variants": variants})
    write_json(os.path.join(m, "item", "muffle_furnace.json"), {"parent": ml("block/muffle_furnace")})

    # Porte en cuivre blindée (4 niveaux d'oxydation, la cire ne change pas l'apparence)
    parts = ["bottom_left", "bottom_left_open", "bottom_right", "bottom_right_open",
             "top_left", "top_left_open", "top_right", "top_right_open"]
    for stage in STAGES:
        prefix = f"{stage}_" if stage else ""
        for part in parts:
            write_json(os.path.join(m, "block", f"{prefix}armored_copper_door_{part}.json"), {
                "parent": f"minecraft:block/door_{part}",
                "textures": {
                    "bottom": ml(f"block/{prefix}armored_copper_door_bottom"),
                    "top": ml(f"block/{prefix}armored_copper_door_top"),
                },
            })
    # Rotations identiques aux portes de Minecraft
    rot = {
        ("east", "left", False): 0, ("east", "left", True): 90, ("east", "right", False): 0, ("east", "right", True): 270,
        ("north", "left", False): 270, ("north", "left", True): 0, ("north", "right", False): 270, ("north", "right", True): 180,
        ("south", "left", False): 90, ("south", "left", True): 180, ("south", "right", False): 90, ("south", "right", True): 0,
        ("west", "left", False): 180, ("west", "left", True): 270, ("west", "right", False): 180, ("west", "right", True): 90,
    }
    variants = {}
    for stage in STAGES:
        prefix = f"{stage}_" if stage else ""
        oxidation = stage or "unaffected"
        for (facing, hinge, opened), y in rot.items():
            for half, part in (("lower", "bottom"), ("upper", "top")):
                model = ml(f"block/{prefix}armored_copper_door_{part}_{hinge}" + ("_open" if opened else ""))
                key = f"facing={facing},half={half},hinge={hinge},open={str(opened).lower()},oxidation={oxidation}"
                v = {"model": model}
                if y:
                    v["y"] = y
                variants[key] = v
    write_json(os.path.join(bs, "armored_copper_door.json"), {"variants": variants})

    # Objets "plats"
    flat = ["pressed_plank", "compressed_wood", "mold", "saw_blade", "blank_key", "copper_key", "armored_copper_door"]
    flat += [f"{metal}_bar" for metal in METALS]
    flat += [f"molten_{metal}_bucket" for metal in METALS]
    flat += [f"molten_{metal}_bottle" for metal in METALS]
    for name in flat:
        write_json(os.path.join(m, "item", f"{name}.json"), {
            "parent": "minecraft:item/handheld" if name.endswith("_bar") else "minecraft:item/generated",
            "textures": {"layer0": ml(f"item/{name}")},
        })


# ============================================================================================
# Traductions
# ============================================================================================
METAL_NAMES = {
    "iron": ("Iron", "fer", "de fer"),
    "gold": ("Gold", "or", "d'or"),
    "copper": ("Copper", "cuivre", "de cuivre"),
    "zinc": ("Zinc", "zinc", "de zinc"),
}

PONDER = {
    "compressed_wood": (
        ("Making Compressed Wood", "Fabriquer du bois compressé"),
        [
            ("Any wooden plank pressed by a Mechanical Press becomes a Pressed Plank",
             "N'importe quelle planche de bois passée sous une Presse mécanique devient une Planche pressée"),
            ("Then, compacting 4 Pressed Planks in a Basin gives 1 Compressed Wood",
             "Ensuite, compacter 4 Planches pressées dans un Bassin donne 1 Bois compressé"),
            ("Compressed Wood is used to craft Armored Doors",
             "Le bois compressé sert à fabriquer les portes blindées"),
        ],
    ),
    "muffle_furnace": (
        ("Melting Ingots in the Muffle Furnace", "Faire fondre des lingots dans le four à moufle"),
        [
            ("The Muffle Furnace has to sit on a lit Blaze Burner",
             "Le four à moufle doit être posé sur un Brûleur à Blaze allumé"),
            ("Right-click it with Iron, Gold, Copper or Zinc ingots to load them. Funnels and hoppers work too",
             "Clic droit avec des lingots de fer, d'or, de cuivre ou de zinc pour les mettre dedans. Les entonnoirs et les entonnoirs de Minecraft marchent aussi"),
            ("Each ingot melts into 90 mB of molten metal: exactly one bar",
             "Chaque lingot fond en 90 mB de métal en fusion : exactement une barre"),
            ("Collect it with an empty Bucket (1000 mB) or Glass Bottle (90 mB), or pump it out through pipes",
             "Récupérez-le avec un seau vide (1000 mB) ou une fiole (90 mB), ou pompez-le avec des tuyaux"),
            ("A superheated Blaze Burner melts twice as fast",
             "Un Brûleur à Blaze surchauffé fait fondre deux fois plus vite"),
        ],
    ),
    "casting_anvil": (
        ("Casting Bars with the Casting Anvil", "Couler des barres avec l'enclume de moulage"),
        [
            ("Right-click the Casting Anvil to open its interface",
             "Clic droit sur l'enclume de moulage pour ouvrir son interface"),
            ("Put a bucket or a bottle of molten metal in the left slot: it is poured into the mold",
             "Mettez un seau ou une fiole de métal en fusion à gauche : il est versé dans le moule"),
            ("Once it has cooled down, a bar comes out on the right. One bar uses 90 mB",
             "Une fois refroidi, une barre sort à droite. Une barre utilise 90 mB"),
            ("Fluid pipes can also fill the mold, and funnels can take the bars out",
             "Les tuyaux peuvent aussi remplir le moule, et les entonnoirs peuvent sortir les barres"),
        ],
    ),
    "key_cutter": (
        ("Cutting Keys", "Tailler des clés"),
        [
            ("The Key Cutter needs rotational force, given to the shaft at its back",
             "La machine à tailler les clés a besoin de rotation, donnée à l'axe à l'arrière"),
            ("Right-click it, put a Blank Key in the left slot and choose the key to cut",
             "Clic droit dessus, mettez une clé vierge à gauche et choisissez la clé à tailler"),
            ("Every key that comes out is unique, even if two of them look the same",
             "Chaque clé qui sort est unique, même si deux clés se ressemblent"),
            ("The faster it spins, the faster keys are cut",
             "Plus elle tourne vite, plus les clés sont taillées vite"),
        ],
    ),
    "armored_door": (
        ("Locking an Armored Copper Door", "Verrouiller une porte en cuivre blindée"),
        [
            ("Armored Doors cannot be opened by hand, nor with redstone",
             "Les portes blindées ne s'ouvrent ni à la main, ni avec de la redstone"),
            ("The first cut key used on a door becomes its only key",
             "La première clé taillée utilisée sur une porte devient sa seule clé"),
            ("Any other key is refused, even another Copper Key",
             "Toute autre clé est refusée, même une autre clé en cuivre"),
            ("Breaking the door resets its lock. It takes 42 seconds with an Iron Axe, less with a better one",
             "Casser la porte remet sa serrure à zéro. Il faut 42 secondes avec une hache en fer, moins avec une meilleure"),
            ("Like copper, the door slowly oxidizes over time",
             "Comme le cuivre, la porte s'oxyde petit à petit"),
            ("Use a Honeycomb to wax it and keep its look. An Axe scrapes off wax and oxidation",
             "Un rayon de miel la cire et garde son apparence. Une hache retire la cire et l'oxydation"),
        ],
    ),
}


def lang():
    en, fr = {}, {}

    def add(key, e, f):
        en[key] = e
        fr[key] = f

    add(f"itemGroup.{MOD}.main", "Create: Locksmithing", "Create: Locksmithing")

    # Blocs
    add(f"block.{MOD}.armored_copper_door", "Armored Copper Door", "Porte en cuivre blindée")
    add(f"block.{MOD}.muffle_furnace", "Muffle Furnace", "Four à moufle")
    add(f"block.{MOD}.casting_anvil", "Casting Anvil", "Enclume de moulage")
    add(f"block.{MOD}.key_cutter", "Key Cutter", "Machine à tailler les clés")

    # Objets
    add(f"item.{MOD}.pressed_plank", "Pressed Plank", "Planche pressée")
    add(f"item.{MOD}.compressed_wood", "Compressed Wood", "Bois compressé")
    add(f"item.{MOD}.mold", "Mold", "Moule")
    add(f"item.{MOD}.saw_blade", "Saw Blade", "Lame de scie")
    add(f"item.{MOD}.blank_key", "Blank Key", "Clé vierge")
    add(f"item.{MOD}.copper_key", "Copper Key", "Clé en cuivre")
    for metal, (e, f, de) in METAL_NAMES.items():
        add(f"item.{MOD}.{metal}_bar", f"{e} Bar", f"Barre {de}")
        add(f"item.{MOD}.molten_{metal}_bucket", f"Molten {e} Bucket", f"Seau {de} en fusion")
        add(f"item.{MOD}.molten_{metal}_bottle", f"Bottle of Molten {e}", f"Fiole {de} en fusion")
        name_fr = {"iron": "Fer", "gold": "Or", "copper": "Cuivre", "zinc": "Zinc"}[metal] + " en fusion"
        add(f"fluid.{MOD}.molten_{metal}", f"Molten {e}", name_fr)
        add(f"fluid_type.{MOD}.molten_{metal}", f"Molten {e}", name_fr)

    # Clés et porte
    add(f"item.{MOD}.key.uncut", "Not cut yet", "Pas encore taillée")
    add(f"item.{MOD}.key.code", "Key #%s", "Clé n°%s")
    for stage, e, f in [("exposed", "Exposed", "Exposée"), ("weathered", "Weathered", "Érodée"),
                        ("oxidized", "Oxidized", "Oxydée")]:
        add(f"item.{MOD}.armored_door.oxidation.{stage}", e, f)
    add(f"item.{MOD}.armored_door.waxed", "Waxed", "Cirée")
    add(f"message.{MOD}.door.need_key", "This door is locked: it needs a key",
        "Cette porte est verrouillée : il faut une clé")
    add(f"message.{MOD}.door.wrong_key_type", "This key does not fit this kind of door",
        "Cette clé ne va pas dans ce type de porte")
    add(f"message.{MOD}.door.bound", "The lock now belongs to key #%s",
        "La serrure appartient maintenant à la clé n°%s")
    add(f"message.{MOD}.door.wrong_key", "This is not the right key", "Ce n'est pas la bonne clé")

    # Interfaces et lunettes d'ingénieur
    add(f"gui.{MOD}.casting_anvil.empty", "The mold is empty", "Le moule est vide")
    add(f"gui.{MOD}.key_cutter.no_rotation", "Needs rotation", "Rotation nécessaire")
    add(f"{MOD}.gui.goggles.muffle_furnace", "Muffle Furnace:", "Four à moufle :")
    add(f"{MOD}.gui.goggles.casting_anvil", "Casting Anvil:", "Enclume de moulage :")
    add(f"{MOD}.gui.goggles.heated", "Heated", "Chauffé")
    add(f"{MOD}.gui.goggles.not_heated", "Needs a lit Blaze Burner below", "Il faut un Brûleur à Blaze allumé dessous")

    # Infobulles façon Create (Maj pour les détails)
    def tip(key, summary, behaviours):
        add(f"{key}.tooltip.summary", summary[0], summary[1])
        for i, (cond, beh) in enumerate(behaviours, 1):
            add(f"{key}.tooltip.condition{i}", cond[0], cond[1])
            add(f"{key}.tooltip.behaviour{i}", beh[0], beh[1])

    tip(f"block.{MOD}.armored_copper_door",
        ("A _heavy door_ that only opens with its _key_.",
         "Une _porte lourde_ qui ne s'ouvre qu'avec sa _clé_."),
        [(("When R-Clicked with a Key", "Clic droit avec une clé"),
          ("The _first key_ used becomes the _only key_ that opens it. _Breaking_ the door resets the lock.",
           "La _première clé_ utilisée devient la _seule clé_ qui l'ouvre. _Casser_ la porte remet la serrure à zéro.")),
         (("Over time", "Avec le temps"),
          ("_Oxidizes_ like copper. Use a _Honeycomb_ to keep its look, an _Axe_ to scrape it.",
           "_S'oxyde_ comme le cuivre. Un _rayon de miel_ garde son apparence, une _hache_ la gratte."))])
    tip(f"block.{MOD}.muffle_furnace",
        ("_Melts ingots_ into _molten metal_ when placed on a _lit Blaze Burner_.",
         "_Fait fondre les lingots_ en _métal en fusion_ quand il est posé sur un _Brûleur à Blaze allumé_."),
        [(("When R-Clicked", "Clic droit"),
          ("With _ingots_: loads them. With a _Bucket_ or _Glass Bottle_: collects the molten metal.",
           "Avec des _lingots_ : les charge. Avec un _seau_ ou une _fiole_ : récupère le métal en fusion."))])
    tip(f"block.{MOD}.casting_anvil",
        ("Casts _molten metal_ into _bars_. One bar = _90 mB_.",
         "Coule le _métal en fusion_ en _barres_. Une barre = _90 mB_."),
        [(("When R-Clicked", "Clic droit"),
          ("Opens the _interface_: molten metal _bucket or bottle_ on the left, _bar_ on the right.",
           "Ouvre l'_interface_ : _seau ou fiole_ de métal en fusion à gauche, _barre_ à droite."))])
    tip(f"block.{MOD}.key_cutter",
        ("Cuts _Blank Keys_ into _unique keys_. Needs _rotational force_ from the back.",
         "Taille les _clés vierges_ en _clés uniques_. A besoin de _rotation_ par l'arrière."),
        [(("When R-Clicked", "Clic droit"),
          ("Opens the _interface_ where you _choose_ which key to cut.",
           "Ouvre l'_interface_ où l'on _choisit_ la clé à tailler."))])
    tip(f"item.{MOD}.copper_key",
        ("Opens _Armored Copper Doors_. Every cut key is _unique_.",
         "Ouvre les _portes en cuivre blindées_. Chaque clé taillée est _unique_."), [])
    tip(f"item.{MOD}.blank_key",
        ("Cut it in a _Key Cutter_ to get a key.",
         "À tailler dans une _machine à tailler les clés_ pour obtenir une clé."), [])

    # Tutos Ponder
    for scene, ((h_en, h_fr), texts) in PONDER.items():
        add(f"{MOD}.ponder.{scene}.header", h_en, h_fr)
        for i, (t_en, t_fr) in enumerate(texts, 1):
            add(f"{MOD}.ponder.{scene}.text_{i}", t_en, t_fr)

    write_json(os.path.join(ASSETS, "lang", "en_us.json"), dict(sorted(en.items())))
    write_json(os.path.join(ASSETS, "lang", "fr_fr.json"), dict(sorted(fr.items())))


# ============================================================================================
# Données : recettes, butins, tags
# ============================================================================================
def item(i):
    return {"item": i}


def tag(t):
    return {"tag": t}


def shaped(name, pattern, key, result, count=1):
    write_json(os.path.join(DATA, MOD, "recipe", "crafting", f"{name}.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "key": key,
        "pattern": pattern,
        "result": {"count": count, "id": result},
    })


def recipes():
    r = os.path.join(DATA, MOD, "recipe")
    alloy = item("create:andesite_alloy")
    iron_plate = tag("c:plates/iron")
    iron_block = tag("c:storage_blocks/iron")

    # Bois compressé
    write_json(os.path.join(r, "pressing", "pressed_plank.json"), {
        "type": "create:pressing",
        "ingredients": [tag("minecraft:planks")],
        "results": [{"id": ml("pressed_plank")}],
    })
    write_json(os.path.join(r, "compacting", "compressed_wood.json"), {
        "type": "create:compacting",
        "ingredients": [item(ml("pressed_plank"))] * 4,
        "results": [{"id": ml("compressed_wood")}],
    })

    # Fonte, moulage, fioles
    for metal in METALS:
        fluid = ml(f"molten_{metal}")
        write_json(os.path.join(r, "melting", f"{metal}_ingot.json"), {
            "type": ml("melting"),
            "ingredient": tag(f"c:ingots/{metal}"),
            "result": {"id": fluid, "amount": 90},
            "time": 100,
        })
        write_json(os.path.join(r, "casting", f"{metal}_bar.json"), {
            "type": ml("casting"),
            "fluid": {"type": "neoforge:single", "fluid": fluid, "amount": 90},
            "result": {"id": ml(f"{metal}_bar"), "count": 1},
            "time": 60,
        })
        write_json(os.path.join(r, "filling", f"molten_{metal}_bottle.json"), {
            "type": "create:filling",
            "ingredients": [item("minecraft:glass_bottle"),
                            {"type": "neoforge:single", "amount": 90, "fluid": fluid}],
            "results": [{"id": ml(f"molten_{metal}_bottle")}],
        })
        write_json(os.path.join(r, "emptying", f"molten_{metal}_bottle.json"), {
            "type": "create:emptying",
            "ingredients": [item(ml(f"molten_{metal}_bottle"))],
            "results": [{"id": "minecraft:glass_bottle"}, {"id": fluid, "amount": 90}],
        })

    # Clés
    write_json(os.path.join(r, "key_cutting", "copper_key.json"), {
        "type": ml("key_cutting"),
        "ingredient": item(ml("blank_key")),
        "result": {"id": ml("copper_key"), "count": 1},
    })

    # Crafts
    # Moule : la forme demandée (A.A / AAA) est celle du Bassin de Create, donc on ajoute une plaque de fer au fond.
    shaped("mold", ["A A", "APA"], {"A": alloy, "P": iron_plate}, ml("mold"))
    shaped("casting_anvil", ["BMB", " B ", "PPP"], {"B": iron_block, "M": item(ml("mold")), "P": iron_plate},
           ml("casting_anvil"))
    shaped("saw_blade", [" A ", "AAA", " A "], {"A": alloy}, ml("saw_blade"))
    shaped("key_cutter", ["SBB", " B ", "PPP"], {"S": item(ml("saw_blade")), "B": iron_block, "P": iron_plate},
           ml("key_cutter"))
    shaped("blank_key", ["ABB"], {"A": alloy, "B": item(ml("iron_bar"))}, ml("blank_key"))
    shaped("armored_copper_door", ["BBB", "BCB", "BCB"],
           {"B": item(ml("copper_bar")), "C": item(ml("compressed_wood"))}, ml("armored_copper_door"))
    shaped("muffle_furnace", ["PPP", "PFP", "KKK"],
           {"P": iron_plate, "F": item("minecraft:furnace"), "K": item("minecraft:bricks")}, ml("muffle_furnace"))


def loot():
    lt = os.path.join(DATA, MOD, "loot_table", "blocks")
    for name in ("muffle_furnace", "casting_anvil", "key_cutter"):
        write_json(os.path.join(lt, f"{name}.json"), {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1.0,
                "bonus_rolls": 0.0,
                "entries": [{"type": "minecraft:item", "name": ml(name)}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
            "random_sequence": ml(f"blocks/{name}"),
        })
    door = ml("armored_copper_door")
    write_json(os.path.join(lt, "armored_copper_door.json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1.0,
            "bonus_rolls": 0.0,
            "entries": [{
                "type": "minecraft:item",
                "name": door,
                "conditions": [{"condition": "minecraft:block_state_property", "block": door,
                                "properties": {"half": "lower"}}],
                # garde l'oxydation et la cire, seulement si elles ne sont pas "neuves"
                "functions": [{
                    "function": "minecraft:copy_state",
                    "block": door,
                    "properties": ["oxidation", "waxed"],
                    "conditions": [{"condition": "minecraft:inverted", "term": {
                        "condition": "minecraft:block_state_property", "block": door,
                        "properties": {"oxidation": "unaffected", "waxed": "false"}}}],
                }],
            }],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": ml("blocks/armored_copper_door"),
    })


def tags():
    door = ml("armored_copper_door")
    machines = [ml("muffle_furnace"), ml("casting_anvil"), ml("key_cutter")]
    t = lambda *p: os.path.join(DATA, *p)
    write_json(t("minecraft", "tags", "block", "mineable", "axe.json"), {"values": [door]})
    write_json(t("minecraft", "tags", "block", "mineable", "pickaxe.json"), {"values": machines})
    write_json(t("minecraft", "tags", "block", "needs_iron_tool.json"), {"values": [door]})
    write_json(t("minecraft", "tags", "block", "doors.json"), {"values": [door]})
    write_json(t("minecraft", "tags", "item", "doors.json"), {"values": [door]})
    # Une contraption de Create ne doit pas pouvoir emporter une porte verrouillée
    write_json(t("create", "tags", "block", "non_movable.json"), {"values": [door]})
    write_json(t(MOD, "tags", "item", "keys.json"), {"values": [ml("copper_key")]})
    write_json(t(MOD, "tags", "item", "bars.json"), {"values": [ml(f"{m}_bar") for m in METALS]})


# ============================================================================================
# Schémas Ponder (format "structure" de Minecraft, NBT compressé)
# ============================================================================================
TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


class Int(int):
    pass


def nbt_payload(out, value):
    if isinstance(value, Int) or isinstance(value, int):
        out.write(struct.pack(">i", value))
    elif isinstance(value, str):
        b = value.encode("utf-8")
        out.write(struct.pack(">H", len(b)))
        out.write(b)
    elif isinstance(value, list):
        if not value:
            out.write(bytes([TAG_END]))
            out.write(struct.pack(">i", 0))
            return
        out.write(bytes([nbt_type(value[0])]))
        out.write(struct.pack(">i", len(value)))
        for v in value:
            nbt_payload(out, v)
    elif isinstance(value, dict):
        for k, v in value.items():
            out.write(bytes([nbt_type(v)]))
            kb = k.encode("utf-8")
            out.write(struct.pack(">H", len(kb)))
            out.write(kb)
            nbt_payload(out, v)
        out.write(bytes([TAG_END]))


def nbt_type(value):
    if isinstance(value, int):
        return TAG_INT
    if isinstance(value, str):
        return TAG_STRING
    if isinstance(value, list):
        return TAG_LIST
    if isinstance(value, dict):
        return TAG_COMPOUND
    raise TypeError(value)


def write_structure(name, size, blocks, path=None):
    """blocks : dict (x, y, z) -> (nom, {propriétés})"""
    palette, index = [], {}
    entries = []
    for pos, (block, props) in sorted(blocks.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0])):
        key = (block, tuple(sorted(props.items())))
        if key not in index:
            index[key] = len(palette)
            entry = {"Name": block}
            if props:
                entry["Properties"] = dict(props)
            palette.append(entry)
        entries.append({"pos": [pos[0], pos[1], pos[2]], "state": index[key]})
    root = {
        "DataVersion": 3955,
        "size": list(size),
        "palette": palette,
        "blocks": entries,
        "entities": [],
    }
    buf = io.BytesIO()
    buf.write(bytes([TAG_COMPOUND]))
    buf.write(struct.pack(">H", 0))
    nbt_payload(buf, root)
    path = path or os.path.join(ASSETS, "ponder", f"{name}.nbt")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(gzip.compress(buf.getvalue(), mtime=0))


def floor(size=5):
    return {(x, 0, z): ("minecraft:snow_block" if (x + z) % 2 else "minecraft:white_concrete", {})
            for x in range(size) for z in range(size)}


def schematics():
    b = floor()
    b[(2, 1, 2)] = ("create:depot", {})
    b[(2, 3, 2)] = ("create:mechanical_press", {"facing": "north"})
    b[(2, 3, 3)] = ("create:creative_motor", {"facing": "north"})
    write_structure("compressed_wood", (5, 4, 5), b)

    b = floor()
    b[(2, 1, 2)] = ("create:blaze_burner", {"blaze": "smouldering", "facing": "north"})
    b[(2, 2, 2)] = (ml("muffle_furnace"), {"facing": "north", "lit": "false"})
    write_structure("muffle_furnace", (5, 3, 5), b)

    b = floor()
    b[(2, 1, 2)] = (ml("casting_anvil"), {"facing": "north"})
    write_structure("casting_anvil", (5, 2, 5), b)

    b = floor()
    # la lame regarde vers la caméra (nord), le moteur est derrière
    b[(2, 1, 2)] = (ml("key_cutter"), {"facing": "north"})
    b[(2, 1, 3)] = ("create:creative_motor", {"facing": "north"})
    write_structure("key_cutter", (5, 2, 5), b)

    b = floor()
    for pos in [(1, 1, 2), (1, 2, 2), (3, 1, 2), (3, 2, 2), (1, 3, 2), (2, 3, 2), (3, 3, 2)]:
        b[pos] = ("minecraft:stone_bricks", {})
    door = {"facing": "south", "hinge": "left", "open": "false", "powered": "false",
            "oxidation": "unaffected", "waxed": "false"}
    b[(2, 1, 2)] = (ml("armored_copper_door"), dict(door, half="lower"))
    b[(2, 2, 2)] = (ml("armored_copper_door"), dict(door, half="upper"))
    write_structure("armored_door", (5, 4, 5), b)


def gametest_structure():
    """Zone vide (sol en pierre) utilisée par les GameTests."""
    b = {(x, 0, z): ("minecraft:smooth_stone", {}) for x in range(5) for z in range(5)}
    out = os.path.join(ROOT, "..", "..", "gametest", "resources", "data", MOD, "structure", "empty.nbt")
    write_structure("empty", (5, 4, 5), b, os.path.normpath(out))


def main():
    models()
    lang()
    recipes()
    loot()
    tags()
    schematics()
    gametest_structure()
    print("Ressources générées dans", os.path.normpath(ROOT))


if __name__ == "__main__":
    main()

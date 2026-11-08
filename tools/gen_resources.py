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


# Faces des modèles de porte de Minecraft (block/door_*), recopiées pour les portes avec tapisserie
VANILLA_DOOR_FACES = {
    "bottom_left": {"down": {"uv": [16, 13, 0, 16], "cullface": "down", "rotation": 90},
                    "north": {"uv": [3, 0, 0, 16], "cullface": "north"}, "south": {"uv": [0, 0, 3, 16], "cullface": "south"},
                    "west": {"uv": [0, 0, 16, 16], "cullface": "west"}, "east": {"uv": [16, 0, 0, 16]}},
    "bottom_left_open": {"down": {"uv": [0, 16, 16, 13], "cullface": "down", "rotation": 90},
                         "north": {"uv": [0, 0, 3, 16], "cullface": "north"}, "south": {"uv": [0, 0, 3, 16], "cullface": "south"},
                         "west": {"uv": [16, 0, 0, 16], "cullface": "west"}, "east": {"uv": [0, 0, 16, 16]}},
    "bottom_right": {"down": {"uv": [0, 13, 16, 16], "cullface": "down", "rotation": 90},
                     "north": {"uv": [3, 0, 0, 16], "cullface": "north"}, "south": {"uv": [0, 0, 3, 16], "cullface": "south"},
                     "west": {"uv": [16, 0, 0, 16], "cullface": "west"}, "east": {"uv": [0, 0, 16, 16]}},
    "bottom_right_open": {"down": {"uv": [16, 16, 0, 13], "cullface": "down", "rotation": 90},
                          "north": {"uv": [3, 0, 0, 16], "cullface": "north"}, "south": {"uv": [3, 0, 0, 16], "cullface": "south"},
                          "west": {"uv": [0, 0, 16, 16], "cullface": "west"}, "east": {"uv": [16, 0, 0, 16]}},
    "top_left": {"up": {"uv": [0, 3, 16, 0], "cullface": "up", "rotation": 90},
                 "north": {"uv": [3, 0, 0, 16], "cullface": "north"}, "south": {"uv": [0, 0, 3, 16], "cullface": "south"},
                 "west": {"uv": [0, 0, 16, 16], "cullface": "west"}, "east": {"uv": [16, 0, 0, 16]}},
    "top_left_open": {"up": {"uv": [0, 3, 16, 0], "cullface": "up", "rotation": 270},
                      "north": {"uv": [0, 0, 3, 16], "cullface": "north"}, "south": {"uv": [0, 0, 3, 16], "cullface": "south"},
                      "west": {"uv": [16, 0, 0, 16], "cullface": "west"}, "east": {"uv": [0, 0, 16, 16]}},
    "top_right": {"up": {"uv": [0, 0, 16, 3], "cullface": "up", "rotation": 270},
                  "north": {"uv": [3, 0, 0, 16], "cullface": "north"}, "south": {"uv": [0, 0, 3, 16], "cullface": "south"},
                  "west": {"uv": [16, 0, 0, 16], "cullface": "west"}, "east": {"uv": [0, 0, 16, 16]}},
    "top_right_open": {"up": {"uv": [0, 0, 16, 3], "cullface": "up", "rotation": 90},
                       "north": {"uv": [3, 0, 0, 16], "cullface": "north"}, "south": {"uv": [3, 0, 0, 16], "cullface": "south"},
                       "west": {"uv": [0, 0, 16, 16], "cullface": "west"}, "east": {"uv": [16, 0, 0, 16]}},
}


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
# Rotations de l'enclume de Minecraft : le modèle est dessiné pour facing=south
ANVIL_Y = {"south": 0, "west": 90, "north": 180, "east": 270}


def f(tex, uv=None, rotation=None, cull=None):
    out = {"texture": tex}
    if uv is not None:
        out["uv"] = uv
    if rotation:
        out["rotation"] = rotation
    if cull:
        out["cullface"] = cull
    return out


def vanilla_anvil_elements(with_top_face=True):
    """Exactement les éléments de l'enclume de Minecraft (template_anvil), textures #body et #top."""
    top_faces = {
        "down": f("#body", [3, 0, 13, 16], 180),
        "north": f("#body", [3, 0, 13, 6]),
        "south": f("#body", [3, 0, 13, 6]),
        "west": f("#body", [10, 0, 16, 16], 90),
        "east": f("#body", [16, 0, 10, 16], 270),
    }
    if with_top_face:
        top_faces["up"] = f("#top", [3, 0, 13, 16], 180)
    return [
        {"from": [2, 0, 2], "to": [14, 4, 14], "faces": {
            "down": f("#body", [2, 2, 14, 14], 180, "down"),
            "up": f("#body", [2, 2, 14, 14], 180),
            "north": f("#body", [2, 12, 14, 16]),
            "south": f("#body", [2, 12, 14, 16]),
            "west": f("#body", [0, 2, 4, 14], 90),
            "east": f("#body", [4, 2, 0, 14], 270)}},
        {"from": [4, 4, 3], "to": [12, 5, 13], "faces": {
            "up": f("#body", [4, 3, 12, 13], 180),
            "north": f("#body", [4, 11, 12, 12]),
            "south": f("#body", [4, 11, 12, 12]),
            "west": f("#body", [4, 3, 5, 13], 90),
            "east": f("#body", [5, 3, 4, 13], 270)}},
        {"from": [6, 5, 4], "to": [10, 10, 12], "faces": {
            "north": f("#body", [6, 6, 10, 11]),
            "south": f("#body", [6, 6, 10, 11]),
            "west": f("#body", [5, 4, 10, 12], 90),
            "east": f("#body", [10, 4, 5, 12], 270)}},
        {"from": [3, 10, 0], "to": [13, 16, 16], "faces": top_faces},
    ]


def top_piece(x0, z0, x1, z1):
    """Morceau plat du dessus de l'enclume, avec la même texture que l'enclume entière (sans raccord visible)."""
    return {"from": [x0, 16, z0], "to": [x1, 16, z1],
            "faces": {"up": f("#top", [16 - x0, 16 - z0, 16 - x1, 16 - z1])}}


def casting_anvil_model():
    """L'enclume de Minecraft avec un moule incrusté au milieu du dessus (creux : x 6..10, z 4..12, 2 px)."""
    mold_up = lambda x0, z0, x1, z1: {"from": [x0, 16, z0], "to": [x1, 16, z1], "faces": {"up": f("#mold")}}
    elements = vanilla_anvil_elements(with_top_face=False)
    elements += [
        # dessus de l'enclume autour du moule
        top_piece(3, 0, 13, 3), top_piece(3, 13, 13, 16), top_piece(3, 3, 5, 13), top_piece(11, 3, 13, 13),
        # bord du moule
        mold_up(5, 3, 11, 4), mold_up(5, 12, 11, 13), mold_up(5, 4, 6, 12), mold_up(10, 4, 11, 12),
        # parois intérieures du creux
        {"from": [5, 14, 4], "to": [6, 16, 12], "faces": {"east": f("#mold")}},
        {"from": [10, 14, 4], "to": [11, 16, 12], "faces": {"west": f("#mold")}},
        {"from": [6, 14, 3], "to": [10, 16, 4], "faces": {"south": f("#mold")}},
        {"from": [6, 14, 12], "to": [10, 16, 13], "faces": {"north": f("#mold")}},
        # fond du creux (plaque de fer)
        {"from": [6, 13, 4], "to": [10, 14, 12], "faces": {"up": f("#floor")}},
    ]
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": "minecraft:block/anvil",
            "body": "minecraft:block/anvil",
            "top": "minecraft:block/anvil_top",
            "mold": ml("block/mold_side"),
            "floor": ml("block/mold_floor"),
        },
        "elements": elements,
    }


def saw_supports():
    """Deux petits supports en alliage d'andésite qui tiennent l'axe de la lame (au bout, côté sud)."""
    return [
        {"from": [6, 16, 10], "to": [7, 17, 12], "faces": {d: f("#mold") for d in ("north", "south", "east", "west", "up")}},
        {"from": [9, 16, 10], "to": [10, 17, 12], "faces": {d: f("#mold") for d in ("north", "south", "east", "west", "up")}},
    ]


def saw_partial_elements():
    """Lame (disque de 10 px) centrée dans le bloc, dessinée vers le SUD (axe Z) ; le code la place sur l'enclume."""
    full = [0, 0, 16, 16]
    return [
        {"from": [3, 3, 7.5], "to": [13, 13, 8.5],
         "faces": {"north": f("#saw", full), "south": f("#saw", full)}},
        {"from": [7, 7, 7], "to": [9, 9, 9],
         "faces": {d: f("#hub", [0, 0, 2, 2]) for d in ("north", "south", "east", "west", "up", "down")}},
    ]


def models():
    m = os.path.join(ASSETS, "models")
    bs = os.path.join(ASSETS, "blockstates")

    def anvil_states(model):
        return {"variants": {
            f"facing={fa}": ({"model": model, "y": y} if y else {"model": model}) for fa, y in ANVIL_Y.items()}}

    # Enclume de moulage
    write_json(os.path.join(m, "block", "casting_anvil.json"), casting_anvil_model())
    write_json(os.path.join(bs, "casting_anvil.json"), anvil_states(ml("block/casting_anvil")))
    write_json(os.path.join(m, "item", "casting_anvil.json"), {"parent": ml("block/casting_anvil")})

    # Machine à tailler les clés : enclume de Minecraft + supports ; l'axe et la lame tournent (rendu à part)
    textures = {
        "particle": "minecraft:block/anvil",
        "body": "minecraft:block/anvil",
        "top": "minecraft:block/anvil_top",
        "mold": ml("block/mold_side"),
        "saw": ml("block/key_cutter_saw"),
        "hub": ml("block/key_cutter_hub"),
    }
    write_json(os.path.join(m, "block", "key_cutter", "block.json"), {
        "parent": "minecraft:block/block",
        "textures": {k: v for k, v in textures.items() if k not in ("saw", "hub")},
        "elements": vanilla_anvil_elements() + saw_supports(),
    })
    write_json(os.path.join(m, "block", "key_cutter", "saw.json"), {
        "render_type": "minecraft:cutout",
        "textures": {"particle": ml("block/key_cutter_saw"), "saw": ml("block/key_cutter_saw"),
                     "hub": ml("block/key_cutter_hub")},
        "elements": saw_partial_elements(),
    })
    # Objet : la lame est figée à sa place (axe X, centre x 8, y 15, z 11)
    full = [0, 0, 16, 16]
    item_saw = [
        {"from": [7.5, 10, 6], "to": [8.5, 20, 16], "faces": {"east": f("#saw", full), "west": f("#saw", full)}},
        {"from": [7, 14, 10], "to": [9, 16, 12], "faces": {d: f("#hub", [0, 0, 2, 2]) for d in ("north", "south", "east", "west", "up")}},
    ]
    write_json(os.path.join(m, "item", "key_cutter.json"), {
        "parent": "minecraft:block/block",
        "render_type": "minecraft:cutout",
        "textures": textures,
        "elements": vanilla_anvil_elements() + saw_supports() + item_saw,
    })
    write_json(os.path.join(bs, "key_cutter.json"), anvil_states(ml("block/key_cutter/block")))

    # Four à moufle : boîtier en cuivre de Create + bouche de four façon Minecraft (calque transparent)
    for lit in (False, True):
        name = "muffle_furnace_on" if lit else "muffle_furnace"
        casing = "create:block/copper_casing"
        cube = {d: f("#casing", cull=d) for d in ("north", "south", "east", "west", "up", "down")}
        write_json(os.path.join(m, "block", f"{name}.json"), {
            "parent": "minecraft:block/block",
            "render_type": "minecraft:cutout",
            "textures": {
                "particle": casing,
                "casing": casing,
                "front": ml("block/muffle_furnace_front_on" if lit else "block/muffle_furnace_front"),
                "vent": ml("block/muffle_furnace_top"),
            },
            "elements": [
                {"from": [0, 0, 0], "to": [16, 16, 16], "faces": cube},
                {"from": [0, 0, -0.01], "to": [16, 16, -0.01], "faces": {"north": f("#front", cull="north")}},
                {"from": [0, 16.01, 0], "to": [16, 16.01, 16], "faces": {"up": f("#vent", cull="up")}},
            ],
        })
    variants = {}
    for fa, y in FACING_Y.items():
        for lit in ("false", "true"):
            v = {"model": ml("block/muffle_furnace_on" if lit == "true" else "block/muffle_furnace")}
            if y:
                v["y"] = y
            variants[f"facing={fa},lit={lit}"] = v
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
    # Avec une tapisserie : le bois de la texture est transparent, un fond en cuir est caché au milieu de
    # l'épaisseur (au cas où le rendu de la tapisserie n'est pas dessiné, de très loin), et le code dessine la
    # tapisserie avec ses motifs juste derrière chaque face.
    for stage in STAGES:
        prefix = f"{stage}_" if stage else ""
        for part in parts:
            half = "bottom" if part.startswith("bottom") else "top"
            tex = "#" + half
            faces = {f: dict(v, texture=tex) for f, v in VANILLA_DOOR_FACES[part].items()}
            write_json(os.path.join(m, "block", f"{prefix}armored_copper_door_{part}_tapestry.json"), {
                "ambientocclusion": False,
                "render_type": "minecraft:cutout",
                "textures": {
                    "particle": ml(f"block/{prefix}armored_copper_door_{half}"),
                    half: ml(f"block/{prefix}armored_copper_door_{half}_tapestry"),
                    "leather": ml("block/tapestry"),
                },
                "elements": [
                    {"from": [0, 0, 0], "to": [3, 16, 16], "faces": faces},
                    {"from": [1.5, 0, 0.5], "to": [1.5, 16, 15.5], "faces": {
                        "west": {"uv": [0.5, 0, 15.5, 16], "texture": "#leather"},
                        "east": {"uv": [0.5, 0, 15.5, 16], "texture": "#leather"},
                    }},
                ],
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
                for tapestry in (False, True):
                    key = (f"facing={facing},half={half},hinge={hinge},open={str(opened).lower()},"
                           f"oxidation={oxidation},tapestry={str(tapestry).lower()},visible=true")
                    v = {"model": model + ("_tapestry" if tapestry else "")}
                    if y:
                        v["y"] = y
                    variants[key] = v
    # Pendant l'animation d'ouverture, le modèle fixe est caché (le rendu animé dessine la porte)
    variants["visible=false"] = {"model": ml("block/armored_door_hidden")}
    write_json(os.path.join(m, "block", "armored_door_hidden.json"),
               {"textures": {"particle": ml("block/armored_copper_door_bottom")}})
    write_json(os.path.join(bs, "armored_copper_door.json"), {"variants": variants})

    # Tapisserie : rendu par le code (comme une bannière) ; le modèle sert aux particules et à l'icône dynamique
    write_json(os.path.join(bs, "tapestry.json"), {"variants": {"": {"model": ml("block/tapestry")}}})
    write_json(os.path.join(m, "block", "tapestry.json"), {"textures": {"particle": ml("block/tapestry")}})
    write_json(os.path.join(m, "item", "tapestry.json"), {
        "parent": "minecraft:builtin/entity",
        "gui_light": "front",
        "textures": {"particle": ml("block/tapestry")},
        "display": {
            "gui": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
            "head": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
            "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 2.5, 1], "scale": [0.5, 0.5, 0.5]},
            "thirdperson_lefthand": {"rotation": [0, 90, 0], "translation": [0, 2.5, 1], "scale": [0.5, 0.5, 0.5]},
            "firstperson_righthand": {"rotation": [0, 70, 0], "translation": [1, 2, 0], "scale": [0.55, 0.55, 0.55]},
            "firstperson_lefthand": {"rotation": [0, 70, 0], "translation": [1, 2, 0], "scale": [0.55, 0.55, 0.55]},
        },
    })

    # Objets "plats"
    flat = ["pressed_plank", "compressed_wood", "mold", "saw_blade", "blank_key", "copper_key", "armored_copper_door"]
    flat += [f"{metal}_bar" for metal in METALS]
    # Seaux : celui de Minecraft, avec le métal en fusion dedans
    for metal in METALS:
        write_json(os.path.join(m, "item", f"molten_{metal}_bucket.json"), {
            "parent": "neoforge:item/bucket_drip",
            "loader": "neoforge:fluid_container",
            "fluid": ml(f"molten_{metal}"),
        })
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
            ("Pressing any plank gives a Pressed Plank", "La presse aplatit n'importe quelle planche"),
            ("4 Pressed Planks compacted in a Basin = 1 Compressed Wood",
             "4 planches pressées compactées dans un bassin = 1 bois compressé"),
        ],
    ),
    "muffle_furnace": (
        ("The Muffle Furnace", "Le four à moufle"),
        [
            ("Needs a lit Blaze Burner below", "Il lui faut un Brûleur à Blaze allumé dessous"),
            ("Right-click with ingots to load them", "Clic droit avec des lingots pour les charger"),
            ("1 ingot = 90 mB of molten metal", "1 lingot = 90 mB de métal en fusion"),
            ("Collect it with a Bucket or pipes", "Récupérez-le au seau ou avec des tuyaux"),
            ("Superheated: twice as fast", "Surchauffé : deux fois plus rapide"),
        ],
    ),
    "casting_anvil": (
        ("The Casting Anvil", "L'enclume de moulage"),
        [
            ("Right-click to open it", "Clic droit pour l'ouvrir"),
            ("Bucket of molten metal on the left", "Seau de métal en fusion à gauche"),
            ("90 mB = 1 bar, out on the right", "90 mB = 1 barre, qui sort à droite"),
            ("Pipes and funnels work too", "Les tuyaux et les entonnoirs marchent aussi"),
        ],
    ),
    "key_cutter": (
        ("The Key Cutter", "La machine à tailler les clés"),
        [
            ("Needs rotation through its shaft", "Il lui faut de la rotation par l'axe"),
            ("Blank Key on the left, then pick the key", "Clé vierge à gauche, puis choisissez la clé"),
            ("Every cut key is unique", "Chaque clé taillée est unique"),
            ("Faster rotation, faster cutting", "Plus ça tourne vite, plus ça taille vite"),
        ],
    ),
    "armored_door": (
        ("The Armored Copper Door", "La porte en cuivre blindée"),
        [
            ("No hand, no redstone: only a key", "Ni main, ni redstone : seulement une clé"),
            ("The first key used becomes its key", "La première clé utilisée devient sa clé"),
            ("Other keys are refused", "Les autres clés sont refusées"),
            ("Breaking it resets the lock (42 s with an Iron Axe)",
             "La casser remet la serrure à zéro (42 s à la hache en fer)"),
            ("It oxidizes like copper", "Elle s'oxyde comme le cuivre"),
            ("Honeycomb waxes it, an Axe scrapes it", "Le miel la cire, la hache la gratte"),
            ("A Tapestry replaces the wood", "Une tapisserie remplace le bois"),
            ("Shears take it back", "Des cisailles la reprennent"),
        ],
    ),
}

# Sons : nom de l'événement -> (fichiers, sous-titre EN, sous-titre FR)
SOUNDS = {
    "door.unlock": (["door_unlock"], "Lock opens", "Serrure qui s'ouvre"),
    "door.lock": (["door_lock"], "Lock closes", "Serrure qui se ferme"),
    "door.locked": (["door_locked"], "Door is locked", "Porte verrouillée"),
    "key.bind": (["key_bind1", "key_bind2"], "Key fits the lock", "La clé s'adapte à la serrure"),
    "key_cutter.cut": (["key_cutter_cut1", "key_cutter_cut2"], "Key Cutter grinds", "La machine taille une clé"),
    "key_cutter.done": (["key_cutter_done"], "Key cut", "Clé taillée"),
    "casting_anvil.hiss": (["casting_hiss1", "casting_hiss2", "casting_hiss3"], "Metal cools down", "Le métal refroidit"),
    "casting_anvil.clink": (["casting_clink"], "Bar cast", "Barre coulée"),
    "muffle_furnace.load": (["muffle_furnace_load"], "Ingots loaded", "Lingots chargés"),
}


def sounds_json():
    out = {}
    for event, (files, _, _) in SOUNDS.items():
        out[event] = {
            "subtitle": f"subtitles.{MOD}.{event}",
            "sounds": [ml(name) for name in files],
        }
    write_json(os.path.join(ASSETS, "sounds.json"), out)


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
    add(f"block.{MOD}.tapestry", "Leather Tapestry", "Tapisserie en cuir")

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
    add(f"message.{MOD}.door.has_tapestry", "This door already has a tapestry: Shears take it off",
        "Cette porte a déjà une tapisserie : des cisailles la retirent")

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
           "_S'oxyde_ comme le cuivre. Un _rayon de miel_ garde son apparence, une _hache_ la gratte.")),
         (("When R-Clicked with a Tapestry", "Clic droit avec une tapisserie"),
          ("The _tapestry_ replaces the _wood_, on both sides. _Shears_ take it back.",
           "La _tapisserie_ remplace le _bois_, des deux côtés. Des _cisailles_ la reprennent."))])
    tip(f"block.{MOD}.muffle_furnace",
        ("_Melts ingots_ into _molten metal_ when placed on a _lit Blaze Burner_.",
         "_Fait fondre les lingots_ en _métal en fusion_ quand il est posé sur un _Brûleur à Blaze allumé_."),
        [(("When R-Clicked", "Clic droit"),
          ("With _ingots_: loads them. With an empty _Bucket_: collects the molten metal.",
           "Avec des _lingots_ : les charge. Avec un _seau vide_ : récupère le métal en fusion."))])
    tip(f"block.{MOD}.casting_anvil",
        ("Casts _molten metal_ into _bars_. One bar = _90 mB_.",
         "Coule le _métal en fusion_ en _barres_. Une barre = _90 mB_."),
        [(("When R-Clicked", "Clic droit"),
          ("Opens the _interface_: molten metal _bucket_ on the left, _bar_ on the right.",
           "Ouvre l'_interface_ : _seau_ de métal en fusion à gauche, _barre_ à droite."))])
    tip(f"block.{MOD}.key_cutter",
        ("Cuts _Blank Keys_ into _unique keys_. Needs _rotational force_ from the back.",
         "Taille les _clés vierges_ en _clés uniques_. A besoin de _rotation_ par l'arrière."),
        [(("When R-Clicked", "Clic droit"),
          ("Opens the _interface_ where you _choose_ which key to cut.",
           "Ouvre l'_interface_ où l'on _choisit_ la clé à tailler."))])
    tip(f"block.{MOD}.tapestry",
        ("A _leather_ wall hanging. Decorate it in a _Loom_, just like a banner.",
         "Une tenture en _cuir_ à accrocher au mur. Se décore dans un _métier à tisser_, comme une bannière."),
        [(("When used on an Armored Door", "Sur une porte blindée"),
          ("Replaces the door's _wood_, with its patterns. _Shears_ take it back.",
           "Remplace le _bois_ de la porte, avec ses motifs. Des _cisailles_ la reprennent."))])
    tip(f"item.{MOD}.copper_key",
        ("Opens _Armored Copper Doors_. Every cut key is _unique_.",
         "Ouvre les _portes en cuivre blindées_. Chaque clé taillée est _unique_."), [])
    tip(f"item.{MOD}.blank_key",
        ("Cut it in a _Key Cutter_ to get a key.",
         "À tailler dans une _machine à tailler les clés_ pour obtenir une clé."), [])

    # Sous-titres des sons
    for event, (_, sub_en, sub_fr) in SOUNDS.items():
        add(f"subtitles.{MOD}.{event}", sub_en, sub_fr)

    # JEI
    add(f"jei.{MOD}.melting", "Melting", "Fonte")
    add(f"jei.{MOD}.casting", "Casting", "Moulage")
    add(f"jei.{MOD}.key_cutting", "Key Cutting", "Taille de clé")
    add(f"jei.{MOD}.heated", "Lit Blaze Burner", "Brûleur à Blaze allumé")
    add(f"jei.{MOD}.rotation", "Needs rotation", "Rotation nécessaire")
    add(f"jei.{MOD}.seconds", "%s s", "%s s")
    add(f"jei.{MOD}.info.armored_door",
        "Opens only with a cut key. The first key used on the door becomes its only key; breaking the door resets the lock. "
        "Takes 42 seconds to break with an Iron Axe. Oxidizes like copper: wax it with a Honeycomb. "
        "Right-click it with a Tapestry to replace its wood; Shears take the tapestry back.",
        "S'ouvre seulement avec une clé taillée. La première clé utilisée devient sa seule clé ; casser la porte remet "
        "la serrure à zéro. 42 secondes pour la casser à la hache en fer. S'oxyde comme le cuivre : un rayon de miel la cire. "
        "Clic droit avec une tapisserie pour remplacer son bois ; des cisailles reprennent la tapisserie.")
    add(f"jei.{MOD}.info.key",
        "Cut in a Key Cutter. Every cut key is unique: the first one used on an Armored Door becomes its key.",
        "Se taille dans la machine à tailler les clés. Chaque clé est unique : la première utilisée sur une porte "
        "blindée devient sa clé.")

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

    # Fonte et moulage
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
    shaped("tapestry", ["LLL", "LLL", "LLL"], {"L": item("minecraft:leather")}, ml("tapestry"))
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
    # Tapisserie : garde ses motifs et son nom, comme une bannière
    write_json(os.path.join(lt, "tapestry.json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1.0,
            "bonus_rolls": 0.0,
            "entries": [{
                "type": "minecraft:item",
                "name": ml("tapestry"),
                "functions": [{
                    "function": "minecraft:copy_components",
                    "source": "block_entity",
                    "include": ["minecraft:custom_name", "minecraft:item_name", "minecraft:hide_additional_tooltip",
                                "minecraft:banner_patterns", "minecraft:rarity"],
                }],
            }],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
        "random_sequence": ml("blocks/tapestry"),
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
    # machine longue nord-sud (lame au bout nord), l'axe la traverse d'ouest en est : moteur à l'ouest
    b[(2, 1, 2)] = (ml("key_cutter"), {"facing": "north"})
    b[(1, 1, 2)] = ("create:creative_motor", {"facing": "east"})
    write_structure("key_cutter", (5, 2, 5), b)

    b = floor()
    for pos in [(1, 1, 2), (1, 2, 2), (3, 1, 2), (3, 2, 2), (1, 3, 2), (2, 3, 2), (3, 3, 2)]:
        b[pos] = ("minecraft:stone_bricks", {})
    door = {"facing": "south", "hinge": "left", "open": "false", "powered": "false",
            "oxidation": "unaffected", "waxed": "false", "tapestry": "false", "visible": "true"}
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
    sounds_json()
    recipes()
    loot()
    tags()
    schematics()
    gametest_structure()
    print("Ressources générées dans", os.path.normpath(ROOT))


if __name__ == "__main__":
    main()

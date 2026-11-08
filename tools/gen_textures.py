#!/usr/bin/env python3
"""
Génère toutes les textures (pixel art 16x16, fluides animés, interfaces, logo) de Create: Locksmithing.

    python3 tools/gen_textures.py

Les PNG sont écrits dans src/main/resources/assets/create_locksmithing/textures/ (et logo.png).
Tout est dessiné ici, sans reprendre de texture de Minecraft ou de Create.
"""
import math
import os
import random

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
TEX = os.path.join(ROOT, "assets", "create_locksmithing", "textures")

T = (0, 0, 0, 0)


def rgb(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def save(img, *path):
    out = os.path.join(TEX, *path)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    img.save(out)


def save_mcmeta(text, *path):
    out = os.path.join(TEX, *path)
    with open(out, "w") as f:
        f.write(text)


# --------------------------------------------------------------------------------------------
# Palettes (du plus sombre au plus clair) : [contour, sombre, moyen, clair, reflet]
# --------------------------------------------------------------------------------------------
IRON = [rgb("2b2b30"), rgb("5a5a62"), rgb("8d8d96"), rgb("c4c4cc"), rgb("efeff4")]
GOLD = [rgb("5a3a06"), rgb("a8700f"), rgb("e0a92a"), rgb("f8d75a"), rgb("fff4b0")]
COPPER = [rgb("4a2216"), rgb("8f4329"), rgb("c4663f"), rgb("e48f62"), rgb("f9c39d")]
ZINC = [rgb("1f3833"), rgb("436b60"), rgb("7fa394"), rgb("b9dcc2"), rgb("e4fbe8")]
ANDESITE = [rgb("2e322d"), rgb("5c645a"), rgb("8a9387"), rgb("b4bcb0"), rgb("dde3d8")]
BRASS = [rgb("4d3209"), rgb("99681b"), rgb("d39b32"), rgb("f1c85e"), rgb("fff0b8")]
CAST_IRON = [rgb("17181c"), rgb("2f3138"), rgb("454852"), rgb("5f636e"), rgb("8a8f9b")]
WOOD = [rgb("3b2611"), rgb("6b4826"), rgb("98703f"), rgb("b98f57"), rgb("d8b47b")]
CHIP = [rgb("5a3c1c"), rgb("7d5a30"), rgb("a07a46"), rgb("c09a5e"), rgb("8a6a3c"), rgb("b48a50")]

# Cuivre de la porte, selon l'oxydation
COPPER_STAGES = {
    "": COPPER,
    "exposed": [rgb("4a2f25"), rgb("8a5d4b"), rgb("b08670"), rgb("cfa48a"), rgb("e8c9b2")],
    "weathered": [rgb("234035"), rgb("4d7a62"), rgb("6c9b7e"), rgb("8fbf9b"), rgb("c0e2c6")],
    "oxidized": [rgb("163b33"), rgb("2f7362"), rgb("4fa58c"), rgb("74c7aa"), rgb("b3ecd8")],
}

# Métaux en fusion : du plus froid (croûte) au plus chaud
MOLTEN = {
    "iron": [rgb("5a1a05"), rgb("a8360a"), rgb("e86a12"), rgb("ffa733"), rgb("ffe08a")],
    "gold": [rgb("6b3d00"), rgb("b87a05"), rgb("f2b81c"), rgb("ffe04f"), rgb("fffbc4")],
    "copper": [rgb("5c1408"), rgb("a8301a"), rgb("e05a33"), rgb("ff8f5c"), rgb("ffd0b0")],
    "zinc": [rgb("27333d"), rgb("4f6676"), rgb("86a3b3"), rgb("bcd8e2"), rgb("eefaff")],
}
METALS = {"iron": IRON, "gold": GOLD, "copper": COPPER, "zinc": ZINC}


# --------------------------------------------------------------------------------------------
# Outils de dessin
# --------------------------------------------------------------------------------------------
def blank(w=16, h=16):
    return Image.new("RGBA", (w, h), T)


def mask_from(rows):
    return {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c != "."}


def shade(img, mask, pal, outline=True, noise=0.0, seed=0, highlight_line=None):
    """Remplit un masque avec un ombrage simple : bord haut/gauche clair, bas/droite sombre, contour."""
    rnd = random.Random(seed)
    px = img.load()
    for (x, y) in mask:
        up = (x, y - 1) in mask
        left = (x - 1, y) in mask
        down = (x, y + 1) in mask
        right = (x + 1, y) in mask
        if not up or not left:
            c = pal[3]
        elif not down or not right:
            c = pal[1]
        else:
            c = pal[2]
            if noise and rnd.random() < noise:
                c = pal[rnd.choice([1, 3])]
        px[x, y] = c
    if highlight_line:
        for (x, y) in highlight_line:
            if (x, y) in mask:
                px[x, y] = pal[4]
    if outline:
        w, h = img.size
        for (x, y) in list(mask):
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in mask and px[nx, ny][3] == 0:
                    px[nx, ny] = pal[0]
    return img


def circle_mask(cx, cy, r, inner=0.0):
    m = set()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if inner <= d <= r:
                m.add((x, y))
    return m


def rect(x0, y0, x1, y1):
    """Rectangle inclusif."""
    return {(x, y) for x in range(x0, x1 + 1) for y in range(y0, y1 + 1)}


# --------------------------------------------------------------------------------------------
# Objets
# --------------------------------------------------------------------------------------------
BAR_MASK = mask_from([
    "................",
    "................",
    "...........###..",
    "..........####..",
    ".........####...",
    "........####....",
    ".......####.....",
    "......####......",
    ".....####.......",
    "....####........",
    "...####.........",
    "..####..........",
    "..###...........",
    "................",
    "................",
    "................",
])
BAR_SHINE = {(x, 13 - x) for x in range(3, 13)}


def bar(pal):
    img = shade(blank(), BAR_MASK, pal, highlight_line=BAR_SHINE)
    px = img.load()
    # extrémités biseautées un peu plus sombres
    for p in [(2, 12), (3, 12), (13, 2), (12, 2)]:
        if p in BAR_MASK:
            px[p] = pal[2]
    return img


def pressed_plank():
    img = blank()
    px = img.load()
    top = rect(1, 5, 14, 7)
    front = rect(1, 8, 14, 10)
    for (x, y) in top:
        px[x, y] = WOOD[3] if (x * 3 + y) % 7 else WOOD[2]
    for (x, y) in front:
        px[x, y] = WOOD[1] if y == 10 else WOOD[2]
    # veines du bois écrasées
    for x in range(2, 14, 3):
        px[x, 6] = WOOD[2]
        px[x + 1, 6] = WOOD[2]
    for x in range(1, 15):
        px[x, 5] = WOOD[4] if x % 4 else WOOD[3]
    # marques de la presse
    for x in (4, 9):
        px[x, 9] = WOOD[1]
    shade_outline(img, top | front, WOOD[0])
    return img


def shade_outline(img, mask, color):
    px = img.load()
    for (x, y) in list(mask):
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in mask and px[nx, ny][3] == 0:
                px[nx, ny] = color


def chips(px, region, seed, palette=CHIP):
    rnd = random.Random(seed)
    for (x, y) in region:
        px[x, y] = rnd.choice(palette)


def compressed_wood():
    img = blank()
    px = img.load()
    top = rect(2, 3, 13, 5)
    front = rect(2, 6, 13, 12)
    chips(px, top, 11, [CHIP[2], CHIP[3], CHIP[5], WOOD[4]])
    chips(px, front, 12, CHIP[:5])
    for x in range(2, 14):
        px[x, 6] = WOOD[1]
        px[x, 12] = WOOD[1]
    for y in range(6, 13):
        px[13, y] = WOOD[1]
    shade_outline(img, top | front, WOOD[0])
    return img


def mold():
    img = blank()
    px = img.load()
    top = rect(1, 4, 14, 8)
    front = rect(1, 9, 14, 12)
    for (x, y) in top:
        px[x, y] = ANDESITE[3]
    for (x, y) in front:
        px[x, y] = ANDESITE[2] if y < 12 else ANDESITE[1]
    # fond en fer (la plaque)
    for x in range(1, 15):
        px[x, 12] = IRON[1]
    # creux en forme de barre
    for (x, y) in rect(4, 5, 11, 7):
        px[x, y] = ANDESITE[0] if y == 5 else rgb("1b1d1a")
    for x in range(4, 12):
        px[x, 7] = rgb("262923")
    for x in range(1, 15):
        px[x, 4] = ANDESITE[4]
    shade_outline(img, top | front, ANDESITE[0])
    return img


def saw_blade():
    img = blank()
    px = img.load()
    disc = circle_mask(8, 8, 6.2, 1.6)
    teeth = set()
    for i in range(12):
        a = i / 12 * math.tau
        tx = int(round(8 + math.cos(a) * 7.0 - 0.5))
        ty = int(round(8 + math.sin(a) * 7.0 - 0.5))
        teeth.add((tx, ty))
    full = disc | teeth
    shade(img, full, ANDESITE, noise=0.08, seed=5)
    # anneau brillant et centre
    for (x, y) in circle_mask(8, 8, 4.2, 3.4):
        if (x, y) in full:
            px[x, y] = ANDESITE[4] if x < 8 else ANDESITE[3]
    for (x, y) in circle_mask(8, 8, 2.4, 1.6):
        px[x, y] = BRASS[2] if (x + y) % 2 else BRASS[3]
    for (x, y) in teeth:
        px[x, y] = IRON[3]
    return img


def key(pal, cut):
    bow = circle_mask(4.5, 7.5, 3.6, 1.5)
    blade = rect(7, 6, 14, 8)
    shape = bow | blade
    if cut:
        shape |= rect(9, 9, 9, 10) | rect(11, 9, 12, 9) | rect(14, 9, 14, 10)
    img = shade(blank(), shape, pal, highlight_line={(x, 6) for x in range(8, 14)})
    return img


def blank_key():
    # anneau en alliage d'andésite, tige en barres de fer, pas encore de dents
    bow = circle_mask(4.5, 7.5, 3.6, 1.5)
    blade = rect(7, 6, 14, 8)
    img = blank()
    shade(img, bow, ANDESITE, outline=False)
    shade(img, blade, IRON, outline=False, highlight_line={(x, 6) for x in range(8, 14)})
    shade_outline(img, bow | blade, IRON[0])
    return img


# --------------------------------------------------------------------------------------------
# Porte en cuivre blindée
# --------------------------------------------------------------------------------------------
def door_panel(px, region, seed):
    rnd = random.Random(seed)
    for (x, y) in region:
        px[x, y] = rnd.choice(CHIP[:5])


def door_half(pal, top):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    door_panel(px, rect(0, 0, 15, 15), 21 if top else 22)
    # cadre en barres de cuivre (2 px)
    frame = rect(0, 0, 1, 15) | rect(14, 0, 15, 15)
    frame |= rect(0, 0, 15, 1) if top else rect(0, 14, 15, 15)
    bands = rect(0, 8, 15, 9) if top else rect(0, 5, 15, 6)
    for (x, y) in frame | bands:
        edge_light = x in (0, 14) or y in (0, 5, 8, 14)
        px[x, y] = pal[3] if edge_light else pal[2]
    for (x, y) in frame | bands:
        if x in (1, 15) or y in (1, 6, 9, 15):
            px[x, y] = pal[1]
    # séparation verticale au milieu (le craft a le bois au centre)
    for y in range(16):
        if px[7, y] in CHIP[:5]:
            px[7, y] = WOOD[0]
    # rivets
    rivets = [(3, 7), (12, 7), (3, 10), (12, 10)] if top else [(3, 3), (12, 3), (3, 11), (12, 11)]
    for (x, y) in rivets:
        px[x, y] = pal[4]
        px[x + 1, y + 1] = pal[0]
    # serrure (sur la moitié basse, côté opposé aux gonds)
    if not top:
        for (x, y) in rect(10, 0, 13, 3):
            px[x, y] = pal[2]
        for (x, y) in [(10, 0), (13, 0), (10, 3), (13, 3)]:
            px[x, y] = pal[1]
        px[11, 1] = rgb("0e0e10")
        px[12, 1] = rgb("0e0e10")
        px[11, 2] = rgb("0e0e10")
        px[10, 1] = pal[4]
    return img


def door_item():
    img = blank()
    px = img.load()
    shape = rect(4, 0, 11, 15)
    rnd = random.Random(3)
    for (x, y) in shape:
        px[x, y] = rnd.choice(CHIP[:5])
    for y in range(16):
        px[4, y] = COPPER[3]
        px[11, y] = COPPER[1]
    for x in range(4, 12):
        px[x, 0] = COPPER[3]
        px[x, 15] = COPPER[1]
        px[x, 5] = COPPER[2]
        px[x, 10] = COPPER[2]
    for y in range(1, 15):
        if px[7, y] in CHIP[:5]:
            px[7, y] = WOOD[0]
    px[10, 7] = rgb("0e0e10")
    px[9, 7] = COPPER[4]
    px[5, 2] = COPPER[4]
    px[10, 13] = COPPER[4]
    return img


# --------------------------------------------------------------------------------------------
# Blocs
# --------------------------------------------------------------------------------------------
STONE = [rgb("1c1c1f"), rgb("2c2c31"), rgb("3a3a40"), rgb("4a4a52"), rgb("5c5c66")]
FIRE = [rgb("7a2a05"), rgb("c9520e"), rgb("f08a1c"), rgb("ffc23a"), rgb("fff2a0")]


def furnace_front_overlay(lit):
    """Bouche de four façon Minecraft, posée par-dessus le boîtier en cuivre de Create (le reste est transparent)."""
    img = blank()
    px = img.load()
    # linteau en pierre
    for x in range(3, 13):
        px[x, 3] = STONE[4]
        px[x, 4] = STONE[3]
    # ouverture voûtée
    mouth = rect(4, 6, 11, 12) | rect(5, 5, 10, 5)
    for (x, y) in mouth:
        px[x, y] = STONE[0]
    for y in range(5, 13):
        px[3, y] = STONE[3]
        px[12, y] = STONE[2]
    px[4, 5] = STONE[3]
    px[11, 5] = STONE[2]
    # grille
    for x in (6, 9):
        for y in range(6, 9):
            px[x, y] = STONE[2]
    if lit:
        flames = [
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "......2....2....",
            ".....232..23....",
            "....233322333...",
            "....334343443...",
            "....3444444443..",
            "....44444444....",
        ]
        for y, row in enumerate(flames):
            for x, c in enumerate(row):
                if c != "." and (x, y) in mouth:
                    px[x, y] = FIRE[int(c)]
        for x in range(4, 12):
            px[x, 12] = FIRE[4] if x % 3 else FIRE[3]
    # sole en pierre
    for x in range(3, 13):
        px[x, 13] = STONE[4]
    return img


def furnace_top_overlay():
    """Grille d'aération sur le dessus."""
    img = blank()
    px = img.load()
    for y in (4, 7, 10):
        for x in range(4, 12):
            px[x, y] = STONE[0]
            px[x, y + 1] = STONE[3]
    return img


def mold_side():
    """Le moule (alliage d'andésite), incrusté dans l'enclume."""
    img = Image.new("RGBA", (16, 16), ANDESITE[2])
    px = img.load()
    rnd = random.Random(61)
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            px[x, y] = ANDESITE[3] if r < 0.18 else ANDESITE[1] if r < 0.3 else ANDESITE[2]
    for i in range(16):
        px[i, 0] = ANDESITE[4]
    return img


def mold_floor():
    """Fond du moule : la plaque de fer de la recette."""
    img = Image.new("RGBA", (16, 16), IRON[2])
    px = img.load()
    rnd = random.Random(62)
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.15:
                px[x, y] = IRON[1]
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        px[x, y] = IRON[3]
    return img


def saw_disc():
    """Lame vue de face, transparente autour (rendu cutout)."""
    img = blank()
    px = img.load()
    disc = circle_mask(8, 8, 7.0, 0)
    for (x, y) in disc:
        d = math.hypot(x + 0.5 - 8, y + 0.5 - 8)
        if d > 6.2:
            # dents : une sur deux autour du bord
            a = math.atan2(y + 0.5 - 8, x + 0.5 - 8)
            if int((a + math.pi) / math.tau * 20) % 2 == 0:
                px[x, y] = IRON[3]
            continue
        px[x, y] = ANDESITE[3] if (x + 2 * y) % 5 else ANDESITE[2]
    for (x, y) in circle_mask(8, 8, 6.3, 5.4):
        px[x, y] = ANDESITE[1]
    for (x, y) in circle_mask(8, 8, 3.2, 2.4):
        px[x, y] = ANDESITE[4]
    # rayons pour bien voir la rotation
    for i in range(4):
        a = i / 4 * math.tau + 0.4
        for r in range(3, 6):
            x = int(8 + math.cos(a) * r)
            y = int(8 + math.sin(a) * r)
            px[x, y] = ANDESITE[1]
    return img


def hub():
    img = Image.new("RGBA", (16, 16), BRASS[2])
    px = img.load()
    for i in range(16):
        px[i, 0] = BRASS[3]
        px[0, i] = BRASS[3]
        px[i, 15] = BRASS[1]
        px[15, i] = BRASS[1]
    for (x, y) in circle_mask(8, 8, 3, 0):
        px[x, y] = BRASS[1]
    return img



# --------------------------------------------------------------------------------------------
# Tapisserie en cuir
# --------------------------------------------------------------------------------------------
LEATHER = [rgb("3b2010"), rgb("5e3519"), rgb("7d4a24"), rgb("965c30"), rgb("b0763f"), rgb("c99257")]
STITCH = rgb("d8b98a")
DARK_WOOD = [rgb("2a1b0e"), rgb("3e2814"), rgb("54371c"), rgb("6b4826")]


def leather_fill(px, x0, y0, x1, y1, seed):
    """Grain du cuir : fond brun avec de petites marbrures."""
    n1 = value_noise(seed, size=64, cells=8)
    rnd = random.Random(seed)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            v = n1(x % 64, y % 64)
            idx = 2 + (1 if v > 0.55 else 0) + (1 if v > 0.75 else 0) - (1 if v < 0.25 else 0)
            if rnd.random() < 0.06:
                idx = max(1, idx - 1)
            px[x, y] = LEATHER[idx]


def stitches(px, x0, y0, x1, y1):
    """Couture tout autour, un point sur deux, à 1 px du bord."""
    for x in range(x0 + 1, x1):
        if x % 2 == 0:
            px[x, y0 + 1] = STITCH
            px[x, y1 - 1] = STITCH
    for y in range(y0 + 1, y1):
        if y % 2 == 0:
            px[x0 + 1, y] = STITCH
            px[x1 - 1, y] = STITCH
    for x in range(x0, x1 + 1):
        px[x, y0] = LEATHER[0]
        px[x, y1] = LEATHER[0]
    for y in range(y0, y1 + 1):
        px[x0, y] = LEATHER[0]
        px[x1, y] = LEATHER[0]


def tapestry_leather():
    """Texture 64x64 au format de la bannière de Minecraft (cuir sur le drapeau, bois sur la barre)."""
    img = Image.new("RGBA", (64, 64), T)
    px = img.load()
    # Drapeau 20x40x1 en (0,0) : dessus/dessous, côtés, face avant (1,1)-(20,40), face arrière (22,1)-(41,40)
    leather_fill(px, 0, 0, 41, 40, 71)
    stitches(px, 1, 1, 20, 40)
    stitches(px, 22, 1, 41, 40)
    # rangée d'œillets en haut (où le cuir est accroché)
    for x in range(3, 20, 4):
        px[x, 3] = DARK_WOOD[0]
        px[x + 23 - 2, 3] = DARK_WOOD[0]
    # Barre 20x2x2 en (0,42) : bois sombre avec des embouts en fer
    for y in range(42, 46):
        for x in range(0, 44):
            px[x, y] = DARK_WOOD[1 + ((x * 3 + y) % 3 == 0)]
    for x in range(0, 44):
        px[x, 44] = DARK_WOOD[3]
    for x in (2, 3, 20, 21, 24, 25, 42, 43):
        for y in range(44, 46):
            px[x, y] = IRON[2]
    return img


def tapestry_particle():
    img = Image.new("RGBA", (16, 16), LEATHER[2])
    px = img.load()
    leather_fill(px, 0, 0, 15, 15, 72)
    return img

# --------------------------------------------------------------------------------------------
# Fluides animés
# --------------------------------------------------------------------------------------------
def value_noise(seed, size=16, cells=4):
    rnd = random.Random(seed)
    grid = [[rnd.random() for _ in range(cells)] for _ in range(cells)]

    def sample(x, y):
        gx = x / size * cells
        gy = y / size * cells
        x0 = int(math.floor(gx)) % cells
        y0 = int(math.floor(gy)) % cells
        x1 = (x0 + 1) % cells
        y1 = (y0 + 1) % cells
        fx = gx - math.floor(gx)
        fy = gy - math.floor(gy)
        fx = fx * fx * (3 - 2 * fx)
        fy = fy * fy * (3 - 2 * fy)
        a = grid[y0][x0] * (1 - fx) + grid[y0][x1] * fx
        b = grid[y1][x0] * (1 - fx) + grid[y1][x1] * fx
        return a * (1 - fy) + b * fy

    return sample


def fluid_frames(ramp, frames, flowing):
    n1 = value_noise(101, cells=4)
    n2 = value_noise(202, cells=8)
    img = Image.new("RGBA", (16, 16 * frames), T)
    px = img.load()
    for f in range(frames):
        t = f / frames
        for y in range(16):
            for x in range(16):
                if flowing:
                    sx, sy = x, (y - t * 16) % 16
                else:
                    sx = (x + math.sin(t * math.tau) * 2) % 16
                    sy = (y + t * 16 * 0.5) % 16
                v = 0.65 * n1(sx, sy) + 0.35 * n2((sx * 1.3) % 16, (sy + t * 8) % 16)
                v = 0.5 + 0.5 * math.sin((v + t) * math.tau)
                idx = min(4, int(v * 5))
                px[x, y + 16 * f] = ramp[idx]
    return img


# --------------------------------------------------------------------------------------------
# Interfaces (176x166 dans une texture 256x256)
# --------------------------------------------------------------------------------------------
GUI_BG = (198, 198, 198, 255)
GUI_LIGHT = (255, 255, 255, 255)
GUI_DARK = (85, 85, 85, 255)
GUI_SLOT = (139, 139, 139, 255)
GUI_EDGE = (0, 0, 0, 255)


def gui_panel(img, x0, y0, w, h):
    px = img.load()
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            px[x, y] = GUI_BG
    for x in range(x0 + 2, x0 + w - 2):
        px[x, y0] = GUI_EDGE
        px[x, y0 + 1] = GUI_LIGHT
        px[x, y0 + 2] = GUI_LIGHT
        px[x, y0 + h - 1] = GUI_EDGE
        px[x, y0 + h - 2] = GUI_DARK
        px[x, y0 + h - 3] = GUI_DARK
    for y in range(y0 + 2, y0 + h - 2):
        px[x0, y] = GUI_EDGE
        px[x0 + 1, y] = GUI_LIGHT
        px[x0 + 2, y] = GUI_LIGHT
        px[x0 + w - 1, y] = GUI_EDGE
        px[x0 + w - 2, y] = GUI_DARK
        px[x0 + w - 3, y] = GUI_DARK
    for (x, y) in [(x0 + 1, y0 + 1), (x0 + w - 2, y0 + h - 2)]:
        px[x, y] = GUI_EDGE
    px[x0 + 1, y0 + h - 2] = GUI_EDGE
    px[x0 + w - 2, y0 + 1] = GUI_EDGE
    # coins arrondis
    for (x, y) in [(x0, y0), (x0 + 1, y0), (x0, y0 + 1), (x0 + w - 1, y0), (x0 + w - 2, y0), (x0 + w - 1, y0 + 1),
                   (x0, y0 + h - 1), (x0 + 1, y0 + h - 1), (x0, y0 + h - 2), (x0 + w - 1, y0 + h - 1),
                   (x0 + w - 2, y0 + h - 1), (x0 + w - 1, y0 + h - 2)]:
        px[x, y] = T
    px[x0 + 2, y0 + 2] = GUI_LIGHT


def gui_slot(img, x, y, w=18, h=18):
    """Cadre d'emplacement : (x, y) = coin haut-gauche du cadre."""
    px = img.load()
    for i in range(w):
        px[x + i, y] = GUI_DARK
        px[x + i, y + h - 1] = GUI_LIGHT
    for j in range(h):
        px[x, y + j] = GUI_DARK
        px[x + w - 1, y + j] = GUI_LIGHT
    px[x + w - 1, y] = GUI_SLOT
    px[x, y + h - 1] = GUI_SLOT
    for j in range(1, h - 1):
        for i in range(1, w - 1):
            px[x + i, y + j] = GUI_SLOT


def gui_player_inventory(img):
    for row in range(3):
        for col in range(9):
            gui_slot(img, 7 + col * 18, 83 + row * 18)
    for col in range(9):
        gui_slot(img, 7 + col * 18, 141)


ARROW = mask_from([
    "..............#.........",
    "..............##........",
    "..............###.......",
    "..............####......",
    "..............#####.....",
    "#####################...",
    "######################..",
    "#######################.",
    "######################..",
    "#####################...",
    "..............#####.....",
    "..............####......",
    "..............###.......",
    "..............##........",
    "..............#.........",
    "........................",
    "........................",
])


def draw_arrow(img, ox, oy, color, mask=ARROW, width=24):
    px = img.load()
    for (x, y) in mask:
        if x < width:
            px[ox + x, oy + y] = color


def scale_mask(mask, sx, sy, w, h):
    out = set()
    for (x, y) in mask:
        nx, ny = int(x * sx), int(y * sy)
        if nx < w and ny < h:
            out.add((nx, ny))
    return out


def casting_gui():
    img = Image.new("RGBA", (256, 256), T)
    gui_panel(img, 0, 0, 176, 166)
    gui_player_inventory(img)
    px = img.load()
    # le moule (silhouette d'enclume) autour de l'emplacement du seau
    for (x, y) in rect(22, 26, 51, 56):
        px[x, y] = (150, 152, 158, 255)
    for x in range(22, 52):
        px[x, 26] = (220, 222, 228, 255)
        px[x, 56] = (70, 72, 78, 255)
    for y in range(26, 57):
        px[22, y] = (220, 222, 228, 255)
        px[51, y] = (70, 72, 78, 255)
    for (x, y) in rect(28, 57, 45, 62):
        px[x, y] = (120, 122, 128, 255)
    gui_slot(img, 34, 34)
    # jauge de fluide (fond sombre)
    gx, gy, gw, gh = 58, 18, 14, 50
    for (x, y) in rect(gx - 1, gy - 1, gx + gw, gy + gh):
        px[x, y] = GUI_DARK if (x == gx - 1 or y == gy - 1) else GUI_LIGHT
    for (x, y) in rect(gx, gy, gx + gw - 1, gy + gh - 1):
        px[x, y] = (55, 55, 60, 255)
    # flèche vide
    draw_arrow(img, 82, 34, GUI_SLOT)
    # grand emplacement de sortie (26x26)
    gui_slot(img, 115, 30, 26, 26)
    # sprites : flèche pleine (176,0) et graduations (176,17)
    draw_arrow(img, 176, 0, (255, 140, 40, 255))
    for i in range(0, gh, 10):
        for x in range(176, 176 + 5):
            px[x, 17 + i] = (210, 210, 220, 200)
        for x in range(176 + gw - 5, 176 + gw):
            px[x, 17 + i] = (210, 210, 220, 200)
    return img


def key_cutter_gui():
    img = Image.new("RGBA", (256, 256), T)
    gui_panel(img, 0, 0, 176, 166)
    gui_player_inventory(img)
    px = img.load()
    gui_slot(img, 19, 32)
    # zone de choix des clés (4 x 3 boutons de 16 x 18)
    for (x, y) in rect(51, 14, 116, 69):
        px[x, y] = GUI_DARK if (x == 51 or y == 14) else GUI_LIGHT if (x == 116 or y == 69) else (120, 120, 120, 255)
    # flèche vide 20x15
    small = scale_mask(ARROW, 20 / 24, 15 / 17, 20, 15)
    draw_arrow(img, 118, 34, GUI_SLOT, small, 20)
    gui_slot(img, 142, 32)
    # sprites des boutons : normal (176,0), choisi (176,18), survolé (176,36)
    for i, (face, light, dark) in enumerate([
        ((198, 198, 198, 255), GUI_LIGHT, GUI_DARK),
        ((120, 150, 200, 255), (180, 210, 255, 255), (50, 70, 120, 255)),
        ((220, 220, 220, 255), GUI_LIGHT, GUI_DARK),
    ]):
        oy = i * 18
        for (x, y) in rect(176, oy, 191, oy + 17):
            px[x, y] = face
        for x in range(176, 192):
            px[x, oy] = light
            px[x, oy + 17] = dark
        for y in range(oy, oy + 18):
            px[176, y] = light
            px[191, y] = dark
    # flèche pleine (192,0)
    draw_arrow(img, 192, 0, (255, 170, 60, 255), small, 20)
    return img


# --------------------------------------------------------------------------------------------
# Logo
# --------------------------------------------------------------------------------------------
def logo():
    small = Image.new("RGBA", (32, 32), (36, 30, 26, 255))
    px = small.load()
    rnd = random.Random(7)
    # fond : briques sombres
    for y in range(32):
        for x in range(32):
            row = y // 4
            off = 0 if row % 2 == 0 else 4
            if y % 4 == 3 or (x + off) % 8 == 7:
                px[x, y] = (24, 20, 18, 255)
            else:
                px[x, y] = rnd.choice([(58, 46, 40, 255), (64, 50, 42, 255), (52, 42, 36, 255)])
    # porte
    for y in range(4, 30):
        for x in range(9, 23):
            px[x, y] = rnd.choice(CHIP[:5])
    for y in range(4, 30):
        for x in (9, 10):
            px[x, y] = COPPER[3]
        for x in (21, 22):
            px[x, y] = COPPER[1]
    for x in range(9, 23):
        for y in (4, 5):
            px[x, y] = COPPER[3]
        for y in (12, 13, 21, 22):
            px[x, y] = COPPER[2]
        for y in (28, 29):
            px[x, y] = COPPER[1]
    for y in range(6, 28):
        if px[15, y] in CHIP[:5]:
            px[15, y] = WOOD[0]
    px[19, 16] = (10, 10, 12, 255)
    px[19, 17] = (10, 10, 12, 255)
    # clé dorée en bas à droite
    keyimg = key(GOLD, True)
    k = keyimg.load()
    for y in range(16):
        for x in range(16):
            if k[x, y][3]:
                px[min(31, 16 + x), min(31, 17 + y)] = k[x, y]
    return small.resize((128, 128), Image.NEAREST)


def main():
    # Objets
    save(pressed_plank(), "item", "pressed_plank.png")
    save(compressed_wood(), "item", "compressed_wood.png")
    for name, pal in METALS.items():
        save(bar(pal), "item", f"{name}_bar.png")
    save(mold(), "item", "mold.png")
    save(saw_blade(), "item", "saw_blade.png")
    save(blank_key(), "item", "blank_key.png")
    save(key(COPPER, True), "item", "copper_key.png")
    save(door_item(), "item", "armored_copper_door.png")

    # Porte (4 niveaux d'oxydation)
    for stage, pal in COPPER_STAGES.items():
        prefix = f"{stage}_" if stage else ""
        save(door_half(pal, False), "block", f"{prefix}armored_copper_door_bottom.png")
        save(door_half(pal, True), "block", f"{prefix}armored_copper_door_top.png")

    # Four à moufle (par-dessus le boîtier en cuivre de Create)
    save(furnace_front_overlay(False), "block", "muffle_furnace_front.png")
    save(furnace_front_overlay(True), "block", "muffle_furnace_front_on.png")
    save(furnace_top_overlay(), "block", "muffle_furnace_top.png")

    # Moule de l'enclume, lame de la machine à clés (le reste reprend les textures de l'enclume de Minecraft)
    save(mold_side(), "block", "mold_side.png")
    save(mold_floor(), "block", "mold_floor.png")
    save(saw_disc(), "block", "key_cutter_saw.png")
    save(hub(), "block", "key_cutter_hub.png")

    # Tapisserie
    save(tapestry_leather(), "entity", "banner", "tapestry_leather.png")
    save(tapestry_particle(), "block", "tapestry.png")

    # Fluides
    for name, ramp in MOLTEN.items():
        save(fluid_frames(ramp, 16, False), "block", "fluid", f"molten_{name}_still.png")
        save(fluid_frames(ramp, 16, True), "block", "fluid", f"molten_{name}_flow.png")
        save_mcmeta('{\n  "animation": {\n    "frametime": 3,\n    "interpolate": true\n  }\n}\n',
                    "block", "fluid", f"molten_{name}_still.png.mcmeta")
        save_mcmeta('{\n  "animation": {\n    "frametime": 2\n  }\n}\n',
                    "block", "fluid", f"molten_{name}_flow.png.mcmeta")

    # Interfaces
    save(casting_gui(), "gui", "casting_anvil.png")
    save(key_cutter_gui(), "gui", "key_cutter.png")

    logo().save(os.path.join(ROOT, "logo.png"))
    print("Textures générées dans", os.path.normpath(TEX))


if __name__ == "__main__":
    main()

"""Preserve source JPEGs and generate local display assets with Pillow."""
import sys
from pathlib import Path
root = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(root / '.tools/python'))
from PIL import Image, ImageOps
target = root / 'frontend/assets/images'
target.mkdir(parents=True, exist_ok=True)
for source in root.glob('*.jpg'):
    with Image.open(source) as image:
        image = ImageOps.exif_transpose(image).convert('RGB')
        image.thumbnail((1200, 1200))
        image.save(target / (source.stem + '.webp'), 'WEBP', quality=82, method=6)
print('Optimized existing images; originals preserved.')

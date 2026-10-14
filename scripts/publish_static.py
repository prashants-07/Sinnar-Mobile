"""Sync frontend into repository root for existing branch-based GitHub Pages hosting.

Copies only known frontend files, never deletes existing source photos or Git history.
The preferred workflow deploys frontend/ directly instead.
"""
from pathlib import Path
import shutil
root=Path(__file__).resolve().parents[1]
source=root/'frontend'
for item in source.iterdir():
    if item.is_dir():
        shutil.copytree(item,root/item.name,dirs_exist_ok=True)
    elif item.suffix in ('.html','.xml','.txt'):
        shutil.copy2(item,root/item.name)
(root/'.nojekyll').touch()
print('Static Pages output updated at repository root; existing photos preserved.')

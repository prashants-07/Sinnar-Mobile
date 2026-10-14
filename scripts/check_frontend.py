"""Check static assets, links, page metadata and JavaScript syntax without a browser."""
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urlsplit,unquote
import json
import subprocess
root=Path(__file__).resolve().parents[1]/'frontend'
errors=[]
class Page(HTMLParser):
    def __init__(self,path):
        super().__init__();self.path=path;self.ids=set();self.links=[];self.h1=0;self.schema=False;self.json_text='';self.title=False;self.meta=set();self.labels=set();self.inputs=[]
    def handle_starttag(self,tag,attrs):
        a=dict(attrs)
        if a.get('id'):
            if a['id'] in self.ids:errors.append(f'{self.path.name}: duplicate id {a["id"]}')
            self.ids.add(a['id'])
        if tag=='h1':self.h1+=1
        if tag=='title':self.title=True
        if tag=='meta':self.meta.add(a.get('name') or a.get('property'))
        if tag=='label' and a.get('for'):self.labels.add(a['for'])
        if tag in ('input','select','textarea') and a.get('id'):self.inputs.append(a['id'])
        if tag=='img' and not a.get('alt') and a.get('aria-hidden')!='true':errors.append(f'{self.path.name}: image missing alt')
        if tag=='iframe' and not a.get('title'):errors.append(f'{self.path.name}: map missing title')
        for key in ('src','href'):
            if a.get(key):self.links.append(a[key])
        if tag=='script' and a.get('type')=='application/ld+json':self.schema=True
    def handle_data(self,data):
        if self.schema:self.json_text+=data
    def handle_endtag(self,tag):
        if tag=='script' and self.schema:
            try:json.loads(self.json_text)
            except Exception:errors.append(f'{self.path.name}: invalid structured data')
            self.schema=False;self.json_text=''
pages=list(root.rglob('*.html'))
for path in pages:
    page=Page(path);text=path.read_text(encoding='utf-8');page.feed(text)
    if '8446130696' in text:errors.append(f'{path.name}: obsolete phone')
    if not page.title or 'viewport' not in page.meta:errors.append(f'{path.name}: missing title or viewport')
    if path.parent==root:
        if page.h1!=1:errors.append(f'{path.name}: expected one main heading')
        for meta in ('description','og:title','og:description','og:image'):
            if meta not in page.meta:errors.append(f'{path.name}: missing {meta}')
        if '© 2026 Sinnar Mobile. All Rights Reserved.' not in text:errors.append(f'{path.name}: footer missing')
    for control in page.inputs:
        if control not in page.labels:errors.append(f'{path.name}: input {control} missing label')
    for url in page.links:
        parsed=urlsplit(url)
        if parsed.scheme or parsed.netloc:continue
        if not parsed.path:
            if parsed.fragment and parsed.fragment not in page.ids:errors.append(f'{path.name}: broken anchor {url}')
            continue
        target=(path.parent/unquote(parsed.path)).resolve()
        if target.is_dir():target=target/'index.html'
        if not target.exists():errors.append(f'{path.name}: missing {url}')
        elif parsed.fragment and target.suffix=='.html':
            other=Page(target);other.feed(target.read_text(encoding='utf-8'))
            if parsed.fragment not in other.ids:errors.append(f'{path.name}: missing target anchor {url}')
for js in (root/'assets/js').glob('*.js'):
    result=subprocess.run(['node','--check',str(js)],capture_output=True,text=True)
    if result.returncode:errors.append(result.stderr)
if errors:
    print('\n'.join(errors));raise SystemExit(1)
print(f'PASS: {len(pages)} HTML pages; local links/assets, form labels, alt text, metadata and JavaScript syntax checked.')
print('Browser rendering, viewport overflow, console errors and UI interaction still require a browser.')

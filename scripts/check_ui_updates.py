"""Focused regression checks for generated branding, metadata, links, icons and raster assets."""
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urlsplit
import json
import re
import struct
import zlib
import xml.etree.ElementTree as ET
from branding import ROOT, CATEGORY_ICONS, MAP_LINK, MAP_EMBED, MAP_COORDS

class Elements(HTMLParser):
    def __init__(self):super().__init__();self.elements=[];self.title='';self.in_title=False
    def handle_starttag(self,tag,attrs):
        self.elements.append((tag,dict(attrs)))
        if tag=='title':self.in_title=True
    def handle_endtag(self,tag):
        if tag=='title':self.in_title=False
    def handle_data(self,data):
        if self.in_title:self.title+=data

expected={'index.html':'Sinnar Mobile | Smartphones, Accessories & Wholesale','about.html':'About Us | Sinnar Mobile','products.html':'Smartphones & Accessories | Sinnar Mobile','offers.html':'Best Smartphone Offers | Sinnar Mobile','services.html':'Mobile Repair & Services | Sinnar Mobile','contact.html':'Contact Us | Sinnar Mobile','faq.html':'Frequently Asked Questions | Sinnar Mobile','privacy.html':'Privacy Information | Sinnar Mobile'}
for file,title in expected.items():
    path=ROOT/'frontend'/file;text=path.read_text(encoding='utf-8');page=Elements();page.feed(text)
    assert page.title==title,(file,page.title)
    credit=[a for tag,a in page.elements if tag=='a' and a.get('href')=='https://clarigence.in/']
    assert len(credit)==1 and credit[0].get('target')=='_blank' and credit[0].get('rel')=='noopener noreferrer',file
    assert 'Created by ' in text and '© 2026 Sinnar Mobile. All Rights Reserved.' in text
    assert any(tag=='meta' and a.get('property')=='og:site_name' and a.get('content')=='Sinnar Mobile' for tag,a in page.elements)
    assert len([a for tag,a in page.elements if tag=='img' and a.get('src')=='assets/images/smartphone-logo.svg'])==2
    for tag,a in page.elements:
        if tag=='a' and 'data-directions' in a:
            assert a['href']==MAP_LINK and a['target']=='_blank' and a['rel']=='noopener noreferrer'
        if tag=='a' and 'data-instagram' in a:
            assert 'hidden' in a and 'href' not in a and a.get('aria-label')
        if tag=='a' and 'data-wa' in a:assert a['href']=='https://wa.me/917620050500'
        if tag=='iframe':assert a['src']==MAP_EMBED
    for name in ('favicon.svg','favicon-16x16.png','favicon-32x32.png','favicon.ico','apple-touch-icon.png'):assert name in text
    assert (ROOT/file).read_bytes()==path.read_bytes(),f'{file}: root output not synced'

home=Elements();home.feed((ROOT/'frontend/index.html').read_text(encoding='utf-8'))
cards=[a for tag,a in home.elements if tag=='a' and a.get('class')=='category-card']
assert len(cards)==17
for name,icon in CATEGORY_ICONS.items():
    svg=ET.parse(ROOT/'frontend/assets/icons'/f'{icon}.svg').getroot()
    assert svg.attrib['viewBox']=='0 0 24 24' and svg.attrib['stroke-width']=='1.8'
    assert svg.attrib['aria-hidden']=='true'
for path in (ROOT/'frontend/assets/images').glob('*.svg'):ET.parse(path)

def validate_png(name,size):
    data=(ROOT/'frontend/assets/images'/name).read_bytes();assert data[:8]==b'\x89PNG\r\n\x1a\n'
    assert struct.unpack('>II',data[16:24])==(size,size)
    pos=8;compressed=b''
    while pos<len(data):
        length=struct.unpack('>I',data[pos:pos+4])[0];tag=data[pos+4:pos+8];payload=data[pos+8:pos+8+length]
        crc=struct.unpack('>I',data[pos+8+length:pos+12+length])[0]
        assert zlib.crc32(tag+payload)&0xffffffff==crc
        if tag==b'IDAT':compressed+=payload
        pos+=12+length
    assert len(zlib.decompress(compressed))==size*(1+size*4)
for size in (16,32,192):validate_png(f'favicon-{size}x{size}.png',size)
validate_png('apple-touch-icon.png',180)
ico=(ROOT/'frontend/assets/images/favicon.ico').read_bytes()
assert struct.unpack('<HHH',ico[:6])==(0,1,2)
config=(ROOT/'frontend/assets/js/config.js').read_text()
configured=re.search(r"INSTAGRAM_URL:\s*'([^']*)'",config)
assert configured and MAP_LINK in config and MAP_EMBED in config
if configured.group(1):
    profile=urlsplit(configured.group(1))
    assert profile.scheme=='https' and profile.hostname in ('instagram.com','www.instagram.com') and profile.path not in ('','/')
assert (ROOT/'assets/js/config.js').read_bytes()==(ROOT/'frontend/assets/js/config.js').read_bytes()
assert 'categoryIcon(c.name)' in (ROOT/'frontend/assets/js/app.js').read_text(encoding='utf-8')
print('PASS: titles, shared logos, footer links/credit, pending Instagram, exact maps, 17 category cards, SVGs, PNG/ICO integrity and root sync.')
print('Viewport visual verification remains pending: 320, 375, 425, 768, 1024, 1440 and 1920 px.')

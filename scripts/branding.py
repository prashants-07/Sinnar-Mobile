"""Shared brand and decorative SVG markup for public/admin page generation."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
MAP_LINK='https://maps.app.goo.gl/g274oeQUb9x3pte76'
# Coordinates from the place location (!3d / !4d) in the owner's resolved Google Maps URL.
MAP_COORDS=(19.8431708,73.9948891)
MAP_EMBED='https://maps.google.com/maps?q=19.8431708,73.9948891&z=18&output=embed'
SHAPES=json.loads((ROOT/'frontend/assets/icons/icons.json').read_text(encoding='utf-8'))
CATEGORY_ICONS={
    'Smartphones':'smartphone','Smart Watches':'watch','Mobile Covers':'phone-case','D+ Glass':'shield',
    'Chargers':'plug','Earphones':'headphones','Bluetooth Earbuds':'earbuds','Neckbands':'headphones',
    'Power Banks':'battery-charging','Data Cables':'usb-cable','Bluetooth Speakers':'speaker',
    'Car Chargers':'car-charging','Memory Cards':'sd-card','Pen Drives':'usb-drive',
    'Mobile Repair':'wrench','Mobile Repair Services':'wrench','Gift Items':'gift',
    'Accessories':'package','Other Accessories':'package','Mobile Holders':'phone-holder'
}
def icon(name):
    return f'<svg class="ui-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">{SHAPES[name]}</svg>'
def brand_markup(prefix='',subtitle='Phones. Accessories. More.'):
    return f'<span class="brand-mark" aria-hidden="true"><img src="{prefix}assets/images/smartphone-logo.svg" width="48" height="48" alt="" aria-hidden="true"></span><span class="brand-copy"><strong class="brand-name"><span>SINNAR</span> <span class="brand-accent">MOBILE</span></strong><small>{subtitle}</small></span>'
def favicon_links(prefix=''):
    folder=prefix+'assets/images/'
    return f'<link rel="icon" href="{folder}favicon.svg" type="image/svg+xml"><link rel="icon" href="{folder}favicon-32x32.png" type="image/png" sizes="32x32"><link rel="icon" href="{folder}favicon-16x16.png" type="image/png" sizes="16x16"><link rel="shortcut icon" href="{folder}favicon.ico"><link rel="apple-touch-icon" href="{folder}apple-touch-icon.png" sizes="180x180">'
def footer_markup(address):
    quick=[('index.html','Home'),('about.html','About Us'),('products.html','Products'),('services.html','Services'),('offers.html','Offers'),('contact.html','Contact Us')]
    links=''.join(f'<a href="{path}">{label}</a>' for path,label in quick)
    return f'''<footer class="site-footer"><div class="container"><div class="footer-grid">
<div class="footer-brand"><a class="brand" href="index.html" aria-label="Sinnar Mobile home">{brand_markup()}</a><p class="footer-tagline">Everything Your Mobile Needs – Under One Roof!</p><p class="footer-range">Wholesale Smartphones | Mobile Accessories | Smart Watches | Designer Covers | D+ Glass | Mobile Repair</p></div>
<div><h3>Quick Links</h3><nav class="footer-links" aria-label="Footer quick links">{links}</nav></div>
<div><h3>Contact Information</h3><div class="footer-links footer-contact-links"><a class="footer-contact" data-phone href="tel:7620050500">{icon('phone-call')}<span>+91 <span data-setting="phone">7620050500</span></span></a><a class="footer-contact" data-email href="mailto:sinnarmobile0@gmail.com">{icon('mail')}<span data-setting="email">sinnarmobile0@gmail.com</span></a><div class="footer-contact">{icon('map-pin')}<address data-setting="address">{address}</address></div><a class="footer-directions" data-directions href="{MAP_LINK}" target="_blank" rel="noopener noreferrer">Get Directions ↗</a></div></div>
<div><h3>Social Media</h3><div class="footer-socials"><a class="social-link" data-wa href="https://wa.me/917620050500" target="_blank" rel="noopener noreferrer" aria-label="Chat with Sinnar Mobile on WhatsApp">{icon('whatsapp')}</a><a class="social-link" data-instagram target="_blank" rel="noopener noreferrer" aria-label="Visit Sinnar Mobile on Instagram" hidden>{icon('instagram')}</a><span class="social-link social-pending" data-instagram-pending role="img" aria-label="Instagram profile not yet available" title="Instagram profile coming soon">{icon('instagram')}</span></div><h3 class="footer-help-title">Here to help</h3><nav class="footer-links" aria-label="Footer help links"><a href="faq.html">Frequently asked questions</a><a href="privacy.html">Privacy information</a><a href="admin/" data-admin>Shop administration</a></nav></div>
</div><div class="footer-bottom"><span>© 2026 Sinnar Mobile. All Rights Reserved.</span><span class="developer-credit">Created by <a href="https://clarigence.in/" target="_blank" rel="noopener noreferrer">Clarigence.in</a></span></div></div></footer>'''

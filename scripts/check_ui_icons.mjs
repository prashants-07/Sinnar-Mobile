import assert from 'node:assert/strict';
import {categoryIcon,categoryIconName} from '../frontend/assets/js/icons.js';
const required={Smartphones:'smartphone','Smart Watches':'watch','Mobile Covers':'phone-case','D+ Glass':'shield',Chargers:'plug',Earphones:'headphones','Bluetooth Earbuds':'earbuds',Neckbands:'headphones','Power Banks':'battery-charging','Data Cables':'usb-cable','Bluetooth Speakers':'speaker','Car Chargers':'car-charging','Memory Cards':'sd-card','Pen Drives':'usb-drive','Mobile Repair':'wrench','Gift Items':'gift',Accessories:'package','Other Accessories':'package','Mobile Holders':'phone-holder'};
for(const [category,name] of Object.entries(required)){
  assert.equal(categoryIconName(category),name);
  const svg=categoryIcon(category);assert.ok(svg.startsWith('<svg '));assert.ok(svg.includes('stroke-width="1.8"'));assert.ok(!svg.includes('↗'));
}
assert.equal(categoryIconName('New administrator category'),'package');
assert.ok(!categoryIcon('<script>alert(1)</script>').includes('<script>'));
console.log('PASS: all requested dynamic category mappings, safe package fallback and consistent SVG strokes.');

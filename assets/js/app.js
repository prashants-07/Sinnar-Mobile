import {request,esc,whatsapp,imageUrl,publicSettings,base} from './api.js';

const settings={...publicSettings};
const menu=document.querySelector('.menu-toggle');
const nav=document.querySelector('.main-nav');
function closeMenu() {nav?.classList.remove('open');menu?.setAttribute('aria-expanded','false');menu?.setAttribute('aria-label','Open navigation');}
menu?.addEventListener('click',()=>{
  const open=nav.classList.toggle('open');menu.setAttribute('aria-expanded',String(open));menu.setAttribute('aria-label',open?'Close navigation':'Open navigation');
});
nav?.addEventListener('click',e=>{if(e.target.closest('a'))closeMenu();});
document.addEventListener('keydown',e=>{if(e.key==='Escape'&&nav?.classList.contains('open')){closeMenu();menu.focus();}});
document.addEventListener('click',e=>{if(!e.target.closest('.site-header'))closeMenu();});
window.addEventListener('resize',()=>{if(window.innerWidth>800)closeMenu();});
document.addEventListener('error',e=>{
  if(e.target instanceof HTMLImageElement&&!e.target.dataset.fallback){e.target.dataset.fallback='true';e.target.src='assets/images/phones.webp';}
},true);

function applySettings() {
  document.querySelectorAll('[data-setting]').forEach(el=>{if(settings[el.dataset.setting])el.textContent=settings[el.dataset.setting];});
  document.querySelectorAll('[data-phone]').forEach(el=>{el.href='tel:'+settings.phone;if(el.classList.contains('nav-call'))el.textContent='↗ Call '+settings.phone;});
  document.querySelectorAll('[data-email]').forEach(el=>el.href='mailto:'+settings.email);
  document.querySelectorAll('[data-wa]').forEach(el=>el.href=whatsapp('Hello Sinnar Mobile, I would like to enquire about your products and services.',settings.whatsapp));
  document.querySelectorAll('[data-admin]').forEach(el=>{const url=window.SM_CONFIG?.ADMIN_URL||(base?base+'/admin/':'admin/');el.href=url;});
  document.querySelectorAll('iframe.map').forEach(el=>{el.src='https://maps.google.com/maps?q='+encodeURIComponent(settings.address)+'&z=16&output=embed';});
  const schema=document.querySelector('script[type="application/ld+json"]');
  if(schema){const data=JSON.parse(schema.textContent);data.name=settings.businessName;data.telephone='+91'+settings.phone;data.email=settings.email;data.address.streetAddress=settings.address;schema.textContent=JSON.stringify(data);}
}
applySettings();
const settingsReady=request('/api/public/settings').then(data=>{Object.assign(settings,data);applySettings();}).catch(()=>{});
const categoriesReady=request('/api/public/categories').catch(()=>null);

function status(title,description,retry=false) {
  return `<div class="status-panel"><h3>${esc(title)}</h3><p>${esc(description)}</p><div class="actions status-actions"><a class="btn small" href="${esc(whatsapp('Hello Sinnar Mobile, please share current models, prices and availability.',settings.whatsapp))}" target="_blank" rel="noopener noreferrer">Ask on WhatsApp ↗</a>${retry?'<button class="btn secondary small" type="button" data-retry>Try again</button>':''}</div></div>`;
}
function productCard(p,categories=[]) {
  const category=categories.find(c=>c.id===p.categoryId)?.name||'Mobile collection';
  const availability={ENQUIRE:'Enquire for availability',AVAILABLE:'Available',OUT_OF_STOCK:'Out of stock',PRE_ORDER:'Pre-order enquiry'}[p.availability]||'Enquire for availability';
  const money=n=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:2}).format(n);
  const price=p.price!=null?money(p.price):'Ask for price';
  const discount=p.originalPrice!=null&&p.price!=null&&Number(p.originalPrice)>Number(p.price)?Math.round((1-Number(p.price)/Number(p.originalPrice))*100):null;
  return `<article class="collection-card product-card"><div class="photo"><img src="${esc(imageUrl(p.imageUrl))}" alt="${esc(p.name)}" loading="lazy" width="600" height="500"></div><div class="card-body"><span class="badge ${p.demo?'demo':''}">${p.demo?'Demo listing':esc(category)}</span>${discount?` <span class="badge">${discount}% off</span>`:''}<h3>${esc(p.name)}</h3>${p.brand?`<p>${esc(p.brand)} · ${esc(category)}</p>`:`<p>${esc(category)}</p>`}<p class="product-description">${esc(p.description)}</p><div class="product-price">${esc(price)}${discount?`<del>${esc(money(p.originalPrice))}</del>`:''}</div><div class="availability">${esc(availability)}</div><a class="btn secondary small" href="${esc(whatsapp(`Hello Sinnar Mobile, I want to know more about ${p.name}. Please share the price and availability.`,settings.whatsapp))}" target="_blank" rel="noopener noreferrer">WhatsApp Enquiry ↗</a></div></article>`;
}
function pager(el,data,change) {
  if(!el)return;
  if(data.totalPages<=1){el.replaceChildren();return;}
  el.innerHTML=`<button class="btn secondary small" ${data.page===0?'disabled':''} data-prev>Previous</button><span>Page ${data.page+1} of ${data.totalPages}</span><button class="btn secondary small" ${data.page+1>=data.totalPages?'disabled':''} data-next>Next</button>`;
  el.querySelector('[data-prev]').onclick=()=>change(data.page-1);el.querySelector('[data-next]').onclick=()=>change(data.page+1);
}

async function loadFeatured() {
  const target=document.querySelector('[data-featured]');if(!target)return;
  await settingsReady;
  try {
    const categories=await categoriesReady;
    const phones=categories?.find(c=>c.name==='Smartphones');
    const data=await request('/api/public/products?featured=true&size=4'+(phones?'&categoryId='+phones.id:''));
    target.innerHTML=data.items.length?data.items.map(p=>productCard(p,categories||[])).join(''):status('Your next phone starts with a conversation.','Ask us which smartphones suit your needs and budget. New listings will appear here as the shop adds them.');
  } catch { target.innerHTML=status('Find the phone that fits you.','Ask us for the latest smartphone models, prices and availability.'); }
}
async function loadCategories() {
  const target=document.querySelector('[data-categories]');if(!target)return;
  const data=await categoriesReady;if(!data)return;
  const icons={'Smartphones':'▯','Mobile Covers':'▣','D+ Glass':'◇','Smart Watches':'◷','Chargers':'ϟ','Bluetooth Earbuds':'◉'};
  target.innerHTML=data.map(c=>`<a class="category-card" href="products.html?category=${encodeURIComponent(c.name)}"><span class="category-icon" aria-hidden="true">${icons[c.name]||'↗'}</span>${esc(c.name)}</a>`).join('');
}
async function setupCatalog() {
  const grid=document.querySelector('#catalog-grid');if(!grid)return;
  const form=document.querySelector('#catalog-filters');const count=document.querySelector('#catalog-count');const pagination=document.querySelector('#catalog-pagination');
  let categories=await categoriesReady;categories=categories||[];
  const select=form.elements.categoryId;
  select.innerHTML='<option value="">All categories</option>'+categories.map(c=>`<option value="${c.id}">${esc(c.name)}</option>`).join('');
  const params=new URLSearchParams(location.search);
  const selected=categories.find(c=>c.name===params.get('category'));
  if(selected)select.value=String(selected.id);
  if(params.get('search'))form.elements.search.value=params.get('search').slice(0,150);
  let controller;
  async function load(page=0) {
    controller?.abort();const own=controller=new AbortController();
    grid.setAttribute('aria-busy','true');count.textContent='Loading products…';
    const query=new URLSearchParams(new FormData(form));query.set('page',page);query.set('size','12');if(!select.value)query.delete('categoryId');
    try {
      await settingsReady;
      const data=await request('/api/public/products?'+query,{signal:own.signal});if(own.signal.aborted)return;
      grid.innerHTML=data.items.length?data.items.map(p=>productCard(p,categories)).join(''):status('No products found.','Try another search or category. You can also ask us about the item you need.');
      count.textContent=`${data.totalElements} product${data.totalElements===1?'':'s'} found`;
      pager(pagination,data,n=>{load(n);form.scrollIntoView({block:'start'});});
    } catch(e) {
      if(own.signal.aborted)return;
      count.textContent='';pagination.replaceChildren();grid.innerHTML=status('Let’s find what you need.','The online collection is currently unavailable. Contact us for current models, prices and availability.',true);
      grid.querySelector('[data-retry]').onclick=()=>load(page);
    } finally {if(!own.signal.aborted)grid.removeAttribute('aria-busy');}
  }
  let timer;
  form.elements.search.addEventListener('input',()=>{clearTimeout(timer);timer=setTimeout(()=>load(),300);});
  form.addEventListener('change',()=>{clearTimeout(timer);load();});form.addEventListener('submit',e=>{e.preventDefault();clearTimeout(timer);load();});
  load();
}
async function loadOffers(page=0) {
  const target=document.querySelector('[data-offers]');if(!target)return;
  const preview=target.dataset.offers==='preview';await settingsReady;
  try {
    const data=await request(`/api/public/offers?size=${preview?4:12}&page=${page}`);
    target.innerHTML=data.items.length?data.items.map(o=>`<article class="collection-card"><div class="photo"><img src="${esc(imageUrl(o.imageUrl))}" alt="${esc(o.title)}" loading="lazy" width="600" height="400"></div><div class="card-body"><span class="badge">Current offer</span><h3>${esc(o.title)}</h3><p class="product-description">${esc(o.description)}</p><p class="offer-expiry">Valid through ${esc(new Date(o.expiresOn+'T12:00:00').toLocaleDateString('en-IN',{day:'numeric',month:'short',year:'numeric'}))}</p><a class="btn secondary small" href="${esc(whatsapp(`Hello Sinnar Mobile, please share details and terms for the offer: ${o.title}.`,settings.whatsapp))}" target="_blank" rel="noopener noreferrer">Enquire About This Offer ↗</a></div></article>`).join(''):status('No published offers right now.','Check back for new offers, or ask the shop about current smartphone and accessory deals.');
    if(!preview)pager(document.querySelector('#offers-pagination'),data,loadOffers);
  } catch {target.innerHTML=status('Ask about today’s offers.','Contact the shop for current smartphone deals, accessories offers and their terms.',!preview);target.querySelector('[data-retry]')?.addEventListener('click',()=>loadOffers(page));}
}
async function loadBanners() {
  const target=document.querySelector('[data-banners]');if(!target)return;
  try {
    const data=await request('/api/public/banners?size=4');
    target.innerHTML=data.items.map(b=>`<article class="collection-card"><div class="photo"><img src="${esc(imageUrl(b.imageUrl))}" alt="${esc(b.title)}" loading="lazy" width="600" height="400"></div><div class="card-body"><span class="badge">From the shop</span><h3>${esc(b.title)}</h3><p>${esc(b.description)}</p><a class="text-link" href="${esc(whatsapp(`Hello Sinnar Mobile, I would like to enquire about: ${b.title}.`,settings.whatsapp))}">Enquire ↗</a></div></article>`).join('');
  } catch {target.replaceChildren();}
}
async function loadFaqs() {
  const target=document.querySelector('[data-faqs]');if(!target)return;
  try {
    let rows=await request('/api/public/faqs');if(target.dataset.faqs==='preview')rows=rows.slice(0,4);
    if(!rows.length){target.innerHTML=status('Have a question?','Contact the shop and we’ll help you with your enquiry.');return;}
    const half=Math.ceil(rows.length/2);
    const col=items=>'<div class="faq-column">'+items.map(f=>`<details class="faq-item"><summary>${esc(f.question)}</summary><p>${esc(f.answer)}</p></details>`).join('')+'</div>';
    target.innerHTML=col(rows.slice(0,half))+col(rows.slice(half));
  } catch { /* Keep crawlable, factual business answers while the backend is unavailable. */ }
}
document.querySelectorAll('.enquiry-form').forEach(form=>{
  let submitting=false;
  form.addEventListener('submit',async e=>{
    e.preventDefault();if(submitting||!form.reportValidity())return;
    const statusEl=form.querySelector('.form-status');const button=form.querySelector('button[type=submit]');
    const body=Object.fromEntries(new FormData(form));for(const k in body)body[k]=body[k].trim();
    if(!body.name||!body.message){statusEl.className='form-status error-text';statusEl.textContent='Please enter your name and enquiry.';return;}
    submitting=true;button.disabled=true;const original=button.textContent;button.textContent='Sending…';statusEl.className='form-status';statusEl.textContent='Submitting your enquiry…';
    try {
      const result=await request('/api/public/'+form.dataset.kind,{method:'POST',body});
      form.reset();statusEl.className='form-status success-text';statusEl.textContent=`Thank you. Your enquiry has been saved (reference ${result.reference}). The shop can now review your request.`;
    } catch(err) {statusEl.className='form-status error-text';statusEl.textContent=err.message;}
    finally{submitting=false;button.disabled=false;button.textContent=original;}
  });
});
await Promise.allSettled([loadFeatured(),loadCategories(),setupCatalog(),loadOffers(),loadBanners(),loadFaqs()]);
if(!window.matchMedia('(prefers-reduced-motion: reduce)').matches&&'IntersectionObserver' in window){
  const observer=new IntersectionObserver(entries=>{
    entries.forEach(entry=>{if(entry.isIntersecting){entry.target.classList.add('is-visible');observer.unobserve(entry.target);}});
  },{threshold:.08});
  document.querySelectorAll('.section-heading,.feature-banner,.editorial-card,.benefit').forEach(el=>{
    el.classList.add('scroll-reveal');observer.observe(el);
  });
}

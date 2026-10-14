import {request,resetCsrf,esc,base} from './api.js';

const $=s=>document.querySelector(s);
const titles={stats:'Overview',products:'Products',categories:'Categories',offers:'Offers',banners:'Promotional banners',enquiries:'Contact enquiries','repair-enquiries':'Repair enquiries',faqs:'FAQs',settings:'Shop settings'};
const schema={
  products:[['name','Product name','text',true,150],['brand','Brand','text',false,100],['categoryId','Category','category',true],['description','Description','textarea',true,2000],['imageUrl','Image path','image',false,500],['price','Price (₹) — leave blank if unknown','money'],['originalPrice','Original price (₹) — optional','money'],['availability','Availability','availability',true],['featured','Featured on homepage','checkbox'],['demo','Clearly labelled demo listing','checkbox']],
  categories:[['name','Category name','text',true,100],['description','Description','textarea',false,500]],
  offers:[['title','Offer title','text',true,150],['description','Terms and offer details','textarea',true,2000],['imageUrl','Image path','image',false,500],['productId','Related product ID (optional)','number'],['expiresOn','Valid through (India time)','date',true],['active','Publish offer','checkbox']],
  banners:[['title','Banner title','text',true,150],['description','Banner description','textarea',true,1000],['imageUrl','Image path','image',false,500],['expiresOn','Valid through (optional)','date'],['active','Publish banner','checkbox']],
  faqs:[['question','Question','text',true,300],['answer','Answer','textarea',true,2000],['displayOrder','Display order','order',true]],
  settings:[['businessName','Business name','text',true,100],['phone','Phone (10 digits)','tel',true,20],['whatsapp','WhatsApp with country code (digits only)','tel',true,20],['email','Email','email',true,254],['address','Shop address','textarea',true,500],['businessHours','Business hours','textarea',true,1000],['tagline','Tagline','text',true,200],['announcement','Announcement bar','textarea',true,300]],
};
let section='stats',page=0,rows=[],categories=[],editingId=null,loadVersion=0,search='',statusFilter='',deleteId=null;
let loading=false,saving=false,uploading=false;
function notice(message,error=false){$('#admin-status').className=error?'admin-status-error':'admin-status-success';$('#admin-status').textContent=message;}
function clearNotice(){$('#admin-status').className='';$('#admin-status').textContent='';}
function loginView(message='') {
  loadVersion++;$('#dashboard').classList.add('is-hidden');$('#logout').classList.add('is-hidden');$('#login-panel').classList.remove('is-hidden');
  if($('#editor').open)$('#editor').close();if($('#confirm-delete').open)$('#confirm-delete').close();
  $('#login-status').textContent=message;resetCsrf();
}
async function showDashboard() {
  const me=await request('/api/auth/me',{auth:true});
  $('#signed-in').textContent='Signed in as '+me.username;
  $('#login-panel').classList.add('is-hidden');$('#dashboard').classList.remove('is-hidden');$('#logout').classList.remove('is-hidden');
  categories=await request('/api/admin/categories',{auth:true});await navigate('stats');
}
function handleError(e) {if(e.status===401)loginView('Your session expired. Please sign in again.');else notice(e.message,true);}
$('#login-form').addEventListener('submit',async e=>{
  e.preventDefault();const form=e.currentTarget;const button=form.querySelector('button');button.disabled=true;$('#login-status').textContent='Signing in…';
  try {await request('/api/auth/login',{method:'POST',body:new URLSearchParams(new FormData(form)),auth:true});resetCsrf();form.reset();await showDashboard();}
  catch(e){$('#login-status').textContent=e.message;$('#login-status').className='error-text';}finally{button.disabled=false;}
});
$('#logout').onclick=async()=>{try{await request('/api/auth/logout',{method:'POST',auth:true});loginView('You have been logged out.');}catch(e){handleError(e);}};
$('#admin-nav').addEventListener('click',e=>{const button=e.target.closest('[data-section]');if(button)navigate(button.dataset.section);});
async function navigate(next) {
  section=next;page=0;search='';statusFilter='';clearNotice();$('#section-title').textContent=titles[section];
  $('#admin-nav').querySelectorAll('button').forEach(b=>{if(b.dataset.section===section)b.setAttribute('aria-current','page');else b.removeAttribute('aria-current');});
  $('#add-record').classList.toggle('is-hidden',!schema[section]||section==='settings');
  const target=$('#admin-filters');
  if(section==='products')target.innerHTML='<div class="admin-filters"><label class="field" for="admin-search">Search products<input type="search" id="admin-search" maxlength="150" placeholder="Name, brand or description"></label></div>';
  else if(section.includes('enquiries'))target.innerHTML='<div class="admin-filters"><label class="field" for="status-filter">Enquiry status<select id="status-filter"><option value="">All statuses</option><option value="NEW">New</option><option value="IN_PROGRESS">In progress</option><option value="CLOSED">Closed</option></select></label></div>';
  else target.replaceChildren();
  let timer;
  $('#admin-search')?.addEventListener('input',e=>{clearTimeout(timer);timer=setTimeout(()=>{search=e.target.value;page=0;load();},300);});
  $('#status-filter')?.addEventListener('change',e=>{statusFilter=e.target.value;page=0;load();});
  await load();
}
async function load() {
  const version=++loadVersion;const current=section;loading=true;
  $('#admin-data').innerHTML='<p class="loading">Loading…</p>';$('#admin-pagination').replaceChildren();
  try {
    const query=new URLSearchParams({page,size:'20'});if(search)query.set('search',search);if(statusFilter)query.set('status',statusFilter);
    const data=await request('/api/admin/'+current+(schema[current]&&['categories','faqs','settings'].includes(current)||current==='stats'?'':'?'+query),{auth:true});
    if(version!==loadVersion)return;
    if(current==='stats'){
      $('#admin-data').innerHTML='<div class="stats-grid">'+Object.entries(data).map(([key,value])=>`<div class="stat"><strong>${esc(value)}</strong><span>${esc({products:'Products',offers:'Offers (all)',enquiries:'Contact enquiries',repairEnquiries:'Repair enquiries',newEnquiries:'New enquiries'}[key])}</span></div>`).join('')+'</div><div class="admin-note">These counts come from your database. Add products and current offers, confirm your contact details and business hours, and review new enquiries. Demo listings are clearly labelled and should be replaced with real products before publication.</div>';
    } else if(current==='settings'){
      $('#admin-data').innerHTML='<form class="form-panel" id="settings-form"><div class="form-grid">'+fields(schema.settings,data)+'</div><button class="btn" type="submit">Save shop settings</button><p role="status" class="form-status" id="settings-status"></p></form>';
      $('#settings-form').onsubmit=saveSettings;
    } else {
      rows=Array.isArray(data)?data:data.items;
      if(current==='categories')categories=rows;
      renderTable(current);
      if(!Array.isArray(data))renderPager(data);
    }
  } catch(e){if(version!==loadVersion)return;handleError(e);$('#admin-data').innerHTML='<div class="status-panel"><h3>Unable to load records.</h3><button class="btn small" id="retry-load">Try again</button></div>';$('#retry-load').onclick=load;}
  finally{if(version===loadVersion)loading=false;}
}
function renderPager(data) {
  if(data.totalPages<=1)return;
  $('#admin-pagination').innerHTML=`<button class="btn secondary small" data-page="${data.page-1}" ${data.page===0?'disabled':''}>Previous</button><span>Page ${data.page+1} of ${data.totalPages}</span><button class="btn secondary small" data-page="${data.page+1}" ${data.page+1>=data.totalPages?'disabled':''}>Next</button>`;
}
$('#admin-pagination').onclick=e=>{const b=e.target.closest('[data-page]');if(b&&!b.disabled){page=Number(b.dataset.page);load();}};
function renderTable(current) {
  if(!rows.length){$('#admin-data').innerHTML='<div class="status-panel"><h3>No records yet.</h3><p>'+esc(current.includes('enquiries')?'New customer submissions will appear here.':'Use “Add new” to create your first record.')+'</p></div>';return;}
  const enquiries=current.includes('enquiries');
  const header=enquiries?'<th>Customer &amp; request</th><th>Status</th><th>Actions</th>':'<th>Record</th><th>Details</th><th>Actions</th>';
  const body=rows.map(r=>{
    let first='',second='';
    if(enquiries){
      first=`<strong>${esc(r.name)}</strong><p>${esc(r.phone)}${r.email?' · '+esc(r.email):''}</p>${r.device?`<p>${esc(r.device)} · ${esc(r.service)}</p>`:''}<p>${esc(r.message)}</p><p>Ref. ${r.id} · ${esc(new Date(r.createdAt).toLocaleString('en-IN'))}</p>`;
      second=`<label class="is-hidden" for="status-${r.id}">Status for enquiry ${r.id}</label><select id="status-${r.id}" data-status="${r.id}" aria-label="Status for enquiry ${r.id}">${['NEW','IN_PROGRESS','CLOSED'].map(s=>`<option value="${s}" ${r.status===s?'selected':''}>${s.replaceAll('_',' ')}</option>`).join('')}</select>`;
    } else {
      first=`<strong>${esc(r.name||r.title||r.question)}</strong><p>ID ${r.id}${r.demo?' · Demo listing':''}</p>`;
      if(current==='products')second=`${esc(categories.find(c=>c.id===r.categoryId)?.name||'Category '+r.categoryId)}<p>${r.price==null?'Price on enquiry':'₹'+esc(r.price)} · ${esc(r.availability.replaceAll('_',' '))}</p><p>${r.featured?'Featured on homepage':'Standard listing'}</p>`;
      else if(current==='offers'||current==='banners')second=`<p>${esc(r.description)}</p><p>${r.active?'Published':'Hidden'}${r.expiresOn?' · Valid through '+esc(r.expiresOn):''}${r.productId?' · Product ID '+r.productId:''}</p>`;
      else if(current==='faqs')second=`<p>${esc(r.answer)}</p><p>Order ${r.displayOrder}</p>`;
      else second=`<p>${esc(r.description||'')}</p>`;
    }
    return `<tr><td>${first}</td><td>${second}</td><td><div class="actions">${!enquiries?`<button class="btn secondary small" data-edit="${r.id}">Edit</button>`:''}<button class="btn danger small" data-delete="${r.id}">Delete</button></div></td></tr>`;
  }).join('');
  $('#admin-data').innerHTML=`<div class="admin-table-wrap"><table class="admin-table"><caption class="is-hidden">${esc(titles[current])}</caption><thead><tr>${header}</tr></thead><tbody>${body}</tbody></table></div>`;
}
function fields(definition,record={}) {
  return definition.map(([name,label,type,required,max])=>{
    const value=record[name]??(type==='availability'?'ENQUIRE':type==='order'?0:'');
    const attrs=`name="${name}" id="field-${name}" ${required?'required':''} ${max?'maxlength="'+max+'"':''}`;
    if(type==='checkbox')return `<div class="field"><label class="checkbox-label"><input type="checkbox" name="${name}" ${record[name]?'checked':''}>${esc(label)}</label></div>`;
    let input;
    if(type==='textarea')input=`<textarea ${attrs}>${esc(value)}</textarea>`;
    else if(type==='category')input=`<select ${attrs}><option value="">Choose category</option>${categories.map(c=>`<option value="${c.id}" ${Number(value)===c.id?'selected':''}>${esc(c.name)}</option>`).join('')}</select>`;
    else if(type==='availability')input=`<select ${attrs}>${['ENQUIRE','AVAILABLE','OUT_OF_STOCK','PRE_ORDER'].map(v=>`<option value="${v}" ${value===v?'selected':''}>${esc(v.replaceAll('_',' '))}</option>`).join('')}</select>`;
    else input=`<input type="${['money','number','order'].includes(type)?'number':type==='image'?'text':type}" ${attrs} value="${esc(value)}" ${type==='money'?'min="0" max="9999999999.99" step="0.01"':type==='number'?'min="1" step="1"':type==='order'?'min="0" step="1"':''} ${name==='phone'?'pattern="[0-9]{10}"':name==='whatsapp'?'pattern="[0-9]{10,15}"':''}>`;
    return `<div class="field ${['textarea','image'].includes(type)?'full':''}"><label for="field-${name}">${esc(label)}</label>${input}${type==='image'?'<div class="upload-block"><label for="image-file">Upload a product or banner image</label><input id="image-file" type="file" accept="image/png,image/jpeg"><small>PNG or JPEG, up to 5 MB. Maximum 6000 px per side and 20 megapixels.</small><button type="button" class="btn secondary small" id="upload-image">Upload selected image</button><p id="upload-status" role="status"></p></div>':''}</div>`;
  }).join('');
}
function collect(form,definition) {
  const data=new FormData(form);const body={};
  definition.forEach(([name,,type])=>{
    if(type==='checkbox')body[name]=data.has(name);
    else {const v=String(data.get(name)||'').trim();body[name]=['money','number','order','category'].includes(type)?(v===''?null:Number(v)):type==='date'?(v||null):v;}
  });return body;
}
async function openEditor(id=null) {
  clearNotice();editingId=id;
  if(section==='products'){
    try{categories=await request('/api/admin/categories',{auth:true});}catch(e){handleError(e);return;}
  }
  const record=id==null?{active:true,availability:'ENQUIRE'}:rows.find(r=>r.id===id);
  $('#editor-title').textContent=(id==null?'Add ':'Edit ')+({products:'product',categories:'category',offers:'offer',banners:'banner',faqs:'FAQ'}[section]);
  $('#editor-fields').innerHTML=fields(schema[section],record);$('#editor-status').textContent='';$('#editor-status').className='form-status';
  $('#editor').showModal();$('#upload-image')?.addEventListener('click',uploadImage);
}
$('#add-record').onclick=()=>openEditor();
$('#close-editor').onclick=$('#cancel-editor').onclick=()=>{if(!saving&&!uploading)$('#editor').close();};
$('#editor').addEventListener('cancel',e=>{if(saving||uploading)e.preventDefault();});
async function uploadImage() {
  const file=$('#image-file').files[0];const status=$('#upload-status');const button=$('#upload-image');
  if(!file){status.textContent='Choose a PNG or JPEG first.';return;}
  if(file.size>5*1024*1024){status.textContent='Image must be smaller than 5 MB.';return;}
  uploading=true;button.disabled=true;$('#editor-form button[type=submit]').disabled=true;status.textContent='Uploading…';
  try {const form=new FormData();form.append('file',file);const result=await request('/api/admin/uploads',{method:'POST',body:form,auth:true});$('#field-imageUrl').value=result.url;status.textContent='Image uploaded. Save the record to use it.';}
  catch(e){status.textContent=e.message;}
  finally{uploading=false;button.disabled=false;$('#editor-form button[type=submit]').disabled=false;}
}
$('#editor-form').onsubmit=async e=>{
  e.preventDefault();if(saving||uploading)return;
  const form=e.currentTarget;const button=form.querySelector('button[type=submit]');saving=true;button.disabled=true;
  $('#editor-status').className='form-status';$('#editor-status').textContent='Saving…';
  try {
    await request('/api/admin/'+section+(editingId==null?'':'/'+editingId),{method:editingId==null?'POST':'PUT',body:collect(form,schema[section]),auth:true});
    $('#editor').close();await load();notice('Record saved.');
  }catch(e){$('#editor-status').className='form-status error-text';$('#editor-status').textContent=e.message+(e.fields?'\n'+Object.entries(e.fields).map(([k,v])=>k+': '+v).join('\n'):'');if(e.status===401)loginView('Your session expired. Please sign in again.');}
  finally{saving=false;button.disabled=false;}
};
async function saveSettings(e) {
  e.preventDefault();const form=e.currentTarget;const button=form.querySelector('button');button.disabled=true;
  const status=$('#settings-status');status.className='form-status';status.textContent='Saving…';
  try{await request('/api/admin/settings',{method:'PUT',body:collect(form,schema.settings),auth:true});status.textContent='Shop settings saved.';status.className='form-status success-text';}
  catch(e){status.textContent=e.message;status.className='form-status error-text';if(e.status===401)loginView('Your session expired. Please sign in again.');}finally{button.disabled=false;}
}
$('#admin-data').addEventListener('click',e=>{
  const edit=e.target.closest('[data-edit]');if(edit){openEditor(Number(edit.dataset.edit));return;}
  const del=e.target.closest('[data-delete]');if(del){deleteId=Number(del.dataset.delete);$('#delete-description').textContent=`Record ${deleteId} will be permanently deleted. This cannot be undone.`;$('#delete-confirm').disabled=false;$('#confirm-delete').showModal();}
});
$('#admin-data').addEventListener('change',async e=>{
  if(!e.target.matches('[data-status]'))return;const input=e.target;input.disabled=true;
  try{await request('/api/admin/'+section+'/'+input.dataset.status,{method:'PATCH',body:{status:input.value},auth:true});await load();notice('Enquiry status updated.');}
  catch(e){handleError(e);await load();}finally{input.disabled=false;}
});
$('#delete-cancel').onclick=()=>$('#confirm-delete').close();
$('#delete-confirm').onclick=async()=>{
  const button=$('#delete-confirm');button.disabled=true;
  try{await request('/api/admin/'+section+'/'+deleteId,{method:'DELETE',auth:true});$('#confirm-delete').close();if(rows.length===1&&page>0)page--;await load();notice('Record deleted.');}
  catch(e){$('#confirm-delete').close();handleError(e);}finally{button.disabled=false;}
};

const configuredAdmin=window.SM_CONFIG?.ADMIN_URL;
if(base&&new URL(base).origin!==location.origin){location.replace(configuredAdmin||base+'/admin/');}
else if(!base){$('#login-status').textContent='Open this dashboard on your deployed Spring Boot backend at /admin/. Configure API_BASE_URL in assets/js/config.js to connect the public website.';$('#login-form button').disabled=true;}
else {try{await showDashboard();}catch(e){if(e.status!==401)$('#login-status').textContent=e.message;}}

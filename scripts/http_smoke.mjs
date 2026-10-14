// Real HTTP integration checks using the frontend Fetch API module.
// Run only against a disposable QA backend; the credentials below are test-only.
import assert from 'node:assert/strict';
const origin=process.env.SM_QA_ORIGIN||'http://localhost:9080';
globalThis.window={SM_CONFIG:{API_BASE_URL:origin}};
globalThis.location=new URL('http://localhost:5500');
// Node has no browser cookie jar. Keep session cookies in memory for this HTTP test.
const realFetch=globalThis.fetch;let cookie='';
globalThis.fetch=async(url,options={})=>{
  const headers=new Headers(options.headers);
  if(options.credentials==='include'&&cookie)headers.set('Cookie',cookie);
  const response=await realFetch(url,{...options,headers});
  const setCookie=response.headers.getSetCookie();
  if(options.credentials==='include'&&setCookie.length)cookie=setCookie.map(s=>s.split(';')[0]).join('; ');
  return response;
};
const {request,resetCsrf,whatsapp,esc}=await import('../frontend/assets/js/api.js');
assert.ok(whatsapp('Phone & case?').includes('Phone%20%26%20case%3F'));
assert.equal(esc('<img onerror="x">'),'&lt;img onerror=&quot;x&quot;&gt;');
for(const page of ['index.html','about.html','products.html','offers.html','services.html','faq.html','contact.html','privacy.html','admin/']){
  const res=await fetch(origin+'/'+page);assert.equal(res.status,200,page+' must load');
}
const settings=await request('/api/public/settings');assert.equal(settings.phone,'7620050500');
const categories=await request('/api/public/categories');assert.equal(categories.length,17);
assert.equal((await request('/api/public/faqs')).length,10);
const form={name:'HTTP QA customer',phone:'7620050500',message:'Disposable integration enquiry',email:'qa@example.com',website:''};
const enquiry=await request('/api/public/enquiries',{method:'POST',body:form});assert.ok(enquiry.reference);
const repair=await request('/api/public/repair-enquiries',{method:'POST',body:{...form,device:'QA phone',service:'Battery Replacement'}});assert.ok(repair.reference);
await assert.rejects(()=>request('/api/public/enquiries',{method:'POST',body:{...form,name:''}}),e=>e.status===400);
await assert.rejects(()=>request('/api/admin/stats',{auth:true}),e=>e.status===401);
await request('/api/auth/login',{method:'POST',body:new URLSearchParams({username:'testadmin',password:'Test-only-password-123!'}),auth:true});resetCsrf();
assert.equal((await request('/api/auth/me',{auth:true})).role,'ADMIN');
const product=await request('/api/admin/products',{method:'POST',body:{name:'HTTP QA phone',brand:'QA',categoryId:categories[0].id,description:'Disposable test listing',availability:'ENQUIRE',demo:true,featured:false},auth:true});
assert.equal(product.price,null);
const filtered=await request('/api/public/products?search=HTTP%20QA&size=1');assert.equal(filtered.totalElements,1);
await request('/api/admin/products/'+product.id,{method:'PUT',body:{name:'HTTP QA phone edited',categoryId:categories[0].id,description:'Updated QA listing',availability:'AVAILABLE',demo:true,featured:true},auth:true});
const png=Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a4V8AAAAASUVORK5CYII=','base64');
const upload=new FormData();upload.append('file',new Blob([png],{type:'image/png'}),'test.png');
const image=await request('/api/admin/uploads',{method:'POST',body:upload,auth:true});
assert.match(image.url,/^\/uploads\/[a-f0-9-]+\.png$/);assert.equal((await fetch(origin+image.url)).status,200);
await request('/api/admin/enquiries/'+enquiry.reference,{method:'PATCH',body:{status:'CLOSED'},auth:true});
await request('/api/admin/repair-enquiries/'+repair.reference,{method:'PATCH',body:{status:'IN_PROGRESS'},auth:true});
await request('/api/admin/enquiries/'+enquiry.reference,{method:'DELETE',auth:true});
await request('/api/admin/repair-enquiries/'+repair.reference,{method:'DELETE',auth:true});
await request('/api/admin/products/'+product.id,{method:'DELETE',auth:true});
const cors=await fetch(origin+'/api/public/products',{headers:{Origin:'http://localhost:5500'}});assert.equal(cors.headers.get('access-control-allow-origin'),'http://localhost:5500');
await request('/api/auth/logout',{method:'POST',auth:true});resetCsrf();
await assert.rejects(()=>request('/api/admin/stats',{auth:true}),e=>e.status===401);
console.log('PASS: real HTTP pages, frontend Fetch integration, validation, authenticated session/CSRF, product CRUD, upload retrieval, enquiry status/deletion and CORS.');

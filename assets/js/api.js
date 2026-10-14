export const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const configured = (window.SM_CONFIG?.API_BASE_URL || '').replace(/\/$/, '');
const local = ['localhost', '127.0.0.1'].includes(location.hostname);
export const base = configured || (local || !location.pathname.startsWith('/Sinnar-Mobile/') ? location.origin : '');
export const publicSettings = {
  businessName:'Sinnar Mobile',phone:'7620050500',whatsapp:'917620050500',email:'sinnarmobile0@gmail.com',
  address:'Sinnar Mobile, Shop No. 1, Opp. Hiralal Tower, Near Sinnar Bus Stand, Sinnar, Maharashtra 422103',
  businessHours:"Please call to confirm today's opening hours.",tagline:'Everything Your Mobile Needs – Under One Roof!',
  announcement:"Smartphone offers, wholesale enquiries & everyday essentials. WhatsApp us for today's details."
};
let csrf = null;
export async function request(path, { method='GET', body, auth=false, signal }={}) {
  if (!base) throw new Error('Online enquiries are currently unavailable. Please call or WhatsApp the shop.');
  const headers = {};
  if (auth && method !== 'GET') {
    if (!csrf) csrf = await request('/api/auth/csrf', {auth:true});
    headers[csrf.headerName] = csrf.token;
  }
  if (body && !(body instanceof FormData) && !(body instanceof URLSearchParams)) {
    headers['Content-Type']='application/json'; body=JSON.stringify(body);
  }
  const timeout = AbortSignal.timeout(12000);
  let response;
  try {
    response=await fetch(base+path,{method,body,headers,credentials:auth?'include':'omit',signal:signal?AbortSignal.any([signal,timeout]):timeout});
  } catch(e) {
    if (signal?.aborted) throw e;
    throw new Error('Unable to connect. Please try again, or call or WhatsApp the shop.');
  }
  let result;
  try { result=await response.json(); } catch { throw new Error('Online enquiries are unavailable. Please call or WhatsApp the shop.'); }
  if (!response.ok || !result.success) {
    const e=new Error(result.message || 'Unable to complete your request.');e.status=response.status;e.fields=result.data;
    if(response.status===403) csrf=null;
    throw e;
  }
  return result.data;
}
export function resetCsrf() { csrf=null; }
export function whatsapp(message, number=publicSettings.whatsapp) { return `https://wa.me/${number}?text=${encodeURIComponent(message)}`; }
export function imageUrl(url, fallback='assets/images/phones.webp') {
  if (!url) return fallback;
  if (/^\/uploads\/[a-f0-9-]+\.(jpg|png)$/.test(url)) return base+url;
  if (/^assets\/images\/[\w-]+\.(webp|jpg|png)$/.test(url)) return url;
  return fallback;
}

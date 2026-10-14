# REST API

All API responses use `{ "success": true|false, "data": ..., "message": ... }`. Validation failures include field messages in `data`. Success codes are 200 for reads/updates/deletes and 201 for creation; invalid input is 400, unauthenticated requests 401, access/CSRF failures 403, missing records 404, unsupported methods 405, conflicting dependencies 409, oversized requests 413 and throttled requests 429.

Paginated data is `{ items, page, size, totalElements, totalPages }`; pages start at zero and sizes must be 1–100. Categories and FAQs are complete ordered reference lists. Shop settings are a singleton.

| Route | Methods | Purpose |
|---|---|---|
| `/api/public/products` | GET | Catalogue: `page`, `size`, `search`, `categoryId`, `featured`, `availability`, `sort` |
| `/api/public/products/{id}` | GET | One product |
| `/api/public/categories` | GET | Categories alphabetically |
| `/api/public/offers` | GET | Active, unexpired offers; pagination |
| `/api/public/banners` | GET | Active, unexpired banners; pagination |
| `/api/public/faqs` | GET | FAQs by display order |
| `/api/public/settings` | GET | Public shop information |
| `/api/public/enquiries` | POST | Create a customer enquiry |
| `/api/public/repair-enquiries` | POST | Create a repair enquiry |
| `/api/auth/csrf` | GET | Obtain session CSRF token and header name |
| `/api/auth/login` | POST | Form URL-encoded `username` and `password`; requires CSRF header |
| `/api/auth/me` | GET | Current ADMIN account |
| `/api/auth/logout` | POST | Invalidate session; requires CSRF header |
| `/api/admin/products` | GET, POST | List/create products |
| `/api/admin/products/{id}` | GET, PUT, DELETE | Read/update/delete product |
| `/api/admin/categories` | GET, POST | List/create categories |
| `/api/admin/categories/{id}` | PUT, DELETE | Update/delete category |
| `/api/admin/offers` | GET, POST | List all offers/create offer |
| `/api/admin/offers/{id}` | PUT, DELETE | Update/delete offer |
| `/api/admin/banners` | GET, POST | List all banners/create banner |
| `/api/admin/banners/{id}` | PUT, DELETE | Update/delete banner |
| `/api/admin/faqs` | GET, POST | List/create FAQs |
| `/api/admin/faqs/{id}` | PUT, DELETE | Update/delete FAQ |
| `/api/admin/settings` | GET, PUT | Read/update shop details |
| `/api/admin/enquiries` | GET | Paginated enquiries; optional `status` filter |
| `/api/admin/repair-enquiries` | GET | Paginated repair requests; optional `status` filter |
| `/api/admin/enquiries/{id}` | PATCH, DELETE | Change status or permanently delete |
| `/api/admin/repair-enquiries/{id}` | PATCH, DELETE | Change status or permanently delete |
| `/api/admin/uploads` | POST | Multipart `file`; validated PNG/JPEG |
| `/api/admin/stats` | GET | Database counts |

Product sort values: `newest`, `name`, `price-asc`, `price-desc`. Availability values: `ENQUIRE`, `AVAILABLE`, `OUT_OF_STOCK`, `PRE_ORDER`. Enquiry status values: `NEW`, `IN_PROGRESS`, `CLOSED`.

```json
{
  "name": "Administrator-supplied product name",
  "brand": "Administrator-supplied brand",
  "categoryId": 1,
  "description": "Actual product details",
  "imageUrl": "assets/images/phones.webp",
  "price": null,
  "originalPrice": null,
  "availability": "ENQUIRE",
  "featured": false,
  "demo": false
}
```

Category IDs are assigned by the database; get the actual ID from the categories endpoint. `originalPrice` requires a price and cannot be lower than it. Discounts are calculated by the frontend from these two real values. Product quantities are not asserted; administrators manage availability states.

Offers require `title`, `description`, `expiresOn` (ISO date) and `active`; `productId` and `imageUrl` are optional. Banners use `title`, `description`, `active`, and optional `imageUrl`/`expiresOn`. Expiry dates are inclusive through that calendar day in Asia/Kolkata. Promotions without configured discounts display only administrator-entered text.

An enquiry requires `name`, `phone`, and `message`; `email` is optional. Repairs additionally require `device` and `service`. `website` is a hidden honeypot and must be blank. A successful public submission returns only a `reference`, never the stored personal details.

Uploads return `{ "url": "/uploads/<uuid>.png" }` or a JPEG URL. Content is decoded and re-encoded with ImageIO; SVG, HTML, arbitrary external URLs and path traversal are rejected. Limits: 5 MB, 6000 px per side and 20 megapixels. Save the returned URL on a product, offer or banner. Upload files are retained separately from records; removing a product does not remove its image, since other records may reuse it. Review unused files through an authorised storage maintenance process.

For admin requests, fetch CSRF with credentials included, then send the returned token in the returned header. Refresh the token after login/logout. The session cookie is HttpOnly, Secure in production and defaults to SameSite=Lax. Serve admin from the backend origin; do not depend on third-party cookies.

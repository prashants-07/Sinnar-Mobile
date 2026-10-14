package com.sinnarmobile;

import com.sinnarmobile.Models.*;
import com.sinnarmobile.Requests.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api")
public class CatalogController {
    private final Products products; private final Categories categories; private final Offers offers;
    private final Faqs faqs; private final Banners banners; private final WebsiteSettings settings;
    public CatalogController(Products p,Categories c,Offers o,Faqs f,Banners b,WebsiteSettings s) {products=p;categories=c;offers=o;faqs=f;banners=b;settings=s;}
    static <T> T found(Optional<T> item) {return item.orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Record not found."));}
    static Pageable paging(int page,int size,Sort sort) {
        if(page<0||size<1||size>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Page must be non-negative; size must be 1–100.");
        return PageRequest.of(page,size,sort);
    }
    static void image(String url) {
        if(url!=null&&!url.isBlank()&&!url.matches("(?:assets/images/[A-Za-z0-9_-]+\\.(?:webp|jpg|png)|/uploads/[a-f0-9-]+\\.(?:jpg|png))"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use an uploaded image or a local assets/images image.");
    }
    @GetMapping({"/public/products","/admin/products"}) Api<Map<String,Object>> products(
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size,
        @RequestParam(defaultValue="") String search,@RequestParam(required=false) Long categoryId,
        @RequestParam(required=false) Boolean featured,@RequestParam(required=false) Availability availability,
        @RequestParam(defaultValue="newest") String sort) {
        if(search.length()>150) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Search is too long.");
        Sort order=switch(sort) {case "name"->Sort.by("name").ascending();case "price-asc"->Sort.by("price").ascending();case "price-desc"->Sort.by("price").descending();case "newest"->Sort.by("createdAt").descending();default->throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown sort order.");};
        Specification<Product> spec=(root,q,cb)->{
            var terms=new ArrayList<jakarta.persistence.criteria.Predicate>();
            if(!search.isBlank()) {
                String pattern="%"+search.toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
                terms.add(cb.or(cb.like(cb.lower(root.get("name")),pattern,'\\'),cb.like(cb.lower(root.get("brand")),pattern,'\\'),cb.like(cb.lower(root.get("description")),pattern,'\\')));
            }
            if(categoryId!=null) terms.add(cb.equal(root.get("categoryId"),categoryId));
            if(featured!=null) terms.add(cb.equal(root.get("featured"),featured));
            if(availability!=null) terms.add(cb.equal(root.get("availability"),availability));
            return cb.and(terms.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return Api.page(products.findAll(spec,paging(page,size,order.and(Sort.by("id")))));
    }
    @GetMapping({"/public/products/{id}","/admin/products/{id}"}) Api<Product> product(@PathVariable Long id) {return Api.ok(found(products.findById(id)));}
    Product apply(Product p,ProductInput i) {
        found(categories.findById(i.categoryId())); image(i.imageUrl());
        BigDecimal price=i.price(),original=i.originalPrice();
        if(original!=null&&(price==null||original.compareTo(price)<0)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Original price requires a price and cannot be lower than it.");
        p.setName(i.name().trim()); p.setBrand(i.brand()); p.setCategoryId(i.categoryId()); p.setDescription(i.description()); p.setImageUrl(i.imageUrl());
        p.setPrice(price); p.setOriginalPrice(original); p.setAvailability(i.availability()); p.setFeatured(i.featured()); p.setDemo(i.demo());return p;
    }
    @PostMapping("/admin/products") @ResponseStatus(HttpStatus.CREATED) Api<Product> addProduct(@Valid @RequestBody ProductInput i) {return Api.ok(products.save(apply(new Product(),i)));}
    @PutMapping("/admin/products/{id}") Api<Product> editProduct(@PathVariable Long id,@Valid @RequestBody ProductInput i) {return Api.ok(products.save(apply(found(products.findById(id)),i)));}
    @DeleteMapping("/admin/products/{id}") Api<Void> deleteProduct(@PathVariable Long id) {
        found(products.findById(id)); if(offers.existsByProductId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Remove linked offers first."); products.deleteById(id); return Api.done("Product deleted.");
    }
    @GetMapping({"/public/categories","/admin/categories"}) Api<List<Category>> categories() {return Api.ok(categories.findAll(Sort.by("name")));}
    @PostMapping("/admin/categories") @ResponseStatus(HttpStatus.CREATED) Api<Category> addCategory(@Valid @RequestBody Category c) { c.setId(null); return Api.ok(categories.save(c)); }
    @PutMapping("/admin/categories/{id}") Api<Category> editCategory(@PathVariable Long id,@Valid @RequestBody Category input) {
        var c=found(categories.findById(id)); c.setName(input.getName()); c.setDescription(input.getDescription());return Api.ok(categories.save(c));
    }
    @DeleteMapping("/admin/categories/{id}") Api<Void> deleteCategory(@PathVariable Long id) {
        found(categories.findById(id)); if(products.existsByCategoryId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Move or delete products in this category first."); categories.deleteById(id);return Api.done("Category deleted.");
    }
    @GetMapping("/public/offers") Api<Map<String,Object>> publicOffers(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size) {
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return Api.page(offers.findAll((r,q,c)->c.and(c.isTrue(r.get("active")),c.greaterThanOrEqualTo(r.get("expiresOn"),today)),paging(page,size,Sort.by("expiresOn"))));
    }
    @GetMapping("/admin/offers") Api<Map<String,Object>> offers(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return Api.page(offers.findAll(paging(page,size,Sort.by("createdAt").descending())));}
    Offer apply(Offer o,Offer i) { image(i.getImageUrl()); if(i.getProductId()!=null) found(products.findById(i.getProductId()));o.setTitle(i.getTitle());o.setDescription(i.getDescription());o.setImageUrl(i.getImageUrl());o.setProductId(i.getProductId());o.setExpiresOn(i.getExpiresOn());o.setActive(i.isActive());return o; }
    @PostMapping("/admin/offers") @ResponseStatus(HttpStatus.CREATED) Api<Offer> addOffer(@Valid @RequestBody Offer o) {return Api.ok(offers.save(apply(new Offer(),o)));}
    @PutMapping("/admin/offers/{id}") Api<Offer> editOffer(@PathVariable Long id,@Valid @RequestBody Offer o) {return Api.ok(offers.save(apply(found(offers.findById(id)),o)));}
    @DeleteMapping("/admin/offers/{id}") Api<Void> deleteOffer(@PathVariable Long id) {offers.delete(found(offers.findById(id)));return Api.done("Offer deleted.");}
    @GetMapping({"/public/faqs","/admin/faqs"}) Api<List<Faq>> faqs() {return Api.ok(faqs.findAll(Sort.by("displayOrder").and(Sort.by("id"))));}
    @PostMapping("/admin/faqs") @ResponseStatus(HttpStatus.CREATED) Api<Faq> addFaq(@Valid @RequestBody Faq f) {f.setId(null); return Api.ok(faqs.save(f));}
    @PutMapping("/admin/faqs/{id}") Api<Faq> editFaq(@PathVariable Long id,@Valid @RequestBody Faq i) {var f=found(faqs.findById(id));f.setQuestion(i.getQuestion());f.setAnswer(i.getAnswer());f.setDisplayOrder(i.getDisplayOrder());return Api.ok(faqs.save(f));}
    @DeleteMapping("/admin/faqs/{id}") Api<Void> deleteFaq(@PathVariable Long id) {faqs.delete(found(faqs.findById(id)));return Api.done("FAQ deleted.");}
    @GetMapping("/public/banners") Api<Map<String,Object>> publicBanners(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size) {
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Kolkata"));return Api.page(banners.findAll((r,q,c)->c.and(c.isTrue(r.get("active")),c.or(c.isNull(r.get("expiresOn")),c.greaterThanOrEqualTo(r.get("expiresOn"),today))),paging(page,size,Sort.by("id").descending())));
    }
    @GetMapping("/admin/banners") Api<Map<String,Object>> banners(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return Api.page(banners.findAll(paging(page,size,Sort.by("id").descending())));}
    Banner apply(Banner b,Banner i) {image(i.getImageUrl());b.setTitle(i.getTitle());b.setDescription(i.getDescription());b.setImageUrl(i.getImageUrl());b.setExpiresOn(i.getExpiresOn());b.setActive(i.isActive());return b;}
    @PostMapping("/admin/banners") @ResponseStatus(HttpStatus.CREATED) Api<Banner> addBanner(@Valid @RequestBody Banner i) {return Api.ok(banners.save(apply(new Banner(),i)));}
    @PutMapping("/admin/banners/{id}") Api<Banner> editBanner(@PathVariable Long id,@Valid @RequestBody Banner i) {return Api.ok(banners.save(apply(found(banners.findById(id)),i)));}
    @DeleteMapping("/admin/banners/{id}") Api<Void> deleteBanner(@PathVariable Long id) {banners.delete(found(banners.findById(id)));return Api.done("Banner deleted.");}
    @GetMapping({"/public/settings","/admin/settings"}) Api<Settings> settings() {return Api.ok(found(settings.findById(1L)));}
    @PutMapping("/admin/settings") Api<Settings> editSettings(@Valid @RequestBody Settings i) {
        var s=found(settings.findById(1L));s.setBusinessName(i.getBusinessName());s.setPhone(i.getPhone());s.setWhatsapp(i.getWhatsapp());s.setEmail(i.getEmail());s.setAddress(i.getAddress());s.setBusinessHours(i.getBusinessHours());s.setTagline(i.getTagline());s.setAnnouncement(i.getAnnouncement());return Api.ok(settings.save(s));
    }
}

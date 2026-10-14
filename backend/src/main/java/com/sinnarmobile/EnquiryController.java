package com.sinnarmobile;

import com.sinnarmobile.Models.*;
import com.sinnarmobile.Requests.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api")
public class EnquiryController {
    private final Enquiries enquiries; private final RepairEnquiries repairs; private final Products products; private final Offers offers;
    public EnquiryController(Enquiries e,RepairEnquiries r,Products p,Offers o) {enquiries=e;repairs=r;products=p;offers=o;}
    private void apply(Submission s,EnquiryInput i) {
        if(i.website()!=null&&!i.website().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unable to submit this enquiry.");
        s.setName(i.name().trim());s.setPhone(i.phone());s.setEmail(i.email());s.setMessage(i.message());
    }
    @PostMapping("/public/enquiries") @ResponseStatus(HttpStatus.CREATED) Api<Map<String,Long>> submit(@Valid @RequestBody EnquiryInput i) {var e=new Enquiry();apply(e,i);return Api.ok(Map.of("reference",enquiries.save(e).getId()));}
    @PostMapping("/public/repair-enquiries") @ResponseStatus(HttpStatus.CREATED) Api<Map<String,Long>> repair(@Valid @RequestBody EnquiryInput i) {
        if(i.device()==null||i.device().isBlank()||i.service()==null||i.service().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Device and service are required.");
        var e=new RepairEnquiry();apply(e,i);e.setDevice(i.device());e.setService(i.service());return Api.ok(Map.of("reference",repairs.save(e).getId()));
    }
    @GetMapping("/admin/enquiries") Api<Map<String,Object>> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) Status status) {
        return Api.page(enquiries.findAll((r,q,c)->status==null?c.conjunction():c.equal(r.get("status"),status),CatalogController.paging(page,size,Sort.by("createdAt").descending())));
    }
    @GetMapping("/admin/repair-enquiries") Api<Map<String,Object>> repairs(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) Status status) {
        return Api.page(repairs.findAll((r,q,c)->status==null?c.conjunction():c.equal(r.get("status"),status),CatalogController.paging(page,size,Sort.by("createdAt").descending())));
    }
    @PatchMapping("/admin/enquiries/{id}") Api<Enquiry> status(@PathVariable Long id,@Valid @RequestBody StatusInput i) {var e=CatalogController.found(enquiries.findById(id));e.setStatus(i.status());return Api.ok(enquiries.save(e));}
    @PatchMapping("/admin/repair-enquiries/{id}") Api<RepairEnquiry> repairStatus(@PathVariable Long id,@Valid @RequestBody StatusInput i) {var e=CatalogController.found(repairs.findById(id));e.setStatus(i.status());return Api.ok(repairs.save(e));}
    @DeleteMapping("/admin/enquiries/{id}") Api<Void> delete(@PathVariable Long id) {enquiries.delete(CatalogController.found(enquiries.findById(id)));return Api.done("Enquiry deleted.");}
    @DeleteMapping("/admin/repair-enquiries/{id}") Api<Void> deleteRepair(@PathVariable Long id) {repairs.delete(CatalogController.found(repairs.findById(id)));return Api.done("Repair enquiry deleted.");}
    @GetMapping("/admin/stats") Api<Map<String,Long>> stats() {return Api.ok(Map.of("products",products.count(),"offers",offers.count(),"enquiries",enquiries.count(),"repairEnquiries",repairs.count(),"newEnquiries",enquiries.countByStatus(Status.NEW)+repairs.countByStatus(Status.NEW)));}
}

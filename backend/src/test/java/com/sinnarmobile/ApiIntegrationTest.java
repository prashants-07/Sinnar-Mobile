package com.sinnarmobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnarmobile.Models.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired Products products;@Autowired Categories categories;
    @Autowired Offers offers;@Autowired Banners banners;@Autowired Enquiries enquiries;@Autowired RepairEnquiries repairs;
    @Autowired AdminUsers admins;@Autowired PasswordEncoder passwords;
    @BeforeEach void clear(){offers.deleteAll();banners.deleteAll();products.deleteAll();enquiries.deleteAll();repairs.deleteAll();}
    String encode(Object o)throws Exception{return json.writeValueAsString(o);}
    long id(MvcResult r)throws Exception{return json.readTree(r.getResponse().getContentAsString()).path("data").path("id").asLong();}
    Map<String,Object> productInput(){return Map.of("name","Phone test","brand","Test brand","categoryId",categories.findAll().getFirst().getId(),"description","Test-only product","availability","ENQUIRE","featured",true,"demo",false);}
    @Test void publicReadEndpointsAndMigrations()throws Exception{
        mvc.perform(get("/api/public/categories")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(17));
        mvc.perform(get("/api/public/settings")).andExpect(status().isOk()).andExpect(jsonPath("$.data.phone").value("7620050500")).andExpect(jsonPath("$.data.email").value("sinnarmobile0@gmail.com"));
        mvc.perform(get("/api/public/faqs")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(10));
        for(String p:new String[]{"products","offers","banners"})mvc.perform(get("/api/public/"+p)).andExpect(status().isOk()).andExpect(jsonPath("$.data.items").isArray());
    }
    @Test void anonymousAdminAccessDenied()throws Exception{
        for(String route:new String[]{"products","categories","offers","banners","enquiries","repair-enquiries","faqs","settings","stats"})mvc.perform(get("/api/admin/"+route)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(productInput()))).andExpect(status().isUnauthorized());
    }
    @Test @WithMockUser(roles="CUSTOMER") void nonAdminDenied()throws Exception{mvc.perform(get("/api/admin/stats")).andExpect(status().isForbidden());}
    @Test @WithMockUser(roles="ADMIN") void csrfRequired()throws Exception{
        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content(encode(productInput()))).andExpect(status().isForbidden());
    }
    @Test void loginSessionAndLogoutWithRealCsrf()throws Exception{
        var tokenResult=mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        var session=(MockHttpSession)tokenResult.getRequest().getSession(false);
        String token=json.readTree(tokenResult.getResponse().getContentAsString()).path("data").path("token").asText();
        mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN",token).param("username","testadmin").param("password","Test-only-password-123!" )).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.data.role").value("ADMIN"));
        var refreshed=mvc.perform(get("/api/auth/csrf").session(session)).andReturn();
        token=json.readTree(refreshed.getResponse().getContentAsString()).path("data").path("token").asText();
        mvc.perform(post("/api/auth/logout").session(session).header("X-CSRF-TOKEN",token)).andExpect(status().isOk());
        assertThat(session.isInvalid()).isTrue();
        assertThat(admins.findByUsername("testadmin").orElseThrow().getPasswordHash()).startsWith("$2");
        assertThat(passwords.matches("Test-only-password-123!",admins.findByUsername("testadmin").orElseThrow().getPasswordHash())).isTrue();
    }
    @Test void invalidLoginAndMissingCsrf()throws Exception{
        mvc.perform(post("/api/auth/login").param("username","testadmin").param("password","bad")).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").with(csrf()).param("username","testadmin").param("password","bad")).andExpect(status().isUnauthorized());
    }
    @Test @WithMockUser(roles="ADMIN") void productCrudSearchFilterAndPaging()throws Exception{
        long id=id(mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(productInput()))).andExpect(status().isCreated()).andExpect(jsonPath("$.data.price").isEmpty()).andReturn());
        mvc.perform(get("/api/public/products").param("search","phone").param("featured","true").param("categoryId",String.valueOf(productInput().get("categoryId")))).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(get("/api/public/products").param("search","missing")).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/public/products").param("search","%")).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/public/products/"+id)).andExpect(status().isOk());
        var updated=new java.util.HashMap<>(productInput());updated.put("name","Updated phone");updated.put("price",100);updated.put("originalPrice",120);
        mvc.perform(put("/api/admin/products/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(updated))).andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("Updated phone"));
        mvc.perform(delete("/api/admin/products/"+id).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/public/products/"+id)).andExpect(status().isNotFound());
    }
    @Test @WithMockUser(roles="ADMIN") void invalidProductsAndImagePathsRejected()throws Exception{
        var input=new java.util.HashMap<>(productInput());input.put("name","");
        mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(input))).andExpect(status().isBadRequest());
        input.put("name","Test");input.put("price",-1);
        mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(input))).andExpect(status().isBadRequest());
        input.put("price",100);input.put("originalPrice",50);
        mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(input))).andExpect(status().isBadRequest());
        input.remove("originalPrice");input.put("imageUrl","javascript:alert(1)");
        mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(input))).andExpect(status().isBadRequest());
        input.remove("imageUrl");input.put("categoryId",999999);
        mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(input))).andExpect(status().isNotFound());
    }
    @Test void pagingAndSortValidation()throws Exception{
        mvc.perform(get("/api/public/products").param("size","101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/public/products").param("page","-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/public/products").param("sort","passwordHash")).andExpect(status().isBadRequest());
    }
    @Test @WithMockUser(roles="ADMIN") void categoryCrudAndForeignKeyProtection()throws Exception{
        long id=id(mvc.perform(post("/api/admin/categories").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Test category\"}")).andExpect(status().isCreated()).andReturn());
        mvc.perform(put("/api/admin/categories/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated category\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/admin/categories/"+id).with(csrf())).andExpect(status().isOk());
        mvc.perform(post("/api/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(productInput()))).andExpect(status().isCreated());
        mvc.perform(delete("/api/admin/categories/"+productInput().get("categoryId")).with(csrf())).andExpect(status().isConflict());
    }
    @Test @WithMockUser(roles="ADMIN") void offerCrudAndExpiry()throws Exception{
        var body=new java.util.HashMap<String,Object>();body.put("title","Test offer");body.put("description","Actual terms supplied by admin");body.put("expiresOn",LocalDate.now().plusDays(1).toString());body.put("active",true);
        long id=id(mvc.perform(post("/api/admin/offers").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(body))).andExpect(status().isCreated()).andReturn());
        mvc.perform(get("/api/public/offers")).andExpect(jsonPath("$.data.totalElements").value(1));
        body.put("expiresOn",LocalDate.now().minusDays(1).toString());
        mvc.perform(put("/api/admin/offers/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(body))).andExpect(status().isOk());
        mvc.perform(get("/api/public/offers")).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(delete("/api/admin/offers/"+id).with(csrf())).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles="ADMIN") void bannerCrudAndVisibility()throws Exception{
        var body=new java.util.HashMap<String,Object>();body.put("title","Test banner");body.put("description","Promotional content");body.put("active",true);
        long id=id(mvc.perform(post("/api/admin/banners").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(body))).andExpect(status().isCreated()).andReturn());
        mvc.perform(get("/api/public/banners")).andExpect(jsonPath("$.data.totalElements").value(1));
        body.put("active",false);mvc.perform(put("/api/admin/banners/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(body))).andExpect(status().isOk());
        mvc.perform(get("/api/public/banners")).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(delete("/api/admin/banners/"+id).with(csrf())).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles="ADMIN") void faqCrud()throws Exception{
        String body="{\"question\":\"Test question?\",\"answer\":\"Test answer\",\"displayOrder\":11}";
        long id=id(mvc.perform(post("/api/admin/faqs").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn());
        mvc.perform(put("/api/admin/faqs/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(delete("/api/admin/faqs/"+id).with(csrf())).andExpect(status().isOk());
    }
    String enquiryBody(){return "{\"name\":\"Test customer\",\"phone\":\"7620050500\",\"email\":\"test@example.com\",\"message\":\"Product enquiry\"}";}
    @Test void customerEnquirySavedWithoutExposingPersonalData()throws Exception{
        mvc.perform(post("/api/public/enquiries").contentType(MediaType.APPLICATION_JSON).content(enquiryBody())).andExpect(status().isCreated()).andExpect(jsonPath("$.data.reference").isNumber()).andExpect(jsonPath("$.data.phone").doesNotExist());
        assertThat(enquiries.count()).isEqualTo(1);mvc.perform(get("/api/public/enquiries")).andExpect(status().isMethodNotAllowed());
    }
    @Test void enquiryValidationAndRepairSubmission()throws Exception{
        mvc.perform(post("/api/public/enquiries").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"phone\":\"123\",\"message\":\"\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/public/repair-enquiries").contentType(MediaType.APPLICATION_JSON).content(enquiryBody())).andExpect(status().isBadRequest());
        var body=json.readValue(enquiryBody(),Map.class);body.put("device","Test phone");body.put("service","Screen Replacement");
        mvc.perform(post("/api/public/repair-enquiries").contentType(MediaType.APPLICATION_JSON).content(encode(body))).andExpect(status().isCreated());assertThat(repairs.count()).isEqualTo(1);
    }
    @Test @WithMockUser(roles="ADMIN") void enquiryStatusListDeleteAndStats()throws Exception{
        mvc.perform(post("/api/public/enquiries").contentType(MediaType.APPLICATION_JSON).content(enquiryBody())).andExpect(status().isCreated());long id=enquiries.findAll().getFirst().getId();
        mvc.perform(get("/api/admin/enquiries")).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(patch("/api/admin/enquiries/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/enquiries").param("status","NEW")).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/admin/stats")).andExpect(jsonPath("$.data.enquiries").value(1));
        mvc.perform(delete("/api/admin/enquiries/"+id).with(csrf())).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles="ADMIN") void imageUploadValidAndSpoofedFile()throws Exception{
        var bytes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(8,8,BufferedImage.TYPE_INT_RGB),"png",bytes);
        var result=mvc.perform(multipart("/api/admin/uploads").file(new MockMultipartFile("file","../../photo.png","image/png",bytes.toByteArray())).with(csrf())).andExpect(status().isCreated()).andReturn();
        String url=json.readTree(result.getResponse().getContentAsString()).path("data").path("url").asText();assertThat(url).matches("/uploads/[a-f0-9-]+\\.png");
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(multipart("/api/admin/uploads").file(new MockMultipartFile("file","fake.jpg","image/jpeg","<script>alert(1)</script>".getBytes())).with(csrf())).andExpect(status().isBadRequest());
        Files.deleteIfExists(Path.of("target/test-uploads").resolve(url.substring("/uploads/".length())));
    }
    @Test void corsApprovedAndUnapprovedOrigins()throws Exception{
        mvc.perform(options("/api/public/products").header("Origin","https://prashants-07.github.io").header("Access-Control-Request-Method","GET")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","https://prashants-07.github.io"));
        mvc.perform(options("/api/admin/products").header("Origin","https://unapproved.example").header("Access-Control-Request-Method","POST")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="ADMIN") void settingsCanBeUpdatedAndInvalidPhoneRejected()throws Exception{
        var current=mvc.perform(get("/api/admin/settings")).andReturn();
        var original=json.readTree(current.getResponse().getContentAsString()).path("data");
        var body=(com.fasterxml.jackson.databind.node.ObjectNode)original.deepCopy();body.put("announcement","Test-only announcement");
        try {
            mvc.perform(put("/api/admin/settings").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(body))).andExpect(status().isOk()).andExpect(jsonPath("$.data.announcement").value("Test-only announcement"));
            body.put("phone","123");mvc.perform(put("/api/admin/settings").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(body))).andExpect(status().isBadRequest());
        }finally{mvc.perform(put("/api/admin/settings").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(encode(original))).andExpect(status().isOk());}
    }
    @Test @WithMockUser(roles="ADMIN") void repairStatusCrud()throws Exception{
        var repair=new RepairEnquiry();repair.setName("Test customer");repair.setPhone("7620050500");repair.setMessage("Repair question");repair.setDevice("Test device");repair.setService("Battery Replacement");long id=repairs.save(repair).getId();
        mvc.perform(get("/api/admin/repair-enquiries")).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(patch("/api/admin/repair-enquiries/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/admin/repair-enquiries/"+id).with(csrf())).andExpect(status().isOk());
    }
    @Test void rateLimitAndHoneypot()throws Exception{
        for(int n=0;n<5;n++)mvc.perform(post("/api/public/enquiries").with(r->{r.setRemoteAddr("10.200.0.1");return r;}).contentType(MediaType.APPLICATION_JSON).content(enquiryBody())).andExpect(status().isCreated());
        mvc.perform(post("/api/public/enquiries").with(r->{r.setRemoteAddr("10.200.0.1");return r;}).contentType(MediaType.APPLICATION_JSON).content(enquiryBody())).andExpect(status().isTooManyRequests());
        var bot=json.readValue(enquiryBody(),Map.class);bot.put("website","bot spam");
        mvc.perform(post("/api/public/enquiries").with(r->{r.setRemoteAddr("10.200.0.2");return r;}).contentType(MediaType.APPLICATION_JSON).content(encode(bot))).andExpect(status().isBadRequest());
    }
    @Test void adminDirectoryRouteAndMissingResource()throws Exception{
        mvc.perform(get("/admin/")).andExpect(status().isOk()).andExpect(forwardedUrl("/admin/index.html"));
        mvc.perform(get("/missing-image.png")).andExpect(status().isNotFound());
    }
}

package com.sinnarmobile;

import com.sinnarmobile.Models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Bootstrap implements CommandLineRunner {
    private final AdminUsers admins;private final Products products;private final Categories categories;private final PasswordEncoder passwords;
    @Value("${app.admin-username}") String username; @Value("${app.admin-password}") String password; @Value("${app.seed-demo}") boolean demo;
    public Bootstrap(AdminUsers a,Products p,Categories c,PasswordEncoder b) {admins=a;products=p;categories=c;passwords=b;}
    @Override public void run(String... args) {
        if(admins.count()==0) {
            if(username.isBlank()||username.length()>100||password.length()<12||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalStateException("Set ADMIN_USERNAME (1–100 characters) and ADMIN_PASSWORD (at least 12 characters, at most 72 UTF-8 bytes) for first startup.");
            var admin=new AdminUser();admin.setUsername(username);admin.setPasswordHash(passwords.encode(password));admins.save(admin);
        }
        if(demo&&products.count()==0) {
            String[][] rows={{"Smartphone collection","Smartphones","iphone1"},{"Designer phone covers","Mobile Covers","case1"},{"Everyday mobile accessories","Other Accessories","Accessories1"},{"Smart watch collection","Smart Watches","watch1"}};
            for(String[] row:rows) {
                var category=categories.findAll().stream().filter(c->c.getName().equals(row[1])).findFirst().orElseThrow();
                var p=new Product();p.setName(row[0]);p.setCategoryId(category.getId());p.setDescription("Demo listing for layout preview. Contact the shop for actual models, prices and availability.");p.setImageUrl("assets/images/"+row[2]+".webp");p.setDemo(true);p.setFeatured(true);products.save(p);
            }
        }
    }
}

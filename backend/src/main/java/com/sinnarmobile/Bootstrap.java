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
    @Value("${app.admin-password-reset-id:}") String resetId="";
    public Bootstrap(AdminUsers a,Products p,Categories c,PasswordEncoder b) {admins=a;products=p;categories=c;passwords=b;}
    @Override public void run(String... args) {
        if(admins.count()==0) {
            validateCredentials();
            var admin=new AdminUser();admin.setUsername(username);admin.setPasswordHash(passwords.encode(password));admin.setPasswordResetId(resetId.isBlank()?null:resetId);admins.save(admin);
        } else if(!resetId.isBlank()) {
            var admin=admins.findByUsername(username).orElseThrow(()->new IllegalStateException("Password reset requires ADMIN_USERNAME to match an existing administrator. Check admin_users in your database."));
            if(!resetId.equals(admin.getPasswordResetId())) {
                validateCredentials();admin.setPasswordHash(passwords.encode(password));admin.setPasswordResetId(resetId);admins.save(admin);
            }
        }
        if(demo&&products.count()==0) {
            String[][] rows={{"Smartphone collection","Smartphones","iphone1"},{"Designer phone covers","Mobile Covers","case1"},{"Everyday mobile accessories","Other Accessories","Accessories1"},{"Smart watch collection","Smart Watches","watch1"}};
            for(String[] row:rows) {
                var category=categories.findAll().stream().filter(c->c.getName().equals(row[1])).findFirst().orElseThrow();
                var p=new Product();p.setName(row[0]);p.setCategoryId(category.getId());p.setDescription("Demo listing for layout preview. Contact the shop for actual models, prices and availability.");p.setImageUrl("assets/images/"+row[2]+".webp");p.setDemo(true);p.setFeatured(true);products.save(p);
            }
        }
    }
    private void validateCredentials() {
        if(username.isBlank()||username.length()>100||password.length()<12||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalStateException("Set ADMIN_USERNAME (1–100 characters) and ADMIN_PASSWORD (at least 12 characters, at most 72 UTF-8 bytes).");
        if(!resetId.isBlank()&&!resetId.matches("[A-Za-z0-9_-]{1,64}"))
            throw new IllegalStateException("ADMIN_PASSWORD_RESET_ID must contain 1–64 letters, digits, underscores or hyphens.");
    }
}

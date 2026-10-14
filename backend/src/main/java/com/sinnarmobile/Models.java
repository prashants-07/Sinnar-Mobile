package com.sinnarmobile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

public class Models {
    @MappedSuperclass @Getter @Setter
    public abstract static class Base {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
        @Column(nullable=false,updatable=false) private Instant createdAt;
        @Column(nullable=false) private Instant updatedAt;
        @PrePersist void create() { createdAt=Instant.now(); updatedAt=createdAt; }
        @PreUpdate void update() { updatedAt=Instant.now(); }
    }
    @Entity @Table(name="admin_users") @Getter @Setter
    public static class AdminUser extends Base {
        @Column(nullable=false,unique=true,length=100) private String username;
        @JsonIgnore @Column(nullable=false) private String passwordHash;
        @JsonIgnore @Column(length=64) private String passwordResetId;
        @Column(nullable=false,length=20) private String role="ADMIN";
    }
    @Entity @Table(name="categories") @Getter @Setter
    public static class Category extends Base {
        @NotBlank @Size(max=100) @Column(nullable=false,unique=true,length=100) private String name;
        @Size(max=500) @Column(length=500) private String description;
    }
    public enum Availability { ENQUIRE, AVAILABLE, OUT_OF_STOCK, PRE_ORDER }
    @Entity @Table(name="products") @Getter @Setter
    public static class Product extends Base {
        @Column(nullable=false,length=150) private String name;
        @Column(length=100) private String brand;
        @Column(nullable=false) private Long categoryId;
        @Column(nullable=false,length=2000) private String description;
        @Column(length=500) private String imageUrl;
        @Column(precision=12,scale=2) private BigDecimal price;
        @Column(precision=12,scale=2) private BigDecimal originalPrice;
        @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Availability availability=Availability.ENQUIRE;
        @Column(nullable=false) private boolean featured;
        @Column(nullable=false) private boolean demo;
    }
    @Entity @Table(name="offers") @Getter @Setter
    public static class Offer extends Base {
        @NotBlank @Size(max=150) @Column(nullable=false,length=150) private String title;
        @NotBlank @Size(max=2000) @Column(nullable=false,length=2000) private String description;
        @Size(max=500) private String imageUrl;
        private Long productId;
        @NotNull @Column(nullable=false) private LocalDate expiresOn;
        @Column(nullable=false) private boolean active=true;
    }
    @Entity @Table(name="banners") @Getter @Setter
    public static class Banner extends Base {
        @NotBlank @Size(max=150) @Column(nullable=false,length=150) private String title;
        @NotBlank @Size(max=1000) @Column(nullable=false,length=1000) private String description;
        @Size(max=500) private String imageUrl;
        private LocalDate expiresOn;
        @Column(nullable=false) private boolean active=true;
    }
    @Entity @Table(name="faqs") @Getter @Setter
    public static class Faq extends Base {
        @NotBlank @Size(max=300) @Column(nullable=false,length=300) private String question;
        @NotBlank @Size(max=2000) @Column(nullable=false,length=2000) private String answer;
        @Min(0) @Column(nullable=false) private int displayOrder;
    }
    public enum Status { NEW, IN_PROGRESS, CLOSED }
    @MappedSuperclass @Getter @Setter
    public abstract static class Submission extends Base {
        @Column(nullable=false,length=100) private String name;
        @Column(nullable=false,length=20) private String phone;
        @Column(length=254) private String email;
        @Column(nullable=false,length=2000) private String message;
        @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status=Status.NEW;
    }
    @Entity @Table(name="enquiries") @Getter @Setter
    public static class Enquiry extends Submission {}
    @Entity @Table(name="repair_enquiries") @Getter @Setter
    public static class RepairEnquiry extends Submission {
        @Column(nullable=false,length=150) private String device;
        @Column(nullable=false,length=100) private String service;
    }
    @Entity @Table(name="website_settings") @Getter @Setter
    public static class Settings extends Base {
        @NotBlank @Size(max=100) @Column(nullable=false,length=100) private String businessName;
        @NotBlank @Pattern(regexp="[0-9]{10}") @Column(nullable=false,length=20) private String phone;
        @NotBlank @Pattern(regexp="[0-9]{10,15}") @Column(nullable=false,length=20) private String whatsapp;
        @NotBlank @Email @Size(max=254) @Column(nullable=false,length=254) private String email;
        @NotBlank @Size(max=500) @Column(nullable=false,length=500) private String address;
        @NotBlank @Size(max=1000) @Column(nullable=false,length=1000) private String businessHours;
        @NotBlank @Size(max=200) @Column(nullable=false,length=200) private String tagline;
        @NotBlank @Size(max=300) @Column(nullable=false,length=300) private String announcement;
    }
}

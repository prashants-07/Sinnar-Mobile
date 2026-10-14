package com.sinnarmobile;

import com.sinnarmobile.Models.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface AdminUsers extends JpaRepository<AdminUser,Long> { Optional<AdminUser> findByUsername(String username); }
interface Categories extends JpaRepository<Category,Long> {}
interface Products extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> { boolean existsByCategoryId(Long id); }
interface Offers extends JpaRepository<Offer,Long>, JpaSpecificationExecutor<Offer> { boolean existsByProductId(Long id); }
interface Banners extends JpaRepository<Banner,Long>, JpaSpecificationExecutor<Banner> {}
interface Faqs extends JpaRepository<Faq,Long> {}
interface Enquiries extends JpaRepository<Enquiry,Long>, JpaSpecificationExecutor<Enquiry> { long countByStatus(Status status); }
interface RepairEnquiries extends JpaRepository<RepairEnquiry,Long>, JpaSpecificationExecutor<RepairEnquiry> { long countByStatus(Status status); }
interface WebsiteSettings extends JpaRepository<Settings,Long> {}

package com.sinnarmobile;

import com.sinnarmobile.Models.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class Requests {
    public record ProductInput(
        @NotBlank @Size(max=150) String name,
        @Size(max=100) String brand,
        @NotNull @Positive Long categoryId,
        @NotBlank @Size(max=2000) String description,
        @Size(max=500) String imageUrl,
        @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal price,
        @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal originalPrice,
        @NotNull Availability availability, boolean featured, boolean demo) {}
    public record EnquiryInput(
        @NotBlank @Size(max=100) String name,
        @NotBlank @Pattern(regexp="(?=(?:\\D*\\d){10,15}\\D*$)\\+?[0-9 ()-]{10,20}") String phone,
        @Email @Size(max=254) String email,
        @NotBlank @Size(max=2000) String message,
        @Size(max=150) String device, @Size(max=100) String service,
        @Size(max=100) String website) {}
    public record StatusInput(@NotNull Status status) {}
}

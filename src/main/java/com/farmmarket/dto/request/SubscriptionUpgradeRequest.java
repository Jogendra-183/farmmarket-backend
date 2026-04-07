package com.farmmarket.dto.request;

import com.farmmarket.entity.Subscription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionUpgradeRequest {

    @NotNull(message = "Plan type is required")
    private Subscription.PlanType planType;

    // Personal Details
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "ZIP code is required")
    private String zipCode;

    // Payment Details (last 4 digits only - never store full card)
    private String cardLastFour;

    @NotBlank(message = "Billing address is required")
    private String billingAddress;
}

package com.payment.common.enums;

/**
 * User roles for Role-Based Access Control (RBAC).
 * Used by Spring Security to control endpoint access.
 */
public enum UserRole {

    /** Standard customer — can browse products, place orders */
    CUSTOMER,

    /** Administrator — can manage products, inventory, view all orders */
    ADMIN
}

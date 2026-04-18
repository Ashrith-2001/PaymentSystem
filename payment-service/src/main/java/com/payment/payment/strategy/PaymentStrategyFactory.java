package com.payment.payment.strategy;

import com.payment.common.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * FACTORY PATTERN — Resolves the correct PaymentStrategy at runtime.
 * 
 * This factory maps PaymentMethod enum values to their corresponding
 * strategy implementations. When a payment request arrives, the factory
 * looks up the right strategy without the caller needing to know which
 * concrete class to use.
 * 
 * Adding a new payment method:
 * 1. Add the enum value to PaymentMethod
 * 2. Create a new class implementing PaymentStrategy
 * 3. Register it in this factory's constructor
 * 
 * This follows the Open/Closed Principle.
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    /**
     * Constructor injection — Spring injects all PaymentStrategy beans.
     * We map them by PaymentMethod for O(1) lookup.
     */
    public PaymentStrategyFactory(
            CreditCardPayment creditCardPayment,
            WalletPayment walletPayment,
            UpiPayment upiPayment) {

        strategies = new EnumMap<>(PaymentMethod.class);
        strategies.put(PaymentMethod.CREDIT_CARD, creditCardPayment);
        strategies.put(PaymentMethod.DEBIT_CARD, creditCardPayment);  // Same gateway for debit
        strategies.put(PaymentMethod.WALLET, walletPayment);
        strategies.put(PaymentMethod.UPI, upiPayment);
        strategies.put(PaymentMethod.NET_BANKING, creditCardPayment); // Reuse for demo
    }

    /**
     * Get the appropriate payment strategy for the given method.
     * 
     * @param method the payment method
     * @return the corresponding PaymentStrategy
     * @throws IllegalArgumentException if no strategy exists for the method
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("No payment strategy found for method: " + method);
        }
        return strategy;
    }
}

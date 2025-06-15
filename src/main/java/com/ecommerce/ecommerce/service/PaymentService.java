package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.PaymentDetail;
import com.ecommerce.ecommerce.entity.PaymentMethod;
import com.ecommerce.ecommerce.model.PaymentDetailDto;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class PaymentService {

    public void validatePaymentDetail(PaymentDetailDto paymentDto) {
        if (paymentDto.getPaymentMethod() == PaymentMethod.CARD) {
            validateCardPayment(paymentDto);
        }
        // COD doesn't need additional validation
    }

    private void validateCardPayment(PaymentDetailDto paymentDto) {
        // Check required fields for card payment
        if (paymentDto.getCardHolderName() == null || paymentDto.getCardHolderName().trim().isEmpty()) {
            throw new RuntimeException("Card holder name is required for card payment");
        }

        if (paymentDto.getCardNumber() == null || paymentDto.getCardNumber().trim().isEmpty()) {
            throw new RuntimeException("Card number is required for card payment");
        }

        if (paymentDto.getCvv() == null || paymentDto.getCvv().trim().isEmpty()) {
            throw new RuntimeException("CVV is required for card payment");
        }

        if (paymentDto.getExpiryDate() == null || paymentDto.getExpiryDate().trim().isEmpty()) {
            throw new RuntimeException("Expiry date is required for card payment");
        }

        // Validate card number (Luhn algorithm - simplified)
        validateCardNumber(paymentDto.getCardNumber());

        // Validate expiry date
        validateExpiryDate(paymentDto.getExpiryDate());
    }

    private void validateCardNumber(String cardNumber) {
        // Remove spaces and check if all digits
        String cleanCardNumber = cardNumber.replaceAll("\\s", "");
        if (!cleanCardNumber.matches("^[0-9]{13,19}$")) {
            throw new RuntimeException("Invalid card number format");
        }

        // Basic Luhn algorithm validation
        if (!isValidLuhn(cleanCardNumber)) {
            throw new RuntimeException("Invalid card number");
        }
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }

    private void validateExpiryDate(String expiryDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            YearMonth expiry = YearMonth.parse(expiryDate, formatter);
            YearMonth now = YearMonth.now();

            if (expiry.isBefore(now)) {
                throw new RuntimeException("Card has expired");
            }
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid expiry date format. Use MM/YYYY");
        }
    }

    public PaymentDetail createPaymentDetail(PaymentDetailDto paymentDto) {
        validatePaymentDetail(paymentDto);

        PaymentDetail paymentDetail = new PaymentDetail();
        paymentDetail.setPaymentMethod(paymentDto.getPaymentMethod());

        if (paymentDto.getPaymentMethod() == PaymentMethod.CARD) {
            paymentDetail.setCardHolderName(paymentDto.getCardHolderName());
            // Mask card number for security (store only last 4 digits)
            paymentDetail.setCardNumber("****-****-****-" + paymentDto.getCardNumber().substring(paymentDto.getCardNumber().length() - 4));
            // Don't store CVV for security reasons
            paymentDetail.setCvv("***");
            paymentDetail.setExpiryDate(paymentDto.getExpiryDate());
        }

        return paymentDetail;
    }

    public PaymentDetailDto mapToDto(PaymentDetail paymentDetail) {
        if (paymentDetail == null) return null;

        PaymentDetailDto dto = new PaymentDetailDto();
        dto.setPaymentMethod(paymentDetail.getPaymentMethod());

        if (paymentDetail.getPaymentMethod() == PaymentMethod.CARD) {
            dto.setCardHolderName(paymentDetail.getCardHolderName());
            dto.setCardNumber(paymentDetail.getCardNumber()); // Already masked
            dto.setCvv(paymentDetail.getCvv()); // Already masked
            dto.setExpiryDate(paymentDetail.getExpiryDate());
        }

        return dto;
    }
}

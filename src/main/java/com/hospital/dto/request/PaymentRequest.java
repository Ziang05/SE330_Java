package com.hospital.dto.request;

import com.hospital.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload để xác nhận thanh toán một hóa đơn (T39).
 * Client gửi: ID hóa đơn + phương thức thanh toán.
 */
@Getter
@Setter
@NoArgsConstructor
public class PaymentRequest {

    @NotNull(message = "ID hóa đơn không được để trống")
    private Long invoiceId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
}

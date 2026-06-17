package com.hospital.service;

import com.hospital.dto.response.SpendingInvoiceItem;
import com.hospital.dto.response.SpendingSummaryResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service cung cấp dữ liệu chi tiêu cho bệnh nhân đã đăng nhập.
 *
 * <p>Quy tắc nghiệp vụ:
 * <ul>
 *   <li>Chỉ hóa đơn có trạng thái PAID mới được tính vào tổng chi tiêu.</li>
 *   <li>patientId phải được lấy từ JWT token (UserPrincipal), không nhận từ client.</li>
 * </ul>
 */
public interface PatientSpendingService {

    /**
     * Trả về tổng hợp chi tiêu của bệnh nhân trong khoảng thời gian.
     * Chỉ tính hóa đơn PAID.
     *
     * @param patientId ID bệnh nhân (lấy từ JWT, không từ request)
     * @param from      Ngày bắt đầu (null → 1 năm trước)
     * @param to        Ngày kết thúc (null → hôm nay)
     */
    SpendingSummaryResponse getSummary(Long patientId, LocalDate from, LocalDate to);

    /**
     * Trả về danh sách toàn bộ hóa đơn (mọi trạng thái) trong khoảng thời gian.
     * Mỗi item có flag {@code countedInSpending} để phân biệt hóa đơn đã thanh toán.
     *
     * @param patientId ID bệnh nhân (lấy từ JWT, không từ request)
     * @param from      Ngày bắt đầu (null → 1 năm trước)
     * @param to        Ngày kết thúc (null → hôm nay)
     */
    List<SpendingInvoiceItem> getInvoiceHistory(Long patientId, LocalDate from, LocalDate to);
}

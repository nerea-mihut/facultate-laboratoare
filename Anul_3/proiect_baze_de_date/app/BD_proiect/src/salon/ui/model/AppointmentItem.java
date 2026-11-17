package salon.ui.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentItem(
        int id,
        LocalDate date,
        LocalTime time,
        String status,
        String cancellationReason,
        String clientLastName,
        String clientFirstName,
        String clientPhone,
        String clientEmail,
        String allergies,
        String allergyDetails,
        int totalDuration,
        BigDecimal totalPrice
) {
}
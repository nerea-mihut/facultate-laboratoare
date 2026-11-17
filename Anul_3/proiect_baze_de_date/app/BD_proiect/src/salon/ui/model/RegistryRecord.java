package salon.ui.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record RegistryRecord(
        int id,
        LocalDate date,
        String device,
        String material,
        BigDecimal temperature,
        LocalTime startTime,
        LocalTime endTime,
        String testReference,
        int employeeId
) {
}
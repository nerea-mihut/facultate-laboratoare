package salon.ui.model;

import java.math.BigDecimal;

public record ServiceOption(
        int serviceId,
        String serviceName,
        String optionName,
        BigDecimal price,
        int durationMinutes,
        String details
) {

    public String displayName() {
        if (optionName == null || optionName.isBlank()) {
            return serviceName;
        }

        return serviceName + " · " + optionName;
    }
}
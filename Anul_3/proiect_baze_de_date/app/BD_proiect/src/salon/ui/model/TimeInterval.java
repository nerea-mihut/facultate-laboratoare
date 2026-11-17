package salon.ui.model;

import java.time.LocalDateTime;

public record TimeInterval(
        LocalDateTime start,
        LocalDateTime end,
        String type
) {

    public TimeInterval(
            LocalDateTime start,
            LocalDateTime end
    ) {
        this(start, end, "Lucru");
    }

    public boolean overlaps(TimeInterval other) {
        return start.isBefore(other.end())
                && end.isAfter(other.start());
    }
}
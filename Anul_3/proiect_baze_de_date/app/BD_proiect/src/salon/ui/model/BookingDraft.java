package salon.ui.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDraft {

    private Integer technicianId;
    private LocalDate date;
    private LocalTime time;

    private final List<ServiceOption> selectedServices =
            new ArrayList<>();

    public Integer getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public List<ServiceOption> getSelectedServices() {
        return selectedServices;
    }

    public void addService(ServiceOption option) {
        selectedServices.add(option);
    }

    public void clearServices() {
        selectedServices.clear();
    }

    public int getTotalDuration() {
        return selectedServices.stream()
                .mapToInt(ServiceOption::durationMinutes)
                .sum();
    }

    public BigDecimal getTotalPrice() {
        return selectedServices.stream()
                .map(ServiceOption::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isReadyToBook() {
        return technicianId != null
                && date != null
                && time != null
                && !selectedServices.isEmpty();
    }
}
package salon.ai;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Înțelege perioade scrise în română, fără să depindă de un serviciu extern. */
public final class PeriodParser {

    public record Period(LocalDate from, LocalDate to, String label) {
    }

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String[] MONTHS = {
            "ianuarie", "februarie", "martie", "aprilie", "mai", "iunie",
            "iulie", "august", "septembrie", "octombrie", "noiembrie", "decembrie"
    };

    private PeriodParser() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }

        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static Period period(LocalDate from, LocalDate to) {
        String label = from.equals(to)
                ? from.format(FORMAT)
                : from.format(FORMAT) + " - " + to.format(FORMAT);

        return new Period(from, to, label);
    }

    public static Period parse(String text, LocalDate today) {
        String t = normalize(text);

        Matcher dateMatcher =
                Pattern.compile("(\\d{1,2})[./-](\\d{1,2})[./-](\\d{4})").matcher(t);

        List<LocalDate> dates = new ArrayList<>();

        while (dateMatcher.find()) {
            try {
                dates.add(LocalDate.of(
                        Integer.parseInt(dateMatcher.group(3)),
                        Integer.parseInt(dateMatcher.group(2)),
                        Integer.parseInt(dateMatcher.group(1))
                ));
            } catch (DateTimeException ignored) {
                // dată inexistentă, o ignorăm
            }
        }

        if (dates.size() >= 2) {
            LocalDate first = dates.get(0);
            LocalDate second = dates.get(1);

            return first.isBefore(second)
                    ? period(first, second)
                    : period(second, first);
        }

        if (dates.size() == 1) {
            return period(dates.get(0), dates.get(0));
        }

        if (t.contains("luna trecuta") || t.contains("luna precedenta")) {
            YearMonth month = YearMonth.from(today).minusMonths(1);
            return period(month.atDay(1), month.atEndOfMonth());
        }

        if (t.contains("luna aceasta") || t.contains("luna asta") || t.contains("luna curenta")) {
            YearMonth month = YearMonth.from(today);
            return period(month.atDay(1), month.atEndOfMonth());
        }

        if (t.contains("saptamana trecuta")) {
            LocalDate monday = today.with(DayOfWeek.MONDAY).minusWeeks(1);
            return period(monday, monday.plusDays(6));
        }

        if (t.contains("saptamana aceasta") || t.contains("saptamana asta")
                || t.contains("saptamana curenta")) {
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            return period(monday, monday.plusDays(6));
        }

        if (t.contains("anul trecut")) {
            int year = today.getYear() - 1;
            return period(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        }

        if (t.contains("anul acesta") || t.contains("anul asta") || t.contains("anul curent")) {
            int year = today.getYear();
            return period(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        }

        Matcher lastDays =
                Pattern.compile("ultimele\\s+(\\d{1,3})\\s+(?:zile|zi)").matcher(t);

        if (lastDays.find()) {
            int days = Math.max(1, Integer.parseInt(lastDays.group(1)));
            return period(today.minusDays(days - 1L), today);
        }

        if (t.contains("ultima luna")) {
            return period(today.minusDays(29), today);
        }

        if (Pattern.compile("\\bieri\\b").matcher(t).find()) {
            return period(today.minusDays(1), today.minusDays(1));
        }

        if (Pattern.compile("\\b(?:azi|astazi)\\b").matcher(t).find()) {
            return period(today, today);
        }

        for (int index = 0; index < MONTHS.length; index++) {
            String name = MONTHS[index];

            String regex = "mai".equals(name)
                    ? "\\bluna\\s+mai\\b"
                    : "\\b(?:luna|in|din)\\s+" + name + "\\b";

            if (Pattern.compile(regex).matcher(t).find()) {
                YearMonth month = YearMonth.of(today.getYear(), index + 1);

                if (month.isAfter(YearMonth.from(today))) {
                    month = month.minusYears(1);
                }

                return period(month.atDay(1), month.atEndOfMonth());
            }
        }

        return null;
    }
}

package salon.ai;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Decide ce face asistentul. Modelul AI nu execută SQL și nu salvează nimic:
 * cifrele vin din ReportService, iar programările rămân în fluxul existent.
 */
public final class AssistantEngine {

    public record Reply(String text, boolean offerBooking) {
    }

    private static final String SYSTEM_GENERAL =
            "Ești asistentul unei aplicații pentru un salon de manichiură și o academie de nail art. "
                    + "Răspunde scurt, în română. Nu ai acces la date despre clienți și nu poți modifica nimic. "
                    + "Dacă nu știi un răspuns, spune asta.";

    private static final String SYSTEM_COURSES =
            "Ești asistentul unei academii de nail art. Răspunde scurt, în română. "
                    + "Recomandă DOAR din lista de sesiuni primită. Nu inventa cursuri, prețuri, date sau traineri. "
                    + "Dacă nu există o potrivire, spune asta. Nu promite locuri: clientul se înscrie "
                    + "din secțiunea Cursuri a aplicației.";

    private final boolean client;
    private final int employeeId;
    private final boolean technician;
    private final boolean trainer;

    private final AiClient ai = new AiClient();
    private final ReportService reports = new ReportService();
    private final CourseAdvisor advisor = new CourseAdvisor();

    private AssistantEngine(boolean client, int employeeId, boolean technician, boolean trainer) {
        this.client = client;
        this.employeeId = employeeId;
        this.technician = technician;
        this.trainer = trainer;
    }

    public static AssistantEngine forClient() {
        return new AssistantEngine(true, 0, false, false);
    }

    public static AssistantEngine forEmployee(int employeeId, boolean technician, boolean trainer) {
        return new AssistantEngine(false, employeeId, technician, trainer);
    }

    public String greeting() {
        String mode = AiConfig.enabled()
                ? ""
                : "\n\n(Mod de bază: nu este configurată o cheie API, deci răspund doar din datele aplicației.)";

        if (client) {
            return "Bună! Te pot ajuta să alegi un curs sau un trainer și să începi o programare. "
                    + "Întreabă-mă, de exemplu: «ce curs să aleg ca începătoare?»" + mode;
        }

        return "Bună! Pot genera rapoarte pentru perioada dorită, de exemplu: "
                + "«raport pentru luna trecută» sau «câți clienți am avut în ultimele 30 de zile»." + mode;
    }

    public Reply handle(String userText) throws Exception {
        String t = PeriodParser.normalize(userText);

        if (t.isEmpty()) {
            return new Reply("Scrie o întrebare ca să te pot ajuta.", false);
        }

        return client ? handleClient(userText, t) : handleEmployee(userText, t);
    }

    private Reply handleClient(String userText, String t) throws Exception {
        if (containsAny(t, "raport", "venit", "bani", "incasat")) {
            return new Reply("Rapoartele sunt disponibile pentru tehnicieni și traineri.", false);
        }

        if (containsAny(t, "curs", "trainer", "academie", "cursant", "invat",
                "nivel", "incepator", "avansat", "master")) {
            return suggestCourses(userText);
        }

        if (containsAny(t, "programare", "programa", "rezerv", "manichiur", "oja",
                "construct", "intretin", "slim", "ora libera", "liber")) {
            return new Reply(
                    "Te pot duce la programări. Acolo alegi tehniciana, serviciul și ora. "
                            + "Zilele și orele afișate țin cont de serviciile pe care tehniciana "
                            + "le oferă în ziua respectivă, iar programarea o confirmi tu.",
                    true
            );
        }

        return general(userText);
    }

    private Reply handleEmployee(String userText, String t) throws Exception {
        PeriodParser.Period period = PeriodParser.parse(userText, LocalDate.now());

        boolean wantsReport = containsAny(t, "raport", "venit", "bani", "incasat", "castig",
                "client", "cursant", "programari", "sesiuni", "cat am", "cati", "cate ");

        if (!wantsReport && period == null) {
            return general(userText);
        }

        if (!technician && !trainer) {
            return new Reply("Contul tău nu are rol de tehnician sau de trainer.", false);
        }

        if (period == null) {
            return new Reply(
                    "Spune-mi perioada, de exemplu: «luna trecută», «ultimele 30 de zile» "
                            + "sau «01.11.2026 - 25.11.2026».",
                    false
            );
        }

        StringBuilder text = new StringBuilder("Perioada: ").append(period.label()).append('\n');

        if (technician) {
            ReportService.TechnicianReport r = reports.technician(employeeId, period.from(), period.to());

            text.append("\nActivitate tehnician\n")
                    .append("- Programări finalizate: ").append(r.finished()).append('\n')
                    .append("- Clienți distincți: ").append(r.clients()).append('\n')
                    .append("- Venit din programările finalizate: ").append(money(r.revenue())).append('\n')
                    .append("- Confirmate, încă neînchise: ").append(r.confirmed()).append('\n')
                    .append("- Anulate: ").append(r.cancelled())
                    .append(", neprezentări: ").append(r.noShow()).append('\n');
        }

        if (trainer) {
            ReportService.TrainerReport r = reports.trainer(employeeId, period.from(), period.to());

            text.append("\nActivitate trainer\n")
                    .append("- Sesiuni în perioadă: ").append(r.sessions()).append('\n')
                    .append("- Cursanți distincți înscriși: ").append(r.students()).append('\n')
                    .append("- Valoarea înscrierilor (preț aplicat, fără retrageri): ")
                    .append(money(r.value())).append('\n');
        }

        text.append("\nSumele sunt prețurile aplicate în aplicație; aplicația nu înregistrează încasări.");

        return new Reply(text.toString(), false);
    }

    private Reply suggestCourses(String userText) throws Exception {
        String sessions = advisor.describeUpcomingSessions();

        if (sessions.isBlank()) {
            return new Reply("Momentan nu există sesiuni planificate. Revino mai târziu.", false);
        }

        if (!AiConfig.enabled()) {
            return new Reply("Sesiunile planificate:\n" + sessions
                    + "\nPentru recomandări personalizate este nevoie de o cheie API pentru asistent.", false);
        }

        try {
            String answer = ai.ask(
                    SYSTEM_COURSES,
                    "Sesiuni disponibile:\n" + sessions + "\nÎntrebarea clientului: " + userText
            );

            return new Reply(answer, false);

        } catch (IOException exception) {
            exception.printStackTrace();

            return new Reply("Serviciul AI nu a putut fi contactat acum. Iată sesiunile planificate:\n"
                    + sessions, false);
        }
    }

    private Reply general(String userText) throws Exception {
        if (!AiConfig.enabled()) {
            return new Reply(client
                    ? "Pot recomanda cursuri și trainer sau te pot duce la programări. "
                    + "Pentru alte întrebări este nevoie de o cheie API."
                    : "Pot genera rapoarte pe o perioadă, de exemplu «raport pentru luna trecută».", false);
        }

        try {
            return new Reply(ai.ask(SYSTEM_GENERAL, userText), false);

        } catch (IOException exception) {
            exception.printStackTrace();
            return new Reply("Serviciul AI nu a putut fi contactat acum. Încearcă din nou.", false);
        }
    }

    private static boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private static String money(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return safe.stripTrailingZeros().toPlainString() + " lei";
    }
}

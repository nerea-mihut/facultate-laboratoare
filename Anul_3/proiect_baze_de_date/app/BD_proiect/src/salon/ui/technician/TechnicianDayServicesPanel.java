package salon.ui.technician;

import salon.service.TechnicianDayService.DayConfiguration;
import salon.service.TechnicianDayService.ServiceOffer;
import salon.ui.components.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TechnicianDayServicesPanel
        extends JPanel
        implements Scrollable {

    private final Map<ServiceOffer, JToggleButton> choices =
            new LinkedHashMap<>();

    private final List<ServiceOffer> legacy =
            new ArrayList<>();

    private final Runnable onChange;

    private int row;

    public TechnicianDayServicesPanel(
            DayConfiguration config,
            Runnable onChange
    ) {
        super(new GridBagLayout());

        this.onChange = onChange;

        setOpaque(false);

        Map<String, List<ServiceOffer>> groups =
                new LinkedHashMap<>();

        for (String name : List.of(
                "Ojă semi",
                "Întreținere",
                "Slim",
                "Construcție",
                "Design",
                "Extraopțiuni pentru întreținere",
                "Alte servicii"
        )) {
            groups.put(name, new ArrayList<>());
        }

        for (ServiceOffer offer : config.availableOffers()) {
            if (isLegacy(offer)) {
                legacy.add(offer);
            } else {
                groups.get(group(offer)).add(offer);
            }
        }

        for (
                Map.Entry<String, List<ServiceOffer>> entry
                : groups.entrySet()
        ) {
            List<ServiceOffer> items = entry.getValue();

            if (items.isEmpty()) {
                continue;
            }

            items.sort(
                    Comparator.comparingInt(
                            ServiceOffer::serviceId
                    )
            );

            addRow(
                    UiStyle.label(
                            entry.getKey(),
                            16,
                            true,
                            UiStyle.INK
                    ),
                    0,
                    8
            );

            int columns = items.size() == 2 ? 2 : 3;

            JPanel buttons = new JPanel(
                    new GridLayout(
                            0,
                            columns,
                            10,
                            10
                    )
            );

            buttons.setOpaque(false);

            for (ServiceOffer offer : items) {
                String offerFamily = family(offer);

                boolean hasExplicitVariant =
                        config.availableOffers()
                                .stream()
                                .anyMatch(candidate ->
                                        !isLegacy(candidate)
                                                && !offerFamily.isEmpty()
                                                && family(candidate)
                                                .equals(offerFamily)
                                                && config.selectedServiceIds()
                                                .contains(
                                                        candidate.serviceId()
                                                )
                                );

                boolean inherited =
                        !hasExplicitVariant
                                && !offerFamily.isEmpty()
                                && legacy.stream()
                                .anyMatch(candidate ->
                                        family(candidate)
                                                .equals(offerFamily)
                                                && config.selectedServiceIds()
                                                .contains(
                                                        candidate.serviceId()
                                                )
                                );

                JToggleButton button =
                        new ServiceButton(offer);

                /*
                 * Zi nouă: nimic selectat.
                 * Zi configurată: păstrăm selecțiile salvate.
                 */
                boolean selectedForDay =
                        config.explicitlyConfigured()
                                && (
                                config.selectedServiceIds()
                                        .contains(offer.serviceId())
                                        || inherited
                        );

                button.setSelected(selectedForDay);

                button.addActionListener(event -> {
                    if (this.onChange != null) {
                        this.onChange.run();
                    }
                });

                choices.put(offer, button);
                buttons.add(button);
            }

            int height =
                    ((items.size() + columns - 1) / columns)
                            * 80;

            buttons.setPreferredSize(
                    new Dimension(620, height)
            );

            buttons.setMinimumSize(
                    new Dimension(200, height)
            );

            addRow(buttons, 0, 6);

            if ("Slim".equals(entry.getKey())) {
                addRow(
                        UiStyle.label(
                                "<html>"
                                        + "Potrivit pentru persoanele "
                                        + "cu unghii care cresc în jos."
                                        + "</html>",
                                12,
                                false,
                                UiStyle.MUTED
                        ),
                        0,
                        4
                );
            }

            if ("Ojă semi".equals(entry.getKey())) {
                addRow(
                        UiStyle.label(
                                "<html>"
                                        + "Hybrid: Întărirea vârfului "
                                        + "cu polygel pentru unghii mai moi "
                                        + "sau corecții de arhitectură."
                                        + "</html>",
                                12,
                                false,
                                UiStyle.MUTED
                        ),
                        0,
                        4
                );
            }

            addRow(
                    Box.createVerticalStrut(12),
                    0,
                    0
            );
        }

        if (choices.isEmpty()) {
            addRow(
                    UiStyle.label(
                            "<html>"
                                    + "Nu au fost primite variante active "
                                    + "pentru acest tehnician."
                                    + "<br>"
                                    + "Trebuie verificată oferta "
                                    + "contului autentificat."
                                    + "</html>",
                            13,
                            false,
                            UiStyle.RED
                    ),
                    0,
                    12
            );
        } else {
            addRow(
                    UiStyle.label(
                            choices.size()
                                    + " variante încărcate"
                                    + " · apasă pentru a selecta / deselecta",
                            12,
                            false,
                            UiStyle.MUTED
                    ),
                    4,
                    12
            );
        }

        GridBagConstraints filler =
                new GridBagConstraints();

        filler.gridx = 0;
        filler.gridy = row;
        filler.weightx = 1;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;

        JPanel empty = new JPanel();
        empty.setOpaque(false);

        add(empty, filler);
    }

    private void addRow(
            Component component,
            int top,
            int bottom
    ) {
        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = row++;
        constraints.weightx = 1;
        constraints.fill =
                GridBagConstraints.HORIZONTAL;
        constraints.anchor =
                GridBagConstraints.NORTHWEST;

        constraints.insets =
                new Insets(top, 4, bottom, 4);

        add(component, constraints);
    }

    private List<ServiceOffer> selected() {
        return choices.entrySet()
                .stream()
                .filter(entry ->
                        entry.getValue().isSelected()
                )
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<ServiceOffer> selectedBase() {
        return selected()
                .stream()
                .filter(offer ->
                        !isDesign(offer)
                                && !isExtra(offer)
                )
                .toList();
    }

    public List<ServiceOffer> selectedDesign() {
        return selected()
                .stream()
                .filter(
                        TechnicianDayServicesPanel::isDesign
                )
                .toList();
    }

    public List<ServiceOffer> selectedExtra() {
        return selected()
                .stream()
                .filter(
                        TechnicianDayServicesPanel::isExtra
                )
                .toList();
    }

    public Set<Integer> selectedIds() {
        Set<Integer> ids = new HashSet<>();

        List<ServiceOffer> selectedOffers =
                selected();

        for (ServiceOffer offer : selectedOffers) {
            ids.add(offer.serviceId());
        }

        /*
         * Compatibilitate cu rezervările vechi,
         * care foloseau serviciile generice.
         */
        for (ServiceOffer old : legacy) {
            boolean sameFamilySelected =
                    selectedOffers.stream()
                            .anyMatch(offer ->
                                    family(offer)
                                            .equals(family(old))
                            );

            if (sameFamilySelected) {
                ids.add(old.serviceId());
            }
        }

        return ids;
    }

    public static boolean isMaintenance(
            ServiceOffer offer
    ) {
        return norm(offer.category())
                .equals("intretinere");
    }

    public static boolean isDesign(
            ServiceOffer offer
    ) {
        return norm(offer.category())
                .equals("design");
    }

    public static boolean isExtra(
            ServiceOffer offer
    ) {
        return norm(offer.category())
                .equals("supliment");
    }

    private static boolean isLegacy(
            ServiceOffer offer
    ) {
        String name = norm(offer.name());

        return name.equals("slim")
                || name.equals("oja semipermanenta");
    }

    private static String family(
            ServiceOffer offer
    ) {
        String name = norm(offer.name());

        if (name.startsWith("slim")) {
            return "slim";
        }

        if (name.startsWith("oja semi")) {
            return "semi";
        }

        return "";
    }

    private static String group(
            ServiceOffer offer
    ) {
        if (isDesign(offer)) {
            return "Design";
        }

        if (isExtra(offer)) {
            return "Extraopțiuni pentru întreținere";
        }

        if (family(offer).equals("semi")) {
            return "Ojă semi";
        }

        if (family(offer).equals("slim")) {
            return "Slim";
        }

        if (isMaintenance(offer)) {
            return "Întreținere";
        }

        if (norm(offer.category())
                .equals("constructie")) {
            return "Construcție";
        }

        return "Alte servicii";
    }

    private static String display(
            ServiceOffer offer
    ) {
        String name = norm(offer.name());

        if (name.contains("hybrid")) {
            return "Hybrid";
        }

        if (name.contains("simpla")) {
            return "Simplă";
        }

        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile("marimea\\s+(\\d+)")
                        .matcher(name);

        if (matcher.find()) {
            return "Mărimea " + matcher.group(1);
        }

        if (isDesign(offer)) {
            if (name.contains("simplu")) {
                return "Easy";
            }

            if (name.contains("mediu")) {
                return "Mediu";
            }

            if (name.contains("complex")) {
                return "Complex";
            }
        }

        if (isExtra(offer)) {
            if (name.contains("corect")) {
                return "Corectare arhitectură";
            }

            if (name.contains("schimbare")) {
                return "Schimbare arhitectură";
            }
        }

        return offer.name();
    }

    private static String norm(String text) {
        return Normalizer.normalize(
                        text == null ? "" : text,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT)
                .trim();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(650, 400);
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visibleRect,
            int orientation,
            int direction
    ) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visibleRect,
            int orientation,
            int direction
    ) {
        return Math.max(
                20,
                visibleRect.height - 30
        );
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private static class ServiceButton
            extends JToggleButton {

        private final ServiceOffer offer;

        ServiceButton(ServiceOffer offer) {
            super(display(offer));

            this.offer = offer;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setPreferredSize(
                    new Dimension(180, 74)
            );

            setMinimumSize(
                    new Dimension(50, 74)
            );

            setToolTipText(
                    offer.name()
                            + " · "
                            + offer.price()
                            .stripTrailingZeros()
                            .toPlainString()
                            + " lei · "
                            + offer.durationMinutes()
                            + " min"
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g.setColor(
                    isSelected()
                            ? new Color(220, 208, 188)
                            : UiStyle.BEIGE
            );

            g.fillRoundRect(
                    2,
                    2,
                    getWidth() - 5,
                    getHeight() - 5,
                    20,
                    20
            );

            g.setColor(
                    isSelected()
                            ? UiStyle.GREEN
                            : UiStyle.BORDER
            );

            g.setStroke(
                    new BasicStroke(
                            isSelected() ? 2f : 1f
                    )
            );

            g.drawRoundRect(
                    2,
                    2,
                    getWidth() - 5,
                    getHeight() - 5,
                    20,
                    20
            );

            draw(
                    g,
                    display(offer),
                    getHeight() / 2 - 9,
                    14,
                    true
            );

            draw(
                    g,
                    offer.price()
                            .stripTrailingZeros()
                            .toPlainString()
                            + " lei · "
                            + offer.durationMinutes()
                            + " min",
                    getHeight() / 2 + 13,
                    12,
                    false
            );

            if (hasFocus()) {
                g.setColor(UiStyle.MUTED);

                g.drawRoundRect(
                        6,
                        6,
                        getWidth() - 13,
                        getHeight() - 13,
                        16,
                        16
                );
            }

            g.dispose();
        }

        private void draw(
                Graphics2D g,
                String text,
                int y,
                int size,
                boolean bold
        ) {
            g.setFont(
                    new Font(
                            "SansSerif",
                            bold ? Font.BOLD : Font.PLAIN,
                            size
                    )
            );

            g.setColor(
                    isEnabled()
                            ? UiStyle.INK
                            : UiStyle.MUTED
            );

            FontMetrics metrics =
                    g.getFontMetrics();

            int availableWidth =
                    Math.max(10, getWidth() - 20);

            String fitted = text;

            while (
                    fitted.length() > 1
                            && metrics.stringWidth(fitted)
                            > availableWidth
            ) {
                fitted = fitted.substring(
                        0,
                        fitted.length() - 1
                );
            }

            if (
                    !fitted.equals(text)
                            && fitted.length() > 1
            ) {
                fitted = fitted.substring(
                        0,
                        fitted.length() - 1
                ) + "…";
            }

            g.drawString(
                    fitted,
                    (
                            getWidth()
                                    - metrics.stringWidth(fitted)
                    ) / 2,
                    y
            );
        }
    }
}
package salon.ui.registry;

import salon.service.SterilizationService;
import salon.ui.components.DatePickerDialog;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SterilizationRegistryDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final SterilizationService sterilizationService;
    private final int employeeId;

    public SterilizationRegistryDialog(
            Frame owner,
            SterilizationService sterilizationService,
            int employeeId
    ) {
        super(
                owner,
                "Adaugă înregistrare în registru",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        this.sterilizationService = sterilizationService;
        this.employeeId = employeeId;

        setLayout(new BorderLayout());
        setSize(620, 780);
        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiStyle.WHITE);
        panel.setBorder(
                BorderFactory.createEmptyBorder(28, 34, 28, 34)
        );

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel title = UiStyle.label(
                "Nouă înregistrare", 24, true, UiStyle.INK
        );
        addRow(panel, title, c, 0, 0, 24);

        final LocalDate[] selectedDate = {
                LocalDate.now()
        };

        JButton dateButton = UiStyle.secondaryButton(
                "Alege data · "
                        + selectedDate[0].format(DATE_FORMAT)
        );
        dateButton.setPreferredSize(new Dimension(340, 42));

        dateButton.addActionListener(e -> {
            DatePickerDialog picker = new DatePickerDialog(
                    owner,
                    "Alege data sterilizării",
                    selectedDate[0]
            );

            LocalDate chosen = picker.showDialog();

            if (chosen != null) {
                selectedDate[0] = chosen;

                dateButton.setText(
                        "Alege data · "
                                + chosen.format(DATE_FORMAT)
                );
            }
        });

        JComboBox<String> deviceField = new JComboBox<>(
                new String[]{"Pupinel", "Autoclav"}
        );

        JTextField temperatureField = new JTextField("180");
        JTextField startTimeField = new JTextField();
        JTextField endTimeField = new JTextField();

        UiStyle.styleField(temperatureField);
        UiStyle.styleField(startTimeField);
        UiStyle.styleField(endTimeField);

        deviceField.addActionListener(e -> {
            String device =
                    (String) deviceField.getSelectedItem();

            if ("Pupinel".equals(device)) {
                temperatureField.setText("180");
            } else if ("Autoclav".equals(device)) {
                temperatureField.setText("134");
            }
        });

        JPanel materialsPanel =
                new JPanel(new GridLayout(0, 2, 10, 8));
        materialsPanel.setOpaque(false);

        JCheckBox bitCarbid = new JCheckBox("Bit carbid");
        JCheckBox bitDiamant = new JCheckBox("Bit diamant");
        JCheckBox pusher = new JCheckBox("Pusher");
        JCheckBox scissors = new JCheckBox("Forfecuță");
        JCheckBox pliers = new JCheckBox("Clește");
        JCheckBox metalFile = new JCheckBox("Pilă metalică");

        JCheckBox[] materialChecks = new JCheckBox[]{
                bitCarbid,
                bitDiamant,
                pusher,
                scissors,
                pliers,
                metalFile
        };

        for (JCheckBox checkBox : materialChecks) {
            checkBox.setOpaque(false);
            checkBox.setForeground(UiStyle.INK);
            materialsPanel.add(checkBox);
        }

        JButton chooseImageButton =
                UiStyle.secondaryButton("Importă poza martorului");
        chooseImageButton.setPreferredSize(new Dimension(280, 42));

        JLabel selectedImageLabel = UiStyle.label(
                "Nicio poză selectată.", 12, false, UiStyle.MUTED
        );

        final File[] selectedImage = new File[1];

        chooseImageButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            chooser.setDialogTitle("Selectează poza martorului");
            chooser.setFileSelectionMode(
                    JFileChooser.FILES_ONLY
            );

            chooser.setFileFilter(
                    new FileNameExtensionFilter(
                            "Imagini (JPG, PNG)",
                            "jpg", "jpeg", "png"
                    )
            );

            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImage[0] = chooser.getSelectedFile();

                selectedImageLabel.setText(
                        "Poză selectată: "
                                + selectedImage[0].getName()
                );
            }
        });

        addDialogField(panel, c, 1, "Data sterilizării", dateButton);
        addDialogField(panel, c, 3, "Aparat", deviceField);

        JLabel materialsLabel = UiStyle.label(
                "Materiale sterilizate", 13, false, UiStyle.INK
        );
        addRow(panel, materialsLabel, c, 5, 0, 6);
        addRow(panel, materialsPanel, c, 6, 0, 18);

        addDialogField(panel, c, 7, "Temperatura (°C)", temperatureField);
        addDialogField(panel, c, 9, "Ora începerii", startTimeField);
        addDialogField(panel, c, 11, "Ora terminării", endTimeField);

        JLabel imageLabel = UiStyle.label(
                "Poza martorului", 13, false, UiStyle.INK
        );
        addRow(panel, imageLabel, c, 13, 0, 6);
        addRow(panel, chooseImageButton, c, 14, 0, 6);
        addRow(panel, selectedImageLabel, c, 15, 0, 20);

        JButton saveButton = UiStyle.primaryButton(
                "Salvează înregistrarea"
        );
        saveButton.setPreferredSize(new Dimension(340, 46));

        JLabel dialogStatus = UiStyle.label(
                " ", 12, false, UiStyle.MUTED
        );

        addRow(panel, saveButton, c, 16, 10, 10);
        addRow(panel, dialogStatus, c, 17, 0, 0);

        saveButton.addActionListener(e -> {
            try {
                LocalDate date = selectedDate[0];

                String device =
                        (String) deviceField.getSelectedItem();

                StringBuilder materials = new StringBuilder();
                boolean first = true;

                for (JCheckBox checkBox : materialChecks) {
                    if (checkBox.isSelected()) {
                        if (!first) {
                            materials.append(", ");
                        }

                        materials.append(checkBox.getText());
                        first = false;
                    }
                }

                String material = materials.toString();

                if (material.isBlank()) {
                    dialogStatus.setForeground(UiStyle.RED);
                    dialogStatus.setText(
                            "Selectează cel puțin un material sterilizat."
                    );
                    return;
                }

                String temperatureText =
                        temperatureField.getText().trim();
                String startText =
                        startTimeField.getText().trim();
                String endText =
                        endTimeField.getText().trim();

                if (temperatureText.isEmpty()
                        || startText.isEmpty()
                        || endText.isEmpty()) {

                    dialogStatus.setForeground(UiStyle.RED);
                    dialogStatus.setText(
                            "Completează temperatura și orele."
                    );
                    return;
                }

                BigDecimal temperature =
                        new BigDecimal(temperatureText);

                LocalTime startTime =
                        LocalTime.parse(startText);

                LocalTime endTime =
                        LocalTime.parse(endText);

                if (!endTime.isAfter(startTime)) {
                    dialogStatus.setForeground(UiStyle.RED);
                    dialogStatus.setText(
                            "Ora de sfârșit trebuie să fie "
                                    + "ulterioară orei de început."
                    );
                    return;
                }

                String imageReference = null;

                if (selectedImage[0] != null) {
                    imageReference = sterilizationService.saveImage(
                            selectedImage[0]
                    );
                }

                sterilizationService.saveRecord(
                        employeeId,
                        date,
                        device,
                        material,
                        temperature,
                        startTime,
                        endTime,
                        imageReference
                );

                dispose();

            } catch (Exception exception) {
                exception.printStackTrace();

                dialogStatus.setForeground(UiStyle.RED);
                dialogStatus.setText(
                        "Verifică datele introduse."
                );
            }
        });

        add(new JScrollPane(panel), BorderLayout.CENTER);
    }

    private void addDialogField(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String label,
            JComponent field
    ) {
        JLabel labelText = UiStyle.label(
                label, 13, false, UiStyle.INK
        );
        labelText.setLabelFor(field);
        addRow(panel, labelText, c, row, 0, 6);

        addRow(panel, field, c, row + 1, 0, 18);
    }

    private void addRow(
            JPanel panel,
            Component component,
            GridBagConstraints constraints,
            int row,
            int top,
            int bottom
    ) {
        constraints.gridy = row;
        constraints.insets = new Insets(top, 0, bottom, 0);
        panel.add(component, constraints);
    }
}
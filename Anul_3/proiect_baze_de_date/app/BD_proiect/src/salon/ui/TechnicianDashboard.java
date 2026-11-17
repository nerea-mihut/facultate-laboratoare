package salon.ui;

import salon.ui.components.UiStyle;
import salon.service.AppointmentService;
import salon.service.AvailabilityService;
import salon.service.SterilizationService;
import salon.service.TechnicianService;
import salon.ui.registry.SterilizationRegistryPanel;
import salon.ui.technician.TechnicianAppointmentsPanel;
import salon.ui.technician.TechnicianAvailabilityPanel;
import salon.ui.technician.TechnicianHomePanel;
import salon.ui.technician.TechnicianProfilePanel;

import javax.swing.*;
import java.awt.*;

public class TechnicianDashboard extends JFrame {

    private final String userEmail;

    private final TechnicianService technicianService;
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final SterilizationService sterilizationService;

    private final JPanel contentPanel = new JPanel(new CardLayout());

    public TechnicianDashboard(String userEmail) {
        this.userEmail = userEmail;

        this.technicianService = new TechnicianService(userEmail);
        this.availabilityService = new AvailabilityService();
        this.appointmentService = new AppointmentService();
        this.sterilizationService = new SterilizationService();

        setTitle("Salon & Academie — Panou tehnician");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 780));
        setSize(1280, 840);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UiStyle.CREAM);
        setContentPane(mainPanel);

        mainPanel.add(createTopBar(), BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(
                new TechnicianHomePanel(
                        technicianService,
                        this::showAppointments,
                        this::showAvailability,
                        this::showRegistry
                ),
                "HOME"
        );

        contentPanel.add(
                new TechnicianProfilePanel(technicianService),
                "PROFILE"
        );

        contentPanel.add(
                new TechnicianAppointmentsPanel(
                        appointmentService,
                        technicianService
                ),
                "APPOINTMENTS"
        );

        contentPanel.add(
                new TechnicianAvailabilityPanel(
                        availabilityService,
                        technicianService
                ),
                "AVAILABILITY"
        );

        contentPanel.add(
                new SterilizationRegistryPanel(
                        sterilizationService,
                        technicianService.getTechnicianId()
                ),
                "REGISTRY"
        );

        showHome();
        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UiStyle.INK);
        topBar.setBorder(
                BorderFactory.createEmptyBorder(14, 28, 14, 28)
        );

        JLabel brand = new JLabel("SALON & ACADEMIE");
        brand.setFont(new Font("SansSerif", Font.BOLD, 15));
        brand.setForeground(Color.WHITE);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        actions.setOpaque(false);

        JButton homeButton = createTopBarButton("Acasă");
        homeButton.addActionListener(e -> showHome());

        JButton profileButton = createTopBarButton("Profilul meu");
        profileButton.addActionListener(e -> showProfile());

        JButton logoutButton = createTopBarButton("Deconectare");
        logoutButton.addActionListener(e -> logout());

        actions.add(homeButton);
        actions.add(profileButton);
        actions.add(logoutButton);

        topBar.add(brand, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);

        return topBar;
    }

    private JButton createTopBarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(UiStyle.INK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    private void showHome() {
        ((CardLayout) contentPanel.getLayout())
                .show(contentPanel, "HOME");
    }

    private void showProfile() {
        ((CardLayout) contentPanel.getLayout())
                .show(contentPanel, "PROFILE");
    }

    private void showAppointments() {
        ((CardLayout) contentPanel.getLayout())
                .show(contentPanel, "APPOINTMENTS");
    }

    private void showAvailability() {
        ((CardLayout) contentPanel.getLayout())
                .show(contentPanel, "AVAILABILITY");
    }

    private void showRegistry() {
        ((CardLayout) contentPanel.getLayout())
                .show(contentPanel, "REGISTRY");
    }

    private void logout() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);

        dispose();
    }
}
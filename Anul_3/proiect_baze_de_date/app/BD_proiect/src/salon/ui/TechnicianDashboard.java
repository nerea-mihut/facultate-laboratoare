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
        technicianService = new TechnicianService(userEmail);
        availabilityService = new AvailabilityService();
        appointmentService = new AppointmentService();
        sterilizationService = new SterilizationService();
        setTitle("Salon & Academie — Panou tehnician");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 780));
        setSize(1280, 840);
        setLocationRelativeTo(null);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UiStyle.CREAM);
        setContentPane(main);
        main.add(createTopBar(), BorderLayout.NORTH);
        main.add(contentPanel, BorderLayout.CENTER);
        contentPanel.add(new TechnicianHomePanel(technicianService,
                this::showAppointments, this::showAvailability, this::showRegistry), "HOME");
        contentPanel.add(new TechnicianProfilePanel(technicianService), "PROFILE");
        contentPanel.add(new TechnicianAppointmentsPanel(appointmentService,
                technicianService, this::showHome), "APPOINTMENTS");
        contentPanel.add(new TechnicianAvailabilityPanel(availabilityService,
                technicianService, this::showHome), "AVAILABILITY");
        contentPanel.add(new SterilizationRegistryPanel(sterilizationService,
                technicianService.getTechnicianId()), "REGISTRY");
        showHome();
        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiStyle.INK);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        JLabel brand = new JLabel("SALON & ACADEMIE");
        brand.setFont(new Font("SansSerif", Font.BOLD, 15));
        brand.setForeground(Color.WHITE);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton profile = createTopBarButton("Profilul meu");
        profile.addActionListener(e -> showProfile());
        JButton logout = createTopBarButton("Deconectare");
        logout.addActionListener(e -> logout());
        actions.add(profile);
        actions.add(logout);
        bar.add(brand, BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JButton createTopBarButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(UiStyle.INK);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void showHome() { showCard("HOME"); }
    private void showProfile() { showCard("PROFILE"); }
    private void showAppointments() { showCard("APPOINTMENTS"); }
    private void showAvailability() { showCard("AVAILABILITY"); }
    private void showRegistry() { showCard("REGISTRY"); }
    private void showCard(String name) {
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
    }
    private void logout() {
        LoginFrame login = new LoginFrame();
        login.setVisible(true);
        dispose();
    }
}


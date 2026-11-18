package salon.ui.trainer;

import javax.swing.*;
import java.awt.*;

public class TrainerDashboard extends JFrame {

    private static final Color CREAM = new Color(244, 240, 233);
    private static final Color INK = new Color(30, 30, 30);
    private static final Color WHITE = Color.WHITE;

    private final String userEmail;

    private final JPanel contentPanel = new JPanel(new CardLayout());

    private TrainerHomePanel homePanel;
    private TrainerCoursesPanel coursesPanel;
    private TrainerSessionsPanel sessionsPanel;
    private TrainerAvailabilityPanel availabilityPanel;
    private TrainerProfilePanel profilePanel;

    public TrainerDashboard(String userEmail) {
        this.userEmail = userEmail;

        setTitle("Salon Academie — Panou trainer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setSize(1240, 820);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CREAM);
        setContentPane(mainPanel);

        mainPanel.add(createTopBar(), BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        homePanel = new TrainerHomePanel(
                userEmail,
                this::showCourses,
                this::showSessions,
                this::showAvailability,
                this::showProfile
        );

        coursesPanel = new TrainerCoursesPanel(userEmail, this::showHome);
        sessionsPanel = new TrainerSessionsPanel(userEmail, this::showHome);
        availabilityPanel = new TrainerAvailabilityPanel(
                userEmail,
                this::showHome
        );
        profilePanel = new TrainerProfilePanel(userEmail, this::showHome);

        contentPanel.add(homePanel, "HOME");
        contentPanel.add(coursesPanel, "COURSES");
        contentPanel.add(sessionsPanel, "SESSIONS");
        contentPanel.add(availabilityPanel, "AVAILABILITY");
        contentPanel.add(profilePanel, "PROFILE");

        showHome();
        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(INK);
        topBar.setBorder(
                BorderFactory.createEmptyBorder(14, 28, 14, 28)
        );

        JLabel brand = new JLabel("SALON ACADEMIE");
        brand.setFont(new Font("SansSerif", Font.BOLD, 15));
        brand.setForeground(WHITE);
        topBar.add(brand, BorderLayout.WEST);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        actions.setOpaque(false);

        actions.add(createTopBarButton(
                "Acasă",
                this::showHome
        ));

        actions.add(createTopBarButton(
                "Cursurile mele",
                this::showCourses
        ));

        actions.add(createTopBarButton(
                "Sesiuni",
                this::showSessions
        ));

        actions.add(createTopBarButton(
                "Disponibilitate",
                this::showAvailability
        ));

        actions.add(createTopBarButton(
                "Profilul meu",
                this::showProfile
        ));

        topBar.add(actions, BorderLayout.EAST);
        return topBar;
    }

    private JButton createTopBarButton(
            String text,
            Runnable action
    ) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setForeground(WHITE);
        button.setBackground(INK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.addActionListener(e -> action.run());
        return button;
    }

    public void showHome() {
        show("HOME");
    }

    public void showCourses() {
        show("COURSES");
    }

    public void showSessions() {
        show("SESSIONS");
    }

    public void showAvailability() {
        show("AVAILABILITY");
    }

    public void showProfile() {
        show("PROFILE");
    }

    private void show(String cardName) {
        CardLayout layout =
                (CardLayout) contentPanel.getLayout();
        layout.show(contentPanel, cardName);
    }
}
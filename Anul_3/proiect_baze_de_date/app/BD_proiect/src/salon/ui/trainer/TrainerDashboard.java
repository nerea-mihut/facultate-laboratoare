package salon.ui.trainer;

import javax.swing.*;
import java.awt.*;

public class TrainerDashboard extends JFrame {
    private static final Color INK = new Color(30,30,30);
    private final JPanel contentPanel = new JPanel(new CardLayout());
    private final TrainerCoursesPanel coursesPanel;
    private final TrainerParticipantsPanel participantsPanel;

    public TrainerDashboard(String userEmail) {
        setTitle("Salon Academie — Panou trainer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100,760));
        setSize(1240,820);
        setLocationRelativeTo(null);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(244,240,233));
        setContentPane(main);
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(INK);
        bar.setBorder(BorderFactory.createEmptyBorder(14,28,14,28));
        JLabel brand = new JLabel("SALON ACADEMIE");
        brand.setFont(new Font("SansSerif",Font.BOLD,15));
        brand.setForeground(Color.WHITE);
        bar.add(brand,BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        actions.setOpaque(false);
        actions.add(menu("Acasă",this::showHome));
        actions.add(menu("Cursurile mele",this::showCourses));
        actions.add(menu("Cursanți și documente",this::showParticipants));
        actions.add(menu("Profilul meu",this::showProfile));
        bar.add(actions,BorderLayout.EAST);
        main.add(bar,BorderLayout.NORTH);
        main.add(contentPanel,BorderLayout.CENTER);
        coursesPanel = new TrainerCoursesPanel(userEmail,this::showHome);
        participantsPanel = new TrainerParticipantsPanel(userEmail,this::showHome);
        contentPanel.add(new TrainerHomePanel(userEmail,this::showCourses,this::showParticipants,this::showEnrollment),"HOME");
        contentPanel.add(coursesPanel,"COURSES");
        contentPanel.add(participantsPanel,"PARTICIPANTS");
        contentPanel.add(new TrainerProfilePanel(userEmail,this::showHome),"PROFILE");
        showHome();
        setVisible(true);
    }
    private JButton menu(String text,Runnable action) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif",Font.PLAIN,13));
        b.setForeground(Color.WHITE);
        b.setBackground(INK);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }
    public void showHome() { showCard("HOME"); }
    public void showCourses() { coursesPanel.refreshData(); showCard("COURSES"); }
    public void showParticipants() { participantsPanel.refreshData(); showCard("PARTICIPANTS"); }
    public void showEnrollment() {
        showParticipants();
        SwingUtilities.invokeLater(participantsPanel::openEnrollmentDialog);
    }
    public void showProfile() { showCard("PROFILE"); }
    public void showSessions() { showCourses(); }
    public void showAvailability() { showCourses(); }
    private void showCard(String name) {
        ((CardLayout)contentPanel.getLayout()).show(contentPanel,name);
    }
}
package salon.ui;

import salon.ai.AiAssistant;
import salon.service.*;
import salon.service.EmployeeAccessService.Access;
import salon.ui.components.UiStyle;
import salon.ui.registry.SterilizationRegistryPanel;
import salon.ui.technician.TechnicianAppointmentsPanel;
import salon.ui.technician.TechnicianProfilePanel;
import salon.ui.trainer.TrainerCoursesPanel;
import salon.ui.trainer.TrainerParticipantsPanel;
import salon.ui.trainer.TrainerProfilePanel;
import javax.swing.*;
import java.awt.*;

public class EmployeeDashboard extends JFrame {

    private final Access access;
    private final JPanel content=new JPanel(new CardLayout());
    private TrainerCoursesPanel courses;
    private TrainerParticipantsPanel participants;
    private EmployeeCalendarPanel calendar;
    private SterilizationRegistryPanel registry;
    public EmployeeDashboard(Access access) {
        this.access=access;
        setTitle("Salon & Academie — "+(access.technician()&&access.trainer()?"Tehnician și trainer":access.technician()?"Tehnician":"Trainer"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);setMinimumSize(new Dimension(1180,780));setSize(1320,860);setLocationRelativeTo(null);
        JPanel main=new JPanel(new BorderLayout());main.setBackground(UiStyle.CREAM);setContentPane(main);
        main.add(topBar(),BorderLayout.NORTH);main.add(content,BorderLayout.CENTER);
        TechnicianService technician=access.technician()?new TechnicianService(access.email()):null;
        if(access.technician()) {
            if(technician.getTechnicianId()==null)throw new IllegalStateException("Nu s-au încărcat datele tehnicianului.");
            content.add(new TechnicianAppointmentsPanel(new AppointmentService(),technician,this::showHome),"APPOINTMENTS");
        }
        if(access.trainer()) {
            courses=new TrainerCoursesPanel(access.email(),this::showHome);
            participants=new TrainerParticipantsPanel(access.email(),this::showHome);
            content.add(courses,"COURSES");content.add(participants,"PARTICIPANTS");
        }
        if(access.technician())content.add(new TechnicianProfilePanel(technician),"PROFILE");
        else content.add(new TrainerProfilePanel(access.email(),this::showHome),"PROFILE");
        registry=new SterilizationRegistryPanel(new SterilizationService(),access.employeeId(),this::showHome);
        content.add(registry,"REGISTRY");
        calendar=new EmployeeCalendarPanel(access,new AvailabilityService(),this::showHome,this::showCourses);
        content.add(calendar,"CALENDAR");content.add(home(),"HOME");
        AiAssistant.installForEmployee(this,access.employeeId(),access.technician(),access.trainer());
        showHome();setVisible(true);
    }
    private JPanel topBar(){
        JPanel bar=new JPanel(new BorderLayout());bar.setBackground(UiStyle.INK);bar.setBorder(BorderFactory.createEmptyBorder(14,28,14,28));
        JLabel brand=new JLabel("SALON & ACADEMIE");brand.setFont(new Font("SansSerif",Font.BOLD,15));brand.setForeground(Color.WHITE);bar.add(brand,BorderLayout.WEST);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));actions.setOpaque(false);
        actions.add(nav("Acasă",this::showHome));actions.add(nav("Profilul meu",()->show("PROFILE")));
        actions.add(nav("Deconectare",()->{new LoginFrame().setVisible(true);dispose();}));bar.add(actions,BorderLayout.EAST);return bar;
    }
    private JButton nav(String text,Runnable action){JButton b=new JButton(text);b.setFont(new Font("SansSerif",Font.PLAIN,13));b.setForeground(Color.WHITE);b.setBackground(UiStyle.INK);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.addActionListener(e->action.run());return b;}
    private JPanel home(){
        JPanel background=new JPanel(new GridBagLayout());background.setBackground(UiStyle.CREAM);background.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JPanel card=new JPanel(new GridBagLayout()){
            @Override protected void paintComponent(Graphics graphics){super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(Color.WHITE);g.fillRoundRect(0,0,getWidth(),getHeight(),30,30);g.dispose();}
        };
        card.setOpaque(false);card.setBorder(BorderFactory.createEmptyBorder(34,44,34,44));
        GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.insets=new Insets(0,0,13,0);
        JLabel title=UiStyle.label("Bine ai venit, "+access.name()+"!",26,true,UiStyle.INK);c.gridy=0;card.add(title,c);
        c.gridy++;card.add(UiStyle.label(access.technician()&&access.trainer()?"Tehnician și trainer — același cont, ambele activități.":"Gestionează activitățile tale.",13,false,UiStyle.MUTED),c);
        if(access.technician())addHome(card,c,"Programările mele",()->show("APPOINTMENTS"));
        if(access.trainer()){addHome(card,c,"Cursurile mele",this::showCourses);addHome(card,c,"Cursanți și documente",this::showParticipants);}
        addHome(card,c,"Calendar disponibilitate",()->{show("CALENDAR");calendar.refreshData();});
        addHome(card,c,"Registru de sterilizare",()->{registry.refreshData();show("REGISTRY");});
        background.add(card);return background;
    }
    private void addHome(JPanel card,GridBagConstraints c,String text,Runnable action){JButton b=UiStyle.primaryButton(text);b.setPreferredSize(new Dimension(530,48));b.addActionListener(e->action.run());c.gridy++;card.add(b,c);}
    private void show(String name){
        ((CardLayout)content.getLayout()).show(content,name);
        setAssistantVisible("HOME".equals(name)||"PROFILE".equals(name));
    }
    private void setAssistantVisible(boolean visible){
        for(Component component:getLayeredPane().getComponents()){
            if(component.getClass().getName().equals("salon.ai.AiAssistant$PillButton"))component.setVisible(visible);
        }
    }
    public void showHome(){show("HOME");}
    public void showCourses(){if(courses!=null){courses.refreshData();show("COURSES");}}
    public void showParticipants(){if(participants!=null){participants.refreshData();show("PARTICIPANTS");}}
}
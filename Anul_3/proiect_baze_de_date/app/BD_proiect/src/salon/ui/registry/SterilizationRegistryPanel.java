package salon.ui.registry;

import salon.service.SterilizationService;
import salon.ui.components.RoundedCard;
import salon.ui.components.UiStyle;
import salon.ui.model.RegistryRecord;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SterilizationRegistryPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMAT=DateTimeFormatter.ofPattern("dd.MM.yyyy"),TIME_FORMAT=DateTimeFormatter.ofPattern("HH:mm");
    private final SterilizationService sterilizationService;private final int employeeId;private final Runnable onBack;
    private final JPanel recordsPanel=new JPanel();
    public SterilizationRegistryPanel(SterilizationService service,int employeeId){this(service,employeeId,null);}
    public SterilizationRegistryPanel(SterilizationService service,int employeeId,Runnable onBack){
        this.sterilizationService=service;this.employeeId=employeeId;this.onBack=onBack;
        setLayout(new BorderLayout(0,16));setBackground(UiStyle.CREAM);setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        RoundedCard card=new RoundedCard();card.setLayout(new BorderLayout(0,18));card.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        JPanel heading=new JPanel(new BorderLayout(12,0));heading.setOpaque(false);JButton back=UiStyle.secondaryButton("< Înapoi");back.setPreferredSize(new Dimension(110,38));
        back.addActionListener(e->{if(this.onBack!=null)this.onBack.run();else{Container p=getParent();while(p!=null){if(p.getLayout() instanceof CardLayout layout){layout.show(p,"HOME");return;}p=p.getParent();}}});heading.add(back,BorderLayout.WEST);
        JPanel titles=new JPanel(new GridLayout(0,1,0,5));titles.setOpaque(false);titles.add(UiStyle.label("Registru de sterilizare",26,true,UiStyle.INK));titles.add(UiStyle.label("Înregistrările sunt asociate contului tău.",13,false,UiStyle.MUTED));
        JButton add=UiStyle.primaryButton("Adaugă înregistrare");add.setPreferredSize(new Dimension(220,42));add.addActionListener(e->openRegistryDialog());heading.add(titles,BorderLayout.CENTER);heading.add(add,BorderLayout.EAST);
        recordsPanel.setLayout(new BoxLayout(recordsPanel,BoxLayout.Y_AXIS));recordsPanel.setOpaque(false);JScrollPane scroll=new JScrollPane(recordsPanel);scroll.setBorder(null);scroll.getVerticalScrollBar().setUnitIncrement(18);scroll.getViewport().setBackground(UiStyle.WHITE);
        card.add(heading,BorderLayout.NORTH);card.add(scroll,BorderLayout.CENTER);add(card,BorderLayout.CENTER);refreshData();
    }
    public void refreshData(){
        recordsPanel.removeAll();List<RegistryRecord> records=sterilizationService.loadRecords(employeeId);
        for(RegistryRecord record:records){recordsPanel.add(createRecordCard(record));recordsPanel.add(Box.createVerticalStrut(12));}
        if(records.isEmpty())recordsPanel.add(UiStyle.label("Nu există înregistrări în registru.",13,false,UiStyle.MUTED));recordsPanel.revalidate();recordsPanel.repaint();
    }
    private JPanel createRecordCard(RegistryRecord r){
        JPanel card=new JPanel(new BorderLayout(14,0));card.setBackground(UiStyle.FIELD);card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiStyle.BORDER,1,true),BorderFactory.createEmptyBorder(16,18,16,18)));card.setMaximumSize(new Dimension(Integer.MAX_VALUE,160));card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        JLabel title=UiStyle.label(r.date().format(DATE_FORMAT)+" · "+r.device(),16,true,UiStyle.INK);
        JLabel details=UiStyle.label(r.material()+" · "+r.temperature()+"°C · "+r.startTime().format(TIME_FORMAT)+"–"+r.endTime().format(TIME_FORMAT),13,false,UiStyle.MUTED);
        JLabel test=UiStyle.label(r.testReference()==null||r.testReference().isBlank()?"Fără poză martor atașată.":"Martor: "+r.testReference(),12,false,UiStyle.MUTED);
        for(JLabel item:new JLabel[]{title,details,test}){item.setAlignmentX(Component.LEFT_ALIGNMENT);info.add(item);info.add(Box.createVerticalStrut(5));}card.add(info,BorderLayout.CENTER);return card;
    }
    private void openRegistryDialog(){SterilizationRegistryDialog d=new SterilizationRegistryDialog((Frame)SwingUtilities.getWindowAncestor(this),sterilizationService,employeeId);d.setVisible(true);refreshData();}
}
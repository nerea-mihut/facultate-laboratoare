package salon.ui.technician;

import salon.service.AvailabilityService;
import salon.service.TechnicianDayService;
import salon.service.TechnicianDayService.DayConfiguration;
import salon.service.TechnicianDayService.ServiceOffer;
import salon.service.TechnicianService;
import salon.ui.components.UiStyle;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TechnicianAvailabilityPanel extends JPanel {
    private final AvailabilityService availability;
    private final TechnicianDayService service=new TechnicianDayService();
    private final Integer technicianId;
    private final Runnable onBack;
    private final JPanel grid=new JPanel(new GridLayout(0,7,10,10));
    private final JLabel title=UiStyle.label("",22,true,UiStyle.INK);
    private final JLabel status=UiStyle.label(" ",12,false,UiStyle.MUTED);
    private final JButton previous=UiStyle.secondaryButton("‹");
    private final JButton next=UiStyle.secondaryButton("›");
    private final JButton refresh=UiStyle.secondaryButton("Reîmprospătează");
    private YearMonth month=YearMonth.now();
    private Map<LocalDate,Integer> states=new HashMap<>();
    private boolean busy;
    public TechnicianAvailabilityPanel(AvailabilityService availability,TechnicianService technician){
        this(availability,technician,null);
    }
    public TechnicianAvailabilityPanel(AvailabilityService availability,TechnicianService technician,Runnable onBack){
        this.availability=availability;this.technicianId=technician.getTechnicianId();this.onBack=onBack;
        setLayout(new BorderLayout());setBackground(UiStyle.CREAM);
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel card=new JPanel(new BorderLayout(0,24)){
            @Override protected void paintComponent(Graphics graphics){
                super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(UiStyle.WHITE);g.fillRoundRect(0,0,getWidth(),getHeight(),28,28);g.dispose();
            }
        };card.setOpaque(false);card.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        JPanel heading=transparent(new BorderLayout(16,0));
        JButton back=UiStyle.secondaryButton("< Înapoi");back.setPreferredSize(new Dimension(110,38));
        back.addActionListener(e->goHome());heading.add(back,BorderLayout.WEST);
        heading.add(UiStyle.label("Calendar disponibilitate",26,true,UiStyle.INK),BorderLayout.CENTER);
        heading.add(refresh,BorderLayout.EAST);
        JPanel body=transparent(new BorderLayout(0,20));
        JPanel nav=transparent(new BorderLayout(12,0));title.setHorizontalAlignment(SwingConstants.CENTER);
        previous.setPreferredSize(new Dimension(48,38));next.setPreferredSize(new Dimension(48,38));
        nav.add(previous,BorderLayout.WEST);nav.add(title,BorderLayout.CENTER);nav.add(next,BorderLayout.EAST);
        body.add(nav,BorderLayout.NORTH);grid.setOpaque(false);body.add(grid,BorderLayout.CENTER);
        card.add(heading,BorderLayout.NORTH);card.add(body,BorderLayout.CENTER);card.add(status,BorderLayout.SOUTH);
        add(card);previous.addActionListener(e->changeMonth(-1));next.addActionListener(e->changeMonth(1));
        refresh.addActionListener(e->refreshData());
        addHierarchyListener(e->{
            if((e.getChangeFlags()&java.awt.event.HierarchyEvent.SHOWING_CHANGED)!=0&&isShowing())refreshData();
        });render();
    }
    private JPanel transparent(LayoutManager layout){JPanel p=new JPanel(layout);p.setOpaque(false);return p;}
    private void changeMonth(int delta){if(busy)return;month=month.plusMonths(delta);states=new HashMap<>();render();refreshData();}
    private void render(){
        title.setText(month.format(DateTimeFormatter.ofPattern("LLLL yyyy",java.util.Locale.forLanguageTag("ro-RO"))));
        grid.removeAll();
        for(String name:new String[]{"Lu","Ma","Mi","Jo","Vi","Sâ","Du"}){
            JLabel l=UiStyle.label(name,13,true,UiStyle.MUTED);l.setHorizontalAlignment(SwingConstants.CENTER);grid.add(l);
        }
        int offset=month.atDay(1).getDayOfWeek().getValue()-1;
        int cells=((offset+month.lengthOfMonth()+6)/7)*7;
        for(int i=0;i<cells;i++){
            int n=i-offset+1;
            if(n<1||n>month.lengthOfMonth()){grid.add(transparent(new BorderLayout()));continue;}
            LocalDate date=month.atDay(n);int state=states.getOrDefault(date,0);
            JButton b=new DayButton(n,state,date.equals(LocalDate.now()));
            b.setEnabled(!busy&&technicianId!=null);
            b.setToolTipText(state==1?"Disponibilitate pentru cel puțin un serviciu de bază"
                    :state==2?"Zi liberă sau fără ore disponibile":"Fără program configurat");
            b.addActionListener(e->openDay(date));grid.add(b);
        }grid.revalidate();grid.repaint();
    }
    public void refreshData(){
        if(busy)return;
        if(technicianId==null){error("Contul nu este asociat unui tehnician.");return;}
        YearMonth requested=month;setBusy(true);status.setForeground(UiStyle.MUTED);status.setText("Se încarcă...");
        new SwingWorker<Map<LocalDate,Integer>,Void>(){
            @Override protected Map<LocalDate,Integer> doInBackground()throws Exception{
                Map<LocalDate,Integer> result=new HashMap<>();
                for(int day=1;day<=requested.lengthOfMonth();day++){
                    LocalDate date=requested.atDay(day);DayConfiguration c=service.loadDay(technicianId,date);
                    int state=0;
                    if("Libera".equals(c.dayType()))state=2;
                    else if(!c.workIntervals().isEmpty()||c.explicitlyConfigured()){
                        int shortest=c.availableOffers().stream()
                                .filter(s->c.selectedServiceIds().contains(s.serviceId()))
                                .filter(s->!TechnicianDayServicesPanel.isDesign(s)&&!TechnicianDayServicesPanel.isExtra(s))
                                .mapToInt(ServiceOffer::durationMinutes).filter(d->d>0).min().orElse(0);
                        state=shortest>0&&!availability.availableSlots(technicianId,date,shortest).isEmpty()?1:2;
                    }else if(!c.breaks().isEmpty())state=2;
                    result.put(date,state);
                }return result;
            }
            @Override protected void done(){
                setBusy(false);try{states=get();status.setText(" ");render();}catch(Exception ex){error(message(ex));}
            }
        }.execute();
    }
    private void openDay(LocalDate date){
        if(busy||technicianId==null)return;
        setBusy(true);status.setForeground(UiStyle.MUTED);status.setText("Se deschide ziua...");
        new SwingWorker<DayConfiguration,Void>(){
            @Override protected DayConfiguration doInBackground()throws Exception{return service.loadDay(technicianId,date);}
            @Override protected void done(){
                setBusy(false);
                try{
                    DayConfiguration c=get();status.setText(" ");
                    TechnicianDayDialog d=new TechnicianDayDialog(SwingUtilities.getWindowAncestor(
                            TechnicianAvailabilityPanel.this),technicianId,service,availability,c);
                    d.setVisible(true);if(d.isSaved())refreshData();
                }catch(Exception ex){error(message(ex));}
            }
        }.execute();
    }
    private void setBusy(boolean value){busy=value;previous.setEnabled(!value);next.setEnabled(!value);refresh.setEnabled(!value);render();}
    private void goHome(){
        if(onBack!=null){onBack.run();return;}
        Container p=getParent();while(p!=null){
            if(p.getLayout()instanceof CardLayout layout){layout.show(p,"HOME");return;}p=p.getParent();
        }
    }
    private void error(String text){status.setForeground(UiStyle.RED);status.setText("<html>"+text.replace("&","&amp;").replace("<","&lt;")+"</html>");}
    private String message(Throwable e){
        while(e.getCause()!=null&&(e instanceof ExecutionException||e instanceof AvailabilityService.AvailabilityException))e=e.getCause();
        return e.getMessage()==null?"Operația nu a putut fi efectuată.":e.getMessage();
    }
    private static class DayButton extends JButton{
        final int state;final boolean today;
        DayButton(int number,int state,boolean today){
            super(String.valueOf(number));this.state=state;this.today=today;
            setFont(new Font("SansSerif",Font.BOLD,17));setForeground(UiStyle.INK);
            setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));setPreferredSize(new Dimension(100,66));
        }
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(today?UiStyle.BEIGE:getModel().isRollover()?UiStyle.CREAM:UiStyle.FIELD);
            g.fillRoundRect(1,1,getWidth()-3,getHeight()-3,18,18);g.setColor(UiStyle.BORDER);
            g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,18,18);
            if(state!=0){g.setColor(state==1?UiStyle.GREEN:UiStyle.RED);int w=Math.max(12,getWidth()/3);
                g.fillRoundRect((getWidth()-w)/2,getHeight()-10,w,4,4,4);}
            g.dispose();super.paintComponent(graphics);
        }
    }
}


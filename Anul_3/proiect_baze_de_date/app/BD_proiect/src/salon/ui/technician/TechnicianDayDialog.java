package salon.ui.technician;

import salon.service.AvailabilityService;
import salon.service.TechnicianDayService;
import salon.service.TechnicianDayService.DayConfiguration;
import salon.service.TechnicianDayService.DayInterval;
import salon.service.TechnicianDayService.ServiceOffer;
import salon.ui.components.UiStyle;
import salon.ui.model.TimeInterval;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class TechnicianDayDialog extends JDialog {
    private final int technicianId;
    private final TechnicianDayService service;
    private final AvailabilityService availability;
    private final LocalDate date;
    private final List<IntervalRow> work=new ArrayList<>(),pauses=new ArrayList<>();
    private final JPanel workRows=vertical(),pauseRows=vertical();
    private final TechnicianDayServicesPanel services;
    private final JComboBox<PreviewChoice> choice=new JComboBox<>();
    private final JPanel result=transparent(new BorderLayout());
    private final JLabel total=label(" ",14,true,UiStyle.INK);
    private final JLabel status=label(" ",12,false,UiStyle.MUTED);
    private final JTabbedPane tabs=new JTabbedPane();
    private final Timer previewTimer;
    private boolean ready,rebuilding,saving,saved;
    private long previewVersion;
    private record PreviewChoice(List<ServiceOffer> services){
        int duration(){return services.stream().mapToInt(ServiceOffer::durationMinutes).sum();}
        BigDecimal price(){return services.stream().map(ServiceOffer::price).reduce(BigDecimal.ZERO,BigDecimal::add);}
        @Override public String toString(){return services.stream().map(ServiceOffer::name)
                .collect(java.util.stream.Collectors.joining(" + "));}
    }
    public TechnicianDayDialog(Window owner,int technicianId,TechnicianDayService service,
                               AvailabilityService availability,DayConfiguration config){
        super(owner,"Program · "+config.date().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),ModalityType.APPLICATION_MODAL);
        this.technicianId=technicianId;this.service=service;this.availability=availability;this.date=config.date();
        previewTimer=new Timer(250,e->refreshPreview());previewTimer.setRepeats(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel background=new JPanel(new BorderLayout());background.setBackground(UiStyle.CREAM);
        background.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JPanel card=rounded(UiStyle.WHITE);card.setLayout(new BorderLayout(0,18));
        card.setBorder(BorderFactory.createEmptyBorder(24,26,24,26));
        JPanel heading=vertical();heading.add(label("SALON & ACADEMIE",11,true,UiStyle.MUTED));
        heading.add(Box.createVerticalStrut(8));heading.add(label(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),28,true,UiStyle.INK));
        heading.add(Box.createVerticalStrut(14));
        JPanel types=transparent(new GridLayout(1,2,12,0));
        JButton available=UiStyle.secondaryButton("Zi disponibilă"),off=UiStyle.secondaryButton("Zi liberă");
        available.setPreferredSize(new Dimension(200,42));
        boolean dayOff="Libera".equals(config.dayType());
        available.setBackground(dayOff?UiStyle.FIELD:UiStyle.BEIGE);off.setBackground(dayOff?UiStyle.BEIGE:UiStyle.FIELD);
        types.add(available);types.add(off);heading.add(types);card.add(heading,BorderLayout.NORTH);
        JPanel program=vertical();program.add(sectionHeader("Program de lucru",()->addRow(false,null)));program.add(workRows);
        program.add(Box.createVerticalStrut(16));program.add(sectionHeader("Pauze",()->addRow(true,null)));program.add(pauseRows);
        for(DayInterval i:config.workIntervals())addRow(false,i);
        if(work.isEmpty())addRow(false,null);
        if(!dayOff)for(DayInterval i:config.breaks())addRow(true,i);
        tabs.addTab("Program",scroll(program));
        services=new TechnicianDayServicesPanel(config,this::rebuildChoices);
        tabs.addTab("Servicii",scroll(services));
        JPanel preview=transparent(new BorderLayout(0,14));
        JPanel previewTop=new JPanel(new GridBagLayout());previewTop.setOpaque(false);
        GridBagConstraints pc=new GridBagConstraints();pc.gridx=0;pc.weightx=1;
        pc.fill=GridBagConstraints.HORIZONTAL;pc.anchor=GridBagConstraints.WEST;
        pc.gridy=0;pc.insets=new Insets(0,0,8,0);
        previewTop.add(label("Verifică orele pentru",14,true,UiStyle.INK),pc);
        choice.setPreferredSize(new Dimension(500,42));choice.setMinimumSize(new Dimension(100,42));
        choice.setMaximumRowCount(10);
        choice.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean selected,boolean focus){
                JLabel item=(JLabel)super.getListCellRendererComponent(l,v,i,selected,focus);
                item.setText(v instanceof PreviewChoice c?c.toString():"Bifează un serviciu în fila Servicii");return item;
            }
        });
        choice.addActionListener(e->{if(!rebuilding)schedulePreview();});
        pc.gridy=1;previewTop.add(choice,pc);
        pc.gridy=2;previewTop.add(label("<html>Lista conține combinații ale serviciilor bifate pentru această zi. "
                +"Designul este opțional și nu se rezervă singur.</html>",12,false,UiStyle.MUTED),pc);
        pc.gridy=3;previewTop.add(total,pc);
        preview.add(previewTop,BorderLayout.NORTH);preview.add(scroll(result),BorderLayout.CENTER);
        tabs.addTab("Previzualizare disponibilitate",preview);
        tabs.setFont(new Font("SansSerif",Font.PLAIN,13));
        tabs.addChangeListener(e->{if(tabs.getSelectedIndex()==2)schedulePreview();});
        card.add(tabs,BorderLayout.CENTER);
        JPanel footer=transparent(new BorderLayout(0,12));footer.add(status,BorderLayout.NORTH);
        JPanel actions=transparent(new GridLayout(1,2,12,0));
        JButton close=UiStyle.secondaryButton("Închide"),save=UiStyle.primaryButton("Salvează ziua");
        save.setPreferredSize(new Dimension(200,44));close.addActionListener(e->{if(!saving)dispose();});
        save.addActionListener(e->saveDay(false));off.addActionListener(e->saveDay(true));
        available.addActionListener(e->{available.setBackground(UiStyle.BEIGE);off.setBackground(UiStyle.FIELD);
            status.setText(" ");tabs.setSelectedIndex(0);});
        actions.add(close);actions.add(save);footer.add(actions,BorderLayout.SOUTH);card.add(footer,BorderLayout.SOUTH);
        background.add(card);setContentPane(background);
        getRootPane().registerKeyboardAction(e->{if(!saving)dispose();},KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter(){
            @Override public void windowOpened(WindowEvent e){if(tabs.getSelectedIndex()==2)schedulePreview();}
            @Override public void windowClosed(WindowEvent e){previewTimer.stop();previewVersion++;}
        });
        if(config.availableOffers().isEmpty())status.setText("Tehnician ID "+technicianId+": nicio ofertă activă primită din baza de date.");
        else if(dayOff)status.setText("Zi marcată liberă. Salvează programul pentru a o face disponibilă.");
        ready=true;rebuildChoices();setResizable(true);
        GraphicsConfiguration gc=owner==null?GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration():owner.getGraphicsConfiguration();
        Rectangle screen=new Rectangle(gc.getBounds());Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(gc);
        screen.x+=insets.left;screen.y+=insets.top;screen.width-=insets.left+insets.right;screen.height-=insets.top+insets.bottom;
        int width=Math.min(820,screen.width-32),height=Math.min(740,screen.height-32);
        setMinimumSize(new Dimension(Math.min(740,width),Math.min(560,height)));setSize(width,height);setLocationRelativeTo(owner);
        setLocation(Math.max(screen.x,Math.min(getX(),screen.x+screen.width-getWidth())),
                Math.max(screen.y,Math.min(getY(),screen.y+screen.height-getHeight())));
    }
    public boolean isSaved(){return saved;}
    private static JPanel transparent(LayoutManager layout){JPanel p=new JPanel(layout);p.setOpaque(false);return p;}
    private static JPanel vertical(){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));return p;}
    private static JLabel label(String text,int size,boolean bold,Color color){
        JLabel l=UiStyle.label(text,size,bold,color);l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;
    }
    private static JPanel rounded(Color color){
        JPanel p=new JPanel(){@Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);g.fillRoundRect(0,0,getWidth(),getHeight(),28,28);g.dispose();
        }};p.setOpaque(false);return p;
    }
    private JScrollPane scroll(Component c){
        JScrollPane p=new JScrollPane(c);p.setBorder(BorderFactory.createEmptyBorder(10,2,6,2));
        p.setOpaque(false);p.getViewport().setOpaque(false);p.getVerticalScrollBar().setUnitIncrement(18);return p;
    }
    private JPanel sectionHeader(String title,Runnable add){
        JPanel p=transparent(new BorderLayout());p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));p.add(label(title,15,true,UiStyle.INK),BorderLayout.CENTER);
        JButton b=UiStyle.secondaryButton("+");b.setToolTipText("Adaugă interval");b.addActionListener(e->{if(!saving)add.run();});
        p.add(b,BorderLayout.EAST);return p;
    }
    private void rebuildChoices(){
        if(!ready)return;PreviewChoice old=(PreviewChoice)choice.getSelectedItem();
        rebuilding=true;choice.removeAllItems();
        List<ServiceOffer> designs=new ArrayList<>();designs.add(null);designs.addAll(services.selectedDesign());
        for(ServiceOffer b:services.selectedBase()){
            List<ServiceOffer> extras=new ArrayList<>();extras.add(null);
            if(TechnicianDayServicesPanel.isMaintenance(b))extras.addAll(services.selectedExtra());
            for(ServiceOffer d:designs)for(ServiceOffer x:extras){
                List<ServiceOffer> combination=new ArrayList<>();combination.add(b);
                if(d!=null)combination.add(d);if(x!=null)combination.add(x);
                choice.addItem(new PreviewChoice(List.copyOf(combination)));
            }
        }
        if(old!=null)for(int i=0;i<choice.getItemCount();i++)if(choice.getItemAt(i).equals(old)){choice.setSelectedIndex(i);break;}
        choice.setEnabled(choice.getItemCount()>0);rebuilding=false;schedulePreview();
    }
    private void showResult(String text,Color color){
        result.removeAll();JLabel message=label("<html>"+escape(text)+"</html>",14,false,color);
        message.setBorder(BorderFactory.createEmptyBorder(16,12,16,12));result.add(message,BorderLayout.NORTH);
        result.revalidate();result.repaint();
    }
    private void schedulePreview(){
        if(!ready||saving||rebuilding)return;previewVersion++;previewTimer.stop();
        PreviewChoice selected=(PreviewChoice)choice.getSelectedItem();
        if(selected==null){total.setText(" ");showResult("Nu este selectat un serviciu de bază. Deschide fila Servicii și bifează o variantă.",UiStyle.MUTED);return;}
        total.setText("Total: "+selected.price().stripTrailingZeros().toPlainString()+" lei · "+selected.duration()+" min");
        if(tabs.getSelectedIndex()!=2){showResult("Alege o combinație pentru a verifica orele disponibile.",UiStyle.MUTED);return;}
        showResult("Se calculează orele disponibile...",UiStyle.MUTED);previewTimer.restart();
    }
    private void refreshPreview(){
        if(!ready||saving||tabs.getSelectedIndex()!=2)return;
        PreviewChoice selected=(PreviewChoice)choice.getSelectedItem();if(selected==null){schedulePreview();return;}
        final List<DayInterval>w,p;
        try{
            w=readRows(work);p=readRows(pauses);validateDraft(w,p);
            if(selected.services().stream().anyMatch(s->s.durationMinutes()<=0))
                throw new IllegalArgumentException("Un serviciu nu are durată validă.");
        }catch(RuntimeException ex){showResult(ex.getMessage(),UiStyle.RED);return;}
        int duration=selected.duration();long request=++previewVersion;
        new SwingWorker<List<LocalDateTime>,Void>(){
            @Override protected List<LocalDateTime> doInBackground(){
                List<TimeInterval> busy=new ArrayList<>(availability.loadAppointmentIntervals(technicianId,date));
                busy.addAll(availability.loadCourseIntervals(technicianId,date));
                for(DayInterval i:p)busy.add(new TimeInterval(i.start(),i.end(),"Absenta"));
                List<DayInterval> merged=new ArrayList<>();
                for(DayInterval i:w){
                    if(!merged.isEmpty()&&merged.get(merged.size()-1).end().equals(i.start())){
                        DayInterval old=merged.remove(merged.size()-1);merged.add(new DayInterval(old.start(),i.end()));
                    }else merged.add(i);
                }
                TreeSet<LocalDateTime> times=new TreeSet<>();LocalDateTime now=LocalDateTime.now();
                for(DayInterval i:merged)for(LocalDateTime t=i.start();!t.plusMinutes(duration).isAfter(i.end());t=t.plusMinutes(30)){
                    LocalDateTime end=t.plusMinutes(duration);boolean occupied=false;
                    for(TimeInterval b:busy)if(t.isBefore(b.end())&&end.isAfter(b.start())){occupied=true;break;}
                    if(!occupied&&t.isAfter(now))times.add(t);
                }
                return new ArrayList<>(times);
            }
            @Override protected void done(){
                if(request!=previewVersion||!isDisplayable())return;
                try{
                    List<LocalDateTime> times=get();
                    if(times.isEmpty()){
                        showResult(date.isBefore(LocalDate.now())?"Ziua selectată este în trecut; nu există ore viitoare de rezervat."
                                :"Nu există un interval liber de "+duration+" minute pentru combinația aleasă. Verifică programul, pauzele și rezervările existente.",UiStyle.MUTED);return;
                    }
                    JPanel grid=transparent(new GridLayout(0,4,10,10));
                    for(LocalDateTime t:times){
                        JPanel tile=rounded(UiStyle.FIELD);tile.setLayout(new BorderLayout());
                        tile.setBorder(BorderFactory.createEmptyBorder(14,8,14,8));
                        JLabel hour=label(t.format(DateTimeFormatter.ofPattern("hh:mm a",java.util.Locale.ENGLISH)),15,true,UiStyle.INK);
                        hour.setHorizontalAlignment(SwingConstants.CENTER);tile.add(hour);grid.add(tile);
                    }
                    JPanel body=transparent(new BorderLayout(0,12));
                    body.add(label("<html>"+times.size()+" ore de început · previzualizare pentru modificările nesalvate</html>",12,false,UiStyle.MUTED),BorderLayout.NORTH);
                    body.add(grid,BorderLayout.CENTER);result.removeAll();result.add(body,BorderLayout.NORTH);result.revalidate();result.repaint();
                }catch(Exception ex){ex.printStackTrace();showResult(message(ex),UiStyle.RED);}
            }
        }.execute();
    }
    private void addRow(boolean pause,DayInterval initial){
        List<IntervalRow> list=pause?pauses:work;JPanel container=pause?pauseRows:workRows;
        IntervalRow row=new IntervalRow(initial==null?(pause?780:540):minute(initial.start()),initial==null?(pause?810:1080):minute(initial.end()));
        row.remove.addActionListener(e->{if(saving)return;list.remove(row);container.remove(row);container.revalidate();container.repaint();schedulePreview();});
        list.add(row);container.add(row);container.revalidate();container.repaint();schedulePreview();
    }
    private int minute(LocalDateTime t){return t.toLocalDate().isAfter(date)?1440:t.getHour()*60+t.getMinute();}
    private List<DayInterval> readRows(List<IntervalRow> rows){
        List<DayInterval> result=new ArrayList<>();
        for(IntervalRow row:rows){int from=row.start.value(false),to=row.end.value(true);
            if(to<=from)throw new IllegalArgumentException("Sfârșitul trebuie să fie după început. Verifică și AM/PM.");
            result.add(new DayInterval(date.atStartOfDay().plusMinutes(from),date.atStartOfDay().plusMinutes(to)));}
        result.sort(Comparator.comparing(DayInterval::start));
        for(int i=1;i<result.size();i++)if(result.get(i).start().isBefore(result.get(i-1).end()))
            throw new IllegalArgumentException("Există intervale de același tip suprapuse.");return result;
    }
    private void validateDraft(List<DayInterval>w,List<DayInterval>p){
        if(w.isEmpty())throw new IllegalArgumentException("Adaugă un interval de lucru în fila Program.");
        for(DayInterval pause:p)if(w.stream().noneMatch(i->!pause.start().isBefore(i.start())&&!pause.end().isAfter(i.end())))
            throw new IllegalArgumentException("Pauzele trebuie să fie în interiorul programului de lucru.");
    }
    private void saveDay(boolean off){
        if(saving)return;final List<DayInterval>w,p;final Set<Integer>ids;
        try{
            w=off?List.of():readRows(work);p=off?List.of():readRows(pauses);ids=off?Set.of():services.selectedIds();
            if(!off){validateDraft(w,p);if(services.selectedBase().isEmpty())throw new IllegalArgumentException("Bifează cel puțin un serviciu de bază în fila Servicii.");}
        }catch(RuntimeException ex){error(ex.getMessage());return;}
        saving=true;previewVersion++;previewTimer.stop();setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);enableTree(getContentPane(),false);
        status.setForeground(UiStyle.MUTED);status.setText("Se salvează...");
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground()throws Exception{
                if(off)service.saveDayOff(technicianId,date);else service.saveWorkingDay(technicianId,date,w,p,ids);return null;
            }
            @Override protected void done(){
                saving=false;enableTree(getContentPane(),true);choice.setEnabled(choice.getItemCount()>0);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                try{get();saved=true;dispose();}catch(Exception ex){ex.printStackTrace();error(message(ex));schedulePreview();}
            }
        }.execute();
    }
    private void enableTree(Component c,boolean enabled){c.setEnabled(enabled);if(c instanceof Container p)for(Component child:p.getComponents())enableTree(child,enabled);}
    private String escape(String s){return s==null?"Operația nu a putut fi efectuată.":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private void error(String text){status.setForeground(UiStyle.RED);status.setText("<html>"+escape(text)+"</html>");}
    private String message(Throwable e){while(e.getCause()!=null&&(e instanceof ExecutionException||e instanceof AvailabilityService.AvailabilityException))e=e.getCause();
        return e.getMessage()==null?"Operația nu a putut fi efectuată.":e.getMessage();}
    private class IntervalRow extends JPanel{
        final DayTimePicker start,end;final JButton remove=UiStyle.secondaryButton("×");
        IntervalRow(int from,int to){
            super(new BorderLayout(10,0));setOpaque(false);setAlignmentX(Component.LEFT_ALIGNMENT);
            setBorder(BorderFactory.createEmptyBorder(6,0,8,0));setMaximumSize(new Dimension(Integer.MAX_VALUE,208));
            JPanel pair=transparent(new GridLayout(1,2,16,0));
            start=new DayTimePicker(from,"De la",TechnicianDayDialog.this::schedulePreview);
            end=new DayTimePicker(to,"Până la",TechnicianDayDialog.this::schedulePreview);pair.add(start);pair.add(end);add(pair,BorderLayout.CENTER);
            JPanel action=transparent(new BorderLayout());remove.setPreferredSize(new Dimension(38,36));
            remove.setToolTipText("Șterge intervalul; salvează ziua pentru a aplica");action.add(remove,BorderLayout.NORTH);add(action,BorderLayout.EAST);
        }
    }
}


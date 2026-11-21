package salon.ui.trainer;

import salon.db.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TrainerCoursesPanel extends JPanel {
    private static final Color CREAM=new Color(244,240,233), INK=new Color(30,30,30),
            MUTED=new Color(115,110,103), FIELD=new Color(250,249,247), BORDER=new Color(225,220,213);
    private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final String userEmail;
    private final JPanel courseList=new JPanel(), sessionList=new JPanel();
    private final JLabel status=new JLabel("Selectează un curs pentru a vedea sesiunile tale.");
    private final JLabel courseTitle=new JLabel("Cursurile mele");
    private final JButton propose,configure;
    private Course selected;
    private Integer trainerId;
    private record Course(int id,String name,String level,int days,BigDecimal price,BigDecimal advance) {}

    public TrainerCoursesPanel(String userEmail,Runnable showHome) {
        this.userEmail=userEmail;
        setLayout(new BorderLayout(20,20)); setBackground(CREAM);
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel header=new JPanel(new BorderLayout(14,0)); header.setOpaque(false);
        header.add(button("Înapoi",showHome,false),BorderLayout.WEST);
        courseTitle.setFont(new Font("SansSerif",Font.BOLD,24)); courseTitle.setForeground(INK);
        header.add(courseTitle,BorderLayout.CENTER);
        add(header,BorderLayout.NORTH);
        courseList.setLayout(new BoxLayout(courseList,BoxLayout.Y_AXIS)); courseList.setOpaque(false);
        sessionList.setLayout(new BoxLayout(sessionList,BoxLayout.Y_AXIS)); sessionList.setOpaque(false);
        JScrollPane left=scroll(courseList); left.setPreferredSize(new Dimension(315,0));
        JPanel right=new JPanel(new BorderLayout(0,16)); right.setOpaque(false);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); actions.setOpaque(false);
        propose=button("Propune un curs",()->openCalendar(null),true);
        configure=button("Configurare / modificare curs",this::configureCourse,false);
        actions.add(propose); actions.add(configure);
        right.add(actions,BorderLayout.NORTH); right.add(scroll(sessionList),BorderLayout.CENTER);
        JPanel center=new JPanel(new BorderLayout(20,0)); center.setOpaque(false);
        center.add(left,BorderLayout.WEST); center.add(right,BorderLayout.CENTER); add(center,BorderLayout.CENTER);
        status.setForeground(MUTED); add(status,BorderLayout.SOUTH);
        refreshData();
    }
    public void refreshData() {
        try(Connection c=DatabaseConnection.getConnection()) {
            trainerId=null;
            try(PreparedStatement p=c.prepareStatement("SELECT u.id_angajat FROM utilizator u JOIN trainer t ON t.id_angajat=u.id_angajat WHERE u.email=?")) {
                p.setString(1,userEmail); try(ResultSet r=p.executeQuery()) { if(r.next()) trainerId=r.getInt(1); }
            }
            Integer old=selected==null?null:selected.id(); selected=null; courseList.removeAll();
            try(PreparedStatement p=c.prepareStatement("SELECT id_curs,denumire,nivel,nr_zile,pret_implicit,avans FROM curs ORDER BY id_curs"); ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    Course item=course(r);
                    if(old!=null && old==item.id()) selected=item;
                    JPanel card=card(); card.setLayout(new BorderLayout(0,8));
                    JLabel details=label(item.days()+" zile · "+money(item.price())+" lei · avans "+money(item.advance())+" lei",12);
                    card.add(button(item.name(),()->select(item),false),BorderLayout.NORTH);
                    card.add(details,BorderLayout.CENTER);
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE,105));
                    courseList.add(card); courseList.add(Box.createVerticalStrut(10));
                }
            }
            propose.setEnabled(trainerId!=null && selected!=null);
            configure.setEnabled(trainerId!=null && selected!=null);
            loadSessions();
            if(trainerId==null) status.setText("Contul nu este asociat unui trainer.");
        } catch(SQLException ex) { error(ex); }
        courseList.revalidate(); courseList.repaint();
    }
    private Course course(ResultSet r) throws SQLException {
        return new Course(r.getInt("id_curs"),r.getString("denumire"),r.getString("nivel"),r.getInt("nr_zile"),r.getBigDecimal("pret_implicit"),r.getBigDecimal("avans"));
    }
    private void select(Course item) {
        selected=item; propose.setEnabled(trainerId!=null); configure.setEnabled(trainerId!=null);
        status.setText(item.name()+" — "+item.days()+" zile; preț "+money(item.price())+" lei; avans "+money(item.advance())+" lei.");
        loadSessions();
    }
    private void loadSessions() {
        sessionList.removeAll();
        if(selected==null || trainerId==null) { sessionList.add(label("Selectează un curs din stânga.",14)); finishList(); return; }
        String sql="SELECT id_sesiune,data_sesiune,pret,nr_maxim_cursanti,stare FROM sesiune_curs WHERE id_trainer=? AND id_curs=? ORDER BY data_sesiune DESC,id_sesiune DESC";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,trainerId); p.setInt(2,selected.id());
            try(ResultSet r=p.executeQuery()) {
                boolean any=false;
                while(r.next()) {
                    any=true; int id=r.getInt("id_sesiune"); String state=r.getString("stare");
                    TreeSet<LocalDate> dates=readDates(c,id);
                    JPanel card=card(); card.setLayout(new BorderLayout(12,8));
                    JPanel info=new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
                    info.add(label(selected.name()+" — sesiunea #"+id,16));
                    info.add(Box.createVerticalStrut(8));
                    JTextArea days=new JTextArea(dates.isEmpty()?"Zile nestabilite":formatDates(dates));
                    days.setEditable(false); days.setLineWrap(true); days.setWrapStyleWord(true);
                    days.setOpaque(false); days.setForeground(MUTED); days.setFont(new Font("SansSerif",Font.PLAIN,13)); info.add(days);
                    info.add(label(dates.size()+" zile · "+money(r.getBigDecimal("pret"))+" lei · "+r.getInt("nr_maxim_cursanti")+" locuri · "+state,12));
                    JPanel controls=new JPanel(new GridLayout(0,1,0,8)); controls.setOpaque(false);
                    JButton edit=button("Modifică zilele",()->openCalendar(id),false);
                    edit.setEnabled("Planificata".equals(state)); controls.add(edit);
                    JButton cancel=button("Anulează",()->cancel(id),false);
                    cancel.setEnabled("Planificata".equals(state)); controls.add(cancel);
                    card.add(info,BorderLayout.CENTER); card.add(controls,BorderLayout.EAST);
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE,210));
                    sessionList.add(card); sessionList.add(Box.createVerticalStrut(12));
                }
                if(!any) sessionList.add(label("Nu ai sesiuni propuse pentru acest curs.",14));
            }
        } catch(SQLException ex) { error(ex); }
        finishList();
    }
    private TreeSet<LocalDate> readDates(Connection c,int id) throws SQLException {
        TreeSet<LocalDate> dates=new TreeSet<>();
        try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(data_zi,DATE(inceput)) AS zi FROM interval_sesiune WHERE id_sesiune=? ORDER BY zi")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { while(r.next()) { java.sql.Date d=r.getDate(1); if(d!=null) dates.add(d.toLocalDate()); } }
        }
        return dates;
    }
    private void openCalendar(Integer sessionId) {
        if(selected==null || trainerId==null) return;
        Course target=selected; TreeSet<LocalDate> dates=new TreeSet<>(); int capacity=8;
        if(sessionId!=null) {
            try(Connection c=DatabaseConnection.getConnection()) {
                dates.addAll(readDates(c,sessionId));
                try(PreparedStatement p=c.prepareStatement("SELECT nr_maxim_cursanti FROM sesiune_curs WHERE id_sesiune=? AND id_trainer=?")) {
                    p.setInt(1,sessionId); p.setInt(2,trainerId); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Sesiunea nu mai există."); capacity=r.getInt(1); }
                }
            } catch(SQLException ex) { error(ex); return; }
        }
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this),sessionId==null?"Propune un curs":"Modifică zilele sesiunii",Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root=new JPanel(new BorderLayout(16,16)); root.setBackground(CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        root.add(label(target.name()+" — "+target.days()+" zile · "+money(target.price())+" lei · avans "+money(target.advance())+" lei",18),BorderLayout.NORTH);
        JPanel grid=new JPanel(new GridLayout(0,7,6,6)); grid.setOpaque(false);
        JPanel calendar=new JPanel(new BorderLayout(0,12)); calendar.setOpaque(false);
        YearMonth initial=dates.isEmpty()?YearMonth.now():YearMonth.from(dates.first());
        JComboBox<String> month=new JComboBox<>(new String[]{"Ianuarie","Februarie","Martie","Aprilie","Mai","Iunie","Iulie","August","Septembrie","Octombrie","Noiembrie","Decembrie"});
        month.setSelectedIndex(initial.getMonthValue()-1);
        JSpinner year=new JSpinner(new SpinnerNumberModel(initial.getYear(),1900,2200,1)); year.setEditor(new JSpinner.NumberEditor(year,"#"));
        JPanel nav=new JPanel(new FlowLayout(FlowLayout.CENTER,10,0)); nav.setOpaque(false); nav.add(month); nav.add(year);
        calendar.add(nav,BorderLayout.NORTH); calendar.add(grid,BorderLayout.CENTER);
        JLabel count=label("",14);
        JTextArea chosen=new JTextArea(); chosen.setEditable(false); chosen.setLineWrap(true); chosen.setWrapStyleWord(true);
        chosen.setBackground(FIELD); chosen.setFont(new Font("SansSerif",Font.PLAIN,13));
        JComboBox<String> rule=new JComboBox<>(new String[]{"Alege regula pentru propunere","Zile consecutive, inclusiv weekend","Doar luni–vineri"});
        JSpinner seats=new JSpinner(new SpinnerNumberModel(capacity,1,8,1));
        JLabel message=label("Selectează zilele sau o zi de început și apoi propune automat.",12);
        Runnable[] render=new Runnable[1];
        render[0]=()->{
            grid.removeAll();
            for(String name:new String[]{"Lu","Ma","Mi","Jo","Vi","Sâ","Du"}) grid.add(label(name,13));
            YearMonth ym=YearMonth.of((Integer)year.getValue(),month.getSelectedIndex()+1);
            int offset=ym.atDay(1).getDayOfWeek().getValue()-1;
            for(int i=0;i<offset;i++) grid.add(new JLabel());
            for(int n=1;n<=ym.lengthOfMonth();n++) {
                LocalDate date=ym.atDay(n); JToggleButton b=new JToggleButton(String.valueOf(n),dates.contains(date));
                b.setOpaque(true); b.setBackground(dates.contains(date)?INK:Color.WHITE);
                b.setForeground(dates.contains(date)?Color.WHITE:INK); b.setFocusPainted(false);
                b.setEnabled(!date.isBefore(LocalDate.now()));
                b.addActionListener(e->{
                    if(dates.contains(date)) dates.remove(date);
                    else if(dates.size()<target.days()) dates.add(date);
                    else message.setText("Ai ales deja toate cele "+target.days()+" zile. Deselectează o zi pentru a o înlocui.");
                    render[0].run();
                }); grid.add(b);
            }
            int cells=offset+ym.lengthOfMonth(); for(int i=cells;i<42;i++) grid.add(new JLabel());
            count.setText("Zile alese: "+dates.size()+" / "+target.days()); chosen.setText(formatDates(dates));
            grid.revalidate(); grid.repaint();
        };
        month.addActionListener(e->render[0].run()); year.addChangeListener(e->render[0].run());
        JPanel side=new JPanel(); side.setOpaque(false); side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(310,0));
        side.add(count); side.add(Box.createVerticalStrut(12));
        JScrollPane chosenScroll=new JScrollPane(chosen); chosenScroll.setPreferredSize(new Dimension(300,180)); side.add(chosenScroll);
        side.add(Box.createVerticalStrut(12)); side.add(rule);
        side.add(button("Propune de la prima zi aleasă",()->{
            if(dates.isEmpty()) { message.setText("Selectează mai întâi o zi de început."); return; }
            if(rule.getSelectedIndex()==0) { message.setText("Alege explicit regula pentru weekenduri."); return; }
            LocalDate date=dates.first(); dates.clear();
            while(dates.size()<target.days()) {
                if(rule.getSelectedIndex()==1 || date.getDayOfWeek().getValue()<=5) dates.add(date);
                date=date.plusDays(1);
            }
            message.setText("Poți ajusta manual zilele propuse. Sărbătorile nu sunt excluse automat."); render[0].run();
        },false));
        side.add(button("Golește selecția",()->{dates.clear(); render[0].run();},false));
        side.add(label("Număr maxim de cursanți (1–8)",13)); side.add(seats);
        JPanel middle=new JPanel(new BorderLayout(18,0)); middle.setOpaque(false);
        middle.add(calendar,BorderLayout.CENTER); middle.add(side,BorderLayout.EAST); root.add(middle,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(8,8)); footer.setOpaque(false); footer.add(message,BorderLayout.NORTH);
        footer.add(button("Salvează zilele propuse",()->{
            try {
                if(dates.size()!=target.days()) throw new IllegalArgumentException("Selectează exact "+target.days()+" zile.");
                if(dates.first().isBefore(LocalDate.now())) throw new IllegalArgumentException("Nu poți programa zile în trecut.");
                saveSession(target,sessionId,dates,(Integer)seats.getValue());
                dialog.dispose(); loadSessions(); status.setText("Zilele sesiunii au fost salvate. Orele rămân nestabilite.");
            } catch(Exception ex) { ex.printStackTrace(); message.setText(ex.getMessage()); }
        },true),BorderLayout.EAST); root.add(footer,BorderLayout.SOUTH);
        dialog.setContentPane(root); sizeDialog(dialog,1080,760); render[0].run(); dialog.setVisible(true);
    }
    private void saveSession(Course target,Integer sessionId,TreeSet<LocalDate> dates,int seats) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("SELECT id_angajat FROM trainer WHERE id_angajat=? FOR UPDATE")) {
                    p.setInt(1,trainerId); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Trainerul nu mai există."); }
                }
                Course actual;
                try(PreparedStatement p=c.prepareStatement("SELECT id_curs,denumire,nivel,nr_zile,pret_implicit,avans FROM curs WHERE id_curs=? FOR UPDATE")) {
                    p.setInt(1,target.id()); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Cursul nu mai există."); actual=course(r); }
                }
                if(actual.days()!=dates.size()) throw new SQLException("Configurația cursului s-a schimbat. Reîncarcă pagina.");
                if(actual.price()==null || actual.advance()==null) throw new SQLException("Configurează prețul și avansul cursului.");
                int id;
                if(sessionId==null) {
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO sesiune_curs (id_curs,id_trainer,data_sesiune,durata,pret,nr_maxim_cursanti,stare) VALUES (?,?,?,NULL,?,?,'Planificata')",Statement.RETURN_GENERATED_KEYS)) {
                        p.setInt(1,target.id()); p.setInt(2,trainerId); p.setDate(3,java.sql.Date.valueOf(dates.first())); p.setBigDecimal(4,actual.price()); p.setInt(5,seats); p.executeUpdate();
                        try(ResultSet r=p.getGeneratedKeys()) { if(!r.next()) throw new SQLException("Nu s-a obținut ID-ul sesiunii."); id=r.getInt(1); }
                    }
                } else {
                    id=sessionId;
                    try(PreparedStatement p=c.prepareStatement("SELECT stare FROM sesiune_curs WHERE id_sesiune=? AND id_trainer=? AND id_curs=? FOR UPDATE")) {
                        p.setInt(1,id); p.setInt(2,trainerId); p.setInt(3,target.id());
                        try(ResultSet r=p.executeQuery()) { if(!r.next() || !"Planificata".equals(r.getString(1))) throw new SQLException("Sesiunea nu mai poate fi modificată."); }
                    }
                    try(PreparedStatement p=c.prepareStatement("SELECT id_inscriere FROM inscriere WHERE id_sesiune=? AND stare NOT IN ('Retrasa','Inlocuita') FOR UPDATE")) {
                        p.setInt(1,id); try(ResultSet r=p.executeQuery()) { if(r.next()) throw new SQLException("Sesiunea are înscrieri active. Mutarea lor necesită un flux separat; zilele nu au fost schimbate."); }
                    }
                    try(PreparedStatement p=c.prepareStatement("UPDATE sesiune_curs SET data_sesiune=?,durata=NULL,nr_maxim_cursanti=? WHERE id_sesiune=? AND id_trainer=?")) {
                        p.setDate(1,java.sql.Date.valueOf(dates.first())); p.setInt(2,seats); p.setInt(3,id); p.setInt(4,trainerId); p.executeUpdate();
                    }
                    try(PreparedStatement p=c.prepareStatement("DELETE FROM interval_sesiune WHERE id_sesiune=?")) { p.setInt(1,id); p.executeUpdate(); }
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO interval_sesiune (id_sesiune,data_zi,inceput,sfarsit) VALUES (?,?,NULL,NULL)")) {
                    for(LocalDate d:dates) { p.setInt(1,id); p.setDate(2,java.sql.Date.valueOf(d)); p.addBatch(); } p.executeBatch();
                }
                c.commit();
            } catch(SQLException | RuntimeException ex) {
                try { c.rollback(); } catch(SQLException rollback) { ex.addSuppressed(rollback); } throw ex;
            }
        }
    }
    private void configureCourse() {
        if(selected==null) return; Course target=selected;
        JDialog d=new JDialog(SwingUtilities.getWindowAncestor(this),"Configurare / modificare curs",Dialog.ModalityType.APPLICATION_MODAL);
        JPanel form=new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        JTextField days=new JTextField(String.valueOf(target.days()),20), price=new JTextField(money(target.price()),20), advance=new JTextField(money(target.advance()),20);
        JLabel msg=label("Modificările se aplică configurației, nu prețurilor sesiunilor existente.",12);
        GridBagConstraints g=new GridBagConstraints(); g.gridx=0; g.weightx=1; g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(5,0,5,0);
        JComponent[] rows={label(target.name(),20),label("Număr de zile",13),days,label("Preț integral (lei)",13),price,label("Avans (lei)",13),advance};
        for(int i=0;i<rows.length;i++) {g.gridy=i; form.add(rows[i],g);}
        JButton save=button("Salvează configurația",()->{
            try {
                int n=Integer.parseInt(days.getText().trim());
                BigDecimal p=new BigDecimal(price.getText().trim().replace(',','.')), a=new BigDecimal(advance.getText().trim().replace(',','.'));
                if(n<1 || n>365) throw new IllegalArgumentException("Numărul de zile trebuie să fie între 1 și 365.");
                if(p.signum()<0 || a.signum()<0 || a.compareTo(p)>0) throw new IllegalArgumentException("Avansul trebuie să fie între 0 și prețul integral.");
                p=p.setScale(2,java.math.RoundingMode.UNNECESSARY); a=a.setScale(2,java.math.RoundingMode.UNNECESSARY);
                if(p.precision()>10 || a.precision()>10) throw new IllegalArgumentException("Suma este prea mare.");
                try(Connection c=DatabaseConnection.getConnection(); PreparedStatement st=c.prepareStatement("UPDATE curs SET nr_zile=?,pret_implicit=?,avans=? WHERE id_curs=?")) {
                    st.setInt(1,n); st.setBigDecimal(2,p); st.setBigDecimal(3,a); st.setInt(4,target.id()); if(st.executeUpdate()!=1) throw new SQLException("Cursul nu mai există.");
                }
                d.dispose(); refreshData(); status.setText("Configurația cursului a fost salvată.");
            } catch(Exception ex) { msg.setText("<html>"+escape(ex.getMessage())+"</html>"); }
        },true);
        g.gridy=7; form.add(save,g); g.gridy=8; form.add(msg,g);
        d.setContentPane(form); sizeDialog(d,720,520); d.setVisible(true);
    }
    private void cancel(int id) {
        int answer=JOptionPane.showConfirmDialog(this,"Anulezi sesiunea? Înscrierile și plățile nu sunt modificate sau rambursate automat.","Anulare sesiune",JOptionPane.YES_NO_OPTION);
        if(answer!=JOptionPane.YES_OPTION) return;
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement("UPDATE sesiune_curs SET stare='Anulata' WHERE id_sesiune=? AND id_trainer=? AND stare='Planificata'")) {
            p.setInt(1,id); p.setInt(2,trainerId); int changed=p.executeUpdate(); loadSessions();
            status.setText(changed==1?"Sesiunea a fost anulată. Verifică separat înscrierile și plățile.":"Sesiunea nu mai poate fi anulată din această pagină.");
        } catch(SQLException ex) { error(ex); }
    }
    private static String money(BigDecimal value) {return value==null?"nestabilit":value.stripTrailingZeros().toPlainString();}
    private static String formatDates(TreeSet<LocalDate> dates) { return dates.stream().map(d->d.format(DATE)).collect(Collectors.joining(", ")); }
    private static String escape(String s) {return s==null?"Verifică valorile introduse.":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private JLabel label(String text,int size) { JLabel l=new JLabel(text); l.setForeground(INK); l.setFont(new Font("SansSerif",Font.PLAIN,size)); return l; }
    private JPanel card() {
        JPanel p=new JPanel(); p.setBackground(FIELD); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER,1,true),BorderFactory.createEmptyBorder(16,16,16,16))); return p;
    }
    private JButton button(String text,Runnable action,boolean primary) {
        JButton b=new JButton(text); b.setBackground(primary?INK:FIELD); b.setForeground(primary?Color.WHITE:INK);
        b.setFont(new Font("SansSerif",primary?Font.BOLD:Font.PLAIN,13)); b.setFocusPainted(false);
        b.setMargin(new Insets(10,14,10,14)); b.putClientProperty("JButton.arc",18);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addActionListener(e->action.run()); return b;
    }
    private JScrollPane scroll(JComponent component) {
        JScrollPane s=new JScrollPane(component); s.setBorder(null); s.getViewport().setBackground(CREAM); s.getVerticalScrollBar().setUnitIncrement(16); return s;
    }
    private void finishList() {sessionList.revalidate(); sessionList.repaint();}
    private void error(Exception ex) {ex.printStackTrace(); status.setText("Eroare: "+ex.getMessage());}
    private void sizeDialog(JDialog d,int width,int height) {
        Rectangle bounds=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        d.setSize(Math.min(width,bounds.width-30),Math.min(height,bounds.height-30));
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); d.setLocationRelativeTo(this);
    }
}
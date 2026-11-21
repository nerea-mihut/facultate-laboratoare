package salon.ui.trainer;

import salon.db.DatabaseConnection;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TrainerParticipantsPanel extends JPanel {
    private static final Color CREAM=new Color(244,240,233), INK=new Color(30,30,30),
            FIELD=new Color(250,249,247), MUTED=new Color(115,110,103);
    private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final long MAX_FILE=20L*1024*1024;
    private final String email;
    private Integer trainerId;
    private boolean filling;
    private final JComboBox<CourseChoice> courseBox=new JComboBox<>();
    private final JComboBox<SessionChoice> sessionBox=new JComboBox<>();
    private final DefaultTableModel model=readOnly("ID înscriere","Nume","Prenume","Telefon","E-mail","Stare","Opțiune plată","Preț înscriere (lei)","Documente");
    private final JTable table=new JTable(model);
    private final JLabel status=new JLabel(" ");
    private final JButton enrollButton, documentsButton, moveButton, withdrawButton;

    private record CourseChoice(int id,String name) {
        @Override public String toString(){return name;}
    }
    private record SessionChoice(int id,int courseId,String name,LocalDate date,String state,BigDecimal price,int capacity) {
        @Override public String toString(){return "#"+id+" · "+name+" · "+(date==null?"dată nestabilită":date.format(DATE))+" · "+state;}
    }
    private record ClientChoice(int id,String last,String first,String phone,String email) {
        @Override public String toString(){return last+" "+first+" · "+phone+" · "+email;}
    }
    private record NewClient(String last,String first,String phone,String email) {}
    private record DocumentChoice(int id,String type,LocalDate date) {}
    private record Enrollment(int id,int clientId,int sessionId,String state,String name) {}
    private interface Task<T> {T execute() throws Exception;}

    public TrainerParticipantsPanel(String email,Runnable showHome) {
        this.email=email;
        setLayout(new BorderLayout(16,16));setBackground(CREAM);
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel header=panel(new BorderLayout(12,0));
        header.add(button("Înapoi",showHome,false),BorderLayout.WEST);
        JLabel title=label("Cursanți și documente",24);title.setFont(title.getFont().deriveFont(Font.BOLD));
        header.add(title,BorderLayout.CENTER);
        enrollButton=button("Înscrie un cursant",this::openEnrollmentDialog,true);
        header.add(enrollButton,BorderLayout.EAST);add(header,BorderLayout.NORTH);
        JPanel center=panel(new BorderLayout(12,16));
        JPanel filters=panel(new GridLayout(1,2,16,0));
        filters.add(labeled("Curs",courseBox));filters.add(labeled("Sesiunea ta",sessionBox));
        center.add(filters,BorderLayout.NORTH);
        styleTable(table);center.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=panel(new BorderLayout(8,10));
        documentsButton=button("Documentele cursantului selectat",this::showDocuments,false);
        moveButton=button("Mută în altă sesiune",this::moveEnrollment,false);
        withdrawButton=button("Șterge din sesiune",this::withdrawEnrollment,false);
        JPanel controls=panel(new GridLayout(2,2,10,10));
        controls.add(documentsButton);controls.add(button("Reîncarcă",this::refreshData,false));
        controls.add(moveButton);controls.add(withdrawButton);
        bottom.add(controls,BorderLayout.NORTH);
        status.setForeground(MUTED);status.setFont(new Font("SansSerif",Font.PLAIN,12));
        bottom.add(status,BorderLayout.SOUTH);center.add(bottom,BorderLayout.SOUTH);
        add(center,BorderLayout.CENTER);
        courseBox.addActionListener(e->{if(!filling)loadSessions();});
        sessionBox.addActionListener(e->{if(!filling)loadParticipants();});
        table.getSelectionModel().addListSelectionListener(e->updateButtons());
        enrollButton.setEnabled(false);updateButtons();
    }
    public void refreshData() {
        CourseChoice old=(CourseChoice)courseBox.getSelectedItem();
        SessionChoice previous=(SessionChoice)sessionBox.getSelectedItem();
        filling=true;
        try(Connection c=DatabaseConnection.getConnection()) {
            trainerId=findTrainer(c);courseBox.removeAllItems();
            try(PreparedStatement p=c.prepareStatement("SELECT DISTINCT cu.id_curs,cu.denumire FROM curs cu JOIN sesiune_curs s ON s.id_curs=cu.id_curs WHERE s.id_trainer=? ORDER BY cu.id_curs")) {
                p.setInt(1,trainerId);try(ResultSet r=p.executeQuery()){while(r.next())courseBox.addItem(new CourseChoice(r.getInt(1),r.getString(2)));}
            }
            if(old!=null)for(int i=0;i<courseBox.getItemCount();i++)if(courseBox.getItemAt(i).id()==old.id())courseBox.setSelectedIndex(i);
            filling=false;loadSessions();
            if(previous!=null)for(int i=0;i<sessionBox.getItemCount();i++)if(sessionBox.getItemAt(i).id()==previous.id())sessionBox.setSelectedIndex(i);
        }catch(SQLException ex){filling=false;error(ex);}
    }
    private int findTrainer(Connection c)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT u.id_angajat FROM utilizator u JOIN trainer t ON t.id_angajat=u.id_angajat WHERE u.email=?")) {
            p.setString(1,email);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Contul nu este asociat unui trainer.");return r.getInt(1);}
        }
    }
    private void loadSessions() {
        filling=true;sessionBox.removeAllItems();CourseChoice course=(CourseChoice)courseBox.getSelectedItem();
        if(course!=null&&trainerId!=null) {
            String sql=sessionSelect()+" WHERE s.id_trainer=? AND s.id_curs=? ORDER BY s.data_sesiune DESC,s.id_sesiune DESC";
            try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
                p.setInt(1,trainerId);p.setInt(2,course.id());try(ResultSet r=p.executeQuery()){while(r.next())sessionBox.addItem(session(r));}
            }catch(SQLException ex){error(ex);}
        }
        filling=false;loadParticipants();
    }
    private static String sessionSelect(){return "SELECT s.id_sesiune,s.id_curs,cu.denumire,s.data_sesiune,s.stare,s.pret,s.nr_maxim_cursanti FROM sesiune_curs s JOIN curs cu ON cu.id_curs=s.id_curs";}
    private SessionChoice session(ResultSet r)throws SQLException {
        java.sql.Date date=r.getDate("data_sesiune");
        return new SessionChoice(r.getInt("id_sesiune"),r.getInt("id_curs"),r.getString("denumire"),date==null?null:date.toLocalDate(),r.getString("stare"),r.getBigDecimal("pret"),r.getInt("nr_maxim_cursanti"));
    }
    private SessionChoice session(Connection c,int id,int trainer,boolean lock)throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sessionSelect()+" WHERE s.id_sesiune=? AND s.id_trainer=?"+(lock?" FOR UPDATE":""))) {
            p.setInt(1,id);p.setInt(2,trainer);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Sesiunea nu există sau nu îți aparține.");return session(r);}
        }
    }
    private void loadParticipants() {
        model.setRowCount(0);updateButtons();enrollButton.setEnabled(trainerId!=null);
        SessionChoice selected=(SessionChoice)sessionBox.getSelectedItem();
        if(selected==null||trainerId==null){status.setText("Nu există sesiuni. Propune mai întâi un curs din «Cursurile mele».");return;}
        String sql="SELECT i.id_inscriere,c.nume,c.prenume,c.telefon,c.email,i.stare,i.metoda_plata,i.pret_aplicat,(SELECT COUNT(*) FROM document_inscriere d WHERE d.id_inscriere=i.id_inscriere) AS documente FROM inscriere i JOIN client c ON c.id_client=i.id_client JOIN sesiune_curs s ON s.id_sesiune=i.id_sesiune WHERE s.id_trainer=? AND s.id_sesiune=? ORDER BY c.nume,c.prenume";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,trainerId);p.setInt(2,selected.id());int active=0;
            try(ResultSet r=p.executeQuery()){while(r.next()){
                String state=r.getString("stare");if(!"Retrasa".equals(state)&&!"Inlocuita".equals(state))active++;
                String pay=r.getString("metoda_plata");
                model.addRow(new Object[]{r.getInt("id_inscriere"),r.getString("nume"),r.getString("prenume"),r.getString("telefon"),r.getString("email"),state,pay==null?"Neselectată":pay,r.getBigDecimal("pret_aplicat"),r.getInt("documente")});
            }}
            status.setText(active+" / "+selected.capacity()+" locuri ocupate. Lista include istoricul. Opțiunea de plată nu confirmă încasarea.");
        }catch(SQLException ex){error(ex);}
    }
    private void updateButtons() {
        int view=table.getSelectedRow();boolean selected=view>=0;
        documentsButton.setEnabled(selected);
        boolean active=false;
        if(selected){String state=String.valueOf(model.getValueAt(table.convertRowIndexToModel(view),5));active=editable(state);}
        SessionChoice session=(SessionChoice)sessionBox.getSelectedItem();
        boolean planned=session!=null&&"Planificata".equals(session.state());
        moveButton.setEnabled(active&&planned);withdrawButton.setEnabled(active&&planned);
    }
    private Integer selectedEnrollmentId() {
        int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Selectează mai întâi un cursant din tabel.");return null;}
        return ((Number)model.getValueAt(table.convertRowIndexToModel(row),0)).intValue();
    }
    private Enrollment enrollment(Connection c,int id,int trainer,boolean lock)throws SQLException {
        String sql="SELECT i.id_inscriere,i.id_client,i.id_sesiune,i.stare,CONCAT(cl.nume,' ',cl.prenume) AS nume FROM inscriere i JOIN sesiune_curs s ON s.id_sesiune=i.id_sesiune JOIN client cl ON cl.id_client=i.id_client WHERE i.id_inscriere=? AND s.id_trainer=?"+(lock?" FOR UPDATE":"");
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setInt(2,trainer);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Înscrierea nu există sau nu aparține sesiunilor tale.");return new Enrollment(r.getInt(1),r.getInt(2),r.getInt(3),r.getString(4),r.getString(5));}
        }
    }
    private static boolean editable(String state){return "In_asteptare".equals(state)||"Confirmata".equals(state);}
    private void withdrawEnrollment() {
        Integer id=selectedEnrollmentId();if(id==null)return;
        try(Connection c=DatabaseConnection.getConnection()) {
            Enrollment item=enrollment(c,id,findTrainer(c),false);
            if(!editable(item.state()))throw new SQLException("Înscrierea nu poate fi retrasă din această pagină.");
            String text="Elimini din sesiune cursantul "+item.name()+"?\nÎnscrierea devine Retrasa și locul se eliberează.\nPersoana, documentele și plățile se păstrează. Nu se face rambursare automată.";
            if(!confirm(this,text,"Ștergere din sesiune"))return;
            runTask(()->{
                try(Connection db=DatabaseConnection.getConnection()) {
                    db.setAutoCommit(false);
                    try {
                        int trainer=findTrainer(db);Enrollment snapshot=enrollment(db,id,trainer,false);
                        SessionChoice source=session(db,snapshot.sessionId(),trainer,true);
                        Enrollment actual=enrollment(db,id,trainer,true);
                        if(actual.sessionId()!=source.id()||!editable(actual.state())||!"Planificata".equals(source.state()))throw new SQLException("Înscrierea sau sesiunea s-a schimbat. Reîncarcă lista.");
                        try(PreparedStatement p=db.prepareStatement("UPDATE inscriere SET stare='Retrasa',data_anuntarii_retragerii=CURDATE() WHERE id_inscriere=?")){p.setInt(1,id);p.executeUpdate();}
                        db.commit();return null;
                    }catch(Exception ex){rollback(db,ex);throw ex;}
                }
            },"Cursantul a fost retras. Rândul rămâne în istoric cu starea Retrasa.");
        }catch(Exception ex){showError(this,ex);}
    }
    private void moveEnrollment() {
        Integer id=selectedEnrollmentId();if(id==null)return;
        try(Connection c=DatabaseConnection.getConnection()) {
            int trainer=findTrainer(c);Enrollment item=enrollment(c,id,trainer,false);
            SessionChoice source=session(c,item.sessionId(),trainer,false);
            if(!editable(item.state())||!"Planificata".equals(source.state()))throw new SQLException("Se pot muta doar înscrieri In_asteptare/Confirmata din sesiuni planificate.");
            List<SessionChoice> list=new ArrayList<>();
            String sql=sessionSelect()+" WHERE s.id_trainer=? AND s.id_curs=? AND s.id_sesiune<>? AND s.stare='Planificata' AND s.data_sesiune>=CURDATE() ORDER BY s.data_sesiune,s.id_sesiune";
            try(PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,trainer);p.setInt(2,source.courseId());p.setInt(3,source.id());try(ResultSet r=p.executeQuery()){while(r.next())list.add(session(r));}}
            if(list.isEmpty())throw new SQLException("Nu există altă sesiune viitoare a aceluiași curs la acest trainer. Creează mai întâi o sesiune din Cursurile mele.");
            Object chosen=JOptionPane.showInputDialog(this,"Alege noua sesiune pentru "+item.name()+":","Mută în altă sesiune",JOptionPane.QUESTION_MESSAGE,null,list.toArray(),list.get(0));
            if(!(chosen instanceof SessionChoice target))return;
            if(!confirm(this,"Muți cursantul în "+target+"?\nPrețul aplicat inițial, starea înscrierii, documentele și plățile se păstrează.\nPrețul destinației nu se aplică automat.","Confirmare mutare"))return;
            runTask(()->{moveNow(id,source.id(),target.id());return null;},"Cursantul a fost mutat. Alege sesiunea destinație în filtru pentru a-l vedea.");
        }catch(Exception ex){showError(this,ex);}
    }
    private void moveNow(int id,int sourceId,int targetId)throws Exception {
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int trainer=findTrainer(c);
                SessionChoice first=session(c,Math.min(sourceId,targetId),trainer,true);
                SessionChoice second=session(c,Math.max(sourceId,targetId),trainer,true);
                SessionChoice source=first.id()==sourceId?first:second,target=first.id()==targetId?first:second;
                Enrollment actual=enrollment(c,id,trainer,true);
                if(actual.sessionId()!=sourceId||!editable(actual.state()))throw new SQLException("Înscrierea s-a schimbat între timp. Reîncarcă lista.");
                if(source.courseId()!=target.courseId()||!"Planificata".equals(source.state())||!"Planificata".equals(target.state())||target.date()==null||target.date().isBefore(LocalDate.now()))throw new SQLException("Sesiunile nu mai permit mutarea.");
                ensureNoDuplicate(c,actual.clientId(),targetId);
                if(occupied(c,targetId)>=target.capacity())throw new SQLException("Sesiunea destinație nu mai are locuri disponibile.");
                try(PreparedStatement p=c.prepareStatement("UPDATE inscriere SET id_sesiune=?,data_cursului=? WHERE id_inscriere=?")){p.setInt(1,targetId);p.setDate(2,java.sql.Date.valueOf(target.date()));p.setInt(3,id);p.executeUpdate();}
                removeWaiting(c,actual.clientId(),targetId);c.commit();
            }catch(Exception ex){rollback(c,ex);throw ex;}
        }
    }
    private void showDocuments() {
        Integer id=selectedEnrollmentId();if(id==null)return;
        List<DocumentChoice> docs=new ArrayList<>();DefaultTableModel dm=readOnly("Tip document","Data depunerii");
        String sql="SELECT d.id_document,d.tip_document,d.data_depunerii FROM document_inscriere d JOIN inscriere i ON i.id_inscriere=d.id_inscriere JOIN sesiune_curs s ON s.id_sesiune=i.id_sesiune WHERE d.id_inscriere=? AND s.id_trainer=? ORDER BY d.id_document";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,id);p.setInt(2,findTrainer(c));try(ResultSet r=p.executeQuery()){while(r.next()){
                DocumentChoice doc=new DocumentChoice(r.getInt(1),r.getString(2),r.getDate(3).toLocalDate());docs.add(doc);dm.addRow(new Object[]{doc.type(),doc.date().format(DATE)});
            }}
        }catch(SQLException ex){showError(this,ex);return;}
        if(docs.isEmpty()){JOptionPane.showMessageDialog(this,"Nu există documente atașate acestei înscrieri.");return;}
        JDialog d=dialog("Documentele înscrierii #"+id);JTable dt=new JTable(dm);styleTable(dt);
        JPanel root=panel(new BorderLayout(12,12));root.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));root.add(new JScrollPane(dt),BorderLayout.CENTER);
        root.add(button("Deschide documentul selectat",()->{
            int row=dt.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(d,"Selectează un document.");return;}
            openDocument(docs.get(dt.convertRowIndexToModel(row)).id(),d);
        },true),BorderLayout.SOUTH);
        d.setContentPane(root);size(d,720,440);d.setVisible(true);
    }
    private void openDocument(int id,Component parent) {
        try {
            String ref=null;
            try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT d.referinta_fisier FROM document_inscriere d JOIN inscriere i ON i.id_inscriere=d.id_inscriere JOIN sesiune_curs s ON s.id_sesiune=i.id_sesiune WHERE d.id_document=? AND s.id_trainer=?")) {
                p.setInt(1,id);p.setInt(2,findTrainer(c));try(ResultSet r=p.executeQuery()){if(r.next())ref=r.getString(1);}
            }
            if(ref==null)throw new IOException("Documentul nu mai este disponibil pentru acest trainer.");
            Path file=Path.of(ref).toAbsolutePath().normalize(),root=Path.of("documents").toAbsolutePath().normalize();
            if(!Files.isRegularFile(file))throw new IOException("Fișierul nu există local. Verifică folderul documents și Working directory din IntelliJ.");
            if(!file.toRealPath().startsWith(root.toRealPath()))throw new IOException("Pot fi deschise doar documente din folderul documents al proiectului.");
            extension(file);
            if(!Desktop.isDesktopSupported()||!Desktop.getDesktop().isSupported(Desktop.Action.OPEN))throw new IOException("Deschiderea fișierelor nu este disponibilă pe acest sistem.");
            if(!confirm(parent,"Deschizi documentul în aplicația implicită? Deschide doar fișiere din surse de încredere.","Deschidere document"))return;
            new SwingWorker<Void,Void>() {
                @Override protected Void doInBackground()throws Exception{Desktop.getDesktop().open(file.toFile());return null;}
                @Override protected void done(){try{get();}catch(Exception ex){showError(parent,ex);}}
            }.execute();
        }catch(Exception ex){showError(parent,ex);}
    }
    public void openEnrollmentDialog() {
        List<SessionChoice> sessions=new ArrayList<>();
        String sql=sessionSelect()+" WHERE s.id_trainer=? AND s.stare='Planificata' AND s.data_sesiune>=CURDATE() ORDER BY s.data_sesiune,s.id_sesiune";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            trainerId=findTrainer(c);p.setInt(1,trainerId);try(ResultSet r=p.executeQuery()){while(r.next())sessions.add(session(r));}
        }catch(SQLException ex){showError(this,ex);return;}
        if(sessions.isEmpty()){JOptionPane.showMessageDialog(this,"Nu ai sesiuni planificate cu data de început azi sau în viitor.");return;}
        JDialog d=dialog("Înscrie un cursant");JPanel root=panel(new BorderLayout(14,14));root.setBorder(BorderFactory.createEmptyBorder(22,26,22,26));
        JComboBox<SessionChoice> choices=new JComboBox<>(sessions.toArray(new SessionChoice[0]));
        SessionChoice selected=(SessionChoice)sessionBox.getSelectedItem();if(selected!=null)for(SessionChoice item:sessions)if(item.id()==selected.id())choices.setSelectedItem(item);
        JPanel top=panel(new BorderLayout(0,8));top.add(labeled("Sesiunea la care înscrii persoana",choices),BorderLayout.CENTER);
        JLabel price=label(" ",13);Runnable updatePrice=()->{SessionChoice item=(SessionChoice)choices.getSelectedItem();price.setText(item==null?" ":"Preț sesiune: "+item.price().toPlainString()+" lei. Plata nu se înregistrează automat.");};
        choices.addActionListener(e->updatePrice.run());updatePrice.run();top.add(price,BorderLayout.SOUTH);root.add(top,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();
        JPanel existing=panel(new BorderLayout(10,12));existing.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        JTextField search=new JTextField(28);JComboBox<ClientChoice> clients=new JComboBox<>();
        JPanel searchBar=panel(new BorderLayout(8,0));searchBar.add(search,BorderLayout.CENTER);
        searchBar.add(button("Caută",()->searchClients(search.getText(),clients,d),false),BorderLayout.EAST);
        search.addActionListener(e->searchClients(search.getText(),clients,d));
        existing.add(labeled("Caută după nume, telefon sau e-mail (minimum 2 caractere)",searchBar),BorderLayout.NORTH);
        existing.add(labeled("Alege persoana din rezultate",clients),BorderLayout.CENTER);tabs.addTab("Persoană existentă",existing);
        JPanel fresh=panel(new GridBagLayout());fresh.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        JTextField last=new JTextField(24),first=new JTextField(24),phone=new JTextField(24),mail=new JTextField(24);
        addField(fresh,0,"Nume",last);addField(fresh,1,"Prenume",first);addField(fresh,2,"Telefon",phone);addField(fresh,3,"E-mail",mail);
        tabs.addTab("Persoană nouă",new JScrollPane(fresh));root.add(tabs,BorderLayout.CENTER);
        JPanel bottom=panel(new BorderLayout(8,12));JComboBox<String> payment=new JComboBox<>(new String[]{"Selectează opțiunea de plată","Avans","Integral"});
        JLabel chosenDoc=label("Niciun document selectat (opțional).",12);Path[] doc={null};
        JPanel docRow=panel(new FlowLayout(FlowLayout.LEFT,8,0));
        docRow.add(button("Atașează document",()->{
            JFileChooser chooser=new JFileChooser();chooser.setAcceptAllFileFilterUsed(false);chooser.setFileFilter(new FileNameExtensionFilter("PDF și imagini (maximum 20 MB)","pdf","jpg","jpeg","png"));
            if(chooser.showOpenDialog(d)==JFileChooser.APPROVE_OPTION){try{Path path=chooser.getSelectedFile().toPath();validateFile(path);doc[0]=path;chosenDoc.setText(path.getFileName().toString());}catch(Exception ex){showError(d,ex);}}
        },false));
        docRow.add(chosenDoc);docRow.add(button("Elimină selecția",()->{doc[0]=null;chosenDoc.setText("Niciun document selectat (opțional).");},false));
        JPanel options=panel(new GridLayout(0,1,0,8));options.add(labeled("Opțiune de plată (nu confirmă încasarea)",payment));options.add(docRow);
        options.add(label("Stare inițială: In_asteptare. Pentru persoana nouă nu se creează automat cont sau parolă.",12));
        JLabel info=label(" ",12);options.add(info);bottom.add(options,BorderLayout.CENTER);
        JButton save=button("Salvează înscrierea",()->{},true);
        for(var listener:save.getActionListeners())save.removeActionListener(listener);
        save.addActionListener(e->{
            try {
                SessionChoice target=(SessionChoice)choices.getSelectedItem();if(target==null)throw new IllegalArgumentException("Selectează sesiunea.");
                if(payment.getSelectedIndex()==0)throw new IllegalArgumentException("Selectează opțiunea de plată.");
                Integer clientId=null;NewClient person=null;
                if(tabs.getSelectedIndex()==0){ClientChoice item=(ClientChoice)clients.getSelectedItem();if(item==null)throw new IllegalArgumentException("Caută și selectează persoana existentă.");clientId=item.id();}
                else {
                    String l=required(last.getText(),50,"Nume"),f=required(first.getText(),50,"Prenume"),ph=required(phone.getText(),50,"Telefon"),em=required(mail.getText(),50,"E-mail");
                    if(!ph.matches("[+0-9 ()-]{6,50}"))throw new IllegalArgumentException("Verifică numărul de telefon.");
                    if(!em.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("Verifică adresa de e-mail.");
                    person=new NewClient(l,f,ph,em);
                }
                if(doc[0]!=null)validateFile(doc[0]);
                if(!confirm(d,"Salvezi înscrierea la "+target+"? Nu se înregistrează o încasare.","Confirmare înscriere"))return;
                Integer selectedClient=clientId;NewClient data=person;Path source=doc[0];String pay=(String)payment.getSelectedItem();
                setEnabledTree(root,false);d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);info.setText("Se salvează înscrierea...");
                new SwingWorker<Integer,Void>() {
                    @Override protected Integer doInBackground()throws Exception{return enroll(target.id(),selectedClient,data,pay,source);}
                    @Override protected void done(){
                        try{int id=get();d.dispose();refreshData();status.setText("Înscrierea #"+id+" a fost salvată In_asteptare. Nicio plată nu a fost înregistrată.");}
                        catch(Exception ex){setEnabledTree(root,true);d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);info.setText("<html>"+escape(message(ex))+"</html>");ex.printStackTrace();}
                    }
                }.execute();
            }catch(Exception ex){info.setText("<html>"+escape(message(ex))+"</html>");}
        });
        bottom.add(save,BorderLayout.SOUTH);root.add(bottom,BorderLayout.SOUTH);d.setContentPane(root);size(d,1000,720);d.setVisible(true);
    }
    private void searchClients(String term,JComboBox<ClientChoice> results,Component parent) {
        term=term.trim();results.removeAllItems();if(term.length()<2){JOptionPane.showMessageDialog(parent,"Introdu cel puțin două caractere.");return;}
        String pattern="%"+term.replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        String sql="SELECT id_client,nume,prenume,telefon,email FROM client WHERE CONCAT(nume,' ',prenume) LIKE ? ESCAPE '!' OR CONCAT(prenume,' ',nume) LIKE ? ESCAPE '!' OR telefon LIKE ? ESCAPE '!' OR email LIKE ? ESCAPE '!' ORDER BY nume,prenume LIMIT 100";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            findTrainer(c);for(int i=1;i<=4;i++)p.setString(i,pattern);
            try(ResultSet r=p.executeQuery()){while(r.next())results.addItem(new ClientChoice(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)));}
            results.setSelectedIndex(-1);if(results.getItemCount()==0)JOptionPane.showMessageDialog(parent,"Nu am găsit persoane. Poți folosi fila Persoană nouă.");
        }catch(SQLException ex){showError(parent,ex);}
    }
    private int enroll(int sessionId,Integer existingId,NewClient person,String payment,Path source)throws Exception {
        Path copied=null;boolean committed=false;
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int trainer=findTrainer(c);SessionChoice target=session(c,sessionId,trainer,true);
                if(!"Planificata".equals(target.state())||target.date()==null||target.date().isBefore(LocalDate.now()))throw new SQLException("Sesiunea nu mai permite înscrieri.");
                if(occupied(c,sessionId)>=target.capacity())throw new SQLException("Sesiunea este complet ocupată.");
                int clientId;
                if(existingId!=null){
                    clientId=existingId;try(PreparedStatement p=c.prepareStatement("SELECT id_client FROM client WHERE id_client=? FOR UPDATE")){p.setInt(1,clientId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Persoana nu mai există.");}}
                }else{
                    if(person==null)throw new SQLException("Lipsesc datele persoanei.");
                    try(PreparedStatement p=c.prepareStatement("SELECT IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='client' AND COLUMN_NAME='alergii'" );ResultSet r=p.executeQuery()){
                        if(!r.next()||!"YES".equals(r.getString(1)))throw new SQLException("Rulează întâi scriptul SQL atașat: client.alergii trebuie să permită NULL pentru cursanții noi, fără răspuns medical inventat.");
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO client (nume,prenume,telefon,email,alergii,detalii_alergii) VALUES (?,?,?,?,NULL,NULL)",Statement.RETURN_GENERATED_KEYS)){
                        p.setString(1,person.last());p.setString(2,person.first());p.setString(3,person.phone());p.setString(4,person.email());p.executeUpdate();
                        try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Nu s-a obținut ID-ul persoanei.");clientId=r.getInt(1);}
                    }
                }
                ensureNoDuplicate(c,clientId,sessionId);int enrollmentId;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO inscriere (id_client,id_sesiune,data_cursului,data_inscrierii,stare,pret_aplicat,metoda_plata) VALUES (?,?,?,CURDATE(),'In_asteptare',?,?)",Statement.RETURN_GENERATED_KEYS)){
                    p.setInt(1,clientId);p.setInt(2,sessionId);p.setDate(3,java.sql.Date.valueOf(target.date()));p.setBigDecimal(4,target.price());p.setString(5,payment);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Nu s-a obținut ID-ul înscrierii.");enrollmentId=r.getInt(1);}
                }
                if(source!=null){
                    validateFile(source);Path folder=Path.of("documents");Files.createDirectories(folder);copied=folder.resolve(UUID.randomUUID()+"."+extension(source));Files.copy(source,copied);
                    if(Files.size(copied)>MAX_FILE)throw new IOException("Documentul depășește 20 MB.");
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO document_inscriere (id_inscriere,tip_document,data_depunerii,referinta_fisier) VALUES (?,'Document înscriere',CURDATE(),?)")){p.setInt(1,enrollmentId);p.setString(2,copied.toString());p.executeUpdate();}
                }
                removeWaiting(c,clientId,sessionId);c.commit();committed=true;return enrollmentId;
            }catch(Exception ex){rollback(c,ex);throw ex;}
        }finally{if(!committed&&copied!=null)try{Files.deleteIfExists(copied);}catch(IOException ex){ex.printStackTrace();}}
    }
    private void ensureNoDuplicate(Connection c,int clientId,int sessionId)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT stare FROM inscriere WHERE id_client=? AND id_sesiune=? FOR UPDATE")){
            p.setInt(1,clientId);p.setInt(2,sessionId);try(ResultSet r=p.executeQuery()){if(r.next())throw new SQLException("Persoana are deja o înscriere la această sesiune ("+r.getString(1)+"), inclusiv în istoric. Nu se creează un duplicat.");}
        }
    }
    private int occupied(Connection c,int sessionId)throws SQLException {
        int count=0;try(PreparedStatement p=c.prepareStatement("SELECT id_inscriere FROM inscriere WHERE id_sesiune=? AND stare NOT IN ('Retrasa','Inlocuita') FOR UPDATE")){p.setInt(1,sessionId);try(ResultSet r=p.executeQuery()){while(r.next())count++;}}return count;
    }
    private void removeWaiting(Connection c,int clientId,int sessionId)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("DELETE FROM lista_asteptare WHERE id_client=? AND id_sesiune=?")){p.setInt(1,clientId);p.setInt(2,sessionId);p.executeUpdate();}
    }
    private <T> void runTask(Task<T> task,String success) {
        JDialog progress=dialog("Se salvează");progress.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JLabel text=label("Se actualizează înscrierea. Așteaptă finalizarea...",14);text.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));progress.add(text);progress.pack();progress.setLocationRelativeTo(this);
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground()throws Exception{return task.execute();}
            @Override protected void done(){progress.dispose();try{get();refreshData();JOptionPane.showMessageDialog(TrainerParticipantsPanel.this,success);}catch(Exception ex){showError(TrainerParticipantsPanel.this,ex);}}
        }.execute();progress.setVisible(true);
    }
    private static void rollback(Connection c,Exception original){try{c.rollback();}catch(SQLException ex){original.addSuppressed(ex);}}
    private static String required(String value,int max,String name){value=value.trim();if(value.isEmpty()||value.length()>max)throw new IllegalArgumentException(name+": completează maximum "+max+" caractere.");return value;}
    private static String extension(Path file)throws IOException{
        String name=file.getFileName().toString();int dot=name.lastIndexOf('.');String ext=dot<0?"":name.substring(dot+1).toLowerCase(Locale.ROOT);
        if(!List.of("pdf","jpg","jpeg","png").contains(ext))throw new IOException("Sunt acceptate doar PDF, JPG, JPEG și PNG.");return ext;
    }
    private static void validateFile(Path file)throws IOException{extension(file);if(!Files.isRegularFile(file)||!Files.isReadable(file))throw new IOException("Fișierul nu poate fi citit.");if(Files.size(file)==0||Files.size(file)>MAX_FILE)throw new IOException("Documentul trebuie să aibă între 1 octet și 20 MB.");}
    private static String message(Throwable ex){while(ex.getCause()!=null)ex=ex.getCause();if(ex instanceof SQLException sql&&sql.getErrorCode()==1062)return "Există deja o persoană cu acest e-mail/telefon sau o înscriere la această sesiune. Folosește persoana existentă; nu crea un duplicat.";return ex.getMessage()==null?"Operația nu a putut fi finalizată.":ex.getMessage();}
    private static String escape(String text){return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private static DefaultTableModel readOnly(String... columns){return new DefaultTableModel(columns,0){@Override public boolean isCellEditable(int row,int col){return false;}};}
    private static JPanel panel(LayoutManager layout){JPanel p=new JPanel(layout);p.setOpaque(false);return p;}
    private static JLabel label(String text,int size){JLabel l=new JLabel(text);l.setForeground(INK);l.setFont(new Font("SansSerif",Font.PLAIN,size));return l;}
    private static JPanel labeled(String text,JComponent field){JPanel p=panel(new BorderLayout(0,7));p.add(label(text,13),BorderLayout.NORTH);p.add(field,BorderLayout.CENTER);return p;}
    private static void addField(JPanel p,int row,String text,JComponent field){GridBagConstraints c=new GridBagConstraints();c.gridy=row;c.gridx=0;c.anchor=GridBagConstraints.WEST;c.insets=new Insets(8,0,8,12);p.add(label(text,13),c);c.gridx=1;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;p.add(field,c);}
    private static JButton button(String text,Runnable action,boolean primary){JButton b=new JButton(text);b.setBackground(primary?INK:FIELD);b.setForeground(primary?Color.WHITE:INK);b.setFont(new Font("SansSerif",primary?Font.BOLD:Font.PLAIN,13));b.setFocusPainted(false);b.setMargin(new Insets(10,14,10,14));b.putClientProperty("JButton.arc",18);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.addActionListener(e->action.run());return b;}
    private static void styleTable(JTable t){t.setRowHeight(32);t.setFont(new Font("SansSerif",Font.PLAIN,13));t.setBackground(Color.WHITE);t.setForeground(INK);t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setAutoCreateRowSorter(true);t.setFillsViewportHeight(true);}
    private JDialog dialog(String title){JDialog d=new JDialog(SwingUtilities.getWindowAncestor(this),title,Dialog.ModalityType.APPLICATION_MODAL);d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);d.getContentPane().setBackground(CREAM);return d;}
    private void size(JDialog d,int width,int height){Rectangle bounds=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();d.setSize(Math.min(width,bounds.width-30),Math.min(height,bounds.height-30));d.setLocationRelativeTo(this);}
    private static void setEnabledTree(Component component,boolean enabled){component.setEnabled(enabled);if(component instanceof Container c)for(Component child:c.getComponents())setEnabledTree(child,enabled);}
    private static boolean confirm(Component parent,String text,String title){return JOptionPane.showConfirmDialog(parent,text,title,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION;}
    private void error(Exception ex){ex.printStackTrace();status.setText("Eroare: "+message(ex));}
    private static void showError(Component parent,Throwable ex){ex.printStackTrace();JOptionPane.showMessageDialog(parent,message(ex),"Operație nefinalizată",JOptionPane.ERROR_MESSAGE);}
}
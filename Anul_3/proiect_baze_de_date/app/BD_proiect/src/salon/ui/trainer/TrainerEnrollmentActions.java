package salon.ui.trainer;

import salon.db.DatabaseConnection;
import javax.swing.*;
import java.awt.Component;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class TrainerEnrollmentActions {
    private TrainerEnrollmentActions() {}
    private record Enrollment(int id,int clientId,int sessionId,String state,String name) {}
    private record Session(int id,int courseId,String name,LocalDate date,String state,int capacity) {
        public String toString(){return "#"+id+" · "+name+" · "+(date==null?"dată nestabilită":date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));}
    }
    public static void withdraw(Component parent,String email,JTable table,Runnable refresh) {
        Integer id=selectedId(parent,table);if(id==null)return;
        try(Connection c=DatabaseConnection.getConnection()) {
            int trainer=trainer(c,email);Enrollment enrollment=enrollment(c,id,trainer,false);
            if(!editable(enrollment.state()))throw new SQLException("Pot fi retrase de aici doar înscrieri In_asteptare sau Confirmata.");
            String text="Elimini din sesiune cursantul "+enrollment.name()+"?\nÎnscrierea va deveni Retrasa și nu va mai ocupa loc.\nPersoana, documentele și plățile rămân în evidență. Nu se face rambursare automată.";
            if(JOptionPane.showConfirmDialog(parent,text,"Eliminare din sesiune",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            run(parent,()->withdrawNow(email,id),refresh,"Cursantul a fost retras din sesiune. Istoricul a fost păstrat.");
        }catch(Exception ex){showError(parent,ex);}
    }
    public static void move(Component parent,String email,JTable table,Runnable refresh) {
        Integer id=selectedId(parent,table);if(id==null)return;
        try(Connection c=DatabaseConnection.getConnection()) {
            int trainer=trainer(c,email);Enrollment enrollment=enrollment(c,id,trainer,false);
            if(!editable(enrollment.state()))throw new SQLException("Pot fi mutate doar înscrieri In_asteptare sau Confirmata.");
            Session source=session(c,enrollment.sessionId(),trainer,false);
            if(!"Planificata".equals(source.state()))throw new SQLException("Sesiunea de plecare trebuie să fie planificată.");
            List<Session> choices=new ArrayList<>();
            String sql="SELECT s.id_sesiune,s.id_curs,cu.denumire,s.data_sesiune,s.stare,s.nr_maxim_cursanti FROM sesiune_curs s JOIN curs cu ON cu.id_curs=s.id_curs WHERE s.id_trainer=? AND s.id_curs=? AND s.id_sesiune<>? AND s.stare='Planificata' AND s.data_sesiune>=CURDATE() ORDER BY s.data_sesiune,s.id_sesiune";
            try(PreparedStatement p=c.prepareStatement(sql)){
                p.setInt(1,trainer);p.setInt(2,source.courseId());p.setInt(3,source.id());try(ResultSet r=p.executeQuery()){while(r.next())choices.add(session(r));}
            }
            if(choices.isEmpty())throw new SQLException("Nu există altă sesiune planificată, azi sau în viitor, a aceluiași curs la acest trainer. Creează mai întâi sesiunea din Cursurile mele.");
            Object selected=JOptionPane.showInputDialog(parent,"Alege sesiunea pentru "+enrollment.name()+":","Mută în altă sesiune",JOptionPane.QUESTION_MESSAGE,null,choices.toArray(),choices.get(0));
            if(!(selected instanceof Session target))return;
            String text="Muți cursantul "+enrollment.name()+" în "+target+"?\nPrețul aplicat inițial, starea înscrierii, documentele și plățile se păstrează.\nPrețul sesiunii destinație nu se aplică automat.";
            if(JOptionPane.showConfirmDialog(parent,text,"Confirmare mutare",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            run(parent,()->moveNow(email,id,source.id(),target.id()),refresh,"Cursantul a fost mutat. Alege sesiunea destinație în filtru pentru a-l vedea.");
        }catch(Exception ex){showError(parent,ex);}
    }
    private static void withdrawNow(String email,int id)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                int trainer=trainer(c,email);Enrollment snapshot=enrollment(c,id,trainer,false);
                Session source=session(c,snapshot.sessionId(),trainer,true);
                Enrollment actual=enrollment(c,id,trainer,true);
                if(actual.sessionId()!=source.id())throw new SQLException("Înscrierea a fost mutată între timp. Reîncarcă lista.");
                if(!editable(actual.state())||!"Planificata".equals(source.state()))throw new SQLException("Înscrierea nu mai poate fi retrasă din această pagină.");
                try(PreparedStatement p=c.prepareStatement("UPDATE inscriere SET stare='Retrasa',data_anuntarii_retragerii=CURDATE() WHERE id_inscriere=?")){p.setInt(1,id);p.executeUpdate();}
                c.commit();
            }catch(SQLException|RuntimeException ex){rollback(c,ex);throw ex;}
        }
    }
    private static void moveNow(String email,int id,int expectedSource,int targetId)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                int trainer=trainer(c,email);
                Session first=session(c,Math.min(expectedSource,targetId),trainer,true);
                Session second=session(c,Math.max(expectedSource,targetId),trainer,true);
                Session source=first.id()==expectedSource?first:second;
                Session target=first.id()==targetId?first:second;
                Enrollment actual=enrollment(c,id,trainer,true);
                if(actual.sessionId()!=expectedSource)throw new SQLException("Înscrierea a fost mutată între timp. Reîncarcă lista.");
                if(!editable(actual.state()))throw new SQLException("Starea înscrierii nu mai permite mutarea.");
                if(source.courseId()!=target.courseId()||!"Planificata".equals(source.state())||!"Planificata".equals(target.state())||target.date()==null||target.date().isBefore(LocalDate.now()))throw new SQLException("Sesiunile nu mai permit această mutare.");
                try(PreparedStatement p=c.prepareStatement("SELECT id_inscriere FROM inscriere WHERE id_client=? AND id_sesiune=? FOR UPDATE")){
                    p.setInt(1,actual.clientId());p.setInt(2,targetId);try(ResultSet r=p.executeQuery()){if(r.next())throw new SQLException("Cursantul are deja o înscriere la destinație, inclusiv în istoric. Mutarea nu a fost efectuată.");}
                }
                int occupied=0;
                try(PreparedStatement p=c.prepareStatement("SELECT id_inscriere FROM inscriere WHERE id_sesiune=? AND stare NOT IN ('Retrasa','Inlocuita') FOR UPDATE")){
                    p.setInt(1,targetId);try(ResultSet r=p.executeQuery()){while(r.next())occupied++;}
                }
                if(occupied>=target.capacity())throw new SQLException("Sesiunea destinație este complet ocupată.");
                try(PreparedStatement p=c.prepareStatement("UPDATE inscriere SET id_sesiune=?,data_cursului=? WHERE id_inscriere=?")){
                    p.setInt(1,targetId);p.setDate(2,java.sql.Date.valueOf(target.date()));p.setInt(3,id);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM lista_asteptare WHERE id_client=? AND id_sesiune=?")){
                    p.setInt(1,actual.clientId());p.setInt(2,targetId);p.executeUpdate();
                }
                c.commit();
            }catch(SQLException|RuntimeException ex){rollback(c,ex);throw ex;}
        }
    }
    private static int trainer(Connection c,String email)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT u.id_angajat FROM utilizator u JOIN trainer t ON t.id_angajat=u.id_angajat WHERE u.email=?")){
            p.setString(1,email);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Contul nu mai este asociat unui trainer.");return r.getInt(1);}
        }
    }
    private static Enrollment enrollment(Connection c,int id,int trainer,boolean lock)throws SQLException{
        String sql="SELECT i.id_inscriere,i.id_client,i.id_sesiune,i.stare,CONCAT(cl.nume,' ',cl.prenume) AS nume FROM inscriere i JOIN sesiune_curs s ON s.id_sesiune=i.id_sesiune JOIN client cl ON cl.id_client=i.id_client WHERE i.id_inscriere=? AND s.id_trainer=?"+(lock?" FOR UPDATE":"");
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setInt(2,trainer);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Înscrierea nu există sau nu aparține sesiunilor tale.");return new Enrollment(r.getInt(1),r.getInt(2),r.getInt(3),r.getString(4),r.getString(5));}
        }
    }
    private static Session session(Connection c,int id,int trainer,boolean lock)throws SQLException{
        String sql="SELECT s.id_sesiune,s.id_curs,cu.denumire,s.data_sesiune,s.stare,s.nr_maxim_cursanti FROM sesiune_curs s JOIN curs cu ON cu.id_curs=s.id_curs WHERE s.id_sesiune=? AND s.id_trainer=?"+(lock?" FOR UPDATE":"");
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setInt(2,trainer);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Sesiunea nu există sau nu îți aparține.");return session(r);}
        }
    }
    private static Session session(ResultSet r)throws SQLException{
        java.sql.Date day=r.getDate("data_sesiune");return new Session(r.getInt("id_sesiune"),r.getInt("id_curs"),r.getString("denumire"),day==null?null:day.toLocalDate(),r.getString("stare"),r.getInt("nr_maxim_cursanti"));
    }
    private static boolean editable(String state){return "In_asteptare".equals(state)||"Confirmata".equals(state);}
    private static Integer selectedId(Component parent,JTable table){
        int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(parent,"Selectează mai întâi un cursant din tabel.");return null;}
        return ((Number)table.getModel().getValueAt(table.convertRowIndexToModel(row),0)).intValue();
    }
    private interface Task{void execute()throws Exception;}
    private static void run(Component parent,Task task,Runnable refresh,String success){
        JDialog progress=new JDialog(SwingUtilities.getWindowAncestor(parent),"Se salvează",java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        progress.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JLabel label=new JLabel("Se actualizează înscrierea. Așteaptă finalizarea...");label.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));progress.add(label);progress.pack();progress.setLocationRelativeTo(parent);
        new SwingWorker<Void,Void>(){
            protected Void doInBackground()throws Exception{task.execute();return null;}
            protected void done(){progress.dispose();try{get();refresh.run();JOptionPane.showMessageDialog(parent,success);}catch(Exception ex){showError(parent,ex);}}
        }.execute();
        progress.setVisible(true);
    }
    private static void rollback(Connection c,Exception original){try{c.rollback();}catch(SQLException ex){original.addSuppressed(ex);}}
    private static void showError(Component parent,Throwable ex){while(ex.getCause()!=null)ex=ex.getCause();ex.printStackTrace();JOptionPane.showMessageDialog(parent,ex.getMessage()==null?"Operația nu a putut fi finalizată.":ex.getMessage(),"Actualizare înscriere",JOptionPane.ERROR_MESSAGE);}
}

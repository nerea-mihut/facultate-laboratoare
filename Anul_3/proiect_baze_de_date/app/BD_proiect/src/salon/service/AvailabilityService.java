package salon.service;

import salon.db.DatabaseConnection;
import salon.ui.model.TimeInterval;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class AvailabilityService {
    public record StoredInterval(int id,LocalDateTime start,LocalDateTime end,String type){}
    public static class AvailabilityException extends RuntimeException {public AvailabilityException(String message,Throwable cause){super(message,cause);}}
    public List<StoredInterval> loadStoredDayIntervals(int id,LocalDate date){try(Connection c=DatabaseConnection.getConnection()){return stored(c,id,date);}catch(SQLException ex){throw new AvailabilityException("Intervalele nu au putut fi încărcate.",ex);}}
    private List<StoredInterval> stored(Connection c,int id,LocalDate date)throws SQLException{
        List<StoredInterval> result=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT id_interval,inceput,sfarsit,tip_interval FROM interval_disponibilitate WHERE id_angajat=? AND inceput<? AND sfarsit>? ORDER BY inceput,id_interval")){
            p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(date.plusDays(1).atStartOfDay()));p.setTimestamp(3,Timestamp.valueOf(date.atStartOfDay()));try(ResultSet r=p.executeQuery()){while(r.next())result.add(new StoredInterval(r.getInt(1),r.getTimestamp(2).toLocalDateTime(),r.getTimestamp(3).toLocalDateTime(),r.getString(4)));}
        }return result;
    }
    public List<TimeInterval> loadDayIntervals(int id,LocalDate date){return loadStoredDayIntervals(id,date).stream().map(i->new TimeInterval(i.start(),i.end(),i.type())).toList();}
    public List<TimeInterval> loadWorkIntervals(int id,LocalDate date){return loadDayIntervals(id,date).stream().filter(i->"Lucru".equals(i.type())).toList();}
    public List<TimeInterval> loadAbsenceIntervals(int id,LocalDate date){return loadDayIntervals(id,date).stream().filter(i->"Absenta".equals(i.type())).toList();}
    public void saveInterval(int id,LocalDateTime start,LocalDateTime end,String type)throws SQLException{write(id,null,start,end,type);}
    public void updateInterval(int id,int intervalId,LocalDateTime start,LocalDateTime end,String type)throws SQLException{write(id,intervalId,start,end,type);}
    private void validate(LocalDateTime start,LocalDateTime end,String type){
        if(start==null||end==null||!end.isAfter(start))throw new IllegalArgumentException("Ora de sfârșit trebuie să fie după început.");
        if(!start.toLocalDate().equals(end.toLocalDate())&&!end.equals(start.toLocalDate().plusDays(1).atStartOfDay()))throw new IllegalArgumentException("Intervalul trebuie să aparțină unei singure zile.");
        if(!"Lucru".equals(type)&&!"Absenta".equals(type))throw new IllegalArgumentException("Tipul intervalului nu este valid.");
    }
    private void write(int id,Integer intervalId,LocalDateTime start,LocalDateTime end,String type)throws SQLException{
        validate(start,end,type);
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);try{
                lock(c,id);
                if(!courses(c,id,start.toLocalDate()).isEmpty())throw new SQLException("Ziua are un curs și este blocată integral pentru salon.");
                try(PreparedStatement p=c.prepareStatement("SELECT id_interval FROM interval_disponibilitate WHERE id_angajat=? AND tip_interval=? AND inceput<? AND sfarsit>? AND (? IS NULL OR id_interval<>?) FOR UPDATE")){
                    p.setInt(1,id);p.setString(2,type);p.setTimestamp(3,Timestamp.valueOf(end));p.setTimestamp(4,Timestamp.valueOf(start));p.setObject(5,intervalId);p.setObject(6,intervalId);try(ResultSet r=p.executeQuery()){if(r.next())throw new SQLException("Există un interval de același tip care se suprapune.");}
                }
                if("Absenta".equals(type))ensureNoBookings(c,id,start,end);
                String sql=intervalId==null?"INSERT INTO interval_disponibilitate (id_angajat,inceput,sfarsit,tip_interval) VALUES (?,?,?,?)":"UPDATE interval_disponibilitate SET inceput=?,sfarsit=?,tip_interval=? WHERE id_interval=? AND id_angajat=?";
                try(PreparedStatement p=c.prepareStatement(sql)){
                    if(intervalId==null){p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(start));p.setTimestamp(3,Timestamp.valueOf(end));p.setString(4,type);}
                    else{p.setTimestamp(1,Timestamp.valueOf(start));p.setTimestamp(2,Timestamp.valueOf(end));p.setString(3,type);p.setInt(4,intervalId);p.setInt(5,id);}p.executeUpdate();
                    if(intervalId!=null)try(PreparedStatement check=c.prepareStatement("SELECT id_interval FROM interval_disponibilitate WHERE id_interval=? AND id_angajat=?")){check.setInt(1,intervalId);check.setInt(2,id);try(ResultSet r=check.executeQuery()){if(!r.next())throw new SQLException("Intervalul nu mai există.");}}
                }
                c.commit();
            }catch(SQLException|RuntimeException ex){rollback(c,ex);throw ex;}
        }
    }
    public void deleteStoredInterval(int id,int intervalId)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM interval_disponibilitate WHERE id_interval=? AND id_angajat=?")){p.setInt(1,intervalId);p.setInt(2,id);if(p.executeUpdate()==0)throw new SQLException("Intervalul nu mai există.");}
    }
    public void deleteInterval(int id,TimeInterval interval)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM interval_disponibilitate WHERE id_angajat=? AND inceput=? AND sfarsit=? AND tip_interval=?")){p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(interval.start()));p.setTimestamp(3,Timestamp.valueOf(interval.end()));p.setString(4,interval.type());p.executeUpdate();}
    }
    public void markDayOff(int id,LocalDate date)throws SQLException{
        LocalDateTime start=date.atStartOfDay(),end=date.plusDays(1).atStartOfDay();
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);try{
                lock(c,id);ensureNoBookings(c,id,start,end);
                for(StoredInterval i:stored(c,id,date))if(i.start().isBefore(start)||i.end().isAfter(end))throw new SQLException("Un interval traversează mai multe zile. Modifică-l înainte.");
                try(PreparedStatement p=c.prepareStatement("DELETE FROM interval_disponibilitate WHERE id_angajat=? AND inceput>=? AND sfarsit<=?")){p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(start));p.setTimestamp(3,Timestamp.valueOf(end));p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO interval_disponibilitate (id_angajat,inceput,sfarsit,tip_interval) VALUES (?,?,?,'Absenta')")){p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(start));p.setTimestamp(3,Timestamp.valueOf(end));p.executeUpdate();}c.commit();
            }catch(SQLException|RuntimeException ex){rollback(c,ex);throw ex;}
        }
    }
    private void lock(Connection c,int id)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT id_angajat FROM angajat WHERE id_angajat=? FOR UPDATE")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Angajatul nu există.");}}}
    private void ensureNoBookings(Connection c,int id,LocalDateTime start,LocalDateTime end)throws SQLException{
        for(LocalDate d=start.toLocalDate();!d.isAfter(end.minusNanos(1).toLocalDate());d=d.plusDays(1)){
            List<TimeInterval> busy=new ArrayList<>(appointments(c,id,d));busy.addAll(courses(c,id,d));
            for(TimeInterval i:busy)if(start.isBefore(i.end())&&end.isAfter(i.start()))throw new SQLException("Intervalul se suprapune cu o programare sau o zi de curs.");
        }
    }
    public List<TimeInterval> loadAppointmentIntervals(int id,LocalDate date){try(Connection c=DatabaseConnection.getConnection()){return appointments(c,id,date);}catch(SQLException ex){throw new AvailabilityException("Programările nu au putut fi încărcate.",ex);}}
    private List<TimeInterval> appointments(Connection c,int id,LocalDate date)throws SQLException{
        String sql="SELECT p.id_programare,p.data,p.ora,COALESCE(SUM(ps.durata_aplicata),0) AS durata FROM programare p LEFT JOIN programare_serviciu ps ON ps.id_programare=p.id_programare WHERE p.id_tehnician=? AND p.stare<>'Anulata' AND p.data<=? GROUP BY p.id_programare,p.data,p.ora ORDER BY p.data,p.ora";
        List<TimeInterval> result=new ArrayList<>();try(PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));try(ResultSet r=p.executeQuery()){while(r.next()){
                LocalDateTime start=LocalDateTime.of(r.getDate("data").toLocalDate(),r.getTime("ora").toLocalTime());int duration=r.getInt("durata");
                if(duration<=0){if(start.toLocalDate().equals(date))throw new SQLException("Programarea "+r.getInt("id_programare")+" nu are durată validă.");continue;}
                LocalDateTime end=start.plusMinutes(duration);if(start.isBefore(date.plusDays(1).atStartOfDay())&&end.isAfter(date.atStartOfDay()))result.add(new TimeInterval(start,end,"Programare"));
            }}
        }return result;
    }
    public List<TimeInterval> loadCourseIntervals(int id,LocalDate date){try(Connection c=DatabaseConnection.getConnection()){return courses(c,id,date);}catch(SQLException ex){throw new AvailabilityException("Cursurile nu au putut fi încărcate.",ex);}}
    private List<TimeInterval> courses(Connection c,int id,LocalDate date)throws SQLException{
        String sql="SELECT i.id_interval FROM interval_sesiune i JOIN sesiune_curs s ON s.id_sesiune=i.id_sesiune WHERE s.id_trainer=? AND s.stare<>'Anulata' AND (i.data_zi=? OR (i.inceput<? AND i.sfarsit>?)) LIMIT 1";
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));p.setTimestamp(3,Timestamp.valueOf(date.plusDays(1).atStartOfDay()));p.setTimestamp(4,Timestamp.valueOf(date.atStartOfDay()));
            try(ResultSet r=p.executeQuery()){if(r.next())return List.of(new TimeInterval(date.atStartOfDay(),date.plusDays(1).atStartOfDay(),"Curs"));}
        }return List.of();
    }
    public List<LocalDateTime> availableSlots(int id,LocalDate date,int duration){
        if(duration<=0)throw new IllegalArgumentException("Durata trebuie să fie mai mare decât zero.");
        try(Connection c=DatabaseConnection.getConnection()){
            if(!courses(c,id,date).isEmpty())return List.of();
            List<TimeInterval> work=new ArrayList<>(),busy=new ArrayList<>();
            for(StoredInterval i:stored(c,id,date)){TimeInterval t=new TimeInterval(i.start(),i.end(),i.type());if("Lucru".equals(i.type()))work.add(t);else busy.add(t);}
            busy.addAll(appointments(c,id,date));work=merge(work);TreeSet<LocalDateTime> result=new TreeSet<>();LocalDateTime dayStart=date.atStartOfDay(),dayEnd=date.plusDays(1).atStartOfDay(),now=LocalDateTime.now();
            for(TimeInterval i:work){LocalDateTime start=i.start().isAfter(dayStart)?i.start():dayStart,end=i.end().isBefore(dayEnd)?i.end():dayEnd;
                for(LocalDateTime t=start;!t.plusMinutes(duration).isAfter(end);t=t.plusMinutes(30)){
                    if(!t.isAfter(now))continue;boolean occupied=false;for(TimeInterval b:busy)if(t.isBefore(b.end())&&t.plusMinutes(duration).isAfter(b.start())){occupied=true;break;}if(!occupied)result.add(t);
                }
            }return new ArrayList<>(result);
        }catch(SQLException ex){throw new AvailabilityException("Disponibilitatea nu a putut fi calculată.",ex);}
    }
    private List<TimeInterval> merge(List<TimeInterval> items){
        List<TimeInterval> sorted=new ArrayList<>(items),result=new ArrayList<>();sorted.sort(Comparator.comparing(TimeInterval::start));
        for(TimeInterval i:sorted){if(result.isEmpty()){result.add(i);continue;}TimeInterval last=result.get(result.size()-1);if(!i.start().isAfter(last.end()))result.set(result.size()-1,new TimeInterval(last.start(),i.end().isAfter(last.end())?i.end():last.end(),"Lucru"));else result.add(i);}return result;
    }
    private void rollback(Connection c,Exception original){try{c.rollback();}catch(SQLException ex){original.addSuppressed(ex);}}
}
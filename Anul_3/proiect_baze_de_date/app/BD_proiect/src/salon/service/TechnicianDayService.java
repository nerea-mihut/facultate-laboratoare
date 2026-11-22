package salon.service;

import salon.db.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.*;

public class TechnicianDayService {
    public record ServiceOffer(int serviceId,String name,String category,BigDecimal price,int durationMinutes){@Override public String toString(){return name;}}
    public record DayInterval(LocalDateTime start,LocalDateTime end){}
    public record DayConfiguration(LocalDate date,String dayType,boolean explicitlyConfigured,List<DayInterval> workIntervals,List<DayInterval> breaks,List<ServiceOffer> availableOffers,Set<Integer> selectedServiceIds){}
    public static class DayConfigurationException extends SQLException{public DayConfigurationException(String message){super(message);}}
    public DayConfiguration loadDay(int id,LocalDate date)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection()){
            List<ServiceOffer> offers=offers(c,id);List<DayInterval> work=new ArrayList<>(),breaks=new ArrayList<>();LocalDateTime start=date.atStartOfDay(),end=date.plusDays(1).atStartOfDay();
            try(PreparedStatement p=c.prepareStatement("SELECT inceput,sfarsit,tip_interval FROM interval_disponibilitate WHERE id_angajat=? AND inceput<? AND sfarsit>? ORDER BY inceput,id_interval")){
                p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(end));p.setTimestamp(3,Timestamp.valueOf(start));try(ResultSet r=p.executeQuery()){while(r.next()){
                    LocalDateTime from=r.getTimestamp(1).toLocalDateTime(),to=r.getTimestamp(2).toLocalDateTime();DayInterval i=new DayInterval(from.isBefore(start)?start:from,to.isAfter(end)?end:to);
                    if("Lucru".equals(r.getString(3)))work.add(i);else breaks.add(i);
                }}
            }
            boolean configured=false;String type="Lucru";
            try(PreparedStatement p=c.prepareStatement("SELECT tip_zi FROM configurare_zi_tehnician WHERE id_tehnician=? AND data=?")){
                p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));try(ResultSet r=p.executeQuery()){if(r.next()){configured=true;type=r.getString(1);}}
            }
            Set<Integer> selected=new HashSet<>();
            if(configured){
                try(PreparedStatement p=c.prepareStatement("SELECT sz.id_serviciu FROM serviciu_zi_tehnician sz JOIN tehnician_serviciu ts ON ts.id_tehnician=sz.id_tehnician AND ts.id_serviciu=sz.id_serviciu WHERE sz.id_tehnician=? AND sz.data=? AND ts.stare_oferta='Activa'")){
                    p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));try(ResultSet r=p.executeQuery()){while(r.next())selected.add(r.getInt(1));}
                }
            }else{
                for(ServiceOffer offer:offers)selected.add(offer.serviceId());
                if(breaks.stream().anyMatch(i->i.start().equals(start)&&i.end().equals(end)))type="Libera";
            }
            return new DayConfiguration(date,type,configured,List.copyOf(work),List.copyOf(breaks),List.copyOf(offers),Set.copyOf(selected));
        }
    }
    private List<ServiceOffer> offers(Connection c,int id)throws SQLException{
        List<ServiceOffer> result=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT s.id_serviciu,s.denumire,s.categorie,ts.pret,ts.durata FROM tehnician_serviciu ts JOIN serviciu s ON s.id_serviciu=ts.id_serviciu WHERE ts.id_tehnician=? AND ts.stare_oferta='Activa' ORDER BY s.categorie,s.id_serviciu")){
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){while(r.next())result.add(new ServiceOffer(r.getInt(1),r.getString(2),r.getString(3),r.getBigDecimal(4),r.getInt(5)));}
        }return result;
    }
    public void saveWorkingDay(int id,LocalDate date,List<DayInterval> intervals,List<DayInterval> breaks,Set<Integer> serviceIds)throws SQLException{
        if(date==null)throw new DayConfigurationException("Selectează o dată.");List<DayInterval> work=normalize(date,intervals),pauses=normalize(date,breaks);Set<Integer> selected=serviceIds==null?Set.of():Set.copyOf(serviceIds);
        if(work.isEmpty())throw new DayConfigurationException("Adaugă cel puțin un interval de lucru.");if(selected.isEmpty())throw new DayConfigurationException("Selectează cel puțin un serviciu.");
        for(DayInterval pause:pauses)if(work.stream().noneMatch(i->!pause.start().isBefore(i.start())&&!pause.end().isAfter(i.end())))throw new DayConfigurationException("Fiecare pauză trebuie să fie în interiorul unui interval de lucru.");
        List<DayInterval> effective=subtract(work,pauses);if(effective.isEmpty())throw new DayConfigurationException("Pauzele ocupă întregul program de lucru.");save(id,date,"Lucru",work,pauses,selected,effective);
    }
    public void saveDayOff(int id,LocalDate date)throws SQLException{
        if(date==null)throw new DayConfigurationException("Selectează o dată.");save(id,date,"Libera",List.of(),List.of(new DayInterval(date.atStartOfDay(),date.plusDays(1).atStartOfDay())),Set.of(),List.of());
    }
    private void save(int id,LocalDate date,String type,List<DayInterval> work,List<DayInterval> pauses,Set<Integer> selected,List<DayInterval> effective)throws SQLException{
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                lock(c,id);checkCourse(c,id,date);ensureDay(c,id,date);Set<Integer> active=new HashSet<>();for(ServiceOffer offer:offers(c,id))active.add(offer.serviceId());
                if(!active.containsAll(selected))throw new DayConfigurationException("Un serviciu selectat nu mai este în oferta activă.");checkAppointments(c,id,date,type,selected,effective);
                try(PreparedStatement p=c.prepareStatement("INSERT INTO configurare_zi_tehnician (id_tehnician,data,tip_zi) VALUES (?,?,?) ON DUPLICATE KEY UPDATE tip_zi=VALUES(tip_zi)")){p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));p.setString(3,type);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("DELETE FROM serviciu_zi_tehnician WHERE id_tehnician=? AND data=?")){p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO serviciu_zi_tehnician (id_tehnician,data,id_serviciu) VALUES (?,?,?)")){for(int service:selected){p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));p.setInt(3,service);p.addBatch();}p.executeBatch();}
                try(PreparedStatement p=c.prepareStatement("DELETE FROM interval_disponibilitate WHERE id_angajat=? AND inceput>=? AND sfarsit<=?")){p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(date.atStartOfDay()));p.setTimestamp(3,Timestamp.valueOf(date.plusDays(1).atStartOfDay()));p.executeUpdate();}
                insert(c,id,work,"Lucru");insert(c,id,pauses,"Absenta");c.commit();
            }catch(SQLException|RuntimeException ex){try{c.rollback();}catch(SQLException rollback){ex.addSuppressed(rollback);}throw ex;}
        }
    }
    private void lock(Connection c,int id)throws SQLException{
        for(String table:List.of("angajat","tehnician"))try(PreparedStatement p=c.prepareStatement("SELECT id_angajat FROM "+table+" WHERE id_angajat=? FOR UPDATE")){
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new DayConfigurationException("Contul nu este asociat unui tehnician valid.");}
        }
    }
    private void checkCourse(Connection c,int id,LocalDate date)throws SQLException{
        String sql="SELECT s.id_sesiune FROM interval_sesiune i JOIN sesiune_curs s ON s.id_sesiune=i.id_sesiune WHERE s.id_trainer=? AND s.stare<>'Anulata' AND (i.data_zi=? OR (i.inceput<? AND i.sfarsit>?)) LIMIT 1";
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));p.setTimestamp(3,Timestamp.valueOf(date.plusDays(1).atStartOfDay()));p.setTimestamp(4,Timestamp.valueOf(date.atStartOfDay()));
            try(ResultSet r=p.executeQuery()){if(r.next())throw new DayConfigurationException("Ziua este rezervată integral pentru cursul din sesiunea #"+r.getInt(1)+". Nu modifica programul de salon în această zi. Gestionează mai întâi cursul.");}
        }
    }
    private void ensureDay(Connection c,int id,LocalDate date)throws SQLException{
        LocalDateTime start=date.atStartOfDay(),end=date.plusDays(1).atStartOfDay();
        try(PreparedStatement p=c.prepareStatement("SELECT id_interval,inceput,sfarsit FROM interval_disponibilitate WHERE id_angajat=? AND inceput<? AND sfarsit>? FOR UPDATE")){
            p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(end));p.setTimestamp(3,Timestamp.valueOf(start));try(ResultSet r=p.executeQuery()){while(r.next())if(r.getTimestamp(2).toLocalDateTime().isBefore(start)||r.getTimestamp(3).toLocalDateTime().isAfter(end))throw new DayConfigurationException("Intervalul "+r.getInt(1)+" traversează mai multe zile. Separă-l înainte de configurare.");}
        }
    }
    private void checkAppointments(Connection c,int id,LocalDate date,String type,Set<Integer> selected,List<DayInterval> effective)throws SQLException{
        String sql="SELECT p.id_programare,p.data,p.ora,COALESCE(SUM(ps.durata_aplicata),0) AS durata FROM programare p LEFT JOIN programare_serviciu ps ON ps.id_programare=p.id_programare WHERE p.id_tehnician=? AND p.stare<>'Anulata' AND p.data<=? GROUP BY p.id_programare,p.data,p.ora ORDER BY p.data,p.ora";
        LocalDateTime dayStart=date.atStartOfDay(),dayEnd=date.plusDays(1).atStartOfDay();
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setDate(2,java.sql.Date.valueOf(date));try(ResultSet r=p.executeQuery()){while(r.next()){
                int appointment=r.getInt(1),duration=r.getInt("durata");LocalDateTime start=LocalDateTime.of(r.getDate("data").toLocalDate(),r.getTime("ora").toLocalTime());
                if(duration<=0){if(start.toLocalDate().equals(date))throw new DayConfigurationException("Programarea "+appointment+" nu are durată validă.");continue;}
                LocalDateTime end=start.plusMinutes(duration);if(!start.isBefore(dayEnd)||!end.isAfter(dayStart))continue;
                if("Libera".equals(type))throw new DayConfigurationException("Ziua are deja programarea "+appointment+". Anuleaz-o sau reprogrameaz-o explicit înainte.");
                LocalDateTime from=start.isBefore(dayStart)?dayStart:start,to=end.isAfter(dayEnd)?dayEnd:end;
                if(effective.stream().noneMatch(i->!from.isBefore(i.start())&&!to.isAfter(i.end())))throw new DayConfigurationException("Programarea "+appointment+" ar rămâne în afara programului sau s-ar suprapune cu o pauză.");
                if(start.toLocalDate().equals(date))try(PreparedStatement q=c.prepareStatement("SELECT id_serviciu FROM programare_serviciu WHERE id_programare=?")){
                    q.setInt(1,appointment);try(ResultSet rows=q.executeQuery()){while(rows.next())if(!selected.contains(rows.getInt(1)))throw new DayConfigurationException("Un serviciu debifat este folosit în programarea "+appointment+". Păstrează-l selectat sau modifică programarea.");}
                }
            }}
        }
    }
    private List<DayInterval> normalize(LocalDate date,List<DayInterval> intervals)throws SQLException{
        List<DayInterval> sorted=intervals==null?new ArrayList<>():new ArrayList<>(intervals);LocalDateTime start=date.atStartOfDay(),end=date.plusDays(1).atStartOfDay();
        for(DayInterval i:sorted)if(i==null||i.start()==null||i.end()==null||!i.end().isAfter(i.start())||i.start().isBefore(start)||i.end().isAfter(end))throw new DayConfigurationException("Verifică intervalele: trebuie să aparțină zilei selectate și să aibă sfârșitul după început.");
        sorted.sort(Comparator.comparing(DayInterval::start));List<DayInterval> result=new ArrayList<>();
        for(DayInterval i:sorted){if(result.isEmpty()){result.add(i);continue;}DayInterval last=result.get(result.size()-1);if(i.start().isBefore(last.end()))throw new DayConfigurationException("Două intervale de același tip se suprapun.");if(i.start().equals(last.end()))result.set(result.size()-1,new DayInterval(last.start(),i.end()));else result.add(i);}return result;
    }
    private List<DayInterval> subtract(List<DayInterval> work,List<DayInterval> pauses){
        List<DayInterval> result=new ArrayList<>();for(DayInterval i:work){LocalDateTime cursor=i.start();for(DayInterval pause:pauses){if(!pause.start().isBefore(i.end())||!pause.end().isAfter(i.start()))continue;if(pause.start().isAfter(cursor))result.add(new DayInterval(cursor,pause.start()));if(pause.end().isAfter(cursor))cursor=pause.end();}if(cursor.isBefore(i.end()))result.add(new DayInterval(cursor,i.end()));}return result;
    }
    private void insert(Connection c,int id,List<DayInterval> intervals,String type)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("INSERT INTO interval_disponibilitate (id_angajat,inceput,sfarsit,tip_interval) VALUES (?,?,?,?)")){for(DayInterval i:intervals){p.setInt(1,id);p.setTimestamp(2,Timestamp.valueOf(i.start()));p.setTimestamp(3,Timestamp.valueOf(i.end()));p.setString(4,type);p.addBatch();}p.executeBatch();}
    }
}
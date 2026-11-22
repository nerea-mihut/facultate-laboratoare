package salon.service;

import salon.db.DatabaseConnection;
import java.sql.*;

public class EmployeeAccessService {
    public record Access(String email,int employeeId,String name,boolean technician,boolean trainer) {}
    public Access load(String email)throws SQLException {
        String sql="""
            SELECT u.email,a.id_angajat,a.prenume,a.nume,
              EXISTS(SELECT 1 FROM tehnician t WHERE t.id_angajat=a.id_angajat) AS technician,
              EXISTS(SELECT 1 FROM trainer t WHERE t.id_angajat=a.id_angajat) AS trainer
            FROM utilizator u JOIN angajat a ON a.id_angajat=u.id_angajat
            WHERE u.email=?
            """;
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,email);try(ResultSet r=p.executeQuery()) {
                if(!r.next())throw new SQLException("Contul nu este asociat unui angajat.");
                boolean technician=r.getBoolean("technician"),trainer=r.getBoolean("trainer");
                if(!technician&&!trainer)throw new SQLException("Angajatul nu are funcție de tehnician sau trainer.");
                return new Access(r.getString("email"),r.getInt("id_angajat"),r.getString("prenume")+" "+r.getString("nume"),technician,trainer);
            }
        }
    }
}
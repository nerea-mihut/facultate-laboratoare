package salon.service;

import salon.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TechnicianService {

    private final String userEmail;

    private Integer technicianId;
    private String firstName;
    private String lastName;
    private String phone;

    public TechnicianService(String userEmail) {
        this.userEmail = userEmail;
        loadTechnicianData();
    }

    private void loadTechnicianData() {
        String sql = """
                SELECT
                    a.id_angajat,
                    a.nume,
                    a.prenume,
                    a.telefon
                FROM utilizator u
                JOIN angajat a
                    ON a.id_angajat = u.id_angajat
                JOIN tehnician t
                    ON t.id_angajat = a.id_angajat
                WHERE u.email = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, userEmail);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    technicianId = result.getInt("id_angajat");
                    firstName = result.getString("prenume");
                    lastName = result.getString("nume");
                    phone = result.getString("telefon");
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Integer getTechnicianId() {
        return technicianId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return userEmail;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }

        return (firstName == null ? "" : firstName)
                + " "
                + (lastName == null ? "" : lastName);
    }
}
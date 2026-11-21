package salon.ui.trainer;

import salon.db.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TrainerHomePanel extends JPanel {
    private static final Color CREAM=new Color(244,240,233), INK=new Color(30,30,30);
    public TrainerHomePanel(String email,Runnable courses,Runnable participants,Runnable enrollment) {
        this(email,courses,participants);
    }
    public TrainerHomePanel(String email,Runnable courses,Runnable participants) {
        setLayout(new GridBagLayout());setBackground(CREAM);
        setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        JPanel card=new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g=(Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);g.fillRoundRect(0,0,getWidth(),getHeight(),30,30);g.dispose();
            }
        };
        card.setOpaque(false);card.setBorder(BorderFactory.createEmptyBorder(40,46,40,46));
        card.setPreferredSize(new Dimension(600,370));
        String name=firstName(email);
        JLabel heading=new JLabel(name==null||name.isBlank()?"Bine ai venit!":"Bine ai venit, "+name+"!");
        heading.setFont(new Font("SansSerif",Font.BOLD,28));heading.setForeground(INK);
        JLabel subtitle=new JLabel("Gestionează cursurile, cursanții și documentele de înscriere.");
        subtitle.setFont(new Font("SansSerif",Font.PLAIN,13));subtitle.setForeground(new Color(115,110,103));
        JComponent[] rows={heading,subtitle,button("Cursurile mele",courses),button("Cursanți și documente",participants)};
        GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;
        for(int i=0;i<rows.length;i++){c.gridy=i;c.insets=new Insets(0,0,i==1?30:16,0);card.add(rows[i],c);}
        add(card);
    }
    private JButton button(String text,Runnable action){
        JButton b=new JButton(text);b.setBackground(INK);b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif",Font.BOLD,14));b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(420,48));b.putClientProperty("JButton.arc",18);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.addActionListener(e->action.run());return b;
    }
    private String firstName(String email){
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT a.prenume FROM utilizator u JOIN angajat a ON a.id_angajat=u.id_angajat WHERE u.email=?")){
            p.setString(1,email);try(ResultSet r=p.executeQuery()){if(r.next())return r.getString(1);}
        }catch(SQLException ex){ex.printStackTrace();}return null;
    }
}
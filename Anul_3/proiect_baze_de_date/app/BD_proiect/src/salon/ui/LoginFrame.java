package salon.ui;

import salon.db.DatabaseConnection;
import salon.service.EmployeeAccessService;
import salon.service.EmployeeAccessService.Access;
import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {
    private static final Color CREAM=new Color(244,240,233),INK=new Color(30,30,30),MUTED=new Color(115,110,103),FIELD_BACKGROUND=new Color(250,249,247);
    private final JTextField emailField=new JTextField(24);
    private final JPasswordField passwordField=new JPasswordField(24);
    private final JButton loginButton=new JButton("Autentificare"),createAccountButton=new JButton("Nu ai cont? Creează cont");
    private final JLabel statusLabel=new JLabel(" ");
    private record LoginResult(String role,Access employee) {}
    public LoginFrame() {
        setTitle("Salon & Academie — Autentificare");setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560,660));setSize(680,740);setLocationRelativeTo(null);
        JPanel background=new JPanel(new GridBagLayout());background.setBackground(CREAM);
        background.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));setContentPane(background);
        JPanel card=new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);g.fillRoundRect(0,0,getWidth(),getHeight(),28,28);g.dispose();
            }
        };
        card.setOpaque(false);card.setBorder(BorderFactory.createEmptyBorder(40,42,40,42));
        GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.WEST;
        JLabel brand=new JLabel("SALON & ACADEMIE");brand.setFont(new Font("SansSerif",Font.BOLD,12));brand.setForeground(MUTED);addRow(card,brand,c,0,0,26);
        JLabel heading=new JLabel("Bine ai revenit.");heading.setFont(new Font("SansSerif",Font.BOLD,30));heading.setForeground(INK);addRow(card,heading,c,1,0,10);
        JLabel subtitle=new JLabel("Autentifică-te pentru a continua.");subtitle.setForeground(MUTED);addRow(card,subtitle,c,2,0,32);
        JLabel emailLabel=new JLabel("Adresă de e-mail");emailLabel.setForeground(INK);emailLabel.setLabelFor(emailField);addRow(card,emailLabel,c,3,0,8);
        configureField(emailField);emailField.putClientProperty("JTextField.placeholderText","Introdu adresa de e-mail");addRow(card,emailField,c,4,0,22);
        JLabel passwordLabel=new JLabel("Parolă");passwordLabel.setForeground(INK);passwordLabel.setLabelFor(passwordField);addRow(card,passwordLabel,c,5,0,8);
        configureField(passwordField);passwordField.putClientProperty("JTextField.placeholderText","Introdu parola");addRow(card,passwordField,c,6,0,14);
        char echo=passwordField.getEchoChar();JCheckBox show=new JCheckBox("Arată parola");show.setOpaque(false);show.setForeground(MUTED);
        show.addActionListener(e->passwordField.setEchoChar(show.isSelected()?(char)0:echo));addRow(card,show,c,7,0,22);
        loginButton.setFont(new Font("SansSerif",Font.BOLD,14));loginButton.setBackground(INK);loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(340,48));loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));loginButton.addActionListener(e->login());addRow(card,loginButton,c,8,0,12);
        statusLabel.setForeground(MUTED);statusLabel.setFont(new Font("SansSerif",Font.PLAIN,12));addRow(card,statusLabel,c,9,0,14);
        createAccountButton.setFont(new Font("SansSerif",Font.PLAIN,13));createAccountButton.setForeground(INK);createAccountButton.setContentAreaFilled(false);createAccountButton.setBorderPainted(false);createAccountButton.setFocusPainted(false);
        createAccountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));createAccountButton.addActionListener(e->{new RegisterFrame().setVisible(true);dispose();});addRow(card,createAccountButton,c,10,0,0);
        background.add(card);getRootPane().setDefaultButton(loginButton);setVisible(true);SwingUtilities.invokeLater(emailField::requestFocusInWindow);
    }
    private void configureField(JTextField field){field.setPreferredSize(new Dimension(340,46));field.setBackground(FIELD_BACKGROUND);field.setForeground(INK);field.setCaretColor(INK);field.setMargin(new Insets(10,14,10,14));}
    private void addRow(JPanel panel,Component component,GridBagConstraints c,int row,int top,int bottom){c.gridy=row;c.insets=new Insets(top,0,bottom,0);panel.add(component,c);}
    private void login() {
        String email=emailField.getText().trim();char[] password=passwordField.getPassword();
        if(email.isEmpty()||password.length==0){Arrays.fill(password,'\0');statusLabel.setForeground(new Color(160,45,45));statusLabel.setText("Completează e-mailul și parola.");return;}
        setBusy(true);statusLabel.setForeground(MUTED);statusLabel.setText("Se verifică datele...");
        new SwingWorker<LoginResult,Void>() {
            @Override protected LoginResult doInBackground()throws Exception {
                try {
                    String role=authenticate(email,password);if(role==null)return null;
                    if("Tehnician".equals(role)||"Trainer".equals(role))return new LoginResult(role,new EmployeeAccessService().load(email));
                    if("Client".equals(role))return new LoginResult(role,null);
                    throw new SQLException("Rol de cont necunoscut.");
                }finally{Arrays.fill(password,'\0');}
            }
            @Override protected void done() {
                setBusy(false);
                try {
                    LoginResult result=get();passwordField.setText("");
                    if(result==null){statusLabel.setForeground(new Color(160,45,45));statusLabel.setText("E-mail sau parolă incorectă.");passwordField.requestFocusInWindow();return;}
                    if(result.employee()!=null)new EmployeeDashboard(result.employee());else new ClientDashboard(email).setVisible(true);
                    dispose();
                }catch(Exception ex){ex.printStackTrace();Throwable cause=ex;while(cause.getCause()!=null)cause=cause.getCause();statusLabel.setForeground(new Color(160,45,45));statusLabel.setText("Autentificarea nu a putut fi finalizată.");JOptionPane.showMessageDialog(LoginFrame.this,cause.getMessage(),"Eroare",JOptionPane.ERROR_MESSAGE);}
            }
        }.execute();
    }
    private String authenticate(String email,char[] password)throws Exception {
        String hash=hashPassword(password);
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT rol FROM utilizator WHERE email=? AND parola=?")){
            p.setString(1,email);p.setString(2,hash);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):null;}
        }
    }
    private void setBusy(boolean busy){loginButton.setEnabled(!busy);createAccountButton.setEnabled(!busy);emailField.setEnabled(!busy);passwordField.setEnabled(!busy);loginButton.setText(busy?"Se conectează...":"Autentificare");setCursor(Cursor.getPredefinedCursor(busy?Cursor.WAIT_CURSOR:Cursor.DEFAULT_CURSOR));}
    private String hashPassword(char[] password)throws Exception {
        MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] bytes=new String(password).getBytes(StandardCharsets.UTF_8);byte[] hashed;
        try{hashed=digest.digest(bytes);}finally{Arrays.fill(bytes,(byte)0);}
        StringBuilder result=new StringBuilder();for(byte value:hashed)result.append(String.format("%02x",value&0xff));return result.toString();
    }
}
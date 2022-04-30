import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
/******************************************************************************
 * CGI suvepraktika 2022 proovitöö
 *
 *
 * 01/05/2022
 *
 *
 * Teema: GPS koordinaadid / ÖÖ pikkuse arvutamine
 *
 *
 * Autor: Tahvo Riso
 *****************************************************************************/
public class Aplikatsioon extends JPanel implements Runnable{

    /******************************************************************************
     *Peaklassiga on tegu mis on JPaneli alamklass
     *
     *****************************************************************************/
    private double xkord;
    private double ykord;
    private String kuupaev;
    boolean mustdraw = false;

    //Programm hetkel ei tuvasta kohti, kus on polaaröö, ainult leiab polaarpäevi
    //hetkel ka ei oska joonistada kaardile

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Aplikatsioon());
    }
    @Override
    public void run() {
        JFrame raam = new JFrame("ÖÖpikkus");
        raam.setVisible(true);
        raam.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        raam.setResizable(false);
        JLabel silt = (new JLabel(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("4-050_worldmap_neu_ma_1.jpg")))));
        silt.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("pildile");
                xkord = e.getX();
                System.out.println(xkord);
                ykord = e.getY();
                System.out.println(ykord);
                mustdraw = true;
                repaint();

            }
        });
        raam.getContentPane().add(silt);
        raam.setLayout(new GridBagLayout());
        //GridBagConstraints gbc = new GridBagConstraints();
        raam.add(PohiPaneel());
        raam.pack();
    }
    private JPanel PohiPaneel(){
        JPanel sisu = new JPanel();
        //sisu.setLayout(new BorderLayout());
        JPanel sisu2 = new JPanel();
        sisu2.setLayout(new BoxLayout(sisu2,BoxLayout.PAGE_AXIS));

        sisu2.add(Box.createRigidArea(new Dimension(0,350)));
        TextField x = new TextField("x- koordinaat");
        TextField y = new TextField("y- koordinaat");
        TextField kp = new TextField("kuupäev: dd/MM/yyyy");
        sisu2.add(x);
        sisu2.add(Box.createRigidArea(new Dimension(0,10)));
        sisu2.add(y);
        sisu2.add(Box.createRigidArea(new Dimension(0,10)));
        sisu2.add(kp);
        sisu2.add(Box.createRigidArea(new Dimension(0,10)));
        JButton nupp = new JButton("Enter");
        sisu2.add(nupp);
        sisu2.add(Box.createRigidArea(new Dimension(0,10)));
        JTextArea uut = new JTextArea("ÖÖ pikkus tundides");
        uut.setFocusable(false);
        uut.setBackground(Color.lightGray);
        sisu2.add(uut);
        sisu2.add(Box.createRigidArea(new Dimension(0,400)));
        //sisu.add(sisu2);
        nupp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Double.parseDouble(x.getText())>90||Double.parseDouble(x.getText())<-90){
                    x.setForeground(Color.RED);
                }
                else{
                    x.setForeground(Color.green);
                    xkord = Double.parseDouble(x.getText());
                }
                if(Double.parseDouble(y.getText())<-180||Double.parseDouble(y.getText())>180){
                    y.setForeground(Color.RED);
                }
                else{
                    y.setForeground(Color.green);
                    ykord = Double.parseDouble(y.getText());}
                try {
                    Date kuupaev1 = new SimpleDateFormat("dd/MM/yyyy").parse(kp.getText());
                    kuupaev = kp.getText();
                    kp.setForeground(Color.green);
                    double nr = Math.round(arvuta(kuupaev,Double.parseDouble(x.getText()),Double.parseDouble(y.getText()))*100);
                    double nr2 = nr/100;
                    String arv = Double.toString(nr2);
                    uut.setText(arv+" tundi");
                    y.setForeground(Color.black);
                    x.setForeground(Color.black);
                    kp.setForeground(Color.black);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    kp.setForeground(Color.red);
                }
            }
        });

        return sisu2;
    }
    @Override
    public void paintComponent(Graphics e){
        if(!mustdraw){return;}
        super.paintComponent(e);
        Graphics2D gd = (Graphics2D)e;
        gd.setColor(Color.RED);
        gd.fillOval((int)xkord,(int)ykord,100,100);
        gd.fillRect(200,200,200,200);
        //gd.dispose();
    }

    private double arvuta(String kuupaev, double x,double y){
        String[] paevad = kuupaev.split("/");
        double paev = Integer.parseInt(paevad[0]);
        double kuu = Integer.parseInt(paevad[1]);
        int aasta = Integer.parseInt(paevad[2]);
        double jdk = (1461*(aasta+4800+(kuu-14)/12))/4 + (367*(kuu-2-12*((kuu-14)/12)))/12 - (3*((aasta+4900+(kuu-14)/12)/100))/4 + paev - 32075;
        double n = Math.ceil(jdk-2451545.0 + 0.0008);
        double Jstar = n- (y / 360);
        double M = (357.5291 + (0.98560028 * Jstar)) % 360;
        double C = 1.9148 * Math.sin(M*2*Math.PI/360) + (0.02 * Math.sin(2*M*2*Math.PI/360)) + (0.0003 * Math.sin(3*M*2*Math.PI/360));
        double lambda = (M+C+180+102.9372) % 360;
        double Jtransit = 2451545 + Jstar + (0.0053 * Math.sin(M*2*Math.PI/360)) - (0.0069 * Math.sin(lambda*4*Math.PI/360));
        double sindelta = Math.sin(lambda*2*Math.PI/360) * Math.sin(23.44*2*Math.PI/360);
        double delta = Math.asin(sindelta) * 360/(2*Math.PI);
        double cos_omega = (Math.sin(-0.83*2*Math.PI/360) - (sindelta*Math.sin(x*2*Math.PI/360))) /  (Math.cos(x*2*Math.PI/360) * Math.cos(delta*2*Math.PI/360));
        double omega = Math.acos(cos_omega) * 360 / (2*Math.PI);
        double Jrise = Jtransit - omega/360;
        double Jset = Jtransit + omega/360;
        return ((1-(Jset-Jrise))*24);

    }



}

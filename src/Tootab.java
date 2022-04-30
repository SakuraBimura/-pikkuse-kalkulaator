import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 *
 *
 * Programmi kasutusjuhis :
 * Käivita programm ja sisesta vähemalt kuupäeva lahtrisse kuupäev kujul päev/kuu/aasta eraldajaks / ehk kaldkriips
 * Seejärel kirjuta longitude kasti arv vahemikus [-180:180] ja latitude kasti arv vahemikus [-90:90] ning vajuta enter
 * Teine variant on kirjutada ära kuupäev ja valida kaardilt asukoht, mille kohta tahad infot vastava kuupäeva kohta:
 * ehk teisisõnu, koordinaate saab valida kaardilt hiirega klikates või kastidesse kirjutades. Kuupäeva peab valima alati manuaalselt
 * Edasiarendades saaks luuga avaneva menüüga koha, kust siis hiirega sobiv kuupäev valida, täna seda varianti kahjuks ei eksisteeri
 * Kui kuupäev on kirjas siis programm kaardile vajutades arvutab ööpikkuse vastavate andmetega ise, teisel juhul peab vajutama enter
 * nuppu ja tulemus ilmub enter nupu alla halli tekstiala sisse.
 *****************************************************************************/

public class Tootab extends JPanel{
    /******************************************************************************
     *Peaklassiga on tegu mis on JPaneli alamklass
     *Meil on muutujad xkord ja ykord mis vastavad siis koordinaatidele
     * Muutujad xkordkaart ja ykordkaart on teisendatud koordinaadid xkordist ja ykordist
     * mille abil siis joonistame kaardile täppi ja vastupidi, mille abil teisendame
     * nad ümber ja arvutame ööpikkust
     * Kuupäev ehk siis sisestatud kuupäev
     * Textfieldid ja Textarea mis on loodud siin, et neid hiljem ümber väärtustada
     *****************************************************************************/
    private double xkord;
    private double ykord;
    private double xkordkaart;
    private double ykordkaart;
    private String kuupaev;
    private TextField x = new TextField("enter latitude");
    private TextField y = new TextField("enter longitude");
    private JTextArea uut = new JTextArea("ÖÖ pikkus tundides");

    public static void main(String[] args) {
        /******************************************************************************
         *Peaklassiga on tegu, mis käivitab protsessi
         *****************************************************************************/
        new Tootab();
    }
    public Tootab(){
        /******************************************************************************
         *Siin hakkab meie protsess pihta.
         * Alguses üritame sisse lugeda faili, ehk luua pilti taustana sisaldav JPanel
         * kuhu ka lisame muutujad ja joonistamise meetodi
         * Kui JPaneliga saab programm hakkama ja kõik toimib faili lugemisel, siis loome
         * raami Jframe abil, seame ta piirid ja temale sisse siis 2 JPanelit,
         * 1 Neist sisaldab maailmapilti ja teine
         * On loodud nuppude ja tekstiväljadega.
         * Nad on eraldi lisatud, sest maailmapildiga JPanelil on küljes kuular, mis
         * reageerib hiireklahvi vajutustele
         *****************************************************************************/
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Pildiraam pr = null;
                try{
                    pr = new Pildiraam(); // Siin loome meie pildifailiga esimene JPaneli
                }catch(IOException e){
                    e.printStackTrace();
                    System.exit(-1); // kui faili ei saa lugeda siis programm sulgub
                }
                JFrame raam = new JFrame("ÖÖ pikkuse arvutaja"); // loome raami kuhu sisse paigutame JPanelid
                raam.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                raam.setLayout(new BorderLayout());
                raam.getContentPane().add(pr); // Lisame esimese JPaneli koos maailmapildiga
                raam.add(new Nupud(),BorderLayout.AFTER_LINE_ENDS); // loome teise JPaneli ja lisame samuti raami sisse
                raam.pack();
                raam.setVisible(true);
                raam.setResizable(false);
            }
        });
    }
    public class Nupud extends JPanel{
        /******************************************************************************
         *Klass, teise JPaneli jaoks, kus sees on tekstiväljad ja nupud
         * nupul on ka küljes actionlistener, mis käivitub talle vajutades ja viib läbi erinevaid
         * tingimuslausete kontrolle ja reageerib siis vastavalt.
         * Kui kõik on korras, siis arvutab öö pikkuse antud andmetega
         *
         *****************************************************************************/
        public Nupud(){
            setBorder(new EmptyBorder(4,4,4,4));
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints(); // Siin kasutame layoutmanageri, mis aitab meil lihtsamini nuppe paigutada
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            TextField kp = new TextField("kuupäev: dd/MM/yyyy");
            JButton nupp = new JButton("Enter");

            uut.setFocusable(false);
            uut.setBackground(Color.lightGray);

            nupp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (Double.parseDouble(x.getText()) > 90 || Double.parseDouble(x.getText()) < -90) {
                            x.setForeground(Color.RED);
                        } else {
                            x.setForeground(Color.green);
                            xkord = Double.parseDouble(x.getText());
                        }
                        if (Double.parseDouble(y.getText()) < -180 || Double.parseDouble(y.getText()) > 180) {
                            y.setForeground(Color.RED);
                        } else {
                            y.setForeground(Color.green);
                            ykord = Double.parseDouble(y.getText());
                        }
                        try {
                            /******************************************************************************
                             *Kui oleme juba niikaugele jõudnud siis sisestatud koordinaadid vastavad tingimustele ja nüüd vaatame üle ka kuupäeva
                             * Kui kuupäev klapib, arvutame ööpikkuse, teisendame antud koordinaate ning joonistame vastavasse kohta kaardile täpi
                             *Hetkel saab sisestada vaid kitsendatud juhul kuupäeva, kus eraldajaks on / nagu näidatud
                             *****************************************************************************/
                            Date kuupaev1 = new SimpleDateFormat("dd/MM/yyyy").parse(kp.getText());
                            kuupaev = kp.getText();
                            String[] paevad = kuupaev.split("/");
                            double paev = Integer.parseInt(paevad[0]);
                            double kuu = Integer.parseInt(paevad[1]);
                            int aasta = Integer.parseInt(paevad[2]);
                            if (paev < 0 || paev > 31 || kuu < 0 || kuu > 12 || aasta > 9999) {
                                kp.setForeground(Color.RED);
                                return;
                            }

                            kp.setForeground(Color.green);
                            double nr = Math.round(arvuta(kuupaev, Double.parseDouble(x.getText()), Double.parseDouble(y.getText())) * 100);
                            double nr2 = nr / 100;
                            String arv = Double.toString(nr2);
                            xkordkaart = teisendakaardilex(xkord);
                            ykordkaart = teisendakaardiley(ykord);
                            uut.setText(arv + " tundi");
                            y.setForeground(Color.black);
                            x.setForeground(Color.black);
                            kp.setForeground(Color.black);
                            repaint();
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                            kp.setForeground(Color.red);
                        }
                    }catch(NumberFormatException f){
                        x.setForeground(Color.RED);
                        y.setForeground(Color.RED);
                        kp.setForeground(Color.RED);
                        uut.setText("Sisesta palun asju korrektselt");
                    }
                }
            });
            // väljade lisamine JPanelisse
            add(x, gbc);
            gbc.gridy++;
            add(y, gbc);
            gbc.gridy++;
            add(kp, gbc);
            gbc.gridy++;
            add(nupp, gbc);
            gbc.gridy++;
            add(uut,gbc);
        }
    }
    public class Pildiraam extends JPanel{
        /******************************************************************************
         *Meie taustapildi loomise klass, kus üritame pilti sisse lugeda ja paintcombonendiga siis taustale joonistada
         * Kui antud koordinaadid ei ole 0 väärtusega, siis joonistab ka meetod kaardile vastavasse kohta täpikese
         *
         *****************************************************************************/
        BufferedImage bimg = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("4-050_worldmap_neu_ma_1.jpg")));
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(bimg!=null)
            g.drawImage(bimg,0,0,this);
            g.setColor(Color.RED);
            if(xkordkaart != 0 && ykordkaart != 0){
            g.fillOval((int)ykordkaart-5,(int)xkordkaart-5,10,10);}
            g.dispose();
            repaint();
        }
        public Pildiraam() throws IOException {
            /******************************************************************************
             *Siin määrame dimensioonid ja lisame hiire klõpsule reageerija, millega siis vastavalt arvutatakse
             * öö pikkus seal kohas ja joonistatakse ka hiire alla väikene ringike, et näha kuhu vajutati
             *
             *****************************************************************************/
            setBorder(new EmptyBorder(4,4,4,4));
            setPreferredSize(new Dimension(1444,900));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    xkordkaart = e.getY();
                    ykordkaart = e.getX();
                    x.setText(Double.toString(teisenday(xkordkaart)));
                    y.setText(Double.toString(teisendax(ykordkaart)));
                    double nr = Math.round(arvuta(kuupaev,Double.parseDouble(x.getText()),Double.parseDouble(y.getText()))*100);
                    double nr2 = nr/100;
                    String arv = Double.toString(nr2);
                    uut.setText(arv+" tundi");
                    repaint();
                }
            });
        }
    }
    // Meetodid, et teisendada koordinaate kas kaardilt x-ks ja y-ks või vastupidi. Need liiguvad textfield lahtritest
    // Joonistamismeetodisse ja vastupidi. Hiire nupuvajutuse alt textfieldi, kuvamiseks, kuhu vajutati
    private double teisenday(double arv){
        return -((arv-450)/5);
    }
    private double teisendax(double arv){
        return (arv-720)/4;
    }
    private double teisendakaardilex(double arv){
        return -5*arv+450;
    }
    private double teisendakaardiley(double arv){
        return 4*arv+720;
    }
    // viimaks wikipediast võetud ja ise kokku pandud meetod, mille abil siis arvutatakse ööpikkus vastavate andmete põhjal
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

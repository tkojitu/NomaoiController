package nomaoi;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public class NomaoiController implements Runnable {
    private Connector connector = new Connector();
    private Metronome metronome = new Metronome(60);
    private JFrame frame;

    public void dumpMidiDevices() throws MidiUnavailableException {
        connector.dumpMidiDevices();
    }

    public void setup(int indexMidiIn, int indexMidiOut, int indexInst)
    throws MidiUnavailableException {
        connector.setup(indexMidiIn, indexMidiOut, indexInst);
    }

    public void createAndShowGui() {
        newFrame();
        frame.setVisible(true);
    }

    public JPanel createPane() {
        JPanel pane = new JPanel();
        GroupLayout layout = new GroupLayout(pane);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        pane.setLayout(layout);

        JPanel connPane = connector.createPane();
        connPane.setBorder(new TitledBorder("Connector"));
        JPanel metroPane = metronome.createPane();
        metroPane.setBorder(new TitledBorder("Metronome"));

        GroupLayout.ParallelGroup groupH = 
                layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        groupH.addComponent(connPane).addComponent(metroPane);
        layout.setHorizontalGroup(groupH);

        GroupLayout.SequentialGroup groupV = layout.createSequentialGroup();
        groupV.addComponent(connPane).addComponent(metroPane);
        layout.setVerticalGroup(groupV);
        return pane;
    }

    private JFrame newFrame() {
        frame = new JFrame("NomaoiController");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createPane());
        frame.pack();
        return frame;
    }

    @Override
    public void run() {
        createAndShowGui();
    }

    public static void main(String[] args) throws Exception {
        NomaoiController app = new NomaoiController();
        app.dumpMidiDevices();
        int indexMidiIn = getDeviceIndexMidiIn(args);
        int indexMidiOut = getDeviceIndexMidiOut(args);
        int indexInst = getInstrumentIndex(args);
        app.setup(indexMidiIn, indexMidiOut, indexInst);
        SwingUtilities.invokeLater(app);
    }

    private static int getDeviceIndexMidiIn(String[] args) {
        return getOptionArg(args, "-i");
    }

    private static int getDeviceIndexMidiOut(String[] args) {
        return getOptionArg(args, "-o");
    }

    private static int getInstrumentIndex(String[] args) {
        return getOptionArg(args, "-p");
    }

    private static int getOptionArg(String[] args, String opt) {
        for (int i = 0; i < args.length; ++i) {
            if (!opt.equals(args[i])) {
                continue;
            }
            if (i + 1 >= args.length) {
                return -1;
            }
            try {
                return Integer.parseInt(args[i + 1]);
            } catch (NumberFormatException e) {
                System.out.println("illegal index: " + args[i + 1]);
                return -1;
            }
        }
        return -1;
    }
}

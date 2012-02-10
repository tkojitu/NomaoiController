package nomaoi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;

public class Connector implements ActionListener, AutoCloseable, Runnable {
    private JLabel keyLabel;
    private JComboBox<String> comboxIn;
    private JComboBox<String> comboxOut;
    private JComboBox<String> comboxInst;
    private ConnectorModel model = new ConnectorModel();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void close() {
        model.close();
    }

    public void setup(int indexMidiIn, int indexMidiOut, int indexInst)
            throws MidiUnavailableException {
        model.setup(indexMidiIn, indexMidiOut, indexInst);
    }

    public void dumpMidiDevices() throws MidiUnavailableException {
        model.dumpMidiDevices();
    }

    public void createAndShowGui() {
        JFrame frame = createFrame();
        frame.setVisible(true);
    }

    public JFrame createFrame() {
        JFrame frame = new JFrame("NomaoiController");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createPane());
        frame.pack();
        return frame;
    }

    public JPanel createPane() {
        JPanel pane = new JPanel();
        GroupLayout layout = setGroupLayout(pane);
        JLabel labelInTitle = createLabel("MIDI Input:");
        keyLabel = labelInTitle;
        comboxIn = newComboxIn();
        JLabel labelOutTitle = createLabel("MIDI Output:");
        comboxOut = newComboxOut();
        JLabel labelInst = createLabel("Instrument:");
        comboxInst = newComboxInst();

        GroupLayout.SequentialGroup groupH = layout.createSequentialGroup();
        groupH.addGroup(layout.createParallelGroup().
                        addComponent(labelInTitle).
                        addComponent(labelOutTitle).
                        addComponent(labelInst));
        groupH.addGroup(layout.createParallelGroup().
                        addComponent(comboxIn).
                        addComponent(comboxOut).
                        addComponent(comboxInst));
        layout.setHorizontalGroup(groupH);

        GroupLayout.SequentialGroup groupV = layout.createSequentialGroup();
        groupV.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                        addComponent(labelInTitle).
                        addComponent(comboxIn));
        groupV.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                        addComponent(labelOutTitle).
                        addComponent(comboxOut));
        groupV.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                        addComponent(labelInst).
                        addComponent(comboxInst));
        layout.setVerticalGroup(groupV);

        return pane;
    }

    private JLabel createLabel(String text) {
        JLabel result = new JLabel(text);
        result.setFocusable(true);
        result.addKeyListener(model.getKeyListener());
        return result;
    }

    private JComboBox<String> newComboxIn() {
        return newComboxDevice(model.getMidiDeviceIndexIn());
    }

    private JComboBox<String> newComboxOut() {
        return newComboxDevice(model.getMidiDeviceIndexOut());
    }

    private JComboBox<String> newComboxDevice(int deviceIndex) {
        JComboBox<String> combox = new JComboBox<>(model.getMidiDeviceNames());
        combox.setSelectedIndex(deviceIndex);
        combox.addActionListener(this);
        return combox;
    }

    private JComboBox<String> newComboxInst() {
        JComboBox<String> combox = new JComboBox<>(model.getInstNames());
        combox.setSelectedIndex(model.getIndexInst());
        combox.addActionListener(this);
        return combox;
    }

    private GroupLayout setGroupLayout(JPanel pane) {
        GroupLayout layout = new GroupLayout(pane);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        pane.setLayout(layout);
        return layout;
    }

    @Override
    public void run() {
        createAndShowGui();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == comboxIn) {
            model.resetMidiIn(comboxIn.getItemAt(comboxIn.getSelectedIndex()));
        } else if (src == comboxOut) {
            model.resetMidiOut(comboxOut.getItemAt(comboxOut.getSelectedIndex()));
        } else if (src == comboxInst) {
            model.programChange(comboxInst.getSelectedIndex());
        }
        keyLabel.requestFocusInWindow();
    }

    public static void main(String[] args) throws Exception {
        Connector app = new Connector();
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

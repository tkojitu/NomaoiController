package nomaoi;

import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.sound.midi.*;
import javax.swing.*;

public class NomaoiController implements ActionListener, AutoCloseable, Runnable {
    private MidiDevice midiIn;
    private MidiDevice midiOut;
    private JComboBox<String> comboxIn;
    private JComboBox<String> comboxOut;

    public NomaoiController() {}

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public void setup(int indexMidiIn, int indexMidiOut)
            throws MidiUnavailableException {
        midiIn = findMidiInDevice(indexMidiIn);
        if (midiIn == null) {
            System.err.println("cannot find midi input device.");
            System.exit(1);
        }
        midiOut = findMidiOutDevice(indexMidiOut);
        Receiver recv = new AsSoonAsPossibleReceiver(midiOut.getReceiver());
        Transmitter trans = midiIn.getTransmitter();
        trans.setReceiver(recv);
        midiIn.open();
        midiOut.open();

        System.out.println("Input:");
        dumpMidiDevice(midiIn);
        System.out.println("Output:");
        dumpMidiDevice(midiOut);
    }

    private MidiDevice findMidiInDevice(int index) throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        if (0 <= index && index < infos.length) {
            return MidiSystem.getMidiDevice(infos[index]);
        }
        final String klass = "class com.sun.media.sound.MidiInDevice";
        for (int i = 0; i < infos.length; ++ i) {
            MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
            if (klass.equals(dev.getClass().toString())) {
                return dev;
            }
        }
        return null;
    }

    private MidiDevice findMidiOutDevice(int index)
            throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        if (0 <= index && index < infos.length) {
            return MidiSystem.getMidiDevice(infos[index]);
        }
        final String klass = "class com.sun.media.sound.MidiOutDevice";
        for (int i = 0; i < infos.length; ++ i) {
            MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
            if (dev instanceof Synthesizer
                || klass.equals(dev.getClass().toString())) {
                return dev;
            }
        }
        return null;
    }

    public void dumpMidiDevices() throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; ++i) {
            System.out.println("device#" + i);
            MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
            dumpMidiDevice(dev);
        }
    }

    private void dumpMidiDevice(MidiDevice dev) {
        MidiDevice.Info info = dev.getDeviceInfo();
        System.out.println(" " + dev.getClass());
        System.out.println(" " + info.getName());
        System.out.println(" " + info.getDescription());
        System.out.println(" " + info.getVendor());
    }

    public void createAndShowGui() {
        JFrame frame = createFrame();
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("NomaoiController");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createPane());
        frame.pack();
        return frame;
    }

    private JPanel createPane() {
        JPanel pane = new JPanel();
        GroupLayout layout = new GroupLayout(pane);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        pane.setLayout(layout);
        JLabel labelInTitle = new JLabel("MIDI Input: ");
        comboxIn = createCombox(midiIn);
        JLabel labelOutTitle = new JLabel("MIDI Output: ");
        comboxOut = createCombox(midiOut);

        GroupLayout.SequentialGroup groupH = layout.createSequentialGroup();
        groupH.addGroup(layout.createParallelGroup().
                        addComponent(labelInTitle).
                        addComponent(labelOutTitle));
        groupH.addGroup(layout.createParallelGroup().
                        addComponent(comboxIn).
                        addComponent(comboxOut));
        layout.setHorizontalGroup(groupH);

        GroupLayout.SequentialGroup groupV = layout.createSequentialGroup();
        groupV.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                        addComponent(labelInTitle).
                        addComponent(comboxIn));
        groupV.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                        addComponent(labelOutTitle).
                        addComponent(comboxOut));
        layout.setVerticalGroup(groupV);
        return pane;
    }

    private JComboBox createCombox(MidiDevice dev) {
        JComboBox combox = new JComboBox(getMidiDeviceNames());
        setSelectComboxItem(combox, dev);
        combox.addActionListener(this);
        return combox;
    }

    private Vector<String> getMidiDeviceNames() {
        Vector<String> results = new Vector<String>();
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; ++i) {
            results.add("" + i + ": " + infos[i].getName());
        }
        return results;
    }

    private void setSelectComboxItem(JComboBox<String> combox, MidiDevice dev) {
        for (int i = 0; i < combox.getItemCount(); ++i) {
            String str = combox.getItemAt(i);
            int n = extractIndexFromItem(str);
            int m = getMidiDeviceIndex(dev);
            if (n < 0 || m < 0 || n != m) {
                continue;
            }
            combox.setSelectedIndex(i);
            return;
        }
    }

    private int extractIndexFromItem(String item) {
        StringTokenizer tokens = new StringTokenizer(item, ": \t");
        if (!tokens.hasMoreTokens()) {
            return -1;
        }
        try {
            return Integer.parseInt(tokens.nextToken());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int getMidiDeviceIndex(MidiDevice dev) {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; ++i) {
            try {
                if (MidiSystem.getMidiDevice(infos[i]) == dev) {
                    return i;
                }
            } catch (MidiUnavailableException e) {
                continue;
            }
        }
        return -1;
    }

    @Override
    public void run() {
        createAndShowGui();
    }

    @Override
    public void close() {
        if (midiIn != null) {
            midiIn.close();
        }
        if (midiOut != null) {
            midiOut.close();
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == comboxIn) {
            resetMidiIn(comboxIn.getItemAt(comboxIn.getSelectedIndex()));
            return;
        }
        if (src == comboxOut) {
            resetMidiOut(comboxOut.getItemAt(comboxOut.getSelectedIndex()));
            return;
        }
    }

    private void resetMidiIn(String item) {
        int indexIn = extractIndexFromItem(item);
        if (indexIn < 0) {
            return;
        }
        int indexOut = getMidiDeviceIndex(midiOut);
        if (indexOut < 0) {
            return;
        }
        close();
        try {
            setup(indexIn, indexOut);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void resetMidiOut(String item) {
        int indexOut = extractIndexFromItem(item);
        if (indexOut < 0) {
            return;
        }
        int indexIn = getMidiDeviceIndex(midiIn);
        if (indexIn < 0) {
            return;
        }
        close();
        try {
            setup(indexIn, indexOut);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        NomaoiController app = new NomaoiController();
        app.dumpMidiDevices();
        int indexMidiIn = getDeviceIndexMidiIn(args);
        int indexMidiOut = getDeviceIndexMidiOut(args);
        app.setup(indexMidiIn, indexMidiOut);
        SwingUtilities.invokeLater(app);
    }

    private static int getDeviceIndexMidiIn(String[] args) {
        return getOptionArg(args, "-i");
    }

    private static int getDeviceIndexMidiOut(String[] args) {
        return getOptionArg(args, "-o");
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

class AsSoonAsPossibleReceiver implements Receiver {
    private Receiver receiver;

    public AsSoonAsPossibleReceiver(Receiver realReceiver) {
        receiver = realReceiver;
    }

    @Override
    public void close() {
        if (receiver == null) {
            return;
        }
        receiver.close();
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        receiver.send(message, -1);
    }
}

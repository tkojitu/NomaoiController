package nomaoi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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

    public void setup(int indexMidiIn, int indexMidiOut, int indexInst)
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
        setInstrument(indexInst);

        System.out.println("Input:");
        dumpMidiDevice(midiIn);
        System.out.println("Output:");
        dumpMidiDevice(midiOut);
        System.out.println("Instruments:");
        dumpInstruments(midiOut);
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

    private void setInstrument(int index) {
        loadInstrument(index);
        programChange(index);
    }

    private boolean loadInstrument(int index) {
        if (!(midiOut instanceof Synthesizer)) {
            System.out.println("" + midiOut + " is not a Synthesizer.");
            return false;
        }
        Instrument inst = selectInstrument((Synthesizer)midiOut, index);
        if (inst == null) {
            return false;
        }
        ((Synthesizer)midiOut).loadInstrument(inst);
        return true;
    }

    private Instrument selectInstrument(Synthesizer synth, int index) {
        Soundbank bank = synth.getDefaultSoundbank();
        if (bank == null) {
            System.out.println("" + synth + " does not have a SoundBank.");
            return null;
        }
        Instrument[] insts = synth.getDefaultSoundbank().getInstruments();
        if (index < 0 || index <= insts.length) {
            return insts[0];
        }
        return insts[index];
    }

    private void programChange(int program) {
        if (!(midiOut instanceof Synthesizer)) {
            System.out.println("" + midiOut + " is not a Synthesizer.");
            return;
        }
        MidiChannel[] channels = ((Synthesizer)midiOut).getChannels();
        channels[0].programChange((program < 0) ? 0 : program);
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

    private void dumpInstruments(MidiDevice dev) {
        if (!(dev instanceof Synthesizer)) {
            System.out.println("" + dev + " is not a Synthesizer.");
            return;
        }
        Synthesizer synth = (Synthesizer)dev;
        Soundbank bank = synth.getDefaultSoundbank();
        if (bank == null) {
            System.out.println("" + dev + " does not have a SoundBank.");
            return;
        }
        Instrument[] insts = synth.getDefaultSoundbank().getInstruments();
        for (int i = 0; i < insts.length; ++i) {
            System.out.println(" " + i + ": " + insts[i].getName());
        }
    }

    public void createAndShowGui() {
        JFrame frame = newFrame();
        frame.setVisible(true);
    }

    private JFrame newFrame() {
        JFrame frame = new JFrame("NomaoiController");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(newPane());
        frame.pack();
        return frame;
    }

    private JPanel newPane() {
        JPanel pane = new JPanel();
        GroupLayout layout = setGroupLayout(pane);
        JLabel labelInTitle = new JLabel("MIDI Input: ");
        comboxIn = newCombox(midiIn);
        JLabel labelOutTitle = new JLabel("MIDI Output: ");
        comboxOut = newCombox(midiOut);

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

    private GroupLayout setGroupLayout(JPanel pane) {
        GroupLayout layout = new GroupLayout(pane);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        pane.setLayout(layout);
        return layout;
    }

    private JComboBox<String> newCombox(MidiDevice dev) {
        JComboBox<String> combox = new JComboBox<String>(getMidiDeviceNames());
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
            setup(indexIn, indexOut, 0);
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
            setup(indexIn, indexOut, 0);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
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

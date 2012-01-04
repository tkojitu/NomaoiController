package nomaoi;

import java.awt.Dimension;
import javax.sound.midi.*;
import javax.swing.*;

public class NomaoiController implements Runnable {
    MidiDevice midiIn;
    Synthesizer synth;

    public NomaoiController() {}

    public void setup(int midiInPort) throws MidiUnavailableException {
        midiIn = findMidiInDevice(midiInPort);
        if (midiIn == null) {
            System.err.println("cannot find midi input device.");
            System.exit(1);
        }
        synth = MidiSystem.getSynthesizer();
        Transmitter trans = midiIn.getTransmitter();
        trans.setReceiver(synth.getReceiver());
        midiIn.open();
        synth.open();
    }

    private MidiDevice findMidiInDevice(int midiInPort) throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        if (0 <= midiInPort && midiInPort < infos.length) {
            return MidiSystem.getMidiDevice(infos[midiInPort]);
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

    public void dumpMidiDevices() throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; ++i) {
            System.out.println("device#" + i);
            MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
            System.out.println(" " + dev.getClass());
            System.out.println(" " + infos[i].getName());
            System.out.println(" " + infos[i].getDescription());
            System.out.println(" " + infos[i].getVendor());
        }
    }

    public void createAndShowGui() {
        JFrame frame = createFrame();
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("NomaoiKeyboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createPane());
        frame.pack();
        return frame;
    }

    private JPanel createPane() {
        JPanel pane = new JPanel();
        pane.setPreferredSize(new Dimension(320, 240));
        return pane;
    }

    public void run() {
        createAndShowGui();
    }

    public static void main(String[] args) throws Exception {
        NomaoiController app = new NomaoiController();
        int midiInPort = -1;
        if (args.length > 0) {
            if (args[0].equals("-d")) {
                app.dumpMidiDevices();
                return;
            }
            midiInPort = Integer.parseInt(args[0]);
        }
        app.setup(midiInPort);
        SwingUtilities.invokeLater(app);
    }
}

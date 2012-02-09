package nomaoi;

import java.awt.event.KeyListener;
import java.util.*;
import javax.sound.midi.*;

class NCModel implements AutoCloseable {
    private MidiDevice midiIn;
    private MidiDevice midiOut;
    private PCKeyboard keyboard = new PCKeyboard();
    private ShortMessage message = new ShortMessage();
    private int indexInst = 0;

    NCModel() {}

    KeyListener getKeyListener() {
        return keyboard;
    }

    int getIndexInst() {
        return indexInst;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public void setup(int indexMidiIn, int indexMidiOut, int indexInst)
            throws MidiUnavailableException {
        midiIn = findMidiInDevice(indexMidiIn);
        midiOut = findMidiOutDevice(indexMidiOut);
        Receiver recv = new AsSoonAsPossibleReceiver(midiOut.getReceiver());
        Transmitter trans = midiIn.getTransmitter();
        trans.setReceiver(recv);
        midiIn.open();
        midiOut.open();
        this.indexInst = (indexInst < 0) ? 0 : indexInst;
        programChange(this.indexInst);

        System.out.println("Input:");
        dumpMidiDevice(midiIn);
        System.out.println("Output:");
        dumpMidiDevice(midiOut);
        // System.out.println("Instruments:");
        // dumpInstruments(midiOut);
    }

    private MidiDevice findMidiInDevice(int index) throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        if (0 <= index && index < infos.length) {
            return MidiSystem.getMidiDevice(infos[index]);
        }
        if (index == infos.length) {
            return keyboard;
        }
        final String klass = "class com.sun.media.sound.MidiInDevice";
        for (int i = 0; i < infos.length; ++ i) {
            MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
            if (klass.equals(dev.getClass().toString())) {
                return dev;
            }
        }
        return keyboard;
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

    void programChange(int index) {
        indexInst = index;
        try {
            message.setMessage(ShortMessage.PROGRAM_CHANGE, 0, indexInst, 0);
            Receiver recv = midiOut.getReceiver();
            recv.send(message, -1);
        } catch (InvalidMidiDataException | MidiUnavailableException e) {
            e.printStackTrace();
        }
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

    @Override
    public void close() {
        if (midiIn != null) {
            midiIn.close();
        }
        if (midiOut != null) {
            midiOut.close();
        }
    }

    void resetMidiIn(String deviceName) {
        resetMidiDevice(deviceName, midiOut);
    }

    void resetMidiOut(String deviceName) {
        resetMidiDevice(deviceName, midiIn);
    }

    private void resetMidiDevice(String deviceName, MidiDevice dev) {
        int indexOut = extractIndexFromDeviceName(deviceName);
        int indexIn = getMidiDeviceIndex(dev);
        close();
        try {
            setup(indexIn, indexOut, indexInst);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    Vector<String> getMidiDeviceNames() {
        Vector<String> results = new Vector<>();
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; ++i) {
            results.add(getMidiDeviceName(i, infos[i]));
        }
        results.add("" + infos.length + ": " + keyboard.getDeviceInfo().getName());
        return results;
    }

    private String getMidiDeviceName(int index, MidiDevice.Info info) {
        StringBuilder buf = new StringBuilder();
        buf.append(index);
        buf.append(": ");
        buf.append(info.getName());
        final String inClass = "class com.sun.media.sound.MidiInDevice";
        final String outClass = "class com.sun.media.sound.MidiOutDevice";
        try {
            MidiDevice dev = MidiSystem.getMidiDevice(info);
            switch (dev.getClass().toString()) {
            case inClass:
                buf.append(" (IN)");
                break;
            case outClass:
                buf.append(" (OUT)");
                break;
            }
        } catch (MidiUnavailableException e) {
            // ignore
        }
        return buf.toString();
    }

    private int extractIndexFromDeviceName(String deviceName) {
        StringTokenizer tokens = new StringTokenizer(deviceName, ": \t");
        if (!tokens.hasMoreTokens()) {
            return -1;
        }
        try {
            return Integer.parseInt(tokens.nextToken());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    int getMidiDeviceIndexIn() {
        return getMidiDeviceIndex(midiIn);
    }

    int getMidiDeviceIndexOut() {
        return getMidiDeviceIndex(midiOut);
    }

    private int getMidiDeviceIndex(MidiDevice dev) {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; ++i) {
            try {
                MidiDevice d = MidiSystem.getMidiDevice(infos[i]);
                if (isEqualDevice(d, dev)) {
                    return i;
                }
            } catch (MidiUnavailableException e) {
                continue;
            }
        }
        if (isEqualDevice(keyboard, dev)) {
            return infos.length;
        }
        return -1;
    }

    private boolean isEqualDevice(MidiDevice d, MidiDevice e) {
        if (d == e) {
            return true;
        }
        if (d == null || e == null) {
            return false;
        }
        if (!d.getClass().equals(e.getClass())) {
            return false;
        }
        MidiDevice.Info di = d.getDeviceInfo();
        MidiDevice.Info ei = e.getDeviceInfo();
        return di.getName().equals(ei.getName())
            && di.getVendor().equals(ei.getVendor())
            && di.getVersion().equals(ei.getVersion());
    }

    Vector<String> getInstNames() {
        Vector<String> results = new Vector<>();
        if (!(midiOut instanceof Synthesizer)) {
            System.out.println("" + midiOut.getDeviceInfo() + " is not a Synthesizer.");
            return results;
        }
        Synthesizer synth = (Synthesizer)midiOut;
        Soundbank bank = synth.getDefaultSoundbank();
        if (bank == null) {
            System.out.println("" + midiOut + " does not have a SoundBank.");
            return results;
        }
        Instrument[] insts = synth.getDefaultSoundbank().getInstruments();
        for (int i = 0; i < insts.length; ++i) {
            results.add("" + i + ": " + insts[i].getName());
        }
        return results;
    }
}

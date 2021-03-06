package nomaoi;

import javax.sound.midi.*;

public class MetronomeModel implements MetaEventListener {
    private Sequencer sequencer;
    private int bpm;
    final int channel = 9;

    public MetronomeModel(int bpm) {
        this.bpm = bpm;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int value) {
        bpm = value;
    }

    public void start() {
        if (sequencer != null && sequencer.isRunning()) {
            return;
        }
        try {
            openSequencer();
            Sequence seq = createSequence();
            startSequence(seq);
        } catch (InvalidMidiDataException | MidiUnavailableException e) {
            e.printStackTrace(System.err);
        }
    }

    private void openSequencer() throws MidiUnavailableException {
        sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequencer.addMetaEventListener(this);
    }

    private Sequence createSequence() {
        try {
            Sequence seq = new Sequence(Sequence.PPQ, 1);
            Track track = seq.createTrack();
            addProgramChange(track, 0);
            addNoteOn(track, 0);
            addNoteOn(track, 1);
            addNoteOn(track, 2);
            addNoteOn(track, 3);
            addProgramChange(track, 4);
            return seq;
        } catch (InvalidMidiDataException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    private void addProgramChange(Track track, long tick)
    throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage(ShortMessage.PROGRAM_CHANGE,
                                                channel, 1, 0);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }

    private void addNoteOn(Track track, long tick)
    throws InvalidMidiDataException {
        final int instrument = 37;
        final int velocity = 100;
        ShortMessage message = new ShortMessage(ShortMessage.NOTE_ON, channel,
                                                instrument, velocity);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }

    private void startSequence(Sequence seq) throws InvalidMidiDataException {
        sequencer.setSequence(seq);
        sequencer.setTempoInBPM(bpm);
        sequencer.start();
    }

    public void stop() {
        if (sequencer == null) {
            return;
        }
        sequencer.stop();
    }

    @Override
    public void meta(MetaMessage message) {
        if (message.getType() != 47) {  // end of track
            return;
        }
        doLoop();
    }

    private void doLoop() {
        if (sequencer == null || !sequencer.isOpen()) {
            return;
        }
        sequencer.setTickPosition(0);
        sequencer.start();
        sequencer.setTempoInBPM(bpm);
    }

    public static void main(String[] args) throws Exception {
        int bpm = 60;
        if (args.length > 0) {
            try {
                bpm = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                bpm = 0;
            }
            if (bpm == 0)
                bpm = 60;
        }
        new MetronomeModel(bpm).start();
    }
}

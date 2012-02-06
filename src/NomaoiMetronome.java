package nomaoi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;

public class NomaoiMetronome implements MetaEventListener {
    private Sequencer sequencer;
    private int bpm;

    public void start(int bpm) {
        try {
            this.bpm = bpm;
            openSequencer();
            Sequence seq = createSequence();
            startSequence(seq);
        } catch (InvalidMidiDataException | MidiUnavailableException ex) {
            log(ex);
        }
    }

    private void log(Exception ex) {
        String classname = NomaoiMetronome.class.getName();
        Logger.getLogger(classname).log(Level.SEVERE, null, ex);
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
        } catch (InvalidMidiDataException ex) {
            log(ex);
            return null;
        }
    }

    private void addProgramChange(Track track, long tick)
    throws InvalidMidiDataException {
        ShortMessage message =
                new ShortMessage(ShortMessage.PROGRAM_CHANGE, 9, 1, 0);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }

    private void addNoteOn(Track track, long tick)
    throws InvalidMidiDataException {
        final int velocity = 100;
        ShortMessage message =
                new ShortMessage(ShortMessage.NOTE_ON, 9, 37, velocity);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }

    private void startSequence(Sequence seq) throws InvalidMidiDataException {
        sequencer.setSequence(seq);
        sequencer.setTempoInBPM(bpm);
        sequencer.start();
    }

    @Override
    public void meta(MetaMessage message) {
        if (message.getType() != 47) {  // 47 is end of track
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
        new NomaoiMetronome().start(bpm);
    }
}

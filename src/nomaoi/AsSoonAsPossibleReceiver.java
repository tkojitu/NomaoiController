package nomaoi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

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

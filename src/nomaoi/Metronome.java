package nomaoi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Metronome implements ActionListener, ChangeListener, Runnable {
    private MetronomeModel model;
    private JSpinner spinner;
    private JButton startButton;
    private JButton stopButton;

    public Metronome(int bpm) {
        model = new MetronomeModel(bpm);
    }

    private void createAndShowGui() {
        JFrame frame = new JFrame("Metronome");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createPane());
        frame.pack();
        frame.setVisible(true);
    }

    public JPanel createPane() {
        JPanel pane = new JPanel();
        pane.add(createSpinner());
        pane.add(createStartButton());
        pane.add(createStopButton());
        return pane;
    }

    private JSpinner createSpinner() {
        SpinnerModel spinModel = new SpinnerNumberModel(model.getBpm(), 1, 999, 1);
        spinner = new JSpinner(spinModel);
        spinner.addChangeListener(this);
        return spinner;
    }

    private JButton createStartButton() {
        startButton = new JButton("Start");
        startButton.addActionListener(this);
        return startButton;
    }

    private JButton createStopButton() {
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        return stopButton;
    }

    @Override
    public void run() {
        createAndShowGui();
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        int bpm = ((Integer)spinner.getValue()).intValue();
        model.setBpm(bpm);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == startButton) {
            model.start();
        } else if (src == stopButton) {
            model.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        int bpm = 60;
        if (args.length > 0) {
            try {
                bpm = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                bpm = 60;
            }
            if (bpm == 0) {
                bpm = 60;
            }
        }
        SwingUtilities.invokeLater(new Metronome(bpm));
    }
}

* NomaoiController

Connect MIDI input device to Gervill (Java default software synthesizer).

NomaoiController can find MIDI input device. You can play it:

  $ java -jar dist/lib/NomaoiController.jar

If you want to specify your device, use -d option that dump all MIDI devices:

  $ java -jar dist/lib/NomaoiController.jar -d

In my Ubuntu 11.10 PC:

  device#0
   class com.sun.media.sound.SoftSynthesizer
   Gervill
   Software MIDI Synthesizer
   OpenJDK
  device#1
   class com.sun.media.sound.MidiInDevice
   K32 [hw:1,0,0]
   Keystation Mini 32, USB MIDI, Keystation Mini 32
   ALSA (http://www.alsa-project.org)
  device#2
   class com.sun.media.sound.MidiOutDevice
   K32 [hw:1,0,0]
   Keystation Mini 32, USB MIDI, Keystation Mini 32
   ALSA (http://www.alsa-project.org)
  device#3
   class com.sun.media.sound.RealTimeSequencer
   Real Time Sequencer
   Software sequencer
   Oracle Corporation

My MIDI input device (M-Audio Keystation Mini 32) has #1. So:

  $ java -jar dist/lib/NomaoiController.jar 1

My development env:

  ASUS EeePC 901 (CPU: Intel Atom N270 1.6GHzx2, Memory: 2.0GB)
  Ubuntu 11.10
  JDK 1.7.0_01

In my env, NomaoiController has a bit latency. Because of CPU power?

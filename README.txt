* NomaoiController

Connect MIDI input device to Gervill (Java default software synthesizer).

NomaoiController can find MIDI input device. You can play it:

  $ java -jar dist/lib/NomaoiController.jar

* Specify MIDI Devices

NomaoiController dumps available MIDI devices into stdout.
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

You can specify MIDI devices with options.
My MIDI input device has #1. Gervile has #0. So:

  $ java -jar dist/lib/NomaoiController.jar -i 1 -o 0

* My Development Environment

ASUS EeePC 901 (CPU: Intel Atom N270 1.6GHzx2, Memory: 2.0GB)
Ubuntu 11.10
JDK 1.7.0_01
M-Audio Keystation Mini 32 (USB)

* Gervill

Gervill is slow. You may want to use another synthesizer.

In my WinPC, NomaoiController dumps this list:

  device#0
   class com.sun.media.sound.SoftSynthesizer
   Gervill
   Software MIDI Synthesizer
   OpenJDK
  device#1
   class com.sun.media.sound.MidiInDevice
   USB ?I?[?f?B?I
   No details available
   Unknown vendor
  device#2
   class com.sun.media.sound.MidiOutDevice
   Microsoft MIDI ?}?b
   Windows MIDI_MAPPER
   Unknown vendor
  device#3
   class com.sun.media.sound.MidiOutDevice
   Microsoft GS Wavetable SW Synth
   Internal software synthesizer
   Unknown vendor
  device#4
   class com.sun.media.sound.MidiOutDevice
   USB ?I?[?f?B?I
   External MIDI Port
   Unknown vendor
  device#5
   class com.sun.media.sound.RealTimeSequencer
   Real Time Sequencer
   Software sequencer
   Oracle Corporation

If I want to use MSGSWS:

  $ java -jar NomaoiController.jar -i 1 -o 3

MSGSWS is faster than Gervill.

In my WinPC (WinXP), NomaoiController works fine (JDK 1.7.0_02).

* Copyright?

NomaoiController is public domain.

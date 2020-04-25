package com.novation;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;

public class LaunchpadMiniMK3Extension extends ControllerExtension
{
   static final String SYSEX_PREFIX = "f0002029020d";
   static final String SYSEX_SUFFIX = "f7";
   static final String SYSEX_SET_DAW_MODE    = sysex("1001");
   static final String SYSEX_CLEAR_DAW_MODE  = sysex("12000000");
   static final String SYSEX_SESSION_LAYOUT  = sysex("0000");
   static final String SYSEX_EXIT            = sysex("1000");

   static String sysex(String msg) {
      return SYSEX_PREFIX + msg + SYSEX_SUFFIX;
   }

   Transport transport;
   MidiIn sessionMidiIn;
   MidiOut sessionMidiOut;
   
   MidiIn defaultModeMidiIn;
   NoteInput customModesNoteInput;

   protected LaunchpadMiniMK3Extension(final LaunchpadMiniMK3ExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();      

      transport = host.createTransport();

      sessionMidiIn = host.getMidiInPort(0);
      sessionMidiIn.setMidiCallback((statusByte, data1, data2) -> {
         host.println(String.format("sessionMidiIn midi: %d %d %d", statusByte, data1, data2));
      });
      sessionMidiIn.setSysexCallback(data -> {
         host.println(String.format("sessionMidiIn sysex: %s", data));
      });

      defaultModeMidiIn = host.getMidiInPort(1);
      defaultModeMidiIn.setMidiCallback((statusByte, data1, data2) -> {
         host.println(String.format("defaultModeMidiIn midi: %d %d %d", statusByte, data1, data2));
      });
      defaultModeMidiIn.setSysexCallback(data -> {
         host.println(String.format("defaultModeMidiIn sysex: %s", data));
      });

      customModesNoteInput = defaultModeMidiIn.createNoteInput("Default Input", "??????");
      customModesNoteInput.setShouldConsumeEvents(false);
      
      sessionMidiOut = host.getMidiOutPort(0);
      // Set to DAW mode
      sessionMidiOut.sendSysex(SYSEX_SET_DAW_MODE);
      // Clear Daw mode
      sessionMidiOut.sendSysex(SYSEX_CLEAR_DAW_MODE);
      // Swap to session mode
      sessionMidiOut.sendSysex(SYSEX_SESSION_LAYOUT);

//      // Set to programmer's mode
//      midiOut_0.sendSysex(SYSEX_HEADER + "0e01f7");
//      midiOut_0.sendSysex(SYSEX_HEADER + "007ff7");
      
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("Launchpad Mini MK3 Initialized");
      host.println("Launchpad Mini MK3 Initialized");
   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      getHost().showPopupNotification("Launchpad Mini MK3 Exited");

      sessionMidiOut.sendSysex(SYSEX_EXIT);
      transport = null;
      sessionMidiIn = null;
      defaultModeMidiIn = null;
      sessionMidiOut = null;
   }

   @Override
   public void flush()
   {
      // TODO Send any updates you need here.
   }


}

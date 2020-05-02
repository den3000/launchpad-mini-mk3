package com.novation.LaunchpadMiniMK3;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;

public class LaunchpadMiniMK3Extension extends ControllerExtension
{
   Transport transport;
   MidiIn sessionMidiIn;
   MidiOut sessionMidiOut;
   
   MidiIn defaultModeMidiIn;
   NoteInput customModesNoteInput;

   Page current = Page.session;
   boolean isInProgrammersMode = false;

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

         if (TopRow.isTopRow(data1)) {
            if (TopRow.isArrow(data1)) { navigate(data1, data2); }
            else changeLayout(data1, data2);
         } else {
            host.println(String.format("sessionMidiIn midi: %d %d %d", statusByte, data1, data2));
         }
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
      switchToDawMode();

      //      // Set to programmer's mode
//      midiOut_0.sendSysex(SYSEX_HEADER + "0e01f7");
//      midiOut_0.sendSysex(SYSEX_HEADER + "007ff7");
      
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("Launchpad Mini MK3 Initialized");
      host.println("Launchpad Mini MK3 Initialized");
   }

   private void switchToDawMode() {
      getHost().println("switchToDawMode");
      // Set to DAW mode
      sessionMidiOut.sendSysex(Sysex.SET_DAW_MODE);
      // Clear Daw mode
      sessionMidiOut.sendSysex(Sysex.CLEAR_DAW_MODE);
      // Swap to session mode
      sessionMidiOut.sendSysex(Sysex.SESSION_LAYOUT);
      isInProgrammersMode = false;
   }

   private void switchToProgrammersMode() {
      getHost().println("switchToProgrammersMode");
      // Set to Programmer's Mode
      sessionMidiOut.sendSysex(Sysex.SET_DAW_MODE);
      // Clear Daw mode
      sessionMidiOut.sendSysex(Sysex.CLEAR_DAW_MODE);
      // Swap to session mode
      sessionMidiOut.sendSysex(Sysex.PROGRAMMERS_LAYOUT);
      sessionMidiOut.sendSysex(Sysex.LIVE_MODE_ON);

      isInProgrammersMode = true;
   }

   private void changeLayout(int data1, int data2) {
      TopRow topRow = TopRow.from(data1);
      PressState pressState = PressState.from(data2);
      if (pressState == PressState.down) {
         getHost().println("Layout:" + topRow.toString() + String.format(" sessionMidiIn midi: %d %d", data1, data2));

         if (!isInProgrammersMode
                 && (topRow == TopRow.drums && current == Page.userDrums
                 || topRow == TopRow.keys && current == Page.userKeys
                 || topRow == TopRow.user && current == Page.user)) {
            switchToProgrammersMode();
            current = Page.custom;
            return;
         }

         if (isInProgrammersMode
                 && (topRow == TopRow.drums || topRow == TopRow.keys || topRow == TopRow.user)) {
            switchToDawMode();
            current = Page.session;
            return;
         }

         if (topRow == TopRow.session) { current = Page.session; }
         else if (topRow == TopRow.drums) { current = Page.userDrums; }
         else if (topRow == TopRow.keys) { current = Page.userKeys; }
         else if (topRow == TopRow.user) { current = Page.user; }
      }
   }

   private void navigate(int data1, int data2) {
      TopRow arrow = TopRow.from(data1);
      PressState pressState = PressState.from(data2);
      if (pressState == PressState.down) {
         getHost().println("Arrow:" + arrow.toString() + String.format(" sessionMidiIn midi: %d %d", data1, data2));
      }
   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      getHost().showPopupNotification("Launchpad Mini MK3 Exited");

      sessionMidiOut.sendSysex(Sysex.EXIT);
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

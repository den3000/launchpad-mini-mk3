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
      sessionMidiIn.setMidiCallback((statusByte, midiNote, pressStateData) -> {

         if (TopRow.isTopRow(midiNote)) {
            if (TopRow.isArrow(midiNote)) { navigate(midiNote, pressStateData); }
            else changeLayout(midiNote, pressStateData);
         } else {
            if (isInProgrammersMode) {
               executeSessionAction(midiNote, pressStateData);
            } else {
               executeCustomAction(midiNote, pressStateData);
            }
            host.println(String.format("sessionMidiIn midi: %d %d %d", statusByte, midiNote, pressStateData));
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
      switchToDawMode(current);

      //      // Set to programmer's mode
//      midiOut_0.sendSysex(SYSEX_HEADER + "0e01f7");
//      midiOut_0.sendSysex(SYSEX_HEADER + "007ff7");
      
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("Launchpad Mini MK3 Initialized");
      host.println("Launchpad Mini MK3 Initialized");
   }

   private void executeSessionAction(int data1, int data2) {

   }

   private void executeCustomAction(int data1, int data2) {

   }

   private void switchToDawMode(Page page) {
      getHost().println("switchToDawMode");
      // Set to DAW mode
      sessionMidiOut.sendSysex(Sysex.SET_DAW_MODE);
      // Clear Daw mode
      sessionMidiOut.sendSysex(Sysex.CLEAR_DAW_MODE);

      // Swap to necessary layout
      switch (page) {
         case session: sessionMidiOut.sendSysex(Sysex.SESSION_LAYOUT); break;
         case userDrums: sessionMidiOut.sendSysex(Sysex.SESSION_DRUMS); break;
         case userKeys: sessionMidiOut.sendSysex(Sysex.SESSION_KEYS); break;
         case user: sessionMidiOut.sendSysex(Sysex.SESSION_USER); break;
      }

      if (page == Page.session) {
         getHost().println("Reset colors");
         setNoColor(sessionMidiOut, 0x0B);
         setNoColor(sessionMidiOut, 0x51);
         setNoColor(sessionMidiOut, 0x12);
      }

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

      setStaticColor(sessionMidiOut, 0x0B, 0x05);
      setFlashingColor(sessionMidiOut, 0x51, 0x13);
      setPulsingColor(sessionMidiOut, 0x12, 0x2D);

      isInProgrammersMode = true;
   }

   private void changeLayout(int midiNoteData, int pressStateData) {
      TopRow topRow = TopRow.from(midiNoteData);
      PressState pressState = PressState.from(pressStateData);
      if (pressState == PressState.down) {
         getHost().println("Layout:" + topRow.toString() + String.format(" sessionMidiIn midi: %d %d", midiNoteData, pressStateData));

         if (!isInProgrammersMode
                 && (topRow == TopRow.drums && current == Page.userDrums
                 || topRow == TopRow.keys && current == Page.userKeys
                 || topRow == TopRow.user && current == Page.user)) {
            switchToProgrammersMode();
            current = Page.custom;
            return;
         }

         if (topRow == TopRow.session) { current = Page.session; }
         else if (topRow == TopRow.drums) { current = Page.userDrums; }
         else if (topRow == TopRow.keys) { current = Page.userKeys; }
         else if (topRow == TopRow.user) { current = Page.user; }

         getHost().println("Selected page: " + current.toString() );
         if (isInProgrammersMode && (topRow == TopRow.drums ||
                 topRow == TopRow.keys ||
                 topRow == TopRow.user ||
                 topRow == TopRow.session)) {
            switchToDawMode(current);
         }

      }
   }

   private void navigate(int midiNoteData, int pressStateData) {
      TopRow arrow = TopRow.from(midiNoteData);
      PressState pressState = PressState.from(pressStateData);
      if (pressState == PressState.down) {
         getHost().println("Arrow:" + arrow.toString() + String.format(" sessionMidiIn midi: %d %d", midiNoteData, pressStateData));
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

   static void setStaticColor(MidiOut midiOut, int padNum, int colorPalletIndex) {
         midiOut.sendMidi(0x90, padNum, colorPalletIndex);
   }

   static void setFlashingColor(MidiOut midiOut, int padNum, int colorPalletIndex) {
         midiOut.sendMidi(0x91, padNum, colorPalletIndex);
   }

   static void setPulsingColor(MidiOut midiOut, int padNum, int colorPalletIndex) {
         midiOut.sendMidi(0x92, padNum, colorPalletIndex);
   }

   static void setNoColor(MidiOut midiOut, int padNum) {
         midiOut.sendMidi(0x80, padNum, 0x00);
   }
}

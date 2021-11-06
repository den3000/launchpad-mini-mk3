package com.novation.LaunchpadMiniMK3;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;
import com.novation.arch.PageType;
import com.novation.arch.TransportPage;
import com.novation.hal.Pad;
import com.novation.hal.PressState;
import com.novation.hal.Sysex;
import com.novation.hal.TopRow;

import java.util.ArrayList;

public class LaunchpadMiniMK3Extension extends ControllerExtension
{
   Transport transport;
   MidiIn sessionMidiIn;
   MidiOut sessionMidiOut;
   
   MidiIn defaultModeMidiIn;
   NoteInput customModesNoteInput;

   TransportPage transportPage;
   TransportHal transportHal;

   Pad padClick = Pad.controlPad(Pad.PadType.click);
   Pad padLoop = Pad.controlPad(Pad.PadType.loop);
   Pad padRec = Pad.controlPad(Pad.PadType.rec);
   Pad padPlay = Pad.controlPad(Pad.PadType.play);

   Pad padClickPlus_0_1 = Pad.regularPad(5,8);
   Pad padClickPlus_1 = Pad.regularPad(6,8);
   Pad padClickPlus_5 = Pad.regularPad(7,8);
   Pad padClickPlus_10 = Pad.regularPad(8,8);
   Pad padClickMinus_0_1 = Pad.regularPad(4,8);
   Pad padClickMinus_1 = Pad.regularPad(3,8);
   Pad padClickMinus_5 = Pad.regularPad(2,8);
   Pad padClickMinus_10 = Pad.regularPad(1,8);
   ArrayList<Integer> tempoChangeNotes = new ArrayList<Integer>();

   PageType current = PageType.session;
   boolean isInProgrammersMode = false;

   protected LaunchpadMiniMK3Extension(final LaunchpadMiniMK3ExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();      

      tempoChangeNotes.add(padClickMinus_10.note());
      tempoChangeNotes.add(padClickMinus_5.note());
      tempoChangeNotes.add(padClickMinus_1.note());
      tempoChangeNotes.add(padClickMinus_0_1.note());
      tempoChangeNotes.add(padClickPlus_0_1.note());
      tempoChangeNotes.add(padClickPlus_1.note());
      tempoChangeNotes.add(padClickPlus_5.note());
      tempoChangeNotes.add(padClickPlus_10.note());

      transport = host.createTransport();
      // Looks like we need to have at least
      // one observer to be able to use transport
      transport.isPlaying().addValueObserver(newValue -> {
         if (isInProgrammersMode) {
            if (newValue) {
               pulseTransportButtons();
            } else {
               showTransportButtons();
            }
         }
      });

      transport.isMetronomeEnabled().addValueObserver(newValue -> {
         if (isInProgrammersMode) {
            toggleClickButton();
         }
      });

      transport.isArrangerLoopEnabled().addValueObserver(newValue -> {
         if (isInProgrammersMode) {
            toggleLoopButton();
         }
      });

      transport.tempo().value().addValueObserver(value -> {
         double tempoInBpm = value * 646 + 20;
         getHost().println(String.format("tempoValue: %f bpm: %f", value, tempoInBpm));
      });

      sessionMidiIn = host.getMidiInPort(0);
      sessionMidiIn.setMidiCallback((statusByte, midiNote, pressStateData) -> {

         if (TopRow.isTopRow(midiNote)) {
            if (TopRow.isArrow(midiNote)) { navigate(midiNote, pressStateData); }
            else changeLayout(midiNote, pressStateData);
         } else {
            if (isInProgrammersMode) {
               executeCustomAction(midiNote, pressStateData);
            } else {
               executeSessionAction(midiNote, pressStateData);
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

   private void executeCustomAction(int midiNoteData, int pressStateData) {
      PressState pressState = PressState.from(pressStateData);
      if (pressState == PressState.down) {
         if (midiNoteData == padPlay.note()) {
            getHost().println("play");
            if (transport.isPlaying().get()) {
               transport.stop();
               showTransportButtons();
            } else {
               transport.play();
               pulseTransportButtons();
            }
         } else if (midiNoteData == padRec.note()) {
            getHost().println("record");
            if (transport.isPlaying().get()) {
               transport.stop();
               showTransportButtons();
            } else {
               transport.record();
               pulseTransportButtons();
            }
         } else if (midiNoteData == padLoop.note()) {
            transport.isArrangerLoopEnabled().toggle();
         } else if (midiNoteData == padClick.note()) {
            transport.isMetronomeEnabled().toggle();
         } else if (tempoChangeNotes.contains(midiNoteData)){
            processTempoChange(midiNoteData);
         }
      }
   }

   private void processTempoChange(int midiNoteData) {
      // TODO: this if else is horrible
      /*
      20 bpm - 0
      0.1 bpm - 0.00015
      666 bpm - 1.0
      */

      double currentTempo = transport.tempo().get();
      double currentBpm = currentTempo * 646 + 20;
      double newBpm = currentBpm;

      if (midiNoteData == padClickMinus_10.note()) {
         newBpm = currentBpm - 10;
      } else if (midiNoteData == padClickMinus_5.note()) {
         newBpm = currentBpm - 5;
      } else if (midiNoteData == padClickMinus_1.note()) {
         newBpm = currentBpm - 1;
      } else if (midiNoteData == padClickMinus_0_1.note()) {
         newBpm = currentBpm - 0.1;
      } else if (midiNoteData == padClickPlus_0_1.note()) {
         newBpm = currentBpm + 0.1;
      } else if (midiNoteData == padClickPlus_1.note()) {
         newBpm = currentBpm + 1;
      } else if (midiNoteData == padClickPlus_5.note()) {
         newBpm = currentBpm + 5;
      } else if (midiNoteData == padClickPlus_10.note()) {
         newBpm = currentBpm + 10;
      }

      double newTempo = (newBpm - 20) / 646;
      if (newTempo > 0 && newTempo < 1.0) {
         transport.tempo().set(newTempo);
      }
   }

   private void showTransportButtons() {
      toggleLoopButton();
      toggleClickButton();
      setStaticColor(sessionMidiOut, padRec, 0x05);
      setStaticColor(sessionMidiOut, padPlay, 0x7A);
   }

   private void showTempoChangeButtons() {
      setStaticColor(sessionMidiOut, padClickPlus_0_1, 0x14);
      setStaticColor(sessionMidiOut, padClickPlus_1, 0x15);
      setStaticColor(sessionMidiOut, padClickPlus_5, 0x16);
      setStaticColor(sessionMidiOut, padClickPlus_10, 0x17);

      setStaticColor(sessionMidiOut, padClickMinus_0_1, 0x14);
      setStaticColor(sessionMidiOut, padClickMinus_1, 0x15);
      setStaticColor(sessionMidiOut, padClickMinus_5, 0x16);
      setStaticColor(sessionMidiOut, padClickMinus_10, 0x17);
   }

   private void pulseTransportButtons() {
      setFlashingColor(sessionMidiOut, padRec, 0x0D);
      setFlashingColor(sessionMidiOut, padPlay, 0x0D);
   }

   private void hideTransportButtons() {
      setNoColor(sessionMidiOut, padClick);
      setNoColor(sessionMidiOut, padLoop);
      setNoColor(sessionMidiOut, padRec);
      setNoColor(sessionMidiOut, padPlay);
   }

   private void hideTempoChangeButtons() {
      setNoColor(sessionMidiOut, padClickPlus_0_1);
      setNoColor(sessionMidiOut, padClickPlus_1);
      setNoColor(sessionMidiOut, padClickPlus_5);
      setNoColor(sessionMidiOut, padClickPlus_10);

      setNoColor(sessionMidiOut, padClickMinus_0_1);
      setNoColor(sessionMidiOut, padClickMinus_1);
      setNoColor(sessionMidiOut, padClickMinus_5);
      setNoColor(sessionMidiOut, padClickMinus_10);
   }

   private void toggleLoopButton() {
      setStaticColor(sessionMidiOut, padLoop, transport.isArrangerLoopEnabled().get() ? 0x2D : 0x2F);
   }

   private void toggleClickButton() {
      setStaticColor(sessionMidiOut, padClick, transport.isMetronomeEnabled().get() ? 0x39 : 0x37);
   }

   private void switchToDawMode(PageType pageType) {
      getHost().println("switchToDawMode");
      // Set to DAW mode
      sessionMidiOut.sendSysex(Sysex.SET_DAW_MODE);
      // Clear Daw mode
      sessionMidiOut.sendSysex(Sysex.CLEAR_DAW_MODE);

      // Swap to necessary layout
      switch (pageType) {
         case session: sessionMidiOut.sendSysex(Sysex.SESSION_LAYOUT); break;
         case userDrums: sessionMidiOut.sendSysex(Sysex.SESSION_DRUMS); break;
         case userKeys: sessionMidiOut.sendSysex(Sysex.SESSION_KEYS); break;
         case user: sessionMidiOut.sendSysex(Sysex.SESSION_USER); break;
      }

      if (pageType == PageType.session) {
         getHost().println("Reset colors");
         hideTransportButtons();
         hideTempoChangeButtons();
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

      if (transport.isPlaying().get()) {
         pulseTransportButtons();
      } else {
         showTransportButtons();
      }
      showTempoChangeButtons();

      isInProgrammersMode = true;
   }

   private void changeLayout(int midiNoteData, int pressStateData) {
      TopRow topRow = TopRow.from(midiNoteData);
      PressState pressState = PressState.from(pressStateData);
      if (pressState == PressState.down) {
         getHost().println("Layout:" + topRow.toString() + String.format(" sessionMidiIn midi: %d %d", midiNoteData, pressStateData));

         if (!isInProgrammersMode
                 && (topRow == TopRow.drums && current == PageType.userDrums
                 || topRow == TopRow.keys && current == PageType.userKeys
                 || topRow == TopRow.user && current == PageType.user)) {
            switchToProgrammersMode();
            current = PageType.custom;
            return;
         }

         if (topRow == TopRow.session) { current = PageType.session; }
         else if (topRow == TopRow.drums) { current = PageType.userDrums; }
         else if (topRow == TopRow.keys) { current = PageType.userKeys; }
         else if (topRow == TopRow.user) { current = PageType.user; }

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

   static void setStaticColor(MidiOut midiOut, Pad pad, int colorPalletIndex) {
      // if pad is control then send as CC else send as NOTE
      midiOut.sendMidi(pad.isControl() ? 0xB0 : 0x90, pad.note(), colorPalletIndex);
   }

   static void setFlashingColor(MidiOut midiOut, Pad pad, int colorPalletIndex) {
      // if pad is control then send as CC else send as NOTE
      midiOut.sendMidi(pad.isControl() ? 0xB1 : 0x91, pad.note(), colorPalletIndex);
   }

   static void setPulsingColor(MidiOut midiOut, Pad pad, int colorPalletIndex) {
      // if pad is control then send as CC else send as NOTE
      midiOut.sendMidi(pad.isControl() ? 0xB2 : 0x92, pad.note(), colorPalletIndex);
   }

   static void setNoColor(MidiOut midiOut, Pad pad) {
      // if pad is control then send as CC else send as NOTE
      midiOut.sendMidi(pad.isControl() ? 0xB0 : 0x90, pad.note(), 0x00);
   }
}

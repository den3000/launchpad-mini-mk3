package com.novation.LaunchpadMiniMK3;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class LaunchpadMiniMK3ExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("a9a82eb4-f862-49a3-9910-af4f4f97b8ce");

   public LaunchpadMiniMK3ExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "Launchpad Mini MK3";
   }
   
   @Override
   public String getAuthor()
   {
      return "den_3k";
   }

   @Override
   public String getVersion()
   {
      return "0.1";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }
   
   @Override
   public String getHardwareVendor()
   {
      return "Novation";
   }
   
   @Override
   public String getHardwareModel()
   {
      return "Launchpad Mini MK3";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 10;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 2;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 2;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
   }

   @Override
   public LaunchpadMiniMK3Extension createInstance(final ControllerHost host)
   {
      return new LaunchpadMiniMK3Extension(this, host);
   }
}

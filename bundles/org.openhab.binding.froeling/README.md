# Froeling Binding

The Froeling binding is used for reading data from Froeling furnace controllers.

## Supported Bridges

Serial-LAN converter

## Supported Things

Froeling P3200 controller (Bridge connected to COM1)

## Discovery

Discovery is not implemented.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The Serial-LAN converter bridge needs ip address and telnet port of the Serial-LAN converter.
The Froeling controller thing needs controller type, COM-port of the Froeling furnace controller and a polling interval.

## Channels

* **thing** `froeling`
    * **channel** `status` (String)
    * **channel** `furnacetemperature-current` (Number)
    * **channel** `exhaustgastemperature-current` (Number)
    * **channel** `furnacecontrolvariable` (Number)
    * **channel** `primaryair` (Number)
    * **channel** `remainoxygen` (Number)
    * **channel** `oxygencontroller` (Number)
    * **channel** `secondaryair` (Number)
    * **channel** `idfan-setpoint` (Number)
    * **channel** `idfan-current` (Number)
    * **channel** `exhaustgastemperature-setpoint` (Number)
    * **channel** `slidein-current` (Number)
    * **channel** `pellet` (Number)
    * **channel** `fillinglevel` (Number)
    * **channel** `intakespeed` (Number)
    * **channel** `deliverypower` (Number)
    * **channel** `sensor-1` (Number)
    * **channel** `furnacetemperature-setpoint` (Number)
    * **channel** `sensor-buffertop` (Number)
    * **channel** `sensor-bufferbottom` (Number)
    * **channel** `bufferpump` (Number)
    * **channel** `sensor-boiler` (Number)
    * **channel** `sensor-flow1` (Number)
    * **channel** `sensor-flow2` (Number)
    * **channel** `heatingcircuitpump1` (Number)
    * **channel** `heatingcircuitpump2` (Number)
    * **channel** `outdoortemperature` (Number)
    * **channel** `collectortemperature` (Number)
    * **channel** `operatinghours` (Number)
    * **channel** `error` (String)
    * **channel** `lastupdate` (DateTime)

## Full Example

Items:
```
String FROELING_Status                  "Status: [%s]"                      <icon>  (Froeling) { channel="froeling:controller:812d9fcb:status" }
String FROELING_Error                   "Error: [%s]"                       <icon>  (Froeling) { channel="froeling:controller:812d9fcb:error" }
Number FROELING_Exhaustgastemperature   "Exhaustgastemperature: [%d °C]"    <icon>  (Froeling) { channel="froeling:controller:812d9fcb:exhaustgastemperature-current" }
Number FROELING_Furnacetemperature      "Furnacetemperature: [%d °C]"       <icon>  (Froeling) { channel="froeling:controller:812d9fcb:furnacetemperature-current" }
Number FROELING_Slidein_current         "Slidein: [%d %%]"                  <icon>  (Froeling) { channel="froeling:controller:812d9fcb:slidein-current" }
DateTime FROELING_Lastupdate "Last update: [%1$tY-%1$tm-%1$td %1$tT]" <clock> (Froeling) { channel="froeling:controller:812d9fcb:lastupdate" }
```
812d9fcb = Controller ID as shown in PaperUI

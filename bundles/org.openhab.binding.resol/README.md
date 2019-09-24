# Resol Binding

The Resol binding is used for reading data from Resol solar controllers.

## Supported Bridges

VBus-LAN converter

## Supported Things

Following Resol controllers connected via VBus-LAN converter:
4214 - Sonnenkraft SKSC2
427B - DeltaSol BS 2009

## Discovery

Discovery is not implemented.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The VBus-LAN converter bridge needs ip address of the VBus-LAN converter.
The Resol controller thing needs just a polling interval.

## Channels

* **thing** `resol`
    * **channel** `temperature-s1` (Number)
    * **channel** `temperature-s2` (Number)
    * **channel** `temperature-s3` (Number)
    * **channel** `temperature-s4` (Number)
    * **channel** `temperature-vfd1` (Number)
    * **channel** `volumetricflowrate-vfd1` (Number)
    * **channel** `speed-relais1` (Number)
    * **channel** `speed-relais2` (Number)
    * **channel** `voltage-10v` (Number)
    * **channel** `errormask` (String)
    * **channel** `operatinghours-relais1` (Number)
    * **channel** `operatinghours-relais2` (Number)
    * **channel** `heatsupplied` (Number)
    * **channel** `sw-version` (String)
    * **channel** `variant` (String)
    * **channel** `unit-type` (String)
    * **channel** `system` (String)
    * **channel** `system-time` (String)
    * **channel** `s1-broken` (Number)
    * **channel** `s2-broken` (Number)
    * **channel** `s3-broken` (Number)
    * **channel** `s4-broken` (Number)
    * **channel** `statusmask` (String)
    * **channel** `lastupdate` (DateTime)

NOTE: Not every controller supports all Channels!

## Example

Items:
```
Number RESOL_TemperatureS1 "Temperature S1: [%.1f °C]" <temperature> (Resol) { channel="resol:controller:6d5fc064:temperature-s1" }
Number RESOL_TemperatureS2 "Temperature S2: [%.1f °C]" <temperature> (Resol) { channel="resol:controller:6d5fc064:temperature-s2" }
Number RESOL_TemperatureS3 "Temperature S3: [%.1f °C]" <temperature> (Resol) { channel="resol:controller:6d5fc064:temperature-s3" }
Number RESOL_TemperatureS4 "Temperature S4: [%.1f °C]" <temperature> (Resol) { channel="resol:controller:6d5fc064:temperature-s4" }
Number RESOL_TemperatureVFD1 "Temperature VFD1: [%.1f °C]" <temperature> (Resol) { channel="resol:controller:6d5fc064:temperature-vfd1" }
Number RESOL_VolumetricflowrateVFD1 "Volumetric flow rate VFD1: [%.1f l/h]" <settings> (Resol, Resol_Chart) { channel="resol:controller:6d5fc064:volumetricflowrate-vfd1" }
Number RESOL_Speedrelais1 "Speed relais 1: [%.1f %%]" <settings> (Resol) { channel="resol:controller:6d5fc064:speed-relais1" }
Number RESOL_Speedrelais2 "Speed relaus 2: [%.1f %%]" <settings> (Resol) { channel="resol:controller:6d5fc064:speed-relais2" }
Number RESOL_Voltage10V "Voltage: [%.1f V]" <settings> (Resol) { channel="resol:controller:6d5fc064:voltage-10v" }
String RESOL_Errormask "Error mask: [%s]" <error> (Resol) { channel="resol:controller:6d5fc064:errormask" }
Number RESOL_OperatinghoursRelais1 "Operating hours relais 1: [%.1f h]" <settings> (Resol, Resol_Chart) { channel="resol:controller:6d5fc064:operatinghours-relais1" }
Number RESOL_OperatinghoursRelais2 "Operating hours relais 2: [%.1f h]" <settings> (Resol) { channel="resol:controller:6d5fc064:operatinghours-relais2" }
Number RESOL_Heatsupplied "Heat supplied: [%.1f Wh]" <fire> (Resol) { channel="resol:controller:6d5fc064:heatsupplied" }
DateTime RESOL_Update "Last update: [%1$tY-%1$tm-%1$td %1$tT]" <clock> (Resol) { channel="resol:controller:6d5fc064:lastupdate" }
```
6d5fc064 = Controller ID as shown in PaperUI

/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.froeling.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link froelingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Hillenbrand - Initial contribution
 */
@NonNullByDefault
public class froelingBindingConstants {

    public static final String BINDING_ID = "froeling";

    // Bridge Type UIDs
    public final static ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");

    // List of all Channel ids
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_FURNACETEMPERATURE_CURRENT = "furnacetemperature-current";
    public final static String CHANNEL_EXHAUSTTEMPERATURE_CURRENT = "exhaustgastemperature-current";
    public final static String CHANNEL_FURNACECONTROLVARIABLE = "furnacecontrolvariable";
    public final static String CHANNEL_PRIMARYAIR = "primaryair";
    public final static String CHANNEL_REMAINOXYGEN = "remainoxygen";
    public final static String CHANNEL_OXYGENCONTROLLER = "oxygencontroller";
    public final static String CHANNEL_SECONDARYAIR = "secondaryair";
    public final static String CHANNEL_IDFAN_SETPOINT = "idfan-setpoint";
    public final static String CHANNEL_IDFAN_CURRENT = "idfan-current";
    public final static String CHANNEL_EXHAUSTTEMPERATURE_SETPOINT = "exhaustgastemperature-setpoint";
    public final static String CHANNEL_SLIDEIN_CURRENT = "slidein-current";
    public final static String CHANNEL_PELLET = "pellet";
    public final static String CHANNEL_FILLING_LEVEL = "fillinglevel";
    public final static String CHANNEL_INTAKESPEED = "intakespeed";
    public final static String CHANNEL_DELIVERYPOWER = "deliverypower";
    public final static String CHANNEL_SENSOR_1 = "sensor-1";
    public final static String CHANNEL_FURNACETEMPERATURE_SETPOINT = "furnacetemperature-setpoint";
    public final static String CHANNEL_SENSOR_BUFFERTOP = "sensor-buffertop";
    public final static String CHANNEL_SENSOR_BUFFERBOTTOM = "sensor-bufferbottom";
    public final static String CHANNEL_BUFFER_PUMP = "bufferpump";
    public final static String CHANNEL_SENSOR_BOILER = "sensor-boiler";
    public final static String CHANNEL_SENSOR_FLOW_1 = "sensor-flow1";
    public final static String CHANNEL_SENSOR_FLOW_2 = "sensor-flow2";
    public final static String CHANNEL_HEATINGCIRCUITPUMP_1 = "heatingcircuitpump1";
    public final static String CHANNEL_HEATINGCIRCUITPUMP_2 = "heatingcircuitpump2";
    public final static String CHANNEL_OUTDOORTEMPERATURE = "outdoortemperature";
    public final static String CHANNEL_COLLECTORTEMPERATURE = "collectortemperature";
    public final static String CHANNEL_OPERATINGHOURS = "operatinghours";
    public final static String CHANNEL_ERRORS = "error";
    public final static String CHANNEL_LASTUPDATE = "lastupdate";
}

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
package org.openhab.binding.resol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link resolBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Hillenbrand - Initial contribution
 */
@NonNullByDefault
public class resolBindingConstants {

    public static final String BINDING_ID = "resol";

    // Bridge Type UIDs
    public final static ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");

    // List of all Channel ids
    public final static String TEMPERATURE_S1 = "temperature-s1";
    public final static String TEMPERATURE_S2 = "temperature-s2";
    public final static String TEMPERATURE_S3 = "temperature-s3";
    public final static String TEMPERATURE_S4 = "temperature-s4";
    public final static String TEMPERATURE_VFD1 = "temperature-vfd1";
    public final static String VOLUMETRICFLOWRATE_VFD1 = "volumetricflowrate-vfd1";
    public final static String SPEED_RELAIS1 = "speed-relais1";
    public final static String SPEED_RELAIS2 = "speed-relais2";
    public final static String VOLTAGE_10V = "voltage-10v";
    public final static String ERRORMASK = "errormask";
    public final static String OPERATINGHOURS_RELAIS1 = "operatinghours-relais1";
    public final static String OPERATINGHOURS_RELAIS2 = "operatinghours-relais2";
    public final static String HEATSUPPLIED = "heatsupplied";
    public final static String SW_VERSION = "sw-version";
    public final static String VARIANT = "variant";
    public final static String UNIT_TYPE = "unit-type";
    public final static String SYSTEM = "system";
    public final static String SYSTEM_TIME = "system-time";
    public final static String S1_BROKEN = "s1-broken";
    public final static String S2_BROKEN = "s2-broken";
    public final static String S3_BROKEN = "s3-broken";
    public final static String S4_BROKEN = "s4-broken";
    public final static String STATUSMASK = "statusmask";

    public final static String CHANNEL_LASTUPDATE = "lastupdate";
}

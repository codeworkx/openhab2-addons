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
package org.openhab.binding.froeling.handler;

import static org.openhab.binding.froeling.internal.froelingBindingConstants.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.froeling.internal.config.FroelingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link froelingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Hillenbrand - Initial contribution
 */
@NonNullByDefault
public class froelingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(froelingHandler.class);
    private @Nullable FroelingConfiguration config;
    private IPBridgeHandler bridgeHandler = null;

    public froelingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: handle command
    }

    @Override
    public void initialize() {
        config = getConfigAs(FroelingConfiguration.class);
        getIPBridgeHandler();

        updateStatus(ThingStatus.ONLINE);
        PollingSchedularService pSS = new PollingSchedularService();
        try {
            this.logger.debug("Entering pollingLoop");
            pSS.pollingLoop();
        } catch (Exception e) {
            this.logger.error("Error while polling:", e);
        }
    }

    @NonNullByDefault
    public class PollingSchedularService {
        FroelingConfiguration config = getThing().getConfiguration().as(FroelingConfiguration.class);
        private Logger logger = LoggerFactory.getLogger(PollingSchedularService.class);

        public void pollingLoop() throws Exception {
            long initialDelay = 10;
            long pollingInterval = config.getPollingInterval().longValue();

            if (pollingInterval < 5 || pollingInterval > 3600) {
                this.logger.warn("Invalid polling rate: {}. Using 5 seconds.", pollingInterval);
                pollingInterval = 5;
            }

            this.logger.info("Starting PollingService with interval: {} seconds", pollingInterval);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(new PollingService(), initialDelay, pollingInterval, TimeUnit.SECONDS);
        }
    }

    @NonNullByDefault
    class PollingService implements Runnable {
        @Override
        public void run() {
            // Get and parse data from froeling controller
            getData();
        }
    }

    public void getData() {
        this.logger.info("Trying to get some data");
        FroelingConfiguration config = null;
        try {
            config = getThing().getConfiguration().as(FroelingConfiguration.class);
        } catch (Exception e) {
            this.logger.error("Error getting Froeling configuration");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Error getting Froeling configuration");
            return;
        }

        switch (config.getControllerType()) {
            case "P3200":
                this.logger.info("Froeling controller: {} COM-Port: {}", config.getControllerType(),
                        config.getComPort());
                switch (config.getComPort()) {
                    case "COM1":
                        this.logger.info("Case: COM1");
                        if (bridgeHandler == null) {
                            this.logger.error("BridgeHandler not available");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                    "BridgeHandler not available");
                            return;
                        }
                        // Connect to bridge
                        bridgeHandler.connect();
                        // Check if bridge is connected and get some data
                        if (bridgeHandler.isConnected()) {
                            this.logger.info("BridgeHandler is connected, calling getP3200COM1Data()");
                            updateStatus(ThingStatus.ONLINE);
                            getP3200COM1Data();
                        } else {
                            this.logger.error("BridgeHandler is not connected, not calling getP3200COM1Data()");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                    "BridgeHandler is not connected");
                            return;
                        }
                        // Disconnect from bridge
                        bridgeHandler.disconnect();
                        break;
                    case "COM2":
                        this.logger.warn("Specified COM-Port not supported yet");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Specified COM-Port not supported yet");
                        break;
                    default:
                        this.logger.error("Invalid COM-Port selected");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Invalid COM-Port selected");
                        break;
                }
                break;
            default:
                this.logger.error("Invalid Froeling controller selected");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid Froeling controller selected");
                break;
        }
    }

    public void getP3200COM1Data() {
        this.logger.info("Getting P3200 controller data");
        long timeStart = System.currentTimeMillis();
        boolean dataStart = false;
        boolean dataSuccess = false;
        int iterations = 0;
        String inputData = "";
        String buffer = "";
        String dataBlockSeparator = "$ ";

        // Get data from controller
        while (dataSuccess == false) {
            try {
                inputData = bridgeHandler.readInput();
                if (inputData != null) {
                    if (dataStart == true && inputData.startsWith(dataBlockSeparator)) {
                        // Got a full data block
                        this.logger.info("Got full data block from Froeling controller");
                        dataSuccess = true;
                        break;
                    }
                    if (dataStart == false && inputData.startsWith(dataBlockSeparator)) {
                        dataStart = true;
                    }
                    if (dataStart == true && !inputData.isEmpty()) {
                        buffer += inputData;
                    } else if (dataStart == true && inputData.isEmpty()) {
                        buffer += "\r\n";
                    }
                }
            } catch (Exception e) {
                this.logger.error("Failed getting data from Froeling controller:", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "failed getting data from Froeling controller");
                break;
            }
            if (iterations >= 200) {
                this.logger.error("No full data block after 200 iterations.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "No full data block after 200 iterations");
                break;
            }
            if ((System.currentTimeMillis() - 60000L) > timeStart) {
                this.logger.error("No full data block after 60 seconds.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "No full data block after 60 seconds");
                break;
            }
            iterations += 1;
        }

        // Parse data
        if (dataSuccess == true) {
            this.logger.info("Parsing P3200 controller data");
            try {
                String[] lineArray = buffer.split("\r\n");

                for (String line : lineArray) {
                    String data[] = line.split(";");

                    /*
                     * data[0] = Label
                     * data[1] = Value
                     * data[2] = Ordinal number
                     * data[3] = Factor for value
                     * data[4] = Unit
                     * data[5] = Unit if data[4] is empty
                     */

                    // Remove data block separator from status label
                    if (data[0].startsWith(dataBlockSeparator)) {
                        data[0] = data[0].replace(dataBlockSeparator, "");
                    }

                    // Trim label
                    data[0] = data[0].trim();

                    // Print raw data
                    // this.logger.info("RAW: (" + data[2] + ") " + data[0] + " = " + data[1] + " " + data[4] + "["
                    // + data[3] + "]");

                    // Don't try to convert status and error texts to int
                    if (!data[2].equals("1") && !data[2].equals("99")) {
                        int factor = Integer.parseInt(data[3]);
                        int value = Integer.parseInt(data[1]);

                        // induced draft fan
                        if (data[2].equals("10")) {
                            data[1] = Integer.toString((value / 30));
                        } else {
                            data[1] = Integer.toString((value / factor));
                        }
                    }
                    // If data[4] is empty, use data[5] to get unit
                    if (data[4].equals("") && !data[5].isEmpty()) {
                        data[4] = data[5];
                    }
                    // Print converted data
                    this.logger.info("CON: ({}) {} = {} {}", data[2], data[0], data[1], data[4]);

                    switch (data[2]) {
                        case "1":
                            updateState(thing.getChannel(CHANNEL_STATUS).getUID(), new StringType(data[0]));
                            break;

                        case "2":
                            updateState(thing.getChannel(CHANNEL_FURNACETEMPERATURE_CURRENT).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "3":
                            updateState(thing.getChannel(CHANNEL_EXHAUSTTEMPERATURE_CURRENT).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "4":
                            updateState(thing.getChannel(CHANNEL_FURNACECONTROLVARIABLE).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "5":
                            updateState(thing.getChannel(CHANNEL_PRIMARYAIR).getUID(), new DecimalType(data[1]));
                            break;
                        case "6":
                            updateState(thing.getChannel(CHANNEL_REMAINOXYGEN).getUID(), new DecimalType(data[1]));
                            break;
                        case "7":
                            updateState(thing.getChannel(CHANNEL_OXYGENCONTROLLER).getUID(), new DecimalType(data[1]));
                            break;
                        case "8":
                            updateState(thing.getChannel(CHANNEL_SECONDARYAIR).getUID(), new DecimalType(data[1]));
                            break;
                        case "9":
                            updateState(thing.getChannel(CHANNEL_IDFAN_SETPOINT).getUID(), new DecimalType(data[1]));
                            break;
                        case "10":
                            updateState(thing.getChannel(CHANNEL_IDFAN_CURRENT).getUID(), new DecimalType(data[1]));
                            break;
                        case "11":
                            updateState(thing.getChannel(CHANNEL_EXHAUSTTEMPERATURE_SETPOINT).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "12":
                            updateState(thing.getChannel(CHANNEL_SLIDEIN_CURRENT).getUID(), new DecimalType(data[1]));
                            break;
                        case "13":
                            updateState(thing.getChannel(CHANNEL_PELLET).getUID(), new DecimalType(data[1]));
                            break;
                        case "14":
                            updateState(thing.getChannel(CHANNEL_FILLING_LEVEL).getUID(), new DecimalType(data[1]));
                            break;
                        case "15":
                            updateState(thing.getChannel(CHANNEL_INTAKESPEED).getUID(), new DecimalType(data[1]));
                            break;
                        case "16":
                            updateState(thing.getChannel(CHANNEL_DELIVERYPOWER).getUID(), new DecimalType(data[1]));
                            break;
                        case "17":
                            updateState(thing.getChannel(CHANNEL_SENSOR_1).getUID(), new DecimalType(data[1]));
                            break;
                        case "18":
                            updateState(thing.getChannel(CHANNEL_FURNACETEMPERATURE_SETPOINT).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "20":
                            updateState(thing.getChannel(CHANNEL_SENSOR_BUFFERTOP).getUID(), new DecimalType(data[1]));
                            break;
                        case "21":
                            updateState(thing.getChannel(CHANNEL_SENSOR_BUFFERBOTTOM).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "22":
                            updateState(thing.getChannel(CHANNEL_BUFFER_PUMP).getUID(), new DecimalType(data[1]));
                            break;
                        case "23":
                            updateState(thing.getChannel(CHANNEL_SENSOR_BOILER).getUID(), new DecimalType(data[1]));
                            break;
                        case "24":
                            updateState(thing.getChannel(CHANNEL_SENSOR_FLOW_1).getUID(), new DecimalType(data[1]));
                            break;
                        case "25":
                            updateState(thing.getChannel(CHANNEL_SENSOR_FLOW_2).getUID(), new DecimalType(data[1]));
                            break;
                        case "26":
                            updateState(thing.getChannel(CHANNEL_HEATINGCIRCUITPUMP_1).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "27":
                            updateState(thing.getChannel(CHANNEL_HEATINGCIRCUITPUMP_2).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "28":
                            updateState(thing.getChannel(CHANNEL_OUTDOORTEMPERATURE).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "29":
                            updateState(thing.getChannel(CHANNEL_COLLECTORTEMPERATURE).getUID(),
                                    new DecimalType(data[1]));
                            break;
                        case "30":
                            updateState(thing.getChannel(CHANNEL_OPERATINGHOURS).getUID(), new DecimalType(data[1]));
                            break;
                        case "99":
                            updateState(thing.getChannel(CHANNEL_ERRORS).getUID(), new StringType(data[1]));
                            break;
                        default:
                            this.logger.warn("No channel available for {} with value {} {}", data[0], data[1], data[4]);
                            break;
                    }
                }
                updateState(thing.getChannel(CHANNEL_LASTUPDATE).getUID(), new DateTimeType());
            } catch (Exception e1) {
                this.logger.error("Error while parsing P3200 controller data: {}", e1);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error while parsing P3200 controller data");
                return;
            }
            this.logger.info("Done parsing P3200 controller data");
        }
    }

    private synchronized IPBridgeHandler getIPBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.error("Required bridge not defined");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Required bridge not defined");
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof IPBridgeHandler) {
                this.bridgeHandler = (IPBridgeHandler) handler;
            } else {
                logger.error("BridgeHandler for bridge {} not available", bridge.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "BridgeHandler not available");
                return null;
            }
        }
        return this.bridgeHandler;
    }
}

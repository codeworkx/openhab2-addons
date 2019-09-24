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
package org.openhab.binding.froeling.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Daniel Hillenbrand - Initial contribution
 */
@NonNullByDefault
public class FroelingConfiguration {
    private String controllerType;
    private String comPort;
    private Integer pollingInterval;

    public String getControllerType() {
        return controllerType;
    }

    public String getComPort() {
        return comPort;
    }

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setControllerType(String controllerType) {
        this.controllerType = controllerType;
    }

    public void setPort(String comPort) {
        this.comPort = comPort;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}

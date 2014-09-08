/**
 *  X10 ActiveEye motion detector.
 *
 *  SmartDevice type for X10 ActiveEye (model MS16A) motion detector with
 *  light sensor.
 *
 *  Copyright (c) 2014 geko@statusbits.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  The latest version of this file can be found at:
 *  https://github.com/statusbits/smartthings/blob/master/X10Things/X10_ActiveEye.device.groovy
 *
 *  Useful links:
 *  - ActiveEye product page: http://www.x10.com/ms16a.html
 *  - ActiveEye setup instructions: http://kbase.x10.com/wiki/Active_Eye_Motion_Sensor_Setup
 *
 *  Revision History
 *  ----------------
 *  2014-08-28  V0.9.1  parse takes 'motion:<value>' as an argument
 *  2014-08-18  V0.9.0  Initial check-in
 */

metadata {
    definition (name:"X10 ActiveEye", namespace:"statusbits", author:"geko@statusbits.com") {
        capability "Sensor"
        capability "Motion Sensor"
        capability "Illuminance Measurement"
        capability "Refresh"

        // custom attributes
        attribute "networkId", "string"

        // custom commands
        command "parse"     // (String "<attribute>:<value>[,<attribute>:<value>]")
    }

    tiles {
        standardTile("motion", "device.motion", width:2, height:2, inactiveLabel:false) {
            state "default", label:'unknown', backgroundColor:"#ffffff"
            state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
            state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        }

        valueTile("illuminance", "device.illuminance", width:1, height:1, inactiveLabel:false) {
            state "default", label:'${currentValue}', unit:"lux",
                backgroundColors:[
                    [value: 10,  color: "#767676"],
                    [value: 400, color: "#fbd41b"]
                ]
        }

        valueTile("networkId", "device.networkId", decoration:"flat", inactiveLabel:false) {
            state "default", label:'${currentValue}', inactiveLabel:false
        }

        standardTile("debug", "device.motion", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["motion", "illuminance"])
        details(["motion", "illuminance", "networkId", "debug"])

        simulator {
            // status messages
            status "Motion Active":     "motion:active"
            status "Motion Inactive":   "motion:inactive"

            for (int i = 10; i < 100; i += 20) {
                status "Illuminance ${i} lux":  "illuminance:${i}"
            }
            for (int i = 100; i < 1000; i += 200) {
                status "Illuminance ${i} lux":  "illuminance:${i}"
            }

            status "Motion Active, 200 lux":    "motion:active, illuminance:200"
            status "Invalid value":             "motion:foobar"
            status "Invalid format":            "foobar"
        }
    }
}

def parse(String message) {
    TRACE("parse(${message})")

    Map msg = stringToMap(message)
    if (msg?.size() == 0) {
        log.error "Invalid message: ${message}"
        return null
    }

    if (msg.containsKey("motion")) {
        parseMotion(msg.motion)
    }

    if (msg.containsKey("illuminance")) {
        parseIlluminance(msg.illuminance)
    }

    return null
}

def refresh() {
    TRACE("refresh()")

    def event = [
        name  : "networkId",
        value : device.deviceNetworkId
    ]

    TRACE("event: (${event})")
    sendEvent(event)
}

private def parseMotion(value) {
    TRACE("parseMotion(${value})")

    def values = ["active", "inactive"]
    if (!values.find {it == value }) {
        log.error "Invalid value: ${value}"
        return
    }

    def event = [
        name  : "motion",
        value : value,
    ]

    TRACE("event: (${event})")
    sendEvent(event)
}

private def parseIlluminance(value) {
    TRACE("parseIlluminance(${value})")

    def event = [
        name  : "illuminance",
        value : value.toInteger(),
        unit  : "lux",
    ]

    TRACE("event: (${event})")
    sendEvent(event)
}

private def TRACE(message) {
    log.debug message
    log.debug "deviceNetworkId : ${device.deviceNetworkId}"
    log.debug "motion          : ${device.currentValue("motion")}"
    log.debug "illuminance     : ${device.currentValue("illuminance")}"
}
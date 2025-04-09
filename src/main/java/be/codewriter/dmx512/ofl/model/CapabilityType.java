package be.codewriter.dmx512.ofl.model;

public enum CapabilityType {
    NO_FUNCTION(),

    SHUTTER_STROBE(
            new Property("shutterEffect", true, true, "Open, Closed, Strobe, Pulse, RampUp, RampDown, RampUpDown, Lightning, Spikes, Burst"),
            new Property("soundControlled", false, true, "Boolean", "false"),
            new Property("speed", false, false, "Speed"),
            new Property("duration", false, false, "Time"),
            new Property("randomTiming", false, true, "Boolean", "false")
    ),

    STROBE_SPEED(
            new Property("speed", true, false, "Speed", "global, doesn't activate strobe directly")
    ),

    STROBE_DURATION(
            new Property("duration", true, false, "Time")
    ),

    INTENSITY(
            new Property("brightness", false, false, "Brightness", "Defaults to brightnessStart: \"off\", brightnessEnd: \"bright\"")
    ),

    COLOR_INTENSITY(
            new Property("color", true, true, "Red, Green, Blue, Cyan, Magenta, Yellow, Amber, White, Warm White, Cold White, UV, Lime, Indigo"),
            new Property("brightness", false, false, "Brightness", "Defaults to brightnessStart: \"off\", brightnessEnd: \"bright\"")
    ),

    COLOR_PRESET(
            new Property("colors", false, false, "array of individual color beams as hex code"),
            new Property("colorTemperature", false, false, "ColorTemperature")
    ),

    COLOR_TEMPERATURE(
            new Property("colorTemperature", true, false, "ColorTemperature")
    ),

    PAN(
            new Property("angle", true, false, "RotationAngle")
    ),

    PAN_CONTINUOUS(
            new Property("speed", true, false, "RotationSpeed")
    ),

    TILT(
            new Property("angle", true, false, "RotationAngle")
    ),

    TILT_CONTINUOUS(
            new Property("speed", true, false, "RotationSpeed")
    ),

    PAN_TILT_SPEED(
            new Property("speed", true, false, "Speed", "either speed or duration is allowed"),
            new Property("duration", true, false, "Time", "either speed or duration is allowed")
    ),

    WHEEL_SLOT(
            new Property("wheel", false, true, "Wheel name", "Defaults to channel name"),
            new Property("slotNumber", true, false, "SlotNumber")
    ),

    WHEEL_SHAKE(
            new Property("isShaking", false, true, "wheel or slot", "Defaults to wheel"),
            new Property("wheel", false, true, "Wheel name or array of wheel names"),
            new Property("slotNumber", false, false, "SlotNumber"),
            new Property("shakeSpeed", false, false, "Speed"),
            new Property("shakeAngle", false, false, "SwingAngle")
    ),

    WHEEL_SLOT_ROTATION(
            new Property("wheel", false, true, "Wheel name or array of wheel names"),
            new Property("slotNumber", false, false, "SlotNumber"),
            new Property("speed", true, false, "RotationSpeed", "either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "either speed or angle is allowed")
    ),

    WHEEL_ROTATION(
            new Property("wheel", false, true, "Wheel name or array of wheel names"),
            new Property("speed", true, false, "RotationSpeed", "either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "either speed or angle is allowed")
    ),

    EFFECT(
            new Property("effectName", true, true, "Free text describing the effect"),
            new Property("effectPreset", true, true, "ColorJump or ColorFade"),
            new Property("speed", false, false, "Speed"),
            new Property("duration", false, false, "Time"),
            new Property("parameter", false, false, "Parameter"),
            new Property("soundControlled", false, true, "Boolean", "false"),
            new Property("soundSensitivity", false, false, "Percent")
    ),

    BEAM_ANGLE(
            new Property("angle", true, false, "BeamAngle")
    ),

    BEAM_POSITION(
            new Property("horizontalAngle", true, false, "HorizontalAngle"),
            new Property("verticalAngle", true, false, "VerticalAngle")
    ),

    EFFECT_SPEED(
            new Property("speed", true, false, "Speed")
    ),

    EFFECT_DURATION(
            new Property("duration", true, false, "Time")
    ),

    EFFECT_PARAMETER(
            new Property("parameter", true, false, "Parameter")
    ),

    SOUND_SENSITIVITY(
            new Property("soundSensitivity", true, false, "Percent")
    ),

    FOCUS(
            new Property("distance", true, false, "Distance")
    ),

    ZOOM(
            new Property("angle", true, false, "BeamAngle")
    ),

    IRIS(
            new Property("openPercent", true, false, "IrisPercent")
    ),

    IRIS_EFFECT(
            new Property("effectName", true, true, "Free text describing the effect"),
            new Property("speed", false, false, "Speed")
    ),

    FROST(
            new Property("frostIntensity", true, false, "Percent")
    ),

    FROST_EFFECT(
            new Property("effectName", true, true, "Free text describing the effect"),
            new Property("speed", false, false, "Speed")
    ),

    PRISM(
            new Property("speed", false, false, "RotationSpeed", "activates fixture's prism; either speed or angle is allowed"),
            new Property("angle", false, false, "RotationAngle", "activates fixture's prism; either speed or angle is allowed")
    ),

    PRISM_ROTATION(
            new Property("speed", true, false, "RotationSpeed", "doesn't activate prism directly; either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "doesn't activate prism directly; either speed or angle is allowed")
    ),

    BLADE_INSERTION(
            new Property("blade", true, true, "Top, Right, Bottom, Left or a number if the position is unknown"),
            new Property("insertion", true, false, "Insertion")
    ),

    BLADE_ROTATION(
            new Property("blade", true, true, "Top, Right, Bottom, Left or a number if the position is unknown"),
            new Property("angle", true, false, "RotationAngle")
    ),

    BLADE_SYSTEM_ROTATION(
            new Property("angle", true, false, "RotationAngle")
    ),

    FOG(
            new Property("fogType", false, true, "Fog or Haze"),
            new Property("fogOutput", false, false, "FogOutput")
    ),

    FOG_OUTPUT(
            new Property("fogOutput", true, false, "FogOutput")
    ),

    FOG_TYPE(
            new Property("fogType", true, true, "Fog or Haze")
    ),

    ROTATION(
            new Property("speed", true, false, "RotationSpeed", "either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "either speed or angle is allowed")
    ),

    SPEED(
            new Property("speed", true, false, "Speed")
    ),

    TIME(
            new Property("time", true, false, "Time")
    ),

    MAINTENANCE(
            new Property("parameter", false, false, "Parameter"),
            new Property("hold", false, true, "Time")
    ),

    GENERIC(); // No type-specific properties

    private final Property[] properties;

    CapabilityType(Property... properties) {
        this.properties = properties;
    }

    public static CapabilityType fromJson(String jsonValue) {
        if (jsonValue == null || jsonValue.isEmpty()) {
            return CapabilityType.NO_FUNCTION;
        }

        for (CapabilityType capabilityType : CapabilityType.values()) {
            if (capabilityType.name()
                    .replace("_", "")
                    .equalsIgnoreCase(jsonValue.replace("_", ""))) {
                return capabilityType;
            }
        }
        return CapabilityType.NO_FUNCTION;
    }

    public Property[] getProperties() {
        return properties;
    }

    // Inner class to hold property information
    public static class Property {
        private final String name;
        private final boolean required;
        private final boolean stepped;
        private final String possibleValues;
        private final String notes;

        public Property(String name, boolean required, boolean stepped, String possibleValues) {
            this(name, required, stepped, possibleValues, "");
        }

        public Property(String name, boolean required, boolean stepped, String possibleValues, String notes) {
            this.name = name;
            this.required = required;
            this.stepped = stepped;
            this.possibleValues = possibleValues;
            this.notes = notes;
        }

        // Getters
        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isStepped() {
            return stepped;
        }

        public String getPossibleValues() {
            return possibleValues;
        }

        public String getNotes() {
            return notes;
        }
    }
}

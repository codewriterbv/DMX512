package be.codewriter.dmx512.ofl.model;

/**
 * All the available capability types defined in OFL
 */
public enum CapabilityType {
    /**
     * No function capability type.
     */
    NO_FUNCTION(),

    /**
     * Shutter strobe capability type. This type includes properties for shutter effect, sound controlled, speed, duration, and random timing.
     */
    SHUTTER_STROBE(
            new Property("shutterEffect", true, true, "Open, Closed, Strobe, Pulse, RampUp, RampDown, RampUpDown, Lightning, Spikes, Burst"),
            new Property("soundControlled", false, true, "Boolean", "false"),
            new Property("speed", false, false, "Speed"),
            new Property("duration", false, false, "Time"),
            new Property("randomTiming", false, true, "Boolean", "false")
    ),

    /**
     * Shutter strobe speed capability type. This type includes a property for speed.
     */
    STROBE_SPEED(
            new Property("speed", true, false, "Speed", "global, doesn't activate strobe directly")
    ),

    /**
     * Shutter strobe duration capability type. This type includes a property for duration.
     */
    STROBE_DURATION(
            new Property("duration", true, false, "Time")
    ),

    /**
     * Intensity capability type. This type includes properties for brightness.
     */
    INTENSITY(
            new Property("brightness", false, false, "Brightness", "Defaults to brightnessStart: \"off\", brightnessEnd: \"bright\"")
    ),

    /**
     * Color intensity capability type. This type includes properties for color and brightness.
     */
    COLOR_INTENSITY(
            new Property("color", true, true, "Red, Green, Blue, Cyan, Magenta, Yellow, Amber, White, Warm White, Cold White, UV, Lime, Indigo"),
            new Property("brightness", false, false, "Brightness", "Defaults to brightnessStart: \"off\", brightnessEnd: \"bright\"")
    ),

    /**
     * Color preset capability type. This type includes properties for colors and color temperature.
     */
    COLOR_PRESET(
            new Property("colors", false, false, "array of individual color beams as hex code"),
            new Property("colorTemperature", false, false, "ColorTemperature")
    ),

    /**
     * Color temperature capability type. This type includes a property for color temperature.
     */
    COLOR_TEMPERATURE(
            new Property("colorTemperature", true, false, "ColorTemperature")
    ),

    /**
     * Pan capability type. This type includes properties for angle.
     */
    PAN(
            new Property("angle", true, false, "RotationAngle")
    ),

    /**
     * Pan continuous capability type. This type includes a property for speed.
     */
    PAN_CONTINUOUS(
            new Property("speed", true, false, "RotationSpeed")
    ),

    /**
     * Tilt capability type. This type includes properties for angle.
     */
    TILT(
            new Property("angle", true, false, "RotationAngle")
    ),

    /**
     * Tilt continuous capability type. This type includes a property for speed.
     */
    TILT_CONTINUOUS(
            new Property("speed", true, false, "RotationSpeed")
    ),

    /**
     * Pan tilt speed capability type. This type includes properties for speed and duration.
     */
    PAN_TILT_SPEED(
            new Property("speed", true, false, "Speed", "either speed or duration is allowed"),
            new Property("duration", true, false, "Time", "either speed or duration is allowed")
    ),

    /**
     * Wheel slot capability type. This type includes properties for wheel and slot number.
     */
    WHEEL_SLOT(
            new Property("wheel", false, true, "Wheel name", "Defaults to channel name"),
            new Property("slotNumber", true, false, "SlotNumber")
    ),

    /**
     * Wheel shake capability type. This type includes properties for shaking, wheel, and slot number.
     */
    WHEEL_SHAKE(
            new Property("isShaking", false, true, "wheel or slot", "Defaults to wheel"),
            new Property("wheel", false, true, "Wheel name or array of wheel names"),
            new Property("slotNumber", false, false, "SlotNumber"),
            new Property("shakeSpeed", false, false, "Speed"),
            new Property("shakeAngle", false, false, "SwingAngle")
    ),

    /**
     * Wheel slot rotation capability type. This type includes properties for wheel, slot number, speed, and angle.
     */
    WHEEL_SLOT_ROTATION(
            new Property("wheel", false, true, "Wheel name or array of wheel names"),
            new Property("slotNumber", false, false, "SlotNumber"),
            new Property("speed", true, false, "RotationSpeed", "either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "either speed or angle is allowed")
    ),

    /**
     * Wheel rotation capability type. This type includes properties for wheel, speed, and angle.
     */
    WHEEL_ROTATION(
            new Property("wheel", false, true, "Wheel name or array of wheel names"),
            new Property("speed", true, false, "RotationSpeed", "either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "either speed or angle is allowed")
    ),

    /**
     * Effect capability type. This type includes properties for effect name, effect preset, speed, duration, parameter, sound controlled, and sound sensitivity.
     */
    EFFECT(
            new Property("effectName", true, true, "Free text describing the effect"),
            new Property("effectPreset", true, true, "ColorJump or ColorFade"),
            new Property("speed", false, false, "Speed"),
            new Property("duration", false, false, "Time"),
            new Property("parameter", false, false, "Parameter"),
            new Property("soundControlled", false, true, "Boolean", "false"),
            new Property("soundSensitivity", false, false, "Percent")
    ),

    /**
     * Beam angle capability type. This type includes a property for beam angle.
     */
    BEAM_ANGLE(
            new Property("angle", true, false, "BeamAngle")
    ),

    /**
     * Beam position capability type. This type includes properties for horizontal and vertical angles.
     */
    BEAM_POSITION(
            new Property("horizontalAngle", true, false, "HorizontalAngle"),
            new Property("verticalAngle", true, false, "VerticalAngle")
    ),

    /**
     * Effect speed capability type. This type includes a property for speed.
     */
    EFFECT_SPEED(
            new Property("speed", true, false, "Speed")
    ),

    /**
     * Effect duration capability type. This type includes a property for duration.
     */
    EFFECT_DURATION(
            new Property("duration", true, false, "Time")
    ),

    /**
     * Effect parameter capability type. This type includes a property for parameter.
     */
    EFFECT_PARAMETER(
            new Property("parameter", true, false, "Parameter")
    ),

    /**
     * Sound sensitivity capability type. This type includes a property for sound sensitivity.
     */
    SOUND_SENSITIVITY(
            new Property("soundSensitivity", true, false, "Percent")
    ),

    /**
     * Focus capability type. This type includes a property for distance.
     */
    FOCUS(
            new Property("distance", true, false, "Distance")
    ),

    /**
     * Zoom capability type. This type includes a property for angle.
     */
    ZOOM(
            new Property("angle", true, false, "BeamAngle")
    ),

    /**
     * Iris capability type. This type includes a property for iris percent.
     */
    IRIS(
            new Property("openPercent", true, false, "IrisPercent")
    ),

    /**
     * Iris effect capability type. This type includes properties for effect name and speed.
     */
    IRIS_EFFECT(
            new Property("effectName", true, true, "Free text describing the effect"),
            new Property("speed", false, false, "Speed")
    ),

    /**
     * Frost capability type. This type includes a property for frost intensity.
     */
    FROST(
            new Property("frostIntensity", true, false, "Percent")
    ),

    /**
     * Frost effect capability type. This type includes properties for effect name and speed.
     */
    FROST_EFFECT(
            new Property("effectName", true, true, "Free text describing the effect"),
            new Property("speed", false, false, "Speed")
    ),

    /**
     * Prism capability type. This type includes properties for speed and angle.
     */
    PRISM(
            new Property("speed", false, false, "RotationSpeed", "activates fixture's prism; either speed or angle is allowed"),
            new Property("angle", false, false, "RotationAngle", "activates fixture's prism; either speed or angle is allowed")
    ),

    /**
     * Prism rotation capability type. This type includes properties for speed and angle.
     */
    PRISM_ROTATION(
            new Property("speed", true, false, "RotationSpeed", "doesn't activate prism directly; either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "doesn't activate prism directly; either speed or angle is allowed")
    ),

    /**
     * Blade insertion capability type. This type includes properties for blade and insertion.
     */
    BLADE_INSERTION(
            new Property("blade", true, true, "Top, Right, Bottom, Left or a number if the position is unknown"),
            new Property("insertion", true, false, "Insertion")
    ),

    /**
     * Blade rotation capability type. This type includes properties for blade and angle.
     */
    BLADE_ROTATION(
            new Property("blade", true, true, "Top, Right, Bottom, Left or a number if the position is unknown"),
            new Property("angle", true, false, "RotationAngle")
    ),

    /**
     * Blade system rotation capability type. This type includes properties for angle.
     */
    BLADE_SYSTEM_ROTATION(
            new Property("angle", true, false, "RotationAngle")
    ),

    /**
     * Fog capability type. This type includes properties for fog type and output.
     */
    FOG(
            new Property("fogType", false, true, "Fog or Haze"),
            new Property("fogOutput", false, false, "FogOutput")
    ),

    /**
     * Fog output capability type. This type includes a property for fog output.
     */
    FOG_OUTPUT(
            new Property("fogOutput", true, false, "FogOutput")
    ),

    /**
     * Fog type capability type. This type includes a property for fog type.
     */
    FOG_TYPE(
            new Property("fogType", true, true, "Fog or Haze")
    ),

    /**
     * Rotation capability type. This type includes properties for speed and angle.
     */
    ROTATION(
            new Property("speed", true, false, "RotationSpeed", "either speed or angle is allowed"),
            new Property("angle", true, false, "RotationAngle", "either speed or angle is allowed")
    ),

    /**
     * Speed capability type. This type includes a property for speed.
     */
    SPEED(
            new Property("speed", true, false, "Speed")
    ),

    /**
     * Time capability type. This type includes a property for time.
     */
    TIME(
            new Property("time", true, false, "Time")
    ),

    /**
     * Maintenance capability type. This type includes properties for parameter and hold.
     */
    MAINTENANCE(
            new Property("parameter", false, false, "Parameter"),
            new Property("hold", false, true, "Time")
    ),

    /**
     * Generic capability type.
     */
    GENERIC();

    private final Property[] properties;

    CapabilityType(Property... properties) {
        this.properties = properties;
    }

    /**
     * Find the capability type by the given value for JSON parsing
     *
     * @param jsonValue the json value
     * @return type
     */
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

    /**
     * Get the properties of the capability type
     *
     * @return array of properties
     */
    public Property[] getProperties() {
        return properties;
    }

    /**
     * Inner class to hold property information
     */
    public static class Property {
        private final String name;
        private final boolean required;
        private final boolean stepped;
        private final String possibleValues;
        private final String notes;

        /**
         * Constructor
         *
         * @param name           name
         * @param required       is required
         * @param stepped        is stepped
         * @param possibleValues possible values
         */
        public Property(String name, boolean required, boolean stepped, String possibleValues) {
            this(name, required, stepped, possibleValues, "");
        }

        /**
         * Constructor with notes
         *
         * @param name           name
         * @param required       is required
         * @param stepped        is stepped
         * @param possibleValues possible values
         * @param notes          notes
         */
        public Property(String name, boolean required, boolean stepped, String possibleValues, String notes) {
            this.name = name;
            this.required = required;
            this.stepped = stepped;
            this.possibleValues = possibleValues;
            this.notes = notes;
        }

        /**
         * Get the name
         *
         * @return name of the property
         */
        public String getName() {
            return name;
        }

        /**
         * Get is required
         *
         * @return is the property required
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Get is stepped
         *
         * @return is the property stepped
         */
        public boolean isStepped() {
            return stepped;
        }

        /**
         * Get possible values
         *
         * @return the possible values
         */
        public String getPossibleValues() {
            return possibleValues;
        }

        /**
         * @return notes about the property
         */
        public String getNotes() {
            return notes;
        }
    }
}

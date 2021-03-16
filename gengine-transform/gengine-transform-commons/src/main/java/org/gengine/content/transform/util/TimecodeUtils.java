package org.gengine.content.transform.util;

/**
 * Utility class for handling timecode strings
 *
 */
public class TimecodeUtils
{

    /**
     * Convert the given number of seconds into a timecode string of the form
     * "HH:mm:ss.SSS"
     *
     * @param totalSeconds
     * @return the timecode string
     */
    public static String convertSecondsToTimecode(float totalSeconds)
    {
        return convertSecondsToTimecode(totalSeconds, 3);
    }

    /**
     * Convert the given number of seconds into a timecode string of the form
     * "HH:mm:ss.SSS" where the given decimalScale indicates the number of
     * digits to retain after the decimal point
     *
     * @param totalSeconds
     * @param decimalScale
     * @return the timecode string
     */
    public static String convertSecondsToTimecode(float totalSeconds, int decimalScale)
    {
        int hours = new Float((totalSeconds / 3600) % 24).intValue();
        String hoursDisplay = String.format("%02d", hours);

        int minutes = new Float((totalSeconds / 60) % 60).intValue();
        String minutesDisplay = String.format("%02d", minutes);

        float seconds = (totalSeconds % 60);

        String secondsDisplay = String.format(
                "%0" + (decimalScale + 3) + "." + decimalScale + "f", seconds);

        return hoursDisplay + ":" + minutesDisplay + ":" + secondsDisplay;
    }

    /**
     * Converts the given timecode string of the form "HH:mm:ss[.SSS]" to the
     * total number of seconds it represents
     *
     * @param timecode
     * @return the total number of seconds
     */
    public static float convertTimecodeToSeconds(String timecode)
    {
        String[] timeParts = timecode.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        float seconds = Float.parseFloat(timeParts[2]) + (60 * minutes) + (60 * 60 * hours);
        return seconds;
    }
}

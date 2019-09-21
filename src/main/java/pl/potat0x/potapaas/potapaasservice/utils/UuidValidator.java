package pl.potat0x.potapaas.potapaasservice.utils;

import java.util.regex.Pattern;

public final class UuidValidator {
    private static final int validUuidLength = 36;
    private static final Pattern uuidPattern = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

    public static boolean checkIfValid(String text) {
        if (text == null || text.length() != validUuidLength || !text.contains("-")) {
            return false;
        } else {
            return uuidPattern.matcher(text.toLowerCase()).matches();
        }
    }
}

package pers.beapoe.menu_merchant;

import java.util.Base64;

public class Base64Utils {

    private static final String BASE64_CHARS_URL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    private static final String BASE64_CHARS_STANDARD = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String encode(String data, boolean url) {
        byte[] bytes = data.getBytes();
        String encoded = Base64.getEncoder().encodeToString(bytes);
        if (url) {
            encoded = encoded.replace('+', '-').replace('/', '_').replaceAll("=", ".");
        }
        return encoded;
    }

    public static String decode(String data, boolean removeLineBreaks) {
        if (removeLineBreaks) {
            data = data.replaceAll("\\s", "");
        }
        byte[] decoded = Base64.getDecoder().decode(data);
        return new String(decoded);
    }

    public static String encodePem(String data) {
        return encode(data, false);
    }

    public static String encodeMime(String data) {
        return encode(data, false);
    }

    public static String encodeUrl(String data) {
        return encode(data, true);
    }
}

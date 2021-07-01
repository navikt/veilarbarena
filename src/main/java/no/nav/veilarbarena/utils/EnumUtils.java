package no.nav.veilarbarena.utils;

public class EnumUtils {

    public static <T extends Enum<?>> T safeValueOf(Class<T> enumClass, String name) {
        return no.nav.common.utils.EnumUtils.valueOf(enumClass, name).orElse(null);
    }

}

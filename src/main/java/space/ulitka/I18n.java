package space.ulitka;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

    public static String get(String key) {
        return bundle.getString(key);
    }
}
package androidx.ui.text.font;

import androidx.ui.text.font.FontSynthesis;
import androidx.ui.text.font.FontWeight;

@SuppressWarnings("KotlinInternalInJava")
public class FontAccessor {

    public static int getFontWeight(FontWeight fontWeight) {
        return fontWeight.getWeight$ui_text_release();
    }

    public static boolean isStyleOn(FontSynthesis fontSynthesis) {
        return fontSynthesis.isStyleOn$ui_text_release();
    }

    public static boolean isWeightOn(FontSynthesis fontSynthesis) {
        return fontSynthesis.isWeightOn$ui_text_release();
    }

}

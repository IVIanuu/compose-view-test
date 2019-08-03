package androidx.compose;

public class CurrentComposerAccessor {

    public static Composer getCurrentComposerNonNull() {
        //noinspection KotlinInternalInJava
        return ViewComposerKt.getCurrentComposerNonNull();
    }

}

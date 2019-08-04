package androidx.compose;

@SuppressWarnings("KotlinInternalInJava")
public class ComposeAccessor {

    public static Composer getCurrentComposerNonNull() {
        return ViewComposerKt.getCurrentComposerNonNull();
    }

    public static boolean isComposing(Composer composer) {
        return composer.isComposing$compose_runtime_release();
    }

}

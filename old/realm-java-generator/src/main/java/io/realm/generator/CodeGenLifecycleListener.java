package io.realm.generator;


public class CodeGenLifecycleListener {

    /*
    private static final CodeGenLifecycleListener INSTANCE = new CodeGenLifecycleListener();

    private long start;

    @Override
    public void beforeCodeGeneration(LifecycleEvent event) {
        event.getContext().getLogger().info("Handling 'before code generation' event");
        start = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void afterCodeGeneration(LifecycleEvent event) {
        event.getContext().getLogger().info("Handling 'after code generation' event");
        GeneratedCodeCleaner cleaner = new GeneratedCodeCleaner();
        int count = cleaner.removeObsoleteGeneratedCode(event.getContext(), start);
        event.getContext().getLogger().info("Total {} obsolete generated files will be deleted...", count);
    }

    public static CodeGenLifecycleListener getInstance() {
        return INSTANCE;
    }

*/
}

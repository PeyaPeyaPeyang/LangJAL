package tokyo.peya.langjal.compiler;

/**
 * Defines compile-time settings for the compiler.
 * <p>
 * Use these constants to control the behavior of the compiler.
 * </p>
 */
public interface CompileSettings
{
    /**
     * No additional compile settings.
     * This is the default setting and does not enable any special features.
     * After JVM 1.6, the stack frame map in the class file is required for working,
     * but this mode does not compute it.
     */
    int NONE = 0x00;

    /**
     * Enables computation of stack frame maps.
     */
    int COMPUTE_STACK_FRAME_MAP = 0x01;

    /**
     * Includes line number table in the output class files.
     * On JAL language's syntax, this is quite non-readable for humans,
     * but it is necessary for debugging purposes with JDWP debugger(IDE's attaching debugger).
     */
    int INCLUDE_LINE_NUMBER_TABLE = 0x02;

    /**
     * Lightweight compile settings.
     * This setting computes the stack frame map but does not include the line number table.
     *
     * @see #COMPUTE_STACK_FRAME_MAP
     */
    int REQUIRED_ONLY = COMPUTE_STACK_FRAME_MAP;

    /**
     * Full compile settings.
     * This setting computes the stack frame map and includes the line number table.
     * @see #COMPUTE_STACK_FRAME_MAP
     * @see #INCLUDE_LINE_NUMBER_TABLE
     */
    int FULL = COMPUTE_STACK_FRAME_MAP | INCLUDE_LINE_NUMBER_TABLE;
}

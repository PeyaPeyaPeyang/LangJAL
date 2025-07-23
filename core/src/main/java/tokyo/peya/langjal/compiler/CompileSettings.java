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
     */
    int NONE = 0x00;

    /**
     * Enables computation of stack frame maps.
     */
    int COMPUTE_STACK_FRAME_MAP = 0x01;
}

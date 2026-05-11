package tokyo.peya.langjal.compiler.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RuntimeUtils")
class RuntimeUtilsTest
{
    @ParameterizedTest
    @CsvSource({
            "1.8, 52",
            "11, 55",
            "21, 65"
    })
    void classFileMajorVersionSupportsLegacyAndModernJavaVersionStrings(String javaVersion, int expectedMajorVersion)
    {
        String original = System.getProperty("java.specification.version");
        try
        {
            System.setProperty("java.specification.version", javaVersion);
            assertEquals(expectedMajorVersion, RuntimeUtils.getClassFileMajorVersion());
        }
        finally
        {
            if (original == null)
                System.clearProperty("java.specification.version");
            else
                System.setProperty("java.specification.version", original);
        }
    }
}

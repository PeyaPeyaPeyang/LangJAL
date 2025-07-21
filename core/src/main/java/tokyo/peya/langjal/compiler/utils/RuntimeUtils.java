package tokyo.peya.langjal.compiler.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RuntimeUtils
{
    public static int getClassFileMajorVersion()
    {
        String version = System.getProperty("java.specification.version"); // 1.8 や 11
        if (version.startsWith("1."))
        {
            int minor = Integer.parseInt(version.substring(2));
            return 44 + minor;
        }
        else
        {
            int major = Integer.parseInt(version);
            return 44 + major;
        }
    }

}

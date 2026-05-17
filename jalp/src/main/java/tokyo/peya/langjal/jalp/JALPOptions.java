package tokyo.peya.langjal.jalp;

public interface JALPOptions {
    int SHOW_ACC_PUBLIC = 0x01;
    int SHOW_ACC_PROTECTED = 0x02;
    int SHOW_ACC_PACKAGE_PRIVATE = 0x04;
    int SHOW_ACC_PRIVATE = 0x08;

    int SHOW_CODE = 0x10;
    int SHOW_CONSTANTS = 0x20;
    int SHOW_HEADER = 0x40;

    int DEFAULT = SHOW_HEADER | SHOW_ACC_PUBLIC | SHOW_ACC_PROTECTED  | SHOW_ACC_PACKAGE_PRIVATE;
    int VERBOSE = 0xFF;

    static boolean is(int options, int flag) {
        return (options & flag) != 0;
    }
}

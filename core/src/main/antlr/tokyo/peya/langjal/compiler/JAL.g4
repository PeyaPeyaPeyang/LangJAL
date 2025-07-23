grammar JAL;
options {
    language = Java;
}

@header {package tokyo.peya.langjal.compiler;}

KWD_CLASS: 'class';
KWD_INTERFACE: 'interface';

KWD_ACC_PUBLIC: 'public';
KWD_ACC_PRIVATE: 'private';
KWD_ACC_PROTECTED: 'protected';
KWD_ACC_ATTR_STATIC: 'static';
KWD_ACC_ATTR_FINAL: 'final';
KWD_ACC_ATTR_SUPER: 'super';
KWD_ACC_ATTR_SYNCHRONIZED: 'synchronized';
KWD_ACC_ATTR_BRIDGE: 'bridge';
KWD_ACC_ATTR_VARARGS: 'varargs';
KWD_ACC_ATTR_NATIVE: 'native';
KWD_ACC_ATTR_ABSTRACT: 'abstract';
KWD_ACC_ATTR_STRICTFP: 'strictfp';
KWD_ACC_ATTR_VOLATILE: 'volatile';
KWD_ACC_ATTR_TRANSIENT: 'transient';
KWD_ACC_ATTR_SYNTHETIC: 'synthetic';
KWD_ACC_ATTR_ANNOTATION: 'annotation';
KWD_ACC_ATTR_ENUM: 'enum';

KWD_CLASS_PROP_MAJOR: 'major_version';
KWD_CLASS_PROP_MINOR: 'minor_version';
KWD_CLASS_PROP_SUPER_CLASS: 'super_class';
KWD_CLASS_PROP_INTERFACES: 'interfaces';

KWD_MNAME_INIT: '<init>';
KWD_MNAME_CLINIT: '<clinit>';
KWD_SWITCH_DEFAULT: 'default';

KWD_METHOD_TYPE: 'MethodType|';
KWD_METHOD_HANDLE: 'MethodHandle|';
KWD_METHOD_HANDLE_TAG_NEWINVOKE: 'newinvokespecial';

INSN_AALOAD: 'aaload';
INSN_AASTORE: 'aastore';
INSN_ACONST_NULL: 'aconst_null';
INSN_ALOAD: 'aload';
INSN_ALOAD_0: 'aload_0';
INSN_ALOAD_1: 'aload_1';
INSN_ALOAD_2: 'aload_2';
INSN_ALOAD_3: 'aload_3';
INSN_ALOAD_4: 'aload_4';
INSN_ANEWARRAY: 'anewarray';
INSN_ARETURN: 'areturn';
INSN_ARRAYLENGTH: 'arraylength';
INSN_ASTORE: 'astore';
INSN_ASTORE_0: 'astore_0';
INSN_ASTORE_1: 'astore_1';
INSN_ASTORE_2: 'astore_2';
INSN_ASTORE_3: 'astore_3';
INSN_ATHROW: 'athrow';
INSN_BALOAD: 'baload';
INSN_BASTORE: 'bastore';
INSN_BIPUSH: 'bipush';
INSN_CALOAD: 'caload';
INSN_CASTORE: 'castore';
INSN_CHECKCAST: 'checkcast';
INSN_D2F: 'd2f';
INSN_D2I: 'd2i';
INSN_D2L: 'd2l';
INSN_DADD: 'dadd';
INSN_DALOAD: 'daload';
INSN_DASTORE: 'dastore';
INSN_DCMPG: 'dcmpg';
INSN_DCMPL: 'dcmpl';
INSN_DCONST_0: 'dconst_0';
INSN_DCONST_1: 'dconst_1';
INSN_DDIV: 'ddiv';
INSN_DLOAD: 'dload';
INSN_DLOAD_0: 'dload_0';
INSN_DLOAD_1: 'dload_1';
INSN_DLOAD_2: 'dload_2';
INSN_DLOAD_3: 'dload_3';
INSN_DMUL: 'dmul';
INSN_DNEG: 'dneg';
INSN_DREM: 'drem';
INSN_DRETURN: 'dreturn';
INSN_DSTORE: 'dstore';
INSN_DSTORE_0: 'dstore_0';
INSN_DSTORE_1: 'dstore_1';
INSN_DSTORE_2: 'dstore_2';
INSN_DSTORE_3: 'dstore_3';
INSN_DSUB: 'dsub';
INSN_DUP: 'dup';
INSN_DUP_X1: 'dup_x1';
INSN_DUP_X2: 'dup_x2';
INSN_DUP2: 'dup2';
INSN_DUP2_X1: 'dup2_x1';
INSN_DUP2_X2: 'dup2_x2';
INSN_F2D: 'f2d';
INSN_F2I: 'f2i';
INSN_F2L: 'f2l';
INSN_FADD: 'fadd';
INSN_FALOAD: 'faload';
INSN_FASTORE: 'fastore';
INSN_FCMPG: 'fcmpg';
INSN_FCMPL: 'fcmpl';
INSN_FCONST_0: 'fconst_0';
INSN_FCONST_1: 'fconst_1';
INSN_FCONST_2: 'fconst_2';
INSN_FDIV: 'fdiv';
INSN_FLOAD: 'fload';
INSN_FLOAD_0: 'fload_0';
INSN_FLOAD_1: 'fload_1';
INSN_FLOAD_2: 'fload_2';
INSN_FLOAD_3: 'fload_3';
INSN_FMUL: 'fmul';
INSN_FNEG: 'fneg';
INSN_FREM: 'frem';
INSN_FRETURN: 'freturn';
INSN_FSTORE: 'fstore';
INSN_FSTORE_0: 'fstore_0';
INSN_FSTORE_1: 'fstore_1';
INSN_FSTORE_2: 'fstore_2';
INSN_FSTORE_3: 'fstore_3';
INSN_FSUB: 'fsub';
INSN_GETFIELD: 'getfield';
INSN_GETSTATIC: 'getstatic';
INSN_GOTO: 'goto';
INSN_GOTO_W: 'goto_w';
INSN_I2B: 'i2b';
INSN_I2C: 'i2c';
INSN_I2D: 'i2d';
INSN_I2F: 'i2f';
INSN_I2L: 'i2l';
INSN_I2S: 'i2s';
INSN_IADD: 'iadd';
INSN_IALOAD: 'iaload';
INSN_IAND: 'iand';
INSN_IASTORE: 'iastore';
INSN_ICONST_M1: 'iconst_m1';
INSN_ICONST_0: 'iconst_0';
INSN_ICONST_1: 'iconst_1';
INSN_ICONST_2: 'iconst_2';
INSN_ICONST_3: 'iconst_3';
INSN_ICONST_4: 'iconst_4';
INSN_ICONST_5: 'iconst_5';
INSN_IDIV: 'idiv';
INSN_IF_ACMPEQ: 'if_acmpeq';
INSN_IF_ACMPNE: 'if_acmpne';
INSN_IF_ICMPEQ: 'if_icmpeq';
INSN_IF_ICMPNE: 'if_icmpne';
INSN_IF_ICMPLT: 'if_icmplt';
INSN_IF_ICMPGE: 'if_icmpge';
INSN_IF_ICMPGT: 'if_icmpgt';
INSN_IF_ICMPLE: 'if_icmple';
INSN_IFEQ: 'ifeq';
INSN_IFNE: 'ifne';
INSN_IFLT: 'iflt';
INSN_IFGE: 'ifge';
INSN_IFGT: 'ifgt';
INSN_IFLE: 'ifle';
INSN_IFNONNULL: 'ifnonnull';
INSN_IFNULL: 'ifnull';
INSN_IINC: 'iinc';
INSN_ILOAD: 'iload';
INSN_ILOAD_0: 'iload_0';
INSN_ILOAD_1: 'iload_1';
INSN_ILOAD_2: 'iload_2';
INSN_ILOAD_3: 'iload_3';
INSN_IMUL: 'imul';
INSN_INEG: 'ineg';
INSN_INSTANCEOF: 'instanceof';
INSN_INVOKEDYNAMIC: 'invokedynamic';
INSN_INVOKEINTERFACE: 'invokeinterface';
INSN_INVOKESPECIAL: 'invokespecial';
INSN_INVOKESTATIC: 'invokestatic';
INSN_INVOKEVIRTUAL: 'invokevirtual';
INSN_IOR: 'ior';
INSN_IREM: 'irem';
INSN_IRETURN: 'ireturn';
INSN_ISHL: 'ishl';
INSN_ISHR: 'ishr';
INSN_ISTORE: 'istore';
INSN_ISTORE_0: 'istore_0';
INSN_ISTORE_1: 'istore_1';
INSN_ISTORE_2: 'istore_2';
INSN_ISTORE_3: 'istore_3';
INSN_ISUB: 'isub';
INSN_IUSHR: 'iushr';
INSN_IXOR: 'ixor';
INSN_JSR: 'jsr';
INSN_JSR_W: 'jsr_w';
INSN_L2D: 'l2d';
INSN_L2F: 'l2f';
INSN_L2I: 'l2i';
INSN_LADD: 'ladd';
INSN_LALOAD: 'laload';
INSN_LAND: 'land';
INSN_LASTORE: 'lastore';
INSN_LCMP: 'lcmp';
INSN_LCONST_0: 'lconst_0';
INSN_LCONST_1: 'lconst_1';
INSN_LDC: 'ldc';
INSN_LDC_W: 'ldc_w';
INSN_LDC2_W: 'ldc2_w';
INSN_LDIV: 'ldiv';
INSN_LLOAD: 'lload';
INSN_LLOAD_0: 'lload_0';
INSN_LLOAD_1: 'lload_1';
INSN_LLOAD_2: 'lload_2';
INSN_LLOAD_3: 'lload_3';
INSN_LMUL: 'lmul';
INSN_LNEG: 'lneg';
INSN_LOOKUPSWITCH: 'lookupswitch';
INSN_LOR: 'lor';
INSN_LREM: 'lrem';
INSN_LRETURN: 'lreturn';
INSN_LSHL: 'lshl';
INSN_LSHR: 'lshr';
INSN_LSTORE: 'lstore';
INSN_LSTORE_0: 'lstore_0';
INSN_LSTORE_1: 'lstore_1';
INSN_LSTORE_2: 'lstore_2';
INSN_LSTORE_3: 'lstore_3';
INSN_LSUB: 'lsub';
INSN_LUSHR: 'lushr';
INSN_LXOR: 'lxor';
INSN_MONITORENTER: 'monitorenter';
INSN_MONITOREXIT: 'monitorexit';
INSN_MULTIANEWARRAY: 'multianewarray';
INSN_NEW: 'new';
INSN_NEWARRAY: 'newarray';
INSN_NOP: 'nop';
INSN_POP: 'pop';
INSN_POP2: 'pop2';
INSN_PUTFIELD: 'putfield';
INSN_PUTSTATIC: 'putstatic';
INSN_RET: 'ret';
INSN_RETURN: 'return';
INSN_SALOAD: 'saload';
INSN_SASTORE: 'sastore';
INSN_SIPUSH: 'sipush';
INSN_SWAP: 'swap';
INSN_TABLESWITCH: 'tableswitch';
INSN_WIDE: 'wide';

TYPE_DESC_BYTE: 'B';
TYPE_DESC_CHAR: 'C';
TYPE_DESC_DOUBLE: 'D';
TYPE_DESC_FLOAT: 'F';
TYPE_DESC_INT: 'I';
TYPE_DESC_LONG: 'J';
TYPE_DESC_SHORT: 'S';
TYPE_DESC_VOID: 'V';
TYPE_DESC_BOOLEAN: 'Z';
TYPE_DESC_OBJECT: 'L' [a-zA-Z0-9_/$]+ ';';

SPACE: [ \t\r\n]+ -> channel(HIDDEN);
NUMBER:   '-'? ( '0x' [0-9a-fA-F]+ [lL]? | [0-9]+ ('.' [0-9]+)? [fFdDlL]?);
BOOLEAN: 'true' | 'false';
ID: [a-zA-Z$_] [a-zA-Z0-9$_]*;
STRING: '\'' ( ~['\\] | '\\' . )* '\'' | '"' ( ~["\\] | '\\' . )* '"' ;
LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT: '/*' .*? '*/' -> channel(HIDDEN);

METHOD_DESCRIPTOR_ARG: LP LBK* (TYPE_DESC_BYTE | TYPE_DESC_CHAR | TYPE_DESC_DOUBLE | TYPE_DESC_FLOAT | TYPE_DESC_INT
                                | TYPE_DESC_LONG | TYPE_DESC_SHORT | TYPE_DESC_VOID
                                | TYPE_DESC_BOOLEAN | TYPE_DESC_OBJECT)* RP;
FULL_QUALIFIED_CLASS_NAME: [a-zA-Z$_][a-zA-Z$0-9_]+ (SLASH FULL_QUALIFIED_CLASS_NAME)*;

SEMI: ';';
COMMA: ',';
COLON: ':';
SLASH: '/';
DOT: '.';
EQ: '=';
LP: '(';
RP: ')';
LBR: '{';
RBR: '}';
LBK: '[';
RBK: ']';
REF: '->';
TIL: '~';

root : classDefinition? EOF ;

// -------------------------------------------------------------------- //


classDefinition : accModClass (KWD_CLASS | KWD_INTERFACE) className (LP classMeta? RP)? LBR classBody RBR;
className : ID | FULL_QUALIFIED_CLASS_NAME;
classMeta : classMetaItem (COMMA classMetaItem)*;
classBody : classBodyItem*;

classMetaItem: classPropMajor | classPropMinor | classPropSuperClass | classPropInterfaces;
classPropMajor: KWD_CLASS_PROP_MAJOR EQ NUMBER;
classPropMinor: KWD_CLASS_PROP_MINOR EQ NUMBER;
classPropSuperClass: KWD_CLASS_PROP_SUPER_CLASS EQ className;
classPropInterfaces: KWD_CLASS_PROP_INTERFACES EQ className (COMMA className)*;

classBodyItem : fieldDefinition | methodDefinition;
fieldDefinition : accModField fieldName COLON typeDescriptor (EQ jvmInsArgScalarType)?;
fieldName : ID;
methodDefinition : accModMethod methodName methodDescriptor methodBody;

methodName : ID | KWD_MNAME_INIT | KWD_MNAME_CLINIT;
methodBody : LBR instructionSet* RBR;
instructionSet : (label tryCatchDirective?)? (instruction SEMI?)+;

typeDescriptor : LBK* (typeDescriptorPrimitive | TYPE_DESC_OBJECT);
typeDescriptorPrimitive : TYPE_DESC_BYTE | TYPE_DESC_CHAR | TYPE_DESC_DOUBLE | TYPE_DESC_FLOAT | TYPE_DESC_INT
                          | TYPE_DESC_LONG | TYPE_DESC_SHORT | TYPE_DESC_VOID | TYPE_DESC_BOOLEAN;
methodDescriptor : METHOD_DESCRIPTOR_ARG typeDescriptor;

accModClass : accessLevel? accAttrClass*;
accModField : accessLevel? accAttrField*;
accModMethod : accessLevel? accAttrMethod*;
accessLevel : KWD_ACC_PUBLIC | KWD_ACC_PRIVATE | KWD_ACC_PROTECTED;
accAttrClass : KWD_ACC_ATTR_FINAL | KWD_ACC_ATTR_SUPER | KWD_INTERFACE | KWD_ACC_ATTR_ABSTRACT | KWD_ACC_ATTR_SYNTHETIC
                 | KWD_ACC_ATTR_ANNOTATION | KWD_ACC_ATTR_ENUM;
accAttrMethod : KWD_ACC_ATTR_STATIC | KWD_ACC_ATTR_FINAL | KWD_ACC_ATTR_SYNCHRONIZED | KWD_ACC_ATTR_BRIDGE
                  | KWD_ACC_ATTR_VARARGS | KWD_ACC_ATTR_NATIVE | KWD_ACC_ATTR_ABSTRACT | KWD_ACC_ATTR_STRICTFP
                  | KWD_ACC_ATTR_SYNTHETIC;
accAttrField : KWD_ACC_ATTR_STATIC | KWD_ACC_ATTR_FINAL | KWD_ACC_ATTR_VOLATILE | KWD_ACC_ATTR_TRANSIENT
                 | KWD_ACC_ATTR_SYNTHETIC | KWD_ACC_ATTR_ENUM;

label : labelName COLON;
labelName : ID;

tryCatchDirective : LBK TIL labelName tryCatchDirectiveEntry* RBK;
tryCatchDirectiveEntry : catchDirective | finallyDirective;
catchDirective : COMMA FULL_QUALIFIED_CLASS_NAME COLON labelName finallyDirective?;
finallyDirective : REF labelName;

localInstigation: LBK (TIL labelName)? REF ID RBK;

jvmInsArgScalarType : STRING | NUMBER | BOOLEAN;

jvmInsArgFieldRef : jvmInsArgFieldRefType REF jvmInsArgFieldRefName COLON typeDescriptor;
jvmInsArgFieldRefType : FULL_QUALIFIED_CLASS_NAME;
jvmInsArgFieldRefName : ID;

jvmInsArgInvokeDynamicRef: jvmInsArgScalarType | jvmInsArgInvokeDynamicMethodType | jvmInsArgInvokeDynamicMethodTypeMethodHandle;
jvmInsArgInvokeDynamicMethodType: KWD_METHOD_TYPE methodDescriptor;
jvmInsArgInvokeDynamicMethodTypeMethodHandle: KWD_METHOD_HANDLE jvmInsArgInvokeDynamicMethodHandleType '|' jvmInsArgMethodRef;
jvmInsArgInvokeDynamicMethodHandleType: INSN_GETFIELD | INSN_GETSTATIC | INSN_PUTFIELD
                                        | INSN_PUTSTATIC | INSN_INVOKEVIRTUAL | INSN_INVOKESPECIAL | INSN_INVOKESTATIC
                                        | KWD_METHOD_HANDLE_TAG_NEWINVOKE | INSN_INVOKEINTERFACE;

jvmInsArgMethodRef : (jvmInsArgMethodRefOwnerType REF)? methodName methodDescriptor;
jvmInsArgMethodRefOwnerType : FULL_QUALIFIED_CLASS_NAME | ID;

jvmInsArgLocalRef : NUMBER | ID;

jvmInsArgTableSwitch : NUMBER LBR jvmInsArgTableSwitchCaseList RBR KWD_SWITCH_DEFAULT labelName;
jvmInsArgTableSwitchCaseList: labelName (COMMA labelName)*;

jvmInsArgLookupSwitch : LBR jvmInsArgLookupSwitchCaseList RBR;
jvmInsArgLookupSwitchCaseList : jvmInsArgLookupSwitchCase (COMMA jvmInsArgLookupSwitchCase)*;
jvmInsArgLookupSwitchCase : jvmInsArgLookupSwitchCaseName COLON labelName;
jvmInsArgLookupSwitchCaseName : NUMBER | KWD_SWITCH_DEFAULT;


// ------------------------------------------------------------  //

instruction: jvmInsAaload | jvmInsAastore  | jvmInsAconstNull | jvmInsAload | jvmInsAloadN  | jvmInsAnewArray
                | jvmInsAreturn | jvmInsArraylength | jvmInsAstore | jvmInsAstoreN | jvmInsAthrow | jvmInsBaload
                | jvmInsBastore | jvmInsBipush | jvmInsCaload | jvmInsCastore | jvmInsCheckcast | jvmInsD2F
                | jvmInsD2I | jvmInsD2L | jvmInsDadd | jvmInsDaload | jvmInsDastore | jvmInsDcmpOP | jvmInsDconstN
                | jvmInsDdiv | jvmInsDload | jvmInsDloadN | jvmInsDmul | jvmInsDneg | jvmInsDrem  | jvmInsDreturn
                | jvmInsDstore | jvmInsDstoreN | jvmInsDsub | jvmInsDup | jvmInsDupX1 | jvmInsDupX2 | jvmInsDup2
                | jvmInsDup2X1 | jvmInsDup2X2 | jvmInsF2D | jvmInsF2I | jvmInsF2L | jvmInsFadd | jvmInsFaload
                | jvmInsFastore | jvmInsFcmpOP | jvmInsFconstN | jvmInsFdiv | jvmInsFload | jvmInsFloadN | jvmInsFmul
                | jvmInsFneg | jvmInsFrem | jvmInsFreturn | jvmInsFstore | jvmInsFstoreN | jvmInsFsub | jvmInsGetfield
                | jvmInsGetstatic | jvmInsGoto | jvmInsGotoW | jvmInsI2B | jvmInsI2C | jvmInsI2D | jvmInsI2F
                | jvmInsI2L | jvmInsI2S | jvmInsIadd | jvmInsIaload | jvmInsIand | jvmInsIastore | jvmInsIconstN
                | jvmInsIdiv | jvmInsIfAcmpOP | jvmInsIfIcmpOP | jvmInsIfOP | jvmInsIfNonnull | jvmInsIfNull
                | jvmInsIinc | jvmInsIload | jvmInsIloadN | jvmInsImul | jvmInsIneg | jvmInsInstanceof
                | jvmInsInvokedynamic | jvmInsInvokeinterface  | jvmInsInvokespecial | jvmInsInvokestatic
                | jvmInsInvokevirtual | jvmInsIor | jvmInsIrem | jvmInsIreturn | jvmInsIshl | jvmInsIshr | jvmInsIstore
                | jvmInsIstoreN | jvmInsIsub | jvmInsIushr | jvmInsIxor | jvmInsJsr | jvmInsJsrW | jvmInsL2D
                | jvmInsL2F | jvmInsL2I | jvmInsLadd | jvmInsLaload | jvmInsLand | jvmInsLastore | jvmInsLcmp
                | jvmInsLconstN | jvmInsLdc | jvmInsLdcW | jvmInsLdc2W | jvmInsLdiv | jvmInsLload | jvmInsLloadN
                | jvmInsLmul | jvmInsLneg | jvmInsLookupswitch | jvmInsLor | jvmInsLrem | jvmInsLreturn | jvmInsLshl
                | jvmInsLshr | jvmInsLstore | jvmInsLstoreN | jvmInsLsub | jvmInsLushr | jvmInsLxor | jvmInsMonitorenter
                | jvmInsMonitorexit | jvmInsMultianewarray | jvmInsNew | jvmInsNewarray | jvmInsNop | jvmInsPop
                | jvmInsPop2 | jvmInsPutfield | jvmInsPutstatic | jvmInsRet | jvmInsReturn | jvmInsSaload
                | jvmInsSastore | jvmInsSipush | jvmInsSwap | jvmInsTableswitch;

jvmInsAaload: INSN_AALOAD;
jvmInsAastore: INSN_AASTORE;
jvmInsAconstNull: INSN_ACONST_NULL;
jvmInsAload: INSN_WIDE? INSN_ALOAD jvmInsArgLocalRef;
jvmInsAloadN: INSN_ALOAD_0 | INSN_ALOAD_1 | INSN_ALOAD_2 | INSN_ALOAD_3 | INSN_ALOAD_4;
jvmInsAnewArray: INSN_ANEWARRAY typeDescriptor;
jvmInsAreturn: INSN_ARETURN;
jvmInsArraylength: INSN_ARRAYLENGTH;
jvmInsAstore: INSN_WIDE? INSN_ASTORE jvmInsArgLocalRef localInstigation?;
jvmInsAstoreN: (INSN_ASTORE_0 | INSN_ASTORE_1 | INSN_ASTORE_2 | INSN_ASTORE_3) localInstigation?;
jvmInsAthrow: INSN_ATHROW;
jvmInsBaload: INSN_BALOAD;
jvmInsBastore: INSN_BASTORE;
jvmInsBipush: INSN_BIPUSH NUMBER;
jvmInsCaload: INSN_CALOAD;
jvmInsCastore: INSN_CASTORE;
jvmInsCheckcast: INSN_CHECKCAST typeDescriptor;
jvmInsD2F: INSN_D2F;
jvmInsD2I: INSN_D2I;
jvmInsD2L: INSN_D2L;
jvmInsDadd: INSN_DADD;
jvmInsDaload: INSN_DALOAD;
jvmInsDastore: INSN_DASTORE;
jvmInsDcmpOP: INSN_DCMPG | INSN_DCMPL;
jvmInsDconstN: INSN_DCONST_0 | INSN_DCONST_1;
jvmInsDdiv: INSN_DDIV;
jvmInsDload: INSN_WIDE? INSN_DLOAD jvmInsArgLocalRef;
jvmInsDloadN: INSN_DLOAD_0 | INSN_DLOAD_1 | INSN_DLOAD_2 | INSN_DLOAD_3;
jvmInsDmul: INSN_DMUL;
jvmInsDneg: INSN_DNEG;
jvmInsDrem: INSN_DREM;
jvmInsDreturn: INSN_DRETURN;
jvmInsDstore: INSN_WIDE? INSN_DSTORE jvmInsArgLocalRef localInstigation?;
jvmInsDstoreN: (INSN_DSTORE_0 | INSN_DSTORE_1 | INSN_DSTORE_2 | INSN_DSTORE_3) localInstigation?;
jvmInsDsub: INSN_DSUB;
jvmInsDup: INSN_DUP;
jvmInsDupX1: INSN_DUP_X1;
jvmInsDupX2: INSN_DUP_X2;
jvmInsDup2: INSN_DUP2;
jvmInsDup2X1: INSN_DUP2_X1;
jvmInsDup2X2: INSN_DUP2_X2;
jvmInsF2D: INSN_F2D;
jvmInsF2I: INSN_F2I;
jvmInsF2L: INSN_F2L;
jvmInsFadd: INSN_FADD;
jvmInsFaload: INSN_FALOAD;
jvmInsFastore: INSN_FASTORE;
jvmInsFcmpOP: INSN_FCMPG | INSN_FCMPL;
jvmInsFconstN: INSN_FCONST_0 | INSN_FCONST_1 | INSN_FCONST_2;
jvmInsFdiv: INSN_FDIV;
jvmInsFload: INSN_WIDE? INSN_FLOAD jvmInsArgLocalRef;
jvmInsFloadN: INSN_FLOAD_0 | INSN_FLOAD_1 | INSN_FLOAD_2 | INSN_FLOAD_3;
jvmInsFmul: INSN_FMUL;
jvmInsFneg: INSN_FNEG;
jvmInsFrem: INSN_FREM;
jvmInsFreturn: INSN_FRETURN;
jvmInsFstore: INSN_WIDE? INSN_FSTORE jvmInsArgLocalRef localInstigation?;
jvmInsFstoreN: (INSN_FSTORE_0 | INSN_FSTORE_1 | INSN_FSTORE_2 | INSN_FSTORE_3) localInstigation?;
jvmInsFsub: INSN_FSUB;
jvmInsGetfield: INSN_GETFIELD jvmInsArgFieldRef;
jvmInsGetstatic: INSN_GETSTATIC jvmInsArgFieldRef;
jvmInsGoto: INSN_GOTO labelName;
jvmInsGotoW: INSN_GOTO_W labelName;
jvmInsI2B: INSN_I2B;
jvmInsI2C: INSN_I2C;
jvmInsI2D: INSN_I2D;
jvmInsI2F: INSN_I2F;
jvmInsI2L: INSN_I2L;
jvmInsI2S: INSN_I2S;
jvmInsIadd: INSN_IADD;
jvmInsIaload: INSN_IALOAD;
jvmInsIand: INSN_IAND;
jvmInsIastore: INSN_IASTORE;
jvmInsIconstN: INSN_ICONST_M1 | INSN_ICONST_0 | INSN_ICONST_1 | INSN_ICONST_2 | INSN_ICONST_3 | INSN_ICONST_4
               | INSN_ICONST_5;
jvmInsIdiv: INSN_IDIV;
jvmInsIfAcmpOP: INSN_IF_ACMPEQ labelName
                | INSN_IF_ACMPNE labelName;
jvmInsIfIcmpOP: INSN_IF_ICMPEQ labelName
                | INSN_IF_ICMPNE labelName
                | INSN_IF_ICMPLT labelName
                | INSN_IF_ICMPGE labelName
                | INSN_IF_ICMPGT labelName
                | INSN_IF_ICMPLE labelName;
jvmInsIfOP: INSN_IFEQ labelName
            | INSN_IFNE labelName
            | INSN_IFLT labelName
            | INSN_IFGE labelName
            | INSN_IFGT labelName
            | INSN_IFLE labelName;
jvmInsIfNonnull: INSN_IFNONNULL labelName;
jvmInsIfNull: INSN_IFNULL labelName;
jvmInsIinc: INSN_WIDE? INSN_IINC jvmInsArgLocalRef NUMBER;
jvmInsIload: INSN_WIDE? INSN_ILOAD jvmInsArgLocalRef;
jvmInsIloadN: INSN_ILOAD_0 | INSN_ILOAD_1 | INSN_ILOAD_2 | INSN_ILOAD_3;
jvmInsImul: INSN_IMUL;
jvmInsIneg: INSN_INEG;
jvmInsInstanceof: INSN_INSTANCEOF typeDescriptor;
jvmInsInvokedynamic: INSN_INVOKEDYNAMIC methodName methodDescriptor jvmInsArgInvokeDynamicMethodTypeMethodHandle jvmInsArgInvokeDynamicRef*;
jvmInsInvokeinterface: INSN_INVOKEINTERFACE jvmInsArgMethodRef;
jvmInsInvokespecial: INSN_INVOKESPECIAL jvmInsArgMethodRef;
jvmInsInvokestatic: INSN_INVOKESTATIC jvmInsArgMethodRef;
jvmInsInvokevirtual: INSN_INVOKEVIRTUAL jvmInsArgMethodRef;
jvmInsIor: INSN_IOR;
jvmInsIrem: INSN_IREM;
jvmInsIreturn: INSN_IRETURN;
jvmInsIshl: INSN_ISHL;
jvmInsIshr: INSN_ISHR;
jvmInsIstore: INSN_WIDE? INSN_ISTORE jvmInsArgLocalRef localInstigation?;
jvmInsIstoreN: (INSN_ISTORE_0 | INSN_ISTORE_1 | INSN_ISTORE_2 | INSN_ISTORE_3) localInstigation?;
jvmInsIsub: INSN_ISUB;
jvmInsIushr: INSN_IUSHR;
jvmInsIxor: INSN_IXOR;
jvmInsJsr: INSN_JSR labelName;
jvmInsJsrW: INSN_JSR_W labelName;
jvmInsL2D: INSN_L2D;
jvmInsL2F: INSN_L2F;
jvmInsL2I: INSN_L2I;
jvmInsLadd: INSN_LADD;
jvmInsLaload: INSN_LALOAD;
jvmInsLand: INSN_LAND;
jvmInsLastore: INSN_LASTORE;
jvmInsLcmp: INSN_LCMP;
jvmInsLconstN: INSN_LCONST_0 | INSN_LCONST_1;
jvmInsLdc: INSN_LDC jvmInsArgScalarType;
jvmInsLdcW: INSN_LDC_W jvmInsArgScalarType;
jvmInsLdc2W: INSN_LDC2_W jvmInsArgScalarType;
jvmInsLdiv: INSN_LDIV;
jvmInsLload: INSN_WIDE? INSN_LLOAD jvmInsArgLocalRef;
jvmInsLloadN: INSN_LLOAD_0 | INSN_LLOAD_1 | INSN_LLOAD_2 | INSN_LLOAD_3;
jvmInsLmul: INSN_LMUL;
jvmInsLneg: INSN_LNEG;
jvmInsLookupswitch: INSN_LOOKUPSWITCH jvmInsArgLookupSwitch;
jvmInsLor: INSN_LOR;
jvmInsLrem: INSN_LREM;
jvmInsLreturn: INSN_LRETURN;
jvmInsLshl: INSN_LSHL;
jvmInsLshr: INSN_LSHR;
jvmInsLstore: INSN_WIDE? INSN_LSTORE jvmInsArgLocalRef localInstigation?;
jvmInsLstoreN: (INSN_LSTORE_0 | INSN_LSTORE_1 | INSN_LSTORE_2 | INSN_LSTORE_3) localInstigation?;
jvmInsLsub: INSN_LSUB;
jvmInsLushr: INSN_LUSHR;
jvmInsLxor: INSN_LXOR;
jvmInsMonitorenter: INSN_MONITORENTER;
jvmInsMonitorexit: INSN_MONITOREXIT;
jvmInsMultianewarray: INSN_MULTIANEWARRAY typeDescriptor NUMBER;
jvmInsNew: INSN_NEW FULL_QUALIFIED_CLASS_NAME;
jvmInsNewarray: INSN_NEWARRAY typeDescriptor;
jvmInsNop: INSN_NOP;
jvmInsPop: INSN_POP;
jvmInsPop2: INSN_POP2;
jvmInsPutfield: INSN_PUTFIELD jvmInsArgFieldRef;
jvmInsPutstatic: INSN_PUTSTATIC jvmInsArgFieldRef;
jvmInsRet: INSN_WIDE? INSN_RET jvmInsArgLocalRef;
jvmInsReturn: INSN_RETURN;
jvmInsSaload: INSN_SALOAD;
jvmInsSastore: INSN_SASTORE;
jvmInsSipush: INSN_SIPUSH NUMBER;
jvmInsSwap: INSN_SWAP;
jvmInsTableswitch: INSN_TABLESWITCH jvmInsArgTableSwitch;


ERRCHAR: . -> channel(HIDDEN);

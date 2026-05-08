# JAL（Java Assembly Language） - 完全構文ガイド（日本語訳）

## 目次

1. 概要
2. 基本構造
3. 字句要素（レキシカル要素）
4. クラス定義
5. フィールド
6. メソッド
7. 型記述子
8. 命令（Instructions）
9. 制御フロー
10. 例外処理
11. 例（Examples）
12. ベストプラクティス

---

## 概要

JAL（Java Assembly Language）は，JVM（Java Virtual Machine）バイトコードを人間が読みやすいアセンブリ風の構文で記述するためのドメイン固有言語です。JALはJavaクラスファイルへの直接的なマッピングを提供しつつ，ラベルやフィールド参照，メソッド呼び出しなどの高レベルな記法を使って可読性を高めています。

低レベルの生のバイトコードと比べて，定数プールやバイトコードオフセットの詳細を抽象化し，バイトコードレベルでのアルゴリズム設計に集中できるようにします。

---

## 基本構造

JALソースファイルは1つのクラスまたはインターフェース定義を含みます：

```
JALファイル
  └── クラス/インターフェース定義
        ├── クラスメタデータ（バージョン，スーパークラス，インターフェース）
        ├── フィールド
        └── メソッド
```

### 最小の例

```jal
public class HelloWorld (
  major_version=55,
  minor_version=0) {
  
  public static main([Ljava/lang/String;)V {
    return
  }
}
```

---

## 字句要素（Lexical Elements）

### キーワード

#### クラス宣言
- `class` - クラスを宣言する
- `interface` - インターフェースを宣言する

#### アクセス修飾子
- `public` - どこからでもアクセス可能
- `private` - クラス内部のみアクセス可能
- `protected` - パッケージ内およびサブクラスからアクセス可能

#### クラスレベル属性
- `static` - インスタンスではなくクラスに属する
- `final` - 継承・オーバーライドを禁止
- `abstract` - サブクラスで実装が必要
- `super` - スーパークラスを示す
- `synthetic` - コンパイラ生成要素
- `annotation` - アノテーション型
- `enum` - 列挙型
- `strictfp` - 浮動小数点演算をIEEE 754に厳密に従わせる

#### メソッド属性
- `synchronized` - 同期化された実行
- `bridge` - ブリッジメソッド（コンパイラ生成）
- `varargs` - 可変長引数
- `native` - ネイティブ実装

#### フィールド属性
- `volatile` - 値が予期せず変わる可能性
- `transient` - シリアライズ対象外

#### クラスプロパティ（メタデータ）
- `major_version` - JVMクラスファイルのメジャーバージョン（例：55 = Java 11）
- `minor_version` - マイナーバージョン
- `super_class` - 親クラス
- `interfaces` - 実装インターフェース

#### 特殊メソッド名
- `<init>` - コンストラクタ
- `<clinit>` - クラス初期化子（static 初期化ブロック）

### リテラル（Literals）

#### 数値
```jal
42              // 10進数整数
-123            // 負の整数
0xFF            // 16進数
3.14            // 浮動小数点
100L            // long 型
3.14f           // float 型
```

#### ブーリアン
```jal
true
false
```

#### 文字列
```jal
"Hello, World!"
'Single quotes work too'
"Escape sequences: \n \t \\ \" \'"
```

#### 識別子（Identifiers）
識別子は英字，`$`，`_` のいずれかで始まり，その後は英数字，`$`，`_` を含めることができます：

```jal
myVariable
_privateField
$dollarField
ClassName
method1Name
```

### コメント

```jal
// 1行コメント

/* 複数行
   コメント */
```

### 演算子と記号

| 記号 | 名前 | 用途 |
|------|------|------|
| `->` | 参照演算子 | `class -> field:Type`，`method -> name(...)` など |
| `~`  | ラベルマーカー | 例外ディレクティブ内で使用（`[~ label]`） |
| `:`  | コロン | ラベル定義や型の区切り |
| `=`  | 代入 | フィールド初期化，メタデータ代入 |
| `;`  | セミコロン | 命令終端（省略可） |
| `()` | 括弧 | メソッド記述子，メタデータのグループ化 |
| `{}` | ブレース | クラス/メソッド本体，switchのケース |
| `[]` | 角括弧 | 配列型やローカル変数情報 |
| `,`  | コンマ | リストの区切り |
| `/`  | スラッシュ | パッケージ/クラス区切り（fully-qualified name） |

---

## クラス定義

### 構文（文法）

```antlr
classDefinition : accModClass (KWD_CLASS | KWD_INTERFACE) className 
                  (LP classMeta? RP)? LBR classBody RBR;
```

### 構造

```jal
[アクセス修飾子] [クラス属性] class|interface クラス名
  (クラスメタデータ)? {
    フィールド定義
    メソッド定義
}
```

### 例

```jal
public class Student (
  major_version=55,
  minor_version=0,
  super_class=java/lang/Object,
  interfaces=java/lang/Cloneable) {
  
  // Fields
  // Methods
}
```

### クラスメタデータ

クラスメタデータは省略可能で，括弧で囲みます：

```jal
(major_version=55, minor_version=0, super_class=BaseClass, 
 interfaces=Serializable, Cloneable)
```

#### major_version

JVMクラスファイルのメジャーバージョンを指定します：

| バージョン | Java リリース |
|-----------:|---------------|
| 52 | Java 8 |
| 53 | Java 9 |
| 54 | Java 10 |
| 55 | Java 11 |
| 61 | Java 17 |

```jal
public class MyClass (major_version=55) {
  // Java 11 向け
}
```

#### super_class

親クラスを定義します。デフォルトは `java/lang/Object` です：

```jal
public class Child (super_class=Parent) {
  // Parent を継承
}
```

#### interfaces

実装するインターフェースの一覧を指定します：

```jal
public class Worker (
  interfaces=Runnable,Serializable) {
  // 複数インターフェース実装
}
```

---

## フィールド

### 構文

```antlr
fieldDefinition : accModField fieldName COLON typeDescriptor 
                  (EQ jvmInsArgScalarType)?;
```

### 構造

```jal
[アクセス修飾子] [フィールド属性] フィールド名 : 型記述子 [= 初期値];
```

### 例

```jal
public count : I;

private phoneNumber : Ljava/lang/String;;

static final PI : D = 3.14;

volatile current : I = 0;
```

### フィールド属性

```jal
static      // クラス変数（インスタンス共有）
final       // 不変（初期化後変更不可）
volatile    // 予期せず変更されうることを示す
transient   // シリアライズ対象外
enum        // 列挙定数
synthetic   // コンパイラ生成
```

### 初期化

スカラー値でフィールドを初期化できます：

```jal
private age : I = 25;
public name : Ljava/lang/String; = "John";
static count : J = 100;
final PI : D = 3.14159;
```

---

## メソッド

### 構文

```antlr
methodDefinition : accModMethod methodName methodDescriptor methodBody;
methodName : ID | KWD_MNAME_INIT | KWD_MNAME_CLINIT;
methodDescriptor : METHOD_DESCRIPTOR_ARG typeDescriptor;
methodBody : LBR instructionSet* RBR;
```

### 構造

```jal
[アクセス修飾子] [メソッド属性] メソッド名 (引数型)戻り型 {
  命令列
}
```

### 例

#### 通常のメソッド

```jal
public add (II)I {
  iload_0
  iload_1
  iadd
  ireturn
}
```

このメソッドは：
- 整数を2つ受け取り，整数を返す
- 第一引数・第二引数を読み込み加算して返す

#### コンストラクタ

```jal
public <init> ()V {
  aload_0
  invokespecial java/lang/Object -> <init> ()V
  return
}
```

#### static 初期化子

```jal
static <clinit> ()V {
  iconst_0
  putstatic MyClass -> counter : I
  return
}
```

### メソッド記述子（Method Descriptor）

メソッド記述子は引数型と戻り型をJVM形式で定義します：

```
(ParameterTypes)ReturnType
```

#### よく使う記述子

```jal
()V              // 引数なし，void 戻り
(I)I             // int 引数1つ，int 戻り
(II)I            // int 引数2つ，int 戻り
(Ljava/lang/String;)I  // String 引数，int 戻り
([I)I            // int[] 引数，int 戻り
([Ljava/lang/String;)V // String[] 引数，void 戻り
```

### メソッド属性

```jal
public        // どこからでもアクセス可能
private       // クラス内のみ
protected     // パッケージ内およびサブクラスから
static        // クラスメソッド（インスタンス参照なし）
final         // オーバーライド不可
abstract      // サブクラスで実装が必須
synchronized  // 同期化
native        // ネイティブ実装
strict        // 厳格な浮動小数点挙動
```

---

## 型記述子（Type Descriptors）

型記述子はJVM形式でJavaの型を表現します：

### プリミティブ型

| 記述子 | 型       | 範囲/サイズ       |
|----:|---------|--------------|
| `V` | void    | N/A          |
| `Z` | boolean | true/false   |
| `B` | byte    | -128〜127     |
| `C` | char    | 0〜65535      |
| `S` | short   | -32768〜32767 |
| `I` | int     | -2³¹〜2³¹-1   |
| `J` | long    | -2⁶³〜2⁶³-1   |
| `F` | float   | IEEE 754 単精度 |
| `D` | double  | IEEE 754 倍精度 |

### オブジェクト型

オブジェクト型は `L` の後に完全修飾クラス名を続け，`;` で終わります：

```jal
Ljava/lang/Object;
Ljava/lang/String;
Ljava/util/ArrayList;
Ljava/io/PrintStream;
```

カスタムクラス例：

```jal
Lcom/example/MyClass;
Lcom/example/package/SomeClass;
```

### 配列型

配列は先頭に `[` を付けて表現します：

```jal
[I              // int[]
[Z              // boolean[]
[[I             // int[][]
[Ljava/lang/String;   // String[]
[[[D            // double[][][]
```

### コンテキストでの例

```jal
// フィールド宣言（様々な型）
public numbers : [I;
private name : Ljava/lang/String;;
static PI : D = 3.14;
volatile flag : Z;
public entities : [Ljava/lang/Object;;

// メソッド記述子
main([Ljava/lang/String;)V  // main(String[] args)
sort([I)V                   // sort(int[] arr)
equals(Ljava/lang/Object;)Z // equals(Object obj)
```

---

## 命令（Instructions）

JALは200以上のJVMバイトコード命令をカテゴリ別にサポートします。

### スタック操作

#### 定数をプッシュ

```jal
aconst_null                  // null をプッシュ
iconst_0 ... iconst_5       // 0〜5 をプッシュ
iconst_m1                   // -1 をプッシュ
lconst_0, lconst_1         // long の 0/1 をプッシュ
fconst_0, fconst_1, fconst_2  // float の 0/1/2 をプッシュ
dconst_0, dconst_1         // double の 0/1 をプッシュ
bipush 100                  // byte 範囲をプッシュ
sipush 10000                // short 範囲をプッシュ
ldc "Hello"                 // 文字列や定数をプッシュ
ldc_w 65535                 // ワイド定数をプッシュ
```

**例:**
```jal
public getZero ()I {
  iconst_0
  ireturn
}

public getMessage ()Ljava/lang/String; {
  ldc "Hello, World!"
  areturn
}
```

#### ローカル変数のロード

```jal
iload_0 ... iload_3        // ローカル0〜3からintをロード
iload 5                    // ローカル5からintをロード
lload_0 ... lload_3        // long をロード
fload_0 ... fload_3        // float をロード
dload_0 ... dload_3        // double をロード
aload_0 ... aload_3        // オブジェクトをロード
aload 10                   // ローカル10からオブジェクトをロード
```

**例:**
```jal
public addTwo (II)I {
  iload_0          // 第1引数をロード
  iload_1          // 第2引数をロード
  iadd             // 加算
  ireturn          // 戻り値
}
```

#### ローカル変数への格納

```jal
istore_0 ... istore_3       // ローカル0〜3にintを格納
istore 5                    // ローカル5にintを格納
lstore_0 ... lstore_3       // long を格納
fstore_0 ... fstore_3       // float を格納
dstore_0 ... dstore_3       // double を格納
astore_0 ... astore_3       // オブジェクトを格納
astore 10                   // ローカル10にオブジェクトを格納
```

**ローカル変数情報付きの例:**
```jal
public setCounter (I)V {
  iload_1 [->counter]     // 変数名ヒント付きでロード
  istore_2 [I]            // 型ヒント付きで格納
  return
}
```

#### スタック操作（dup, swap など）

```jal
dup              // スタックトップを複製
dup_x1           // トップを複製して2番目の位置に挿入
dup_x2           // トップを複製して3番目の位置に挿入
dup2             // 上位2要素を複製
dup2_x1          // 上位2要素を複製して3番目に挿入
dup2_x2          // ...
swap             // 上位2要素を交換
pop              // スタックトップを取り除く
pop2             // 上位2要素を取り除く
```

**例:**
```jal
public printTwice ()V {
  ldc "Message"
  dup              // 文字列を複製
  astore_1         // 1つを格納
  aload_1          // 読み戻し
  // スタックに2つのコピーがある
  return
}
```

### 算術演算

```jal
// 整数演算
iadd, isub, imul, idiv, irem       // +, -, *, /, %
ineg                               // 符号反転
ishl, ishr, iushr                  // シフト演算

// long 演算
ladd, lsub, lmul, ldiv, lrem, lneg

// float 演算
fadd, fsub, fmul, fdiv, frem, fneg

// double 演算
dadd, dsub, dmul, ddiv, drem, dneg

// ビット演算
iand, ior, ixor                    // &, |, ^
land, lor, lxor                    // long 用
```

**例:**
```jal
public factorial (I)I {
  iconst_1        // result = 1
  istore_2
  iload_0         // パラメータをロード
  istore_1        // i = パラメータ
  
  loop:
  iload_1         // i をロード
  ifle loopEnd     // if i <= 0 then exit
  
  iload_2         // result
  iload_1
  imul            // 掛け算
  istore_2        // 結果を保存
  
  iinc 1 -1       // i--
  goto loop
  
  loopEnd:
  iload_2
  ireturn
}
```

### 型変換

```jal
i2l, i2f, i2d              // int -> long/float/double
l2i, l2f, l2d              // long -> int/float/double
f2i, f2l, f2d              // float -> int/long/double
d2i, d2l, d2f              // double -> int/long/float
i2b, i2c, i2s              // int -> byte/char/short
```

**例:**
```jal
public doubleToInt (D)I {
  dload_0        // double パラメータをロード
  d2i            // int に変換
  ireturn
}
```

### 配列操作

#### 配列の作成

```jal
newarray I              // int[] を作る
newarray Z              // boolean[] を作る
anewarray Ljava/lang/String;  // オブジェクト配列を作る
multianewarray [I 2     // 2次元配列を作る
```

#### 配列アクセス

```jal
iaload, iastore        // int 配列 load/store
laload, lastore        // long
faload, fastore        // float
daload, dastore        // double
aaload, aastore        // object
baload, bastore        // byte
caload, castore        // char
saload, sastore        // short
arraylength            // 配列長を取得
```

**例（BubbleSortより）**
```jal
public static main([Ljava/lang/String;)V {
  // int[] arr = {5, 3, 8, 4, 2};
  iconst_5
  newarray I           // 配列長 5 の int[] を作る
  dup
  iconst_0
  iconst_5
  iastore              // arr[0] = 5
  dup
  iconst_1
  iconst_3
  iastore              // arr[1] = 3
  // ... 初期化続く
  astore_1             // 配列参照を保持
  
  aload_1
  iload_2
  iaload               // 配列要素をロード
  return
}
```

### フィールドアクセス

```jal
getfield ClassName -> fieldName : FieldType
putfield ClassName -> fieldName : FieldType
getstatic ClassName -> fieldName : FieldType
putstatic ClassName -> fieldName : FieldType
```

**例:**

```jal
// System.out を取得
getstatic java/lang/System -> out : Ljava/io/PrintStream;

// インスタンスフィールドに格納
putfield MyClass -> count : I

// static フィールド参照
getstatic MyClass -> totalCount : I
```

### メソッド呼び出し

```jal
invokevirtual ClassName -> methodName MethodDescriptor
invokespecial ClassName -> methodName MethodDescriptor
invokestatic ClassName -> methodName MethodDescriptor
invokeinterface ClassName -> methodName MethodDescriptor
invokedynamic methodName MethodDescriptor ...
```

**HelloWorld.jal からの例:**

```jal
// System.out.println("Hello, World!")
getstatic java/lang/System -> out : Ljava/io/PrintStream;
ldc "Hello, World!"
invokevirtual java/io/PrintStream -> println (Ljava/lang/String;)V
```

```jal
// ArrayList.add(element)
invokevirtual java/util/ArrayList -> add (Ljava/lang/Object;)Z

// Math.sqrt(double) - static
invokestatic java/lang/Math -> sqrt (D)D

// Interface のメソッド
invokeinterface java/util/List -> add (Ljava/lang/Object;)Z
```

### 戻り命令

```jal
ireturn             // int を返す
lreturn             // long
freturn             // float
dreturn             // double
areturn             // オブジェクト参照
return              // void
```

**例:**
```jal
public getAge ()I {
  bipush 25
  ireturn
}

public getName ()Ljava/lang/String; {
  ldc "Alice"
  areturn
}

public printMessage ()V {
  getstatic java/lang/System -> out : Ljava/io/PrintStream;
  ldc "Done"
  invokevirtual java/io/PrintStream -> println (Ljava/lang/String;)V
  return
}
```

---

## 制御フロー

### 条件分岐

#### 単項（単一値）条件分岐

```jal
ifeq label          // 0 と等しい場合
ifne label          // 0 と等しくない場合
iflt label          // 0 より小さい場合
ifle label          // 0 以下の場合
ifgt label          // 0 より大きい場合
ifge label          // 0 以上の場合
ifnull label        // null の場合
ifnonnull label     // null でない場合
```

#### 二項（比較）分岐

```jal
if_icmpeq label     // int1 == int2
if_icmpne label     // int1 != int2
if_icmplt label     // int1 < int2
if_icmple label     // int1 <= int2
if_icmpgt label     // int1 > int2
if_icmpge label     // int1 >= int2
if_acmpeq label     // オブジェクト参照同一性
if_acmpne label     // オブジェクト参照不一致
```

#### 無条件ジャンプ

```jal
goto label          // ラベルへジャンプ
goto_w label        // 遠距離ジャンプ
```

**例: FizzBuzz（サンプル）**

（省略せず元のコードを参照）

### インクリメント命令

```jal
iinc index value    // ローカル変数に value を加算
```

**例:**
```jal
iinc 0 1            // i++
iinc 1 -1           // j--
iinc 2 10           // k += 10
```

### 比較命令

```jal
lcmp                // long の比較
fcmpg, fcmpl        // float の比較（NaN の扱いに g/l が関係）
dcmpg, dcmpl        // double の比較
```

結果は -1（小さい），0（等しい），1（大きい）を返すことが想定されます。

---

## スイッチ文（Switch Statements）

### tableswitch（連続する整数ケース向け）

```jal
tableswitch start {
  case_label_1,
  case_label_2,
  case_label_3
} default default_label
```

**SwitchExample.jal の例を参照**

### lookupswitch（疎なケース向け）

```jal
lookupswitch {
  case_value_1 : label_1,
  case_value_2 : label_2,
  default : default_label
}
```

---

## 例外処理（Exception Handling）

### try-catch-finally 構造

JALでは例外処理領域をディレクティブで表現できます：

```jal
label: [~ startLabel
  , ExceptionType : catchLabel -> finallyLabel
] instruction;
```

### 要素

- `[~ startLabel` - 領域の開始を示す
- `, ExceptionType : catchLabel` - キャッチハンドラ（複数可）
- `-> finallyLabel` - finally ブロック（任意）
- `]` - 領域の終了

### 例

```jal
ProtectedBlock: [~ protectedStart,
    java/io/IOException: ioHandler -> cleanup, java/lang/Exception: generalHandler -> cleanup
] 
  getstatic java/lang/System -> out : Ljava/io/PrintStream;
  ldc "Executing protected code"
  invokevirtual java/io/PrintStream -> println (Ljava/lang/String;)V
  goto afterCleanup
  
ioHandler:
  getstatic java/lang/System -> out : Ljava/io/PrintStream;
  ldc "IOException caught"
  invokevirtual java/io/PrintStream -> println (Ljava/lang/String;)V
  goto cleanup
  
generalHandler:
  getstatic java/lang/System -> out : Ljava/io/PrintStream;
  ldc "Exception caught"
  invokevirtual java/io/PrintStream -> println (Ljava/lang/String;)V
  
cleanup:
  getstatic java/lang/System -> out : Ljava/io/PrintStream;
  ldc "Finally block"
  invokevirtual java/io/PrintStream -> println (Ljava/lang/String;)V
  
afterCleanup:
  return
```


---

## ラベルとローカル変数情報

### ラベル

ラベルは分岐命令やswitchのターゲットとして使用する位置の目印です：

```jal
LabelName:
  // 命令列
```

使用例：`goto`, 条件分岐, switch

### ローカル変数情報

ローカル変数への格納時にメタ情報を添えることができます：

```jal
istore_1 [I -> varName]
```

要素：
- 型記述子（任意）: 期待される型の説明
- ラベル範囲（任意）: 変数が有効なスコープ
- 変数名: デバッグや可読性のための識別子

**構文:**

```jal
[TypeDescriptor? (~ labelName)? -> VariableName]
```

**例:**

```jal
public method (II)I {
  istore_1 [I -> firstArg]
  istore_2 [I -> secondArg]
  
  iconst_0
  istore_3 [I -> result]
  
  // ...
}
```

---

## 実例（Examples）

ここでは `examples/` ディレクトリから取ったサンプルを用い，動作と構成を解説します。コードは英語のまま（JAL構文）を示します。

### 例1: HelloWorld（簡単な出力）

```jal
public class HelloWorld (
  major_version=55,
  minor_version=0) {

  public static main([Ljava/lang/String;)V {
    // Print "Hello, World!"
    getstatic java/lang/System->out:Ljava/io/PrintStream;
    ldc "Hello, World!"
    invokevirtual java/io/PrintStream->println(Ljava/lang/String;)V

    // Return from main
    return
  }
}
```

上記は次を行います：
1. static フィールド `System.out`（PrintStream）を読み込む
2. 文字列定数を積む
3. `println()` を呼び出す
4. 戻る（void）

### 例2: FizzBuzz（ループと条件分岐）

（サンプルコードは元の英語例を参照）

重要パターン：
- ローカル変数の使用（`istore_1`, `iload_1`）
- 剰余（`irem`）による条件判定
- 条件分岐命令（`ifne` など）
- ループは `goto` とラベルで実現
- メソッド呼び出しは `invokevirtual`

### 例3: BubbleSort（配列の操作）

（サンプルコードは元の英語例を参照）

重要パターン：
- 配列作成: `newarray I`
- 配列要素アクセス: `iaload`, `iastore` など
- 配列長取得: `arraylength`
- ラベルを用いたネストループ

### 例4: TableSwitch（switch）

（サンプルコードは元の英語例を参照）

- `tableswitch` は連続した整数ケースに最適
- `lookupswitch` は疎なケース向け
- いずれもフォールスルーを防ぐために `goto` を使う設計が一般的

---

## ベストプラクティス

1. **説明的なラベルを使う**: 分岐先を明確にする
   ```jal
   OuterLoopStart:    // 良い
   L1:                // 回避したい
   ```

2. **ローカル変数を注釈する**: ローカル変数情報を使って可読性を高める
   ```jal
   istore_1 [I -> loopCounter]
   astore_2 [-> currentElement]
   ```

3. **メソッドは単一責務にする**: 単一の目的に集中させる

4. **フィールドの初期化とメタデータ**: バージョン互換性を示すためにクラスメタを使う
   ```jal
   public class MyClass (major_version=55, minor_version=0)
   ```

5. **最適な命令フォームを使う**: 可能な場合は最適化されたショートフォームを使用
   ```jal
   iload_0         // 推奨: 最適化されている
   iload 0         // 使用可能だが一般形
   ```

6. **複雑なアルゴリズムはコメントで説明**: ループやスワップ等は注釈をつける

---

## まとめ

JALはJVMバイトコードを表現するための，人間に読みやすい完全な構文を提供します。主な特徴：

- クラスレベルの構造（メタデータ，フィールド，メソッド）
- 豊富な型システム（プリミティブ，オブジェクト，配列）
- 200以上の命令（スタック，算術，制御，メソッド呼び出し等）
- 例外処理（try-catch-finally）ディレクティブ
- デバッグに役立つローカル変数情報とラベル

これらを組み合わせることで，可読性を保ちながら低レベルのJVMコードを記述できます。


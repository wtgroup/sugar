package com.wtgroup.sugar.unit;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * 大小单位
 *
 * @author Nisus Liu
 * @version 0.0.1
 * @email liuhejun108@163.com
 * @date 2018/11/30 17:18
 */
public class Byter implements Comparable<Byter>, Serializable {
    /**
     * Bytes->KB->MB...的进制大小
     */
    private static final int K = 1024;
    //<editor-fold desc="计算机常用的存储单位">
    // 8 bit = 1 Byte 一字节
    //1024 B = 1 KB （KiloByte） 千字节
    //1024 KB = 1 MB （MegaByte） 兆字节
    //1024 MB = 1 GB （GigaByte） 吉字节
    //1024 GB = 1 TB （TeraByte） 太字节
    //1024 TB = 1 PB （PetaByte） 拍字节
    //1024 PB = 1 EB （ExaByte） 艾字节
    //1024 EB = 1 ZB （ZetaByte） 皆字节
    //1024 ZB = 1 YB （YottaByte） 佑字节
    //1024 YB = 1BB（Brontobyte）珀字节
    //1024 BB = 1 NB （NonaByte） 诺字节
    //1024 NB = 1 DB （DoggaByte）刀字节
    //</editor-fold>
    static final int BIT_PER_BYTE = 8;
   // static final long KB = K;
   // static final long MB = KB * K;
   // static final long GB = MB * K;
   // static final long TB = GB * K;
   // static final long PB = TB * K;
   // static final long B_PER_EB = PB * K;
   // static final BigInteger B_PER_ZB = BigInteger.valueOf(B_PER_EB * K);
   // static final BigInteger B_PER_YB = BigInteger.valueOf(B_PER_ZB * K);
   // static final BigInteger B_PER_BB = BigInteger.valueOf(B_PER_YB * K);
   // static final BigInteger B_PER_NB = BigInteger.valueOf(B_PER_BB * K);
   // static final BigInteger B_PER_DB = BigInteger.valueOf(B_PER_NB * K);

    enum ByteUnit {
        // Byte
        B(1, "B"),
        // Kilobyte
        KB(K, "KB"),
        // Megabyte
        MB(K*KB.value,"MB"),
        // Gigabyte
        GB(K*MB.value,"GB"),
        // Trillionbyte
        TB(K*GB.value,"TB"),
        // Petabyte
        PB(K*TB.value,"PB"),
        // Exabyte
        EB(K*PB.value,"EB"),
        ;
        private long value;
        private String unit;

        ByteUnit(long size, String unit) {
            this.value = size;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return value+unit;
        }
    }


    private static final Byter ZERO = new Byter(0);
    private static final DecimalFormat THREE_DECIMALS_FORMATER = new DecimalFormat("0.000");

    private final long bytes;

    private Byter(long bytes) {
        this.bytes = bytes;
    }

    public static Byter ofB(long bytes) {
        if (bytes == 0) {
            return ZERO;
        }
        return new Byter(bytes);
    }

    public static Byter ofKB(long kb) {
        if (kb == 0) {
            return ZERO;
        }
        return new Byter(Math.multiplyExact(ByteUnit.KB.value, kb));
    }

    public static Byter ofMB(long mb) {
        if (mb == 0) {
            return ZERO;
        }
        return new Byter(Math.multiplyExact(ByteUnit.MB.value, mb));
    }

    public static Byter ofGB(long gb) {
        if (gb == 0) {
            return ZERO;
        }
        return new Byter(Math.multiplyExact(ByteUnit.GB.value, gb));
    }

    public static Byter ofTB(long tb) {
        if (tb == 0) {
            return ZERO;
        }
        return new Byter(Math.multiplyExact(ByteUnit.TB.value, tb));
    }

    public static Byter ofPB(long pb) {
        if (pb == 0) {
            return ZERO;
        }
        return new Byter(Math.multiplyExact(ByteUnit.PB.value, pb));
    }

    public static Byter ofEB(long eb) {
        if (eb == 0) {
            return ZERO;
        }
        return new Byter(Math.multiplyExact(ByteUnit.EB.value, eb));
    }


    public long toB() {
        return bytes;
    }

    public float toKB() {
        return (float)bytes / ByteUnit.KB.value;
    }

    public float toMB() {
        return (float)bytes / ByteUnit.MB.value;
    }

    public float toGB() {
        return (float)bytes / ByteUnit.GB.value;
    }

    public float toTB() {
        return (float)bytes / ByteUnit.TB.value;
    }

    public float toPB() {
        return (float)bytes / ByteUnit.PB.value;
    }

    public float toEB() {
        return (float)bytes / ByteUnit.EB.value;
    }


    @Override
    public int compareTo(@NotNull Byter otherByter) {
        return Long.compare(this.bytes, otherByter.bytes);
    }

    // todo 简单的保留 3 位小数的string , 改为 'toShortString', toString 仿照 Duration , 1G2M3K4B
    @Override
    public String toString() {
        float size = 0;
        String unit = "B";
        if (bytes < ByteUnit.KB.value) {
            size=this.toB();
            unit = ByteUnit.B.unit;
        } else if (bytes < ByteUnit.MB.value) {
            size=this.toKB();
            unit = ByteUnit.KB.unit;
        } else if (bytes < ByteUnit.GB.value) {
            size=this.toMB();
            unit = ByteUnit.MB.unit;
        } else {
            size=this.toGB();
            unit = ByteUnit.GB.unit;
        }

        return THREE_DECIMALS_FORMATER.format(size) + unit;
    }


}

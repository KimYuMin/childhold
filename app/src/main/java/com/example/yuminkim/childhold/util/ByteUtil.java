package com.example.yuminkim.childhold.util;

public class ByteUtil {
    public static int byte1ToInt(byte b){
        return (int)(b&0xff);
    }
    public static int byte1ToInt(byte[] b, int offset){
        return (int)(b[offset]&0xff);
    }
    public static int byte2ToInt(byte[] buffer, int offset){
        return (buffer[offset+1]&0xff)<<8 | (buffer[offset]&0xff);
    }
    public static int byte2ToIntBigEndian(byte[] buffer, int offset) {
        return (buffer[offset+1]&0xff) | (buffer[offset]&0xff)<<8;

    }
    public static int byteToInt(byte[] buffer){
        return byteToInt(buffer, 0);
    }
    public static int byteToInt(byte[] buffer, int offset) {
        return (buffer[offset + 3] & 0xff) << 24 | (buffer[offset + 2] & 0xff) << 16 | (buffer[offset + 1] & 0xff) << 8 | (buffer[offset] & 0xff);
    }
    public static int byteToIntBigEndian(byte[] buffer, int offset) {
        return (buffer[offset] & 0xff) << 24 | (buffer[offset + 1] & 0xff) << 16 | (buffer[offset + 2] & 0xff) << 8 | (buffer[offset+3] & 0xff);
    }
    /**
     * short type을 byte로 변형
     **/
    public static byte[] shortToByte(short shortVar){
        byte littleShort[] = new byte[2];
        littleShort[0] = (byte)((shortVar>>0) & 0xff);
        littleShort[1] = (byte)((shortVar>>8) & 0xff);
        return littleShort;
    }
    /**
     * int type을 byte로 변형
     **/
    public static byte[] intToByte(int intVar){
        byte littleInt[] = new byte[4];
        littleInt[0] = (byte)((intVar>>0) & 0xff);
        littleInt[1] = (byte)((intVar>>8) & 0xff);
        littleInt[2] = (byte)((intVar>>16) & 0xff);
        littleInt[3] = (byte)((intVar>>24) & 0xff);
        return littleInt;
    }
    /**
     * int type을 byte로 변형
     **/
    public static byte[] intToByte2(int intVar){
        byte littleInt[] = new byte[2];
        littleInt[0] = (byte)((intVar>>0) & 0xff);
        littleInt[1] = (byte)((intVar>>8) & 0xff);
        return littleInt;
    }

    /**
     * long type을 byte로 변형
     **/
    public static byte[] longToByte(long longVar){
        byte littleLong[] = new byte[8];
        littleLong[0] = (byte)((longVar>>0) & 0xff);
        littleLong[1] = (byte)((longVar>>8) & 0xff);
        littleLong[2] = (byte)((longVar>>16) & 0xff);
        littleLong[3] = (byte)((longVar>>24) & 0xff);
        littleLong[4] = (byte)((longVar>>32) & 0xff);
        littleLong[5] = (byte)((longVar>>40) & 0xff);
        littleLong[6] = (byte)((longVar>>48) & 0xff);
        littleLong[7] = (byte)((longVar>>56) & 0xff);
        return littleLong;
    }

    public static long toLong(byte[] data) {

        if (data == null || data.length != 8)

            return 0x0;

        return (long)((long)(0xff & data[0]) << 56 |

                (long)(0xff & data[1]) << 48 |

                (long)(0xff & data[2]) << 40 |

                (long)(0xff & data[3]) << 32 |

                (long)(0xff & data[4]) << 24 |

                (long)(0xff & data[5]) << 16 |

                (long)(0xff & data[6]) << 8 |

                (long)(0xff & data[7]) << 0 );

    }

    public static long toLong32(byte[] data) {

        if (data == null || data.length != 4)

            return 0x0;

        return (long)((long)(0xff & data[3]) << 40 |

                (long)(0xff & data[2]) << 32 |

                (long)(0xff & data[1]) << 24 |

                (long)(0xff & data[0]) << 16 );

    }
}
package com.idev4.reports.util;

public class Constant {
    public final static String REG_20 = "10.0201001.000.000.203802.00000.0000"; // LAHORE
    public final static String REG_22 = "10.0301011.000.000.205450.00000.0000"; //FAISALABAD
    public final static String REG_21 = "10.0301021.000.000.205451.00000.0000"; //SAHIWAL
    public final static String REG_23 = "10.0301031.000.000.205452.00000.0000"; //SARGODHA
    public final static String REG_24 = "10.0401011.000.000.205453.00000.0000"; //GUJRANWALA
    public final static String REG_35 = "10.0201001.000.000.203802.00000.0000"; //ISLAMABAD
    public final static String REG_26 = "10.0501001.000.000.204800.00000.0000"; //MULTAN
    public final static String REG_27 = "10.0501011.000.000.205456.00000.0000"; //BAHAWALPUR
    public final static String REG_28 = "10.0601001.000.000.205200.00000.0000"; //SINDH
    public final static String REG_29 = "10.0601011.000.000.205455.00000.0000"; //KARACHI
    public final static String REG_25 = "10.0401051.000.000.205457.00000.0000"; //KPK

    public static String getRegionInterunitCode(int regSeq) {
        String code = "";
        switch (regSeq) {
            case 20:
                code = REG_20;
                break;
            case 21:
                code = REG_21;
                break;
            case 22:
                code = REG_22;
                break;
            case 23:
                code = REG_23;
                break;
            case 24:
                code = REG_24;
                break;
            case 25:
                code = REG_25;
                break;
            case 26:
                code = REG_26;
                break;
            case 27:
                code = REG_27;
                break;
            case 28:
                code = REG_28;
                break;
            case 29:
                code = REG_29;
                break;
            case 35:
                code = REG_35;
                break;
            default:
                code = "";
                break;
        }
        return code;
    }
}

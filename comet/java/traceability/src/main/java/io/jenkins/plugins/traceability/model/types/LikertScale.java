package io.jenkins.plugins.traceability.model.types;

public enum LikertScale {

    STRONGLY_DISAGREE, DISAGREE, UNDECIDED, AGREE, STRONGLY_AGREE, NONE;

    /**
     * Values are related to the ones in io.jenkins.plugins.traceability.HtmlHelper.java
     *
     * @param value
     * @return
     */

//    public static LikertScale getLikerScale(double value) {
//        return getLikerScale(getLikerScaleInt(value + ""));
//    }

    public static double getLikerScaleDouble(String value) {
        return getLikerScaleDouble(Integer.parseInt(value));
    }

    public static double getLikerScaleDouble(int value) {
        switch (value) {
            case 1:
                return 0.000001;
            case 2:
                return 0.25;
            case 3:
                return 0.5;
            case 4:
                return 0.75;
            case 5:
                return 0.999999;
            default:
                return -1;
        }
    }

    public static LikertScale getLikerScale(int value) {
        switch (value) {
            case 1:
                return STRONGLY_DISAGREE;
            case 2:
                return DISAGREE;
            case 3:
                return UNDECIDED;
            case 4:
                return AGREE;
            case 5:
                return STRONGLY_AGREE;
            default:
                return NONE;
        }
    }

    public static int getLikerScale(String value) {
        if(value == null){
            return -1;
        }
        switch (value) {
            case "STRONGLY_DISAGREE":
                return 1;
            case "DISAGREE":
                return 2;
            case "UNDECIDED":
                return 3;
            case "AGREE":
                return 4;
            case "STRONGLY_AGREE":
                return 5;
            default:
                return -1;
        }
    }
}

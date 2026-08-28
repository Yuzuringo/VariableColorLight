package jp.example.variablecolorlight;

public interface ColoredLightData {
    int getKelvin(); int getLevel(); boolean isRgbMode();
    int getRed(); int getGreen(); int getBlue();
    void setKelvin(int value); void setLevel(int value); void setRgbMode(boolean value);
    void setRgb(int r,int g,int b); float[] getColor();
    boolean isRedstoneControlled(); void setRedstoneControlled(boolean value);
}

package jp.example.variablecolorlight;

/** Side-neutral color conversion; safe to load on dedicated servers. */
public final class ColorUtil {
    private ColorUtil() {}
    public static float[] kelvinToRgb(int kelvin) {
        double t=kelvin/100.0,r,g,b;
        if(t<=66){r=255;g=99.4708025861*Math.log(t)-161.1195681661;b=t<=19?0:138.5177312231*Math.log(t-10)-305.0447927307;}
        else {r=329.698727446*Math.pow(t-60,-0.1332047592);g=288.1221695283*Math.pow(t-60,-0.0755148492);b=255;}
        return new float[]{clamp(r/255),clamp(g/255),clamp(b/255)};
    }
    private static float clamp(double v){return(float)Math.max(0,Math.min(1,v));}
}

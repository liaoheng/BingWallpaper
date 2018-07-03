package me.liaoheng.wallpaper.util;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author liaoheng
 * @version 2018-05-30 11:40
 * @see <a href="https://www.jianshu.com/p/ba9347a5a05a">jianshu</a>
 */
public class ROM {
    private static final String TAG = ROM.class.getSimpleName();

    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_QIKU = "QIKU";
    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    private static ROM ROM;

    public static ROM getROM() {
        if (ROM == null) {
            String version;
            String name = Build.MANUFACTURER;
            if (!TextUtils.isEmpty(version = getProp(KEY_VERSION_MIUI))) {
                ROM = createMIUI(name, version);
            } else if (!TextUtils.isEmpty(version = getProp(KEY_VERSION_EMUI))) {
                ROM = createEMUI(name, version);
            } else if (!TextUtils.isEmpty(version = getProp(KEY_VERSION_OPPO))) {
                ROM = createOPPO(name, version);
            } else if (!TextUtils.isEmpty(version = getProp(KEY_VERSION_VIVO))) {
                ROM = createVIVO(name, version);
            } else {
                version = Build.DISPLAY;
                if (ROM_QIKU.equalsIgnoreCase(name)) {
                    ROM = createQIKU(name, version);
                } else if (version.toUpperCase().contains(ROM_FLYME)) {
                    ROM = createFLYME(name, version);
                } else {
                    ROM = createOTHER(name, version);
                }
            }
            Log.d(TAG, "getROM : " + ROM);
        }
        return ROM;
    }

    public static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e("getProp", "Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    private static final int MIUI = 1;
    private static final int EMUI = 2;
    private static final int OPPO = 3;
    private static final int VIVO = 4;
    private static final int QIKU = 5;
    private static final int FLYME = 6;
    private static final int OTHER = 0;
    private int rom;
    private String name;
    private String version;

    public ROM(int rom, String name, String version) {
        this.rom = rom;
        this.name = name;
        this.version = version;
    }

    public int getRom() {
        return rom;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "ROM{" +
                "rom=" + rom +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public static ROM createMIUI(String name, String version) {
        return new ROM(MIUI, name, version);
    }

    public static ROM createEMUI(String name, String version) {
        return new ROM(EMUI, name, version);
    }

    public static ROM createOPPO(String name, String version) {
        return new ROM(OPPO, name, version);
    }

    public static ROM createVIVO(String name, String version) {
        return new ROM(VIVO, name, version);
    }

    public static ROM createQIKU(String name, String version) {
        return new ROM(QIKU, name, version);
    }

    public static ROM createFLYME(String name, String version) {
        return new ROM(FLYME, name, version);
    }

    public static ROM createOTHER(String name, String version) {
        return new ROM(OTHER, name, version);
    }

    public boolean check(int rom) {
        return getROM().rom == rom;
    }

    public boolean check(String name) {
        return name.equalsIgnoreCase(getROM().name);
    }

    public boolean isEmui() { return check(EMUI); }

    public boolean isMiui() { return check(MIUI); }

    public boolean isVivo() { return check(VIVO); }

    public boolean isOppo() { return check(OPPO); }

    public boolean isFlyme() { return check(FLYME); }

    public boolean is360() { return check(QIKU) || check("360"); }

}

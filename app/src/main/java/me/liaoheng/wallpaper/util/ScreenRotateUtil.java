package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 屏幕根据重力感应旋转的工具类
 *
 * @author Pinger
 * @author liaoheng
 * @version 2018-06-05 10:41
 * @see <a href="https://www.jianshu.com/p/0028a4428ffa">jianshu</a>
 * @since 2017/2/16 上午 11:31
 */
public class ScreenRotateUtil {
    private static final String TAG = ScreenRotateUtil.class.getSimpleName();

    private ScreenRotateUtil() {
    }

    private static ScreenRotateUtil mInstance;

    private boolean isLandscape = false;      // 默认是竖屏

    private SensorManager sm;
    private OrientationSensorListener listener;
    private Sensor sensor;

    /**
     * 接收重力感应监听的结果，来改变屏幕朝向
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 888) {
                int orientation = msg.arg1;
                try {
                    //根据手机屏幕的朝向角度，来设置内容的横竖屏，并且记录状态
                    if (orientation > 45 && orientation < 135) {
                        isLandscape = true;
                    } else if (orientation > 135 && orientation < 225) {
                        isLandscape = false;
                    } else if (orientation > 225 && orientation < 315) {
                        isLandscape = true;
                    } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                        isLandscape = false;
                    }
                    if (mCallBack != null) {
                        mCallBack.setRequestedOrientation(isLandscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                } catch (IllegalStateException ignore) {
                }
            }
        }
    };

    private ScreenRotateCallBack mCallBack;

    public interface ScreenRotateCallBack {
        void setRequestedOrientation(int requestedOrientation);
    }

    /**
     * 初始化，获取实例
     */
    public static ScreenRotateUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ScreenRotateUtil.class) {
                if (mInstance == null) {
                    mInstance = new ScreenRotateUtil(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化重力感应传感器
     */
    private ScreenRotateUtil(Context context) {
        // 初始化重力感应器
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        listener = new OrientationSensorListener(mHandler);
    }

    /**
     * 重力感应监听者
     */
    public class OrientationSensorListener implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        public static final int ORIENTATION_UNKNOWN = -1;

        private Handler rotateHandler;

        public OrientationSensorListener(Handler handler) {
            rotateHandler = handler;
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];
            float magnitude = X * X + Y * Y;
            // Don't trust the angle if the magnitude is small compared to the y
            // value
            if (magnitude * 4 >= Z * Z) {
                // 屏幕旋转时
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }

            if (rotateHandler != null) {
                rotateHandler.obtainMessage(888, orientation, 0).sendToTarget();
            }
        }
    }

    /**
     * 当前屏幕朝向是否横屏
     */
    private boolean screenIsLandscape(int orientation) {
        return ((orientation > 45 && orientation <= 135) || (orientation > 225 && orientation <= 315));
    }

    /**
     * 当前屏幕朝向是否竖屏
     */
    private boolean screenIsPortrait(int orientation) {
        return (((orientation > 315 && orientation <= 360) || (orientation >= 0 && orientation <= 45))
                || (orientation > 135 && orientation <= 225));
    }

    /**
     * 开启监听
     * 绑定切换横竖屏Activity的生命周期
     */
    public void onCreate(ScreenRotateCallBack callBack) {
        mCallBack = callBack;
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * 注销监听
     */
    public void onDestroy() {
        sm.unregisterListener(listener);
        if (mHandler != null) {
            mHandler.removeMessages(888);
        }
    }

    /**
     * 当前屏幕的朝向，是否是横屏
     */
    public boolean isLandscape() {
        return this.isLandscape;
    }

}
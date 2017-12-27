package me.liaoheng.bingwallpaper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.liaoheng.common.util.UIUtils;

import me.liaoheng.bingwallpaper.R;

/**
 * @author liaoheng
 * @version 2017-12-27 21:47
 */
public class VersionPreference extends Preference {

    public VersionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public VersionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VersionPreference(Context context) {
        super(context);
    }

    private String mVersion;

    @Override
    protected View onCreateView(ViewGroup parent) {
        setWidgetLayoutResource(R.layout.preference_end_text);
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        TextView version = UIUtils.findViewById(view, R.id.pre_text);
        version.setText(mVersion);
        super.onBindView(view);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (defaultValue == null) {
            mVersion = "";
        } else {
            mVersion = getPersistedString(defaultValue.toString());
        }
    }

    public void setVersion(@NonNull String version) {
        mVersion = version;
        notifyChanged();
    }
}

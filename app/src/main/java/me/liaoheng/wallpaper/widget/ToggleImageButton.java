package me.liaoheng.wallpaper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.github.liaoheng.common.core.OnCheckedChangeListener;

import androidx.appcompat.widget.AppCompatImageButton;
import me.liaoheng.wallpaper.R;

/**
 * Toggle  imageButton
 *
 * @author liaoheng
 * @version 2016-11-4 13:53
 */
public class ToggleImageButton extends AppCompatImageButton implements Checkable {
    private OnCheckedChangeListener<ToggleImageButton> mOnCheckedChangeListener;
    private boolean mEnableAsync;
    private boolean mAsyncSelect;
    private boolean mEnableSelected;
    private Drawable mNormalDrawable;
    private Drawable mSelectedDrawable;

    public ToggleImageButton(Context context) {
        super(context);
    }

    public ToggleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ToggleImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.ToggleImageButton);
            mEnableAsync = a.getBoolean(R.styleable.ToggleImageButton_enableAsync, false);
            mEnableSelected = a.getBoolean(R.styleable.ToggleImageButton_enableSelected, false);
            if (mEnableSelected) {
                mNormalDrawable = a.getDrawable(R.styleable.ToggleImageButton_normalDrawableRes);
                if (mNormalDrawable != null) {
                    setImageDrawable(mNormalDrawable);
                }
                mSelectedDrawable = a.getDrawable(R.styleable.ToggleImageButton_selectedDrawableRes);
            }
            boolean checked = a.getBoolean(R.styleable.ToggleImageButton_checked, false);
            setSelected(checked);
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    /**
     * async时，得到上次操作的状态
     */
    public boolean isAsyncSelect() {
        return mAsyncSelect;
    }

    @Override
    public void setSelected(boolean selected) {
        if (mEnableAsync) {
            mAsyncSelect = selected;
        }
        if (mEnableSelected) {
            if (selected && mSelectedDrawable != null) {
                setImageDrawable(mSelectedDrawable);
            } else if (!selected && mNormalDrawable != null) {
                setImageDrawable(mNormalDrawable);
            }
        }
        super.setSelected(selected);
    }

    @Override
    public boolean isChecked() {
        return isSelected();
    }

    /**
     * async时，操作状态会被记录但不会改变控件状态，需自行调用{@link #setSelected(boolean)}改变。
     */
    @Override
    public void setChecked(boolean checked) {
        if (mEnableAsync) {
            mAsyncSelect = checked;
        } else {
            setSelected(checked);
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, checked);
            }
        }
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    /**
     * 打开async
     */
    public void enableAsync() {
        this.mEnableAsync = true;
    }

    /**
     * 关闭async
     */
    public void unableAsync() {
        this.mEnableAsync = false;
    }

    public void setOnCheckedChangeListener(
            OnCheckedChangeListener<ToggleImageButton> onCheckedChangeListener) {
        this.mOnCheckedChangeListener = onCheckedChangeListener;
    }
}


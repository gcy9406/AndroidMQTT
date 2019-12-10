package space.gcy.androidmqtt;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class DeleteEditText extends AppCompatAutoCompleteTextView {

    private Drawable mRightDrable; // 动态设置右边图片的显示
    // 控件是否获得焦点标志位
    boolean mIsHasFocus;

    public DeleteEditText(Context context) {
        super(context);
        init();
    }

    public DeleteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DeleteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 本方法 获得控件 左上右下 四个方位插入的图片
        Drawable[] drawables = this.getCompoundDrawables();
        mRightDrable = drawables[2];  //  0 1 2 3 所以就对应 左上 右 下
        
        // 添加文本改变监听
        this.addTextChangedListener(new TextWatcherImpl());
        // 添加触摸改变监听
        this.setOnFocusChangeListener(new OnFocusChangeListenerImpl());
        // 初始设置所有图片不可见
        setClearDrawableVisible(false);
    }

    private class TextWatcherImpl implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            boolean isNoNull = getText().toString().length() >= 1;
            setClearDrawableVisible(isNoNull);
        }
    }

    public class OnFocusChangeListenerImpl implements OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mIsHasFocus = hasFocus;
            if (mIsHasFocus) {
                // 如果获得焦点并且判断输入的内容不为空，则显示删除图标
                boolean isNoNull = getText().toString().length() >= 1;
                setClearDrawableVisible(isNoNull);
            } else {
                // 否则隐藏图标
                setClearDrawableVisible(false);
            }
        }
    }

    // 控制右边的图片显示与否
    private void setClearDrawableVisible(boolean isNoNull) {
        Drawable rightDrawable;
        if (isNoNull) {
            rightDrawable = mRightDrable;
        } else {
            rightDrawable = null;
        }
        // 使用代码设置改控件 left,top,right,bottom 处图标
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], rightDrawable,
                getCompoundDrawables()[3]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP: // 抬手指 事件
                // 删除图片右侧到 EditText 控件 最左侧距离
                int length1 = getWidth() - getPaddingRight();
                // 删除图片左侧到 EditText 控件最左侧距离
                int length2 = getWidth() - getTotalPaddingRight();
                // 判断 单击位置是否在图片上
                boolean isClean = (event.getX() > length2)
                        &&
                        (event.getX() < length1);
                if (isClean)
                    setText("");
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);

    }
}
package com.xiaoyu.dragviewgroup.library;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 小于 on 2016/7/7.
 */
public class DragViewGroup extends ViewGroup {

    private BaseAdapter mAdapter;

    private List<Integer> childRowHeights; //每一行的子元素在父容器中的高度

    private boolean isScroll = false; //是否可以滑动的

    private List<List<View>> viewss;

    private int maxHeight;	//子元素的总高度

    private int maxScrollHeight;	//最大的移动的高度

    private View mDragView;  //被拖动的子元素

    private static final long DRAGDURATION = 300;   //长按的时间

    public DragViewGroup(Context context) {
        this(context, null, 0);
    }

    public DragViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        prepare();
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    private void prepare(){
        setChildrenDrawingOrderEnabled(true);
        childRowHeights = new ArrayList<>();
        viewss = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childRowHeights.clear();
        viewss.clear();
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int width = getWidthMeasureSpec(widthMeasureSpec);
        int height = getHeightMeasureSpec(heightMeasureSpec);
        maxScrollHeight = maxHeight - height;
        setMeasuredDimension(width, height);
    }

    private int getWidthMeasureSpec(int widthMeasureSpec){

        int result = 0;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                result = getMaxWidth(size);
                break;
            case MeasureSpec.EXACTLY:
                getHeightList(size);
                result = size;
                break;
        }

        return result;
    }

    private void getHeightList(int size){
        int childCount = getChildCount();
        int tempWidth = 0;
        int tempHeight = 0;
        List<View> views = new ArrayList<View>();
        for(int i = 0; i < childCount; i++){
            View child = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams)child.getLayoutParams();
            tempWidth += (params.leftMargin + params.rightMargin + child.getMeasuredWidth());
            int childHeight = (params.topMargin + params.bottomMargin + child.getMeasuredHeight());
            tempHeight = tempHeight > childHeight ? tempHeight : childHeight;
            if(tempWidth > size){
                tempWidth = 0;
                childRowHeights.add(tempHeight);
                tempHeight = 0;
                viewss.add(views);
                views = new ArrayList<View>();
                i--;
            }else{
                views.add(child);
            }
        }
        if(tempWidth > 0){
            viewss.add(views);
        }
        if(tempHeight > 0){
            childRowHeights.add(tempHeight);
        }
    }

    //获得本身最大的宽度
    private int getMaxWidth(int maxSize){
        int result = 0;
        int tempWidth = 0;
        int tempHeight = 0;
        int childCount = getChildCount();
        List<View> views = new ArrayList<>();
        for(int i = 0; i < childCount; i++){
            View child = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams)child.getLayoutParams();
            tempWidth += (params.leftMargin + params.rightMargin + child.getMeasuredWidth());
            int childHeight = (params.topMargin + params.bottomMargin + child.getMeasuredHeight());
            if(tempWidth > result && tempWidth < maxSize){
                views.add(child);
                result = tempWidth;
                tempHeight = tempHeight > childHeight ? tempHeight : childHeight;
            }else if(tempWidth >= maxSize){
                tempWidth = 0;
                i--;
                childRowHeights.add(tempHeight);
                viewss.add(views);
                views = new ArrayList<View>();
            }else{
                views.add(child);
                tempHeight = tempHeight > childHeight ? tempHeight : childHeight;
            }
        }

        if(views.size() > 0){
            viewss.add(views);
        }
        if(tempHeight > 0){
            childRowHeights.add(tempHeight);
        }

        return result;
    }

    private int getHeightMeasureSpec(int heightMeasureSpec){
        int result = 0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                result = getMaxHeight(size);
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                isScroll = getScrollState(size);
                break;
        }

        return result;
    }

    private boolean getScrollState(int size) {
        maxHeight = 0;
        for(Integer temp : childRowHeights){
            maxHeight += temp;
        }

        return maxHeight > size;
    }

    //获得本身最大高度
    private int getMaxHeight(int maxSize){
        int result = 0;
        maxHeight = 0;
        for(Integer temp : childRowHeights){
            maxHeight += temp;
        }
        if(isScroll = maxHeight > maxSize){
            result = maxSize;
        }else{
            result = maxHeight;
        }

        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int leftOffset = 0;
        int topOffset = 0;
        int rows = viewss.size();
        for(int i = 0; i < rows; i++){
            List<View> views = viewss.get(i);
            for(View view : views){
                MarginLayoutParams params = (MarginLayoutParams)view.getLayoutParams();
                int lc = leftOffset + params.leftMargin;
                int rc = lc + view.getMeasuredWidth();
                int tc = topOffset + params.topMargin;
                int bc = tc + view.getMeasuredHeight();
                view.layout(lc, tc, rc, bc);
                leftOffset += (params.leftMargin + params.rightMargin + view.getMeasuredWidth());
            }
            leftOffset = 0;
            topOffset += childRowHeights.get(i);
        }
    }

    //让可以拖动的View显示在最上面
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mDragView != null) {
            int mPosition = indexOfChild(mDragView);
            if (i == childCount - 1) {
                return mPosition;
            }
            if (i == mPosition) {
                return childCount - 1;
            }
        }

        return super.getChildDrawingOrder(childCount, i);
    }

    /**
     * 根据传入的adapter添加view
     * @param baseAdapter
     */
    public void setAdapter(BaseAdapter baseAdapter) {

        if(mAdapter != null){
            mAdapter.unregisterDataSetObserver(observer);
        }

        mAdapter = baseAdapter;
        mAdapter.registerDataSetObserver(observer);

        getAdapterView();

    }

    private DataSetObserver observer = new DataSetObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            getAdapterView();
        }

    };

    private void getAdapterView(){
        removeAllViews();

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View view = mAdapter.getView(i, null, this);
            addView(view);
        }
        requestLayout();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private float downX;
    private float downY;
    private boolean isMove; //用来判断是否是点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mDragView == null) {
            if(isScroll){
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        isMove = false;
                        postDelayed(mDragRun, DRAGDURATION);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isMove = true;
                        removeCallbacks(mDragRun);
                        float currentY = event.getY();
                        float offsetY = downY - currentY;
                        downY = currentY;

                        if(getScrollY() + offsetY < 0){	//控制上边界
                            offsetY = 0 - getScrollY();
                        }else if(getScrollY() + offsetY > maxScrollHeight){	//控制下边界
                            offsetY = maxScrollHeight - getScrollY();
                        }

                        scrollBy(0, (int)offsetY);
                        break;
                    case MotionEvent.ACTION_UP:
                        removeCallbacks(mDragRun);
                        if (!isMove && mItemClickListener != null) {
                            View clickView = findPositionView((int)event.getX(), (int)event.getY() + getScrollY());
                            if (clickView != null) {
                                mItemClickListener.onItemClickListener(indexOfChild(clickView), clickView);
                            }
                        }
                        break;
                }

                return true;
            }else{
                return super.onTouchEvent(event);
            }
        }else{
            switch(event.getAction()){

                case MotionEvent.ACTION_MOVE:
                    float currentX = event.getX();
                    float currentY = event.getY();
                    float offsetX = currentX - downX;
                    float offsetY = currentY - downY;
                    downX = currentX;
                    downY = currentY;

                    ViewCompat.offsetLeftAndRight(mDragView, (int)offsetX);
                    ViewCompat.offsetTopAndBottom(mDragView, (int) offsetY);
                    removeCallbacks(mDragScrollRun);
                    post(mDragScrollRun);
                    break;
                case MotionEvent.ACTION_UP:
                    removeCallbacks(mDragScrollRun);
                    View changeView = findPositionView((mDragView.getLeft() + mDragView.getRight()) /2, (mDragView.getTop() + mDragView.getBottom()) / 2);
                    if (changeView != null) {
                        int first = indexOfChild(mDragView);
                        int second = indexOfChild(changeView);
                        exchangeChild(first, second);
                        if (mChildPositionChangeListener != null) {
                            mChildPositionChangeListener.onChildPositionChange(first, mDragView, second, changeView);
                        }
                    }else{
                        requestLayout();;
                    }
                    playCancelAnimation(mDragView);

                    mDragView = null;
                    break;
            }

            return true;
        }
    }

    //改变子元素的顺序
    private void exchangeChild(int index1, int index2){
        Class<?> cls = null;
        try {
            cls = Class.forName("android.view.ViewGroup");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            Field field = cls.getDeclaredField("mChildren");
            field.setAccessible(true);
            View[] childs = (View[]) field.get(this);

            if(childs.length > index1 && childs.length > index2){
                View temp = childs[index1];
                childs[index1] = childs[index2];
                childs[index2] = temp;
            }
            requestLayout();

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private View findPositionView(int x, int y) {

        int childCount = getChildCount();

        for (int i = childCount - 1; i >= 0; --i) {
            View child = getChildAt(i);
            if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()) {
                if (child != mDragView) {
                    return child;
                }
            }
        }

        return null;
    }

    private Runnable mDragRun = new Runnable() {
        @Override
        public void run() {
            mDragView = findPositionView((int)downX + getScrollX(), (int)downY + getScrollY());
            if (mDragView != null) {
                playStartAnimation(mDragView);
                invalidate();
            }
        }
    };

    private int vDragScroll = 5;
    private Runnable mDragScrollRun = new Runnable() {
        @Override
        public void run() {
            if(mDragView != null) {
                if (getHeight() + getScrollY() < mDragView.getBottom() && getScrollY() < maxScrollHeight) {
                    scrollBy(0, vDragScroll);
                    ViewCompat.offsetTopAndBottom(mDragView, vDragScroll);
                    post(mDragScrollRun);
                } else if (mDragView.getTop() < getScrollY() && getScrollY() > 0) {
                    scrollBy(0, -vDragScroll);
                    ViewCompat.offsetTopAndBottom(mDragView, -vDragScroll);
                    post(mDragScrollRun);
                }
            }
        }
    };

    public interface AnimationCallBack{
        long getDuration();

        Animator getStartAnimator(View view);

        Animator getCancelAnimator(View view);
    }

    //定义自己想要的动画效果
    public void setAnimationCallBack(AnimationCallBack callBack){
        this.mAnimationCallBack = callBack;
    }

    private AnimationCallBack mAnimationCallBack = new AnimationCallBack() {
        @Override
        public long getDuration() {
            return 200;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public Animator getStartAnimator(View view) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(ObjectAnimator.ofFloat(view, "rotationX", 0, 0),
                    ObjectAnimator.ofFloat(view, "rotationY", 0, 0),
                    ObjectAnimator.ofFloat(view, "translationX", 0, 0),
                    ObjectAnimator.ofFloat(view, "translationY", 0, 0),
                    ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f),
                    ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f),
                    ObjectAnimator.ofFloat(view, "alpha", 1, 0.7f));
            return set;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public Animator getCancelAnimator(View view) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(ObjectAnimator.ofFloat(view, "rotationX", 0, 0),
                    ObjectAnimator.ofFloat(view, "rotationY", 0, 0),
                    ObjectAnimator.ofFloat(view, "translationX", 0, 0),
                    ObjectAnimator.ofFloat(view, "translationY", 0, 0),
                    ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1.0f),
                    ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1.0f),
                    ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1));
            return set;
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void playStartAnimation(View view) {
        if (mAnimationCallBack != null) {
            Animator animator = mAnimationCallBack.getStartAnimator(view);
            if (animator != null) {
                animator.setDuration(mAnimationCallBack.getDuration()).start();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void playCancelAnimation(View view) {
        if (mAnimationCallBack != null) {
            Animator animator = mAnimationCallBack.getCancelAnimator(view);
            if (animator != null) {
                animator.setDuration(mAnimationCallBack.getDuration()).start();
            }
        }
    }

    public interface ChildPositionChangeListener{
        //第pos1位置的view1和第pos2位置的view2互换了位置
        void onChildPositionChange(int pos1, View view1, int pos2, View view2);
    }

    public interface ItemClickListener{
        void onItemClickListener(int pos, View view);
    }

    private ChildPositionChangeListener mChildPositionChangeListener;
    private ItemClickListener mItemClickListener;

    public void setChildPositionChangeListener(ChildPositionChangeListener listener){
        this.mChildPositionChangeListener = listener;
    }

    public void setItemClickListener(ItemClickListener listener){
        this.mItemClickListener = listener;
    }
}
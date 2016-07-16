# DragViewGroup
支持控件自由滑动，并且交换控件的位置。
***
##效果图
![Mou icon](https://github.com/yuyinghao/DragViewGroup/blob/master/show.gif)
##支持使用适配器设置子元素
`DragViewGroup.setAdapter(BaseAdapter);`
##拖动动画
可以支持拖动动画：

<pre>DragViewGroup.setAnimationCallBack(new DragViewGroup.AnimationCallBack() {
            @Override
            public long getDuration() {
                return 200;
            }

            @Override
            public Animator getStartAnimator(View view) {
                AnimatorSet set = new AnimatorSet();
                set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f),
                        ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f),
                        ObjectAnimator.ofFloat(view, "alpha", 1, 0.7f));
                return set;
            }

            @Override
            public Animator getCancelAnimator(View view) {
                AnimatorSet set = new AnimatorSet();
                set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1.0f),
                        ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1.0f),
                        ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1));
                return set;
            }
        });
        </pre>
##子元素调换位置回掉
可以设置子元素调换位置的监听：


`DragViewGroup.setChildPositionChangeListener(new DragViewGroup.ChildPositionChangeListener() {
            @Override
            public void onChildPositionChange(int pos1, View view1, int pos2, View view2) {
                Toast.makeText(MainActivity.this, "第" + pos1 + "个元素和第" + pos2 + "个元素交换了位置", Toast.LENGTH_SHORT).show();
            }
        });`

##子元素点击事件
可以设置子元素点击事件的监听：

`DragViewGroup.setItemClickListener(new DragViewGroup.ItemClickListener() {
            @Override
            public void onItemClickListener(int pos, View view) {
                Toast.makeText(MainActivity.this, "第" + pos + "被点击", Toast.LENGTH_SHORT).show();
            }
        });`

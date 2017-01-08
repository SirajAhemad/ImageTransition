package com.vpaliy.transition;

import android.animation.Animator;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.squareup.otto.Bus;
import com.vpaliy.transition.eventBus.EventBusProvider;
import com.vpaliy.transition.eventBus.TriggerVisibility;

import java.util.List;


public abstract class TransitionAnimation {

    private static final String TAG= TransitionAnimation.class.getSimpleName();

    private TransitionAnimation(){ /*Nothing is going on here */}

    public static final TransitionAnimation ENTER=new TransitionAnimation() {


        @Override
        public void runAnimation(final TransitionRunner.TransitionData data) {
            data.target.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    data.listenerList.add(new Listener(data));
                    data.target.getViewTreeObserver().removeOnPreDrawListener(this);
                    data.currentState= ImageState.newInstance(data.target);
                    makeTransformation(data);

                    data.target.animate()
                            .scaleX(1.f)
                            .scaleY(1.f)
                            .translationY(0f)
                            .translationX(0f)
                            .setInterpolator(data.interpolator)
                            .setDuration(data.animationDuration)
                            .setListener(provideListener(data.listenerList));

                    return true;
                }
            });
        }


    };


    private static Animator.AnimatorListener provideListener(final List<TransitionListener> listenerList) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if(listenerList!=null) {
                    for (TransitionListener listener : listenerList) {
                        listener.onAnimationStart(animator);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(listenerList!=null) {
                    for (TransitionListener listener : listenerList) {
                        listener.onAnimationEnd(animator);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                if(listenerList!=null) {
                    for (TransitionListener listener : listenerList) {
                        listener.onAnimationCancel(animator);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                if(listenerList!=null) {
                    for (TransitionListener listener : listenerList) {
                        listener.onAnimationRepeat(animator);
                    }
                }
            }
        };
    }

    public static final TransitionAnimation EXIT=new TransitionAnimation() {
        @Override
        public void runAnimation(TransitionRunner.TransitionData data) {

            data.listenerList.add(new Listener(data));

             float scaleX=data.prevState.width()/data.currentState.width();
             float scaleY=data.prevState.height()/data.currentState.height();


            final float deltaX=data.prevState.locationX()-data.currentState.locationX();
            final float deltaY=data.prevState.locationY()-data.currentState.locationY();


            /* Just to make sure it has appropriate properties, otherwise you get a bad behavior */
            if(data.animationState!= TransitionRunner.TransitionData.CANCELED) {
                data.target.setPivotX(0);
                data.target.setPivotY(0);

                data.target.setScaleX(1.f);
                data.target.setScaleY(1.f);

                data.target.setTranslationX(0f);
                data.target.setTranslationY(0f);
            }

            data.target.animate()
                    .scaleX(scaleX)
                    .scaleY(scaleY)
                    .setInterpolator(data.interpolator)
                    .setDuration(data.animationDuration)
                    .setListener(provideListener(data.listenerList))
                    .translationX(deltaX)
                    .translationY(deltaY);



        }
    };


    private static void makeTransformation(TransitionRunner.TransitionData data) {

        final float scaleX=data.prevState.width()/data.currentState.width();
        final float scaleY=data.prevState.height()/data.currentState.height();

        final float deltaX=data.prevState.locationX()-data.currentState.locationX();
        final float deltaY=data.prevState.locationY()-data.currentState.locationY();

        data.target.setPivotX(0);
        data.target.setPivotY(0);

        data.target.setScaleX(scaleX);
        data.target.setScaleY(scaleY);

        data.target.setTranslationX(deltaX);
        data.target.setTranslationY(deltaY);


    }

    private static class Listener extends TransitionListener {

        private TransitionRunner.TransitionData data;
        private Bus eventBus=EventBusProvider.defaultBus();

        public Listener(TransitionRunner.TransitionData data) {
            this.data = data;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            super.onAnimationStart(animator);
            if(data.startPosition>0) {
                eventBus.post(new TriggerVisibility(data.startPosition,false));
            }
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            super.onAnimationEnd(animator);
            if(data.startPosition>0) {
                eventBus.post(new TriggerVisibility(data.startPosition,true));
            }
        }
    }

    public abstract void runAnimation(TransitionRunner.TransitionData data);

}
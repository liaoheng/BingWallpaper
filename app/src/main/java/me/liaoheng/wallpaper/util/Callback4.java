package me.liaoheng.wallpaper.util;

/**
 * @author liaoheng
 * @date 2024-12-11 23:23
 */
public interface Callback4 <T>{
    void onPreExecute();
    void onYes(T t);
    void onNo(T t);
    void onFinish(T t);

    class EmptyCallback<T> implements Callback4<T>{

        @Override
        public void onPreExecute() {

        }

        @Override
        public void onYes(T t) {

        }

        @Override
        public void onNo(T t) {

        }

        @Override
        public void onFinish(T t) {

        }
    }
}

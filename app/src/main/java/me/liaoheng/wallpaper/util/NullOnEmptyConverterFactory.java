package me.liaoheng.wallpaper.util;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.EOFException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @see <a href="https://github.com/square/retrofit/issues/1554">github</a>
 */
public class NullOnEmptyConverterFactory extends Converter.Factory {

    public static NullOnEmptyConverterFactory create() {
        return new NullOnEmptyConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
        return (Converter<ResponseBody, Object>) body -> {
            try {
                return delegate.convert(body);
            } catch (EOFException e) {
                return null;
            }
        };
    }
}

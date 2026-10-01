package me.liaoheng.wallpaper.data;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import me.liaoheng.wallpaper.BaseTest;
import me.liaoheng.wallpaper.TestApplication;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.NetUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class BingWallpaperNetworkClientTest extends BaseTest {

    @Rule
    public final MockWebServer server = new MockWebServer();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Inject a Retrofit pointed at the mock server so the production URL handling is exercised
        // without going through NetUtils.init (which builds its own client and base URL).
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        NetUtils netUtils = NetUtils.get();
        setField(netUtils, "mRetrofit", retrofit);
        setField(netUtils, "mBingWallpaperNetworkService", null);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = NetUtils.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private String readFixture(String name) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
            assertNotNull("missing test fixture " + name, in);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, read);
                }
            }
            return sb.toString();
        }
    }

    private String requestUrl() {
        return server.url("/HPImageArchive.aspx?format=js&idx=0&n=1&pid=hp&mtk=en-US").toString();
    }

    private Wallpaper call() throws IOException {
        return BingWallpaperNetworkClient.getBingWallpaperSingleCall(requestUrl(), "en-US",
                "public, no-cache");
    }

    @Test
    public void validResponseIsMapped() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody(readFixture("bing_wallpaper_response.json")));

        Wallpaper wallpaper = call();

        assertEquals("20240116", wallpaper.getDateTime());
        assertEquals("/th?id=OHR.AtacamaTorres_ROW1234567890", wallpaper.getBaseUrl());
        assertNotNull(wallpaper.getCopyrightInfo());
    }

    @Test
    public void emptyImagesThrows() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"images\":[]}"));

        try {
            call();
            fail("expected IOException");
        } catch (IOException e) {
            assertEquals("bing wallpaper is not data", e.getMessage());
        }
    }

    @Test
    public void missingImagesFieldThrows() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"tooltips\":{}}"));

        try {
            call();
            fail("expected IOException");
        } catch (IOException e) {
            assertEquals("bing wallpaper is not data", e.getMessage());
        }
    }

    @Test
    public void serverErrorThrows() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));

        try {
            call();
            fail("expected IOException");
        } catch (IOException e) {
            assertEquals("bing server response failure", e.getMessage());
        }
    }

    @Test
    public void malformedJsonThrows() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"images\": ["));

        try {
            call();
            fail("expected a parse failure");
        } catch (RuntimeException expected) {
            // Retrofit surfaces the Gson parse failure as-is
        }
    }
}

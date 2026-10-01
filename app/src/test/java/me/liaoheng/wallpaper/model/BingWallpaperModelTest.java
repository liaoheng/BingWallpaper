package me.liaoheng.wallpaper.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import me.liaoheng.wallpaper.BaseTest;
import me.liaoheng.wallpaper.TestApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Parses a saved Bing HPImageArchive response so a change in the upstream shape is caught.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class BingWallpaperModelTest extends BaseTest {

    private Gson gson;

    @Before
    public void setUp() {
        super.setUp();
        gson = new Gson();
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

    @Test
    public void parsesImagesAndTooltips() throws Exception {
        BingWallpaper bingWallpaper = gson.fromJson(readFixture("bing_wallpaper_response.json"),
                BingWallpaper.class);

        assertNotNull(bingWallpaper.getImages());
        assertEquals(2, bingWallpaper.getImages().size());
        assertNotNull(bingWallpaper.getTooltips());
        assertTrue(bingWallpaper.getTooltips().getWalls().length() > 0);
        assertTrue(bingWallpaper.getTooltips().getWalle().length() > 0);
    }

    @Test
    public void mapsWpTrueImageToWallpaperUsingWalls() throws Exception {
        BingWallpaper bingWallpaper = gson.fromJson(readFixture("bing_wallpaper_response.json"),
                BingWallpaper.class);
        BingWallpaper.ToolTips toolTips = bingWallpaper.getTooltips();

        Wallpaper wallpaper = bingWallpaper.getImages().get(0).to(toolTips);

        assertEquals("20240116", wallpaper.getDateTime());
        assertEquals("/th?id=OHR.AtacamaTorres_ROW1234567890_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp",
                wallpaper.getUrl());
        assertEquals("/th?id=OHR.AtacamaTorres_ROW1234567890", wallpaper.getBaseUrl());
        assertEquals("Torres del Paine, Patagonia, Chile (© Carlos Arriagada/Getty Images)",
                wallpaper.getTitle());
        assertEquals("https://www.bing.com/search?q=Torres+del+Paine&form=hpcapt", wallpaper.getWebUrl());
        assertEquals("Torres del Paine, Patagonia, Chile (© Carlos Arriagada/Getty Images)",
                wallpaper.getDesc());
        assertEquals(toolTips.getWalls(), wallpaper.getCopyrightInfo());
    }

    @Test
    public void mapsWpFalseImageToWallpaperUsingWalle() throws Exception {
        BingWallpaper bingWallpaper = gson.fromJson(readFixture("bing_wallpaper_response.json"),
                BingWallpaper.class);

        Wallpaper wallpaper = bingWallpaper.getImages().get(1).to(bingWallpaper.getTooltips());

        assertEquals("20240115", wallpaper.getDateTime());
        assertEquals("/th?id=OHR.LakeTahoe_ROW0987654321", wallpaper.getBaseUrl());
        assertEquals(bingWallpaper.getTooltips().getWalle(), wallpaper.getCopyrightInfo());
    }

    @Test
    public void missingTooltipsMapsToEmptyCopyright() throws Exception {
        BingWallpaper bingWallpaper = gson.fromJson(readFixture("bing_wallpaper_response.json"),
                BingWallpaper.class);

        Wallpaper wallpaper = bingWallpaper.getImages().get(0).to(null);

        assertEquals("", wallpaper.getCopyrightInfo());
    }

    @Test
    public void emptyImagesArrayParsesToEmptyList() {
        BingWallpaper bingWallpaper = gson.fromJson("{\"images\":[],\"tooltips\":{}}", BingWallpaper.class);

        assertNotNull(bingWallpaper.getImages());
        assertTrue(bingWallpaper.getImages().isEmpty());
    }

    @Test
    public void missingImagesFieldParsesToNullList() {
        BingWallpaper bingWallpaper = gson.fromJson("{\"tooltips\":{}}", BingWallpaper.class);

        assertNull(bingWallpaper.getImages());
    }

    @Test
    public void malformedJsonThrows() {
        assertThrows(JsonSyntaxException.class,
                () -> gson.fromJson("{\"images\": [", BingWallpaper.class));
    }

    @Test
    public void wallpaperMapRoundTrip() {
        Wallpaper original = new Wallpaper("20240116", "url", "baseUrl", "title", "webUrl", "desc",
                "copyright");
        original.setImageUrl("imageUrl");

        Wallpaper restored = Wallpaper.to(original.getMap());

        assertNotNull(restored);
        assertEquals(original.getDateTime(), restored.getDateTime());
        assertEquals(original.getUrl(), restored.getUrl());
        assertEquals(original.getBaseUrl(), restored.getBaseUrl());
        assertEquals(original.getTitle(), restored.getTitle());
        assertEquals(original.getWebUrl(), restored.getWebUrl());
        assertEquals(original.getDesc(), restored.getDesc());
        assertEquals(original.getImageUrl(), restored.getImageUrl());
        assertEquals(original.getCopyrightInfo(), restored.getCopyrightInfo());
    }
}

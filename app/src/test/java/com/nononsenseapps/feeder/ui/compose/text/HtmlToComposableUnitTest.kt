@file:Suppress("ktlint:standard:max-line-length")

package com.nononsenseapps.feeder.ui.compose.text

import io.mockk.every
import io.mockk.mockk
import org.jsoup.nodes.Element
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlToComposableUnitTest {
    private val element = mockk<Element>()

    @Before
    fun setup() {
        every { element.attr("width") } returns ""
        every { element.attr("height") } returns ""
        every { element.attr("data-img-url") } returns ""
    }

    @Test
    fun findImageSrcWithNoSrc() {
        every { element.attr("srcset") } returns ""
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertFalse(result.hasImage)
    }

    @Test
    fun findImageOnlySrcWithZeroPixels() {
        every { element.attr("srcset") } returns ""
        every { element.attr("abs:src") } returns "http://foo/image.jpg"
        every { element.attr("width") } returns "0"
        every { element.attr("height") } returns "0"

        val result = getImageSource("http://foo", element)

        assertTrue(result.notHasImage)
    }

    @Test
    fun findImageBestZeroPixelSrcSetIsNoImage() {
        every { element.attr("srcset") } returns "header640.png 0w"
        every { element.attr("abs:src") } returns ""
        every { element.attr("width") } returns ""
        every { element.attr("height") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        val best = result.getBestImageForMaxSize(maxSize, 1.0f)
        assertTrue("$best should be NoImageCandidate") {
            best is NoImageCandidate
        }
    }

    @Test
    fun findImageOnlySrc() {
        every { element.attr("srcset") } returns ""
        every { element.attr("abs:src") } returns "http://foo/image.jpg"

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)
        val best = result.getBestImageForMaxSize(1, 1.0f)
        assertEquals("http://foo/image.jpg", best.url)
    }

    @Test
    fun findImageOnlySingleSrcSet() {
        every { element.attr("srcset") } returns "image.jpg"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)
        val best = result.getBestImageForMaxSize(1, 1.0f)
        assertEquals("http://foo/image.jpg", best.url)
    }

    @Test
    fun findImageBestMinSrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        val best = result.getBestImageForMaxSize(maxSize, 1.0f)
        assertEquals("http://foo/header.png", best.url)
    }

    @Test
    fun findImageBest640SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 640
        val best = result.getBestImageForMaxSize(maxSize, 1.0f)
        assertEquals("http://foo/header640.png", best.url)
    }

    @Test
    fun findImageBest960SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 900
        val best = result.getBestImageForMaxSize(maxSize, 8.0f)
        assertEquals("http://foo/header960.png", best.url)
    }

    @Test
    fun findImageBest650SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 650
        val best = result.getBestImageForMaxSize(maxSize, 7.0f)
        assertEquals("http://foo/header640.png", best.url)
    }

    @Test
    fun findImageBest950SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 950
        val best = result.getBestImageForMaxSize(maxSize, 7.0f)
        assertEquals("http://foo/header960.png", best.url)
    }

    @Test
    fun findImageBest1500SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1500
        val best = result.getBestImageForMaxSize(maxSize, 8.0f)
        assertEquals("http://foo/header960.png", best.url)
    }

    @Test
    fun findImageBest3xSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        val best = result.getBestImageForMaxSize(maxSize, 3.0f)
        assertEquals("http://foo/header3.0x.png", best.url)
    }

    @Test
    fun findImageBest1xSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns ""

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        val best = result.getBestImageForMaxSize(maxSize, 1.0f)
        assertEquals("http://foo/header.png", best.url)
    }

    @Test
    fun findImageBestJunkSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2Y"
        every { element.attr("abs:src") } returns "http://foo/header.png"

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        val best = result.getBestImageForMaxSize(maxSize, 1.0f)
        assertEquals("http://foo/header.png", best.url)
    }

    @Test
    fun findImageBestPoliticoSrcSet() {
        every {
            element.attr("srcset")
        } returns "https://www.politico.eu/cdn-cgi/image/width=1024,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg 1024w, https://www.politico.eu/cdn-cgi/image/width=300,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg 300w, https://www.politico.eu/cdn-cgi/image/width=1280,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg 1280w"
        every {
            element.attr("abs:src")
        } returns "https://www.politico.eu/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd-1024x683.jpeg"
        every { element.attr("width") } returns "1024"
        every { element.attr("height") } returns "683"

        val result = getImageSource("https://www.politico.eu/feed/", element)

        assertTrue(result.hasImage)

        val maxSize = 1024
        val best =
            result.getBestImageForMaxSize(
                maxSize,
                8.0f,
            )
        assertEquals(
            "https://www.politico.eu/cdn-cgi/image/width=1024,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg",
            best.url,
        )
    }

    @Test
    fun findImageForTheVerge() {
        /*
        <img alt="A pen pointing to a piece of LK-99 standing on its side above a magnet." sizes="(max-width: 768px) calc(100vw - 100px), (max-width: 1180px) 700px, 600px" srcSet="https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/16x11/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 16w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/32x21/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 32w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/48x32/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 48w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/64x43/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 64w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/96x64/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 96w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/128x85/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 128w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/256x171/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 256w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/376x251/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 376w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/384x256/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 384w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/415x277/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 415w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/480x320/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 480w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/540x360/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 540w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/640x427/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 640w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/750x500/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 750w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/828x552/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 828w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1080x720/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1080w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1200x800/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1200w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1440x960/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1440w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1920x1280/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1920w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2048x1365/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2048w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2400w" src="https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png" decoding="async" data-nimg="responsive" style="position:absolute;top:0;left:0;bottom:0;right:0;box-sizing:border-box;padding:0;border:none;margin:auto;display:block;width:0;height:0;min-width:100%;max-width:100%;min-height:100%;max-height:100%;object-fit:cover"/>
         */

        every {
            element.attr("srcset")
        } returns "https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/16x11/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 16w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/32x21/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 32w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/48x32/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 48w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/64x43/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 64w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/96x64/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 96w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/128x85/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 128w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/256x171/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 256w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/376x251/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 376w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/384x256/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 384w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/415x277/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 415w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/480x320/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 480w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/540x360/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 540w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/640x427/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 640w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/750x500/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 750w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/828x552/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 828w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1080x720/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1080w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1200x800/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1200w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1440x960/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1440w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1920x1280/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1920w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2048x1365/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2048w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2400w"
        every {
            element.attr("abs:src")
        } returns "https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png"
        every { element.attr("width") } returns ""
        every { element.attr("height") } returns ""

        val result = getImageSource("https://www.politico.eu/feed/", element)

        assertTrue(result.hasImage)

        val maxSize = 1024
        val best =
            result.getBestImageForMaxSize(
                maxSize,
                8.0f,
            )
        assertEquals(
            "https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1080x720/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png",
            best.url,
        )
    }

    @Test
    fun findImageForXDAWithDataImgUrl() {
        every {
            element.attr("data-img-url")
        } returns "https://static1.xdaimages.com/wordpress/wp-content/uploads/2023/12/onedrive-app-for-microsoft-teams.png"
        every {
            element.attr("srcset")
        } returns ""
        every {
            element.attr("abs:src")
        } returns ""
        every { element.attr("width") } returns ""
        every { element.attr("height") } returns ""

        val result = getImageSource("https://www.xda-developers.com", element)

        assertTrue(result.hasImage)

        val maxSize = 1024
        val best =
            result.getBestImageForMaxSize(
                maxSize,
                8.0f,
            )
        assertEquals(
            "https://static1.xdaimages.com/wordpress/wp-content/uploads/2023/12/onedrive-app-for-microsoft-teams.png",
            best.url,
        )
    }

    @Test
    fun noSourcesMeansEmptyResult() {
        every { element.attr("srcset") } returns ""
        every { element.attr("abs:src") } returns ""
        every { element.attr("width") } returns ""
        every { element.attr("height") } returns ""

        val result = getImageSource("https://www.politico.eu/feed/", element)

        assertFalse(result.hasImage)

        val maxSize = 1024
        val best =
            result.getBestImageForMaxSize(
                maxSize,
                8.0f,
            )
        assertEquals(
            "",
            best.url,
        )
    }
}

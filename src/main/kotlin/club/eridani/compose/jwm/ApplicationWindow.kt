package club.eridani.compose.jwm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.awt.LocalLayerContainer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import club.eridani.compose.jwm.skia.*
import io.github.humbleui.jwm.*
import org.jetbrains.skia.Color
import java.awt.Container
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.util.function.Consumer
import java.awt.event.KeyEvent as AwtKeyEvent

class ApplicationWindow(
    val onClose: ApplicationWindow.() -> Unit = { this.window.setVisible(false) }, val content: @Composable () -> Unit
) : Consumer<Event> {
    val composeScene = ComposeScene()
    val windowInfo = JWMWindowInfo()

    val container = object : Container() {
        override fun getX(): Int {
            return window.contentRectAbsolute.left
        }

        override fun getY(): Int {
            return window.contentRectAbsolute.top
        }

        override fun getGraphicsConfiguration(): GraphicsConfiguration {
            return GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
        }

    }

    val window = App.makeWindow()


    private var lastEventMouseMove = EventMouseMove(0, 0, 0, 0)
    private var lastEventKey: EventKey? = null

    var windowState: WindowState? = null

    init {
        window.eventListener = this
        updateConstrain()
        composeScene.setContent { BasicSetup(this, content) }
        window.layer = createPlatformLayer()
        window.setVisible(true)
        window.setTextInputEnabled(false)
    }


    override fun accept(e: Event) {
        when (e) {
            is EventWindowFocusIn -> windowInfo.isWindowFocused = true
            is EventWindowFocusOut -> windowInfo.isWindowFocused = false

            is EventFrame -> window.requestFrame()
            is EventFrameSkia -> composeScene.render(e.surface.canvas.clear(Color.TRANSPARENT), System.nanoTime())

            is EventWindowResize -> updateConstrain()
            is EventWindowCloseRequest -> onClose()

            is EventWindowMove -> windowState?.position = WindowPosition(e.windowLeft.dp, e.windowTop.dp)

            is EventWindowRestore -> {
                windowState?.apply {
                    isMinimized = false
                    placement = WindowPlacement.Floating
                }
            }

            is EventWindowMaximize -> {
                windowState?.apply {
                    isMinimized = false
                    placement = WindowPlacement.Maximized
                }
            }

            is EventWindowMinimize -> {
                windowState?.apply {
                    isMinimized = true
                    placement = WindowPlacement.Floating
                }
            }

            is EventMouseButton -> composeScene.sendPointerEvent(
                eventType = if (e.isPressed) PointerEventType.Press else PointerEventType.Release,
                position = currentMousePos(),
                buttons = if (e.isPressed) PointerButtons(e.button._mask) else null
            )
            is EventMouseMove -> {
                lastEventMouseMove = e
                composeScene.sendPointerEvent(
                    eventType = PointerEventType.Move,
                    position = Offset(e.x.toFloat(), e.y.toFloat()),
                )
            }
            is EventMouseScroll -> composeScene.sendPointerEvent(
                eventType = PointerEventType.Scroll, position = currentMousePos(), scrollDelta = Offset(e.deltaX, e.deltaY)
            )
            is EventTextInput -> {
                e.text.forEach {
                    composeScene.sendKeyEvent(
                        KeyEvent(
                            JWMAWTKeyEvent(
                                container,
                                AwtKeyEvent.KEY_TYPED,
                                System.nanoTime(),
                                if (lastEventKey != null) lastEventKey!!._modifiers else 0,
                                0,
                                it,
                                AwtKeyEvent.KEY_LOCATION_UNKNOWN
                            )
                        )
                    )
                }
            }

            is EventKey -> {
                lastEventKey = e
                composeScene.sendKeyEvent(
                    KeyEvent(
                        JWMAWTKeyEvent(
                            container,
                            if (e.isPressed) AwtKeyEvent.KEY_PRESSED else AwtKeyEvent.KEY_RELEASED,
                            System.nanoTime(),
                            e._modifiers,
                            jwmKeyToAWTKey(e.key),
                            location = AwtKeyEvent.KEY_LOCATION_STANDARD
                        )
                    )
                )
            }

        }
    }

    private fun currentMousePos() = Offset(lastEventMouseMove.x.toFloat(), lastEventMouseMove.y.toFloat())


    private fun updateConstrain() {
        windowState?.size = DpSize(window.windowRect.width.dp, window.windowRect.height.dp)
        composeScene.constraints = Constraints(maxWidth = window.contentRect.width, maxHeight = window.contentRect.height)
    }


}

private fun createPlatformLayer(): Layer {
    return when (Platform.CURRENT ?: error("Can't determine current platform")) {
        Platform.WINDOWS -> runCatching { runCatching { LayerD3D12Skia() }.getOrElse { LayerGLSkia() } }.getOrElse { LayerRasterSkia() }
        Platform.MACOS -> runCatching { runCatching { LayerMetalSkia() }.getOrElse { LayerGLSkia() } }.getOrElse { LayerRasterSkia() }
        Platform.X11 -> runCatching { LayerD3D12Skia() }.getOrElse { LayerGLSkia() }
    }
}


@Composable private fun BasicSetup(window: ApplicationWindow, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalLayerContainer provides window.container,
        LocalWindowInfo provides window.windowInfo,
        LocalApplicationWindow provides window,
        content = content
    )
}
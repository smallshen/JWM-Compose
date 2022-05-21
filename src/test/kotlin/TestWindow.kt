import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import club.eridani.compose.jwm.ApplicationWindow
import club.eridani.compose.jwm.ManageWindow
import club.eridani.compose.jwm.component.textInputFocusable
import io.github.humbleui.jwm.App

fun main() {


    App.start {
        ApplicationWindow {
            var title by remember { mutableStateOf("Test") }
            ManageWindow(
                title = title
            )

            MaterialTheme {
                Box(Modifier.fillMaxSize().background(Color.White)) {
                    Column(Modifier.fillMaxSize()) {
                        Text("Wow, window in JWM")
                        Text("I wish compose-jb team remove only a little bit of awt code")
                        Text("Then jetpack compose with GraalVM will fully work without glue code and stupid tricks")
                        Text("HURRAY")

                        Spacer(Modifier.height(30.dp))
                        SelectionContainer {
                            Column {
                                Text("Related classes:", fontStyle = MaterialTheme.typography.h5.fontStyle)
                                Text("androidx.compose.ui.ComposeScene internal constructor made custom PlatformComponent possible")
                                Text("androidx.compose.ui.platform.PlatformComponent")
                            }
                        }


                        Spacer(Modifier.height(30.dp))
                        Text("Remove more awt related code plzzzzzzzz")
                        Button(onClick = { title += "1" }) {
                            Text("Magic")
                        }

                        TextField(
                            value = title,
                            onValueChange = {
                                title = it
                            },
                            modifier = Modifier.textInputFocusable()
                        )
                    }
                }
            }
        }
    }
}
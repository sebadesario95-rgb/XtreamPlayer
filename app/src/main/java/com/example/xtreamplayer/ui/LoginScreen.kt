package com.example.xtreamplayer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtreamplayer.data.Credentials
import com.example.xtreamplayer.viewmodel.AppViewModel

private val LoginBlue = Color(0xFF1677FF)
private val LoginBlueDark = Color(0xFF0B3D91)
private val LoginBackground = Color(0xFF05070B)
private val LoginCard = Color(0xE6121720)
private val LoginField = Color(0xFF0D121A)
private val LoginMuted = Color(0xFF9AA6B2)

@Composable
fun LoginScreen(vm: AppViewModel) {
    var server by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val canLogin = !vm.loading &&
            server.isNotBlank() &&
            username.isNotBlank() &&
            password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF020305),
                        LoginBackground,
                        Color(0xFF071326),
                        Color(0xFF02050A)
                    )
                )
            )
    ) {

        Box(
            modifier = Modifier
                .size(430.dp)
                .offset(x = (-170).dp, y = (-150).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x551677FF), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(520.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 210.dp, y = 230.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x441677FF), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            val wideLayout = maxWidth >= 760.dp

            if (wideLayout) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(56.dp)
                ) {
                    BrandPanel(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 24.dp)
                    )

                    LoginCard(
                        server = server,
                        username = username,
                        password = password,
                        passwordVisible = passwordVisible,
                        loading = vm.loading,
                        canLogin = canLogin,
                        error = vm.error,
                        onServerChange = { server = it },
                        onUsernameChange = { username = it },
                        onPasswordChange = { password = it },
                        onPasswordVisibilityChange = {
                            passwordVisible = !passwordVisible
                        },
                        onLogin = {
                            vm.login(
                                Credentials(
                                    server.trim(),
                                    username.trim(),
                                    password
                                )
                            )
                        },
                        modifier = Modifier.widthIn(max = 440.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 520.dp)
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center
                ) {
                    BrandPanel(compact = true)

                    Spacer(Modifier.height(28.dp))

                    LoginCard(
                        server = server,
                        username = username,
                        password = password,
                        passwordVisible = passwordVisible,
                        loading = vm.loading,
                        canLogin = canLogin,
                        error = vm.error,
                        onServerChange = { server = it },
                        onUsernameChange = { username = it },
                        onPasswordChange = { password = it },
                        onPasswordVisibilityChange = {
                            passwordVisible = !passwordVisible
                        },
                        onLogin = {
                            vm.login(
                                Credentials(
                                    server.trim(),
                                    username.trim(),
                                    password
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandPanel(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(modifier = modifier) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (compact) 42.dp else 52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(LoginBlue, Color(0xFF58A6FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    color = Color.White,
                    fontSize = if (compact) 20.sp else 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = "FUTURE SMART",
                color = Color.White,
                fontSize = if (compact) 24.sp else 31.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        if (!compact) {
            Spacer(Modifier.height(28.dp))

            Text(
                text = "IL PLAYER\nCHE FA PER TE",
                color = Color.White,
                fontSize = 34.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Live TV, Film e Serie in un'esperienza semplice, moderna e pensata per il grande schermo.",
                color = LoginMuted,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.widthIn(max = 540.dp)
            )

            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureChip("LIVE TV")
                FeatureChip("FILM")
                FeatureChip("SERIE")
            }
        } else {
            Spacer(Modifier.height(10.dp))

            Text(
                text = "IL PLAYER CHE FA PER TE",
                color = LoginMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun FeatureChip(text: String) {
    Surface(
        color = Color(0x66101620),
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0x332B8CFF)
        )
    ) {
        Text(
            text = text,
            color = Color(0xFFD8E9FF),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 9.dp
            )
        )
    }
}

@Composable
private fun LoginCard(
    server: String,
    username: String,
    password: String,
    passwordVisible: Boolean,
    loading: Boolean,
    canLogin: Boolean,
    error: String?,
    onServerChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LoginCard,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 24.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0x223B82D0)
        )
    ) {
        Column(
            modifier = Modifier.padding(30.dp)
        ) {
            Text(
                text = "Accedi al tuo account",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Inserisci le credenziali fornite dal tuo servizio.",
                color = LoginMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(26.dp))

            LoginTextField(
                value = server,
                onValueChange = onServerChange,
                label = "Server URL",
                placeholder = "http://example.com:8080",
                keyboardType = KeyboardType.Uri
            )

            Spacer(Modifier.height(14.dp))

            LoginTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = "Username",
                placeholder = "Il tuo username"
            )

            Spacer(Modifier.height(14.dp))

            LoginTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "La tua password",
                visualTransformation =
                    if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                trailingContent = {
                    LoginSmallTvButton(
                        text = if (passwordVisible) "NASCONDI" else "MOSTRA",
                        onClick = onPasswordVisibilityChange
                    )
                }
            )

            error?.let {
                Spacer(Modifier.height(14.dp))

                Surface(
                    color = MaterialTheme.colorScheme.errorContainer
                        .copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            LoginTvButton(
                enabled = canLogin,
                loading = loading,
                onClick = onLogin
            )

            Spacer(Modifier.height(18.dp))

            HorizontalDivider(
                color = Color(0x221D8BFF)
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Usa esclusivamente servizi e contenuti per i quali disponi delle necessarie autorizzazioni.",
                color = LoginMuted.copy(alpha = 0.75f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun LoginTvButton(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused && enabled) 1.025f else 1f,
        label = "loginButtonFocusScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(12.dp),
        border = if (isFocused && enabled) {
            BorderStroke(3.dp, Color(0xFF58A6FF))
        } else {
            null
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused && enabled) Color(0xFF2389FF) else LoginBlue,
            contentColor = Color.White,
            disabledContainerColor = LoginBlueDark.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.55f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = "ACCEDI",
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun LoginSmallTvButton(
    text: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1f,
        label = "loginSmallButtonFocusScale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() },
        color = if (isFocused) Color(0xFF153454) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (isFocused) 2.dp else 0.dp,
            color = if (isFocused) Color(0xFF58A6FF) else Color.Transparent
        )
    ) {
        Text(
            text = text,
            color = if (isFocused) Color(0xFF8CC4FF) else LoginBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation =
        VisualTransformation.None,
    trailingContent: (@Composable (() -> Unit))? = null
) {
    var containerFocused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    val textFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val scale by animateFloatAsState(
        targetValue = if (containerFocused || editing) 1.02f else 1f,
        label = "loginFieldFocusScale"
    )

    LaunchedEffect(editing) {
        if (editing) {
            textFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .onFocusChanged {
                containerFocused = it.isFocused
                if (!it.hasFocus) {
                    editing = false
                }
            }
            .onKeyEvent { event ->
                if (
                    !editing &&
                    event.type == KeyEventType.KeyUp &&
                    (
                        event.key == Key.Enter ||
                        event.key == Key.NumPadEnter ||
                        event.key == Key.DirectionCenter
                    )
                ) {
                    editing = true
                    true
                } else {
                    false
                }
            }
            .focusable(),
        color = LoginField,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (containerFocused || editing) 3.dp else 1.dp,
            color = if (containerFocused || editing) {
                Color(0xFF58A6FF)
            } else {
                Color(0xFF273242)
            }
        )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(textFocusRequester)
                .onFocusChanged {
                    if (!it.isFocused && editing) {
                        editing = false
                    }
                },
            enabled = editing,
            singleLine = true,
            label = {
                Text(label)
            },
            placeholder = {
                Text(
                    placeholder,
                    color = LoginMuted.copy(alpha = 0.55f)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            visualTransformation = visualTransformation,
            trailingIcon = trailingContent,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White,
                focusedContainerColor = LoginField,
                unfocusedContainerColor = LoginField,
                disabledContainerColor = LoginField,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedLabelColor = LoginBlue,
                unfocusedLabelColor = LoginMuted,
                disabledLabelColor = if (containerFocused) LoginBlue else LoginMuted,
                cursorColor = LoginBlue,
                disabledPlaceholderColor = LoginMuted.copy(alpha = 0.55f)
            )
        )
    }
}


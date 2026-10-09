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

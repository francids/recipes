import "package:flutter/material.dart";

class ModernFormField extends StatelessWidget {
  final String hintText;
  final String initialValue;
  final TextEditingController? controller;
  final TextInputType keyboardType;
  final bool obscureText;
  final String? Function(String?)? validator;
  final void Function(String)? onChanged;
  final TextInputAction? textInputAction;
  final bool? expands;
  final String topLabel;
  final ModernFormFieldDecoration decoration;
  final bool autofocus;

  ModernFormField({
    Key? key,
    required this.hintText,
    this.initialValue = "",
    this.controller,
    this.keyboardType = TextInputType.text,
    this.obscureText = false,
    this.validator,
    this.onChanged,
    this.topLabel = "",
    this.textInputAction,
    this.expands,
    this.autofocus = false,
    ModernFormFieldDecoration? decoration,
  })  : decoration =
            decoration ?? ModernFormFieldDecoration(topLabel: topLabel),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (topLabel.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(bottom: 6.0, left: 2.0),
              child: Text(
                topLabel,
                style: theme.textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.colorScheme.onSurface,
                ),
              ),
            ),
          TextFormField(
            controller: controller,
            focusNode: decoration.focusNode,
            autofocus: autofocus,
            keyboardType: keyboardType,
            obscureText: obscureText,
            maxLines: decoration.wrapText ? null : 1,
            minLines: decoration.minLines,
            expands: expands ?? false,
            textInputAction: textInputAction,
            initialValue: controller == null ? initialValue : null,
            onChanged: onChanged,
            validator: validator,
            style: theme.textTheme.bodyMedium!.copyWith(
              color: theme.colorScheme.onSurface,
            ),
            decoration: decoration.buildInputDecoration(context, hintText),
          ),
        ],
      ),
    );
  }
}

class ModernFormFieldDecoration {
  final Widget? prefixIcon;
  final Widget? suffixIcon;
  final int? maxLines;
  final int? minLines;
  final FocusNode? focusNode;
  final bool autofocus;
  final InputBorder? customEnabledBorder;
  final InputBorder? customFocusedBorder;
  final InputBorder? customErrorBorder;
  final bool wrapText;
  final Color? fillColor;
  final String? topLabel;

  const ModernFormFieldDecoration({
    this.prefixIcon,
    this.suffixIcon,
    this.maxLines,
    this.minLines = 1,
    this.focusNode,
    this.autofocus = false,
    this.customEnabledBorder,
    this.customFocusedBorder,
    this.customErrorBorder,
    this.wrapText = true,
    this.fillColor,
    this.topLabel,
  });

  InputDecoration buildInputDecoration(BuildContext context, String hintText) {
    final theme = Theme.of(context);

    return InputDecoration(
      hintText: hintText,
      hintStyle: theme.textTheme.bodyMedium?.copyWith(
        color: theme.colorScheme.onSurface.withAlpha(48),
      ),
      prefixIcon: prefixIcon,
      suffixIcon: suffixIcon,
      filled: true,
      fillColor: fillColor ?? theme.colorScheme.surface,
      isCollapsed: !wrapText,
      contentPadding: const EdgeInsets.symmetric(
        horizontal: 16.0,
        vertical: 16.0,
      ),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12.0),
        borderSide: BorderSide.none,
      ),
      enabledBorder: customEnabledBorder ??
          OutlineInputBorder(
            borderRadius: BorderRadius.circular(12.0),
            borderSide: BorderSide(
              color: theme.colorScheme.outline.withAlpha(128),
              width: 1.0,
            ),
          ),
      focusedBorder: customFocusedBorder ??
          OutlineInputBorder(
            borderRadius: BorderRadius.circular(12.0),
            borderSide: BorderSide(
              color: theme.colorScheme.primary,
              width: 2.0,
            ),
          ),
      errorBorder: customErrorBorder ??
          OutlineInputBorder(
            borderRadius: BorderRadius.circular(12.0),
            borderSide: BorderSide(
              color: theme.colorScheme.error,
              width: 1.0,
            ),
          ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12.0),
        borderSide: BorderSide(
          color: theme.colorScheme.error,
          width: 2.0,
        ),
      ),
      errorStyle: TextStyle(
        color: theme.colorScheme.error,
        fontSize: 12.0,
      ),
    );
  }
}

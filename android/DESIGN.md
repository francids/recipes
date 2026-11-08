# Diseño: port nativo del diseño original

La interfaz se ha reconstruido tomando como referencia el diseño original de la app Flutter
(commit `b9b3dbf`) y reescribiendo su implementación en Kotlin/Jetpack Compose. No se usa el
lenguaje visual de Material 3 Expressive ni sus roles tonales como base del diseño.

## Referencia original

- Tema: `lib/recipes_app_theme.dart`
- Pantalla principal: `lib/views/screens/inicial_screen.dart`
- Tarjetas: `lib/views/widgets/recipe_card.dart`
- Detalle: `lib/views/widgets/common_recipe_view.dart`
- Formularios: `lib/views/widgets/form_field.dart`
- Perfil y ajustes: `lib/views/screens/profile_page.dart`, `settings_screen.dart`
- Diálogos: `lib/views/widgets/ui_helpers.dart`

## Fundamentos visuales

### Color

| Rol | Valor |
| --- | --- |
| Marca / acento | `#FF5722` |
| Fondo claro | `#FFFFFF` |
| Superficie oscura | `#1E1E1E` |
| Fondo oscuro | `#121212` |
| Tarjeta clara | `#FAFAFA` |
| Tarjeta oscura | `#2A2A2A` |
| Borde claro | negro al 26 % |
| Borde oscuro | blanco al 30 % |
| Chip claro | `#F5F5F5` |
| Chip oscuro | `#2A2A2A` |

No se utilizan `primaryContainer`, `secondaryContainer`, `tertiaryContainer` ni superficies
tonales de Material como sistema principal. Los colores se declaran de forma explícita en
`AppPalette`.

### Tipografía

Montserrat, con la escala original:

- `28 sp` semibold: título de receta.
- `24 sp` bold: título de pantalla.
- `20 sp` medium: títulos de sección, tarjetas y AppBar secundaria.
- `16 sp` medium: cuerpo principal.
- `14 sp` regular: cuerpo secundario.
- `12 sp` regular: texto auxiliar.
- `14 sp` medium: etiquetas de chips y botones.

### Forma y profundidad

- Tarjetas, chips, botones y FAB: radio `10 dp`.
- Campos de texto: radio `12 dp`.
- Diálogos: radio `16 dp`.
- Tarjetas con borde de `0.5 dp` y sombra suave.
- Sin elevaciones tonales ni contenedores M3.

## Componentes

- **AppBar personalizada** de 64 dp con borde inferior y fondo transparente/blanco.
- **Navegación inferior flotante** en píldora del 60 % del ancho, con indicador naranja.
- **FAB** naranja de radio 10 dp con animación de escala al pulsar.
- **Tarjetas de receta** con borde, sombra, imagen de 80×80 en lista y portada con degradado en rejilla.
- **Chips** claros con borde y selección naranja translúcida.
- **Campos de texto** rellenos sobre la superficie, borde de 0.5 dp y foco naranja de 2 dp.
- **Diálogos** con icono, título, mensaje y botones Cancelar/Confirmar.
- **Bottom sheets** sin botón de cancelar redundante; se cierran por gesto, fondo o atrás.
- **Menú** popup blanco/oscuro con borde y filas icono + texto, como el `showMenu` original.

## Layout

- Márgenes laterales de 16 dp.
- Separación de 12 dp entre tarjetas y de 8 dp entre chips.
- Contenido inferior con 140 dp para no quedar bajo la píldora/FAB.
- Barra de estado y navegación respetadas con `WindowInsets`.
- Lista de recetas en una columna o rejilla de dos columnas, como en Flutter.

## Accesibilidad

- Objetivos táctiles de al menos 48 dp.
- `contentDescription` en acciones.
- Filas de idioma con semántica `Role.RadioButton`.
- Contraste de texto comprobado sobre blanco, `#FAFAFA`, `#1E1E1E` y `#121212`.
- El texto no depende de tamaños fijos de contenedores.

## Animaciones y comportamiento

- Se reutilizan los archivos Lottie originales (`profile`, `alert`, `delete`, `exit`, `back`,
  `save_file`, `load_file`, `sync` y `loading`) en perfil y diálogos.
- El reordenado de ingredientes y pasos se hace manteniendo pulsada la manija de arrastre.
- Las transiciones de página se aproximan con fundidos cortos.
- La lógica, persistencia, API, enlaces profundos y funcionalidad se mantienen.

## Validación visual

```sh
cd android
./gradlew :app:testDebugUnitTest --tests '*ScreenshotTest*'
```

Las capturas se guardan en `android/app/build/reports/screenshots/`:

```text
home.png
home_with_recipes.png
home_grid.png
home_menu.png
home_dark.png
profile_signed_out.png
add_recipe.png
recipe_detail.png
settings.png
```

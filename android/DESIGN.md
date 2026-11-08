# Diseño: Material 3 Expressive

La interfaz de Recipes se ha rediseñado siguiendo **Material 3 Expressive**. La app usa
`androidx.compose.material3:material3:1.5.0-rc01` con Compose BOM `2026.09.00`,
`MotionScheme.expressive()` y los componentes expresivos experimentales de Material 3.

## Principios aplicados

Material 3 Expressive utiliza color, tamaño, forma, contención y movimiento para
dirigir la atención y agrupar información. En Recipes se aplican así:

| Principio | Decisión de diseño |
| --- | --- |
| **Jerarquía** | Títulos con estilos `*Emphasized`; cuerpo con estilos M3 de lectura. La acción principal de cada pantalla es única. |
| **Color** | Paleta completa generada desde el naranja de marca (`#FF5722`) con el algoritmo oficial `SchemeFidelity`. Los roles secundarios y terciarios se derivan del mismo color fuente. Se usan pares `on*` sobre `*Container` para garantizar contraste. |
| **Contención** | Tarjetas tonales (`surfaceContainerLow`) con radios grandes, en lugar de bordes y sombras fuertes. |
| **Forma** | Escala expresiva: 8/12/16/24/32 dp. Las tarjetas principales usan 24–32 dp; los controles usan la forma dinámica de los componentes M3. |
| **Movimiento** | `MotionScheme.expressive()` global; transiciones de pasos con especificaciones de efectos del tema; morphing nativo de botones, toolbar y loading indicator. |
| **Tipografía** | Montserrat con escala M3 completa y variantes enfatizadas para títulos, secciones y acciones. |
| **Adaptabilidad** | Rejilla adaptativa (`GridCells.Adaptive(160.dp)`), contenido con scroll y navegación compacta. |

## Componentes expresivos utilizados

- **LargeFlexibleTopAppBar**: cabecera principal con título grande que colapsa al hacer scroll.
- **ShortNavigationBar** con tres destinos: Recetas, Favoritos y Perfil.
- **MediumExtendedFloatingActionButton** para la acción principal “Añadir receta”.
- **ButtonGroup** con botones toggle para ordenar y cambiar entre lista/rejilla. Incluye
  morphing de forma y overflow automático.
- **HorizontalFloatingToolbar** en el detalle de receta para favorito, compartir y eliminar.
- **LinearWavyProgressIndicator** para el progreso del asistente de alta de receta.
- **ContainedLoadingIndicator** para estados de carga y diálogos de progreso.
- **DropdownMenuPopup**, **DropdownMenuGroup** y **DropdownMenuItem** con shapes de grupo para el menú expresivo.
- **ModalBottomSheet** para idioma y compartición, con filas seleccionables y QR; se cierran por gesto, botón atrás o toque fuera, sin acciones de cancelar redundantes.
- **MotionScheme.expressive()** aplicado a todos los componentes M3.

## Color y contraste

### Origen exacto de la paleta

- Color fuente de marca: `#FF5722`.
- Librería: `@material/material-color-utilities`.
- Esquema: `SchemeFidelity` (el modo de fidelidad de marca de Material Theme Builder).
- Nivel de contraste: `0.0`.
- Se generaron los roles claros y oscuros desde ese único color fuente, sin reemplazar
  manualmente secondary/tertiary por colores ajenos a la paleta.

```text
source      #FF5722
algorithm   SchemeFidelity
contrast    0.0
```

La paleta se revisó con los ratios de contraste de los pares principales:

| Par | Ratio |
| --- | --- |
| `onPrimaryContainer` / `primaryContainer` (claro) | 4.54:1 |
| `onSecondaryContainer` / `secondaryContainer` (claro) | 4.57:1 |
| `onTertiaryContainer` / `tertiaryContainer` (claro) | 4.56:1 |
| `onPrimaryContainer` / `primaryContainer` (oscuro) | 4.54:1 |
| `onSecondaryContainer` / `secondaryContainer` (oscuro) | 4.57:1 |
| `onTertiaryContainer` / `tertiaryContainer` (oscuro) | 4.55:1 |
| `onSurfaceVariant` / `surface` (claro) | 8.94:1 |
| `onSurface` / `surface` (oscuro) | 14.32:1 |

Todos los pares superan el mínimo AA de 4.5:1 para texto normal.

## Tipografía

Se usa la escala M3 completa con Montserrat:

- `headlineLarge` / `headlineLargeEmphasized`: títulos principales.
- `headlineSmall`: títulos de pasos y hojas.
- `titleLarge` / `titleLargeEmphasized`: secciones y cabeceras de tarjetas.
- `titleMedium` / `titleMediumEmphasized`: títulos de receta y filas.
- `bodyLarge`: contenido de recetas y descripciones.
- `bodyMedium` / `bodySmall`: texto secundario.
- `labelLarge` / `labelMedium`: botones, chips y metadatos.

Las variantes enfatizadas usan mayor peso y tracking negativo para crear momentos
editoriales sin romper la legibilidad ni la escala.

## Layout y accesibilidad

- Márgenes de pantalla de 16–20 dp; separación vertical de 12–24 dp; padding de
  contenido de 16 dp.
- Objetivos táctiles de al menos 48 dp en botones, icon buttons, filas y navegación.
- `Scaffold`, `navigationBarsPadding()` e `imePadding()` respetan barras del sistema,
  gestos y teclado.
- El snackbar se muestra por encima de la navegación inferior.
- Contenido desplazable con padding inferior para que FAB/toolbar no tapen la última fila.
- Todos los iconos interactivos tienen `contentDescription`; los decorativos usan `null`.
- Las filas de idioma usan semántica `Role.RadioButton` y estado seleccionado.
- Se evitan combinaciones de color de bajo contraste y textos menores de 11 sp.
- La rejilla de recetas usa columnas adaptativas para teléfonos, plegables y tablets.

## Decisiones deliberadas

- No se usa color dinámico por defecto: se prioriza la identidad de marca y un contraste
  predecible. La estructura del tema permite añadirlo más adelante.
- No se usa motion decorativo continuo. El movimiento se reserva para cambios de estado,
  navegación, carga y morphing de controles.
- Las tarjetas no acumulan borde + sombra + elevación tonal al mismo tiempo.
- La acción destructiva (eliminar cuenta/receta) usa `errorContainer` y confirmación explícita.
- Las pantallas mantienen una sola acción primaria visible; el resto se agrupa en toolbar,
  menú o bottom sheet.

## Validación visual

Las pruebas de Robolectric renderizan las pantallas principales con gráficos nativos y guardan
capturas para revisar jerarquía, espaciado, estados y contraste:

```sh
cd android
./gradlew :app:testDebugUnitTest --tests '*ScreenshotTest*'
```

Las capturas quedan en:

```text
android/app/build/reports/screenshots/
├── home.png
├── home_with_recipes.png
├── home_menu.png
├── home_dark.png
├── add_recipe.png
├── recipe_detail.png
└── settings.png
```

Además, `HomeScreenComposeTest` comprueba que la navegación, el menú y la acción principal
están presentes en el árbol de accesibilidad.

## Referencias

- Material Design 3, *Start building with Material 3 Expressive*: <https://m3.material.io/blog/building-with-m3-expressive>
- Material Design 3, componentes expresivos: <https://m3.material.io/components>
- Android Developers, *Material Design 3 in Compose*: <https://developer.android.com/develop/ui/compose/designsystems/material3>
- Android Developers, *Compose Material 3 release notes*: <https://developer.android.com/jetpack/androidx/releases/compose-material3>
- Google Design, *Expressive Design: Google's UX Research*: <https://design.google/library/expressive-material-design-google-research>

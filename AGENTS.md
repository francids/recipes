# Guía de Agentes IA

Este documento proporciona pautas para los agentes de IA que trabajan en esta aplicación de gestión de recetas.

## Estructura del repositorio

| Proyecto           | Ubicación             | Tecnología           |
|--------------------|-----------------------|----------------------|
| Aplicación móvil   | `lib/`                | Flutter + Riverpod   |
| Sitio web          | `website/`            | Astro + Tailwind CSS |
| Función de IA      | `functions/ai/`       | Python (Gemini API)  |
| Función de cuentas | `functions/accounts/` | Python               |

Los servicios backend utilizan Appwrite (Auth, Database, Storage, Functions).

## Comandos

### Aplicación móvil (Flutter)

```bash
# Instala las dependencias
flutter pub get

# Genera los archivos necesarios con build_runner (requerido después de cambios en modelos)
dart run build_runner build --delete-conflicting-outputs

# Corre la aplicación
flutter run

# Analiza el código en busca de errores y advertencias
flutter analyze

# Formatea el código
dart format .
```

### Sitio web (Astro) (`website/`)

```bash
# Instala las dependencias
npm install

# Ejecuta el servidor de desarrollo (localhost:4321)
npm run dev

# Construye para producción
npm run build

# Vista previa de la versión de producción
npm run preview
```

### Python Functions

```bash
# Instala las dependencias de la función (desde functions/ai/ o functions/accounts/)
uv sync

# Prueba localmente con Appwrite CLI (revisar el archivo `appwrite.config.json` para obtener el `function-id`)
appwrite run function --function-id <function-id> --port 3000
```

---

## Guía para el estilo de código

### Dart/Flutter

#### Organización de importaciones

Usa **dobles comillas** para las importaciones.

Ordena las importaciones de la siguiente manera:

1. Librerías principales de Dart (`dart:async`, `dart:io`)
2. Paquetes de Flutter (`package:flutter/material.dart`)
3. Paquetes externos (`package:flutter_riverpod/...`)
4. Paquetes internos (`package:the_recipes/...`)

```dart
import "dart:async";
import "dart:io";

import "package:flutter/material.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
```

#### Convenciones de nombres

- Archivos: `snake_case.dart`
- Clases: `PascalCase` (e.g., `RecipeController`, `AuthState`)
- Variables/funciones: `camelCase` (e.g., `getRecipes`, `isLoading`)
- Atributos privados: `_camelCase` (e.g., `_authState`)
- Riverpod providers: `camelCaseProvider` (e.g., `recipeControllerProvider`)

#### Uso de tipos

- Usa tipos explícitos para miembros de clases y parámetros de funciones
- Usa `final` para variables inmutables
- Usa `required` para parámetros nombrados obligatorios
- Usa tipos nulos con `?` cuando sea apropiado
- Usa constructores `const` cuando sea posible

```dart
class RecipeState {
  final List<Recipe> recipes;
  final bool isLoading;

  RecipeState({required this.recipes, this.isLoading = false});

  RecipeState copyWith({List<Recipe>? recipes, bool? isLoading}) {
    return RecipeState(
      recipes: recipes ?? this.recipes,
      isLoading: isLoading ?? this.isLoading,
    );
  }
}
```

#### Gestión de estado (Riverpod 3.0)

Utilice el patrón «Notificador» con clases de estado inmutables

```dart
class RecipeController extends Notifier<RecipeState> {
  @override
  RecipeState build() => RecipeState(recipes: []);

  Future<void> refreshRecipes() async { ... }
}

final recipeControllerProvider =
    NotifierProvider<RecipeController, RecipeState>(() => RecipeController());
```

#### Estructura de Widgets

- Usa `ConsumerStatefulWidget` para stateful widgets con Riverpod
- Usa `super.key` en constructores
- Extrae árboles de widgets complejos en métodos privados

```dart
class RecipesPage extends ConsumerStatefulWidget {
  const RecipesPage({super.key});

  @override
  ConsumerState<RecipesPage> createState() => _RecipesPageState();
}
```

#### Manejo de errores

- Usa `try-catch` con tipos de excepción específicos siempre que sea posible
- Usa `debugPrint` para el registro (no `print`)
- Usa `EasyLoading.showError` para errores que detecta el usuario
- Usa claves de traducción para los mensajes de error

```dart
try {
  await operation();
} on AppwriteException catch (e) {
  debugPrint("Appwrite error: ${e.code} - ${e.message}");
  EasyLoading.showError("auth_controller.sign_in_error".tr);
} catch (e) {
  debugPrint("General error: $e");
  rethrow;
}
```

#### Localización

Utilice la extensión `.tr` para traducciones y `.trParams()` para cadenas parametrizadas:

```dart
"recipe_controller.loading_recipes".tr
"recipe_controller.load_error".trParams({"0": errorMessage})
```
Los archivos de traducción están en `assets/messages/` en formatos JSON. Hay un archivo para cada idioma soportado.

---

### Python (Functions)

#### Convenciones de nombres

- Archivos: `snake_case.py`
- Funciones: `snake_case` (e.g., `generate_recipe`)
- Clases: `PascalCase` (e.g., `Recipe`)
- Constantes: `UPPER_SNAKE_CASE` (e.g., `MAX_IMAGE_SIZE_BYTES`)

#### Sugerencias de tipo

Utilice siempre sugerencias de tipo para los parámetros de función y los tipos de retorno:

```python
def generate_recipe(img: str, lang: str) -> Recipe:
    ...
```

#### Manejo de errores

Devuelve respuestas de error JSON con códigos de estado HTTP apropiados:

```python
try:
    recipe = generate_recipe(image, language)
    return context.res.json(recipe.model_dump())
except Exception as e:
    context.error(f"Error generating recipe: {e}")
    return context.res.json({"error": "Failed to generate recipe"}, 500)
```

---

### TypeScript/Astro (Website)

#### Convenciones de nombres

- Componentes: `PascalCase.astro`
- Archivos de utilidad: `camelCase.ts`
- Tipos/interfaces: `PascalCase`
- Funciones/variables: `camelCase`

#### Alias ​​de ruta para las importaciones

Usa `@/` para las importaciones:

```typescript
import { detectUserLocale } from "@/utils/i18n";
```

---

## Dependencias clave

### Flutter

- **Gestión de estado**: Riverpod 3.0
- **Base de datos local**: Hive CE
- **Backend**: Appwrite SDK
- **Linting**: flutter_lints, riverpod_lint

### Sitio web

- **Framework**: Astro 5.14 (modo SSR)
- **Estilos**: Tailwind CSS 4.1
- **TypeScript**: Modo estricto

### Funciones de Python

- **Validación de datos**: Pydantic
- **IA**: google-genai
- **Backend**: Appwrite SDK

---

## Idiomas soportados (i18n)

de, en, es, fr, it, ja, ko, pt, zh

## Archivos importantes

- `lib/main.dart` - Punto de entrada de la aplicación
- `lib/appwrite_config.dart` - Instancia y configuración para usar Appwrite
- `lib/controllers/` - Controladores de estado de Riverpod
- `lib/models/recipe.dart` - Modelo de datos de recetas
- `lib/hive_boxes.dart` - Cajas de Hive para el almacenamiento local
- `lib/services/` - Servicios de lógica de negocio
- `pubspec.yaml` - Dependencias de Flutter
- `appwrite.config.json` - Configuración del proyecto Appwrite
- `website/astro.config.ts` - Configuración de Astro

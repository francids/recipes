import "package:flutter_animate/flutter_animate.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/add_recipe_controller.dart";
import "package:the_recipes/gestures/drag_start_listener.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/form_field.dart";
import "package:the_recipes/views/widgets/pressable_button.dart";

class DynamicListStepWidget extends ConsumerStatefulWidget {
  final String type;
  final String label;
  final VoidCallback? onChanged;

  const DynamicListStepWidget({
    super.key,
    required this.type,
    required this.label,
    this.onChanged,
  });

  @override
  ConsumerState<DynamicListStepWidget> createState() =>
      _DynamicListStepWidgetState();
}

class _DynamicListStepWidgetState extends ConsumerState<DynamicListStepWidget> {
  final Set<String> _animatedItems = {};
  final List<TextEditingController> _controllers = [];

  @override
  void dispose() {
    for (var controller in _controllers) {
      controller.dispose();
    }
    super.dispose();
  }

  void _syncControllers(List<String> list) {
    while (_controllers.length < list.length) {
      _controllers.add(TextEditingController());
    }
    while (_controllers.length > list.length) {
      _controllers.removeLast().dispose();
    }

    for (int i = 0; i < list.length; i++) {
      if (_controllers[i].text != list[i]) {
        _controllers[i].text = list[i];
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(addRecipeControllerProvider);
    final List<String> list = widget.type == "ingredients"
        ? state.ingredientsList
        : widget.type == "directions"
        ? state.directionsList
        : [];

    _syncControllers(list);

    return Column(
      children: [
        PressableButton(
              child: FilledButton(
                onPressed: () => _addItem(ref),
                child: Text(
                  "dynamic_list.add_item".trParams({"0": widget.label}),
                ),
              ),
            )
            .animate()
            .fadeIn(duration: 400.ms)
            .slideY(begin: -0.2, curve: Curves.easeOutCubic),
        const SizedBox(height: 16),
        _buildListContent(list, ref),
      ],
    );
  }

  Widget _buildListContent(List<String> list, WidgetRef ref) {
    if (list.isEmpty) {
      return Center(
        child: Text(
          "dynamic_list.no_items_yet".trParams({
            "0": widget.label.toLowerCase(),
          }),
          style: const TextStyle(
            color: Colors.grey,
            fontStyle: FontStyle.italic,
          ),
        ),
      ).animate().fadeIn(delay: 100.ms, duration: 300.ms);
    }

    return ReorderableListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      onReorderItem: (oldIndex, newIndex) =>
          _onReorder(oldIndex, newIndex, list, ref),
      itemCount: list.length,
      itemBuilder: (context, i) => _buildListItem(i, list, ref),
    ).animate().fadeIn(delay: 200.ms, duration: 400.ms);
  }

  Widget _buildListItem(int index, List<String> list, WidgetRef ref) {
    final itemKey = ValueKey("${widget.type}-$index");
    final itemId = "${widget.type}-$index";
    final shouldAnimate = !_animatedItems.contains(itemId);

    if (shouldAnimate) {
      _animatedItems.add(itemId);
    }

    return Animate(
      key: itemKey,
      delay: shouldAnimate ? (index * 50).ms : 0.ms,
      effects: shouldAnimate
          ? [
              FadeEffect(duration: 300.ms),
              SlideEffect(
                begin: const Offset(0.1, 0),
                curve: Curves.easeOutCubic,
              ),
            ]
          : [],
      child: Container(
        decoration: BoxDecoration(borderRadius: BorderRadius.circular(10)),
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: Row(
          children: [
            CustomDragStartListener(
              index: index,
              child: const Padding(
                padding: EdgeInsets.only(right: 8.0),
                child: Icon(Icons.drag_handle, color: Colors.grey),
              ),
            ),
            Expanded(
              child: ModernFormField(
                controller: _controllers[index],
                onChanged: (text) => _onItemChanged(index, text, list, ref),
                keyboardType: TextInputType.text,
                hintText: "${widget.label} ${index + 1}",
              ),
            ),
            PressableButton(
              child: IconButton(
                icon: const Icon(Icons.remove_circle, color: Colors.red),
                onPressed: () => _removeItem(index, list, ref),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _addItem(WidgetRef ref) {
    final state = ref.read(addRecipeControllerProvider);
    final list = widget.type == "ingredients"
        ? state.ingredientsList
        : state.directionsList;
    final newList = List<String>.from(list)..add("");
    if (widget.type == "ingredients") {
      ref.read(addRecipeControllerProvider.notifier).updateIngredients(newList);
    } else if (widget.type == "directions") {
      ref.read(addRecipeControllerProvider.notifier).updateDirections(newList);
    }
    widget.onChanged?.call();
  }

  void _onReorder(
    int oldIndex,
    int newIndex,
    List<String> list,
    WidgetRef ref,
  ) {
    final controller = _controllers.removeAt(oldIndex);
    _controllers.insert(newIndex, controller);

    final newList = List<String>.from(list);
    final item = newList.removeAt(oldIndex);
    newList.insert(newIndex, item);

    if (widget.type == "ingredients") {
      ref.read(addRecipeControllerProvider.notifier).updateIngredients(newList);
    } else if (widget.type == "directions") {
      ref.read(addRecipeControllerProvider.notifier).updateDirections(newList);
    }
    widget.onChanged?.call();
  }

  void _onItemChanged(
    int index,
    String text,
    List<String> list,
    WidgetRef ref,
  ) {
    final newList = List<String>.from(list);
    newList[index] = text;
    if (widget.type == "ingredients") {
      ref.read(addRecipeControllerProvider.notifier).updateIngredients(newList);
    } else if (widget.type == "directions") {
      ref.read(addRecipeControllerProvider.notifier).updateDirections(newList);
    }
    widget.onChanged?.call();
  }

  void _removeItem(int index, List<String> list, WidgetRef ref) {
    _controllers[index].dispose();
    _controllers.removeAt(index);

    final newList = List<String>.from(list)..removeAt(index);
    if (widget.type == "ingredients") {
      ref.read(addRecipeControllerProvider.notifier).updateIngredients(newList);
    } else if (widget.type == "directions") {
      ref.read(addRecipeControllerProvider.notifier).updateDirections(newList);
    }
    widget.onChanged?.call();
  }
}

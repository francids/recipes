import "package:flutter/cupertino.dart";
import "package:flutter/material.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:lottie/lottie.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/controllers/profile_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/screens/favorites_recipes_screen.dart";
import "package:the_recipes/views/screens/profile_info_screen.dart";
import "package:the_recipes/views/screens/public_recipes_search_screen.dart";
import "package:the_recipes/views/screens/shared_recipes_screen.dart";
import "package:the_recipes/views/widgets/pressable_button.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";
import "package:flutter_animate/flutter_animate.dart";

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      body: Consumer(
        builder: (context, ref, child) {
          final authController = ref.watch(authControllerProvider);
          if (!authController.isLoggedIn) {
            return Center(
              child: _buildSignInPrompt(context, ref),
            );
          }
          return SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              spacing: 16,
              children: [
                SizedBox(
                  height: MediaQuery.of(context).padding.top,
                ),
                _buildProfileHeader(context, ref)
                    .animate()
                    .fadeIn(duration: 300.ms)
                    .slideX(begin: -0.2, curve: Curves.easeOutCubic),
                _buildFavoritesCard(context, ref)
                    .animate()
                    .fadeIn(delay: 50.ms, duration: 250.ms)
                    .slideX(begin: -0.15, curve: Curves.easeOutCubic),
                _buildRecipesManagementCard(context, ref)
                    .animate()
                    .fadeIn(delay: 100.ms, duration: 250.ms)
                    .slideX(begin: -0.15, curve: Curves.easeOutCubic),
                const Divider(
                  indent: 16,
                  endIndent: 16,
                )
                    .animate()
                    .fadeIn(delay: 150.ms, duration: 200.ms)
                    .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                _buildSignOutCard(context, ref)
                    .animate()
                    .fadeIn(delay: 200.ms, duration: 250.ms)
                    .slideX(begin: -0.15, curve: Curves.easeOutCubic),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildSignInPrompt(
    BuildContext context,
    WidgetRef ref,
  ) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;
    final AuthController authController =
        ref.read(authControllerProvider.notifier);

    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          LottieBuilder.asset(
            "assets/lottie/profile.json",
            width: 80,
            height: 80,
            fit: BoxFit.contain,
          ),
          const SizedBox(height: 24),
          Text(
            "profile_page.sign_in_required".tr,
            style: textTheme.displayMedium,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 8),
          Text(
            "profile_page.sign_in_description".tr,
            style: textTheme.bodyMedium,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          PressableButton(
            child: FilledButton.icon(
              onPressed: () => authController.signInWithGoogle(),
              icon: const Icon(CupertinoIcons.person_add),
              label: Text("profile_page.sign_in_with_google".tr),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileHeader(
    BuildContext context,
    WidgetRef ref,
  ) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;
    final authController = ref.watch(authControllerProvider);
    final user = authController.user;

    return Card(
      elevation: 0,
      margin: EdgeInsets.zero,
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () => Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => const ProfileInfoScreen(),
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Row(
            children: [
              CircleAvatar(
                radius: 32,
                backgroundColor: theme.colorScheme.primary.withAlpha(25),
                backgroundImage: authController.userProfileImageUrl != null
                    ? NetworkImage(authController.userProfileImageUrl!)
                    : null,
                child: authController.userProfileImageUrl == null
                    ? Icon(
                        CupertinoIcons.person,
                        size: 32,
                        color: theme.colorScheme.primary,
                      )
                    : null,
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      user?.name.isNotEmpty == true
                          ? user!.name
                          : "profile_page.unknown_user".tr,
                      style: textTheme.displayMedium,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      user?.email ?? "",
                      style: textTheme.bodyMedium,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSignOutCard(BuildContext context, WidgetRef ref) {
    final authController = ref.read(authControllerProvider.notifier);

    return Card(
      elevation: 0,
      margin: EdgeInsets.zero,
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () {
          UIHelpers.showConfirmationDialog(
            context: context,
            title: "profile_page.sign_out_confirmation_title".tr,
            message: "profile_page.sign_out_confirmation_message".tr,
            lottieAsset: "assets/lottie/exit.json",
            confirmAction: () {
              authController.signOut();
            },
          );
        },
        child: ListTile(
          title: Text(
            "profile_page.sign_out".tr,
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                  fontWeight: FontWeight.w500,
                ),
          ),
          subtitle: Text(
            "profile_page.sign_out_description".tr,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: Theme.of(context).colorScheme.outline,
                ),
          ),
          leading: Icon(
            CupertinoIcons.square_arrow_right,
            color: Theme.of(context).colorScheme.primary,
          ),
          trailing: Icon(
            CupertinoIcons.chevron_forward,
            color: Theme.of(context).colorScheme.outline,
          ),
        ),
      ),
    );
  }

  Widget _buildRecipesManagementCard(
    BuildContext context,
    WidgetRef ref,
  ) {
    final authState = ref.watch(authControllerProvider);
    final profileController = ref.read(profileControllerProvider);

    return Card(
      elevation: 0,
      margin: EdgeInsets.zero,
      clipBehavior: Clip.antiAlias,
      child: Column(
        children: [
          SwitchListTile(
            title: Text(
              "profile_page.backup_recipes".tr,
              style: Theme.of(context).textTheme.bodyLarge,
            ),
            subtitle: Text(
              "profile_page.backup_recipes_description".tr,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Theme.of(context).colorScheme.outline,
                  ),
            ),
            value: authState.autoSyncEnabled,
            onChanged: (value) =>
                profileController.handleAutoSyncToggle(value, context),
            secondary: Icon(
              CupertinoIcons.cloud_upload,
              color: Theme.of(context).colorScheme.primary,
            ),
          ),
          if (authState.autoSyncEnabled) ...[
            const Divider(height: 1),
            ListTile(
              title: Text(
                "profile_page.shared_recipes".tr,
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      fontWeight: FontWeight.w500,
                    ),
              ),
              subtitle: Text(
                "profile_page.shared_recipes_description".tr,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: Theme.of(context).colorScheme.outline,
                    ),
              ),
              leading: Icon(
                CupertinoIcons.person_2_square_stack,
                color: Theme.of(context).colorScheme.primary,
              ),
              trailing: Icon(
                CupertinoIcons.chevron_forward,
                color: Theme.of(context).colorScheme.outline,
              ),
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const SharedRecipesScreen(),
                ),
              ),
            ),
          ],
          const Divider(height: 1),
          ListTile(
            title: Text(
              "profile_page.search_public_recipes".tr,
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    fontWeight: FontWeight.w500,
                  ),
            ),
            subtitle: Text(
              "profile_page.search_public_recipes_description".tr,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Theme.of(context).colorScheme.outline,
                  ),
            ),
            leading: Icon(
              CupertinoIcons.search,
              color: Theme.of(context).colorScheme.primary,
            ),
            trailing: Icon(
              CupertinoIcons.chevron_forward,
              color: Theme.of(context).colorScheme.outline,
            ),
            onTap: () => Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => const PublicRecipesSearchScreen(),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFavoritesCard(
    BuildContext context,
    WidgetRef ref,
  ) {
    return Card(
      elevation: 0,
      margin: EdgeInsets.zero,
      clipBehavior: Clip.antiAlias,
      child: ListTile(
        title: Text(
          "profile_page.favorite_recipes".tr,
          style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                fontWeight: FontWeight.w500,
              ),
        ),
        subtitle: Text(
          "profile_page.favorite_recipes_description".tr,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Theme.of(context).colorScheme.outline,
              ),
        ),
        leading: Icon(
          CupertinoIcons.heart,
          color: Theme.of(context).colorScheme.primary,
        ),
        trailing: Icon(
          CupertinoIcons.chevron_forward,
          color: Theme.of(context).colorScheme.outline,
        ),
        onTap: () => Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => const FavoritesRecipesScreen(),
          ),
        ),
      ),
    );
  }
}

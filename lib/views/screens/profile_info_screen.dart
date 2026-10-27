import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_animate/flutter_animate.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class ProfileInfoScreen extends ConsumerWidget {
  const ProfileInfoScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;

    return Scaffold(
      appBar: AppBar(
        title: Text("profile_info_screen.title".tr),
        leading: IconButton(
          onPressed: () => Navigator.of(context).pop(),
          icon: const Icon(CupertinoIcons.back),
        ),
      ),
      body: Consumer(
        builder: (context, ref, child) {
          final controller = ref.watch(authControllerProvider);
          if (!controller.isLoggedIn) {
            return _buildNotSignedInView(context);
          }

          final user = controller.user!;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildProfileAvatar(
                      context,
                      controller.userProfileImageUrl ?? "",
                    )
                    .animate()
                    .fadeIn(duration: 300.ms)
                    .scale(
                      begin: const Offset(0.8, 0.8),
                      curve: Curves.easeOutCubic,
                    ),
                const SizedBox(height: 32),
                Text(
                      "profile_info_screen.personal_info".tr,
                      style: textTheme.displayMedium,
                    )
                    .animate()
                    .fadeIn(delay: 100.ms, duration: 250.ms)
                    .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                const SizedBox(height: 16),
                Card(
                      elevation: 0,
                      margin: EdgeInsets.zero,
                      clipBehavior: Clip.antiAlias,
                      child: Column(
                        children: [
                          _buildInfoListTile(
                            context,
                            title: "profile_info_screen.display_name".tr,
                            value: user.name.isNotEmpty
                                ? user.name
                                : "profile_info_screen.not_available".tr,
                            icon: CupertinoIcons.person,
                          ),
                          const Divider(height: 1, indent: 16, endIndent: 16),
                          _buildInfoListTile(
                            context,
                            title: "profile_info_screen.email".tr,
                            value: user.email.isNotEmpty
                                ? user.email
                                : "profile_info_screen.not_available".tr,
                            icon: CupertinoIcons.mail,
                          ),
                          const Divider(height: 1, indent: 16, endIndent: 16),
                          _buildInfoListTile(
                            context,
                            title: "profile_info_screen.user_id".tr,
                            value: user.$id,
                            icon: CupertinoIcons.number,
                            isSelectable: true,
                          ),
                        ],
                      ),
                    )
                    .animate()
                    .fadeIn(delay: 150.ms, duration: 250.ms)
                    .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                const SizedBox(height: 32),
                Text(
                      "profile_info_screen.account_details".tr,
                      style: textTheme.displayMedium,
                    )
                    .animate()
                    .fadeIn(delay: 300.ms, duration: 250.ms)
                    .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                const SizedBox(height: 16),
                Card(
                      elevation: 0,
                      margin: EdgeInsets.zero,
                      clipBehavior: Clip.antiAlias,
                      child: Column(
                        children: [
                          _buildInfoListTile(
                            context,
                            title: "profile_info_screen.email_verified".tr,
                            value: user.emailVerification
                                ? "profile_info_screen.verified".tr
                                : "profile_info_screen.not_verified".tr,
                            icon: user.emailVerification
                                ? CupertinoIcons.checkmark_shield
                                : CupertinoIcons.exclamationmark_shield,
                            valueColor: user.emailVerification
                                ? Colors.green
                                : Colors.orange,
                          ),
                          const Divider(height: 1, indent: 16, endIndent: 16),
                          _buildInfoListTile(
                            context,
                            title: "profile_info_screen.account_created".tr,
                            value: user.registration.isNotEmpty
                                ? _formatDate(DateTime.parse(user.registration))
                                : "profile_info_screen.not_available".tr,
                            icon: CupertinoIcons.calendar,
                          ),
                          const Divider(height: 1, indent: 16, endIndent: 16),
                          _buildInfoListTile(
                            context,
                            title: "profile_info_screen.last_sign_in".tr,
                            value: user.accessedAt.isNotEmpty
                                ? _formatDate(DateTime.parse(user.accessedAt))
                                : "profile_info_screen.not_available".tr,
                            icon: CupertinoIcons.time,
                          ),
                        ],
                      ),
                    )
                    .animate()
                    .fadeIn(delay: 350.ms, duration: 250.ms)
                    .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                const SizedBox(height: 32),
                Text(
                      "profile_info_screen.danger_zone".tr,
                      style: textTheme.displayMedium?.copyWith(
                        color: Colors.red,
                      ),
                    )
                    .animate()
                    .fadeIn(delay: 400.ms, duration: 250.ms)
                    .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                const SizedBox(height: 16),
                Card(
                      elevation: 0,
                      margin: EdgeInsets.zero,
                      clipBehavior: Clip.antiAlias,
                      color: Colors.red.withAlpha(25),
                      child: ListTile(
                        title: Text(
                          "profile_info_screen.delete_account".tr,
                          style: textTheme.bodyLarge?.copyWith(
                            color: Colors.red,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        subtitle: Text(
                          "profile_info_screen.delete_account_warning".tr,
                          style: textTheme.bodySmall?.copyWith(
                            color: Colors.red.withAlpha(179),
                          ),
                        ),
                        leading: const Icon(
                          CupertinoIcons.delete,
                          color: Colors.red,
                        ),
                        trailing: const Icon(
                          CupertinoIcons.chevron_right,
                          color: Colors.red,
                        ),
                        onTap: () => _showDeleteAccountDialog(context, ref),
                      ),
                    )
                    .animate()
                    .fadeIn(delay: 450.ms, duration: 250.ms)
                    .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                const SizedBox(height: 32),
              ],
            ),
          );
        },
      ),
    );
  }

  void _showDeleteAccountDialog(BuildContext context, WidgetRef ref) {
    UIHelpers.showConfirmationDialog(
      context: context,
      title: "profile_info_screen.confirm_delete_title".tr,
      message:
          "profile_info_screen.confirm_delete_enhanced_message_with_reauth".tr,
      lottieAsset: "assets/lottie/delete.json",
      confirmAction: () {
        final authController = ref.read(authControllerProvider.notifier);
        authController.deleteAccount().then((_) {
          final authState = ref.read(authControllerProvider);
          if (context.mounted && !authState.isLoggedIn) {
            Navigator.of(context).pop();
          }
        });
      },
    );
  }

  Widget _buildNotSignedInView(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              CupertinoIcons.exclamationmark_circle,
              size: 80,
              color: theme.colorScheme.outline,
            ),
            const SizedBox(height: 24),
            Text(
              "profile_info_screen.not_signed_in".tr,
              style: textTheme.displayMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              "profile_info_screen.sign_in_to_view".tr,
              style: textTheme.bodyMedium,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileAvatar(BuildContext context, String? photoURL) {
    final theme = Theme.of(context);

    return Center(
      child: Container(
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: Border.all(
            color: theme.colorScheme.outline.withAlpha(51),
            width: 2,
          ),
        ),
        child: CircleAvatar(
          radius: 60,
          backgroundColor: theme.colorScheme.primary.withAlpha(25),
          backgroundImage: photoURL != null ? NetworkImage(photoURL) : null,
          child: photoURL == null
              ? Icon(
                  CupertinoIcons.person,
                  size: 60,
                  color: theme.colorScheme.primary,
                )
              : null,
        ),
      ),
    );
  }

  Widget _buildInfoListTile(
    BuildContext context, {
    required String title,
    required String value,
    required IconData icon,
    bool isSelectable = false,
    Color? valueColor,
  }) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;

    return ListTile(
      title: Text(title, style: textTheme.bodyLarge),
      subtitle: isSelectable
          ? SelectableText(
              value,
              style: textTheme.bodySmall?.copyWith(
                color: valueColor ?? theme.colorScheme.outline,
              ),
            )
          : Text(
              value,
              style: textTheme.bodySmall?.copyWith(
                color: valueColor ?? theme.colorScheme.outline,
              ),
            ),
      leading: Icon(icon, color: theme.colorScheme.primary),
    );
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date);

    if (difference.inDays > 365) {
      final years = (difference.inDays / 365).floor();
      return "profile_info_screen.years_ago".trParams({"0": years.toString()});
    } else if (difference.inDays > 30) {
      final months = (difference.inDays / 30).floor();
      return "profile_info_screen.months_ago".trParams({
        "0": months.toString(),
      });
    } else if (difference.inDays > 0) {
      return "profile_info_screen.days_ago".trParams({
        "0": difference.inDays.toString(),
      });
    } else if (difference.inHours > 0) {
      return "profile_info_screen.hours_ago".trParams({
        "0": difference.inHours.toString(),
      });
    } else {
      return "profile_info_screen.minutes_ago".trParams({
        "0": difference.inMinutes.toString(),
      });
    }
  }
}

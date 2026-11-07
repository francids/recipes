class AppUser {
  final String id;
  final String name;
  final String email;
  final String? picture;
  final bool emailVerification;
  final String registration;
  final String accessedAt;

  const AppUser({
    required this.id,
    required this.name,
    required this.email,
    this.picture,
    required this.emailVerification,
    required this.registration,
    required this.accessedAt,
  });

  factory AppUser.fromMap(Map<String, dynamic> map) {
    return AppUser(
      id: map["id"]?.toString() ?? "",
      name: map["name"]?.toString() ?? "",
      email: map["email"]?.toString() ?? "",
      picture: map["picture"]?.toString(),
      emailVerification: map["emailVerification"] == true,
      registration: map["registration"]?.toString() ?? "",
      accessedAt: map["accessedAt"]?.toString() ?? "",
    );
  }
}

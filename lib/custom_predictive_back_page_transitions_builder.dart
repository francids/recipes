import "package:flutter/services.dart";
import "package:material_ui/material_ui.dart";

class CustomPredictiveBackPageTransitionsBuilder
    extends PageTransitionsBuilder {
  const CustomPredictiveBackPageTransitionsBuilder();

  @override
  Widget buildTransitions<T>(
    PageRoute<T> route,
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
    Widget child,
  ) {
    return _CustomPredictiveBackGestureDetector(
      route: route,
      animation: animation,
      secondaryAnimation: secondaryAnimation,
      child: child,
    );
  }
}

enum _PredictiveBackPhase { idle, start, update, commit, cancel }

class _CustomPredictiveBackGestureDetector extends StatefulWidget {
  const _CustomPredictiveBackGestureDetector({
    required this.route,
    required this.animation,
    required this.secondaryAnimation,
    required this.child,
  });

  final PageRoute<dynamic> route;
  final Animation<double> animation;
  final Animation<double> secondaryAnimation;
  final Widget child;

  @override
  State<_CustomPredictiveBackGestureDetector> createState() =>
      _CustomPredictiveBackGestureDetectorState();
}

class _CustomPredictiveBackGestureDetectorState
    extends State<_CustomPredictiveBackGestureDetector>
    with WidgetsBindingObserver {
  bool get _isEnabled {
    return widget.route.isCurrent && widget.route.popGestureEnabled;
  }

  _PredictiveBackPhase _phase = _PredictiveBackPhase.idle;
  PredictiveBackEvent? _startBackEvent;
  PredictiveBackEvent? _currentBackEvent;

  @override
  bool handleStartBackGesture(PredictiveBackEvent backEvent) {
    final bool gestureInProgress = !backEvent.isButtonEvent && _isEnabled;
    if (!gestureInProgress) {
      return false;
    }

    setState(() {
      _phase = _PredictiveBackPhase.start;
      _startBackEvent = backEvent;
      _currentBackEvent = backEvent;
    });

    widget.route.handleStartBackGesture(progress: 1 - backEvent.progress);
    return true;
  }

  @override
  void handleUpdateBackGestureProgress(PredictiveBackEvent backEvent) {
    setState(() {
      _phase = _PredictiveBackPhase.update;
      _currentBackEvent = backEvent;
    });

    widget.route.handleUpdateBackGestureProgress(
      progress: 1 - backEvent.progress,
    );
  }

  @override
  void handleCancelBackGesture() {
    setState(() {
      _phase = _PredictiveBackPhase.cancel;
      _startBackEvent = null;
      _currentBackEvent = null;
    });

    widget.route.handleCancelBackGesture();
  }

  @override
  void handleCommitBackGesture() {
    setState(() {
      _phase = _PredictiveBackPhase.commit;
      _startBackEvent = null;
      _currentBackEvent = null;
    });

    widget.route.handleCommitBackGesture();
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final effectivePhase = widget.route.popGestureInProgress
        ? _phase
        : _PredictiveBackPhase.idle;

    return _CustomPredictiveBackPageTransition(
      animation: widget.animation,
      secondaryAnimation: widget.secondaryAnimation,
      phase: effectivePhase,
      startBackEvent: _startBackEvent,
      currentBackEvent: _currentBackEvent,
      hasGesture: widget.route.popGestureInProgress,
      child: widget.child,
    );
  }
}

class _CustomPredictiveBackPageTransition extends StatefulWidget {
  const _CustomPredictiveBackPageTransition({
    required this.animation,
    required this.secondaryAnimation,
    required this.phase,
    required this.startBackEvent,
    required this.currentBackEvent,
    required this.hasGesture,
    required this.child,
  });

  final Animation<double> animation;
  final Animation<double> secondaryAnimation;
  final _PredictiveBackPhase phase;
  final PredictiveBackEvent? startBackEvent;
  final PredictiveBackEvent? currentBackEvent;
  final bool hasGesture;
  final Widget child;

  @override
  State<_CustomPredictiveBackPageTransition> createState() =>
      _CustomPredictiveBackPageTransitionState();
}

class _CustomPredictiveBackPageTransitionState
    extends State<_CustomPredictiveBackPageTransition>
    with SingleTickerProviderStateMixin {
  static const double _kDivisionFactor = 20.0;
  static const double _kMargin = 8.0;
  static const int _kCommitMilliseconds = 300;
  static const Curve _kCurve = Curves.fastOutSlowIn;
  static const Interval _kCommitInterval = Interval(
    0.0,
    _kCommitMilliseconds / 500,
    curve: _kCurve,
  );

  final Tween<double> _opacityTween = Tween<double>(begin: 1.0, end: 0.0);
  final ProxyAnimation _commitAnimation = ProxyAnimation();
  final ProxyAnimation _animation = ProxyAnimation();

  CurvedAnimation? _curvedAnimation;
  CurvedAnimation? _curvedAnimationReversed;
  late Animation<Offset> _positionAnimation;
  Offset _lastDrag = Offset.zero;

  Offset _calculateOffset(Size screenSize) {
    if (widget.currentBackEvent?.swipeEdge != SwipeEdge.left) {
      return Offset.zero;
    }

    final double xShift = (screenSize.width / _kDivisionFactor) - _kMargin;
    final double progress = widget.currentBackEvent?.progress ?? 0.0;

    return Offset(xShift * progress, 0.0);
  }

  void _updateAnimations(Size screenSize) {
    _animation.parent = switch (widget.phase) {
      _PredictiveBackPhase.commit => _curvedAnimationReversed,
      _ => widget.animation,
    };

    _commitAnimation.parent = switch (widget.phase) {
      _PredictiveBackPhase.commit => _animation,
      _ => kAlwaysDismissedAnimation,
    };

    final double xShift = (screenSize.width / _kDivisionFactor) - _kMargin;
    final bool shouldAnimate =
        widget.currentBackEvent?.swipeEdge == SwipeEdge.left;

    _positionAnimation = _animation.drive(switch (widget.phase) {
      _PredictiveBackPhase.commit => Tween<Offset>(
        begin: _lastDrag,
        end: Offset(screenSize.width, 0.0),
      ),
      _ => Tween<Offset>(
        begin: Offset.zero,
        end: shouldAnimate ? Offset(xShift, 0.0) : Offset.zero,
      ),
    });
  }

  void _updateCurvedAnimations() {
    _curvedAnimation?.dispose();
    _curvedAnimationReversed?.dispose();
    _curvedAnimation = CurvedAnimation(
      parent: widget.animation,
      curve: _kCommitInterval,
    );
    _curvedAnimationReversed = CurvedAnimation(
      parent: ReverseAnimation(widget.animation),
      curve: _kCommitInterval,
    );
  }

  @override
  void initState() {
    super.initState();
  }

  @override
  void didUpdateWidget(_CustomPredictiveBackPageTransition oldWidget) {
    super.didUpdateWidget(oldWidget);

    if (widget.animation != oldWidget.animation) {
      _updateCurvedAnimations();
    }
    if (widget.phase != oldWidget.phase) {
      _updateAnimations(MediaQuery.sizeOf(context));
    }
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _updateCurvedAnimations();
    _updateAnimations(MediaQuery.sizeOf(context));
  }

  @override
  void dispose() {
    _curvedAnimation?.dispose();
    _curvedAnimationReversed?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final screenSize = MediaQuery.sizeOf(context);

    if (!widget.hasGesture) {
      return SlideTransition(
        position: Tween<Offset>(begin: const Offset(1, 0.0), end: Offset.zero)
            .animate(
              CurvedAnimation(
                parent: widget.animation,
                curve: Curves.easeOutCubic,
                reverseCurve: Curves.easeInOut,
              ),
            ),
        child: widget.child,
      );
    }

    return AnimatedBuilder(
      animation: widget.animation,
      builder: (BuildContext context, Widget? child) {
        final offset = switch (widget.phase) {
          _PredictiveBackPhase.idle => Offset.zero,
          _PredictiveBackPhase.commit => _positionAnimation.value,
          _PredictiveBackPhase.start || _PredictiveBackPhase.update =>
            _lastDrag = _calculateOffset(screenSize),
          _PredictiveBackPhase.cancel => Offset.zero,
        };

        return Transform.translate(
          offset: offset,
          child: Opacity(
            opacity: _opacityTween.evaluate(_commitAnimation),
            child: child,
          ),
        );
      },
      child: widget.child,
    );
  }
}

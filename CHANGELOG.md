# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [0.2.0] 2023-11-11

### Added
- Added an `enterAlways` param to `CollapsableTopBehaivor` to start expanding as soon as you start
  scrolling up instead of only when you reach the top of the scrolling content.
- Added methods to directly expand/collapse `CollapsableState` with or without an animation.

### Changed
- Clip children of `CollapsableColumn` instead of the column itself. This makes it easier to draw
  outside it's bounds as well as not have the children overlap other views when you collapse. You
  can disable this clipping for a specific child with `Modifier.collapse(clip = false)`.
- `CollapsableBehavior` has been renamed to `CollapsableTopBehavior` to make it more clear it's
  designed to work with collapsable content on top of scrolling content.
- behavior argument is now required in `CollapsableColumn` overload that take a
  `CollapsableTopBehavior` to make it more clear which one you are calling.

### Fixed
- Fixed CollapsableColumn child placement on more complex cases of collapsable and non-collapsable
  children.

## [0.1.0] 2023-11-05

### Added
- Initial Release
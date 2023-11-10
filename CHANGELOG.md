# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Clip children of CollapsableColumn instead of the column itself. This makes it easier to draw
  outside it's bounds as well as not have the children overlap other views when you collapse. You
  can disable this clipping for a specific child with `Modifier.collapse(clip = false)`.

## [0.1.0] 2023-11-05

### Added
- Initial Release
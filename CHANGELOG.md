# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### `scitos-hmx`
#### Fixed
- regression introduced in 2.4.1: misleading warning about failed save even though it was successful

## [2.4.1] - 2020-10-31
### `scitos-hmx`
#### Fixed
- in some situations, relations are not properly displayed

## [2.4.0] - 2020-09-13
### `scitos`
#### Added
- capability to generate basic macOS `.app` bundle alternative for future releases

#### Changed
- __*BREAKING CHANGE*__: Minimum Java version is now `9` (and no longer `8`)! *not relevant for the macOS `.app` as a JRE 11 is included in the bundle*

#### Fixed
- show error message if the closing of the connection to a saved file encounters an error
- re-instate macOS specific handlers for About/Preferences/Quit actions (necessitated by changes in Java 9)

## [2.3.1] - 2020-03-09
### `scitos-ais`
#### Fixed
- improve error handling for import of interviews from ODS spreadsheet #29

## [2.3.0] - 2020-03-08
### `scitos`
#### Changed
- BREAKING CHANGE: Minimum Java version is now `8` (and no longer `6`)!

### `scitos-ais`
#### Added
- allow importing multiple unscored interviews at once from an ODS spreadsheet

## [2.2.0] - 2017-09-27
### `scitos-hmx`
#### Added
- allow showing both translations (syntactical/semantical) at the same time #16
- single analysis view with toggles for showing/hiding separate parts #17

#### Fixed
- translation in SVG export of semantical analysis #11
- data loss in specific situation for "Split Proposition" action #21

## [2.1.0] - 2017-09-15
### `scitos-hmx`
#### Added
- offering new menu items for hiding the proposition labels and translations, allowing you to focus on the analysis part at hand (and to cater for uses of HermeneutiX where one of those fields is not being used) #9

#### Changed
- If the translation fields are hidden, the height of each displayed proposition is reduced. Therefore, more propositions can be shown at once. In case of the Semantical Analysis this also required a slight change in the way how the roles within relations are being displayed, in order to fit into the smaller available space.
The SVG export however is not affected by these changes. Labels and translations are still always included there.

#### Fixed
- now enabling the usage of undo/redo also on the Analysis view (and not just on the initial Text Input view) #7

## [2.0.0] - 2016-07-29
### `scitos-hmx` (HermeneutiX)
#### Added
- new Module – a graphical tool for syntactic and semantic structure analysis of complex (foreign language) texts with a number of changes in comparison to the standalone HermeneutiX v1.12. #4
- configurability of available relations for Semantical Analysis: assignable roles in relations
- configurability of available origin languages for Syntactical Analysis including the assignable functions for Clause Items and Propositions
- allow saving of project in text input mode
- new more self-contained file structure for saving
- maintaining backwards compatible opening of old file structure
- allow project creation without requiring a target file path

#### Fixed
- various (minor) bug fixes, in comparison to the standalone predecessor HermeneutiX v1.12

### `scitos-ais`
#### Fixed
- for some cases when assigning a detail category to tokens that already have a specific constellation of detail categories assigned to them

## [1.2.0] - 2015-09-14
### `scitos`
#### Added
- allow specific setting of the UI translation via the 'Preferences' dialog.
- increasing/decreasing the global content font size via the new 'View' menu. #2
- hide/show project tree via the new 'View' menu, to maximize usable space. #3

### `scitos-ais`
#### Changed
- added the result table Spreadsheet export to the 'File' > 'Export' menu entry.

#### Fixed
- adjust width of result table columns to fit their contents, enabling a horizontal scroll bar if necessary. #5
- added missing error message, for the 'selected Project file is already open' case.

## [1.1.0] - 2015-09-06
### `scitos-ais`
#### Added
- 'Export' entry in the 'File' menu, allowing the generation of a HTML representation, which can be displayed in any modern browser out-of-the-box.

## [1.0.0] - 2015-09-04
### `scitos`
#### Added
- General framework for adding different types of tools with some common/shared (technical) features

### `scitos-ais` (Autobiographical Interview Scoring
#### Added
- Scoring via mouse and/or keyboard short cuts
- Use of a configurable category model (i.e. specific aspects of internal and external details)
- Automatic tallying
- Result export to ODS Spreadsheet
- XML based file format, that can also be viewed in modern Web Browsers (and printed there)
    - Firefox does this out-of-the-box,
    - other browsers (e.g. Chrome) might prevent this by default for local files (for security reasons), as this feature is realised via an embedded XSLT stylesheet (i.e. a script)


[Unreleased]: https://github.com/scientific-tool-set/scitos/compare/v2.4.1...HEAD
[2.4.1]: https://github.com/scientific-tool-set/scitos/compare/v2.4.0...v2.4.1
[2.4.0]: https://github.com/scientific-tool-set/scitos/compare/v2.3.1...v2.4.0
[2.3.1]: https://github.com/scientific-tool-set/scitos/compare/v2.3.0...v2.3.1
[2.3.0]: https://github.com/scientific-tool-set/scitos/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/scientific-tool-set/scitos/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/scientific-tool-set/scitos/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/scientific-tool-set/scitos/compare/v1.2.0...v2.0.0
[1.2.0]: https://github.com/scientific-tool-set/scitos/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/scientific-tool-set/scitos/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/scientific-tool-set/scitos/releases/tag/v1.0.0

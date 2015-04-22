# The Problem #
Some editors insert a "BOM" or Byte Order Mark into UTF-8 files.  This BOM is optional and not-recommended and can cause issues for parsing of XML and Java files during builds and at runtime.  If you have a large set of files being edited by a large group of people ( e.g. a large team of developers ) it becomes increasingly hard to avoid these BOMs from creeping into the source code.

This plugin scans your source code and remove any BOMs that it finds.  It is imperative that this plugin only be run on UTF-8 files.

Vist the Wiki MainPage for more information and links.

# Status #

This project is currently in its 1.0.0 Alpha period.  Therefore no release has been cut.  A SNAPSHOT is available.

The plugin should be downloaded by maven so please follow the directions in the ConfigurePlugin page.
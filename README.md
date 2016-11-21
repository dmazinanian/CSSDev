# CSSDev

An eclipse plugin for css-analyser.

We support refactoring duplication to grouping selectors and Mixins (for the moment, in the Less syntax. It is not difficult to add the functionality for Sass as well).

## License

This project is licensed under the MIT License.

## Usage

For the moment you have to import the plug-in code to eclipse and run it as an Eclipse Application.

I will create update sites, etc when the code reaches a more stable state :)


1. Download (or clone) [css-analyser](https://github.com/dmazinanian/css-analyser).
2. Download (or clone) `CSSDev`, and **put it right besides the folder of `css-analyser`**.
3. Install [Gradle](http://gradle.org/).
4. Run `gradle buildAndCopyLibs` inside `CSSDev` directory.
`css-analyser` is built, and the required files are copied into `CSSDev/libs` directory
5. Import the plug-in project to Eclipse and make an Eclipse Application run configuration. Run it with default settings.
6. In the run-time IDE, open a CSS file. Right click in the editor (or, if the CSS file is in a project, on the CSS file in the package explorer), and from the CSSDev menu, select Duplications.
7. In the opened Duplications view, click on the little i icon in the top-right corner of the view to detect declaration-level duplications.
8. Right click on the duplication you want to get rid of to see the options for refactoring.
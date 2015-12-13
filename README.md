# css-analyser-eclipse-plugin

An eclipse plugin for css-analyser.

We support refactoring duplication to grouping selectors and Mixins (in the Less syntax).

## License

This project is licensed under the MIT License.

## Usage

For the moment you have to import the plug-in code to eclipse and run it as an Eclipse Application.
I will create update sites, etc when the code reaches a more stable state :)

1. Download (or clone) [css-analyser](https://github.com/dmazinanian/css-analyser).
2. Follow the instructions in the README file to build it.
3. Make a folder `lib` in the root of the plug-in directory.
4. Copy the contents of the `css-analyser\build\libs` (which is created when you build css-analyser) to the folder you made in step 3 (I use a symboli link instead of copying the data, but this is optional). 
5. Import the plug-in project to Eclipse and make an Eclipse Application run configuration. Run it with default settings.
6. In the run-time IDE, open a CSS file. Right click in the editor (or, if the CSS file is in a project, on the CSS file in the package explorer), and from the css-analyser menu, select Duplications.
7. In the opened Duplications view, click on the little i icon in the top-right corner of the view to detect declaration-level duplications.
8. Right click on the duplication you want to get rid of to see the options for refactoring.
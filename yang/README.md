# Syntax highlighting for Yang files

## Intellij Idea

### Installation

Copy file `idea/yang.xml` to `<idea config dir>/config/filetypes`.  `<idea config dir>` is usually located in your home directory, e.g. `~/.IdeaIC12`.

## Eclipse

### Installation

   * Install `Color Editor` plugin http://gstaff.org/colorEditor/
   * Copy `eclipse/yang.xml` into plugin `modes` directory (tip: rename jar file to zip, copy `yang.xml` to zip file, rename file back to jar)
   * update `catalog` file in `modes` directory, add folowing line to the end before last `</MODES>`

          <MODE NAME="yang"               FILE="yang.xml"
                                          FILE_NAME_GLOB="*.yang" />


   * start eclipse, open yang file with `Open with ...`, choose `Syntax coloring editor`

# Fomatter (indentation)

   * requires emacs to be installed   (`sudo apt-get install emacs` or `sudo apt-get install emacs23-nox`)
   * Copy `formatter/indent_yang` file shell script to some directory that is referenced by $PATH variable (e.g. `~/bin')
   * In Intellij Idea go to Settings -> Extrnal Tools, install it as external tool with  `$FilePath$` parameter
   * in Eclipse -> Install it as External Tool (uncheck Build before launch), note: Indent only saved file

`indent_yang` file provided by Miroslav Hedl

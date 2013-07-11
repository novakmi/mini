#!/usr/bin/env groovy
/*
Copyright (c) 2009 bubbles.way@gmail.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/**
* Project:AccentRemove
*/


class AccentRemove {
        /**
         * //http://www.rgagnon.com/javadetails/java-0456.html
         * //http://www.macroware.cz/software/unicode/
         * Remove accent in string
         */
        static public String removeAccents(String text) {
                java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        }

        /**
         * Get list of files and subdirectories to be normalized.
         * The list contains pairs of original name and normlized name
         * @param File representing root directory to start 
         */
        static List getCandidates(dirFile) {
                def files = dirFile.listFiles()
                List candidateList = new ArrayList()
                files.each {
                        def fileName = it.getPath()
                        def newFileName = removeAccents(fileName)
                        if (fileName != newFileName) {
                                candidateList.add([fileName, newFileName])
                        }
                        if (it.isDirectory()) {
                                candidateList.addAll(getCandidates(it))
                        }
                }
                return candidateList
        }

        /**
         *  Rename files and directories to non normalized from.
         *  Go recursively into directories
         */
        static def normalizeFiles(dirFile) {
                def files = dirFile.listFiles()
                List candidateList = new ArrayList()
                files.each {
                        def file = it
                        def fileName = file.getPath()
                        def newFileName = removeAccents(fileName)
                        if (fileName != newFileName) {
                                println "Renaming  " + it.getPath() + " -> " + newFileName
                                file = new File(newFileName)
                                ((File) it).renameTo(new File(newFileName))
                        }
                        if (file.isDirectory()) {
                                normalizeFiles(file)
                        }
                }
        }

        static void main(String[] args) {
		println "aa"
                def javaVersion = System.getProperty("java.version")
                def dirName = "." // current direoctory               
                if (args.size() != 0 &&
                        (["-?", "--?", "--help"].contains(args[0]))
                ) {
                        println 'AccentRemove v0.1'
                        println "Usage:\nAccentRemove [directory]\nIf no directory is given, current directory is used."
                } else {
                        if (args.size() != 0) {
                                dirName = args[0]
                        }
                        def rootDir = new File(dirName)
                        if (!rootDir.exists() || !rootDir.isDirectory()) {
                                println dirName + " is not valid directory name!"
                                return
                        }
                        println "Searching directory: " + dirName
                        def renameFiles = getCandidates(rootDir)
                        if (renameFiles.size() == 0) {
                                println "No files with accent found!"
                                return
                        }
                        println "Following files with accent found:"
                        renameFiles.each {
                                println it[0] + " -> " + it[1]
                        }
                        def stdin = new BufferedReader(new InputStreamReader(System.in))
                        println "Proceeed with rename? [Y/-]?"
                        def result = stdin.readLine()
                        if (result.size() == 0 || result.startsWith('Y') || result.startsWith('y')) {                                
                                normalizeFiles(rootDir)
                        }
                }
        }
}
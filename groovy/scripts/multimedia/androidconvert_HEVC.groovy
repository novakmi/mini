#!/usr/bin/env groovy
/*
Copyright (c) 2016 it.novakmi@gmail.com

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
 * Convert video MP4 files from Android device (and possibly other models) in current directory
 * Dependencies: HandBrakeCLI in PATH
 */

// get all AVI files in current directory
def files = new File('.').listFiles().grep(~/(.*[Mm][Pp]4$)/)
def failedFiles = []
//println "Files $files"
files.eachWithIndex {file, i ->
        println "Processing file ${file}  (${i + 1} out of ${files.size()})"
        def lastMod = file.lastModified()
        def outputFileName = "VID_ANDROID_${new Date(lastMod).format('yyyyMMdd_HHmm')}_${file.name[file.name.lastIndexOf('_')+1..file.name.lastIndexOf('.') - 1]}_HEVC.mkv"
        def command = "HandBrakeCLI --input ${file} --output ${outputFileName}" // command, input, output
        command += " --encoder x265 -q 28.0 --turbo --encopts b-adapt=2:rc-lookahead=50" // video options
        command += " --aencoder ffaac" // audio options
        command += " --denoise --decomb --detelecine" // picture/filters options
        for (int n = 0; n < 2; n++) { // on failure repeat once
                print command
                def proc = command.execute()
                proc.consumeProcessOutput() // do not block on output
                proc.waitFor()
                println " ... ${proc.exitValue() == 0 ? 'OK' : 'FAILED'} [${proc.exitValue()}]"
                if (proc.exitValue() == 0) {
                        new File(outputFileName).setLastModified(lastMod)
                        break
                }
                if (n == 1) {
                        println "***** RETRY FAILED *****"
                        failedFiles += file.name
                        break
                }
                println "***** RETRYING *****"
        }
}
if (failedFiles.size()) {
        println "Failed to decode:"
        failedFiles.each {println it}
}

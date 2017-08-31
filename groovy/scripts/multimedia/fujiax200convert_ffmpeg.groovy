#!/usr/bin/env groovy
/*
Copyright (c) 2010 bubbles.way@gmail.com

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
 * Convert video files from Fuji AX200 (and possibly other models) in current directory
 * Dependencies: ffmpeg in PATH
 */

// get all AVI files in current directory
def files = new File('.').listFiles().grep(~/(.*[Aa][Vv][Ii]$)/)
def failedFiles = []
def h265 = false
if (this.args && this.args[0] == "x265") {
   h265 = true
}
def h2xxText = h265?"x265":"x264"
files.eachWithIndex {file, i ->
        println "Processing file ${file}  (${i + 1} out of ${files.size()}) ${h2xxText}"
        def lastMod = file.lastModified()
        def outputFileName = "fuji_ax200_${new Date(lastMod).format('yyyyMMdd_HHmm')}_${file.name[0..file.name.lastIndexOf('.') - 1]}_${h2xxText}.${h265?"mp4":"mkv"}" // mkv not working well with h265
        def command = "ffmpeg -y -i ${file}" // command, input
        if (h265) {
	  command += " -c:v libx265 -x265-params crf=23" // video options
        } else {
	  command += " -c:v libx264 -preset slow -crf 25" // video options
        }
        command += " -c:a libvo_aacenc -b:a 128k" // audio options
        //command += " --denoise --decomb --detelecine" // picture/filters options
        command += " ${outputFileName}" 
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

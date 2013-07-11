#!/usr/bin/env groovy
/*
Copyright (c) 2011 bubbles.way@gmail.com

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
 * Convert video files from ???? (adujst script) in current directory
 * Dependencies: HandBrakeCLI in PATH
 */

// get all mov files in current directory
def video_bitrate = 28
def files = new File('.').listFiles().grep(~/(.*[Mm][Oo][Vv]$)/)
files.eachWithIndex {file, i ->
        println "Processing file ${file}  (${i + 1} out of ${files.size()})"
        def lastMod = file.lastModified()
        def outputFileName = "${file.name[0 .. file.name.lastIndexOf('.')-1]}.mp4"
        def command = "HandBrakeCLI --input ${file} --output ${outputFileName}" // command, input, output
        command += " --encoder x264 --two-pass --vb ${video_bitrate}" // video options
        command += " --aencoder lame --ab 20" // audio options
        command += " --denoise strong" // picture/filters options
        println command
        def proc = command.execute()
        proc.consumeProcessOutput() // do not block on output
        proc.waitFor()
        new File(outputFileName).setLastModified(lastMod)
}

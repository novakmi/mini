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
 * Join and convert video files from Canon PowerShot A40 (and possibly other models) in current directory
 * Dependencies: mencoder in PATH
 */

// get all AVI files in current directory
def filesTimeModificationMap = [:]
new File('.').listFiles().grep(~/(.*[Aa][Vv][Ii]$)/).each {
        filesTimeModificationMap[it.lastModified()] = it
}
def filesGroupMap = [:]
def prevFileTimestamp = 0L
def groupTimeDifference = 15 * 60 * 1000L // group video files with this maximum time difference in milliseconds
def groupTimestamp = 0L
filesTimeModificationMap.keySet().sort().each {
        if (it > prevFileTimestamp + groupTimeDifference) { // new group?
                groupTimestamp = it // key is first timestamp
                filesGroupMap[groupTimestamp] = [filesTimeModificationMap[it]]
        } else {
                filesGroupMap[groupTimestamp] += filesTimeModificationMap[it]
        }
        prevFileTimestamp = it
}
def numberOfGroups = filesGroupMap.keySet().size()
filesGroupMap.eachWithIndex {group, files, i ->
        println "Processing file group ${i+1} out of $numberOfGroups"
        def command = "mencoder -fps 20 -ofps 24 -oac mp3lame -lameopts cbr:br=64 -srate 11025 -ovc xvid -xvidencopts bitrate=512"
        command += " ${files.join(' ')} -o canon_${new Date(group).format('yyyyMMdd_HHmm')}_${String.format("%03d",i+1)}.avi"
        println command
        def proc = command.execute()
        proc.consumeProcessOutput() // do not block on output
        proc.waitFor()
}

#!/usr/bin/env groovy
// get all AVI files in current directory
def files = new File('.').listFiles().grep(~/(.*[Mm][Kk][Vv]$)/)
def failedFiles = []
//println "Files $files"
files.eachWithIndex {file, i ->
        println "Processing file ${file}  (${i + 1} out of ${files.size()})"
        def date = file.name[12..19]
        def time = file.name[21..26]
        //def outputFileName = "VID_ANDROID_${new Date(lastMod).format('yyyyMMdd_HHmm')}_${file.name[file.name.indexOf('_')+1..file.name.lastIndexOf('.') - 1]}_HEVC.mkv"
        def dt = new Date().parse("yyyyMMdd_HHmmss", "${date}_${time}")
        println "$file.name -> ${dt.toString()}"        
        file.lastModified = dt.getTime()
}

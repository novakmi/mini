#!/usr/bin/env groovy
if (this.args.size() == 1) {
	def file = (this.args[0][0 .. this.args[0].lastIndexOf('.')-1])
	if (file.contains(' ')) {
		println "sapce character in input file is not yet supported!"
        } else {           
		def projectx = "projectx -demux ${file}.m2t"
		def mplex = "mplex -f8 -o ${file}.mpeg ${file}.m2v ${file}.mp2"
		println "Processing: ${file}"
		println "Demuxing with projectx command: ${projectx}"
        	def proc = projectx.execute()
        	//ProcessBuilder theBuilder = new ProcessBuilder("projectx", "-demux", "${file}.m2t")
        	//def proc = theBuilder.start()
        	proc.waitFor()
		//println proc.text
		println "Remuxing with mplex commad: $mplex"
        	proc = mplex.execute()
        	proc.waitFor()
        	println "Cleaning ${file}.m2v ${file}.m2v ${file}_log.txt"
        	proc = "rm ${file}.m2v ${file}.mp2 ${file}_log.txt".execute()
		proc.waitFor()
		println "Done!"
	}
} else {
	println "Usage: m2t2mpeg <m2t file to convert>"
}


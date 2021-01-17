#!/usr/bin/env groovy
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import groovy.util.logging.Slf4j
import org.apache.commons.io.filefilter.WildcardFileFilter

/*
Copyright (c) 2016-2021 it.novakmi@gmail.com

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
 * Batch convert video files (e.g. from Phone) using HandBrakeCLI
 * Dependencies: HandBrakeCLI in PATH
 * HandbrakeConvert.groovy -h
 */
@Grapes([
    @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.2.3'),
    @Grab(group = 'commons-io', module = 'commons-io', version = '2.8.0')
])

@Slf4j
class HandbrakeConvert {
    static def VERSION = "0.2.0"
    static def defaultPrefix = ""
    static def defaultSuffix = "_encoded"
    static def defaultExtension = "mkv"
    static def defaultProfile = "x265_22"

    static def optionsDestMkv = "--format av_mkv"
    static def optionsVideoX264 = "--encoder x264 --turbo --encopts b-adapt=2:rc-lookahead=50"
    static def optionsVideoX265 = "--encoder x265 --turbo --encopts b-adapt=2:rc-lookahead=50"
    static def optionsVideoX265Br = "--encoder x265 --encopts strong-intra-smoothing=0:rect=0:aq-mode=1"
    static def optionsAudioFfacc = "--aencoder ffaac"
    static def optionsOther = "--denoise --decomb --detelecine --crop 0:0:0:0"

    static def profiles = [
        "x264_22"     : [options: "$optionsDestMkv $optionsVideoX264 -q 22.0 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: defaultSuffix, extension: defaultExtension],
        "x264_24"     : [options: "$optionsDestMkv $optionsVideoX264 -q 24.0 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: defaultSuffix, extension: defaultExtension],
        "x264_26"     : [options: "$optionsDestMkv $optionsVideoX264 -q 26.0 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: defaultSuffix, extension: defaultExtension],
        "x265_22"     : [options: "$optionsDestMkv $optionsVideoX265 -q 22.0 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: defaultSuffix, extension: defaultExtension],
        "x265_24"     : [options: "$optionsDestMkv $optionsVideoX265 -q 24.0 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: defaultSuffix, extension: defaultExtension],
        "x265_24_w256": [options: "$optionsDestMkv $optionsVideoX265 -q 24.0 --maxWidth 256 $optionsAudioFfacc $optionsOther"], // prefix and extension can be skipped - default used
        "x265_26"     : [options: "$optionsDestMkv $optionsVideoX265 -q 26.0 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: "", extension: defaultExtension, comment:
            "For devices (phones, cameras)"],
        "x265_br4000"     : [options: "$optionsDestMkv $optionsVideoX265Br -2 -b 4000 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: defaultSuffix, extension: defaultExtension],
        "x265_br2500"     : [options: "$optionsDestMkv $optionsVideoX265Br -2 -b 2500 $optionsAudioFfacc $optionsOther", prefix: defaultPrefix, suffix: defaultSuffix, extension: defaultExtension],
    ]

    static int handbrakeConvert(inputFileName, outputFileName, handbrakeOptions, dry = false, repeat = 2) {
        log.debug("==> handbrakeConvert inputFileName={} outputFileName={} handbrakeOptions={} dry={} repeat={}",
            inputFileName, outputFileName, handbrakeOptions, dry, repeat)

        int rv = 1
        def file = new File(inputFileName)
        def lastMod = file.lastModified()
        def command = ["HandBrakeCLI", "--input", inputFileName, "--output", outputFileName]
        command.addAll(handbrakeOptions.split(" "))
        log.trace("command={}", command)
        print "${inputFileName} ==>>> ${outputFileName} ..."
        if (!dry) {
            for (int n = 0; n < repeat; n++) { // on failure repeat once
                Process proc = command.execute()
                proc.consumeProcessOutput() // do not block on output
                proc.waitFor()
                //println " ... ${proc.exitValue() == 0 ? 'OK' : 'FAILED'} [${proc.exitValue()}]"
                if (proc.exitValue() == 0) {
                    log.trace("Converted OK")
                    new File(outputFileName).setLastModified(lastMod)
                    break
                }
                log.warn("inputFileName={} conversion failed, proc.exitValue()={}", inputFileName, proc.exitValue())
                if (repeat > 1 && n == repeat - 1) {
                    log.warn("RETRY FAILED")
                    rv = 0
                    break
                }
                log.warn("RETRYING")
            }
        }
        println "done"
        log.debug("==> handbrakeConvert rv={}", rv)
        return rv
    }

    static File[] getFileList(fileArgument) {
        log.trace("==> getFileList fileArgument={}", fileArgument)
        File dir = new File(".")
        FileFilter fileFilter = new WildcardFileFilter(fileArgument)
        File[] rv = dir.listFiles((FilenameFilter) fileFilter)
        log.trace("<== getFileList rv={} rv.length={}", rv, rv.length)
        return rv
    }

    static void main(String[] args) {
        ((Logger) log).setLevel(Level.WARN)

        log.info("HandbrakeConvert started")
        def cli = new CliBuilder(
            header: "HandbrakeConvert ver. ${VERSION}",
            usage: 'HandbrakeConvert.groovy [-hl] inputFiles'
        )
        cli.expandArgumentFiles = false
        cli.with {
            h longOpt: 'help', 'Show usage information'
            l longOpt: 'log-level', args: 1, argName: "level", 'Log level to use (ALL,TRACE, DEBUG, INFO, WARN, ERROR)'
            s longOpt: 'suffix', args: 1, argName: "suffix", 'Filename suffix'
            n longOpt: "dry-run", 'Dry run (do nothing)'
            p longOpt: 'profile', args: 1, argName: "profile", 'Profile to use'
            L(longOpt: 'ls', 'List available profiles')
        }

        def dry = false
        def profileName = null, suffix = null, prefix = null, extension = null

        // add 90,180 and 270 rotation to following profiles
        ["90", "180", "270"].each { rot ->
            ["x265_26"].each { prof ->
                def name = "${prof}_rotate_$rot"
                profiles[name] = profiles[prof].clone()
                profiles[name].options += " --rotate=angle=${rot}"
                if (!profiles[name].comment) {
                    profiles[name].comment = ""
                } else {
                    profiles[name].comment += ", "
                }
                profiles[name].comment += "rotate by ${rot} degrees"
            }
        }

        def files = []
        def options = cli.parse(args)
        if (options) {
            if (options.'log-level') {
                ((Logger) log).setLevel(Level.toLevel(options.l))
            }
            if (options.h || options.L) {
                if (options.h) cli.usage()
                if (options.L) {
                    profiles.each { k, v ->
                        println "${k}  ${v}"
                    }
                }
            } else {
                if (options.n) {
                    dry = true
                    log.trace("dry-run set")
                }
                if (options.p) {
                    profileName = options.p
                    log.trace("Profile name set to ${profileName}")
                }
                if (options.s) {
                    suffix = options.s
                    log.trace("Suffix set to ${suffix}")
                }
                def extraArguments = options.arguments()
                log.trace("extraArguments={}", extraArguments)
                extraArguments.each { file ->
                    log.trace("file={}", file)
                    files.addAll(getFileList(file))
                }
                log.trace("files={}", files)

                profileName = profileName ?: defaultProfile
                def profile = profiles[profileName]
                prefix = prefix ?: (profile.prefix != null ? profile.prefix : defaultPrefix)
                suffix = suffix ?: (profile.suffix != null ? profile.suffix : defaultSuffix)
                extension = extension ?: (profile.extension ?: defaultExtension)
                log.trace("profileName={} prefix={} suffix={} extension={} dry={}",
                    profileName, prefix, suffix, extension, dry)
                log.trace("profiles={}", profile)

                if (files.size()) {
                    files.each { file ->
                        def fileNoExt = file.name.take(file.name.lastIndexOf('.'))
                        def outputFileBase = prefix + fileNoExt + suffix
                        if (new File("$outputFileBase.$extension").exists()) {
                            outputFileBase = outputFileBase + defaultSuffix
                        }
                        def outputFile = outputFileBase + ".${extension}"
                        handbrakeConvert(file.name, outputFile, profile.options, dry)
                    }
                } else {
                    println "Please specify files to convert!"
                }
            }
        }

        log.info("HandbrakeConvert ended")
    }

}

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
 *  This script converts Forex data retrieved  from http://fx.sauder.ubc.ca/data.html
 *  Converted data are appended to output file
 *  The format of forex file e.g.:

 Jul.Day YYYY/MM/DD Wdy CZK/USD
 2451548 2000/01/04 Tue 35.250
 2451549 2000/01/05 Wed 35.090
 ...

 Output example:
 CZK/USD,2000-01-04,35.250
 CZK/USD,2000-01-05,35.090
 ...

 */

if (this.args.size() == 2) {
        def isFirst = true
        def symbol = ''
        outFile = new File(this.args[1])
        new File(this.args[0]).eachLine {line ->
                if (isFirst) {
                        symbol = line.split()[3]
                        isFirst = false
                } else {
                        def vals = line.split()
                        def value = vals[3]
                        def date = Date.parse('yyyy/MM/dd', vals[1])
                        def text = "${symbol} ${date.format('yyyy-MM-dd')} ${value}"
                        //println text
                        outFile.append("${text}\n")
                }
        }
} else {
        println "Usage: fxconvert <file to convert> <output file to append>"
}

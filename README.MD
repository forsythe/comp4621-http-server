# COMP4621 HTTP Server Project
## How to start
In the command line, run `./gradlew run`.  
Then, go to [http://localhost/index.html](http://localhost/index.html).
## Required features
* Multithreading
* Support for different filetypes
    * Text: html, css, txt, java, csv
    * Image: jpg, png, gif, svg, tiff, webp
    * Audio: midi, mp3, ogg, wav, aac, weba
    * Video: avi, mpeg, ogv, flv, mp4, mov, wmv
    * Application: ppt, pdf
    * ...and more
* User friendly 404 error
* GZIP compression
    * Test with `Accept-Encoding: gzip`
* Chunked transfer encoding
    * Enabled for `HTTP/1.1`, disabled for `HTTP/1.0`
    * Reduce `ChunkedOutputStream.DEFAULT_BUFFER_SIZE` to make effect more obvious

## Extra features
* Browse directory
* Persistent connections via `Connection: keep-alive`
    * Default timeout in `HTTPSessionHandler.SO_KEEP_ALIVE_TIMEOUT_MS`
    * Test on firefox with `network.http.max-persistent-connections-per-server=1`
* Logging

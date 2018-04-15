# COMP4621 HTTP Server Project
## How to start
In the command line, run `./gradlew run`.  
Then, navigate to [localhost/index.html](http://localhost/index.html).
## Required features
* Multithreading
* Support for different filetypes
    * HTML & CSS
    * PDF
    * PPT
    * JPG & PNG
* User friendly 404 error
* GZIP compression
    * Add or remove `Accept-Encoding: gzip`
* Chunked transfer encoding
    * Enabled for `HTTP/1.1`, disabled for `HTTP/1.0`
    * Reduce `ChunkedOutputStream.DEFAULT_BUFFER_SIZE` to make effect more obvious

## Extra features
* Browse directory
* Persistent connections via `Connection: keep-alive`
    * Default timeout in `HTTPSessionHandler.SO_KEEP_ALIVE_TIMEOUT_MS`
* Logging

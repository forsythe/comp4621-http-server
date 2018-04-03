import org.slf4j.LoggerFactory;

public class HTTPServer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HTTPServer.class);

    static class Bob implements Runnable {

        @Override
        public void run() {
            log.warn("hi");
        }
    }
    public static void main (String[] args){
        log.info("Hello world");
        new Thread(new Bob()).start();
    }
}

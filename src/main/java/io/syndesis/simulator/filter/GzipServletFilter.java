package io.syndesis.simulator.filter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.servlet.GzipHttpServletResponseWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Christoph Deppisch
 */
public class GzipServletFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest filteredRequest = request;
        HttpServletResponse filteredResponse = response;

        String contentEncoding = request.getHeader(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding != null && contentEncoding.contains("gzip")) {
            filteredRequest = new GzipHttpServletRequestWrapper(request);
        }

        String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            filteredResponse = new GzipHttpServletResponseWrapper(response);
        }

        filterChain.doFilter(filteredRequest, filteredResponse);

        if (filteredResponse instanceof GzipHttpServletResponseWrapper) {
            ((GzipHttpServletResponseWrapper) filteredResponse).finish();
        }
    }

    private static class GzipHttpServletRequestWrapper extends HttpServletRequestWrapper {
        /**
         * Constructs a request adaptor wrapping the given request.
         *
         * @param request
         * @throws IllegalArgumentException if the request is null
         */
        public GzipHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            try {
                return new GzipServletInputStream(getRequest());
            } catch (ZipException e) {
                return getRequest().getInputStream();
            }
        }

        /**
         * Gzip enabled servlet input stream.
         */
        private static class GzipServletInputStream extends ServletInputStream {
            private final GZIPInputStream gzipStream;

            /**
             * Default constructor using wrapped input stream.
             *
             * @param request
             * @throws IOException
             */
            public GzipServletInputStream(ServletRequest request) throws IOException {
                super();
                gzipStream = new GZIPInputStream(request.getInputStream());
            }

            @Override
            public boolean isFinished() {
                try {
                    return gzipStream.available() == 0;
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to check gzip intput stream availability", e);
                }
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(final ReadListener readListener) {
                throw new UnsupportedOperationException("Unsupported operation");
            }

            @Override
            public int read() {
                try {
                    return gzipStream.read();
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read gzip input stream", e);
                }
            }

            @Override
            public int read(byte[] b) throws IOException {
                return gzipStream.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return gzipStream.read(b, off, len);
            }
        }
    }
}

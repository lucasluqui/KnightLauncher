package com.lucasluqui.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A single HTTP client for all the launcher's needs.
 */
public final class HttpUtil
{
  private HttpUtil ()
  {
    // empty.
  }

  /**
   * Thrown when the server answers with anything other than a 2xx status.
   */
  public static class HttpStatusException extends IOException
  {
    public final int statusCode;

    public HttpStatusException (int statusCode, URI uri)
    {
      super("HTTP " + statusCode + " for " + uri);
      this.statusCode = statusCode;
    }

    /**
     * Whether trying again could make a difference.
     * Client errors like a 404 won't go away on their own.
     */
    public boolean isRetryable ()
    {
      return statusCode >= 500 || statusCode == 408 || statusCode == 429;
    }
  }

  /**
   * Fetches the body of a GET request as text.
   */
  public static String getString (String url)
    throws IOException
  {
    return send("GET", URI.create(url));
  }

  /**
   * Sends a request without a body and returns the body of the reply as text.
   */
  public static String send (String method, URI uri)
    throws IOException
  {
    HttpRequest request = newRequest(uri)
      .method(method, HttpRequest.BodyPublishers.noBody())
      .timeout(REQUEST_TIMEOUT)
      .build();
    return checkStatus(execute(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)), uri);
  }

  /**
   * Fetches the body of a GET request as raw bytes.
   */
  public static byte[] getBytes (URI uri)
    throws IOException
  {
    HttpRequest request = newRequest(uri).GET().timeout(REQUEST_TIMEOUT).build();
    return checkStatus(execute(request, HttpResponse.BodyHandlers.ofByteArray()), uri);
  }

  /**
   * Downloads a file. It's written to a temporary file next to the target first, and only moved into
   * place once complete, so a failed download never leaves a broken file behind under the real name.
   * Gives up if the server goes quiet for longer than {@link #STALL_TIMEOUT}.
   */
  public static void download (URI uri, Path target)
    throws IOException
  {
    target = target.toAbsolutePath();
    Files.createDirectories(target.getParent());
    Path partial = target.resolveSibling(target.getFileName() + ".part");

    AtomicLong lastActivity = new AtomicLong(System.nanoTime());
    HttpResponse.BodyHandler<Path> handler = info -> {
      lastActivity.set(System.nanoTime());
      if (info.statusCode() / 100 != 2) return HttpResponse.BodySubscribers.replacing(null);
      return new ActivityTrackingSubscriber<>(
        HttpResponse.BodySubscribers.ofFile(partial,
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
        lastActivity);
    };

    HttpRequest request = newRequest(uri).GET().build();
    CompletableFuture<HttpResponse<Path>> future = CLIENT.sendAsync(request, handler);

    boolean done = false;
    try {
      HttpResponse<Path> response;
      while (true) {
        try {
          response = future.get(1, TimeUnit.SECONDS);
          break;
        } catch (TimeoutException e) {
          if (System.nanoTime() - lastActivity.get() > STALL_TIMEOUT.toNanos()) {
            throw new HttpTimeoutException("Download stalled for over " + STALL_TIMEOUT.toSeconds() + "s: " + uri);
          }
        }
      }

      if (response.body() == null) throw new HttpStatusException(response.statusCode(), uri);
      Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
      done = true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException("Download interrupted: " + uri);
    } catch (ExecutionException e) {
      throw toIOException(e.getCause());
    } finally {
      if (!done) {
        // Aborts the transfer if it's still going.
        future.cancel(true);
        try {
          Files.deleteIfExists(partial);
        } catch (IOException ignored) {
          // Might still be held open while the transfer winds down. It gets overwritten next time anyway.
        }
      }
    }
  }

  /**
   * Converts a URL into a URI, escaping characters URIs don't allow (like spaces) if needed.
   */
  public static URI toURI (URL url)
    throws IOException
  {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      try {
        return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
          url.getPath(), url.getQuery(), url.getRef());
      } catch (URISyntaxException e2) {
        throw new IOException("Invalid URL: " + url, e2);
      }
    }
  }

  /**
   * Whether the URL is something this client can fetch.
   * Anything else (like local files) is ignored.
   */
  public static boolean isHttp (URL url)
  {
    String protocol = url.getProtocol();
    return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
  }

  private static HttpRequest.Builder newRequest (URI uri)
  {
    HttpRequest.Builder builder = HttpRequest.newBuilder(uri).header("User-Agent", USER_AGENT);

    // Over plain http, trying HTTP/2 means asking the server to upgrade the connection, which some servers
    // handle badly. Over https it's negotiated safely, so it's only worth trying there.
    if ("http".equalsIgnoreCase(uri.getScheme())) builder.version(HttpClient.Version.HTTP_1_1);

    return builder;
  }

  private static <T> HttpResponse<T> execute (HttpRequest request, HttpResponse.BodyHandler<T> handler)
    throws IOException
  {
    try {
      return CLIENT.send(request, handler);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException("Request interrupted: " + request.uri());
    }
  }

  private static <T> T checkStatus (HttpResponse<T> response, URI uri)
    throws HttpStatusException
  {
    if (response.statusCode() / 100 != 2) throw new HttpStatusException(response.statusCode(), uri);
    return response.body();
  }

  private static IOException toIOException (Throwable cause)
  {
    if (cause instanceof IOException io) return io;
    return new IOException(cause);
  }

  /**
   * Passes everything through to another subscriber,
   * noting down whenever data comes in.
   */
  private static final class ActivityTrackingSubscriber<T> implements HttpResponse.BodySubscriber<T>
  {
    private final HttpResponse.BodySubscriber<T> delegate;
    private final AtomicLong lastActivity;

    ActivityTrackingSubscriber (HttpResponse.BodySubscriber<T> delegate, AtomicLong lastActivity)
    {
      this.delegate = delegate;
      this.lastActivity = lastActivity;
    }

    @Override
    public CompletionStage<T> getBody ()
    {
      return delegate.getBody();
    }

    @Override
    public void onSubscribe (Flow.Subscription subscription)
    {
      delegate.onSubscribe(subscription);
    }

    @Override
    public void onNext (List<ByteBuffer> item)
    {
      lastActivity.set(System.nanoTime());
      delegate.onNext(item);
    }

    @Override
    public void onError (Throwable throwable)
    {
      delegate.onError(throwable);
    }

    @Override
    public void onComplete ()
    {
      delegate.onComplete();
    }
  }

  /**
   * How long to wait for a connection to be established.
   */
  public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

  /**
   * How long to wait for a reply to a small request (API calls, web pages, images).
   */
  public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

  /**
   * How long a download can go without receiving anything before we give up on it.
   * Downloads have no overall time limit, as large ones on slow connections can take a long time.
   */
  public static final Duration STALL_TIMEOUT = Duration.ofSeconds(30);

  private static final HttpClient CLIENT = HttpClient.newBuilder()
    .connectTimeout(CONNECT_TIMEOUT)
    // Doesn't follow redirects from https to http, which the old URL connections didn't either.
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build();

  private static final String USER_AGENT =
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
}

package com.loopswork.loops.util;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetSocket;
import io.vertx.core.streams.WriteStream;

import java.util.Objects;

/**
 * @author liwei
 * @description 下载文件的流处理
 * @date 2019-11-22 13:30
 */
public class GatewayWriteStream implements WriteStream<Buffer> {

  private final Vertx vertx;
  private final Context context;
  private NetSocket netSocket;
  private Handler<Throwable> exceptionHandler;
  private int maxWrites = 128 * 1024;
  private long writesOutstanding;
  private Runnable closedDeferred;
  private boolean closed;
  private int lwm = maxWrites / 2;

  private Handler<Void> drainHandler;

  private Logger log;


  public GatewayWriteStream(NetSocket netSocket, Vertx vertx, String length) {
    Objects.requireNonNull(netSocket, "NetSocket");
    this.netSocket = netSocket;
    //返回响应头，来通知客户端文件大小
    String header;
    if (length == null) {
      header = "HTTP/1.0 200 \n" +
        HttpHeaders.CONTENT_TYPE.toString() + ": application/octet-stream" + " \n\n";
    } else {
      header = "HTTP/1.0 200 \n" +
        HttpHeaders.CONTENT_LENGTH.toString() + ": " + length + " \n" +
        HttpHeaders.CONTENT_TYPE.toString() + ": application/octet-stream" + " \n\n";
    }

    Buffer buffer = Buffer.buffer(header);
    this.netSocket.write(buffer);
    this.vertx = vertx;
    this.context = vertx.getOrCreateContext();
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @Override
  public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
    check();
    this.exceptionHandler = handler;
    return this;
  }

  @Override
  public WriteStream<Buffer> write(Buffer buffer) {
    return write(buffer, null);
  }

  @Override
  public synchronized WriteStream<Buffer> write(Buffer buffer, Handler<AsyncResult<Void>> handler) {
    doWrite(buffer, handler);
    return this;
  }

  @Override
  public void end() {
    netSocket.end();
  }

  @Override
  public void end(Handler<AsyncResult<Void>> handler) {
    netSocket.end(handler);
  }

  @Override
  public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
    netSocket.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return netSocket.writeQueueFull();
  }

  @Override
  public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
    check();
    this.drainHandler = handler;
    checkDrained();
    return this;
  }

  private synchronized WriteStream<Buffer> doWrite(Buffer buffer, Handler<AsyncResult<Void>> handler) {
    Objects.requireNonNull(buffer, "buffer");
    check();
    Handler<AsyncResult<Void>> wrapped = ar -> {
      if (ar.succeeded()) {
        checkContext();
        Runnable action;
        synchronized (GatewayWriteStream.this) {
          if (writesOutstanding == 0 && closedDeferred != null) {
            action = closedDeferred;
          } else {
            action = this::checkDrained;
          }
        }
        action.run();
        if (handler != null) {
          handler.handle(ar);
        }
      } else {
        if (handler != null) {
          handler.handle(ar);
        } else {
          handleException(ar.cause());
        }
      }
    };

    doWriteBuffer(buffer, buffer.length(), wrapped);

    return this;
  }

  private void doWriteBuffer(Buffer buff, long toWrite, Handler<AsyncResult<Void>> handler) {
    if (toWrite > 0) {
      synchronized (this) {
        writesOutstanding += toWrite;
      }
      writeInternal(buff, handler);
    } else {
      handler.handle(Future.succeededFuture());
    }
  }


  private void writeInternal(Buffer buff, Handler<AsyncResult<Void>> handler) {
    netSocket.write(buff, as -> {
      if (as.succeeded()) {
        synchronized (GatewayWriteStream.this) {
          writesOutstanding -= buff.getByteBuf().nioBuffer().limit();
        }
        handler.handle(Future.succeededFuture());
      }
    });
  }

  private synchronized void closeInternal(Handler<AsyncResult<Void>> handler) {
    check();

    closed = true;

    if (writesOutstanding == 0) {
      doClose(handler);
    } else {
      closedDeferred = () -> doClose(handler);
    }
  }

  private void check() {
    checkClosed();
  }

  private void checkClosed() {
    if (closed) {
      throw new IllegalStateException("File handle is closed");
    }
  }

  private void doClose(Handler<AsyncResult<Void>> handler) {
    Context handlerContext = vertx.getOrCreateContext();
    handlerContext.executeBlocking(res -> {
      netSocket.end();
      res.complete(null);

    }, handler);
  }

  private void checkContext() {
//        if (!vertx.getOrCreateContext().equals(context)) {
//            throw new IllegalStateException("AsyncFile must only be used in the context that created it, expected: "
//                    + context + " actual " + vertx.getOrCreateContext());
//        }
  }

  private synchronized void checkDrained() {
    if (drainHandler != null && writesOutstanding <= lwm) {
      Handler<Void> handler = drainHandler;
      drainHandler = null;
      handler.handle(null);
    }
  }

  private void handleException(Throwable t) {
    if (exceptionHandler != null && t instanceof Exception) {
      exceptionHandler.handle(t);
    } else {
      log.error("Unhandled exception", t);

    }
  }
}

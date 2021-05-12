package com.loopswork.loops.http.entity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Created by Codi on 2019-04-18.
 */
public class BodyBuffer {
  private ByteBuf buffer;

  public BodyBuffer() {
    this(0);
  }

  public BodyBuffer(int initialSizeHint) {
    buffer = Unpooled.unreleasableBuffer(Unpooled.buffer(initialSizeHint, Integer.MAX_VALUE));
  }

  public BodyBuffer(byte[] bytes) {
    buffer = Unpooled.unreleasableBuffer(Unpooled.buffer(bytes.length, Integer.MAX_VALUE)).writeBytes(bytes);
  }

  public BodyBuffer(String str, String enc) {
    this(str.getBytes(Charset.forName(Objects.requireNonNull(enc))));
  }

  public BodyBuffer(String str, Charset cs) {
    this(str.getBytes(cs));
  }

  public BodyBuffer(String str) {
    this(str, StandardCharsets.UTF_8);
  }

  public BodyBuffer(ByteBuf buffer) {
    this.buffer = Unpooled.unreleasableBuffer(buffer);
  }

  public String toString() {
    return buffer.toString(StandardCharsets.UTF_8);
  }

  public String toString(String enc) {
    return buffer.toString(Charset.forName(enc));
  }

  public String toString(Charset enc) {
    return buffer.toString(enc);
  }

  public byte getByte(int pos) {
    return buffer.getByte(pos);
  }

  public short getUnsignedByte(int pos) {
    return buffer.getUnsignedByte(pos);
  }

  public int getInt(int pos) {
    return buffer.getInt(pos);
  }

  public int getIntLE(int pos) {
    return buffer.getIntLE(pos);
  }

  public long getUnsignedInt(int pos) {
    return buffer.getUnsignedInt(pos);
  }

  public long getUnsignedIntLE(int pos) {
    return buffer.getUnsignedIntLE(pos);
  }

  public long getLong(int pos) {
    return buffer.getLong(pos);
  }

  public long getLongLE(int pos) {
    return buffer.getLongLE(pos);
  }

  public double getDouble(int pos) {
    return buffer.getDouble(pos);
  }

  public float getFloat(int pos) {
    return buffer.getFloat(pos);
  }

  public short getShort(int pos) {
    return buffer.getShort(pos);
  }

  public short getShortLE(int pos) {
    return buffer.getShortLE(pos);
  }

  public int getUnsignedShort(int pos) {
    return buffer.getUnsignedShort(pos);
  }

  public int getUnsignedShortLE(int pos) {
    return buffer.getUnsignedShortLE(pos);
  }

  public int getMedium(int pos) {
    return buffer.getMedium(pos);
  }

  public int getMediumLE(int pos) {
    return buffer.getMediumLE(pos);
  }

  public int getUnsignedMedium(int pos) {
    return buffer.getUnsignedMedium(pos);
  }

  public int getUnsignedMediumLE(int pos) {
    return buffer.getUnsignedMediumLE(pos);
  }

  public byte[] getBytes() {
    byte[] arr = new byte[buffer.writerIndex()];
    buffer.getBytes(0, arr);
    return arr;
  }

  public byte[] getBytes(int start, int end) {
    byte[] arr = new byte[end - start];
    buffer.getBytes(start, arr, 0, end - start);
    return arr;
  }

  public BodyBuffer getBytes(byte[] dst) {
    return getBytes(dst, 0);
  }

  public BodyBuffer getBytes(byte[] dst, int dstIndex) {
    return getBytes(0, buffer.writerIndex(), dst, dstIndex);
  }

  public BodyBuffer getBytes(int start, int end, byte[] dst) {
    return getBytes(start, end, dst, 0);
  }

  public BodyBuffer getBytes(int start, int end, byte[] dst, int dstIndex) {
    buffer.getBytes(start, dst, dstIndex, end - start);
    return this;
  }

  public BodyBuffer getBuffer(int start, int end) {
    return new BodyBuffer(getBytes(start, end));
  }

  public String getString(int start, int end, String enc) {
    byte[] bytes = getBytes(start, end);
    Charset cs = Charset.forName(enc);
    return new String(bytes, cs);
  }

  public String getString(int start, int end) {
    byte[] bytes = getBytes(start, end);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public BodyBuffer appendBuffer(BodyBuffer buff) {
    buffer.writeBytes(buff.getByteBuf());
    return this;
  }

  public BodyBuffer appendBuffer(BodyBuffer buff, int offset, int len) {
    ByteBuf byteBuf = buff.getByteBuf();
    int from = byteBuf.readerIndex() + offset;
    buffer.writeBytes(byteBuf, from, len);
    return this;
  }

  public BodyBuffer appendBytes(byte[] bytes) {
    buffer.writeBytes(bytes);
    return this;
  }

  public BodyBuffer appendBytes(byte[] bytes, int offset, int len) {
    buffer.writeBytes(bytes, offset, len);
    return this;
  }

  public BodyBuffer appendByte(byte b) {
    buffer.writeByte(b);
    return this;
  }

  public BodyBuffer appendUnsignedByte(short b) {
    buffer.writeByte(b);
    return this;
  }

  public BodyBuffer appendInt(int i) {
    buffer.writeInt(i);
    return this;
  }

  public BodyBuffer appendIntLE(int i) {
    buffer.writeIntLE(i);
    return this;
  }

  public BodyBuffer appendUnsignedInt(long i) {
    buffer.writeInt((int) i);
    return this;
  }

  public BodyBuffer appendUnsignedIntLE(long i) {
    buffer.writeIntLE((int) i);
    return this;
  }

  public BodyBuffer appendMedium(int i) {
    buffer.writeMedium(i);
    return this;
  }

  public BodyBuffer appendMediumLE(int i) {
    buffer.writeMediumLE(i);
    return this;
  }

  public BodyBuffer appendLong(long l) {
    buffer.writeLong(l);
    return this;
  }

  public BodyBuffer appendLongLE(long l) {
    buffer.writeLongLE(l);
    return this;
  }

  public BodyBuffer appendShort(short s) {
    buffer.writeShort(s);
    return this;
  }

  public BodyBuffer appendShortLE(short s) {
    buffer.writeShortLE(s);
    return this;
  }

  public BodyBuffer appendUnsignedShort(int s) {
    buffer.writeShort(s);
    return this;
  }

  public BodyBuffer appendUnsignedShortLE(int s) {
    buffer.writeShortLE(s);
    return this;
  }

  public BodyBuffer appendFloat(float f) {
    buffer.writeFloat(f);
    return this;
  }

  public BodyBuffer appendDouble(double d) {
    buffer.writeDouble(d);
    return this;
  }

  public BodyBuffer appendString(String str, String enc) {
    return append(str, Charset.forName(Objects.requireNonNull(enc)));
  }

  public BodyBuffer appendString(String str) {
    return append(str, CharsetUtil.UTF_8);
  }

  public BodyBuffer setByte(int pos, byte b) {
    ensureWritable(pos, 1);
    buffer.setByte(pos, b);
    return this;
  }

  public BodyBuffer setUnsignedByte(int pos, short b) {
    ensureWritable(pos, 1);
    buffer.setByte(pos, b);
    return this;
  }

  public BodyBuffer setInt(int pos, int i) {
    ensureWritable(pos, 4);
    buffer.setInt(pos, i);
    return this;
  }

  public BodyBuffer setIntLE(int pos, int i) {
    ensureWritable(pos, 4);
    buffer.setIntLE(pos, i);
    return this;
  }

  public BodyBuffer setUnsignedInt(int pos, long i) {
    ensureWritable(pos, 4);
    buffer.setInt(pos, (int) i);
    return this;
  }

  public BodyBuffer setUnsignedIntLE(int pos, long i) {
    ensureWritable(pos, 4);
    buffer.setIntLE(pos, (int) i);
    return this;
  }

  public BodyBuffer setMedium(int pos, int i) {
    ensureWritable(pos, 3);
    buffer.setMedium(pos, i);
    return this;
  }

  public BodyBuffer setMediumLE(int pos, int i) {
    ensureWritable(pos, 3);
    buffer.setMediumLE(pos, i);
    return this;
  }

  public BodyBuffer setLong(int pos, long l) {
    ensureWritable(pos, 8);
    buffer.setLong(pos, l);
    return this;
  }

  public BodyBuffer setLongLE(int pos, long l) {
    ensureWritable(pos, 8);
    buffer.setLongLE(pos, l);
    return this;
  }

  public BodyBuffer setDouble(int pos, double d) {
    ensureWritable(pos, 8);
    buffer.setDouble(pos, d);
    return this;
  }

  public BodyBuffer setFloat(int pos, float f) {
    ensureWritable(pos, 4);
    buffer.setFloat(pos, f);
    return this;
  }

  public BodyBuffer setShort(int pos, short s) {
    ensureWritable(pos, 2);
    buffer.setShort(pos, s);
    return this;
  }

  public BodyBuffer setShortLE(int pos, short s) {
    ensureWritable(pos, 2);
    buffer.setShortLE(pos, s);
    return this;
  }

  public BodyBuffer setUnsignedShort(int pos, int s) {
    ensureWritable(pos, 2);
    buffer.setShort(pos, s);
    return this;
  }

  public BodyBuffer setUnsignedShortLE(int pos, int s) {
    ensureWritable(pos, 2);
    buffer.setShortLE(pos, s);
    return this;
  }

  public BodyBuffer setBuffer(int pos, BodyBuffer b) {
    ensureWritable(pos, b.length());
    buffer.setBytes(pos, b.getByteBuf());
    return this;
  }

  public BodyBuffer setBuffer(int pos, BodyBuffer b, int offset, int len) {
    ensureWritable(pos, len);
    ByteBuf byteBuf = b.getByteBuf();
    buffer.setBytes(pos, byteBuf, byteBuf.readerIndex() + offset, len);
    return this;
  }

  public BodyBuffer setBytes(int pos, ByteBuffer b) {
    ensureWritable(pos, b.limit());
    buffer.setBytes(pos, b);
    return this;
  }

  public BodyBuffer setBytes(int pos, byte[] b) {
    ensureWritable(pos, b.length);
    buffer.setBytes(pos, b);
    return this;
  }

  public BodyBuffer setBytes(int pos, byte[] b, int offset, int len) {
    ensureWritable(pos, len);
    buffer.setBytes(pos, b, offset, len);
    return this;
  }

  public BodyBuffer setString(int pos, String str) {
    return setBytes(pos, str, CharsetUtil.UTF_8);
  }

  public BodyBuffer setString(int pos, String str, String enc) {
    return setBytes(pos, str, Charset.forName(enc));
  }

  public int length() {
    return buffer.writerIndex();
  }

  public BodyBuffer copy() {
    return new BodyBuffer(buffer.copy());
  }

  public BodyBuffer slice() {
    return new BodyBuffer(buffer.slice());
  }

  public BodyBuffer slice(int start, int end) {
    return new BodyBuffer(buffer.slice(start, end - start));
  }

  public ByteBuf getByteBuf() {
    // Return a duplicate so the Buffer can be written multiple times.
    // See #648
    return buffer.duplicate();
  }

  private BodyBuffer append(String str, Charset charset) {
    byte[] bytes = str.getBytes(charset);
    buffer.writeBytes(bytes);
    return this;
  }

  private BodyBuffer setBytes(int pos, String str, Charset charset) {
    byte[] bytes = str.getBytes(charset);
    ensureWritable(pos, bytes.length);
    buffer.setBytes(pos, bytes);
    return this;
  }

  private void ensureWritable(int pos, int len) {
    int ni = pos + len;
    int cap = buffer.capacity();
    int over = ni - cap;
    if (over > 0) {
      buffer.writerIndex(cap);
      buffer.ensureWritable(over);
    }
    //We have to make sure that the writerindex is always positioned on the last bit of data set in the buffer
    if (ni > buffer.writerIndex()) {
      buffer.writerIndex(ni);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BodyBuffer buffer1 = (BodyBuffer) o;
    return Objects.equals(buffer, buffer1.buffer);
  }

  @Override
  public int hashCode() {
    return buffer != null ? buffer.hashCode() : 0;
  }

  public void writeToBuffer(BodyBuffer buff) {
    buff.appendInt(this.length());
    buff.appendBuffer(this);
  }

  public int readFromBuffer(int pos, BodyBuffer buffer) {
    int len = buffer.getInt(pos);
    BodyBuffer b = buffer.getBuffer(pos + 4, pos + 4 + len);
    this.buffer = b.getByteBuf();
    return pos + 4 + len;
  }
}
